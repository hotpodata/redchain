package com.hotpodata.redchain.activity

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.*
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TimePicker
import com.google.android.gms.analytics.HitBuilders
import com.hotpodata.redchain.AnalyticsMaster
import com.hotpodata.redchain.ChainMaster
import com.hotpodata.redchain.R
import com.hotpodata.redchain.adapter.ColorCircleAdapter
import com.hotpodata.redchain.data.Chain
import com.hotpodata.redchain.interfaces.ColorSelectedListener
import org.joda.time.LocalTime
import timber.log.Timber

/**
 * Created by jdrotos on 9/16/15.
 */
public class ChainEditActivity : ColorSelectedListener, ChameleonActivity() {
    val STATE_TITLE = "STATE_TITLE"
    val STATE_COLOR = "STATE_COLOR"
    val STATE_REMINDER_TIME = "STATE_REMINDER_TIME"
    val STATE_BROKEN_TIME = "STATE_BROKEN_TIME"

    var appBarLayout: AppBarLayout? = null
    var toolBar: Toolbar? = null
    var fab: FloatingActionButton? = null
    var titleEt: EditText? = null
    var chainColorRecyclerView: RecyclerView? = null
    var notifReminderEnabledCb: AppCompatCheckBox? = null
    var notifBrokenEnabledCb: AppCompatCheckBox? = null
    var notifReminderRadioGrp: RadioGroup? = null
    var notifBrokenRadioGrp: RadioGroup? = null
    var notifReminderTimeRadio: AppCompatRadioButton? = null
    var notifBrokenTimeRadio: AppCompatRadioButton? = null

    var colorAdapter: ColorCircleAdapter? = null

    var reminderTime: LocalTime = LocalTime.MIDNIGHT.plusHours(9)
    var brokenTime: LocalTime = LocalTime.MIDNIGHT.plusHours(9)

    var timeFormat = "hh:mmaa"

    object IntentGenerator {
        val ARG_SELECTED_CHAIN_ID = "SELECTED_CHAIN_ID"
        public fun generateNewChainIntent(context: Context): Intent {
            var intent = Intent(context, ChainEditActivity::class.java)
            return intent
        }

        public fun generateEditChainIntent(context: Context, chainId: String?): Intent {
            var intent = Intent(context, ChainEditActivity::class.java)
            intent.putExtra(ARG_SELECTED_CHAIN_ID, chainId)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_chain);
        appBarLayout = findViewById(R.id.app_bar_layout) as AppBarLayout?
        toolBar = findViewById(R.id.toolbar) as Toolbar?
        fab = findViewById(R.id.fab) as FloatingActionButton
        titleEt = findViewById(R.id.chain_title_edittext) as EditText
        chainColorRecyclerView = findViewById(R.id.chain_color_recyclerview) as RecyclerView
        notifReminderEnabledCb = findViewById(R.id.notif_daily_reminder_enabled_checkbox) as AppCompatCheckBox
        notifBrokenEnabledCb = findViewById(R.id.notif_broken_chain_enabled_checkbox) as AppCompatCheckBox
        notifReminderRadioGrp = findViewById(R.id.radiogroup_notif_reminder) as RadioGroup
        notifBrokenRadioGrp = findViewById(R.id.radiogroup_notif_broken) as RadioGroup
        notifReminderTimeRadio = findViewById(R.id.radio_reminder_custom_time) as AppCompatRadioButton
        notifBrokenTimeRadio = findViewById(R.id.radio_broken_custom_time) as AppCompatRadioButton

        colorAdapter = ColorCircleAdapter(this)
        colorAdapter?.colorSelectionLisetner = this
        chainColorRecyclerView?.adapter = colorAdapter
        chainColorRecyclerView?.layoutManager = GridLayoutManager(this, 4)

        setSupportActionBar(toolBar)
        supportActionBar.setDisplayHomeAsUpEnabled(true)
        supportActionBar.setHomeButtonEnabled(true)

        notifReminderEnabledCb?.setOnCheckedChangeListener { compoundButton, b ->
            notifReminderRadioGrp?.visibility = if (b) View.VISIBLE else View.GONE
        }

        notifBrokenEnabledCb?.setOnCheckedChangeListener { compoundButton, b ->
            notifBrokenRadioGrp?.visibility = if (b) View.VISIBLE else View.GONE
        }

        if (savedInstanceState != null) {
            titleEt?.setText(savedInstanceState.getString(STATE_TITLE, ""))
            colorAdapter?.selectedColor = savedInstanceState.getInt(STATE_COLOR, Color.RED)
            savedInstanceState.getString(STATE_REMINDER_TIME)?.let {
                reminderTime = LocalTime.parse(it)
            }
            savedInstanceState.getString(STATE_BROKEN_TIME)?.let {
                brokenTime = LocalTime.parse(it)
            }
        } else {
            getChainFromIntent()?.let {
                titleEt?.setText(it.title)
                colorAdapter?.selectedColor = it.color

                //Bind notification settings
                it.notifReminderSettings?.let {
                    notifReminderEnabledCb?.isChecked = it.enabled
                    it.customTime?.let { reminderTime = it }
                    notifReminderRadioGrp?.check(if (it.tracksLastActionTime) R.id.radio_reminder_complete_time else R.id.radio_reminder_custom_time)
                }

                it.notifBrokenSettings?.let {
                    notifBrokenEnabledCb?.isChecked = it.enabled
                    it.customTime?.let { brokenTime = it }
                    notifBrokenRadioGrp?.check(if (it.tracksLastActionTime) R.id.radio_broken_complete_time else R.id.radio_broken_custom_time)
                }
            }
        }

