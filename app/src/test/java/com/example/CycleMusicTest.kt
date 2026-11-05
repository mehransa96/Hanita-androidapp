package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CyclePhaseType
import com.example.data.repository.CycleMusicRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CycleMusicTest {

    @Test
    fun `verify all 16 cycle music tracks exist and are partitioned correctly`() {
        val allTracks = CycleMusicRepository.allTracks
        assertEquals(16, allTracks.size)

        val periodTracks = CycleMusicRepository.getTracksForPhase(CyclePhaseType.PERIOD)
        val follicularTracks = CycleMusicRepository.getTracksForPhase(CyclePhaseType.FOLLICULAR)
        val ovulationTracks = CycleMusicRepository.getTracksForPhase(CyclePhaseType.OVULATION)
        val lutealTracks = CycleMusicRepository.getTracksForPhase(CyclePhaseType.LUTEAL)

        assertEquals("دوره قاعدگی باید ۴ موزیک اختصاصی داشته باشد", 4, periodTracks.size)
        assertEquals("فاز فولیکولار باید ۴ موزیک اختصاصی داشته باشد", 4, follicularTracks.size)
        assertEquals("فاز تخمک‌گذاری باید ۴ موزیک اختصاصی داشته باشد", 4, ovulationTracks.size)
        assertEquals("فاز لوتئال باید ۴ موزیک اختصاصی داشته باشد", 4, lutealTracks.size)
    }

    @Test
    fun `verify all 16 music tracks have real playable non-empty audio files`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        CycleMusicRepository.allTracks.forEach { track ->
            assertNotNull("Track title should not be empty", track.title)
            assertNotNull("Track benefit description should not be empty", track.benefitDescription)
            assertTrue("Track resId must be valid", track.resId != 0)

            // Open actual stream to verify file integrity and playability
            val inputStream = context.resources.openRawResource(track.resId)
            val availableBytes = inputStream.available()
            assertTrue(
                "فایل صوتی ${track.id} باید دارای محتوای واقعی و قابل پخش باشد (بیش از ۱۰ کیلوبایت)",
                availableBytes > 10000
            )

            // Verify MP3 signature header or length
            val header = ByteArray(3)
            inputStream.read(header)
            // MP3 files begin with ID3 (0x49 0x44 0x33) or sync word 0xFF 0xFB
            val isId3 = header[0] == 0x49.toByte() && header[1] == 0x44.toByte() && header[2] == 0x33.toByte()
            val isSync = (header[0].toInt() and 0xFF) == 0xFF && ((header[1].toInt() and 0xE0) == 0xE0)
            assertTrue("فایل ${track.id} باید یک فایل معتبر MP3 باشد", isId3 || isSync)

            inputStream.close()
        }
    }

    @Test
    fun `verify getTrackById retrieves specific track accurately`() {
        val track = CycleMusicRepository.getTrackById("period_piano_calm")
        assertNotNull(track)
        assertEquals("پیانو مینیمال تسکین درد", track?.title)
        assertEquals(CyclePhaseType.PERIOD, track?.phase)
    }
}
