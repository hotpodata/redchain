package com.hotpodata.redchain

import android.content.Context
import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.Tracker

/**
 * Created by jdrotos on 11/18/15.
 */
object AnalyticsMaster {

    //SCREENS
    val SCREEN_CHAIN = "Chain"
    val SCREEN_NEW = "New"
    val SCREEN_PRO_FEATURES = "ProFeature"
    val SCREEN_EDIT = "Edit"

    //CATEGORIES
    val CATEGORY_ACTION = "Action"

    //ACTIONS
    val ACTION_SELECT_CHAIN = "Selected_Chain"
    val ACTION_NEW_CHAIN = "New_Chain"
    val ACTION_EDIT_CHAIN = "Edit_Chain"
    val ACTION_RESET_CHAIN = "Reset_Chain"
    val ACTION_RESET_TODAY = "Reset_Today"
    val ACTION_DELETE_CHAIN = "Delete_Chain"
    val ACTION_RATE_APP = "Rate_App"
    val ACTION_CONTACT = "Contact"
    val ACTION_WEBSITE = "Website"
    val ACTION_GO_PRO_SIDEBAR = "Go_Pro_Sidebar"
    val ACTION_GO_PRO_FRAGMENT = "Go_Pro_Fragment"
    val ACTION_TOGGLE_REMINDER_NOTIFICATION = "Toggle_Reminder_Notification"
    val ACTION_TOGGLE_BROKEN_NOTIFICATION = "Toggle_Broken_Chain_Notification"
    val ACTION_CHANGE_COLOR = "Edit_Change_Color"
    val ACTION_SAVE = "Save"

    //LABELS
    val LABEL_COLOR = "Color"

    private var tracker: Tracker? = null
    public fun getTracker(context: Context): Tracker? {
        if (tracker == null) {
            var analytics = GoogleAnalytics.getInstance(context)
            tracker = analytics.newTracker(R.xml.global_tracker)
            tracker?.enableExceptionReporting(true);
            tracker?.enableAdvertisingIdCollection(true);
            tracker?.enableAutoActivityTracking(true);
        }
        return tracker
    }
}