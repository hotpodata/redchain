package com.hotpodata.redchain.data

import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import java.util.*

/**
 * Created by jdrotos on 10/5/15.
 */
public class Chain(name: String, links: List<LocalDateTime>) {
    var dateTimes: List<LocalDateTime>
    var title: String
        get

    init {
        title = name
        dateTimes = sanitizeDateTimes(links)
    }

    val chainLength: Int
        get() = dateTimes.size

    val oldestDate: LocalDateTime
        get() = if (dateTimes.size > 0) dateTimes.get(dateTimes.size - 1) else LocalDateTime.now()

    public fun chainLink(position: Int): LocalDateTime? {
        if (position >= 0 && position < chainLength) {
            return dateTimes.get(position)
        }
        return null
    }

    public fun chainContainsToday(): Boolean {
        return chainDepth(LocalDateTime.now()) >= 0;
    }

    public fun chainContainsYesterday(): Boolean {
        return chainDepth(LocalDateTime.now().minusDays(1)) >= 0;
    }

    public fun addNowToChain() {
        if (!chainContainsToday()) {
            var dts = ArrayList<LocalDateTime>(dateTimes)
            dts.add( LocalDateTime.now())
            dateTimes = sanitizeDateTimes(dts)

        }
    }

    public fun chainDepth(localDateTime: LocalDateTime): Int {
        for (i in dateTimes.indices) {
            if (localDateTime.toLocalDate().isEqual(dateTimes.get(i).toLocalDate())) {
                return i;
            } else if (dateTimes.get(i).isBefore(localDateTime)) {
                break;
            }
        }
        return -1;
    }

    private fun sanitizeDateTimes(dateTimeList: List<LocalDateTime>) : List<LocalDateTime>{
        var dtSet = HashSet<LocalDateTime>()
        for(dt in dateTimeList){
            if(dt.toLocalDate().isBefore(LocalDate.now().plusDays(1))){
                dtSet.add(dt)
            }
        }
        var dtList = ArrayList<LocalDateTime>(dtSet)
        Collections.sort(dtList)
        Collections.reverse(dtList)
        return dtList
    }


}
