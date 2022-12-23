package com.experopero.sampleandroidautoapplication

import android.Manifest
import android.annotation.SuppressLint
import android.media.*
import androidx.annotation.RequiresPermission
import androidx.car.app.CarContext
import androidx.car.app.media.CarAudioRecord
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream

class VoicePlayer(private val carContext: CarContext) {
    @SuppressLint("ClassVerificationFailure") // runtime check for < API 26
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private fun play(fileName: String, audioFocusRequest: AudioFocusRequest) {
        val inputStream: InputStream = try {
            carContext.openFileInput(fileName)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return
        }
        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_DEFAULT)
                    .setSampleRate(CarAudioRecord.AUDIO_CONTENT_SAMPLING_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(CarAudioRecord.AUDIO_CONTENT_BUFFER_SIZE)
            .build()
        audioTrack.play()
        try {
            while (inputStream.available() > 0) {
                val audioData = ByteArray(CarAudioRecord.AUDIO_CONTENT_BUFFER_SIZE)
                val size = inputStream.read(audioData, 0, audioData.size)
                if (size < 0) {
                    // End of file
                    return
                }
                audioTrack.write(audioData, 0, size)
            }
        } catch (e: IOException) {
            throw IllegalStateException(e)
        }
        audioTrack.stop()
        // Abandon the FocusRequest so that user's media can be resumed
        carContext.getSystemService(AudioManager::class.java).abandonAudioFocusRequest(
            audioFocusRequest
        )
    }
}