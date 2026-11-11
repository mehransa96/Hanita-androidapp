package com.example.tools

import android.content.Context
import android.content.SharedPreferences
import com.example.domain.CyclePredictor
import com.hanita.core.partner.PartnerCard
import com.hanita.core.partner.PartnerLink
import java.time.LocalDate

/**
 * The link to the partner app ("همراه هانیتا"), stored only on this phone.
 *
 * Holds the shared key of the pairing and the owner's sharing choices. Nothing is sent
 * anywhere by the app itself: every update is shown as a QR code or handed to a messenger
 * only when the owner asks for it.
 */
class CompanionLinkStore(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    val pairing: PartnerLink.Pairing?
        get() = PartnerLink.Pairing.restore(prefs.getString(KEY_PAIR_ID, null), prefs.getString(KEY_KEY, null))

    val isPaired: Boolean get() = pairing != null

    /** Starts a new pairing (any old one stops working) and returns it. */
    fun newPairing(): PartnerLink.Pairing {
        val p = PartnerLink.Pairing.create()
        prefs.edit()
            .putString(KEY_PAIR_ID, p.pairId)
            .putString(KEY_KEY, p.keyText())
            .remove(KEY_LAST_SENT_AT)
            .remove(KEY_LAST_SENT_CONTENT)
            .apply()
        return p
    }

    var shareFertile: Boolean
        get() = prefs.getBoolean(KEY_FERTILE, false)
        set(value) = prefs.edit().putBoolean(KEY_FERTILE, value).apply()

    var paused: Boolean
        get() = prefs.getBoolean(KEY_PAUSED, false)
        set(value) = prefs.edit().putBoolean(KEY_PAUSED, value).apply()

    var note: String
        get() = prefs.getString(KEY_NOTE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_NOTE, value.take(PartnerCard.MAX_NOTE_LENGTH)).apply()

    /** When the last card was shown or shared (epoch millis), 0 if never. */
    val lastSentAt: Long get() = prefs.getLong(KEY_LAST_SENT_AT, 0L)

    /** True when the card would differ from the last one the partner got. */
    fun hasUnsentChanges(card: PartnerCard): Boolean =
        prefs.getString(KEY_LAST_SENT_CONTENT, null) != card.contentKey()

    fun markSent(card: PartnerCard, now: Long = System.currentTimeMillis()) {
        prefs.edit().putLong(KEY_LAST_SENT_AT, now).putString(KEY_LAST_SENT_CONTENT, card.contentKey()).apply()
    }

    /** Forgets the pairing and every sharing choice. */
    fun clear() {
        prefs.edit().clear().apply()
    }

    /** The card for the current data and choices. */
    fun card(
        ownerName: String,
        lastPeriodStart: LocalDate?,
        cycleLength: Int,
        periodLength: Int,
        revoked: Boolean = false,
        note: String = this.note,
        now: Long = System.currentTimeMillis()
    ): PartnerCard = PartnerCard.create(
        issuedAtMillis = now,
        ownerName = ownerName,
        lastPeriodStart = lastPeriodStart,
        cycleLength = if (cycleLength > 0) cycleLength else CyclePredictor.DEFAULT_CYCLE_LENGTH,
        periodLength = periodLength,
        shareFertile = shareFertile,
        paused = paused,
        revoked = revoked,
        note = note
    )

    companion object {
        const val NAME = "companion_link"
        private const val KEY_PAIR_ID = "pair_id"
        private const val KEY_KEY = "pair_key"
        private const val KEY_FERTILE = "share_fertile"
        private const val KEY_PAUSED = "paused"
        private const val KEY_NOTE = "note"
        private const val KEY_LAST_SENT_AT = "last_sent_at"
        private const val KEY_LAST_SENT_CONTENT = "last_sent_content"
    }
}
