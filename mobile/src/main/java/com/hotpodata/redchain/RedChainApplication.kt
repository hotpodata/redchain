package com.hotpodata.redchain

import android.support.multidex.MultiDexApplication
import timber.log.Timber

/**
 * Created by jdrotos on 9/17/15.
 */
class RedChainApplication : MultiDexApplication() {
    public override fun onCreate(){
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        ChainMaster.init(this)
        NotificationMaster.init(this)
    }
}