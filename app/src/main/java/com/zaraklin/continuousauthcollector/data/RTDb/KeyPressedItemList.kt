package com.zaraklin.continuousauthcollector.data.RTDb

class KeyPressedItemList : Item {
    var list : List<KeyPressedItem>? = null

    constructor(uid : String, list : List<KeyPressedItem>) : super(uid){
        this.list = list
    }
}