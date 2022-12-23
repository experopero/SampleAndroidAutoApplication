package com.experopero.sampleandroidautoapplication

import android.Manifest
import android.Manifest.permission
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import androidx.annotation.RequiresPermission
import androidx.car.app.CarContext
import androidx.car.app.media.CarAudioRecord
import kotlinx.coroutines.runInterruptible
import java.io.IOException
import java.io.OutputStream

class VoiceRecorder(private val carContext: CarContext) {
    @RequiresPermission(permission.RECORD_AUDIO)
    suspend fun record() = runInterruptible {
        val carAudioRecord = CarAudioRecord.create(carContext)

        // audio のフォーカスを貰う。
        // 音楽が鳴っていてもここで静音にし録音できる
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
            .build()
        val audioFocusRequest =
            AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
                .setAudioAttributes(audioAttributes)
                .setOnAudioFocusChangeListener { state: Int ->
                    if (state == AudioManager.AUDIOFOCUS_LOSS) {
                        // 他のアプリによってなど、何かしらの理由でフォーカスが外れた時、録音を終了させる
                        carAudioRecord.stopRecording()
                    }
                }
                .build()
        if (carContext.getSystemService(AudioManager::class.java)
                .requestAudioFocus(audioFocusRequest)
            != AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        ) {
            return@runInterruptible
        }
        // 録音を開始する。このメソッドが呼ばれると録音 UI が表示される。このメソッドはブロックされず、すぐに次の行へ進む。
        carAudioRecord.startRecording()
        val bytes: MutableList<Byte> = ArrayList()
        val byteArray = ByteArray(CarAudioRecord.AUDIO_CONTENT_BUFFER_SIZE)
        // 録音が停止されない限り、carAudioRecord は音声データを提供し続ける
        while (carAudioRecord.read(byteArray, 0, CarAudioRecord.AUDIO_CONTENT_BUFFER_SIZE) >= 0) {
            byteArray.forEach {
                bytes.add(it)
            }
        }

        try {
            val outputStream: OutputStream =
                carContext.openFileOutput("voice.wav", Context.MODE_PRIVATE)
            addHeader(outputStream, bytes.size)
            bytes.forEach {
                outputStream.write(it.toInt())
            }
            outputStream.flush()
            outputStream.close()
        } catch (e: IOException) {
            throw IllegalStateException(e)
        } finally {
            carAudioRecord.stopRecording()
        }
    }

    private fun addHeader(outputStream: OutputStream, totalAudioLen: Int) {
        val totalDataLen = totalAudioLen + 36
        val header = ByteArray(44)
        val channels = 1
        val dataElementSize = 16
        val blockAlign = (channels * dataElementSize / 8).toLong()
        val longSampleRate = CarAudioRecord.AUDIO_CONTENT_SAMPLING_RATE.toLong() // 16000 なので 16kHz
        val byteRate = longSampleRate * blockAlign

        // RIFF 識別子(4byte)。それぞれを R I F F 文字にする
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()

        // チャンクサイズ(4byte)。
        header[4] = (totalAudioLen and 0xff).toByte() // ファイルサイズ
        header[5] = (totalDataLen shr 8 and 0xff).toByte()
        header[6] = (totalDataLen shr 16 and 0xff).toByte()
        header[7] = (totalDataLen shr 24 and 0xff).toByte()

        // フォーマット(4byte)。wave を表したいので W A V E と入れる。
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()

        // フォーマットチャンク(4byte)。f m t の3文字でよくて、残り余っている 1byte は空白で。
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()

        // フォーマットサイズ(4byte)。ここではリニア PCM だけを考えればよく、16 を 1byte 目に入れて、残りは 0 で。
        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0

        // オーディオフォーマット(2byte)。非圧縮リニア PCM であるため 1 を入れて残りは 0　で。
        header[20] = 1
        header[21] = 0

        // チャンネル数(2byte)。モノラルは 1、ステレオは 2。ここではモノラルを指定している。
        header[22] = channels.toByte()
        header[23] = 0

        // サンプリングレート(4byte)。
        header[24] = (longSampleRate and 0xff).toByte()
        header[25] = (longSampleRate shr 8 and 0xff).toByte()
        header[26] = (longSampleRate shr 16 and 0xff).toByte()
        header[27] = (longSampleRate shr 24 and 0xff).toByte()

        // バイトレート(4byte)。
        header[28] = (byteRate and 0xff).toByte()
        header[29] = (byteRate shr 8 and 0xff).toByte()
        header[30] = (byteRate shr 16 and 0xff).toByte()
        header[31] = (byteRate shr 24 and 0xff).toByte()

        // ブロックのサイズ
        header[32] = blockAlign.toByte() // block align
        header[33] = 0

        // ビットサンプル。
        header[34] = (dataElementSize and 0xff).toByte() // bits per sample
        header[35] = (dataElementSize shr 8 and 0xff).toByte()
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = (totalAudioLen shr 8 and 0xff).toByte()
        header[42] = (totalAudioLen shr 16 and 0xff).toByte()
        header[43] = (totalAudioLen shr 24 and 0xff).toByte()
        outputStream.write(header, 0, 44)
    }
}