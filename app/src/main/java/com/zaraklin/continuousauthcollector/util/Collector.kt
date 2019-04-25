package com.zaraklin.continuousauthcollector.util

enum class Collector(val id : Int) {
    APP_LIST_COLLECTOR_SERVICE(1),
    CALL_LOG_COLLECTOR_SERVICE(2),
    GPS_LOCATION_COLLECTOR_SERVICE(3),
    NETWORK_USAGE_COLLECTOR_SERVICE(4)
}