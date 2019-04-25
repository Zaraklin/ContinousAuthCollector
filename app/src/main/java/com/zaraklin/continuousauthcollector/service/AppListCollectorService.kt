package com.zaraklin.continuousauthcollector.service

import android.app.ActivityManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.preference.PreferenceManager
import android.util.Log
import com.zaraklin.continuousauthcollector.R
import com.zaraklin.continuousauthcollector.data.RTDb.AppItem
import com.zaraklin.continuousauthcollector.data.RTDb.AppItemList
import com.zaraklin.continuousauthcollector.util.Singletons

class AppListCollectorService : DataCollector() {

    val TAG = "AppListCollectorService"
    val USAGE_STATS_IN_API_21 : String = "usagestats"

    override fun collectData() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(applicationContext) ?: return
        currentUid = sharedPref.getString(getString(R.string.uid_key), "")

        if (currentUid != "") {
            Log.i(TAG, "Collecting App List")
            val menuAppList = getMenuAppList(applicationContext)
            val runningAppList = getRunninAppList(applicationContext, menuAppList)

            Log.i(TAG, "Sending App List to remote database")
            Singletons.firebaseUtils.addListAppData(AppItemList(currentUid, runningAppList))
        }
    }

    public fun getRunninAppList(context: Context, validApps : ArrayList<AppItem>) : ArrayList<AppItem>{
        var appItemList : ArrayList<AppItem> = arrayListOf()
        var appItemAux : AppItem

        val runningAppProcessInfo = getRunningApp()

        for (i in runningAppProcessInfo.indices) {
            appItemAux = runningAppProcessInfo[i]

            for (j in validApps.indices){
                if (validApps[j].appName == appItemAux.appName){
                    appItemList.add(appItemAux)
                }
            }
        }
        return appItemList
    }

    public fun getRunningApp() : List<AppItem> {
        var appItemList : ArrayList<AppItem> = arrayListOf()
        val usageStatsManager : UsageStatsManager = getSystemService(USAGE_STATS_IN_API_21) as UsageStatsManager
        var stats : Map<String, UsageStats> = usageStatsManager.queryAndAggregateUsageStats((System.currentTimeMillis() - (5 * 60 * 1000)), System.currentTimeMillis())

        for ((key, value) in stats){
            appItemList.add(AppItem(key))
        }

        return appItemList
    }

    public fun getMenuAppList(context: Context) : ArrayList<AppItem>{
        var appMenuList : ArrayList<AppItem> = arrayListOf()

        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = packageManager.queryIntentActivities(intent, PackageManager.GET_META_DATA)

        for (i in apps.indices){
            appMenuList.add(AppItem(apps[i].activityInfo.processName))
        }

        return appMenuList
    }
}
