package com.hanita.core

import com.hanita.core.partner.PartnerCard
import com.hanita.core.partner.PartnerContent
import com.hanita.core.partner.PartnerDay
import com.hanita.core.partner.PartnerDay.Event
import com.hanita.core.partner.PartnerDay.Phase
import com.hanita.core.partner.PartnerLink
import com.hanita.core.partner.PartnerLink.Opened
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.SecureRandom
import java.time.LocalDate

class PartnerLinkTest {

    private val start = LocalDate.of(2026, 11, 1)

    private fun card(
        issued: Long = 1_000L,
        lastStart: LocalDate? = start,
        shareFertile: Boolean = false,
        paused: Boolean = false,
        revoked: Boolean = false,
        note: String = "شکلات تلخ دوست دارم"
    ) = PartnerCard.create(issued, "هانیه", lastStart, 28, 5, shareFertile, paused, revoked, note)

    @Test
    fun cardRoundTripsThroughBytes() {
        val c = card(shareFertile = true)
        assertEquals(c, PartnerCard.fromBytes(c.toBytes()))
        val empty = card(lastStart = null, note = "")
        assertEquals(empty, PartnerCard.fromBytes(empty.toBytes()))
    }

    @Test
    fun pausedAndRevokedCardsCarryNoCycleData() {
        val paused = card(paused = true, shareFertile = true)
        assertNull(paused.lastPeriodStart)
        assertFalse(paused.shareFertile)
        assertEquals("", paused.note)
        assertFalse(paused.hasCycleData)
        val revoked = card(revoked = true, paused = true)
        assertTrue(revoked.revoked)
        assertFalse(revoked.paused)
        assertNull(revoked.lastPeriodStart)
    }

    @Test
    fun cardValuesAreClamped() {
        val c = PartnerCard.create(1, "x".repeat(80), start, 90, 30, false, note = "n".repeat(500))
        assertEquals(PartnerCard.MAX_NAME_LENGTH, c.ownerName.length)
        assertEquals(PartnerCard.MAX_NOTE_LENGTH, c.note.length)
        assertEquals(28, c.cycleLength)
        assertEquals(10, c.periodLength)
    }

    @Test
    fun damagedBytesAreRejected() {
        val bytes = card().toBytes()
        assertNull(PartnerCard.fromBytes(bytes.copyOf(bytes.size - 3)))
        assertNull(PartnerCard.fromBytes(bytes + byteArrayOf(1)))
        assertNull(PartnerCard.fromBytes(byteArrayOf(9) + bytes.copyOfRange(1, bytes.size)))
        assertNull(PartnerCard.fromBytes(ByteArray(0)))
    }

    @Test
    fun scannedPairingOpensAndUpdatesFollow() {
        val pairing = PartnerLink.Pairing.create(SecureRandom())
        val qr = PartnerLink.pairingText(pairing, card())
        val opened = PartnerLink.open(qr, current = null, fromScan = true)
        assertTrue(opened is Opened.NewPairing)
        opened as Opened.NewPairing
        assertEquals(card(), opened.card)
        assertEquals(pairing.pairId, opened.pairing.pairId)
        assertEquals(pairing.verificationCode(), opened.pairing.verificationCode())
        assertEquals(4, pairing.verificationCode().length)

        val update = card(issued = 2_000L, lastStart = start.plusDays(29))
        val text = PartnerLink.updateText(pairing, update)
        val received = PartnerLink.open(text, opened.pairing, fromScan = false)
        assertTrue(received is Opened.Update)
        assertEquals(update, (received as Opened.Update).card)
    }

    @Test
    fun pairingIsAcceptedOnlyFromTheCamera() {
        val pairing = PartnerLink.Pairing.create()
        val qr = PartnerLink.pairingText(pairing, card())
        assertTrue(PartnerLink.open("سلام $qr", null, fromScan = false) is Opened.PairingNeedsScan)
    }

    @Test
    fun updatesAreFoundInsideMessagesAndLinks() {
        val pairing = PartnerLink.Pairing.create()
        val text = PartnerLink.updateText(pairing, card())
        val message = "💌 به‌روزرسانی هانیتا:\n${PartnerLink.updateLink(text)}\nبا GentleMan باز کن."
        val opened = PartnerLink.open(message, pairing, fromScan = false)
        assertTrue(opened is Opened.Update)
    }

    @Test
    fun foreignTamperedAndEarlyUpdatesAreRejected() {
        val mine = PartnerLink.Pairing.create()
        val other = PartnerLink.Pairing.create()
        val text = PartnerLink.updateText(mine, card())

        assertTrue(PartnerLink.open(text, other, false) is Opened.WrongPair)
        assertTrue(PartnerLink.open(text, null, false) is Opened.NotPaired)
        assertTrue(PartnerLink.open("hello", mine, false) is Opened.Invalid)

        // Flip one character of the sealed card: AES-GCM must refuse it.
        val i = text.length - 10
        val flipped = text.substring(0, i) + (if (text[i] == 'A') 'B' else 'A') + text.substring(i + 1)
        assertTrue(PartnerLink.open(flipped, mine, false) is Opened.Invalid)

        // The same key with a forged pair id or a pairing payload replayed as an update fails too.
        val sealedFromPairing = PartnerLink.pairingText(mine, card()).substringAfterLast('.')
        assertTrue(PartnerLink.open("HNP1.U.${mine.pairId}.$sealedFromPairing", mine, false) is Opened.Invalid)
    }

