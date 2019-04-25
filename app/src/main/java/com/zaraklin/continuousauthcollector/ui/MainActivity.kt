package com.zaraklin.continuousauthcollector.ui

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.CountDownTimer
import android.preference.PreferenceManager.getDefaultSharedPreferences
import android.support.v7.app.AppCompatActivity
import android.text.format.DateUtils
import android.util.Log
import android.view.View
import android.view.WindowManager
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.iid.FirebaseInstanceId
import com.zaraklin.continuousauthcollector.R
import com.zaraklin.continuousauthcollector.service.AppListCollectorService
import com.zaraklin.continuousauthcollector.service.CallLogCollectorService
import com.zaraklin.continuousauthcollector.service.GPSLocationCollectorService
import com.zaraklin.continuousauthcollector.util.Collector
import com.zaraklin.continuousauthcollector.util.CollectorCountdownTime
import com.zaraklin.continuousauthcollector.util.Singletons
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_activity_main.*
import net.danlew.android.joda.JodaTimeAndroid
import org.joda.time.LocalDateTime
import org.joda.time.Period
import org.joda.time.Seconds


class MainActivity : AppCompatActivity() {

    private lateinit var timer : CountDownTimer
    val TAG = "ContAuth_MainActitity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        supportActionBar?.setIcon(R.drawable.ic_school_24dp)
        supportActionBar?.title = "    Cont. Auth Collector"

        setProgressBar(false)
        JodaTimeAndroid.init(this)

        val bundle : Bundle? = intent.extras
        setNotificationRoute(bundle)

