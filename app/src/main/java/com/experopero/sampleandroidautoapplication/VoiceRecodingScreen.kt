package com.experopero.sampleandroidautoapplication

import android.Manifest.permission
import android.content.pm.PackageManager
import androidx.car.app.CarContext
import androidx.car.app.CarToast
import androidx.car.app.Screen
import androidx.car.app.model.*
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VoiceRecodingScreen(carContext: CarContext) : Screen(carContext) {
    private val voiceRecorder = VoiceRecorder(carContext)

    private fun onRecordClicked() {
            if (carContext.checkSelfPermission(permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED
            ) {
                CarToast.makeText(
                    carContext, "マイクを使用するには権限が必要です",
                    CarToast.LENGTH_LONG
                ).show()
                carContext.requestPermissions(listOf(permission.RECORD_AUDIO)) { grantedPermissions, _ ->
                    if (grantedPermissions.contains(permission.RECORD_AUDIO)) {
                        onRecordClicked()
                    }
                }
                return
            }
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                runCatching {
                    voiceRecorder.record()
                }.fold(
                    onSuccess = {
                        CarToast.makeText(
                            carContext, "録音が完了しました",
                            CarToast.LENGTH_LONG
                        ).show()
                    },
                    onFailure = {
                        CarToast.makeText(
                            carContext, "録音に失敗しました",
                            CarToast.LENGTH_LONG
                        ).show()
                    }
                )
            }
        }
    }

    override fun onGetTemplate(): Template {
        val row = Row.Builder().setTitle("Voice Recoding Screen").build()
        val pane = Pane.Builder().addRow(row).build()
        return PaneTemplate.Builder(pane)
            .setHeaderAction(Action.BACK)
            .setActionStrip(
                ActionStrip.Builder().addAction(
                    Action.Builder()
                        .setTitle("REC")
                        .setOnClickListener { onRecordClicked() }
                        .build()
                ).build()
            )
            .build()
    }
}