package com.hotpodata.redchain

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import com.hotpodata.redchain.data.Chain
import org.joda.time.LocalDateTime
import org.json.JSONArray
import timber.log.Timber
import java.util.*

/**
 * Created by jdrotos on 11/7/15.
 */
object ChainMaster {

    /**
     * PREF DATA MAPS ->
     * chainids = list of chain ids
     * active_chain_id = id of the currently selected chain
     * <id> = each chain is stored under the key of its id
     */

    val PREFS_CORE = "core"
    val PREF_ACTIVE_CHAIN_ID = "active_chain_id"
    val PREF_ALL_CHAINS = "chainids"
    val DEFAULT_CHAIN_ID = "DEFAULT_CHAIN_ID"

    var context: Context? = null
    var selectedChainId: String = DEFAULT_CHAIN_ID
    var allChains: MutableMap<String, Chain> = HashMap()

    fun init(ctx: Context) {
        context = ctx;
        readDataFromPrefs()
        ensureValidData()
    }

    @Suppress("DEPRECATION")
    private fun ensureValidData() {
        if (allChains.size <= 0) {
            //WE DONT HAVE ANY REAL DATA??
            var chain = Chain(DEFAULT_CHAIN_ID, context!!.getString(R.string.app_name), context!!.resources.getColor(R.color.primary), ArrayList<LocalDateTime>())
            saveChain(chain)
            setSelectedChain(chain.id)
            Timber.d("ensureValidData - Setting selected chain:" + chain.title + " id:" + chain.id)
        }
        if (!allChains.containsKey(selectedChainId)) {
            //WE DONT HAVE DATA FOR OUR SELECTED CHAIN...
            Timber.d("ensureValidData - selectedChainId not found");
            var chain = allChains.get(allChains.keys.first())
            if (chain != null) {
                Timber.d("ensureValidData - Setting selected chain:" + chain.title + " id:" + chain.id)
                setSelectedChain(chain.id)
            } else {
                Timber.e("ensureValidData - fail")
            }
        }
    }

    public fun expireExpiredChains() {
        for (chainid in allChains.keys) {
            var chain = allChains.get(chainid)
            if (chain != null && chain.chainExpired()) {
                chain.clearDates()
                saveChain(chain)
            }
        }
    }

    private fun readDataFromPrefs() {
        selectedChainId = readSelectedChainId()
        allChains.clear()
        var chainIds = readChainIds()
        for (chainId in chainIds) {
            var chain = readChain(chainId)
            if (chain != null) {
                allChains.put(chainId, chain)
            }
        }
    }

    public fun getSelectedChain(): Chain {
        ensureValidData()
        return allChains.get(selectedChainId)!!
    }

    public fun getLongestRunOfAllChains(): Int{
        var longest = 0
        for(chain in allChains.values){
            if(chain.longestRun > longest){
                longest = chain.longestRun
            }
        }
        return longest
    }

    public fun getChain(chainId: String): Chain? {
        return allChains.get(chainId);
    }

    public fun setSelectedChain(chainId: String): Boolean {
        if (allChains.contains(chainId)) {
            if (!chainId.equals(selectedChainId)) {
                selectedChainId = chainId
                writeSelectedChainId(chainId)
            }
            return true
        }
        return false
    }

    public fun deleteChain(chainId: String) {
        if (allChains.containsKey(chainId)) {
            allChains.remove(chainId)
            writeChainIds(allChains.keys)
        }
        eraseChain(chainId)
        ensureValidData()
    }

    public fun saveChain(chain: Chain) {
        var needsIdUpdate = !allChains.containsKey(chain.id)
        allChains.put(chain.id, chain)
        if (needsIdUpdate) {
            writeChainIds(allChains.keys)
        }
        writeChain(chain)

        //This still seems like a weird spot, but it should do the trick
        scheduleChainNotifications(chain)
    }

    private fun scheduleChainNotifications(chain: Chain){
        if (chain.chainLength == 0) {
            NotificationMaster.dismissBrokenNotification(chain.id)
            NotificationMaster.dismissReminderNotification(chain.id)
        }
        if (chain.chainContainsToday()) {
            NotificationMaster.dismissReminderNotification(chain.id)
            NotificationMaster.scheduleReminderNotification(chain)

            NotificationMaster.dismissBrokenNotification(chain.id)
            NotificationMaster.scheduleBrokenNotification(chain)
        }
    }

    private fun readChain(id: String): Chain? {
        var sharedPref = getSharedPrefs()
        var chainStr = sharedPref.getString(id, "");
        Timber.d("readChain:" + id + " dat:" + chainStr)
        var chain = Chain.Serializer.chainFromJson(chainStr)
        return chain
    }

    private fun writeChain(chain: Chain) {
        var sharedPref = getSharedPrefs()
        var editor = sharedPref.edit();
        var chainStr = Chain.Serializer.chainToJson(chain).toString()
        Timber.d("writeChain" + chainStr)
        editor.putString(chain.id, chainStr);
        editor.commit();
    }

    private fun eraseChain(id: String) {
        var sharedPref = getSharedPrefs()
        var editor = sharedPref.edit();
        editor.remove(id)
        editor.commit();
    }


    private fun readSelectedChainId(): String {
        var sharedPref = getSharedPrefs()
        var id = sharedPref.getString(PREF_ACTIVE_CHAIN_ID, DEFAULT_CHAIN_ID);
        Timber.d("readSelectedChainId:" + id)
        return id
    }

    private fun writeSelectedChainId(id: String) {
        var sharedPref = getSharedPrefs()
        var editor = sharedPref.edit();
        Timber.d("writeSelectedChainId:" + id)
        editor.putString(PREF_ACTIVE_CHAIN_ID, id);
        editor.commit();
    }

    private fun readChainIds(): List<String> {
        var sharedPref = getSharedPrefs()
        var chainStr = sharedPref.getString(PREF_ALL_CHAINS, null);
        if (chainStr == null) {
            Timber.d("readChainIds:emptylist")
            return ArrayList()
        } else {
            var jsonChainIds = JSONArray(chainStr)
            var chainIds = ArrayList<String>()
            for (i in 0..(jsonChainIds.length() - 1)) {
                chainIds.add(jsonChainIds.getString(i))
            }
            Timber.d("readChainIds:" + chainIds)
            return chainIds
        }
    }

    private fun writeChainIds(chainIds: MutableSet<String>) {
        var sharedPref = getSharedPrefs()
        var chainIdsJsonArr = JSONArray()
        for (chainId in chainIds) {
            chainIdsJsonArr.put(chainId)
        }
        var editor = sharedPref.edit();
        Timber.d("writeChainIds" + chainIdsJsonArr.toString())
        editor.putString(PREF_ALL_CHAINS, chainIdsJsonArr.toString());
        editor.commit();
    }

    private fun getSharedPrefs(): SharedPreferences {
        return context!!.getSharedPreferences(PREFS_CORE, Context.MODE_PRIVATE);
    }




}