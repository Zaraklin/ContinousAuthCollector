package com.zaraklin.continuousauthcollector.data.RTDb

import com.google.firebase.database.IgnoreExtraProperties
import org.joda.time.DateTime
import org.joda.time.LocalDateTime

@IgnoreExtraProperties
class KeyPressedItem{

    var keyPressed : String? = null
    var timeKeyPressed : DateTime? = null
    var intervalBetweenKeyPress : Long? = null

    constructor(keyPressed : String, timeKeyPressed : DateTime, intervalBetweenKeyPress : Long){
        this.keyPressed = keyPressed
        this.timeKeyPressed = timeKeyPressed
        this.intervalBetweenKeyPress = intervalBetweenKeyPress
    }
}