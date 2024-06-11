package com.gabrielkaiki.youtubeapp.fireBase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ConfiguracaoFireBase {
    companion object {
        var auth: FirebaseAuth? = null
        var realTimeDB: DatabaseReference? = null

        fun getAutenticacao(): FirebaseAuth? {
            if (auth == null) auth = FirebaseAuth.getInstance()

            return auth
        }


        fun getFireBase(): DatabaseReference? {
            if (realTimeDB == null) realTimeDB = Firebase.database.reference

            return realTimeDB
        }
    }
}