package com.zaraklin.continuousauthcollector.util

import org.joda.time.LocalDateTime
import org.joda.time.Period
import org.joda.time.Seconds

class CollectorCountdownTime {

    companion object {
        public fun daysRemaing() : Long {
            val period = periodRemaining()

            return period.toStandardDays().days.toLong()
        }

        public fun secondsRemaining() : Long {
            val now : LocalDateTime = LocalDateTime.now()
            val collectDataEnd =
                collectDataEnd()

            return Seconds.secondsBetween(now, collectDataEnd).seconds.toLong()
        }

        public fun secondsRemaining(collectDataEnd : LocalDateTime) : Long {
            val now : LocalDateTime = LocalDateTime.now()

            return Seconds.secondsBetween(now, collectDataEnd).seconds.toLong()
        }

        private fun periodRemaining() : Period {
            val now : LocalDateTime = LocalDateTime.now()
            val collectDataEnd =
                collectDataEnd()

            return Period(now, collectDataEnd)
        }

        private fun collectDataEnd() : LocalDateTime {
            val collectDataEnd : LocalDateTime = LocalDateTime("2019-03-29T00:00:00.000")

            return collectDataEnd
        }
    }
}