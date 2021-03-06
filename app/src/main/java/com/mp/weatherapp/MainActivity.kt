package com.mp.weatherapp

import android.animation.ObjectAnimator
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import java.io.IOException
import java.lang.Exception
import java.text.DateFormat
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {

    private val PREF_NAME = "testin123"

    var descripton: String = ""
    var temp: Int = 0
    var feelsLike: Int = 0
//  var windSpeed: Int = 0
//  var windDirection: Int = 0
//  var windGust: Int = 0
//  var sunrise: Int = 0
//  var sunset: Int = 0
    var name: String = ""
    var icon: String = ""
    var id: Int = 0

    var flag: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val sharedPref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        var cityStored = sharedPref.getString("city", "gg")

        if (sharedPref.getString("city", "toronto") != "gg") {
            if (cityStored != null) {
                getWeather(cityStored, sharedPref)
            }
        }
        flag = true

        val toast = Toast.makeText(applicationContext, "Type your city and press enter", Toast.LENGTH_LONG)
        toast.setGravity(Gravity.BOTTOM, 0, 0)
        toast.show()

        val city: EditText = buttonSearch
//      val weather: TextView = weather

        city.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            try {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                    if (buttonSearch.text.toString() != "") {
                        getWeather(buttonSearch.text.toString(), sharedPref)
                    } else if (cityId.text.toString() != "") {
                        getWeather(cityId.text.toString(), sharedPref)
                    }

                    if (flag) {
                        val toast = Toast.makeText(applicationContext, "Click on arrow to update weather", Toast.LENGTH_SHORT)
                        toast.setGravity(Gravity.BOTTOM, 0, 0)
                        toast.show()
                    }
                    flag = false;
                    return@OnKeyListener true
                } else {
                    return@OnKeyListener false
                }
            } catch (e: Exception) {
                return@OnKeyListener false
            } finally {
                return@OnKeyListener false
            }
        })

        refreshArrow.setOnClickListener {

            val tx1 = ObjectAnimator.ofFloat(refreshArrow, View.ROTATION, 0f, 360f)
            tx1.setDuration(1000)
            tx1.start()

            val tx3 = ObjectAnimator.ofFloat(weatherIcon, View.ROTATION, 0f, 360f)
            tx3.setDuration(1000)
            tx3.start()

            if (buttonSearch.text.toString() == "") {
                getWeather(cityId.text.toString(), sharedPref)
            } else {
                getWeather(buttonSearch.text.toString(), sharedPref)
            }

            val toast = Toast.makeText(applicationContext, "Refreshed", Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.BOTTOM, 0, 0)
            toast.show()
        }
    }


    private fun getWeather(city: String, pref: SharedPreferences) {
        var lat: Double = 0.0
        var lon: Double = 0.0
        val client = OkHttpClient()

        val cityCoord = "http://api.openweathermap.org/data/2.5/weather?q=$city&appid=GET YOUR OWN KEY"
        val requestCoord = Request.Builder().url(cityCoord).build()
        val callCoord = client.newCall(requestCoord)

        var edit = pref.edit()

        callCoord.enqueue(object : Callback {
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
                    icon = weather4.weather[0].icon
                    lat = weatherAPI.coord.lat
                    lon = weatherAPI.coord.lon
                    name = weatherAPI.name

                    getForecast(lat, lon, city, pref, icon)
                } catch (e: Exception) {
                    runOnUiThread {
                        val toast =
                            Toast.makeText(applicationContext, "City not found", Toast.LENGTH_SHORT)
                        toast.setGravity(Gravity.BOTTOM, 0, 0)
                        toast.show()
                    }
                } finally {
                    println("Nothing")
                }
            }

        })
        edit.putString("city", city)
        edit.commit()
    }

    private fun getForecast(lat: Double, lon: Double, city: String, pref: SharedPreferences, icon: String) {
        val client = OkHttpClient()
        val url = "http://api.openweathermap.org/data/2.5/onecall?lat=$lat&lon=$lon&appid=GET YOUR OWN KEY"
        val request = Request.Builder().url(url).build()
        val call = client.newCall(request)

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println(e)
                println("Request Failed")
            }

            override fun onResponse(call: Call, response: Response) {
                var body = response.body()?.string()
                var gson = GsonBuilder().create()
                var one: oneCall = gson.fromJson(body, oneCall::class.java)
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

                    descripton = one.current.weather[0].description
                    temp = one.current.temp.toInt()
                    feelsLike = one.current.feels_like.toInt()
//                    windSpeed = weatherAPI.wind.speed.toInt()
//                    windDirection = weatherAPI.wind.deg.toInt()
//                    windGust = weatherAPI.wind.gust.toInt()
//                    sunrise = weatherAPI.sys.sunrise
//                    sunset = weatherAPI.sys.sunset
                    id = one.current.weather[0].id
                    val date = java.util.Date(one.daily[0].dt * 1000L)
                    val dateInfo = DateFormat.getDateInstance(DateFormat.FULL).format(date)

                    val date2 = java.util.Date(one.daily[1].dt * 1000L)
                    val dateInfo2 = DateFormat.getDateInstance(DateFormat.FULL).format(date2)

                    val date3 = java.util.Date(one.daily[2].dt * 1000L)
                    val dateInfo3 = DateFormat.getDateInstance(DateFormat.FULL).format(date3)

                    val date4 = java.util.Date(one.daily[3].dt * 1000L)
                    val dateInfo4 = DateFormat.getDateInstance(DateFormat.FULL).format(date4)

                    val date5 = java.util.Date(one.daily[4].dt * 1000L)
                    val dateInfo5 = DateFormat.getDateInstance(DateFormat.FULL).format(date5)

                    runOnUiThread {
                        // Stuff that updates the UI
                        weather.setText(temp.toString() + " \u2103")
                        descriptionId.setText(descripton)
                        cityId.setText(city)


                        dayOne.setText(dateInfo.substringBefore(","))
                        dayOneCondition.setText(one.daily[0].weather[0].description)
                        day1High.setText(one.daily[0].temp.max.roundToInt().toString())
                        day1Low.setText(one.daily[0].temp.min.roundToInt().toString())
                        if (one.daily[0].weather[0].id >= 200 && one.daily[0].weather[0].id < 300) {
                            dayOneIcon.setImageResource(R.drawable.ic_thunderstorm_sharp)
                        } else if (one.daily[0].weather[0].id >= 300 && one.daily[0].weather[0].id < 500) {
                            dayOneIcon.setImageResource(R.drawable.ic_umbrella_sharp)
                        } else if (one.daily[0].weather[0].id >= 500 && one.daily[0].weather[0].id < 600) {
                            dayOneIcon.setImageResource(R.drawable.ic_rainy_sharp)
                        } else if (one.daily[0].weather[0].id >= 600 && one.daily[0].weather[0].id < 700) {
                            dayOneIcon.setImageResource(R.drawable.ic_snow_sharp)
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
                            dayOneIcon.setImageResource(R.drawable.ic_sunny_sharp)
                        } else if (one.daily[0].weather[0].id >= 801 && one.daily[0].weather[0].id < 805) {
                            dayOneIcon.setImageResource(R.drawable.ic_cloudy_sharp)
                        }

                        dayTwo.setText(dateInfo2.substringBefore(","))
                        dayTwoCondition.setText(one.daily[1].weather[0].description)
                        day2High.setText(one.daily[1].temp.max.roundToInt().toString())
                        day2Low.setText(one.daily[1].temp.min.roundToInt().toString())
                        if (one.daily[1].weather[0].id >= 200 && one.daily[1].weather[0].id < 300) {
                            dayTwoIcon.setImageResource(R.drawable.ic_thunderstorm_sharp)
                        } else if (one.daily[1].weather[0].id >= 300 && one.daily[1].weather[0].id < 500) {
                            dayTwoIcon.setImageResource(R.drawable.ic_umbrella_sharp)
                        } else if (one.daily[1].weather[0].id >= 500 && one.daily[1].weather[0].id < 600) {
                            dayTwoIcon.setImageResource(R.drawable.ic_rainy_sharp)
                        } else if (one.daily[1].weather[0].id >= 600 && one.daily[1].weather[0].id < 700) {
                            dayTwoIcon.setImageResource(R.drawable.ic_snow_sharp)
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
                            dayTwoIcon.setImageResource(R.drawable.ic_sunny_sharp)
                        } else if (one.daily[1].weather[0].id >= 801 && one.daily[1].weather[0].id < 805) {
                            dayTwoIcon.setImageResource(R.drawable.ic_cloudy_sharp)
                        }

                        dayThree.setText(dateInfo3.substringBefore(","))
                        dayThreeCondition.setText(one.daily[2].weather[0].description)
                        day3High.setText(one.daily[2].temp.max.roundToInt().toString())
                        day3Low.setText(one.daily[2].temp.min.roundToInt().toString())

                        if (one.daily[2].weather[0].id >= 200 && one.daily[2].weather[0].id < 300) {
                            dayThreeIcon.setImageResource(R.drawable.ic_thunderstorm_sharp)
                        } else if (one.daily[2].weather[0].id >= 300 && one.daily[2].weather[0].id < 500) {
                            dayThreeIcon.setImageResource(R.drawable.ic_umbrella_sharp)
                        } else if (one.daily[2].weather[0].id >= 500 && one.daily[2].weather[0].id < 600) {
                            dayThreeIcon.setImageResource(R.drawable.ic_rainy_sharp)
                        } else if (one.daily[2].weather[0].id >= 600 && one.daily[2].weather[0].id < 700) {
                            dayThreeIcon.setImageResource(R.drawable.ic_snow_sharp)
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
                            dayThreeIcon.setImageResource(R.drawable.ic_sunny_sharp)
                        } else if (one.daily[2].weather[0].id >= 801 && one.daily[2].weather[0].id < 805) {
                            dayThreeIcon.setImageResource(R.drawable.ic_cloudy_sharp)
                        }


                        dayFour.setText(dateInfo4.substringBefore(","))
                        dayFourCondition.setText(one.daily[3].weather[0].description)
                        day4High.setText(one.daily[3].temp.max.roundToInt().toString())
                        day4Low.setText(one.daily[3].temp.min.roundToInt().toString())

                        if (one.daily[3].weather[0].id >= 200 && one.daily[3].weather[0].id < 300) {
                            dayFourIcon.setImageResource(R.drawable.ic_thunderstorm_sharp)
                        } else if (one.daily[3].weather[0].id >= 300 && one.daily[3].weather[0].id < 500) {
                            dayFourIcon.setImageResource(R.drawable.ic_umbrella_sharp)
                        } else if (one.daily[3].weather[0].id >= 500 && one.daily[3].weather[0].id < 600) {
                            dayFourIcon.setImageResource(R.drawable.ic_rainy_sharp)
                        } else if (one.daily[3].weather[0].id >= 600 && one.daily[3].weather[0].id < 700) {
                            dayFourIcon.setImageResource(R.drawable.ic_snow_sharp)
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
                            dayFourIcon.setImageResource(R.drawable.ic_sunny_sharp)
                        } else if (one.daily[3].weather[0].id >= 801 && one.daily[3].weather[0].id < 805) {
                            dayFourIcon.setImageResource(R.drawable.ic_cloudy_sharp)
                        }

                        dayFive.setText(dateInfo5.substringBefore(","))
                        dayFiveCondition.setText(one.daily[4].weather[0].description)
                        day5High.setText(one.daily[4].temp.max.roundToInt().toString())
                        day5Low.setText(one.daily[4].temp.min.roundToInt().toString())


                        if (one.daily[4].weather[0].id >= 200 && one.daily[4].weather[0].id < 300) {
                            dayFiveIcon.setImageResource(R.drawable.ic_thunderstorm_sharp)
                        } else if (one.daily[4].weather[0].id >= 300 && one.daily[4].weather[0].id < 500) {
                            dayFiveIcon.setImageResource(R.drawable.ic_umbrella_sharp)
                        } else if (one.daily[4].weather[0].id >= 500 && one.daily[4].weather[0].id < 600) {
                            dayFiveIcon.setImageResource(R.drawable.ic_rainy_sharp)
                        } else if (one.daily[4].weather[0].id >= 600 && one.daily[4].weather[0].id < 700) {
                            dayFiveIcon.setImageResource(R.drawable.ic_snow_sharp)
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
                            dayFiveIcon.setImageResource(R.drawable.ic_sunny_sharp)
                        } else if (one.daily[4].weather[0].id >= 801 && one.daily[4].weather[0].id < 805) {
                            dayFiveIcon.setImageResource(R.drawable.ic_cloudy_sharp)
                        }

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
                            weatherIcon.setImageResource(R.drawable.ic_sunny_sharp)
                            weather.setBackgroundResource(R.drawable.weatherclear)
                        } else if (id >= 801 && id < 805) {
                            weatherIcon.setImageResource(R.drawable.ic_cloudy_sharp)
                            weather.setBackgroundResource(R.drawable.weatherclear)
                        }

                    }

                } catch (e: Exception) {
                    runOnUiThread {
                        val toast = Toast.makeText(applicationContext, "City not found", Toast.LENGTH_SHORT)
                        toast.setGravity(Gravity.BOTTOM, 0, 0)
                        toast.show()
                    }
                } finally {
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
data class main(
    var temp: Double,
    var feels_like: Double,
    var pressure: Double,
    var humidity: Double,
    var weather: List<weather>
)

data class wind(var speed: Double, var deg: Double, var gust: Double)
data class clouds(var all: Int)
data class city(
    var id: Int,
    var name: String,
    var coord: coord,
    var country: String,
    var population: Int,
    var timezone: Int,
    var sunrise: Int,
    var sunset: Int
)

data class sys(var type: Int, var id: Int, var country: String, var sunrise: Int, var sunset: Int)

data class temp(
    var day: Double,
    var min: Double,
    var max: Double,
    var night: Double,
    var eve: Double,
    var morn: Double
)

data class feels(var day: Double, var night: Double, var eve: Double, var morn: Double)
