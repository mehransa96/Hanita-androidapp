package com.hanita.core.partner

import com.example.domain.CyclePredictor
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.time.LocalDate

/**
 * The only data Hanita ever shares with the partner app: a tiny "cycle card".
 *
 * It holds the first day of the last period, the average cycle and period length, and the
 * owner's choices. Symptoms, moods, notes, weight, medicines or any history are never part of
 * it. The partner app projects the phases from these numbers on its own, so a new card is only
 * needed when a new period is logged or a setting changes.
 *
 * When [paused] or [revoked] is set the card carries no cycle data at all.
 */
data class PartnerCard(
    /** When the card was made (epoch millis). The partner app ignores cards older than the one it has. */
    val issuedAtMillis: Long,
    /** The name the owner wants the partner to see. */
    val ownerName: String,
    val lastPeriodStart: LocalDate?,
    val cycleLength: Int,
    val periodLength: Int,
    /** Fertile-window days are shown to the partner only when the owner turns this on. */
    val shareFertile: Boolean,
    /** Sharing is on hold: the partner sees no cycle data until a new card arrives. */
    val paused: Boolean = false,
    /** The owner disconnected: the partner app deletes the pairing and everything it stored. */
    val revoked: Boolean = false,
    /** An optional short note to the partner ("این هفته شکلات تلخ دوست دارم"). */
    val note: String = ""
) {
    val hasCycleData: Boolean get() = lastPeriodStart != null && !paused && !revoked

    /** Everything except the timestamp: equal keys mean nothing changed since the last card. */
    fun contentKey(): String =
        listOf(ownerName, lastPeriodStart, cycleLength, periodLength, shareFertile, paused, revoked, note).joinToString("|")

    fun toBytes(): ByteArray {
        val bytes = ByteArrayOutputStream()
        DataOutputStream(bytes).use { out ->
            out.writeByte(FORMAT_VERSION)
            out.writeLong(issuedAtMillis)
            out.writeUTF(ownerName)
            out.writeLong(lastPeriodStart?.toEpochDay() ?: NO_DATE)
            out.writeByte(cycleLength)
            out.writeByte(periodLength)
            var flags = 0
            if (shareFertile) flags = flags or FLAG_FERTILE
            if (paused) flags = flags or FLAG_PAUSED
            if (revoked) flags = flags or FLAG_REVOKED
            out.writeByte(flags)
            out.writeUTF(note)
        }
        return bytes.toByteArray()
    }

    companion object {
        const val FORMAT_VERSION = 1
        const val MAX_NAME_LENGTH = 30
        const val MAX_NOTE_LENGTH = 100
        private const val NO_DATE = Long.MIN_VALUE
        private const val FLAG_FERTILE = 1
        private const val FLAG_PAUSED = 2
        private const val FLAG_REVOKED = 4

        /**
         * Builds a card with every value cleaned and clamped. Paused and revoked cards drop the
         * cycle data, so nothing is shared while sharing is off.
         */
        fun create(
            issuedAtMillis: Long,
            ownerName: String,
            lastPeriodStart: LocalDate?,
            cycleLength: Int,
            periodLength: Int,
            shareFertile: Boolean,
            paused: Boolean = false,
            revoked: Boolean = false,
            note: String = ""
        ): PartnerCard {
            val share = !paused && !revoked
            return PartnerCard(
                issuedAtMillis = issuedAtMillis,
                ownerName = ownerName.trim().take(MAX_NAME_LENGTH),
                lastPeriodStart = if (share) lastPeriodStart else null,
                cycleLength = if (share) CyclePredictor.clampCycleLength(cycleLength) else CyclePredictor.DEFAULT_CYCLE_LENGTH,
                periodLength = if (share) periodLength.coerceIn(2, 10) else CyclePredictor.DEFAULT_PERIOD_LENGTH,
                shareFertile = share && shareFertile,
                paused = paused && !revoked,
                revoked = revoked,
                note = if (share) note.trim().take(MAX_NOTE_LENGTH) else ""
            )
        }

        /** Null when the bytes are not a valid card of a known format. */
        fun fromBytes(bytes: ByteArray): PartnerCard? = try {
            DataInputStream(ByteArrayInputStream(bytes)).use { input ->
                if (input.readUnsignedByte() != FORMAT_VERSION) return null
                val issued = input.readLong()
                val name = input.readUTF()
                val epochDay = input.readLong()
                val cycle = input.readUnsignedByte()
                val period = input.readUnsignedByte()
                val flags = input.readUnsignedByte()
                val note = input.readUTF()
                if (input.read() != -1) return null // trailing garbage
                val start = if (epochDay == NO_DATE) null
                else if (epochDay in MIN_EPOCH_DAY..MAX_EPOCH_DAY) LocalDate.ofEpochDay(epochDay)
                else return null
                create(
                    issuedAtMillis = issued,
                    ownerName = name,
                    lastPeriodStart = start,
                    cycleLength = cycle,
                    periodLength = period,
                    shareFertile = flags and FLAG_FERTILE != 0,
                    paused = flags and FLAG_PAUSED != 0,
                    revoked = flags and FLAG_REVOKED != 0,
                    note = note
                )
            }
        } catch (e: IOException) {
            null
        }

        // 1970-01-01 .. 2200-01-01: anything else is corrupt.
        private const val MIN_EPOCH_DAY = 0L
        private const val MAX_EPOCH_DAY = 84006L
    }
}
