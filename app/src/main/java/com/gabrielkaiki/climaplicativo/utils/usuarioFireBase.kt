package com.gabrielkaiki.climaplicativo.utils

import com.google.firebase.auth.FirebaseUser

fun getUsuarioAtual(): FirebaseUser? {
    val autenticacao = getFirebaseAuth()!!
    return autenticacao.currentUser
}

fun getIdUsuarioLogado(): String? {
    val usuario = getUsuarioAtual()
    return usuario?.uid
}

