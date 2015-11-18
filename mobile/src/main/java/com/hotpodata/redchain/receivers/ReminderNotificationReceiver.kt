package com.hotpodata.redchain.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import com.hotpodata.redchain.ChainMaster
import com.hotpodata.redchain.NotificationMaster
import com.hotpodata.redchain.activity.ChainActivity

/**
 * Created by jdrotos on 11/7/15.
 */
class ReminderNotificationReceiver : BroadcastReceiver() {

    object IntentGenerator {
        val ARG_SELECTED_CHAIN = "SELECTED_CHAIN"
        public fun generateIntent(context: Context, chainId: String?): Intent {
            var intent = Intent(context, ReminderNotificationReceiver::class.java)
            intent.putExtra(ARG_SELECTED_CHAIN, chainId)
            return intent
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null && intent.hasExtra(ReminderNotificationReceiver.IntentGenerator.ARG_SELECTED_CHAIN)) {
            var chainId = intent.getStringExtra(ReminderNotificationReceiver.IntentGenerator.ARG_SELECTED_CHAIN)
            if (!TextUtils.isEmpty(chainId)) {
                var chain = ChainMaster.getChain(chainId)
                if (chain != null && !chain.chainContainsToday()) {
                    NotificationMaster.showReminderNotification(chainId)
                }
            }
        }
    }
}