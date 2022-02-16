package com.cursoandroid.meulocalizador.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.billingclient.api.*
import com.cursoandroid.meulocalizador.adapter.PostAdapter
import com.cursoandroid.meulocalizador.databinding.ActivityMainBinding
import com.cursoandroid.meulocalizador.model.Cifra
import com.cursoandroid.meulocalizador.model.Post
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity(),
    BillingClientStateListener,
    SkuDetailsResponseListener,
    PurchasesUpdatedListener,
    ConsumeResponseListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var userApp: BillingClient
    lateinit var auth: FirebaseAuth
    private var mUser: FirebaseUser? = null
    private var currentSku = "android.test.purchased"
    private val PREF_FILE = "PREF_FILE"
    private lateinit var sharedPref: SharedPreferences
    private var mapSku = HashMap<String, SkuDetails>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = FirebaseAuth.getInstance()

        MobileAds.initialize(this)
        val getMonetization = AdRequest.Builder().build()
        binding.adView.loadAd(getMonetization)

        binding.premiumBtn.setOnClickListener {
            val skuDetails = mapSku[currentSku]
            val purchaseParams = BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetails).build()
            userApp.launchBillingFlow(this, purchaseParams)
        }

        binding.addButton.setOnClickListener {
            startActivity(Intent(this, NovoPostActivity::class.java))
        }

        binding.logoutButton.setOnClickListener {
            auth.signOut()

            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        userApp = BillingClient.newBuilder(this)
            .enablePendingPurchases()
            .setListener(this)
            .build()
        userApp.startConnection(this)

        checkBuy()
    }

    override fun onDestroy() {
        userApp.endConnection()
        super.onDestroy()
    }


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onStart() {
        super.onStart()
        mUser = auth.currentUser
        recyclerview()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun recyclerview(){
        binding.recyclerView.adapter = PostAdapter(updateRecycler())
        binding.recyclerView.layoutManager =
            LinearLayoutManager(this)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun updateRecycler(): List<Post> {
        val pathArq = File(this.filesDir.toURI())
        var prefix = ""
        val data = mutableListOf<Post>()
        val files = pathArq.listFiles()

        files?.forEach {
            if ("$prefix.txt" != it.name && "$prefix.fig" != it.name) {
                prefix = it.name.removeSuffix(".txt")
                prefix = prefix.removeSuffix(".fig")

                data.add(getData(prefix))
            }
        }
        return data
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getData(prefix: String): Post {
        var delSufix = prefix.removeSuffix(".fig")
        delSufix = delSufix.removeSuffix(".txt")
        val imagemData: ByteArray = Cifra().encryptReadImage("$delSufix.fig", this)
        val textoData: String = Cifra().encryptReadText("$delSufix.txt", this)[2]
        val tituloData = prefix.split("*")[0]
        val dataData = prefix.split("*")[1].removeSuffix("*")
        val bitmapData = BitmapFactory.decodeByteArray(imagemData, 0, imagemData.size)

        return Post(tituloData, textoData, dataData, bitmapData)
    }

    override fun onBillingServiceDisconnected() {
        Log.d("COMPRA>>","Serviço InApp desconectado")

    }

    override fun onBillingSetupFinished(billingResult: BillingResult?) {
        if(billingResult?.responseCode ==
            BillingClient.BillingResponseCode.OK){
            Log.d("COMPRA>>","Serviço InApp conectado")
            val skuList = arrayListOf(currentSku)
            val params = SkuDetailsParams.newBuilder()
            params.setSkusList(skuList).setType(
                BillingClient.SkuType.INAPP)
            userApp.querySkuDetailsAsync(params.build(), this)
        }
    }

    override fun onSkuDetailsResponse(billingResult: BillingResult?,
                                      skuDetailsList: MutableList<SkuDetails>?) {
        if(billingResult?.responseCode ==
            BillingClient.BillingResponseCode.OK){
            mapSku.clear()
            skuDetailsList?.forEach{
                    t ->
                mapSku[t.sku] = t
                val preco = t.price
                val descricao = t.description
                Log.d("COMPRA>>",
                    "Produto Disponivel ($preco): $descricao")
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode ==
            BillingClient.BillingResponseCode.OK &&
            purchases != null){

            for (purchase in purchases) {
                GlobalScope.launch (Dispatchers.IO){
                    handlePurchase(purchase)
                }
            }
        }
        else if (billingResult.responseCode ==
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED){
            Log.d("COMPRA>>","Produto já foi comprado")

            val userId = auth.currentUser?.uid
            val editor =
                getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE).edit()
            editor.putBoolean(userId, true)
            editor.commit()

        }
        else if (billingResult.responseCode ==
            BillingClient.BillingResponseCode.USER_CANCELED){
            Log.d("COMPRA>>","Usuário cancelou a compra")

        }
        else{
            Log.d("COMPRA>>",
                "Código de erro desconhecido: ${billingResult.responseCode}")
        }

    }

    suspend fun handlePurchase (purchase: Purchase) {
        if (purchase.purchaseState === Purchase.PurchaseState.PURCHASED){
            Log.d("COMPRA>>","Produto obtido com sucesso!")
            val userId = auth.currentUser?.uid
            val editor =
                getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE).edit()
            editor.putBoolean(userId, true)
            editor.apply()

            if (!purchase.isAcknowledged){
                val acknowledgePurchaseParams = AcknowledgePurchaseParams
                    .newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)

                val ackPurchaseResult = withContext(Dispatchers.IO){
                    userApp.acknowledgePurchase(
                        acknowledgePurchaseParams.build())
                }
            }
        }
    }


    private fun checkBuy() {
        val preferences =
            getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        val userId = auth.currentUser?.uid
        val isPurchase = preferences.getBoolean(userId, false)
        if (isPurchase) {
            binding.adView.setVisibility(View.GONE)
            binding.premiumBtn.setVisibility(View.GONE)
        }
    }

    override fun onConsumeResponse(billingResult: BillingResult?, string: String?) {
        if (billingResult?.responseCode ==
            BillingClient.BillingResponseCode.OK){
            Log.d( "COMPRA>>" , "Produto Consumido" )
        }
    }
}