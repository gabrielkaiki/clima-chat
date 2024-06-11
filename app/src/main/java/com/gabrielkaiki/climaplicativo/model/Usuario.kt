package com.gabrielkaiki.climaplicativo.model

import com.gabrielkaiki.climaplicativo.utils.getDatabaseReference
import java.io.Serializable

class Usuario : Serializable {
    var id: String? = null
    var nome: String? = null
    var estado: String? = null
    var cidade: String? = null
    var email: String? = null
    var senha: String? = null
    var imagemPerfil: String? = null

    fun salvar(): Boolean {
        try {
            val fireBase = getDatabaseReference()
            val usuarioRef = fireBase!!.child("usuarios").child(id!!)
            usuarioRef.setValue(this)
            return true
        } catch (e: Exception) {
            return false
        }
    }
}