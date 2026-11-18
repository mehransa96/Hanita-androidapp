package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CyclePhaseType
import com.example.data.model.MusicIntent
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
    fun `every phase has 8 tracks covering at least 4 different intentions`() {
        val allTracks = CycleMusicRepository.allTracks
        assertEquals(32, allTracks.size)
        assertEquals("track ids must be unique", allTracks.size, allTracks.map { it.id }.toSet().size)
        assertEquals("every track must use its own audio file", allTracks.size, allTracks.map { it.resId }.toSet().size)

        CyclePhaseType.values().forEach { phase ->
            val tracks = CycleMusicRepository.getTracksForPhase(phase)
            assertEquals("${phase.persianTitle} باید ۸ قطعه داشته باشد", 8, tracks.size)
            assertTrue(
                "${phase.persianTitle} باید دست‌کم ۴ حال‌وهوای مختلف داشته باشد",
                CycleMusicRepository.intentsFor(phase).size >= 4
            )
        }
    }

    @Test
    fun `filters by phase and intention and every track has listening guidance`() {
        val sleepDuringPeriod = CycleMusicRepository.filter(CyclePhaseType.PERIOD, MusicIntent.SLEEP)
        assertTrue(sleepDuringPeriod.isNotEmpty())
        assertTrue(sleepDuringPeriod.all { it.phase == CyclePhaseType.PERIOD && it.intent == MusicIntent.SLEEP })
        assertEquals(CycleMusicRepository.allTracks, CycleMusicRepository.filter(null, null))
        assertEquals(MusicIntent.values().toList(), CycleMusicRepository.intentsFor(null))

        CycleMusicRepository.allTracks.forEach { track ->
            assertTrue("${track.id} needs a how-to-listen line", track.howToListen.isNotBlank())
            assertTrue("${track.id} duration must be realistic", track.durationSeconds in 45..120)
            track.bpm?.let { assertTrue("${track.id} bpm out of range", it in 40..180) }
        }
        // binaural pieces only work on headphones and must say so
        assertTrue(CycleMusicRepository.getTrackById("period_frequency_528")!!.needsHeadphones)
        assertTrue(CycleMusicRepository.getTrackById("luteal_frequency_432")!!.needsHeadphones)
    }

    @Test
    fun `verify all music tracks have real playable non-empty audio files`() {
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
