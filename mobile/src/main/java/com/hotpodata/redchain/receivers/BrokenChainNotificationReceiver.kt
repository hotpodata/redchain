package com.hotpodata.redchain.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hotpodata.redchain.NotificationMaster

/**
 * Created by jdrotos on 11/7/15.
 */
class BrokenChainNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        NotificationMaster.dismissReminderNotification()
        NotificationMaster.showBrokenChainNotification()
    }
}