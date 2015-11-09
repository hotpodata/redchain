package com.hotpodata.redchain

import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v4.app.NotificationManagerCompat
import android.support.v7.app.NotificationCompat
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

    public fun scheduleReminderNotification() {
        var intent = Intent(context, ReminderNotificationReceiver::class.java)
        var pending = PendingIntent.getBroadcast(context, RECEIVER_CODE_REMINDER, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        var alarmManager: AlarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        try {
            alarmManager.cancel(pending)
        } catch(ex: Exception) {
            Timber.e(ex, "scheduleReminderNotification Exception")
        }
        var alarmTime = LocalDateTime.now().plusDays(1).minusHours(2).toDateTime().millis
        alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pending)
    }

    public fun dismissReminderNotification() {
        var notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(NOTIF_REMINDER)
    }

    public fun scheduleBrokenNotification() {
        var intent = Intent(context, BrokenChainNotificationReceiver::class.java)
        var pending = PendingIntent.getBroadcast(context, RECEIVER_CODE_BROKEN, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        var alarmManager: AlarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        try {
            alarmManager.cancel(pending)
        } catch(ex: Exception) {
            Timber.e(ex, "scheduleBrokenNotification Exception")
        }
        var alarmTime = LocalDateTime.now().plusDays(2).toLocalDate().toDateTimeAtStartOfDay().millis
        alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pending)
    }

    public fun dismissBrokenNotification() {
        var notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(NOTIF_BROKEN)
    }

    public fun showReminderNotification() {
        if (showReminderEnabled()) {
            var notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(NOTIF_REMINDER, generateReminderNotification())
        }
    }

    public fun showBrokenChainNotification() {
        if (showBrokenEnabled()) {
            var notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(NOTIF_BROKEN, generateBrokenChainNotification())
        }
    }

    public fun generateReminderNotification(): Notification {
        var builder = NotificationCompat.Builder(context)
        builder.setSmallIcon(R.drawable.ic_white_chain)
        builder.setColor(context?.resources!!.getColor(R.color.primary))
        builder.setContentTitle(context?.getString(R.string.notification_reminder_title))
        builder.setContentText(context?.getString(R.string.notification_reminder_text))
        builder.setContentIntent(genPendingIntent())
        builder.setAutoCancel(true)
        return builder.build()
    }

    public fun generateBrokenChainNotification(): Notification {
        var builder = NotificationCompat.Builder(context)
        builder.setSmallIcon(R.drawable.ic_white_chain)
        builder.setColor(context?.resources!!.getColor(R.color.primary))
        builder.setContentTitle(context?.getString(R.string.notification_broken_title))
        builder.setContentText(context?.getString(R.string.notification_broken_text))
        builder.setContentIntent(genPendingIntent())
        builder.setAutoCancel(true)
        return builder.build()
    }

    fun genPendingIntent(): PendingIntent {
        var intent = Intent(context, ChainActivity::class.java)
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
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