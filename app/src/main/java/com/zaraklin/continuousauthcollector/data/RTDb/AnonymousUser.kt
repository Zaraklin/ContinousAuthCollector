package com.zaraklin.continuousauthcollector.data.RTDb

import com.google.firebase.database.IgnoreExtraProperties
import org.joda.time.DateTime

@IgnoreExtraProperties
class AnonymousUser() {

    var uid : String = ""
    var age : Int = 0
    var start_date_collect : DateTime? = null
    var end_date_collect : DateTime? = null

    constructor(uid : String, age : Int, start_date_collect : DateTime?, end_date_collect : DateTime?) : this(){
        this.uid = uid
        this.age = age
        this.start_date_collect = start_date_collect
        this.end_date_collect = end_date_collect
    }
}