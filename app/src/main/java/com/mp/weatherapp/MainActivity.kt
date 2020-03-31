package com.mp.weatherapp

import android.animation.ObjectAnimator
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import java.io.IOException
import java.lang.Exception



class MainActivity : AppCompatActivity() {

    var descripton: String = ""
    var temp: Int = 0
    var feelsLike: Int = 0
    var windSpeed: Int = 0
    var windDirection: Int = 0
    var windGust: Int = 0
    var sunrise: Int = 0
    var sunset: Int = 0
    var name: String = ""
    var icon: String = ""
    var id: Int = 0

    var flag: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        flag = true

        val toast = Toast.makeText(applicationContext, "Type your city and press enter", Toast.LENGTH_LONG)
        toast.setGravity(Gravity.BOTTOM, 0, 0)
        toast.show()

        val city: EditText = buttonSearch
        val weather: TextView = weather

        city.setOnKeyListener(View.OnKeyListener{v, keyCode, event ->
            try {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                    if(buttonSearch.text.toString() != ""){
                        getWeather(buttonSearch.text.toString())
                    }
                    else if(cityId.text.toString() != ""){
                        getWeather(cityId.text.toString())
                    }

                    if(flag) {
                        val toast =
                            Toast.makeText(applicationContext, "Click on arrow to update weather", Toast.LENGTH_SHORT)
                        toast.setGravity(Gravity.BOTTOM, 0, 0)
                        toast.show()
                    }

                    flag = false;

                    return@OnKeyListener true
                } else {
                    println("false")
                    return@OnKeyListener false
                }
            } catch(e: Exception){
                return@OnKeyListener false
            } finally {
                return@OnKeyListener false
            }
        })

        refreshArrow.setOnClickListener{

            val tx1 = ObjectAnimator.ofFloat(refreshArrow, View.ROTATION, 0f, 360f)
            tx1.setDuration(1000)
            tx1.start()

            val tx3 = ObjectAnimator.ofFloat(weatherIcon, View.ROTATION, 0f, 360f)
            tx3.setDuration(1000)
            tx3.start()

            println("BUTTON SEARCH")
            println(buttonSearch.text.toString())
            println(weather.text.toString())

            if(buttonSearch.text.toString() == ""){
                getWeather(cityId.text.toString())
            }
            else{
                getWeather(buttonSearch.text.toString())
            }


            val toast = Toast.makeText(applicationContext, "Refreshed", Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.BOTTOM, 0, 0)
            toast.show()
        }
    }



    private fun getWeather(city: String){
        val url = "http://api.openweathermap.org/data/2.5/weather?q=$city&appid=0fe37647bf3c4095418a1c5392bb60cc"
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        val call = client.newCall(request)

        call.enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                println(e)
                println("Request Failed")
            }

            override fun onResponse(call: Call, response: Response) {
                var body = response.body()?.string()
                var gson = GsonBuilder().create()
                var weatherAPI: weatherObj = gson.fromJson(body, weatherObj::class.java)

                var weather4 = gson.fromJson(body, weatherArray::class.java)

                try {

                    weatherAPI.main.temp = weatherAPI.main.temp - 273.15
                    weatherAPI.main.feels_like = weatherAPI.main.feels_like - 273.15


                    descripton = weather4.weather[0].description.toString()
                    temp = weatherAPI.main.temp.toInt()
                    feelsLike = weatherAPI.main.feels_like.toInt()
                    windSpeed = weatherAPI.wind.speed.toInt()
                    windDirection = weatherAPI.wind.deg.toInt()
                    windGust = weatherAPI.wind.gust.toInt()
                    sunrise = weatherAPI.sys.sunrise
                    sunset = weatherAPI.sys.sunset
                    name = weatherAPI.name
                    icon = weather4.weather[0].icon
                    id = weather4.weather[0].id

                    runOnUiThread {
                        // Stuff that updates the UI
                        weather.setText(temp.toString() + " \u2103")
                        descriptionId.setText(descripton)
                        cityId.setText(name)


                        if (id >= 200 && id < 300) {
                            weatherIcon.setImageResource(R.drawable.ic_thunderstorm_sharp)
                            weather.setBackgroundResource(R.drawable.weatherthunderstorm)
                        } else if (id >= 300 && id < 500) {
                            weatherIcon.setImageResource(R.drawable.ic_umbrella_sharp)
                            weather.setBackgroundResource(R.drawable.weatherrain)
                        } else if (id >= 500 && id < 600) {
                            weatherIcon.setImageResource(R.drawable.ic_rainy_sharp)
                            weather.setBackgroundResource(R.drawable.weatherrain)
                        } else if (id >= 600 && id < 700) {
                            weatherIcon.setImageResource(R.drawable.ic_snow_sharp)
                            weather.setBackgroundResource(R.drawable.weathersnow)
                        } else if (id >= 700 && id < 800) {
                            var uri: Uri = Uri.parse("http://openweathermap.org/img/w/$icon.png")
                            Glide.with(applicationContext)
                                .load(uri)
                                .into(weatherIcon)
                            if (id == 701) {
                                weather.setBackgroundResource(R.drawable.weathermist)
                            } else if (id == 741) {
                                weather.setBackgroundResource(R.drawable.weatherfog)
                            } else {
                                weather.setBackgroundResource(R.drawable.weather)
                            }
                        } else if (id == 800) {
                            var uri: Uri = Uri.parse("http://openweathermap.org/img/w/$icon.png")
                            Glide.with(applicationContext)
                                .load(uri)
                                .into(weatherIcon)
                            weather.setBackgroundResource(R.drawable.weatherclear)
                        } else if (id >= 801 && id < 805) {
                            weatherIcon.setImageResource(R.drawable.ic_cloudy_sharp)
                            weather.setBackgroundResource(R.drawable.weatherclear)
                        }

                    }

                }
                catch(e: Exception){
                    runOnUiThread{
                        val toast = Toast.makeText(applicationContext, "City not found", Toast.LENGTH_SHORT)
                        toast.setGravity(Gravity.BOTTOM, 0, 0)
                        toast.show()
                    }
                }finally {
                    println("Nothing")
                }

            }
        })

    }
}


data class weatherObj(
    //var coord: coord,
    //var weather: weather,
    var base: String,
    var main: main,
    var visibility: Int,
    var wind: wind,
    //var clouds: clouds,
    var dt: Int,
    var sys: sys,
    var timezone: Int,
    var id: Int,
    var name: String,
    var cod: Int
)

data class weatherArray(
    val weather: List<weather>
)

data class coord(var lon: Double, var lat: Double)
data class weather(var id: Int, var main: String, var description: String, var icon: String)
data class main(var temp: Double, var feels_like: Double, var temp_min: Double, var temp_max: Double, var pressure: Double, var humidity: Double)
data class wind(var speed: Double, var deg: Double, var gust: Double)
data class clouds(var all: Int)
data class sys(var type: Int, var id: Int, var country: String, var sunrise: Int, var sunset: Int)
