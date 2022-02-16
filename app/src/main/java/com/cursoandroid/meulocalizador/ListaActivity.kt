package com.cursoandroid.meulocalizador

import android.R
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.lista.*
import java.io.*

class ListaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.cursoandroid.meulocalizador.R.layout.lista)

        val locaisLista = getExternalFilesDir(null)!!.list()
        listaItems.adapter = ArrayAdapter(this, R.layout.simple_list_item_1, locaisLista!!.toList())

        listaItems.setOnItemClickListener { parent, view, position, id ->
            val localizacao = "local"


            Toast.makeText(this, localizacao, Toast.LENGTH_SHORT).show()
        }
    }
}
