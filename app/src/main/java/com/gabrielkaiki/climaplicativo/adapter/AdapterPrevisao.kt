package com.gabrielkaiki.climaplicativo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gabrielkaiki.climaplicativo.R
import com.gabrielkaiki.climaplicativo.model.DiaClima
import com.gabrielkaiki.climaplicativo.utils.descricaoCondicao

class AdapterPrevisao(var previsoes: ArrayList<DiaClima>, var contexto: Context) :
    RecyclerView.Adapter<AdapterPrevisao.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var data: TextView = itemView.findViewById(R.id.textDateAdapter)
        var diaSemana: TextView = itemView.findViewById(R.id.textWeekdayAdapter)
        var max: TextView = itemView.findViewById(R.id.textMaxAdapter)
        var min: TextView = itemView.findViewById(R.id.textMinAdapter)
        var descricao: TextView = itemView.findViewById(R.id.textDescricaoAdapter)
        var imagemClima: ImageView = itemView.findViewById(R.id.imageClimaAdapter)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        var view = LayoutInflater.from(parent.context)
            .inflate(R.layout.adapter_previsao_dias, parent, false)

        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        var diaPrevisao = previsoes.get(position)
        holder.data.text = diaPrevisao.date
        holder.descricao.text = diaPrevisao.description
        holder.diaSemana.text = diaPrevisao.weekday
        holder.imagemClima.setImageResource(descricaoCondicao.get(diaPrevisao.description) as Int)
        holder.max.text = "${diaPrevisao.max.toString()} ºC"
        holder.min.text = "${diaPrevisao.min.toString()} ºC"
    }

    override fun getItemCount(): Int {
        return previsoes.size
    }
}