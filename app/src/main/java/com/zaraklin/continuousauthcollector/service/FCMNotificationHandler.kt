package com.zaraklin.continuousauthcollector.service

import android.content.Intent
import android.preference.PreferenceManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.zaraklin.continuousauthcollector.R
import com.zaraklin.continuousauthcollector.ui.StylometryCollectorActivity
import org.joda.time.LocalDateTime

class FCMNotificationHandler : FirebaseMessagingService() {

    val TAG = "FCMNotificationHandler"

    override fun onMessageReceived(p0: RemoteMessage?) {
        super.onMessageReceived(p0)
        val stylometryCollectorIntent = Intent(this, StylometryCollectorActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val end_collection : LocalDateTime = LocalDateTime(sharedPref.getLong(getString(R.string.end_data_key), 0))
        val now : LocalDateTime = LocalDateTime.now()

        if (now < end_collection){
            startActivity(stylometryCollectorIntent)
        }
    }

    override fun onDeletedMessages() {
        super.onDeletedMessages()
    }
}
