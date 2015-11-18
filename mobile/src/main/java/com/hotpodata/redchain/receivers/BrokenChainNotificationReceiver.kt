package com.hotpodata.redchain.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hotpodata.redchain.NotificationMaster

/**
 * Created by jdrotos on 11/7/15.
 */
class BrokenChainNotificationReceiver : BroadcastReceiver() {

    object IntentGenerator {
        val ARG_SELECTED_CHAIN = "SELECTED_CHAIN"
        public fun generateIntent(context: Context, chainId: String?): Intent {
            var intent = Intent(context, BrokenChainNotificationReceiver::class.java)
            intent.putExtra(ARG_SELECTED_CHAIN, chainId)
            return intent
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent != null && intent.hasExtra(IntentGenerator.ARG_SELECTED_CHAIN)) {
            var chainId = intent.getStringExtra(IntentGenerator.ARG_SELECTED_CHAIN)
            NotificationMaster.dismissReminderNotification(chainId)
            NotificationMaster.showBrokenChainNotification(chainId)
        }
    }
}