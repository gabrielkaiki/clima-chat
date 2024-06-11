package com.gabrielkaiki.climaplicativo.model

import java.io.Serializable

class DiaClima: Serializable {
    var date: String? = null
    var weekday: String? = null
    var max: Int? = null
    var min: Int? = null
    var description: String? = null
    var condition: String? = null
}