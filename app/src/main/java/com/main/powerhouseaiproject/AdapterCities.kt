package com.main.powerhouseaiproject

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AdapterCities(private val context: Context):Adapter<AdapterCities.HolderWeather>(){

    private val cityNameList: ArrayList<String> = arrayListOf("New York", "Singapore", "Mumbai", "Delhi", "Sydney", "Melbourne")
    private val cityLatLonList: Array<Array<Double>> = arrayOf(
        arrayOf(40.7128, 74.0060),
        arrayOf(1.3521, 103.8198),
        arrayOf(19.0760, 72.8777),
        arrayOf(28.7041,77.1025),
        arrayOf(33.8688,151.2093),
        arrayOf(37.8136,144.9631)
    )



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderWeather {
        val view =LayoutInflater.from(context).inflate(R.layout.row_cities,parent,false)
        return HolderWeather(view)
    }

    override fun onBindViewHolder(holder: HolderWeather, position: Int) {
        val tempArray =cityLatLonList[position]
        holder.cityName.text=cityNameList[position]
        val weather= WeatherService.weatherInstance.getWeather(tempArray[0],tempArray[1])
        weather.enqueue(object : Callback<ModelWeather> {
            override fun onResponse(
                call: Call<ModelWeather>,
                response: Response<ModelWeather>
            ) {

                holder.cityMain.text=response.body()!!.current.weather[0].main
                val temp: String =(response.body()!!.current.temp-273.1).toString().substring(0,4)+"°C"
                holder.cityTemp.text=temp
                val image = response.body()!!.current.weather[0].icon.reversed()
                val resources = context.resources
                val resourceId = resources.getIdentifier(image, "drawable", context.packageName)
                holder.cityIcon.setImageResource(resourceId)
                holder.pbar.visibility=View.INVISIBLE
            }

            override fun onFailure(call: Call<ModelWeather>, t: Throwable) {
            }

        })
    }

    override fun getItemCount(): Int {
        return cityNameList.size
    }


    inner class HolderWeather(itemView: View):ViewHolder(itemView){
        var pbar=itemView.findViewById<ProgressBar>(R.id.citiesPBar)
        var cityName=itemView.findViewById<TextView>(R.id.cityName)
        var cityTemp=itemView.findViewById<TextView>(R.id.cityTemp)
        var cityMain=itemView.findViewById<TextView>(R.id.cityMain)
        var cityIcon=itemView.findViewById<ImageView>(R.id.cityIcon)
    }

}