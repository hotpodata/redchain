package com.hotpodata.redchain.data

import android.os.Bundle
import android.text.TextUtils
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.LocalTime
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.util.*

/**
 * Created by jdrotos on 10/5/15.
 */
public class Chain(chainId: String, name: String, chainColor: Int, links: List<LocalDateTime>) {

    object Builder {
        public fun buildFreshChain(chainTitle: String, chainColor: Int): Chain {
            return Chain(UUID.randomUUID().toString(), chainTitle, chainColor, ArrayList<LocalDateTime>())
        }
    }


    var id: String
    var title: String
    var dateTimes: List<LocalDateTime>
    var color: Int
    var longestRun: Int = 0
        get() = if (chainLength > field) {
            field = chainLength
            longestRunLastDate = newestDate
            field
        } else {
            field
        }
    var longestRunLastDate: LocalDateTime? = null
    var notifReminderSettings: NotificationSettings? = null
    var notifBrokenSettings: NotificationSettings? = null

    init {
        id = chainId
        title = name
        dateTimes = sanitizeDateTimes(links)
        color = chainColor
        notifReminderSettings = NotificationSettings(true, true)
        notifBrokenSettings = NotificationSettings(true, true)
    }

    val chainLength: Int
        get() = dateTimes.size

    val oldestDate: LocalDateTime
        get() = if (dateTimes.size > 0) dateTimes.get(dateTimes.size - 1) else LocalDateTime.now()
    val newestDate: LocalDateTime?
        get() = if (dateTimes.size > 0) dateTimes[0] else null

    public fun chainLink(position: Int): LocalDateTime? {
        if (position >= 0 && position < chainLength) {
            return dateTimes.get(position)
        }
        return null
    }

    public fun chainContainsToday(): Boolean {
        return chainDepth(LocalDateTime.now()) >= 0;
    }

    public fun removeTodayFromChain() {
        var todayDt: LocalDateTime? = null
        for (dt in dateTimes) {
            if (dt.toLocalDate().equals(LocalDate.now())) {
                todayDt = dt;
                break;
            }
        }
        if (todayDt != null) {
            var dates = ArrayList<LocalDateTime>(dateTimes)
            dates.remove(todayDt)
            dateTimes = sanitizeDateTimes(dates)
        }
    }

    public fun chainContainsYesterday(): Boolean {
        return chainDepth(LocalDateTime.now().minusDays(1)) >= 0;
    }

    public fun clearDates() {
        dateTimes = ArrayList<LocalDateTime>()
    }

    public fun chainExpired(): Boolean {
        return (chainLength > 1 && !chainContainsYesterday()) || (chainLength == 1 && !chainContainsYesterday() && !chainContainsToday())
    }

