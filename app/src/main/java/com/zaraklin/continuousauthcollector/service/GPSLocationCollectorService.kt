package com.zaraklin.continuousauthcollector.service

import android.Manifest
import android.app.job.JobParameters
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.util.Log
import com.zaraklin.continuousauthcollector.R
import com.zaraklin.continuousauthcollector.data.RTDb.GPSLocationItem
import com.zaraklin.continuousauthcollector.util.Singletons

class GPSLocationCollectorService : DataCollector() {

    val TAG = "GPSCollectorService"

    override fun collectData() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(applicationContext) ?: return
        currentUid = sharedPref.getString(getString(R.string.uid_key), "")

        if (currentUid != "") {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                Log.i(TAG, "No permission to get GPS location")
            }
            else{
                Log.i(TAG, "Starting GPS Location Collector")
                startCollectingLocation()
            }
        }
    }

    private fun startCollectingLocation(){
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val lastKnownLocation: Location? = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            collectCurrentLocation(lastKnownLocation)
        }
    }

    private fun collectCurrentLocation(location: Location?){
        Log.i(TAG, "Collecting GPS Location")

        if (location != null){
            var latitude : Double = 0.0
            var longitude : Double = 0.0
            var speed : Float = 0f
            var altitude : Double = 0.0

            latitude = location.latitude
            longitude = location.longitude
            speed = location.speed

            if (location.hasAltitude()) {
                altitude = location.altitude
            }

            val gpsLocationItem : GPSLocationItem =
                GPSLocationItem(currentUid, latitude, longitude, speed, altitude)

            Log.i(TAG, "Sending GPS Location to remote database")
            Singletons.firebaseUtils.addGpsData(gpsLocationItem)
        }
        else{
            Log.i(TAG, "last location not recorded")
        }
    }
}