        notifReminderRadioGrp?.setOnCheckedChangeListener { radioGroup, i ->
            when (i) {
                R.id.radio_reminder_custom_time -> {
                    var dialog = TimePickerDialog(this, R.style.AppTheme,
                            {
                                timePicker: TimePicker?, i: Int, i1: Int ->
                                reminderTime = LocalTime(i, i1)
                                bindNotificationTimes()
                            }
                            , reminderTime.hourOfDay, reminderTime.minuteOfHour, false)
                            .show()
                }else -> {
            }
            }
        }

        notifBrokenRadioGrp?.setOnCheckedChangeListener { radioGroup, i ->
            when (i) {
                R.id.radio_broken_custom_time -> {
                    var dialog = TimePickerDialog(this, R.style.AppTheme,
                            {
                                timePicker: TimePicker?, i: Int, i1: Int ->
                                brokenTime = LocalTime(i, i1)
                                bindNotificationTimes()
                            }
                            , brokenTime.hourOfDay, brokenTime.minuteOfHour, false)
                            .show()
                }else -> {
            }
            }
        }



        fab?.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (validate()) {
                    var chain = getChainFromIntent()
                    if (chain == null) {
                        chain = Chain.Builder.buildFreshChain(getTitleFromEditText(), getColorFromPicker())
                    } else {
                        chain.title = getTitleFromEditText()
                        chain.color = getColorFromPicker()
                    }
                    chain.notifReminderSettings = getNotifSettingsForReminder()
                    chain.notifBrokenSettings = getNotifSettingsForBroken()

                    ChainMaster.saveChain(chain)

                    try {
                        AnalyticsMaster.getTracker(this@ChainEditActivity).send(HitBuilders.EventBuilder()
                                .setCategory(AnalyticsMaster.CATEGORY_ACTION)
                                .setAction(AnalyticsMaster.ACTION_SAVE)
                                .build());
                    } catch(ex: Exception) {
                        Timber.e(ex, "Analytics Exception");
                    }

                    var chainActivityIntent = ChainActivity.IntentGenerator.generateIntent(this@ChainEditActivity, chain.id)
                    startActivity(chainActivityIntent)
                    finish()
                }
            }
        })

        var actionbar = supportActionBar
        if (getChainFromIntent() == null) {
            actionbar?.title = resources.getString(R.string.create_chain)
        } else {
            actionbar?.title = resources.getString(R.string.edit_chain)
        }


    }

    override fun onResume() {
        super.onResume()
        var screenName = if (getChainFromIntent() == null) AnalyticsMaster.SCREEN_NEW else AnalyticsMaster.SCREEN_EDIT
        Timber.i("Setting screen name:" + screenName);
        AnalyticsMaster.getTracker(this).setScreenName(screenName);
        AnalyticsMaster.getTracker(this).send(HitBuilders.ScreenViewBuilder().build());

        setColor(getColorFromPicker(), false)
        bindNotificationTimes()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putString(STATE_TITLE, getTitleFromEditText())
        outState?.putInt(STATE_COLOR, getColorFromPicker())
        outState?.putString(STATE_REMINDER_TIME, reminderTime.toString())
        outState?.putString(STATE_BROKEN_TIME, brokenTime.toString())
    }

    fun bindNotificationTimes() {
        notifReminderTimeRadio?.text = getString(R.string.notif_radio_custom_time_template, reminderTime.toString(timeFormat))
        notifBrokenTimeRadio?.text = getString(R.string.notif_radio_custom_time_template, brokenTime.toString(timeFormat))
    }

    fun getTitleFromEditText(): String {
        if (titleEt != null) {
            return titleEt?.text.toString()
        }
        return ""
    }

    fun getColorFromPicker(): Int {
        if (colorAdapter != null) {
            return colorAdapter!!.selectedColor
        }
        return Color.RED
    }

    fun getNotifSettingsForReminder(): Chain.NotificationSettings {
        return Chain.NotificationSettings(notifReminderEnabledCb?.isChecked ?: false, notifReminderRadioGrp?.checkedRadioButtonId == R.id.radio_reminder_complete_time, reminderTime)
    }

    fun getNotifSettingsForBroken(): Chain.NotificationSettings {
        return Chain.NotificationSettings(notifBrokenEnabledCb?.isChecked ?: false, notifBrokenRadioGrp?.checkedRadioButtonId == R.id.radio_broken_complete_time, brokenTime)
    }

    fun getChainFromIntent(): Chain? {
        var intent = intent
        var chain: Chain? = null;
        if (intent != null && intent.hasExtra(IntentGenerator.ARG_SELECTED_CHAIN_ID)) {
            chain = ChainMaster.getChain(intent.getStringExtra(IntentGenerator.ARG_SELECTED_CHAIN_ID))
        }
        return chain
    }

    fun validate(): Boolean {
        if (TextUtils.isEmpty(getTitleFromEditText())) {
            if (titleEt != null) {
                titleEt?.error = resources.getString(R.string.error_invalid_title)
            }
            return false;
        }
        return true;
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item);
    }

    override fun onColorSelected(color: Int) {
        setColor(color, true)
        try {
            AnalyticsMaster.getTracker(this).send(HitBuilders.EventBuilder()
                    .setCategory(AnalyticsMaster.CATEGORY_ACTION)
                    .setAction(AnalyticsMaster.ACTION_CHANGE_COLOR)
                    .setLabel(AnalyticsMaster.LABEL_COLOR)
                    .setValue(color.toLong())
                    .build());
        } catch(ex: Exception) {
            Timber.e(ex, "Analytics Exception");
        }
    }
}