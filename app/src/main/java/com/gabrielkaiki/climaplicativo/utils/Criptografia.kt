package com.gabrielkaiki.climaplicativo.utils

import android.util.Base64

fun encode(senha: String): String {
    return Base64.encodeToString(senha.toByteArray(), Base64.DEFAULT)
        .replace("(\\n|\\r)".toRegex(), "")
}

fun decode(senha: String): String {
    return String(Base64.decode(senha, Base64.DEFAULT))
}