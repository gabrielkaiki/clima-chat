package com.gabrielkaiki.climaplicativo.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.gabrielkaiki.climaplicativo.R

var cidadeAtual: String? = null
var notificacao: Boolean = false

fun alertDialogPadrao(contexto: Context): AlertDialog {
    var construtor = AlertDialog.Builder(contexto)
        .setCancelable(false)
        .setTitle("Aguarde enquando carrega!")
        .setView(R.layout.tela_carregando)

    var dialog = construtor.create()
    return dialog
}

fun alertDialogCustomizada(contexto: Context, titulo: String): AlertDialog {
    var construtor = AlertDialog.Builder(contexto)
        .setCancelable(false)
        .setTitle(titulo)
        .setView(R.layout.tela_carregando)

    var dialog = construtor.create()
    return dialog
}

fun alertDialogLocalizacao(contexto: Context): AlertDialog {
    var construtor = AlertDialog.Builder(contexto)
        .setCancelable(false)
        .setTitle("Recuperando a localização! Aguarde.")
        .setView(R.layout.tela_carregando)

    var dialog = construtor.create()
    return dialog
}

fun alertDialogSomenteProgressBar(contexto: Context): AlertDialog {
    var construtor = AlertDialog.Builder(contexto)
        .setCancelable(false)
        .setView(R.layout.tela_carregando)

    var dialog = construtor.create()
    return dialog
}

