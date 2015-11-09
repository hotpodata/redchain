package com.hotpodata.redchain

import android.content.Context
import android.text.TextUtils
import com.hotpodata.redchain.data.Chain
import org.joda.time.LocalDateTime
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.util.*

/**
 * Created by jdrotos on 11/7/15.
 */
object ChainMaster {
    val PREFS_CORE = "core"
    val PREF_KEY_CHAIN = "CHAIN"


    var context: Context? = null

    fun init(ctx: Context) {
        context = ctx;
    }


    public fun saveChain(chain: Chain) {
        var sharedPref = context!!.getSharedPreferences(PREFS_CORE, Context.MODE_PRIVATE);
        var editor = sharedPref.edit();
        editor.putString(PREF_KEY_CHAIN, chainToJson(chain).toString());
        editor.commit();

        if(chain.chainLength == 0){
            NotificationMaster.dismissBrokenNotification()
            NotificationMaster.dismissReminderNotification()
        }

        if(chain.chainContainsToday()){
            NotificationMaster.dismissBrokenNotification()
            NotificationMaster.dismissReminderNotification()

            NotificationMaster.scheduleReminderNotification()
            NotificationMaster.scheduleBrokenNotification()
        }
    }

    public fun getCurrentChain(): Chain {
        var sharedPref = context!!.getSharedPreferences(PREFS_CORE, Context.MODE_PRIVATE);
        var chainStr = sharedPref.getString(PREF_KEY_CHAIN, "");
        var chain = chainFromJson(chainStr)
        if (chain == null || (!chain.chainContainsYesterday() && !chain.chainContainsToday())) {
            chain = generateDefaultChain()
            saveChain(chain)
        }
        return chain
    }


    val JSON_KEY_CHAINNAME = "chainname"
    val JSON_KEY_CHAINDATES = "chaindates"

    fun chainToJson(chain: Chain): JSONObject {
        var chainjson = JSONObject()
        chainjson.putOpt(JSON_KEY_CHAINNAME, chain.title)
        var datesjson = JSONArray()
        for (datetime in chain.dateTimes) {
            datesjson.put(datetime.toString())
        }
        chainjson.putOpt(JSON_KEY_CHAINDATES, datesjson)
        return chainjson

    }

    fun chainFromJson(chainJsonStr: String): Chain? {
        try {
            if (!TextUtils.isEmpty(chainJsonStr)) {
                var chainjson = JSONObject(chainJsonStr)
                if (chainjson != null) {
                    var chainName: String? = null
                    var chainDates: MutableSet<LocalDateTime>? = null
                    if (chainjson.has(JSON_KEY_CHAINNAME)) {
                        chainName = chainjson.getString(JSON_KEY_CHAINNAME)
                    }
                    if (chainName != null && chainjson.has(JSON_KEY_CHAINDATES)) {
                        chainDates = HashSet<LocalDateTime>()
                        var jsonarrDates = chainjson.getJSONArray(JSON_KEY_CHAINDATES)
                        if (jsonarrDates.length() > 0) {
                            for (i in 0..(jsonarrDates.length() - 1)) {
                                chainDates.add(LocalDateTime.parse(jsonarrDates.get(i).toString()))
                            }
                        }
                        return Chain(chainName, ArrayList<LocalDateTime>(chainDates))
                    }
                }
            }
        } catch(ex: Exception) {
            Timber.e(ex, "chainFromJson Fail")
        }
        return null
    }

    fun generateDefaultChain(): Chain {
        return Chain(context!!.getString(R.string.app_name), ArrayList<LocalDateTime>())
    }

    fun resetChain(){
        saveChain(generateDefaultChain())
    }
}