package com.hotpodata.redchain.data

import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import java.util.*

/**
 * Created by jdrotos on 10/5/15.
 */
public class Chain(chainId: String, name: String, chainColor: Int, links: List<LocalDateTime>) {

    object Builder{
        public fun buildFreshChain(chainTitle: String, chainColor: Int): Chain{
            return Chain(UUID.randomUUID().toString(), chainTitle,chainColor, ArrayList<LocalDateTime>())
        }
    }

    var id: String
        get
    var title: String
        get
    var dateTimes: List<LocalDateTime>
    var color: Int
        get

    init {
        id = chainId
        title = name
        dateTimes = sanitizeDateTimes(links)
        color = chainColor
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

    public fun removeTodayFromChain(){
        var todayDt: LocalDateTime? = null
        for(dt in dateTimes){
            if(dt.toLocalDate().equals(LocalDate.now())){
                todayDt = dt;
                break;
            }
        }
        if(todayDt != null){
            var dates = ArrayList<LocalDateTime>(dateTimes)
            dates.remove(todayDt)
            dateTimes = sanitizeDateTimes(dates)
        }
    }

    public fun chainContainsYesterday(): Boolean {
        return chainDepth(LocalDateTime.now().minusDays(1)) >= 0;
    }

    public fun clearDates(){
        dateTimes = ArrayList<LocalDateTime>()
    }

    public fun chainExpired(): Boolean{
        return (chainLength > 1 && !chainContainsYesterday()) || (chainLength == 1 && !chainContainsYesterday() && !chainContainsToday())
    }

    public fun addNowToChain() {
        if (!chainContainsToday()) {
            var dts = ArrayList<LocalDateTime>(dateTimes)
            dts.add(LocalDateTime.now())
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

    private fun sanitizeDateTimes(dateTimeList: List<LocalDateTime>): List<LocalDateTime> {
        var dtSet = HashSet<LocalDateTime>()
        for (dt in dateTimeList) {
            if (dt.toLocalDate().isBefore(LocalDate.now().plusDays(1))) {
                dtSet.add(dt)
            }
        }
        var dtList = ArrayList<LocalDateTime>(dtSet)
        Collections.sort(dtList)
        Collections.reverse(dtList)
        return dtList
    }


}
