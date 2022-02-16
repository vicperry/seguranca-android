package com.cursoandroid.meulocalizador.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cursoandroid.meulocalizador.R
import com.cursoandroid.meulocalizador.model.Post
import kotlinx.android.synthetic.main.post_item.view.*

class PostAdapter (val files: List<Post>) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(view: View) :
        RecyclerView.ViewHolder(view){

        val titulo = view.titulo_tv
        val texto = view.texto_tv
        val data = view.data_tv
        val imagem = view.imagem_iv

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(
                R.layout.post_item ,
                parent,
                false)

        return PostViewHolder(view)
    }

    override fun getItemCount(): Int = files.size

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val infoArq = files[position]
        holder.titulo.text = infoArq.titulo
        holder.texto.text = infoArq.texto
        holder.data.text = infoArq.data
        holder.imagem.setImageBitmap(infoArq.imagem)
    }

}
