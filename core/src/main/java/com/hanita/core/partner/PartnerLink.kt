package com.hanita.core.partner

import java.security.GeneralSecurityException
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * The encrypted, server-free link between Hanita and the partner app.
 *
 * Pairing happens face to face: Hanita shows a QR code that holds a fresh random 256-bit key,
 * a random pair id and the first encrypted card. The partner app scans it, so the key never
 * travels through a messenger, SMS or any server.
 *
 * Every later update is only the encrypted card ("HNP1.U.<pair id>.<sealed card>"). It can be
 * shown as a QR code again or sent as text through any messenger: without the key it is
 * unreadable, and AES-256-GCM rejects any change to it.
 *
 * Text formats (Base64url, no padding):
 *  - pairing: `HNP1.P.<pairId>.<key>.<sealed>`  (QR only)
 *  - update:  `HNP1.U.<pairId>.<sealed>`
 *  - sealed = 12-byte nonce || AES-GCM(card bytes), with "HNP1|<kind>|<pairId>" as associated data.
 */
object PartnerLink {
    /** The partner app's name, shown in both apps. */
    const val PARTNER_APP_NAME = "Gentleman"
    const val PREFIX = "HNP1"
    /** Links like `hanitapartner://u/HNP1.U....` open the partner app directly. */
    const val SCHEME = "hanitapartner"
    const val KEY_BYTES = 32
    const val PAIR_ID_BYTES = 9
    private const val NONCE_BYTES = 12
    private const val TAG_BITS = 128

    private val TOKEN = Regex("""HNP1\.([PU])\.([A-Za-z0-9_-]+)(?:\.([A-Za-z0-9_-]+))?\.([A-Za-z0-9_-]+)""")

    /** The shared secret of one pairing. [pairId] is public; [key] must never leave the two phones. */
    class Pairing(val pairId: String, val key: ByteArray) {
        init {
            require(key.size == KEY_BYTES) { "key must be $KEY_BYTES bytes" }
            require(Base64Url.decode(pairId)?.size == PAIR_ID_BYTES) { "bad pair id" }
        }

        fun keyText(): String = Base64Url.encode(key)

        /** A 4-digit code both phones show after pairing, so the couple can see they match. */
        fun verificationCode(): String {
            val digest = MessageDigest.getInstance("SHA-256").digest(key + "hanita-verify".toByteArray())
            val n = ((digest[0].toInt() and 0xFF) shl 16) or ((digest[1].toInt() and 0xFF) shl 8) or (digest[2].toInt() and 0xFF)
            return (n % 10000).toString().padStart(4, '0')
        }

        companion object {
            fun create(random: SecureRandom = SecureRandom()): Pairing {
                val id = ByteArray(PAIR_ID_BYTES).also(random::nextBytes)
                val key = ByteArray(KEY_BYTES).also(random::nextBytes)
                return Pairing(Base64Url.encode(id), key)
            }

            /** Restores a stored pairing; null when the stored values are damaged. */
            fun restore(pairId: String?, keyText: String?): Pairing? {
                if (pairId.isNullOrBlank() || keyText.isNullOrBlank()) return null
                val key = Base64Url.decode(keyText) ?: return null
                return try { Pairing(pairId, key) } catch (e: IllegalArgumentException) { null }
            }
        }
    }

    enum class Kind { PAIRING, UPDATE }

    /** A token found in scanned or pasted text, not yet decrypted. */
    class Token internal constructor(
        val kind: Kind,
        val pairId: String,
        internal val keyText: String?,
        internal val sealed: String
    )

    sealed class Opened {
        /** A first-time pairing (scanned from Hanita's QR code). */
        class NewPairing(val pairing: Pairing, val card: PartnerCard) : Opened()
        /** A new card for the existing pairing. */
        class Update(val card: PartnerCard) : Opened()
        /** A pairing token arrived as text: only a scanned QR code is accepted. */
        object PairingNeedsScan : Opened()
        /** An update for another pairing (an old or someone else's link). */
        object WrongPair : Opened()
        /** An update arrived before the app was paired. */
        object NotPaired : Opened()
        /** Damaged, edited or not a Hanita link. */
        object Invalid : Opened()
    }

    fun pairingText(pairing: Pairing, card: PartnerCard, random: SecureRandom = SecureRandom()): String =
        "$PREFIX.P.${pairing.pairId}.${pairing.keyText()}.${seal(pairing, Kind.PAIRING, card, random)}"

    fun updateText(pairing: Pairing, card: PartnerCard, random: SecureRandom = SecureRandom()): String =
        "$PREFIX.U.${pairing.pairId}.${seal(pairing, Kind.UPDATE, card, random)}"

    /** A clickable link for messengers that open custom schemes. */
    fun updateLink(updateText: String): String = "$SCHEME://u/$updateText"

    /** Finds the first Hanita token in any text (a scanned QR, a pasted message or a link). */
    fun find(text: String?): Token? {
        if (text.isNullOrBlank()) return null
        val m = TOKEN.find(text) ?: return null
        val kind = if (m.groupValues[1] == "P") Kind.PAIRING else Kind.UPDATE
        val key = m.groups[3]?.value
        if ((kind == Kind.PAIRING) != (key != null)) return null
        return Token(kind, m.groupValues[2], key, m.groupValues[4])
    }

    /**
     * Decrypts [text]. [current] is the pairing the partner app already has (null before pairing).
     * [fromScan] is true only for a QR code scanned with the camera.
     */
    fun open(text: String?, current: Pairing?, fromScan: Boolean): Opened {
        val token = find(text) ?: return Opened.Invalid
        return when (token.kind) {
            Kind.PAIRING -> {
                if (!fromScan) return Opened.PairingNeedsScan
                val pairing = Pairing.restore(token.pairId, token.keyText) ?: return Opened.Invalid
                val card = unseal(pairing, Kind.PAIRING, token.sealed) ?: return Opened.Invalid
                Opened.NewPairing(pairing, card)
            }
            Kind.UPDATE -> {
                if (current == null) return Opened.NotPaired
                if (token.pairId != current.pairId) return Opened.WrongPair
                val card = unseal(current, Kind.UPDATE, token.sealed) ?: return Opened.Invalid
                Opened.Update(card)
            }
        }
    }

    private fun aad(pairing: Pairing, kind: Kind): ByteArray =
        "$PREFIX|${if (kind == Kind.PAIRING) "P" else "U"}|${pairing.pairId}".toByteArray()

    private fun seal(pairing: Pairing, kind: Kind, card: PartnerCard, random: SecureRandom): String {
        val nonce = ByteArray(NONCE_BYTES).also(random::nextBytes)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(pairing.key, "AES"), GCMParameterSpec(TAG_BITS, nonce))
        cipher.updateAAD(aad(pairing, kind))
        return Base64Url.encode(nonce + cipher.doFinal(card.toBytes()))
    }

    private fun unseal(pairing: Pairing, kind: Kind, sealed: String): PartnerCard? {
        val bytes = Base64Url.decode(sealed) ?: return null
        if (bytes.size <= NONCE_BYTES + TAG_BITS / 8) return null
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(
                Cipher.DECRYPT_MODE, SecretKeySpec(pairing.key, "AES"),
                GCMParameterSpec(TAG_BITS, bytes, 0, NONCE_BYTES)
            )
            cipher.updateAAD(aad(pairing, kind))
            PartnerCard.fromBytes(cipher.doFinal(bytes, NONCE_BYTES, bytes.size - NONCE_BYTES))
        } catch (e: GeneralSecurityException) {
            null
        }
    }
}