        if (isTimeToCollectStylometry()){
            startStylometryCollector()
        }
        else{
            bt_stylometry_start.setVisibility(View.GONE)
        }
    }

    override fun onPause() {
        super.onPause()
        this.timer.cancel()
    }

    override fun onResume() {
        super.onResume()
        loadScreenDependencies()
        Singletons.firebaseUtils.performUserLogin(this)

        if (Singletons.firebaseUtils.user == null){
            setProgressBar(true)
        }
        var load = LoadUser()
        load.execute()

        val token = FirebaseInstanceId.getInstance().getToken()
        Log.i(TAG, "Actual Firebase Token: " + token)
    }

    private fun setNotificationRoute(extras : Bundle?){
        Log.i(TAG, "Processing notification...")

        if (extras != null){
            Log.i(TAG, extras?.getString("collector", "not found"))

            val action = extras?.getString("collector")
            if (action == "stylometry"){
                startStylometryCollector()
            }
        }
    }

    private fun startStylometryCollector(){
        Log.i(TAG, "Starting Stylometry Collection")
        val stylometryCollectorIntent = Intent(this, StylometryCollectorActivity::class.java)
        startActivity(stylometryCollectorIntent)
    }

    private fun setProgressBar(show: Boolean){
        if (show){
            loadingLinearLayout.setVisibility(View.VISIBLE)
            progressBar.setVisibility(View.VISIBLE)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
        else if (!show){
            loadingLinearLayout.setVisibility(View.GONE)
            progressBar.setVisibility(View.GONE)
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }

    fun loadScreenDependencies(){
        this.timer = object : CountDownTimer(DateUtils.SECOND_IN_MILLIS, DateUtils.SECOND_IN_MILLIS){
            override fun onFinish() = onTimerFinished()
            override fun onTick(millisUntilFinished : Long){
            }
        }
    }

    fun isFirstAccess(uid : String){
        val sharedPref = getDefaultSharedPreferences(applicationContext) ?: return
        val record = sharedPref.getString(getString(R.string.uid_key), "")

        if (record != ""){
            Log.i(TAG, "AnonymousUser data loaded.")
            screenTimer()
            scheduleCollectors()
        }
        else{
            Log.i(TAG, "Starting FirstAcces.")
            startFirstAcces()
        }
    }

    private fun startFirstAcces(){
        val firstAccessIntent = Intent(this, FirstAccessActivity::class.java)
        startActivity(firstAccessIntent)
    }

    private fun isTimeToCollectStylometry() : Boolean{
        val sharedPref = getDefaultSharedPreferences(applicationContext)
        val last_collection : LocalDateTime = LocalDateTime(sharedPref.getLong(getString(R.string.last_stylometry_collection), 0))
        val end_collection : LocalDateTime = LocalDateTime(sharedPref.getLong(getString(R.string.end_data_key), 0))
        val now : LocalDateTime = LocalDateTime.now()
        val period : Period = Period(Seconds.secondsBetween(last_collection, now).seconds.toLong())

        if (now > end_collection){
            return false
        }
        else {
            return (period.toStandardDays().days > 0)
        }
    }

    private fun screenTimer(){
        val sharedPref = getDefaultSharedPreferences(applicationContext) ?: return
        val end_collect : LocalDateTime = LocalDateTime(sharedPref.getLong(getString(R.string.end_data_key), 0))
        val secondsRemaining = CollectorCountdownTime.secondsRemaining(end_collect)

        if (end_collect > LocalDateTime.now()){
            this.timer = object : CountDownTimer(secondsRemaining * DateUtils.SECOND_IN_MILLIS, DateUtils.SECOND_IN_MILLIS){
                override fun onFinish() = onTimerFinished()
                override fun onTick(millisUntilFinished : Long){
                    updateCountdownUI(millisUntilFinished)
                }
            }.start()
        }
        else{
            txtAcknowledgements.setText(R.string.end_collect)
            txtDaysLeft.setText("")
            txtDaysLeftDown.setText("")
        }
    }

    private fun onTimerFinished(){

    }

    private fun updateCountdownUI(millisRemaining : Long = 0){
        val period : Period = Period(millisRemaining)

        if (period.toStandardDays().days > 0){
            val daysRmg = period.toStandardDays().days
            txtDaysLeft.text = String.format(getString(R.string.days_left), daysRmg)
            txtDaysLeftDown.setText(R.string.days_remaining)
        }
        else{
            val hoursRmg = period.hours
            val minutesRmg = period.minutes
            val secondsRmg = period.seconds
            txtDaysLeft.text = String.format(getString(R.string.hours_left), hoursRmg, minutesRmg, secondsRmg)

            txtDaysLeftDown.setText(R.string.hours_remaining)
        }
    }

    private fun scheduleCollectors(){

        var appListCollectorScheduled : Boolean = false
        var appListCollectorComponentName : ComponentName = ComponentName(this, AppListCollectorService::class.java)
        var appListCollectorJobInfo : JobInfo = JobInfo.Builder(Collector.APP_LIST_COLLECTOR_SERVICE.id, appListCollectorComponentName)
            .setPersisted(true)
            .setPeriodic(1 * 60 * 1000)
            .build()

        var callLogCollectorScheduled : Boolean = false
        var callLogCollectorComponentName : ComponentName = ComponentName(this, CallLogCollectorService::class.java)
        var callLogCollectorJobInfo : JobInfo = JobInfo.Builder(Collector.CALL_LOG_COLLECTOR_SERVICE.id, callLogCollectorComponentName)
            .setPersisted(true)
            .setPeriodic(24 * 60 * 60 * 1000)
            .build()

        var gpsLocationCollectorScheduled : Boolean = false
        var gpsLocationCollectorComponentName : ComponentName = ComponentName(this, GPSLocationCollectorService::class.java)
        var gpsLocationCollectorJobInfo : JobInfo = JobInfo.Builder(Collector.GPS_LOCATION_COLLECTOR_SERVICE.id, gpsLocationCollectorComponentName)
            .setPersisted(true)
            .setPeriodic(1 * 60 * 1000)
            .build()

        var scheduler : JobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

        for (jobInfo in scheduler.allPendingJobs){
            if (jobInfo.id == Collector.APP_LIST_COLLECTOR_SERVICE.id){
                appListCollectorScheduled = true
            }
            else if (jobInfo.id == Collector.CALL_LOG_COLLECTOR_SERVICE.id){
                callLogCollectorScheduled = true
            }
            else if (jobInfo.id == Collector.GPS_LOCATION_COLLECTOR_SERVICE.id){
                gpsLocationCollectorScheduled = true
            }
        }

        if (appListCollectorScheduled == false){
            if (JobScheduler.RESULT_SUCCESS == scheduler.schedule(appListCollectorJobInfo)){
                Log.i(TAG, "AppListCollectorService: Scheduled")
            }
            else{
                Log.i(TAG, "AppListCollectorService: Failed do Schedule")
            }
        }

        if (callLogCollectorScheduled == false){
            if (JobScheduler.RESULT_SUCCESS == scheduler.schedule(callLogCollectorJobInfo)){
                Log.i(TAG, "CallLogCollectorService: Scheduled")
            }
            else{
                Log.i(TAG, "CallLogCollectorService: Failed do Schedule")
            }
        }

        if (gpsLocationCollectorScheduled == false){
            if (JobScheduler.RESULT_SUCCESS == scheduler.schedule(gpsLocationCollectorJobInfo)){
                Log.i(TAG, "GPSLocationCollectorService: Scheduled")
            }
            else{
                Log.i(TAG, "GPSLocationCollectorService: Failed do Schedule")
            }
        }
    }

    inner class LoadUser : AsyncTask<Void, Void, String>(){
        override fun doInBackground(vararg params: Void?): String {
            var user : FirebaseUser? = null
            do {
                Log.i(TAG, "Waiting for user")
                user = Singletons.firebaseUtils.user
                Thread.sleep(500)
            } while (user == null)

            return user.uid
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            setProgressBar(false)
            isFirstAccess(result)
        }
    }
}
