package com.hotpodata.redchain

import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v4.app.NotificationManagerCompat
import android.support.v7.app.NotificationCompat
import com.hotpodata.redchain.activity.ChainActivity
import com.hotpodata.redchain.receivers.BrokenChainNotificationReceiver
import com.hotpodata.redchain.receivers.ReminderNotificationReceiver
import org.joda.time.LocalDateTime
import timber.log.Timber

/**
 * Created by jdrotos on 11/7/15.
 */
object NotificationMaster {

    val PREFS_NOTIFICATIONMANAGER = "NotificationMaster"
    val PREF_KEY_SHOW_REMINDER = "PREF_KEY_SHOW_REMINDER"
    val PREF_KEY_SHOW_BROKEN = "PREF_KEY_SHOW_BROKEN"

    val NOTIF_REMINDER = 1
    val NOTIF_BROKEN = 2

    val RECEIVER_CODE_REMINDER = 1
    val RECEIVER_CODE_BROKEN = 2

    var context: Context? = null
    public fun init(ctx: Context) {
        context = ctx
    }

    public fun genReceiverCodeForBroken(chainId: String): Int {
        return RECEIVER_CODE_BROKEN + Math.abs(chainId.hashCode())
    }

    public fun genReceiverCodeForReminder(chainId: String): Int {
        return RECEIVER_CODE_REMINDER + Math.abs(chainId.hashCode())
    }

    public fun genNotifIdForBroken(chainId: String): Int {
        return NOTIF_BROKEN + Math.abs(chainId.hashCode())
    }

    public fun genNotifIdForReminder(chainId: String): Int {
        return NOTIF_REMINDER + Math.abs(chainId.hashCode())
    }

    public fun scheduleReminderNotification(chainId: String) {
        var intent = ReminderNotificationReceiver.IntentGenerator.generateIntent(context!!, chainId)
        var pending = PendingIntent.getBroadcast(context, genReceiverCodeForReminder(chainId), intent, PendingIntent.FLAG_UPDATE_CURRENT)
        var alarmManager: AlarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        try {
            alarmManager.cancel(pending)
        } catch(ex: Exception) {
            Timber.e(ex, "scheduleReminderNotification Exception")
        }
        var alarmTime = LocalDateTime.now().plusDays(1).minusHours(2).toDateTime().millis
        alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pending)
    }

    public fun dismissReminderNotification(chainId: String) {
        var notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(genNotifIdForReminder(chainId))
    }

    public fun scheduleBrokenNotification(chainId: String) {
        var intent = BrokenChainNotificationReceiver.IntentGenerator.generateIntent(context!!, chainId)
        var pending = PendingIntent.getBroadcast(context, genReceiverCodeForBroken(chainId), intent, PendingIntent.FLAG_UPDATE_CURRENT)
        var alarmManager: AlarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        try {
            alarmManager.cancel(pending)
        } catch(ex: Exception) {
            Timber.e(ex, "scheduleBrokenNotification Exception")
        }
        var alarmTime = LocalDateTime.now().plusDays(2).toLocalDate().toDateTimeAtStartOfDay().millis
        alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pending)
    }

    public fun dismissBrokenNotification(chainId: String) {
        var notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(genNotifIdForBroken(chainId))
    }

    public fun showReminderNotification(chainId: String) {
        if (showReminderEnabled()) {
            var notif = generateReminderNotification(chainId)
            if (notif != null) {
                var notificationManager = NotificationManagerCompat.from(context)
                notificationManager.notify(genNotifIdForReminder(chainId), notif)
            }
        }
    }

    public fun showBrokenChainNotification(chainId: String) {
        if (showBrokenEnabled()) {
            var notif = generateBrokenChainNotification(chainId)
            if (notif != null) {
                var notificationManager = NotificationManagerCompat.from(context)
                notificationManager.notify(genNotifIdForBroken(chainId), notif)
            }
        }
    }

    public fun generateReminderNotification(chainId: String): Notification? {
        var builder = NotificationCompat.Builder(context)
        var chain = ChainMaster.getChain(chainId)
        if (chain != null) {
            builder.setSmallIcon(R.drawable.ic_white_chain)
            builder.setColor(chain.color)
            builder.setContentTitle(chain.title)
            builder.setContentText(context?.getString(R.string.notification_reminder_text))
            builder.setContentIntent(genPendingIntent(chainId))
            builder.setAutoCancel(false)
            return builder.build()
        } else {
            return null
        }
    }

    public fun generateBrokenChainNotification(chainId: String): Notification? {
        var builder = NotificationCompat.Builder(context)
        var chain = ChainMaster.getChain(chainId)
        if (chain != null) {
            builder.setSmallIcon(R.drawable.ic_white_chain)
            builder.setColor(chain.color)
            builder.setContentTitle(context?.getString(R.string.notification_broken_title))
            builder.setContentText(context?.getString(R.string.notification_broken_text_template, chain.title))
            builder.setContentIntent(genPendingIntent(chainId))
            builder.setAutoCancel(true)
            return builder.build()
        } else {
            return null
        }
    }

    fun genPendingIntent(chainId: String): PendingIntent {
        var intent = ChainActivity.IntentGenerator.generateIntent(context!!, chainId)
        return PendingIntent.getActivity(context, chainId.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun setShowReminder(enabled: Boolean) {
        var sharedPref = context?.getSharedPreferences(PREFS_NOTIFICATIONMANAGER, Context.MODE_PRIVATE);
        var editor = sharedPref?.edit();
        editor?.putBoolean(PREF_KEY_SHOW_REMINDER, enabled);
        editor?.commit();
    }

    fun setShowBroken(enabled: Boolean) {
        var sharedPref = context?.getSharedPreferences(PREFS_NOTIFICATIONMANAGER, Context.MODE_PRIVATE);
        var editor = sharedPref?.edit();
        editor?.putBoolean(PREF_KEY_SHOW_BROKEN, enabled);
        editor?.commit();
    }

    fun showReminderEnabled(): Boolean {
        var sharedPref = context?.getSharedPreferences(PREFS_NOTIFICATIONMANAGER, Context.MODE_PRIVATE)
        return sharedPref!!.getBoolean(PREF_KEY_SHOW_REMINDER, true)
    }

    fun showBrokenEnabled(): Boolean {
        var sharedPref = context?.getSharedPreferences(PREFS_NOTIFICATIONMANAGER, Context.MODE_PRIVATE)
        return sharedPref!!.getBoolean(PREF_KEY_SHOW_BROKEN, true)
    }
}