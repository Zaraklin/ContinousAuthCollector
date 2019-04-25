package com.zaraklin.continuousauthcollector.service

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.preference.PreferenceManager
import android.provider.CallLog
import android.support.v4.content.ContextCompat
import android.util.Log
import com.zaraklin.continuousauthcollector.R
import com.zaraklin.continuousauthcollector.data.RTDb.CallLogItem
import com.zaraklin.continuousauthcollector.data.RTDb.CallLogItemList
import com.zaraklin.continuousauthcollector.util.Singletons
import java.util.*

class CallLogCollectorService : DataCollector() {

    val TAG = "CallLogCollectorService"

    override fun collectData(){
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(applicationContext) ?: return
        currentUid = sharedPref.getString(getString(R.string.uid_key), "")

        if (currentUid != "") {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED){
                Log.i(TAG, "No permission to read Call Logs")
            }
            else{
                Log.i(TAG, "Collecting Call Logs")
                val callLogItemList: List<CallLogItem> = getCallLogs()

                Log.i(TAG, "Sending Call Logs to remote database")
                Singletons.firebaseUtils.addListCallLog(CallLogItemList(currentUid, callLogItemList))
            }
        }
    }

    public fun getCallLogs() : ArrayList<CallLogItem>{
        var callLogList : ArrayList<CallLogItem> = arrayListOf()

        var allCalls : Uri = Uri.parse("content://call_log/calls")
        var c : Cursor = contentResolver.query(allCalls, null, null, null, null, null)

        val numberIndex = c.getColumnIndex(CallLog.Calls.NUMBER)
        val typeIndex = c.getColumnIndex(CallLog.Calls.TYPE)
        val dateIndex = c.getColumnIndex(CallLog.Calls.DATE)
        val durationIndex = c.getColumnIndex(CallLog.Calls.DURATION)

        while (c.moveToNext()){
            var callType = Integer.parseInt(c.getString(typeIndex))
            var callDirection : String = ""

            if (callType == CallLog.Calls.OUTGOING_TYPE){
                callDirection = "Outgoing"
            }
            else if (callType == CallLog.Calls.INCOMING_TYPE){
                callDirection = "Incoming"
            }
            else if (callType == CallLog.Calls.MISSED_TYPE){
                callDirection = "Missed"
            }

            val call = CallLogItem(
                c.getString(numberIndex),
                callDirection,
                c.getString(dateIndex),
                c.getString(durationIndex)
            )
            callLogList.add(call)
        }

        return callLogList
    }
}