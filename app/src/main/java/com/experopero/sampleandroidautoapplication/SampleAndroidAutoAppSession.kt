package com.experopero.sampleandroidautoapplication

import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session
import com.experopero.sampleandroidautoapplication.SampleAndroidAutoAppScreen

class SampleAndroidAutoAppSession : Session() {
    override fun onCreateScreen(intent: Intent): Screen {
        return SampleAndroidAutoAppScreen(carContext)
    }
}