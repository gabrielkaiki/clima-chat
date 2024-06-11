package com.gabrielkaiki.climaplicativo.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.gabrielkaiki.climaplicativo.R
import com.gabrielkaiki.climaplicativo.model.Mensagem
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.lang.Exception

class AdapterMensagens(var mensagens: MutableList<Mensagem?>, var context: Context) :
    RecyclerView.Adapter<AdapterMensagens.MyViewHolder?>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        var item = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.adapter_mensagem, parent, false)

        return MyViewHolder(item)
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        var mensagem = mensagens.get(position)!!
        holder.mensagem.visibility = View.VISIBLE
        holder.imageMensagem.visibility = View.VISIBLE
        holder.barraProgresso.visibility = View.VISIBLE

        //Hora e data de envio
        holder.dataHora.text = mensagem.horario

        //Carrega a foto de perfil do usuário, caso ela exista.
        var imagemPerfil = mensagem.imagemPerfilUsuario
        if (!imagemPerfil.isNullOrEmpty()) {
            Picasso.get().load(Uri.parse(imagemPerfil)).into(holder.imagemPerfil)
        }

        //Recupera o nome do usuário que enviou a mensagem.
        holder.nome.text = mensagem.nomeRemetente

        var textoMensagem = mensagem.mensagem
        if (!textoMensagem.isNullOrEmpty()) {
            //Carrega texto como mensagem.
            holder.mensagem.text = textoMensagem
            holder.imageMensagem.visibility = View.GONE
            holder.barraProgresso.visibility = View.GONE
        } else {
            //Carrega imagem como mensagem.
            var imagemMensagem = mensagem.urlImagem
            Picasso.get().load(Uri.parse(imagemMensagem))
                .into(holder.imageMensagem, object : com.squareup.picasso.Callback {
                    override fun onSuccess() {
                        holder.barraProgresso.visibility = View.GONE
                    }

                    override fun onError(e: Exception?) {
                        Toast.makeText(context, "Erro: ${e!!.message}", Toast.LENGTH_SHORT).show()
                    }

                })

            //Define a visibilidade do textView da mensagem texto para GONE
            holder.mensagem.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return mensagens.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imagemPerfil: CircleImageView = itemView.findViewById(R.id.imagePerfil)
        var imageMensagem: ImageView = itemView.findViewById(R.id.imageMensagem)
        var nome: TextView = itemView.findViewById(R.id.textNomeUsuario)
        var mensagem: TextView = itemView.findViewById(R.id.textMensagem)
        var dataHora: TextView = itemView.findViewById(R.id.textDataHora)
        var barraProgresso: ProgressBar = itemView.findViewById(R.id.barraProgresso)
        var cardView: CardView = itemView.findViewById(R.id.cardViewAdapter)
    }
}