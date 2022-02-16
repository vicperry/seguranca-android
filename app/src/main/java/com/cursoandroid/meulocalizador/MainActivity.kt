package com.cursoandroid.meulocalizador

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listar_btn.setOnClickListener {
            acessarLista()
        }

        acessar_btn.setOnClickListener {
            acessarLocal()
        }
    }

    val REQUEST_PERMISSIONS_CODE = 101

    private fun acessarLista() {
        val intent = Intent (this, ListaActivity::class.java)
        startActivity(intent)    }

    private fun getCurrentCoordinates() {
        val locationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGPSEnabled = locationManager.isProviderEnabled(
            LocationManager.GPS_PROVIDER
        )
        val isNetworkEnabled = locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
        if (!isGPSEnabled && !isNetworkEnabled) {
            Toast.makeText(this, "Ative o GPS e tente novamente.", Toast.LENGTH_LONG).show()
            Log.d("Permissão", "Ative os serviços necessários. ")
        } else {
            if (isGPSEnabled) {
                Toast.makeText(this, "Localizando...", Toast.LENGTH_LONG).show()
                try {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        30000L, 0f, locationListener
                    )
                } catch (ex: SecurityException) {
                    Toast.makeText(this, "Erro de permissão.", Toast.LENGTH_LONG).show()
                    Log.d("Permissão", "Erro de permissão. ")
                }
            } else if (isNetworkEnabled) {
                try {
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        2000L, 0f, locationListener
                    )
                } catch (ex: SecurityException) {
                    Toast.makeText(this, "Erro de permissão.", Toast.LENGTH_LONG).show()
                    Log.d("Permissão", "Erro de permissão. ")
                }
            }
        }
    }

    private val locationListener =
        object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val hora = Calendar.getInstance().time
                val formatarData = SimpleDateFormat("HH_mm_ss-dd_MM_yyyy")
                val fileName = formatarData.format(hora) + ".crd"
                val local = "${location.latitude} ${location.longitude}"

                try {
                    if (
                        ContextCompat.checkSelfPermission(
                            applicationContext,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(
                            applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE
                        ) != PackageManager.PERMISSION_GRANTED){
                        if (ActivityCompat.shouldShowRequestPermissionRationale(
                                this@MainActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                            (ActivityCompat.shouldShowRequestPermissionRationale(
                                this@MainActivity, Manifest.permission.READ_EXTERNAL_STORAGE))
                        ){
                            callDialog(
                                "Permita WRITE_EXTERNAL_STORAGE e tente novamente.",
                                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                            callDialog(
                                "Permita READ_EXTERNAL_STORAGE e tente novamente. ",
                                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
                        } else {
                            ActivityCompat.requestPermissions(this@MainActivity,
                                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                REQUEST_PERMISSIONS_CODE)
                            ActivityCompat.requestPermissions(this@MainActivity,
                                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                                REQUEST_PERMISSIONS_CODE)
                        }
                    } else {
                        if (isExternalStorageWritable() and isExternalStorageReadable()) {
                            val file = File(getExternalFilesDir(null), fileName)
                            BufferedWriter(FileWriter(file)).use {
                                it.write(local)
                            }
                        }
                    }
                }catch (e: Exception){
                    Log.e("File Error", "Não foi possível ler ou escrever o arquivo.")
                }
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

    fun acessarLocal() {
        val permissionAFL = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val permissionACL = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (permissionAFL != PackageManager.PERMISSION_GRANTED &&
            permissionACL != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                callDialog(
                    "Permita acesso à localização e tente novamente!", arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSIONS_CODE)
            }
        } else {
            getCurrentCoordinates()
        }
    }

    private fun callDialog(
        mensagem: String,
        permissions: Array<String>
    ) {
        var mDialog = AlertDialog.Builder(this)
            .setTitle("Permissão")
            .setMessage(mensagem)
            .setPositiveButton("Permitir")
            { dialog, id ->
                ActivityCompat.requestPermissions(
                    this@MainActivity, permissions,
                    REQUEST_PERMISSIONS_CODE
                )
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar")
            { dialog, id ->
                dialog.dismiss()
            }
        mDialog.show()
    }

    fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    fun isExternalStorageReadable(): Boolean {
        return Environment.getExternalStorageState() in
                setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)
    }
}