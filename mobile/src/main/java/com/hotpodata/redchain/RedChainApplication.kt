package com.hotpodata.redchain

import android.support.multidex.MultiDexApplication
import timber.log.Timber

/**
 * Created by jdrotos on 9/17/15.
 */
class RedChainApplication : MultiDexApplication() {


    public override fun onCreate(){
        super.onCreate()
        if(BuildConfig.LOGGING_ENABLED) {
            Timber.plant(Timber.DebugTree())
        }

        //Init chain master
        ChainMaster.init(this)

//        //Add some test data for debug builds
//        if(BuildConfig.IS_DEBUG_BUILD && BuildConfig.IS_PRO){
//            var chainOne = Chain("test1","BlueChain", resources.getColor(R.color.material_blue), ArrayList())
//            var chainTwo = Chain("test2","AmberChain", resources.getColor(R.color.material_amber), ArrayList())
//            var chainThree = Chain("test3","PurpleChain", resources.getColor(R.color.material_purple), ArrayList())
//            if(!ChainMaster.allChains.containsKey("test1")) {
//                ChainMaster.saveChain(chainOne)
//            }
//            if(!ChainMaster.allChains.containsKey("test2")) {
//                ChainMaster.saveChain(chainTwo)
//            }
//            if(!ChainMaster.allChains.containsKey("test3")) {
//                ChainMaster.saveChain(chainThree)
//            }
//        }

        NotificationMaster.init(this)
    }



}