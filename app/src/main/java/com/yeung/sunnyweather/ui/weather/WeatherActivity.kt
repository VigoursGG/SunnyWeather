package com.yeung.sunnyweather.ui.weather

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.yeung.sunnyweather.databinding.ActivityWeatherBinding
import com.yeung.sunnyweather.databinding.ForecastBinding
import com.yeung.sunnyweather.databinding.ForecastItemBinding
import com.yeung.sunnyweather.databinding.LifeIndexBinding
import com.yeung.sunnyweather.databinding.NowBinding
import com.yeung.sunnyweather.logic.model.Weather
import com.yeung.sunnyweather.logic.model.getSky
import java.text.SimpleDateFormat
import java.util.Locale


class WeatherActivity : AppCompatActivity() {

    private lateinit var weatherBinding: ActivityWeatherBinding

    private lateinit var forecastBinding: ForecastBinding

    private lateinit var lifeIndexBinding: LifeIndexBinding

    private lateinit var nowBinding: NowBinding

    val viewModel by lazy {
        ViewModelProvider(this).get(WeatherViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 21) {
            val decorView = window.decorView
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            window.statusBarColor = Color.TRANSPARENT
        }
        weatherBinding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(weatherBinding.root)
        nowBinding = NowBinding.bind(weatherBinding.nowView.root)
        forecastBinding = ForecastBinding.bind(weatherBinding.forecastView.root)
        lifeIndexBinding = LifeIndexBinding.bind(weatherBinding.lifeIndexView.root)

        if (viewModel.locationLng.isEmpty()) {
            viewModel.locationLng = intent.getStringExtra("location_lng") ?: ""
        }
        if (viewModel.locationLat.isEmpty()) {
            viewModel.locationLat = intent.getStringExtra("location_lat") ?: ""
        }
        if (viewModel.placeName.isEmpty()) {
            viewModel.placeName = intent.getStringExtra("place_name") ?: ""
        }
        viewModel.weatherLiveData.observe(this) { result ->
            val weather = result.getOrNull()
            if (weather != null) {
                showWeatherInfo(weather)
            } else {
                Toast.makeText(this, "无法成功获取天气信息", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
        }
        viewModel.refreshWeather(viewModel.locationLng, viewModel.locationLat)
    }

    private fun showWeatherInfo(weather: Weather) {
        nowBinding.placeName.text = viewModel.placeName
        val realtime = weather.realtime
        val daily = weather.daily

        // 填充now.xml布局中的数据
        val currentTempText = "${realtime.temperature.toInt()} ℃"
        nowBinding.currentTemp.text = currentTempText
        nowBinding.currentSky.text = getSky(realtime.skycon).info
        val currentPM25Text = "空气指数 ${realtime.airQuality.aqi.chn.toInt()}"
        nowBinding.currentAQI.text = currentPM25Text
        nowBinding.nowLayout.setBackgroundResource(getSky(realtime.skycon).bg)

        // 填充forecast.xml布局中的数据
        forecastBinding.forecastLayout.removeAllViews()
        val days = daily.skycon.size
        for (i in 0 until days) {
            val forecastItemBinding = ForecastItemBinding.inflate(layoutInflater)
            val skycon = daily.skycon[i]
            val temperature = daily.temperature[i]
            val dateInfo = forecastItemBinding.dateInfo
            val skyIcon = forecastItemBinding.skyIcon
            val skyInfo = forecastItemBinding.skyInfo
            val temperatureInfo = forecastItemBinding.temperatureInfo
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateInfo.text = simpleDateFormat.format(skycon.date)
            val sky = getSky(skycon.value)
            skyIcon.setImageResource(sky.icon)
            skyInfo.text = sky.info
            val tempText = "${temperature.min.toInt()} ~ ${temperature.max.toInt()} ℃"
            temperatureInfo.text = tempText
            forecastBinding.forecastLayout.addView(forecastItemBinding.root)
            val layoutParams =
                forecastItemBinding.root.layoutParams  as ViewGroup.MarginLayoutParams
            val marginInPx = (16 * resources.displayMetrics.density).toInt()
            layoutParams.setMargins(0,  marginInPx, 0, marginInPx)
            forecastItemBinding.root.layoutParams = layoutParams
        }

        // 填充life_index.xml布局中的数据
        val lifeIndex = daily.lifeIndex
        lifeIndexBinding.coldRiskText.text = lifeIndex.coldRisk[0].desc
        lifeIndexBinding.dressingText.text = lifeIndex.dressing[0].desc
        lifeIndexBinding.ultravioletText.text = lifeIndex.ultraviolet[0].desc
        lifeIndexBinding.carWashingText.text = lifeIndex.carWashing[0].desc
        weatherBinding.weatherLayout.visibility = View.VISIBLE
    }
}