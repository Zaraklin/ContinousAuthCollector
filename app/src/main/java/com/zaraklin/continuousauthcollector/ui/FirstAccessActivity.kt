package com.zaraklin.continuousauthcollector.ui

import android.Manifest
import android.app.AppOpsManager
import android.app.AppOpsManager.MODE_ALLOWED
import android.app.AppOpsManager.OPSTR_GET_USAGE_STATS
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Process.myUid
import android.preference.PreferenceManager.getDefaultSharedPreferences
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import com.google.firebase.auth.FirebaseUser
import com.zaraklin.continuousauthcollector.R
import com.zaraklin.continuousauthcollector.data.RTDb.AnonymousUser
import com.zaraklin.continuousauthcollector.util.Singletons
import kotlinx.android.synthetic.main.activity_first_access.*
import org.joda.time.DateTime


class FirstAccessActivity : AppCompatActivity() {

    val TAG = "FirstAccessActivity"

    val REQ_ACCESS = 99
    val REQ_ACCESS_FINE_LOCATION = 1
    val REQ_READ_CALL_LOG = 2
    val RECEIVE_BOOT_COMPLETED = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_access)

        concludeBtFirstAccess.setOnClickListener{ view ->
            validateEnteredInfo()
        }
    }

    private fun validateEnteredInfo(){
        var user : FirebaseUser? = Singletons.firebaseUtils.performUserLogin(this)

        if (user != null &&
            ageInput.text.toString().isNotEmpty() &&
            ageInput.text.toString().toInt() > 0){

            val anAge : Int = ageInput.text.toString().toInt()
            val start : DateTime = DateTime.now().withMillisOfDay(0)
            val end : DateTime = DateTime.now().plusDays(15).withMillisOfDay(0)

            val sharedPref = getDefaultSharedPreferences(applicationContext) ?: return
            with (sharedPref.edit()) {
                putString(getString(R.string.uid_key), user.uid)
                putLong(getString(R.string.start_date_key), start.millis)
                putLong(getString(R.string.end_data_key), end.millis)
                putInt(getString(R.string.stylometry_text_key), 1)
                putLong(getString(R.string.last_stylometry_collection), 0L)
                commit()
            }

            val anUser : AnonymousUser = AnonymousUser(user.uid, anAge, start, end)
            if (anUser != null){
                Singletons.firebaseUtils.writeAnonymousUser(anUser)
            }

            requestAllPermissions()
        }
        else{
            Snackbar.make(currentFocus, R.string.validateAgeMessage, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun requestAllPermissions(){
        var reqPermissionArray : Array<String> = arrayOf()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            reqPermissionArray = reqPermissionArray.plus(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED){
            reqPermissionArray = reqPermissionArray.plus(Manifest.permission.READ_CALL_LOG)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_BOOT_COMPLETED) != PackageManager.PERMISSION_GRANTED){
            reqPermissionArray = reqPermissionArray.plus(Manifest.permission.RECEIVE_BOOT_COMPLETED)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.PACKAGE_USAGE_STATS) != PackageManager.PERMISSION_GRANTED){
            reqPermissionArray = reqPermissionArray.plus(Manifest.permission.PACKAGE_USAGE_STATS)
        }

        ActivityCompat.requestPermissions(this, reqPermissionArray, REQ_ACCESS)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            REQ_ACCESS -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    checkLastPermission()
                    finish()
                } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1){
                    Snackbar.make(currentFocus, R.string.permissionMessage, Snackbar.LENGTH_SHORT).show()
                    requestAllPermissions()
                } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1){
                    startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                    finish()
                }
                return
            }
        }
    }

    private fun checkLastPermission(){
        if (!checkAppListPermission()){
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
    }

    private fun checkAppListPermission() : Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(OPSTR_GET_USAGE_STATS, myUid(), getPackageName())
        return mode == MODE_ALLOWED
    }
}
