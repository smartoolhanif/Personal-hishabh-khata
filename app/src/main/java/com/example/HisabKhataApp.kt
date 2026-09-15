package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class HisabKhataApp : Application() {
    lateinit var container: AppContainer
    
    override fun onCreate() {
        super.onCreate()
        initFirebase()
        container = AppContainer(this)
    }

    private fun initFirebase() {
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val app = FirebaseApp.initializeApp(this)
                if (app == null) {
                    val options = FirebaseOptions.Builder()
                        .setApplicationId("1:181413238517:android:com.aistudio.hanifhisabkhata.kxmpzq")
                        .setApiKey("AIzaSyDummyKeyForInitializationKxmpzq99")
                        .setProjectId("hanif-hisab-khata")
                        .build()
                    FirebaseApp.initializeApp(this, options)
                    Log.d("HisabKhataApp", "FirebaseApp initialized with fallback options")
                }
            }
        } catch (e: Exception) {
            Log.e("HisabKhataApp", "Failed to initialize Firebase with resources, using fallback", e)
            try {
                if (FirebaseApp.getApps(this).isEmpty()) {
                    val options = FirebaseOptions.Builder()
                        .setApplicationId("1:181413238517:android:com.aistudio.hanifhisabkhata.kxmpzq")
                        .setApiKey("AIzaSyDummyKeyForInitializationKxmpzq99")
                        .setProjectId("hanif-hisab-khata")
                        .build()
                    FirebaseApp.initializeApp(this, options)
                }
            } catch (inner: Exception) {
                Log.e("HisabKhataApp", "Fallback Firebase initialization failed", inner)
            }
        }
    }
}

