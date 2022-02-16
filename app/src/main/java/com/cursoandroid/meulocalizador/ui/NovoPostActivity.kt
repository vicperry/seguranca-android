package com.cursoandroid.meulocalizador.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.drawToBitmap
import com.cursoandroid.meulocalizador.databinding.ActivityNovoPostBinding
import com.cursoandroid.meulocalizador.model.Cifra
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class NovoPostActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNovoPostBinding
    private var imagem_bytes: ByteArray? = null
    private var lat: String = ""
    private var lon: String = ""
    val REQUEST_PERMISSIONS_CODE = 101
    val REQUEST_CAPTURE_IMAGE = 102


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNovoPostBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.imagemInserir.setOnClickListener {
            val pictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            if (pictureIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(pictureIntent, REQUEST_CAPTURE_IMAGE)
            }
        }

        binding.salvarBtn.setOnClickListener {
            salvarPost()
        }

        binding.voltarBtn.setOnClickListener {
            finish()
        }
    }

    override fun onActivityResult(
        requestCode: Int, resultCode: Int,
        data: Intent?
    ) {
        if (requestCode == REQUEST_CAPTURE_IMAGE &&
            resultCode == Activity.RESULT_OK
        ) {
            if (data != null && data.extras != null) {
                val imageBitmap = data.extras!!["data"] as Bitmap?
                binding.imagemInserir.setImageBitmap(imageBitmap)

                val streamOutput = ByteArrayOutputStream()

                imageBitmap?.compress(Bitmap.CompressFormat.PNG, 100, streamOutput)
                val byteArray = streamOutput.toByteArray()
                imagem_bytes = byteArray
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun getCurrentCoordinates() {
        val getGps =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGPSEnabled = getGps.isProviderEnabled(
            LocationManager.GPS_PROVIDER
        )
        val isNetworkEnabled = getGps.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
        if (!isGPSEnabled && !isNetworkEnabled) {
            Log.d("Permissao", "Ative os serviços necessários")
        } else {
            if (isGPSEnabled) {
                try {
                    getGps.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        30000L, 0f, locationListener
                    )
                } catch (ex: SecurityException) {
                    Log.d("Permissao", "Erro de permissão")
                }
            } else if (isNetworkEnabled) {
                try {
                    getGps.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        2000L, 0f, locationListener
                    )
                } catch (ex: SecurityException) {
                    Log.d("Permissao", "Erro de permissão")
                }
            }
        }
    }

    private val locationListener: LocationListener =
        object : LocationListener {
            override fun onLocationChanged(location: Location) {
                lat = "${location.latitude}"
                lon = "${location.longitude}"
                binding.previewLocalTv.text = "$lat $lon"

            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }



    fun salvarPost() {
        val permission_a = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val permission_b = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (permission_a != PackageManager.PERMISSION_GRANTED &&
            permission_b != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                callDialog(
                    "É preciso permitir acesso à localização!",
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
                )
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_PERMISSIONS_CODE
                )
            }
        } else {
            getCurrentCoordinates()
            getCryptoFiles()
        }
    }


  fun getCryptoFiles(){
      if (!binding.tituloEt.text.isNullOrEmpty() && binding.imagemInserir != null && !binding.textoEt.text.isNullOrEmpty()) {

          binding.previewLayout.visibility = View.VISIBLE

          val titulo_post = binding.tituloEt.text.toString()
          val texto_post = binding.textoEt.text.toString()



          binding.previewTituloTv.text = titulo_post
          binding.previewTextoTv.text = texto_post
          binding.previewImagemIv.setImageBitmap(binding.imagemInserir.drawToBitmap())

          val hoje = Calendar.getInstance().time
          val formatarData = SimpleDateFormat("dd.MM.yyyy")
          val data_post = "${formatarData.format(hoje)}"
          binding.previewDataTv.text = data_post


          binding.previewDataTv.setText(data_post)
          val fileName = "${titulo_post.toUpperCase(Locale.ROOT)}*${data_post}*"
          val txtDescription = "$fileName.txt"
          val img_subtitle = "$fileName.fig"
          Cifra().encryptText(
              txtDescription,
              this,
              listOf(lat, lon, texto_post)
          )
          Cifra().encryptImage(
              img_subtitle,
              this,
              imagem_bytes!!
          )
      } else {
          Toast.makeText(
              this,
              "Preencha todos os campos.",
              Toast.LENGTH_SHORT
          ).show()
      }
  }

  private fun callDialog(
      mensage_alerts: String ,
      permissions: Array<String>
  ) {
      var mDialog = AlertDialog.Builder(this)
          .setTitle("Permissão")
          .setMessage(mensage_alerts)
          .setPositiveButton("Ok")
          { dialog, id ->
              ActivityCompat.requestPermissions(
                  this@NovoPostActivity, permissions,
                  REQUEST_PERMISSIONS_CODE
              )
              dialog.dismiss()
          }
          .setNegativeButton("Cancela")
          { dialog, id ->
              dialog.dismiss()
          }
      mDialog.show()
  }
}