    @Test
    fun eachUpdateIsEncryptedDifferently() {
        val pairing = PartnerLink.Pairing.create()
        assertNotEquals(PartnerLink.updateText(pairing, card()), PartnerLink.updateText(pairing, card()))
    }

    @Test
    fun storedPairingRestores() {
        val pairing = PartnerLink.Pairing.create()
        val restored = PartnerLink.Pairing.restore(pairing.pairId, pairing.keyText())!!
        assertTrue(restored.key.contentEquals(pairing.key))
        assertNull(PartnerLink.Pairing.restore(pairing.pairId, "short"))
        assertNull(PartnerLink.Pairing.restore(null, pairing.keyText()))
    }

    @Test
    fun linkFitsInAQrCode() {
        val pairing = PartnerLink.Pairing.create()
        val big = PartnerCard.create(1, "ن".repeat(40), start, 28, 5, true, note = "پ".repeat(200))
        // Byte-mode QR at level M holds 2331 bytes; stay far below for easy scanning.
        assertTrue(PartnerLink.pairingText(pairing, big).length < 900)
    }

    @Test
    fun phasesFollowTheHanitaModel() {
        val c = card()
        assertEquals(Phase.PERIOD, PartnerDay.today(c, start).phase)
        assertEquals(Phase.PERIOD, PartnerDay.today(c, start.plusDays(4)).phase)
        assertEquals(Phase.AFTER_PERIOD, PartnerDay.today(c, start.plusDays(6)).phase)
        assertEquals(Phase.MID_CYCLE, PartnerDay.today(c, start.plusDays(13)).phase)
        assertEquals(Phase.LUTEAL, PartnerDay.today(c, start.plusDays(18)).phase)
        assertEquals(Phase.PMS, PartnerDay.today(c, start.plusDays(24)).phase)
        val due = PartnerDay.today(c, start.plusDays(28))
        assertEquals(Phase.PERIOD_DUE, due.phase)
        assertEquals(Phase.AWAITING_UPDATE, PartnerDay.today(c, start.plusDays(30)).phase)
        // After the waiting window the projection continues into the next cycle.
        assertEquals(Phase.LUTEAL, PartnerDay.today(c, start.plusDays(28 + 16)).phase)
        assertEquals(Phase.STALE, PartnerDay.today(c, start.plusDays(28 * 3)).phase)
    }

    @Test
    fun fertileDaysOnlyWhenShared() {
        val day = start.plusDays(11)
        assertFalse(PartnerDay.today(card(), day).fertile)
        assertTrue(PartnerDay.today(card(shareFertile = true), day).fertile)
        val all = Event.values().toSet()
        assertTrue(PartnerDay.upcoming(card(), start, all, 2).none { it.event == Event.FERTILE_START })
        assertTrue(PartnerDay.upcoming(card(shareFertile = true), start, all, 2).any { it.event == Event.FERTILE_START })
    }

    @Test
    fun pausedRevokedAndEmptyCardsShowNoCycle() {
        assertEquals(Phase.PAUSED, PartnerDay.today(card(paused = true), start).phase)
        assertEquals(Phase.NO_DATA, PartnerDay.today(card(revoked = true), start).phase)
        assertEquals(Phase.NO_DATA, PartnerDay.today(card(lastStart = null), start).phase)
        assertEquals(Phase.NO_DATA, PartnerDay.today(null, start).phase)
        assertTrue(PartnerDay.upcoming(card(paused = true), start, Event.values().toSet(), 2).isEmpty())
    }

    @Test
    fun upcomingEventsMatchTheCycle() {
        val all = setOf(Event.PERIOD_SOON, Event.PERIOD_START, Event.PMS_START)
        val list = PartnerDay.upcoming(card(), start.plusDays(1), all, 2)
        assertEquals(listOf(Event.PMS_START, Event.PERIOD_SOON, Event.PERIOD_START), list.map { it.event })
        assertEquals(start.plusDays(23), list[0].date)
        assertEquals(start.plusDays(26), list[1].date)
        assertEquals(start.plusDays(28), list[2].date)
        assertEquals(listOf(Event.PERIOD_SOON), PartnerDay.eventsOn(card(), start.plusDays(26), all, 2))
        assertEquals(listOf(Event.PERIOD_START), PartnerDay.eventsOn(card(), start.plusDays(28), all, 2))
        // No guesses while an update is awaited.
        assertTrue(PartnerDay.upcoming(card(), start.plusDays(31), all, 2).isEmpty())
    }

    @Test
    fun everyPhaseHasTipsAndMessages() {
        for (phase in Phase.values()) {
            val info = PartnerContent.phaseInfo(phase)
            assertTrue(phase.name, info.tips.isNotEmpty())
            assertTrue(phase.name, info.messages.isNotEmpty())
            for (m in info.messages) {
                assertFalse(PartnerContent.personalize(m, "").contains("{name}"))
                assertFalse(PartnerContent.personalize(m, "هانیه").contains("{name}"))
            }
        }
        assertFalse(PartnerContent.phaseInfo(Phase.MID_CYCLE, false).tips.any { it.contains("بارداری") })
        assertTrue(PartnerContent.phaseInfo(Phase.MID_CYCLE, true).tips.any { it.contains("بارداری") })
        for (event in Event.values()) {
            val text = PartnerContent.notificationText(event, "هانیه", 2)
            assertFalse(text.contains("پریود"))
            assertFalse(text.contains("باروری"))
        }
    }
}
