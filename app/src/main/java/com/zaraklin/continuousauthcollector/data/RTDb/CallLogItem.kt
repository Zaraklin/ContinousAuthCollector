package com.zaraklin.continuousauthcollector.data.RTDb

import com.google.firebase.database.IgnoreExtraProperties
import org.joda.time.DateTime

@IgnoreExtraProperties
class CallLogItem{

    var number : String = ""
    var direction : String = ""
    var datetime : String = ""
    var duration : String = ""

    constructor(number : String, direction : String, dateTime: String, duration: String){
        this.number = number
        this.direction = direction
        this.datetime = dateTime
        this.duration = duration
    }
}