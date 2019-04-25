package com.zaraklin.continuousauthcollector.data.RTDb

class AppItemList : Item {

    var list : List<AppItem>? = null

    constructor(uid : String, list : List<AppItem>) : super(uid){
        this.list = list
    }
}