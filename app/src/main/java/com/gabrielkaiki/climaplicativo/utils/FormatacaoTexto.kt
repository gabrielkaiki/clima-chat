package com.gabrielkaiki.climaplicativo.utils

import java.text.Normalizer

fun removerAcentosEEspacos(cidade: String): String {

    var cidadeFormatada =
        Normalizer.normalize(cidade, Normalizer.Form.NFD)
    cidadeFormatada = cidadeFormatada.replace(
        "[\\p{InCombiningDiacriticalMarks}]".toRegex(),
        ""
    )

    cidadeFormatada = cidadeFormatada.replace("(\\s)".toRegex(), "")

    return cidadeFormatada
}