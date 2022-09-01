package com.experopero.sampleandroidautoapplication

import androidx.car.app.CarAppService
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator
import com.experopero.sampleandroidautoapplication.SampleAndroidAutoAppSession

class SampleAndroidAutoAppService : CarAppService() {
    override fun createHostValidator(): HostValidator {
        // TODO: https://developer.android.com/reference/androidx/car/app/CarAppService#createHostValidator()
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
    }

    override fun onCreateSession(): Session {
        return SampleAndroidAutoAppSession()
    }
}