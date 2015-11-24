package com.hotpodata.redchain.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import com.google.android.gms.analytics.HitBuilders
import com.hotpodata.redchain.AnalyticsMaster
import com.hotpodata.redchain.ChainMaster
import com.hotpodata.redchain.R
import com.hotpodata.redchain.adapter.ColorCircleAdapter
import com.hotpodata.redchain.data.Chain
import com.hotpodata.redchain.interfaces.ColorSelectedListener
import timber.log.Timber

/**
 * Created by jdrotos on 9/16/15.
 */
public class ChainEditActivity : ColorSelectedListener, ChameleonActivity() {
    val STATE_TITLE = "STATE_TITLE"
    val STATE_COLOR = "STATE_COLOR"

    var appBarLayout: AppBarLayout? = null
    var toolBar: Toolbar? = null
    var fab: FloatingActionButton? = null
    var titleEt: EditText? = null
    var chainColorRecyclerView: RecyclerView? = null

    var colorAdapter: ColorCircleAdapter? = null

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

        colorAdapter = ColorCircleAdapter(this)
        colorAdapter?.colorSelectionLisetner = this
        chainColorRecyclerView?.adapter = colorAdapter
        chainColorRecyclerView?.layoutManager = GridLayoutManager(this, 4)

        setSupportActionBar(toolBar)
        supportActionBar.setDisplayHomeAsUpEnabled(true)
        supportActionBar.setHomeButtonEnabled(true)

        if (savedInstanceState != null) {
            titleEt?.setText(savedInstanceState.getString(STATE_TITLE, ""))
            colorAdapter?.selectedColor = savedInstanceState.getInt(STATE_COLOR, Color.RED)
        } else {
            var chain = getChainFromIntent()
            if (chain != null) {
                titleEt?.setText(chain.title)
                colorAdapter?.selectedColor = chain.color
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
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putString(STATE_TITLE, getTitleFromEditText())
        outState?.putInt(STATE_COLOR, getColorFromPicker())
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