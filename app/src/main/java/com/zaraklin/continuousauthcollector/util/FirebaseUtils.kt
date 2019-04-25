package com.zaraklin.continuousauthcollector.util

import android.app.Activity
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.zaraklin.continuousauthcollector.data.RTDb.*

class FirebaseUtils {
    val TAG = "FirebaseUtils"

    var user : FirebaseUser? = null
    var mAuth : FirebaseAuth? = null
    var anonymousUAC : DatabaseReference? = null
    var gpsData : DatabaseReference? = null
    var stylometryData : DatabaseReference? = null
    var appListData : DatabaseReference? = null
    var callLogData : DatabaseReference? = null

    constructor(){
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        this.mAuth = FirebaseAuth.getInstance()
        this.anonymousUAC = FirebaseDatabase.getInstance().getReference("anonymousUAC")
        this.gpsData = FirebaseDatabase.getInstance().getReference("gpsData")
        this.stylometryData = FirebaseDatabase.getInstance().getReference("stylometryData")
        this.appListData = FirebaseDatabase.getInstance().getReference("appListData")
        this.callLogData = FirebaseDatabase.getInstance().getReference("callLogData")
    }

    fun performUserLogin(activity : Activity) : FirebaseUser?{
        user = mAuth?.currentUser

        if (user == null){
            mAuth?.signInAnonymously()?.addOnCompleteListener(activity, OnCompleteListener {
                if (it.isSuccessful){
                    Log.i(TAG, "Logged in anonymously")
                    user = mAuth?.currentUser
                }
            })
        }
        loadUserData()

        return this.user
    }

    fun loadUserData() {
        user?.let {
            val uid: String = it.uid
        }
    }

    fun writeAnonymousUser(anUser : AnonymousUser){
        if (anUser != null){
            Log.i(TAG, "Criando usuario RTDb: " + anUser.uid)
            anonymousUAC?.child(anUser.uid)?.setValue(anUser)
        }
    }

    fun addGpsData(gpsItem : GPSLocationItem?){
        if (gpsItem != null){
            Log.i(TAG, "Salvando dados GPS")
            gpsData?.child("data")?.push()?.setValue(gpsItem)
        }
    }

    fun addListAppData(appItemList : AppItemList?){
        if (appItemList != null){
            Log.i(TAG, "Salvando dados de aplicacoes utilizadas")
            appListData?.child("data")?.push()?.setValue(appItemList)
        }
    }

    fun addListCallLog(callLogList : CallLogItemList?){
        if (callLogList != null){
            Log.i(TAG, "Salvando dados de ligacoes")
            callLogData?.child("data")?.push()?.setValue(callLogList)
        }
    }

    fun addListKeyPressed(keyPressedItemList: KeyPressedItemList?){
        if (keyPressedItemList != null){
            Log.i(TAG, "Salvando dados de teclas pressionadas")
            stylometryData?.child("data")?.push()?.setValue(keyPressedItemList)
        }
    }
}