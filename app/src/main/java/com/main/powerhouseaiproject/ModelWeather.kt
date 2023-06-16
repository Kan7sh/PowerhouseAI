package com.main.powerhouseaiproject

data class ModelWeather(
    val lat: Double,
    val lon: Double,
    val timezone: String,
    val current: CurrentWeather,
)

data class CurrentWeather(
    val dt: Long,
    val sunrise: Long,
    val sunset: Long,
    val temp: Double,
    val feels_like: Double,
    val pressure: Int,
    val humidity: Int,
    val dew_point: Double,
    val uvi: Double,
    val clouds: Int,
    val visibility: Int,
    val wind_speed: Double,
    val wind_deg: Int,
    val weather: List<Weather>
)



data class Weather(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)




