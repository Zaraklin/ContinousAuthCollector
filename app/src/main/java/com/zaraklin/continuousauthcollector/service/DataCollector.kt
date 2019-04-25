package com.zaraklin.continuousauthcollector.service

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobService
import android.preference.PreferenceManager
import com.zaraklin.continuousauthcollector.R
import org.joda.time.LocalDateTime

abstract class DataCollector : JobService(){

    var currentUid : String = ""

    override fun onStopJob(params: JobParameters?): Boolean {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val end_collection : LocalDateTime = LocalDateTime(sharedPref.getLong(getString(R.string.end_data_key), 0))
        val now : LocalDateTime = LocalDateTime.now()

        return (now < end_collection)
    }

    override fun onStartJob(params: JobParameters?): Boolean {

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val end_collection : LocalDateTime = LocalDateTime(sharedPref.getLong(getString(R.string.end_data_key), 0))
        val now : LocalDateTime = LocalDateTime.now()

        if (now < end_collection) {
            doBackgroundWork(params)
        }

        return true
    }

    fun doBackgroundWork(params: JobParameters?){
        Thread(Runnable {
            collectData()
            jobFinished(params, true)
        }).start()
    }

    abstract public fun collectData()
}