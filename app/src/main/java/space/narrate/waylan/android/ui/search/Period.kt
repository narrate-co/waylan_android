package space.narrate.waylan.android.ui.search

import space.narrate.waylan.android.R

//TODO Move this into data package?
enum class Period(val label: Int, val prefString: String, val viewCountProp: String) {
    MINUTE(R.string.period_label_minute, "min", "minuteViewCount"),
    HOUR(R.string.period_label_hour, "hr", "hourViewCount"),
    DAY(R.string.period_label_day, "dy", "dayViewCount"),
    WEEK(R.string.period_label_week, "wk", "weekViewCount"),
    MONTH(R.string.period_label_month, "mon", "monthViewCount"),
    YEAR(R.string.period_label_year, "yr", "yearViewCount"),
    ALL_TIME(R.string.period_label_all_time, "tot", "totalViewCount");

    companion object {
        fun fromPrefString(prefString: String): Period {
            return when (prefString) {
                "min" -> MINUTE
                "hr" -> HOUR
                "dy" -> DAY
                "wk" -> WEEK
                "mon" -> MONTH
                "yr" -> YEAR
                "tot" -> ALL_TIME
                else -> ALL_TIME
            }
        }
    }

}