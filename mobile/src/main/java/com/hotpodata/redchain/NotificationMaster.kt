package com.hotpodata.redchain

import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.support.v4.app.NotificationManagerCompat
import android.support.v7.app.NotificationCompat
import com.hotpodata.redchain.activity.ChainActivity
import com.hotpodata.redchain.data.Chain
import com.hotpodata.redchain.receivers.BrokenChainNotificationReceiver
import com.hotpodata.redchain.receivers.ReminderNotificationReceiver
import org.joda.time.LocalDateTime
import timber.log.Timber

/**
 * Created by jdrotos on 11/7/15.
 */
object NotificationMaster {

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

    public fun scheduleReminderNotification(chain: Chain) {
        var chainId = chain.id
        var intent = ReminderNotificationReceiver.IntentGenerator.generateIntent(context!!, chainId)
        var pending = PendingIntent.getBroadcast(context, genReceiverCodeForReminder(chainId), intent, PendingIntent.FLAG_UPDATE_CURRENT)
        var alarmManager: AlarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        try {
            alarmManager.cancel(pending)
        } catch(ex: Exception) {
            Timber.e(ex, "scheduleReminderNotification Exception")
        }
        chain.notifReminderSettings?.let {
            var alarmTime = if (it.tracksLastActionTime || it.customTime == null) {
                chain.newestDate?.plusDays(1)?.toDateTime()?.millis ?: LocalDateTime.now().plusDays(1).toDateTime().millis
            } else {
                LocalDateTime.now().withHourOfDay(it.customTime.hourOfDay).withMinuteOfHour(it.customTime.minuteOfHour).plusDays(1).toDateTime().millis
            }
            alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pending)
        }

    }

    public fun dismissReminderNotification(chainId: String) {
        var notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(genNotifIdForReminder(chainId))
    }

    public fun scheduleBrokenNotification(chain: Chain) {
        var chainId = chain.id
        var intent = BrokenChainNotificationReceiver.IntentGenerator.generateIntent(context!!, chainId)
        var pending = PendingIntent.getBroadcast(context, genReceiverCodeForBroken(chainId), intent, PendingIntent.FLAG_UPDATE_CURRENT)
        var alarmManager: AlarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        try {
            alarmManager.cancel(pending)
        } catch(ex: Exception) {
            Timber.e(ex, "scheduleBrokenNotification Exception")
        }
        chain.notifBrokenSettings?.let {
            var alarmTime = if (it.tracksLastActionTime || it.customTime == null) {
                chain.newestDate?.plusDays(2)?.toDateTime()?.millis ?: LocalDateTime.now().plusDays(2).toDateTime().millis
            } else {
                LocalDateTime.now().withHourOfDay(it.customTime.hourOfDay).withMinuteOfHour(it.customTime.minuteOfHour).plusDays(1).toDateTime().millis
            }
            alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pending)
        }
    }

    public fun dismissBrokenNotification(chainId: String) {
        var notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(genNotifIdForBroken(chainId))
    }

    public fun showReminderNotification(chainId: String) {
        ChainMaster.getChain(chainId)?.let {
            if (it.notifReminderSettings?.enabled ?: false) {
                generateReminderNotification(chainId)?.let {
                    NotificationManagerCompat.from(context).notify(genNotifIdForReminder(chainId), it)
                }
            }
        }
    }

    public fun showBrokenChainNotification(chainId: String) {
        ChainMaster.getChain(chainId)?.let {
            if (it.notifBrokenSettings?.enabled ?: false) {
                generateBrokenChainNotification(chainId)?.let {
                    NotificationManagerCompat.from(context).notify(genNotifIdForBroken(chainId), it)
                }
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
}