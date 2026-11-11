package com.hanita.core.partner

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/** Draws QR codes offline with ZXing (no Google Play services needed). */
object QrCodes {

    /** The QR modules of [text] (true = dark), including a 2-module quiet zone. */
    fun matrix(text: String): Array<BooleanArray> {
        val hints = mapOf(
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
            EncodeHintType.MARGIN to 2,
            EncodeHintType.CHARACTER_SET to "UTF-8"
        )
        val bits = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, 0, 0, hints)
        return Array(bits.height) { y -> BooleanArray(bits.width) { x -> bits.get(x, y) } }
    }

    /** A black-on-white bitmap of [text], [pixelsPerModule] pixels for each QR module. */
    fun bitmap(text: String, pixelsPerModule: Int = 8): Bitmap {
        val m = matrix(text)
        val modules = m.size
        val size = modules * pixelsPerModule
        val pixels = IntArray(size * size)
        for (y in 0 until size) {
            val row = m[y / pixelsPerModule]
            for (x in 0 until size) {
                pixels[y * size + x] = if (row[x / pixelsPerModule]) Color.BLACK else Color.WHITE
            }
        }
        return Bitmap.createBitmap(pixels, size, size, Bitmap.Config.ARGB_8888)
    }
}
