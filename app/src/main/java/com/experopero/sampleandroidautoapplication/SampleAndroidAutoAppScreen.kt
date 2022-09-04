package com.experopero.sampleandroidautoapplication

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.*

class SampleAndroidAutoAppScreen(carContext: CarContext) : Screen(carContext) {
    override fun onGetTemplate(): Template {
        val carAppLevel = carContext.carAppApiLevel
        val row = Row.Builder().setTitle("CarApiLevel: $carAppLevel").build()
        val pane = Pane.Builder().addRow(row).build()
        return PaneTemplate.Builder(pane)
            .setHeaderAction(Action.APP_ICON)
            .build()
    }
}