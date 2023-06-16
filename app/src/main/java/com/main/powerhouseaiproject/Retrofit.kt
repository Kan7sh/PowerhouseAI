package com.main.powerhouseaiproject

import android.graphics.ColorSpace.Model
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

const val BASE_URL="https://api.openweathermap.org/"
const val API_KEY="56d812637eb6989a7d310957429af99f"

interface WeatherInterface{

    @GET("data/3.0/onecall?appid=$API_KEY")
    fun getWeather(@Query("lat")lat:Double,@Query("lon") lon:Double):Call<ModelWeather>

}

object WeatherService{
    val weatherInstance:WeatherInterface
    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        weatherInstance = retrofit.create(WeatherInterface::class.java)

    }

}