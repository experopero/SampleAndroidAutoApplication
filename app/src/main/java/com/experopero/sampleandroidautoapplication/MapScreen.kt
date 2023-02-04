package com.experopero.sampleandroidautoapplication

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Template
import androidx.car.app.navigation.model.MapTemplate

class MapScreen(carContext: CarContext): Screen(carContext) {
    override fun onGetTemplate(): Template {
        return MapTemplate.Builder()
            .build()
    }

}