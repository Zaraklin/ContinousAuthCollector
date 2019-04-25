package com.zaraklin.continuousauthcollector.data.RTDb

import com.google.firebase.database.IgnoreExtraProperties
import org.joda.time.DateTime

@IgnoreExtraProperties
class AppItem {

    var appName : String? = null

    constructor(appName : String) {
        this.appName = appName
    }
}