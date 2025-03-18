package com.yeung.sunnyweather.ui.weather

import androidx.lifecycle.*
import com.yeung.sunnyweather.logic.Repository
import com.yeung.sunnyweather.logic.model.Location


class WeatherViewModel : ViewModel() {

    private val locationLiveData = MutableLiveData<Location>()

    var locationLng = ""

    var locationLat = ""

    var placeName = ""

    val weatherLiveData = locationLiveData.switchMap { location ->
        Repository.refreshWeather(location.lng, location.lat)
    }

    fun refreshWeather(lng: String, lat: String) {
        locationLiveData.value = Location(lng, lat)
    }

}