package com.mp.weatherapp

import android.animation.ObjectAnimator
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.transition.Explode
import android.view.*
import android.view.animation.AlphaAnimation
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
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.*
import java.util.EnumSet.range
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {

    private var PRIVATE_MODE = 0
    private val PREF_NAME = "testin123"

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



        val sharedPref= getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)


        var cityStored = sharedPref.getString("city","gg")


        println("OPENING APP $cityStored")


        if(sharedPref.getString("city","toronto") != "gg"){
            println("OPENING APP $cityStored")

            if (cityStored != null) {
                println("OPENING APP $cityStored")
                getWeather(cityStored, sharedPref)
            }
        }

        println("STARTING APP shared preference is ${sharedPref.getString("city","")}")

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
                        getWeather(buttonSearch.text.toString(), sharedPref)
                    }
                    else if(cityId.text.toString() != ""){
                        getWeather(cityId.text.toString(), sharedPref)
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
                getWeather(cityId.text.toString(), sharedPref)
            }
            else{
                getWeather(buttonSearch.text.toString(), sharedPref)
            }


            val toast = Toast.makeText(applicationContext, "Refreshed", Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.BOTTOM, 0, 0)
            toast.show()
        }
    }



    private fun getWeather(city: String, pref: SharedPreferences){
        var lat: Double = 0.0
        var lon: Double = 0.0
        val client = OkHttpClient()

        val cityCoord = "http://api.openweathermap.org/data/2.5/weather?q=$city&appid=0fe37647bf3c4095418a1c5392bb60cc"
        val requestCoord = Request.Builder().url(cityCoord).build()
        val callCoord = client.newCall(requestCoord)

        var edit = pref.edit()



        callCoord.enqueue(object: Callback{
            override fun onFailure(call: Call, e: IOException) {
                println(e)
                println("Request Failed")
            }

            override fun onResponse(call: Call, response: Response) {
                var body = response.body()?.string()
                var gson = GsonBuilder().create()
                var weatherAPI: weatherObj = gson.fromJson(body, weatherObj::class.java)
                var weather4 = gson.fromJson(body, weatherArray::class.java)
                icon = weather4.weather[0].icon
                println("-------------------COORD------------------------")
                println(weatherAPI.coord)
                lat = weatherAPI.coord.lat
                lon = weatherAPI.coord.lon
                name = weatherAPI.name

                getForecast(lat, lon, city, pref, icon)
            }

        })

        edit.putString("city",city)
        println("-------------------CITY STORED------------------------")
        edit.commit()

        println(pref.getString("city","gg"))

    }

    private fun getForecast(lat: Double, lon: Double, city: String, pref: SharedPreferences, icon: String){
        val client = OkHttpClient()
        val url = "http://api.openweathermap.org/data/2.5/onecall?lat=$lat&lon=$lon&appid=0fe37647bf3c4095418a1c5392bb60cc"
        val request = Request.Builder().url(url).build()
        val call = client.newCall(request)

        call.enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                println(e)
                println("Request Failed")
            }

            override fun onResponse(call: Call, response: Response) {
                var body = response.body()?.string()
                var gson = GsonBuilder().create()
                //var weatherAPI: weatherObj = gson.fromJson(body, weatherObj::class.java)

                // var weather4 = gson.fromJson(body, weatherArray::class.java)

                var one: oneCall = gson.fromJson(body, oneCall::class.java)

                println("$city")
                println("-------------------------ONE---------------CALL--------------------------------------------")
                println(one.daily[1])

                try {



                    one.current.temp = one.current.temp - 273.15
                    one.current.feels_like = one.current.feels_like - 273.15

                    one.daily[0].temp.max = one.daily[0].temp.max - 273.15
                    one.daily[0].temp.min = one.daily[0].temp.min - 273.15

                    one.daily[1].temp.max = one.daily[1].temp.max - 273.15
                    one.daily[1].temp.min = one.daily[1].temp.min - 273.15

                    one.daily[2].temp.max = one.daily[2].temp.max - 273.15
                    one.daily[2].temp.min = one.daily[2].temp.min - 273.15

                    one.daily[3].temp.max = one.daily[3].temp.max - 273.15
                    one.daily[3].temp.min = one.daily[3].temp.min - 273.15

                    one.daily[4].temp.max = one.daily[4].temp.max - 273.15
                    one.daily[4].temp.min = one.daily[4].temp.min - 273.15
//
//
                    descripton = one.current.weather[0].description
                    temp = one.current.temp.toInt()
                    feelsLike = one.current.feels_like.toInt()
//                    windSpeed = weatherAPI.wind.speed.toInt()
//                    windDirection = weatherAPI.wind.deg.toInt()
//                    windGust = weatherAPI.wind.gust.toInt()
//                    sunrise = weatherAPI.sys.sunrise
//                    sunset = weatherAPI.sys.sunset
//
//
                    id = one.current.weather[0].id



                    runOnUiThread {
                        // Stuff that updates the UI
                        weather.setText(temp.toString() + " \u2103")
                        descriptionId.setText(descripton)
                        cityId.setText(city)

//                        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                        val date = java.util.Date(one.daily[0].dt * 1000L)
//                        sdf.format(date)
//                        println("OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO")
//                        println(sdf.format(date))
//
                        val dateInfo = DateFormat.getDateInstance(DateFormat.FULL).format(date)

                        val date2 = java.util.Date(one.daily[1].dt * 1000L)
//                        sdf.format(date)
//                        println("OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO")
//                        println(sdf.format(date))
//
                        val dateInfo2 = DateFormat.getDateInstance(DateFormat.FULL).format(date2)

                        val date3 = java.util.Date(one.daily[2].dt * 1000L)
//                        sdf.format(date)
//                        println("OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO")
//                        println(sdf.format(date))
//
                        val dateInfo3 = DateFormat.getDateInstance(DateFormat.FULL).format(date3)

                        val date4 = java.util.Date(one.daily[3].dt * 1000L)
//                        sdf.format(date)
//                        println("OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO")
//                        println(sdf.format(date))
//
                        val dateInfo4 = DateFormat.getDateInstance(DateFormat.FULL).format(date4)

                        val date5 = java.util.Date(one.daily[4].dt * 1000L)
//                        sdf.format(date)
//                        println("OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO")
//                        println(sdf.format(date))
//
                        val dateInfo5 = DateFormat.getDateInstance(DateFormat.FULL).format(date5)

                        println(dateInfo.substringBefore(","))

                        dayOne.setText(dateInfo.substringBefore(","))
                        dayOneCondition.setText(one.daily[0].weather[0].description)
                        dayOneHigh.setText(one.daily[0].temp.max.roundToInt().toString())
                        dayOneLow.setText(one.daily[0].temp.min.roundToInt().toString())
                        if (one.daily[0].weather[0].id >= 200 && one.daily[0].weather[0].id < 300) {
                            dayOneIcon.setImageResource(R.drawable.ic_thunderstorm_sharp)
                            //weather.setBackgroundResource(R.drawable.weatherthunderstorm)
                        } else if (one.daily[0].weather[0].id >= 300 && one.daily[0].weather[0].id < 500) {
                            dayOneIcon.setImageResource(R.drawable.ic_umbrella_sharp)
                            //weather.setBackgroundResource(R.drawable.weatherrain)
                        } else if (one.daily[0].weather[0].id >= 500 && one.daily[0].weather[0].id < 600) {
                            dayOneIcon.setImageResource(R.drawable.ic_rainy_sharp)
                            //weather.setBackgroundResource(R.drawable.weatherrain)
                        } else if (one.daily[0].weather[0].id >= 600 && one.daily[0].weather[0].id < 700) {
                            dayOneIcon.setImageResource(R.drawable.ic_snow_sharp)
                            //weather.setBackgroundResource(R.drawable.weathersnow)
                        } else if (one.daily[0].weather[0].id >= 700 && one.daily[0].weather[0].id < 800) {
                            var uri: Uri = Uri.parse("http://openweathermap.org/img/w/$icon.png")
                            Glide.with(applicationContext)
                                .load(uri)
                                .into(dayOneIcon)
                            if (one.daily[0].weather[0].id == 701) {
                                weather.setBackgroundResource(R.drawable.weathermist)
                            } else if (one.daily[0].weather[0].id == 741) {
                                weather.setBackgroundResource(R.drawable.weatherfog)
                            } else {
                                weather.setBackgroundResource(R.drawable.weather)
                            }
                        } else if (one.daily[0].weather[0].id == 800) {
//                            var uri: Uri = Uri.parse("http://openweathermap.org/img/w/$icon.png")
//                            Glide.with(applicationContext)
//                                .load(uri)
//                                .into(weatherIcon)
                            dayOneIcon.setImageResource(R.drawable.ic_sunny_sharp)
                            //weather.setBackgroundResource(R.drawable.weatherclear)
                        } else if (one.daily[0].weather[0].id >= 801 && one.daily[0].weather[0].id < 805) {
                            dayOneIcon.setImageResource(R.drawable.ic_cloudy_sharp)
                            //weather.setBackgroundResource(R.drawable.weatherclear)
                        }

                        dayTwo.setText(dateInfo2.substringBefore(","))
                        dayTwoCondition.setText(one.daily[1].weather[0].description)
                        dayTwoHigh.setText(one.daily[1].temp.max.roundToInt().toString())
                        dayTwoLow.setText(one.daily[1].temp.min.roundToInt().toString())
                        if (one.daily[1].weather[0].id >= 200 && one.daily[1].weather[0].id < 300) {
                            dayTwoIcon.setImageResource(R.drawable.ic_thunderstorm_sharp)
                            //weather.setBackgroundResource(R.drawable.weatherthunderstorm)
                        } else if (one.daily[1].weather[0].id >= 300 && one.daily[1].weather[0].id < 500) {
                            dayTwoIcon.setImageResource(R.drawable.ic_umbrella_sharp)
                            //weather.setBackgroundResource(R.drawable.weatherrain)
                        } else if (one.daily[1].weather[0].id >= 500 && one.daily[1].weather[0].id < 600) {
                            dayTwoIcon.setImageResource(R.drawable.ic_rainy_sharp)
                            //weather.setBackgroundResource(R.drawable.weatherrain)
                        } else if (one.daily[1].weather[0].id >= 600 && one.daily[1].weather[0].id < 700) {
                            dayTwoIcon.setImageResource(R.drawable.ic_snow_sharp)
                            //weather.setBackgroundResource(R.drawable.weathersnow)
                        } else if (one.daily[1].weather[0].id >= 700 && one.daily[1].weather[0].id < 800) {
                            var uri: Uri = Uri.parse("http://openweathermap.org/img/w/$icon.png")
                            Glide.with(applicationContext)
                                .load(uri)
                                .into(dayTwoIcon)
                            if (one.daily[1].weather[0].id == 701) {
                                weather.setBackgroundResource(R.drawable.weathermist)
                            } else if (one.daily[1].weather[0].id == 741) {
                                weather.setBackgroundResource(R.drawable.weatherfog)
                            } else {
                                weather.setBackgroundResource(R.drawable.weather)
                            }
                        } else if (one.daily[1].weather[0].id == 800) {
//                            var uri: Uri = Uri.parse("http://openweathermap.org/img/w/$icon.png")
//                            Glide.with(applicationContext)
//                                .load(uri)
//                                .into(weatherIcon)
                            dayTwoIcon.setImageResource(R.drawable.ic_sunny_sharp)
                            //weather.setBackgroundResource(R.drawable.weatherclear)
                        } else if (one.daily[1].weather[0].id >= 801 && one.daily[1].weather[0].id < 805) {
                            dayTwoIcon.setImageResource(R.drawable.ic_cloudy_sharp)
                            //weather.setBackgroundResource(R.drawable.weatherclear)
                        }

                        dayThree.setText(dateInfo3.substringBefore(","))
                        dayThreeCondition.setText(one.daily[2].weather[0].description)
                        dayThreeHigh.setText(one.daily[2].temp.max.roundToInt().toString())
                        dayThreeLow.setText(one.daily[2].temp.min.roundToInt().toString())

                        if (one.daily[2].weather[0].id >= 200 && one.daily[2].weather[0].id < 300) {
                            dayThreeIcon.setImageResource(R.drawable.ic_thunderstorm_sharp)
                            //weather.setBackgroundResource(R.drawable.weatherthunderstorm)
                        } else if (one.daily[2].weather[0].id >= 300 && one.daily[2].weather[0].id < 500) {
                            dayThreeIcon.setImageResource(R.drawable.ic_umbrella_sharp)
                            //weather.setBackgroundResource(R.drawable.weatherrain)
                        } else if (one.daily[2].weather[0].id >= 500 && one.daily[2].weather[0].id < 600) {
                            dayThreeIcon.setImageResource(R.drawable.ic_rainy_sharp)
                            //weather.setBackgroundResource(R.drawable.weatherrain)
                        } else if (one.daily[2].weather[0].id >= 600 && one.daily[2].weather[0].id < 700) {
                            dayThreeIcon.setImageResource(R.drawable.ic_snow_sharp)
                            //weather.setBackgroundResource(R.drawable.weathersnow)
                        } else if (one.daily[2].weather[0].id >= 700 && one.daily[2].weather[0].id < 800) {
                            var uri: Uri = Uri.parse("http://openweathermap.org/img/w/$icon.png")
                            Glide.with(applicationContext)
                                .load(uri)
                                .into(dayThreeIcon)
                            if (one.daily[2].weather[0].id == 701) {
                                weather.setBackgroundResource(R.drawable.weathermist)
                            } else if (one.daily[2].weather[0].id == 741) {
                                weather.setBackgroundResource(R.drawable.weatherfog)
                            } else {
                                weather.setBackgroundResource(R.drawable.weather)
                            }
                        } else if (one.daily[2].weather[0].id == 800) {
//                            var uri: Uri = Uri.parse("http://openweathermap.org/img/w/$icon.png")
//                            Glide.with(applicationContext)
//                                .load(uri)
//                                .into(weatherIcon)
                            dayThreeIcon.setImageResource(R.drawable.ic_sunny_sharp)
                            //weather.setBackgroundResource(R.drawable.weatherclear)
                        } else if (one.daily[2].weather[0].id >= 801 && one.daily[2].weather[0].id < 805) {
                            dayThreeIcon.setImageResource(R.drawable.ic_cloudy_sharp)
                            //weather.setBackgroundResource(R.drawable.weatherclear)
                        }


                        dayFour.setText(dateInfo4.substringBefore(","))
                        dayFourCondition.setText(one.daily[3].weather[0].description)
                        dayFourHigh.setText(one.daily[3].temp.max.roundToInt().toString())
                        dayFourLow.setText(one.daily[3].temp.min.roundToInt().toString())

                        if (one.daily[3].weather[0].id >= 200 && one.daily[3].weather[0].id < 300) {
                            dayFourIcon.setImageResource(R.drawable.ic_thunderstorm_sharp)
                            //weather.setBackgroundResource(R.drawable.weatherthunderstorm)
                        } else if (one.daily[3].weather[0].id >= 300 && one.daily[3].weather[0].id < 500) {
                            dayFourIcon.setImageResource(R.drawable.ic_umbrella_sharp)
                            //weather.setBackgroundResource(R.drawable.weatherrain)
                        } else if (one.daily[3].weather[0].id >= 500 && one.daily[3].weather[0].id < 600) {
                            dayFourIcon.setImageResource(R.drawable.ic_rainy_sharp)
                            //weather.setBackgroundResource(R.drawable.weatherrain)
                        } else if (one.daily[3].weather[0].id >= 600 && one.daily[3].weather[0].id < 700) {
                            dayFourIcon.setImageResource(R.drawable.ic_snow_sharp)
                            //weather.setBackgroundResource(R.drawable.weathersnow)
                        } else if (one.daily[3].weather[0].id >= 700 && one.daily[3].weather[0].id < 800) {
                            var uri: Uri = Uri.parse("http://openweathermap.org/img/w/$icon.png")
                            Glide.with(applicationContext)
                                .load(uri)
                                .into(dayFourIcon)
                            if (one.daily[3].weather[0].id == 701) {
                                weather.setBackgroundResource(R.drawable.weathermist)
                            } else if (one.daily[3].weather[0].id == 741) {
                                weather.setBackgroundResource(R.drawable.weatherfog)
                            } else {
                                weather.setBackgroundResource(R.drawable.weather)
                            }
                        } else if (one.daily[3].weather[0].id == 800) {
//                            var uri: Uri = Uri.parse("http://openweathermap.org/img/w/$icon.png")
//                            Glide.with(applicationContext)
//                                .load(uri)
//                                .into(weatherIcon)
                            dayFourIcon.setImageResource(R.drawable.ic_sunny_sharp)
                            //weather.setBackgroundResource(R.drawable.weatherclear)
                        } else if (one.daily[3].weather[0].id >= 801 && one.daily[3].weather[0].id < 805) {
                            dayFourIcon.setImageResource(R.drawable.ic_cloudy_sharp)
                            //weather.setBackgroundResource(R.drawable.weatherclear)
                        }

                        dayFive.setText(dateInfo5.substringBefore(","))
                        dayFiveCondition.setText(one.daily[4].weather[0].description)
                        dayFiveHigh.setText(one.daily[4].temp.max.roundToInt().toString())
                        dayFiveLow.setText(one.daily[4].temp.min.roundToInt().toString())


                        if (one.daily[4].weather[0].id >= 200 && one.daily[4].weather[0].id < 300) {
                            dayFiveIcon.setImageResource(R.drawable.ic_thunderstorm_sharp)
                            //weather.setBackgroundResource(R.drawable.weatherthunderstorm)
                        } else if (one.daily[4].weather[0].id >= 300 && one.daily[4].weather[0].id < 500) {
                            dayFiveIcon.setImageResource(R.drawable.ic_umbrella_sharp)
                            //weather.setBackgroundResource(R.drawable.weatherrain)
                        } else if (one.daily[4].weather[0].id >= 500 && one.daily[4].weather[0].id < 600) {
                            dayFiveIcon.setImageResource(R.drawable.ic_rainy_sharp)
                            //weather.setBackgroundResource(R.drawable.weatherrain)
                        } else if (one.daily[4].weather[0].id >= 600 && one.daily[4].weather[0].id < 700) {
                            dayFiveIcon.setImageResource(R.drawable.ic_snow_sharp)
                            //weather.setBackgroundResource(R.drawable.weathersnow)
                        } else if (one.daily[4].weather[0].id >= 700 && one.daily[4].weather[0].id < 800) {
                            var uri: Uri = Uri.parse("http://openweathermap.org/img/w/$icon.png")
                            Glide.with(applicationContext)
                                .load(uri)
                                .into(dayFiveIcon)
                            if (one.daily[4].weather[0].id == 701) {
                                weather.setBackgroundResource(R.drawable.weathermist)
                            } else if (one.daily[4].weather[0].id == 741) {
                                weather.setBackgroundResource(R.drawable.weatherfog)
                            } else {
                                weather.setBackgroundResource(R.drawable.weather)
                            }
                        } else if (one.daily[4].weather[0].id == 800) {
//                            var uri: Uri = Uri.parse("http://openweathermap.org/img/w/$icon.png")
//                            Glide.with(applicationContext)
//                                .load(uri)
//                                .into(weatherIcon)
                            dayFiveIcon.setImageResource(R.drawable.ic_sunny_sharp)
                            //weather.setBackgroundResource(R.drawable.weatherclear)
                        } else if (one.daily[4].weather[0].id >= 801 && one.daily[4].weather[0].id < 805) {
                            dayFiveIcon.setImageResource(R.drawable.ic_cloudy_sharp)
                            //weather.setBackgroundResource(R.drawable.weatherclear)
                        }

//                        for (i in 0..5){
//                            dayOne.setText(one.daily[i].dt)
//                        }

                        //---------------------------------------------------------------------------------------------------------------------------------------------------------------

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
//                            var uri: Uri = Uri.parse("http://openweathermap.org/img/w/$icon.png")
//                            Glide.with(applicationContext)
//                                .load(uri)
//                                .into(weatherIcon)
                            weatherIcon.setImageResource(R.drawable.ic_sunny_sharp)
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
    var coord: coord,
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
    var cod: Int,
    var dt_txt: String
)

data class weatherForecast(
    //
    var cod: String,
    var message: Int,
    var cnt: Int,
    //var list: List<weatherObj>,
    var city: city
)

data class forecastArray(
    var list: List<weatherObj>
)

data class weatherArray(
    val weather: List<weather>

)

data class dailyObj(
    var dt: Long,
    var sunrise: Long,
    var sunset: Long,
    var temp: temp,
    var feels_like: feels,
    var pressure: Int,
    var humidity: Int,
    var dew_point: Double,
    var wind_speed: Double,
    var wind_deg: Int,
    var weather: List<weather>,
    var clouds: Int,
    var rain: Double,
    var uvi: Double
)

data class oneCall(
    var lat: Double,
    var lon: Double,
    var timezone: String,
    var current: main,
    var daily: List<dailyObj>
)

data class coord(var lon: Double, var lat: Double)
data class weather(var id: Int, var main: String, var description: String, var icon: String)
data class main(var temp: Double, var feels_like: Double, var pressure: Double, var humidity: Double, var weather: List<weather>)
data class wind(var speed: Double, var deg: Double, var gust: Double)
data class clouds(var all: Int)
data class city(var id: Int, var name: String, var coord: coord, var country: String, var population: Int, var timezone: Int, var sunrise: Int, var sunset: Int)
data class sys(var type: Int, var id: Int, var country: String, var sunrise: Int, var sunset: Int)

data class temp(var day: Double, var min: Double, var max: Double, var night: Double, var eve: Double, var morn: Double)
data class feels(var day: Double, var night: Double, var eve: Double, var morn: Double)
