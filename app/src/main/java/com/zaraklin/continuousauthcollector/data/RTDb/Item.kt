package com.zaraklin.continuousauthcollector.data.RTDb

import com.google.firebase.database.IgnoreExtraProperties
import org.joda.time.DateTime

@IgnoreExtraProperties
abstract class Item {

    var uid : String = ""
    var dateTimeCollected : DateTime? = null
    var weekDay : Int? = null

    constructor(uid : String){
        this.uid = uid
        this.dateTimeCollected = DateTime.now()
        this.weekDay = this.dateTimeCollected?.dayOfWeek
    }
}