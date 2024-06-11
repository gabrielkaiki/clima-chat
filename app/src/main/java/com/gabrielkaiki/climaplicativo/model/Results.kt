package com.gabrielkaiki.climaplicativo.model

import java.io.Serializable

class Results : Serializable {
    var temp: Int? = null
    var date: String? = null
    var time: String? = null
    var condition_code: String? = null
    var description: String? = null
    var currently: String? = null
    var city: String? = null
    var humidity: String? = null
    var wind_speedy: String? = null
    var sunrise: String? = null
    var sunset: String? = null
    var condition_slug: String? = null
    var city_name: String? = null
    var forecast: ArrayList<DiaClima> = arrayListOf()
}