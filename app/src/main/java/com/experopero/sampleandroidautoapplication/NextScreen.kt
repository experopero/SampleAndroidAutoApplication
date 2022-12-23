package com.experopero.sampleandroidautoapplication

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.*

class NextScreen(carContext: CarContext) : Screen(carContext) {
    override fun onGetTemplate(): Template {
        val row = Row.Builder().setTitle("Next Screen").build()
        val pane = Pane.Builder().addRow(row).build()
        return PaneTemplate.Builder(pane)
            .setHeaderAction(Action.BACK)
            .build()
    }
}