    public fun addNowToChain() {
        if (!chainContainsToday()) {
            var now = LocalDateTime.now()
            var dts = ArrayList<LocalDateTime>(dateTimes)
            dts.add(now)
            dateTimes = sanitizeDateTimes(dts)

            if (chainLength > longestRun) {
                longestRun = chainLength
                longestRunLastDate = now
            }
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


    object Serializer {
        val JSON_KEY_CHAINID = "chainid"
        val JSON_KEY_CHAINNAME = "chainname"
        val JSON_KEY_CHAINCOLOR = "chaincolor"
        val JSON_KEY_CHAINDATES = "chaindates"
        val JSON_KEY_LONGESTRUN = "longestrun"
        val JSON_KEY_LONGESTRUNDATE = "longestrundate"
        val JSON_KEY_NOTIF_REMINDER_SETTINGS = "reminderSettings"
        val JSON_KEY_NOTIF_BROKEN_SETTINGS = "brokenSettings"

        fun chainToJson(chain: Chain): JSONObject {
            var chainjson = JSONObject()
            chainjson.putOpt(JSON_KEY_CHAINID, chain.id)
            chainjson.putOpt(JSON_KEY_CHAINNAME, chain.title)
            chainjson.put(JSON_KEY_CHAINCOLOR, chain.color)
            var datesjson = JSONArray()
            for (datetime in chain.dateTimes) {
                datesjson.put(datetime.toString())
            }
            chainjson.putOpt(JSON_KEY_CHAINDATES, datesjson)
            chainjson.putOpt(JSON_KEY_LONGESTRUN, chain.longestRun)
            chainjson.putOpt(JSON_KEY_LONGESTRUNDATE, chain.longestRunLastDate?.toString())
            chainjson.putOpt(JSON_KEY_NOTIF_REMINDER_SETTINGS, chain.notifReminderSettings?.let { NotificationSettings.Serializer.toJson(it) })
            chainjson.putOpt(JSON_KEY_NOTIF_BROKEN_SETTINGS, chain.notifBrokenSettings?.let { NotificationSettings.Serializer.toJson(it) })
            return chainjson

        }

        fun chainFromJson(chainJsonStr: String): Chain? {
            try {
                if (!TextUtils.isEmpty(chainJsonStr)) {
                    var chainjson = JSONObject(chainJsonStr)
                    var chainId: String? = null
                    var chainName: String? = null
                    var chainColor: Int? = null
                    var chainDates: MutableSet<LocalDateTime>? = null
                    if (chainjson.has(JSON_KEY_CHAINID) && chainjson.has(JSON_KEY_CHAINNAME) && chainjson.has(JSON_KEY_CHAINCOLOR)) {
                        chainId = chainjson.getString(JSON_KEY_CHAINID)
                        chainName = chainjson.getString(JSON_KEY_CHAINNAME)
                        chainColor = chainjson.getInt(JSON_KEY_CHAINCOLOR)
                    }
                    if (chainName != null && chainjson.has(JSON_KEY_CHAINDATES)) {
                        chainDates = HashSet<LocalDateTime>()
                        var jsonarrDates = chainjson.getJSONArray(JSON_KEY_CHAINDATES)
                        if (jsonarrDates.length() > 0) {
                            for (i in 0..(jsonarrDates.length() - 1)) {
                                chainDates.add(LocalDateTime.parse(jsonarrDates.get(i).toString()))
                            }
                        }
                    }
                    if (chainId != null && chainName != null && chainColor != null && chainDates != null) {
                        var chain = Chain(chainId, chainName, chainColor, ArrayList<LocalDateTime>(chainDates))
                        chain.longestRun = chainjson.optInt(JSON_KEY_LONGESTRUN, 0)
                        chain.longestRunLastDate = chainjson.optString(JSON_KEY_LONGESTRUNDATE)?.let {
                            LocalDateTime.parse(it)
                        }
                        chainjson.optJSONObject(JSON_KEY_NOTIF_REMINDER_SETTINGS)?.let {
                            chain.notifReminderSettings = NotificationSettings.Serializer.fromJson(it)
                        }
                        chainjson.optJSONObject(JSON_KEY_NOTIF_BROKEN_SETTINGS)?.let {
                            chain.notifBrokenSettings = NotificationSettings.Serializer.fromJson(it)
                        }
                        return chain
                    }
                }
            } catch(ex: Exception) {
                Timber.e(ex, "chainFromJson Fail")
            }
            return null
        }

        val BUNDLE_KEY_CHAIN_JSON = "BUNDLE_KEY_CHAIN_JSON"

        fun chainToBundle(chain: Chain, bundle: Bundle = Bundle()): Bundle {
            bundle.putString(BUNDLE_KEY_CHAIN_JSON, Chain.Serializer.chainToJson(chain).toString())
            return bundle
        }

        fun chainFromBundle(bundle: Bundle?): Chain? {
            return bundle?.getString(BUNDLE_KEY_CHAIN_JSON)?.let {
                Chain.Serializer.chainFromJson(it)
            }
        }
    }

    class NotificationSettings(val enabled: Boolean, val tracksLastActionTime: Boolean, val customTime: LocalTime = LocalTime.MIDNIGHT.plusHours(9)) {
        object Serializer {
            val JSON_ENABLED = "enabled"
            val JSON_TRACKS_LAST = "tracks"
            val JSON_CUSTOM_TIME = "time"

            public fun toJson(settings: NotificationSettings): JSONObject {
                return with(JSONObject()) {
                    put(JSON_ENABLED, settings.enabled)
                    put(JSON_TRACKS_LAST, settings.tracksLastActionTime)
                    putOpt(JSON_CUSTOM_TIME, settings.customTime?.toString())
                }
            }

            public fun fromJson(jsonObject: JSONObject?): NotificationSettings? {
                return jsonObject?.let {
                    var time = it.optString(JSON_CUSTOM_TIME)
                    if (time != null) {
                        NotificationSettings(it.optBoolean(JSON_ENABLED, true), it.optBoolean(JSON_TRACKS_LAST, true), LocalTime.parse(time))
                    } else {
                        NotificationSettings(it.optBoolean(JSON_ENABLED, true), it.optBoolean(JSON_TRACKS_LAST, true))
                    }
                }
            }
        }
    }

}
