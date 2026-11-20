package com.azkari2027.app.audio

import android.content.Context
import android.media.AudioAttributes as PlatformAudioAttributes
import android.media.SoundPool
import android.net.Uri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.azkari2027.app.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AudioPlaybackState(
    val currentAudioName: String? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L
)

class OfflineAudioController(private val context: Context) {

    private var exoPlayer: ExoPlayer? = null
    private val _state = MutableStateFlow(AudioPlaybackState())
    val state: StateFlow<AudioPlaybackState> = _state.asStateFlow()

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(2)
        .setAudioAttributes(
            PlatformAudioAttributes.Builder()
                .setUsage(PlatformAudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(PlatformAudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val tickSoundId = soundPool.load(context, R.raw.tick, 1)
    private val pageFlipSoundId = soundPool.load(context, R.raw.page_flip, 1)

    private fun ensurePlayer(): ExoPlayer {
        exoPlayer?.let { return it }
        val attrs = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
            .build()
        return ExoPlayer.Builder(context)
            .setAudioAttributes(attrs, true)
            .setHandleAudioBecomingNoisy(true)
            .build()
            .also { player ->
                player.addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        _state.value = _state.value.copy(
                            isPlaying = isPlaying,
                            durationMs = player.duration.coerceAtLeast(0L)
                        )
                    }
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_ENDED) {
                            _state.value = _state.value.copy(isPlaying = false, positionMs = 0L)
                        } else if (playbackState == Player.STATE_READY) {
                            _state.value = _state.value.copy(durationMs = player.duration.coerceAtLeast(0L))
                        }
                    }
                })
                exoPlayer = player
            }
    }

    fun playOrToggle(audioName: String) {
        if (audioName.isBlank()) return
        val player = ensurePlayer()
        if (_state.value.currentAudioName == audioName) {
            if (player.isPlaying) {
                player.pause()
            } else {
                if (player.playbackState == Player.STATE_ENDED) player.seekTo(0)
                player.play()
            }
            return
        }
        val resId = context.resources.getIdentifier(audioName, "raw", context.packageName)
        if (resId == 0) return
        val uri = Uri.parse("android.resource://${context.packageName}/$resId")
        player.stop()
        player.setMediaItem(MediaItem.fromUri(uri))
        player.prepare()
        player.play()
        _state.value = AudioPlaybackState(currentAudioName = audioName, isPlaying = true)
    }

    fun syncProgress() {
        val player = exoPlayer ?: return
        if (player.isPlaying) {
            _state.value = _state.value.copy(
                positionMs = player.currentPosition.coerceAtLeast(0L),
                durationMs = player.duration.coerceAtLeast(1L)
            )
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
        _state.value = _state.value.copy(positionMs = positionMs)
    }

    fun stop() {
        exoPlayer?.stop()
        _state.value = AudioPlaybackState()
    }

    fun playTick(enabled: Boolean) {
        if (enabled && tickSoundId != 0) soundPool.play(tickSoundId, 0.7f, 0.7f, 1, 0, 1f)
    }

    fun playPageFlip(enabled: Boolean) {
        if (enabled && pageFlipSoundId != 0) soundPool.play(pageFlipSoundId, 0.55f, 0.55f, 1, 0, 1f)
    }
}
