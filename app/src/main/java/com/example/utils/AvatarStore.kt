package com.example.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import java.io.File
import java.util.UUID

/**
 * AI-generated avatars are stored as PNG files in the app's private storage.
 *
 * They used to be kept as a Base64 string inside SharedPreferences, which made the whole
 * preferences file (read at every launch and rewritten on every settings change) hundreds of
 * kilobytes large. The avatar id is now only a short reference: "gen_file_<uuid>".
 */
object AvatarStore {
    private const val TAG = "AvatarStore"
    private const val DIR = "avatars"
    const val FILE_PREFIX = "gen_file_"
    private const val LEGACY_PREFIX = "gen_base64_"
    private const val MAX_STORED = 12

    private fun dir(context: Context) = File(context.filesDir, DIR).apply { mkdirs() }

    fun isGenerated(avatarId: String) = avatarId.startsWith(FILE_PREFIX)

    private fun fileFor(context: Context, avatarId: String): File? {
        if (!isGenerated(avatarId)) return null
        val name = avatarId.removePrefix(FILE_PREFIX)
        // Only plain UUID file names are accepted, never a path.
        if (!name.matches(Regex("[A-Za-z0-9-]{1,64}"))) return null
        return File(dir(context), "$name.png")
    }

    /** Saves a Base64 PNG and returns the new avatar id, or null if it is not a valid image. */
    fun saveBase64(context: Context, base64: String): String? = try {
        val bytes = Base64.decode(base64, Base64.DEFAULT)
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth <= 0) {
            null
        } else {
            val id = UUID.randomUUID().toString()
            File(dir(context), "$id.png").writeBytes(bytes)
            trim(context)
            FILE_PREFIX + id
        }
    } catch (e: Exception) {
        Log.w(TAG, "Could not store avatar: ${e.javaClass.simpleName}")
        null
    }

    /** Generated avatars, newest first. */
    fun list(context: Context): List<String> =
        dir(context).listFiles { f -> f.extension == "png" }
            ?.sortedByDescending { it.lastModified() }
            ?.map { FILE_PREFIX + it.nameWithoutExtension }
            .orEmpty()

    fun delete(context: Context, avatarId: String) {
        fileFor(context, avatarId)?.delete()
    }

    /** Decodes a stored avatar, downsampled to roughly [maxSizePx]. */
    fun load(context: Context, avatarId: String, maxSizePx: Int = 512): Bitmap? {
        val file = fileFor(context, avatarId)?.takeIf { it.exists() } ?: return null
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.path, bounds)
            var sample = 1
            while (bounds.outWidth / (sample * 2) >= maxSizePx && bounds.outHeight / (sample * 2) >= maxSizePx) sample *= 2
            BitmapFactory.decodeFile(file.path, BitmapFactory.Options().apply { inSampleSize = sample })
        } catch (e: Exception) {
            null
        }
    }

    /** One-time move of a legacy Base64 avatar out of SharedPreferences. Returns the id to keep. */
    fun migrateLegacy(context: Context, avatarId: String): String {
        if (!avatarId.startsWith(LEGACY_PREFIX)) return avatarId
        return saveBase64(context, avatarId.removePrefix(LEGACY_PREFIX)) ?: "avatar_default"
    }

    private fun trim(context: Context) {
        dir(context).listFiles { f -> f.extension == "png" }
            ?.sortedByDescending { it.lastModified() }
            ?.drop(MAX_STORED)
            ?.forEach { it.delete() }
    }
}
