package com.main.powerhouseaiproject

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.main.powerhouseaiproject.databinding.ActivityHomeBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*


class Home : AppCompatActivity(),ConnectivityCallback {
    private val LOCATION_PERMISSION_REQUEST_CODE = 100
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var networkChangeReceiver: NetworkChangeReceiver? = null
    private lateinit var binding: ActivityHomeBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var adapterCities: AdapterCities



    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        networkChangeReceiver = NetworkChangeReceiver(this)
        sharedPreferences = getSharedPreferences("CurrentWeather",Context.MODE_PRIVATE)
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkChangeReceiver, filter)
        val isConnected = isInternetConnected(this)

        if (!isConnected) {
            if (sharedPreferences.all.isEmpty()){
                Toast.makeText(this,"No Internet",Toast.LENGTH_SHORT).show()
            }else{
                binding.city.text = sharedPreferences.getString("city","none")
                binding.date.text = sharedPreferences.getString("dt","none")
                binding.main.text = sharedPreferences.getString("main","none")
                binding.description.text= sharedPreferences.getString("description","none")
                binding.temp.text=sharedPreferences.getString("temp","none")
                binding.sunrise.text=sharedPreferences.getString("sunrise","none")
                binding.sunset.text=sharedPreferences.getString("sunset","none")
                binding.humidity.text=sharedPreferences.getString("humidity","none")
                binding.windSpeed.text=sharedPreferences.getString("windSpeed","none")
                val updatedAt: String =  "Last Updated "+ sharedPreferences.getString("storedAt","none")
                binding.updated.text=updatedAt
                val image = sharedPreferences.getString("weatherIcon","none")
                val resources = applicationContext.resources
                val resourceId = resources.getIdentifier(image, "drawable", applicationContext.packageName)
                binding.weatherIcon.setImageResource(resourceId)
                binding.pBar.visibility=View.INVISIBLE
            }
            binding.noInternet.text="No Internet"

        }else{
            adapterCities= AdapterCities(this)
            binding.citiesRv.adapter=adapterCities
        }


        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            startLocationUpdates()
        }

    }
    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    val weather= WeatherService.weatherInstance.getWeather(latitude,longitude)
                    weather.enqueue(object :Callback<ModelWeather>{
                        override fun onResponse(
                            call: Call<ModelWeather>,
                            response: Response<ModelWeather>
                        ) {
                            val editor = sharedPreferences.edit()
                            val cityName = getCityName(latitude, longitude)
                            binding.city.text = cityName
                            binding.date.text = SimpleDateFormat("dd-MM-yyyy",Locale.getDefault()).format(
                                Date(response.body()!!.current.dt*1000)
                            )
                            binding.main.text = response.body()!!.current.weather[0].main
                            binding.description.text= response.body()!!.current.weather[0].description
                            val temp: String =(response.body()!!.current.temp-273.1).toString().substring(0,4)+"°C"
                            val sunrise: String ="Sunrise  "+SimpleDateFormat("HH:mm:ss",Locale.getDefault()).format(
                                Date(response.body()!!.current.sunrise*1000)
                            )
                            val sunset: String ="Sunset  "+SimpleDateFormat("HH:mm:ss",Locale.getDefault()).format(
                                Date(response.body()!!.current.sunset*1000)
                            )
                            val humidity: String ="Humidity  " +response.body()!!.current.humidity.toString()
                            val windSpeed: String ="Wind Speed  "+ response.body()!!.current.wind_speed.toString()+"Km/h"
                            binding.temp.text=temp
                            binding.sunrise.text=sunrise
                            binding.sunset.text=sunset
                            binding.humidity.text=humidity
                            binding.windSpeed.text=windSpeed
                            val image = response.body()!!.current.weather[0].icon.reversed()
                            val resources = applicationContext.resources
                            val resourceId = resources.getIdentifier(image, "drawable", applicationContext.packageName)
                            binding.weatherIcon.setImageResource(resourceId)
                            val currentDate = Date()
                            val dateFormat=SimpleDateFormat("HH:mm:ss",Locale.getDefault()).format(currentDate)
                            binding.pBar.visibility=View.INVISIBLE
                            editor.putString("city",cityName)
                            editor.putString("temp",temp)
                            editor.putString("main", binding.main.text as String)
                            editor.putString("description", binding.description.text as String)
                            editor.putString("dt", binding.date.text as String)
                            editor.putString("weatherIcon", image)
                            editor.putString("sunrise", sunrise)
                            editor.putString("sunset", sunset)
                            editor.putString("humidity", humidity)
                            editor.putString("windSpeed", windSpeed)
                            editor.putString("storedAt",dateFormat)
                            editor.apply()
                        }

                        override fun onFailure(call: Call<ModelWeather>, t: Throwable) {
                        }

                    })
                }
            }
            .addOnFailureListener {
                print(it.message)
            }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun isInternetConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)

        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults.isNotEmpty()&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
            enableLocationUpdates()
            startLocationUpdates()
        }else{
            Toast.makeText(this,"Location Permission Needed",Toast.LENGTH_SHORT).show()
        }
    }
    private fun getCityName(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)

        if (addresses!!.isNotEmpty()) {
            val cityName = addresses[0].locality
            return cityName ?: ""
        }

        return ""
    }

    private fun enableLocationUpdates() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            startLocationUpdates()
        } else {
            Toast.makeText(this,"Turn On the Location",Toast.LENGTH_SHORT).show()
        }
    }

    override fun onInternetConnected() {
        binding.noInternet.text=""
        binding.updated.text=""
        startLocationUpdates()
        adapterCities= AdapterCities(this)
        binding.citiesRv.adapter=adapterCities
    }
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(networkChangeReceiver)
    }
}