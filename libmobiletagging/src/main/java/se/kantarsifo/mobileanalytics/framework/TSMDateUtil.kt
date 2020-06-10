package se.kantarsifo.mobileanalytics.framework

import java.util.concurrent.TimeUnit


class TSMDateUtil {
    companion object {
        fun elapsedTimeInDays(start: Long, end: Long): Int  {
            val diff = end - start
            return TimeUnit.MILLISECONDS.toDays(diff).toInt()
        }

        fun elapsedTimeInMinutes(start: Long, end: Long): Int  {
            val diff = end - start
            return TimeUnit.MILLISECONDS.toMinutes(diff).toInt()
        }
    }
}