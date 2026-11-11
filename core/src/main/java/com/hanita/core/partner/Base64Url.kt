package com.hanita.core.partner

/**
 * URL-safe Base64 without padding (RFC 4648 §5). java.util.Base64 needs API 26 and
 * android.util.Base64 does not run in plain JVM tests, so the link uses this small codec.
 */
internal object Base64Url {
    private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"
    private val DECODE = IntArray(128) { -1 }.also { table ->
        ALPHABET.forEachIndexed { i, c -> table[c.code] = i }
    }

    fun encode(bytes: ByteArray): String {
        val out = StringBuilder((bytes.size * 4 + 2) / 3)
        var i = 0
        while (i + 2 < bytes.size) {
            val n = (bytes[i].toInt() and 0xFF shl 16) or (bytes[i + 1].toInt() and 0xFF shl 8) or (bytes[i + 2].toInt() and 0xFF)
            out.append(ALPHABET[n ushr 18 and 63]).append(ALPHABET[n ushr 12 and 63])
                .append(ALPHABET[n ushr 6 and 63]).append(ALPHABET[n and 63])
            i += 3
        }
        when (bytes.size - i) {
            1 -> {
                val n = bytes[i].toInt() and 0xFF shl 16
                out.append(ALPHABET[n ushr 18 and 63]).append(ALPHABET[n ushr 12 and 63])
            }
            2 -> {
                val n = (bytes[i].toInt() and 0xFF shl 16) or (bytes[i + 1].toInt() and 0xFF shl 8)
                out.append(ALPHABET[n ushr 18 and 63]).append(ALPHABET[n ushr 12 and 63]).append(ALPHABET[n ushr 6 and 63])
            }
        }
        return out.toString()
    }

    /** Null when [text] is not valid unpadded Base64url. */
    fun decode(text: String): ByteArray? {
        if (text.length % 4 == 1) return null
        val out = ByteArray(text.length * 3 / 4)
        var buffer = 0
        var bits = 0
        var pos = 0
        for (c in text) {
            val v = if (c.code < 128) DECODE[c.code] else -1
            if (v < 0) return null
            buffer = (buffer shl 6) or v
            bits += 6
            if (bits >= 8) {
                bits -= 8
                out[pos++] = (buffer ushr bits and 0xFF).toByte()
            }
        }
        // Leftover bits must be zero, otherwise two different strings would decode the same.
        if (bits > 0 && buffer and ((1 shl bits) - 1) != 0) return null
        return if (pos == out.size) out else out.copyOf(pos)
    }
}
