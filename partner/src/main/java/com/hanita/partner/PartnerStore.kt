package com.hanita.partner

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import com.hanita.core.gentleman.ImportantDate
import com.hanita.core.gentleman.Status
import com.hanita.core.partner.PartnerCard
import com.hanita.core.partner.PartnerDay
import com.hanita.core.partner.PartnerLink

/** Everything the partner app keeps, only on this phone (backups are turned off). */
class PartnerStore(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    val pairing: PartnerLink.Pairing?
        get() = PartnerLink.Pairing.restore(prefs.getString(KEY_PAIR_ID, null), prefs.getString(KEY_KEY, null))

    val card: PartnerCard?
        get() = prefs.getString(KEY_CARD, null)?.let { text ->
            try {
                PartnerCard.fromBytes(Base64.decode(text, Base64.NO_WRAP))
            } catch (e: IllegalArgumentException) {
                null
            }
        }

    /** When the current card arrived on this phone (epoch millis). */
    val receivedAt: Long get() = prefs.getLong(KEY_RECEIVED_AT, 0L)

    sealed class Result {
        /** Paired for the first time (or again); show the verification code. */
        class Paired(val code: String, val ownerName: String) : Result()
        class Updated(val ownerName: String) : Result()
        /** The owner disconnected: the link and her data were deleted. */
        object Disconnected : Result()
        /** This card is older than the one already here. */
        object Older : Result()
        object PairingNeedsScan : Result()
        object WrongPair : Result()
        object NotPaired : Result()
        object Invalid : Result()
    }

    /** Opens a scanned or received text and stores what it carries. */
    fun receive(text: String?, fromScan: Boolean, now: Long = System.currentTimeMillis()): Result =
        when (val opened = PartnerLink.open(text, pairing, fromScan)) {
            is PartnerLink.Opened.NewPairing -> {
                prefs.edit()
                    .putString(KEY_PAIR_ID, opened.pairing.pairId)
                    .putString(KEY_KEY, opened.pairing.keyText())
                    .apply()
                saveCard(opened.card, now)
                Result.Paired(opened.pairing.verificationCode(), opened.card.ownerName)
            }
            is PartnerLink.Opened.Update -> {
                val old = card
                when {
                    opened.card.revoked -> {
                        clearLink()
                        Result.Disconnected
                    }
                    old != null && opened.card.issuedAtMillis < old.issuedAtMillis -> Result.Older
                    else -> {
                        saveCard(opened.card, now)
                        Result.Updated(opened.card.ownerName)
                    }
                }
            }
            PartnerLink.Opened.PairingNeedsScan -> Result.PairingNeedsScan
            PartnerLink.Opened.WrongPair -> Result.WrongPair
            PartnerLink.Opened.NotPaired -> Result.NotPaired
            PartnerLink.Opened.Invalid -> Result.Invalid
        }

    private fun saveCard(card: PartnerCard, now: Long) {
        prefs.edit()
            .putString(KEY_CARD, Base64.encodeToString(card.toBytes(), Base64.NO_WRAP))
            .putLong(KEY_RECEIVED_AT, now)
            .apply()
    }

    // ---- Reminders ----

    var reminderEvents: Set<PartnerDay.Event>
        get() {
            val stored = prefs.getStringSet(KEY_EVENTS, null)
                ?: return setOf(PartnerDay.Event.PERIOD_SOON, PartnerDay.Event.PERIOD_START, PartnerDay.Event.PMS_START)
            return stored.mapNotNull { PartnerDay.Event.fromCode(it) }.toSet()
        }
        set(value) = prefs.edit().putStringSet(KEY_EVENTS, value.map { it.code }.toSet()).apply()

    var remindersEnabled: Boolean
        get() = prefs.getBoolean(KEY_REMINDERS_ON, true)
        set(value) = prefs.edit().putBoolean(KEY_REMINDERS_ON, value).apply()

    var daysBeforePeriod: Int
        get() = prefs.getInt(KEY_DAYS_BEFORE, 2).coerceIn(1, 5)
        set(value) = prefs.edit().putInt(KEY_DAYS_BEFORE, value.coerceIn(1, 5)).apply()

    var dailyTip: Boolean
        get() = prefs.getBoolean(KEY_DAILY_TIP, true)
        set(value) = prefs.edit().putBoolean(KEY_DAILY_TIP, value).apply()

    /** Minutes after midnight for the daily reminder check (default 09:00). */
    var reminderMinuteOfDay: Int
        get() = prefs.getInt(KEY_TIME, 9 * 60).coerceIn(0, 24 * 60 - 1)
        set(value) = prefs.edit().putInt(KEY_TIME, value.coerceIn(0, 24 * 60 - 1)).apply()

    // ---- The partner's own ready messages ----

    var customMessages: List<String>
        get() = prefs.getString(KEY_MESSAGES, null)
            ?.split(SEPARATOR)?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        set(value) = prefs.edit().putString(
            KEY_MESSAGES,
            value.map { it.replace(SEPARATOR, ' ').trim().take(MAX_MESSAGE_LENGTH) }
                .filter { it.isNotEmpty() }.take(MAX_MESSAGES).joinToString(SEPARATOR.toString())
        ).apply()

    // ---- Profile ----

    /** Single or married; null until the user chooses on the welcome screen. */
    var status: Status?
        get() = Status.fromCode(prefs.getString(KEY_STATUS, null))
        set(value) = prefs.edit().putString(KEY_STATUS, value?.code).apply()

    var userName: String
        get() = prefs.getString(KEY_USER_NAME, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_USER_NAME, value.trim().take(30)).apply()

    // ---- Important dates ----

    var dates: List<ImportantDate>
        get() = ImportantDate.decode(prefs.getString(KEY_DATES, null))
        set(value) = prefs.edit().putString(KEY_DATES, ImportantDate.encode(value)).apply()

    var dateReminders: Boolean
        get() = prefs.getBoolean(KEY_DATE_REMINDERS, true)
        set(value) = prefs.edit().putBoolean(KEY_DATE_REMINDERS, value).apply()

    // ---- Data from version 2 ----

    /** Challenge days kept here by GentleMan 2.x; moved into the database on first start. */
    val legacyDoneDays: Set<Long>
        get() = prefs.getStringSet(KEY_DONE_DAYS, null)?.mapNotNull { it.toLongOrNull() }?.toSet() ?: emptySet()

    fun clearLegacyDoneDays() {
        prefs.edit().remove(KEY_DONE_DAYS).apply()
    }

    /** Whether the starter habits were offered once (so deleting them all does not bring them back). */
    var starterHabitsAdded: Boolean
        get() = prefs.getBoolean(KEY_STARTER_HABITS, false)
        set(value) = prefs.edit().putBoolean(KEY_STARTER_HABITS, value).apply()

    /** Removes only the link with Hanita (pairing and card); the profile and dates stay. */
    fun clearLink() {
        prefs.edit().remove(KEY_PAIR_ID).remove(KEY_KEY).remove(KEY_CARD).remove(KEY_RECEIVED_AT).apply()
    }

    /** Deletes every setting here: the link, the profile and the dates (the database is cleared separately). */
    fun clearAll() {
        prefs.edit().clear().apply()
    }

    companion object {
        const val NAME = "partner_store"
        const val MAX_MESSAGES = 20
        const val MAX_MESSAGE_LENGTH = 300
        private const val SEPARATOR = '\u001E'
        private const val KEY_PAIR_ID = "pair_id"
        private const val KEY_KEY = "pair_key"
        private const val KEY_CARD = "card"
        private const val KEY_RECEIVED_AT = "received_at"
        private const val KEY_EVENTS = "reminder_events"
        private const val KEY_REMINDERS_ON = "reminders_on"
        private const val KEY_DAYS_BEFORE = "days_before"
        private const val KEY_DAILY_TIP = "daily_tip"
        private const val KEY_TIME = "reminder_time"
        private const val KEY_MESSAGES = "custom_messages"
        private const val KEY_STATUS = "status"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_DATES = "important_dates"
        private const val KEY_DATE_REMINDERS = "date_reminders"
        private const val KEY_DONE_DAYS = "done_days"
        private const val KEY_STARTER_HABITS = "starter_habits"
    }
}
