package com.gabrielkaiki.climaplicativo.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

private var dataBase: DatabaseReference? = null
private var auth: FirebaseAuth? = null
private var storage: StorageReference? = null

fun getDatabaseReference(): DatabaseReference? {
    if (dataBase == null) {
        dataBase = Firebase.database.reference
    }
    return dataBase
}

fun getFirebaseAuth(): FirebaseAuth? {
    if (auth == null) {
        auth = FirebaseAuth.getInstance()
    }
    var teste = auth
    return auth
}

fun getStorageReference(): StorageReference? {
    if (storage == null) {
        storage = FirebaseStorage.getInstance().getReference()
    }
    return storage
}