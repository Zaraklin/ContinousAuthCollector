package com.zaraklin.continuousauthcollector.data.RTDb

import org.joda.time.DateTime

class GPSLocationItem : Item {

    var latitude : Double = 0.0
    var longitude : Double = 0.0
    var speed : Float = 0f
    var altitude : Double = 0.0

    constructor(uid : String, latitude : Double, longitude : Double, speed : Float, altitude : Double) : super(uid){
        this.latitude = latitude
        this.longitude = longitude
        this.speed = speed
        this.altitude = altitude
    }

}