package com.gabrielkaiki.climaplicativo.permissoes

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object Permissoes {
    fun validarPermissoes(
        permissoes: Array<String>,
        activity: Activity?,
        requestCode: Int
    ): Boolean {
        if (Build.VERSION.SDK_INT >= 23) {
            val listaPermissoesNegadas: MutableList<String> = ArrayList()

            /*Percorre as permissões passadas,
            verificando uma a uma
            * se já tem a permissao liberada */for (permissao in permissoes) {
                val temPermissao = ContextCompat.checkSelfPermission(
                    activity!!,
                    permissao
                ) == PackageManager.PERMISSION_GRANTED
                if (!temPermissao) listaPermissoesNegadas.add(permissao)
            }

            /*Caso a lista esteja vazia, não é necessário solicitar permissão*/if (listaPermissoesNegadas.isEmpty()) return false
            var novasPermissoesNegadas: Array<String?>
            novasPermissoesNegadas = listaPermissoesNegadas.toTypedArray()

            //Solicita permissão
            ActivityCompat.requestPermissions(activity!!, novasPermissoesNegadas, requestCode)
        } else {
            return false
        }
        return true
    }
}