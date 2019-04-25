package com.zaraklin.continuousauthcollector.data.RTDb

class CallLogItemList : Item {
    var list : List<CallLogItem>? = null

    constructor(uid : String, list : List<CallLogItem>) : super(uid){
        this.list = list
    }
}