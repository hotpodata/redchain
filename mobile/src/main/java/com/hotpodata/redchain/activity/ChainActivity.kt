package com.hotpodata.redchain.activity

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.analytics.Tracker
import com.hotpodata.redchain.*
import com.hotpodata.redchain.adapter.ChainAdapter
import com.hotpodata.redchain.adapter.SideBarAdapter
import com.hotpodata.redchain.data.Chain
import com.hotpodata.redchain.fragment.GoProChainFragment
import com.hotpodata.redchain.interfaces.ChainUpdateListener
import com.hotpodata.redchain.utils.IntentUtils
import org.joda.time.LocalDateTime
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.util.*

/**
 * Created by jdrotos on 9/16/15.
 */
public class ChainActivity : ChainUpdateListener, ChameleonActivity() {

    val FTAG_GO_PRO = "GO_PRO"

    var appBarLayout: AppBarLayout? = null
    var toolBar: Toolbar? = null
    var recyclerView: RecyclerView? = null
    var drawerLayout: DrawerLayout? = null
    var drawerToggle: ActionBarDrawerToggle? = null
    var leftDrawerRecyclerView: RecyclerView? = null

    var chainAdapter: ChainAdapter? = null
    var sideBarAdapter: SideBarAdapter? = null

    object IntentGenerator {
        val ARG_SELECTED_CHAIN = "SELECTED_CHAIN"
        public fun generateIntent(context: Context, chainId: String?): Intent {
            var intent = Intent(context, ChainActivity::class.java)
            intent.putExtra(ARG_SELECTED_CHAIN, chainId)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chain_list);

        appBarLayout = findViewById(R.id.app_bar_layout) as AppBarLayout?
        toolBar = findViewById(R.id.toolbar) as Toolbar?
        recyclerView = findViewById(R.id.recycler_view) as RecyclerView?
        drawerLayout = findViewById(R.id.drawer_layout) as DrawerLayout?
        leftDrawerRecyclerView = findViewById(R.id.left_drawer_recyclerview) as RecyclerView?
        setSupportActionBar(toolBar)

        drawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolBar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout?.setDrawerListener(drawerToggle)

        supportActionBar.setDisplayHomeAsUpEnabled(true)
        supportActionBar.setHomeButtonEnabled(true)

        consumeIntent(intent)
    }

    private fun consumeIntent(intent: Intent?) {
        if (intent != null) {
            if (intent.hasExtra(IntentGenerator.ARG_SELECTED_CHAIN)) {
                var chainId = intent.getStringExtra(IntentGenerator.ARG_SELECTED_CHAIN)
                if (chainId != null) {
                    ChainMaster.setSelectedChain(chainId)
                    drawerLayout?.closeDrawers()
                }
            }
        }
        ChainMaster.expireExpiredChains()
        refreshChain()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        consumeIntent(intent)
    }

    public override fun onResume() {
        super.onResume()
        Timber.i("Setting screen name:" + AnalyticsMaster.SCREEN_CHAIN);
        AnalyticsMaster.getTracker(this)?.setScreenName(AnalyticsMaster.SCREEN_CHAIN);
        AnalyticsMaster.getTracker(this)?.send(HitBuilders.ScreenViewBuilder().build());

        ChainMaster.expireExpiredChains()
        refreshChain()
    }

    public override fun onCreateOptionsMenu(menu: Menu): Boolean {
        var inflater = getMenuInflater()
        inflater.inflate(R.menu.chain_menu, menu)
        return true;
    }

    public override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        var resetTodayItem = menu?.findItem(R.id.reset_today_only)
        if (resetTodayItem != null) {
            var chain = ChainMaster.getSelectedChain()
            if (chain.chainContainsToday()) {
                resetTodayItem.setVisible(true)
                resetTodayItem.setEnabled(true)
            } else {
                resetTodayItem.setVisible(false)
                resetTodayItem.setEnabled(false)
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.edit_chain -> {
                var intent = ChainEditActivity.IntentGenerator.generateEditChainIntent(this@ChainActivity, ChainMaster.selectedChainId);
                startActivity(intent)

                try {
                    AnalyticsMaster.getTracker(this)?.send(HitBuilders.EventBuilder()
                            .setCategory(AnalyticsMaster.CATEGORY_ACTION)
                            .setAction(AnalyticsMaster.ACTION_EDIT_CHAIN)
                            .build());
                } catch(ex: Exception) {
                    Timber.e(ex, "Analytics Exception");
                }

                return true
            }
            R.id.reset_today_only -> {
                var chain = ChainMaster.getSelectedChain()
                chain.removeTodayFromChain()
                ChainMaster.saveChain(chain)
                refreshChain()
                try {
                    AnalyticsMaster.getTracker(this)?.send(HitBuilders.EventBuilder()
                            .setCategory(AnalyticsMaster.CATEGORY_ACTION)
                            .setAction(AnalyticsMaster.ACTION_RESET_TODAY)
                            .build());
                } catch(ex: Exception) {
                    Timber.e(ex, "Analytics Exception");
                }
                supportInvalidateOptionsMenu()
                return true
            }
            R.id.reset_chain -> {
                var builder = AlertDialog.Builder(this)
                builder.setMessage(R.string.reset_chain_confirm)
                builder.setCancelable(true)
                builder.setPositiveButton(R.string.reset,
                        DialogInterface.OnClickListener {
                            dialogInterface, i ->
                            var chain = ChainMaster.getSelectedChain()
                            chain.clearDates()
                            ChainMaster.saveChain(chain)
                            refreshChain()
                            drawerLayout?.closeDrawers()

                            try {
                                AnalyticsMaster.getTracker(this)?.send(HitBuilders.EventBuilder()
                                        .setCategory(AnalyticsMaster.CATEGORY_ACTION)
                                        .setAction(AnalyticsMaster.ACTION_RESET_CHAIN)
                                        .build());
                            } catch(ex: Exception) {
                                Timber.e(ex, "Analytics Exception");
                            }


                        })
                builder.setNegativeButton(R.string.cancel, DialogInterface.OnClickListener { dialogInterface, i -> dialogInterface.cancel() })
                builder.create().show()
                return true;
            }
            R.id.delete_chain -> {
                var builder = AlertDialog.Builder(this)
                builder.setMessage(R.string.delete_chain_confirm)
                builder.setCancelable(true)
                builder.setPositiveButton(R.string.delete,
                        DialogInterface.OnClickListener {
                            dialogInterface, i ->
                            ChainMaster.deleteChain(ChainMaster.selectedChainId as String)
                            refreshChain()

                            try {
                                AnalyticsMaster.getTracker(this)?.send(HitBuilders.EventBuilder()
                                        .setCategory(AnalyticsMaster.CATEGORY_ACTION)
                                        .setAction(AnalyticsMaster.ACTION_DELETE_CHAIN)
                                        .build());
                            } catch(ex: Exception) {
                                Timber.e(ex, "Analytics Exception");
                            }

                        })
                builder.setNegativeButton(R.string.cancel, DialogInterface.OnClickListener { dialogInterface, i -> dialogInterface.cancel() })
                builder.create().show()
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }


    public fun refreshChain() {
        var chain = ChainMaster.getSelectedChain()
        if (chainAdapter == null) {
            chainAdapter = ChainAdapter(this, chain)
            chainAdapter?.chainUpdateListener = this
            recyclerView?.setAdapter(chainAdapter)
            recyclerView?.setLayoutManager(LinearLayoutManager(this))
        } else {
            chainAdapter?.updateChain(chain)
        }
        getSupportActionBar().setTitle(chain.title)
        setColor(chain.color, true)
        refreshSideBar()
    }

    public fun refreshSideBar() {
        var chain = ChainMaster.getSelectedChain()
        var sideBarRows = ArrayList<Any>()

        var version: String? = null
        try {
            val pInfo = packageManager.getPackageInfo(packageName, 0)
            version = getString(R.string.version_template, pInfo.versionName)
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.e(e, "Version fail")
        }

        sideBarRows.add(SideBarAdapter.SideBarHeading(getString(R.string.app_name), version))

        sideBarRows.add(getString(R.string.chains))
        for (chain in ChainMaster.allChains.values) {
            sideBarRows.add(SideBarAdapter.RowChain(chain, chain.id == ChainMaster.selectedChainId, object : View.OnClickListener {
                override fun onClick(view: View) {
                    var intent = IntentGenerator.generateIntent(this@ChainActivity, chain.id)
                    startActivity(intent)
                    try {
                        AnalyticsMaster.getTracker(this@ChainActivity)?.send(HitBuilders.EventBuilder()
                                .setCategory(AnalyticsMaster.CATEGORY_ACTION)
                                .setAction(AnalyticsMaster.ACTION_SELECT_CHAIN)
                                .build());
                    } catch(ex: Exception) {
                        Timber.e(ex, "Analytics Exception");
                    }
                }
            }))
            sideBarRows.add(SideBarAdapter.Div(true))
        }
        sideBarRows.add(SideBarAdapter.RowCreateChain(getString(R.string.create_chain), "", object : View.OnClickListener {
            override fun onClick(view: View) {
                drawerLayout?.closeDrawers()
                if(BuildConfig.IS_PRO) {
                    var intent = ChainEditActivity.IntentGenerator.generateNewChainIntent(this@ChainActivity);
                    startActivity(intent)
                }else{
                    showGoPro()
                }
                try {
                    AnalyticsMaster.getTracker(this@ChainActivity)?.send(HitBuilders.EventBuilder()
                            .setCategory(AnalyticsMaster.CATEGORY_ACTION)
                            .setAction(AnalyticsMaster.ACTION_NEW_CHAIN)
                            .build());
                } catch(ex: Exception) {
                    Timber.e(ex, "Analytics Exception");
                }
            }
        }, R.drawable.ic_action_new))

        sideBarRows.add(getString(R.string.actions))
        if(!BuildConfig.IS_PRO) {
            sideBarRows.add(SideBarAdapter.SettingsRow(getString(R.string.go_pro_action), getString(R.string.go_pro_create_edit_blurb, getString(R.string.app_name)), object : View.OnClickListener {
                override fun onClick(view: View) {
                    var intent = IntentUtils.goPro(this@ChainActivity)
                    startActivity(intent)
                    try {
                        AnalyticsMaster.getTracker(this@ChainActivity)?.send(HitBuilders.EventBuilder()
                                .setCategory(AnalyticsMaster.CATEGORY_ACTION)
                                .setAction(AnalyticsMaster.ACTION_GO_PRO_SIDEBAR)
                                .build());
                    } catch(ex: Exception) {
                        Timber.e(ex, "Analytics Exception");
                    }
                }
            }, R.drawable.ic_action_go_pro))
            sideBarRows.add(SideBarAdapter.Div(true))
        }
        sideBarRows.add(SideBarAdapter.SettingsRow(getString(R.string.rate_us), getString(R.string.rate_us_blerb_template, getString(R.string.app_name)), object : View.OnClickListener {
            override fun onClick(view: View) {
                val intent = Intent(Intent.ACTION_VIEW)
                if (BuildConfig.IS_PRO) {
                    intent.setData(Uri.parse("market://details?id=com.hotpodata.redchain.pro"))
                } else {
                    intent.setData(Uri.parse("market://details?id=com.hotpodata.redchain.free"))
                }
                startActivity(intent)
                try {
                    AnalyticsMaster.getTracker(this@ChainActivity)?.send(HitBuilders.EventBuilder()
                            .setCategory(AnalyticsMaster.CATEGORY_ACTION)
                            .setAction(AnalyticsMaster.ACTION_RATE_APP)
                            .build());
                } catch(ex: Exception) {
                    Timber.e(ex, "Analytics Exception");
                }
            }
        }, R.drawable.ic_action_rate))
        sideBarRows.add(SideBarAdapter.Div(true))
        sideBarRows.add(SideBarAdapter.SettingsRow(getString(R.string.contact_the_developer), getString(R.string.contact_email_addr_template, getString(R.string.app_name)), object : View.OnClickListener {
            override fun onClick(view: View) {
                val intent = Intent(Intent.ACTION_SEND)
                intent.setType("*/*")
                intent.putExtra(Intent.EXTRA_EMAIL, getString(R.string.contact_email_addr_template, getString(R.string.app_name)))
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                }
                try {
                    AnalyticsMaster.getTracker(this@ChainActivity)?.send(HitBuilders.EventBuilder()
                            .setCategory(AnalyticsMaster.CATEGORY_ACTION)
                            .setAction(AnalyticsMaster.ACTION_CONTACT)
                            .build());
                } catch(ex: Exception) {
                    Timber.e(ex, "Analytics Exception");
                }
            }
        }, R.drawable.ic_action_mail))
        sideBarRows.add(SideBarAdapter.Div(true))
        sideBarRows.add(SideBarAdapter.SettingsRow(getString(R.string.visit_website), getString(R.string.visit_website_blurb), object : View.OnClickListener {
            override fun onClick(view: View) {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setData(Uri.parse("http://www.hotpodata.com"))
                startActivity(intent)
                try {
                    AnalyticsMaster.getTracker(this@ChainActivity)?.send(HitBuilders.EventBuilder()
                            .setCategory(AnalyticsMaster.CATEGORY_ACTION)
                            .setAction(AnalyticsMaster.ACTION_WEBSITE)
                            .build());
                } catch(ex: Exception) {
                    Timber.e(ex, "Analytics Exception");
                }
            }
        }, R.drawable.ic_action_web))


        sideBarRows.add(SideBarAdapter.Div(false))
        sideBarRows.add(getString(R.string.notifications_heading))
        sideBarRows.add(SideBarAdapter.SettingsRow(getString(R.string.daily_reminder), if (NotificationMaster.showReminderEnabled()) getString(R.string.enabled) else getString(R.string.disabled),
                View.OnClickListener { view ->
                    NotificationMaster.setShowReminder(!NotificationMaster.showReminderEnabled())
                    refreshSideBar()
                    try {
                        AnalyticsMaster.getTracker(this@ChainActivity)?.send(HitBuilders.EventBuilder()
                                .setCategory(AnalyticsMaster.CATEGORY_ACTION)
                                .setAction(AnalyticsMaster.ACTION_TOGGLE_REMINDER_NOTIFICATION)
                                .build());
                    } catch(ex: Exception) {
                        Timber.e(ex, "Analytics Exception");
                    }
                }, R.drawable.ic_action_reminder))
        sideBarRows.add(SideBarAdapter.Div(true))
        sideBarRows.add(SideBarAdapter.SettingsRow(getString(R.string.broken_chain), if (NotificationMaster.showBrokenEnabled()) getString(R.string.enabled) else getString(R.string.disabled),
                View.OnClickListener { view ->
                    NotificationMaster.setShowBroken(!NotificationMaster.showBrokenEnabled())
                    refreshSideBar()
                    try {
                        AnalyticsMaster.getTracker(this@ChainActivity)?.send(HitBuilders.EventBuilder()
                                .setCategory(AnalyticsMaster.CATEGORY_ACTION)
                                .setAction(AnalyticsMaster.ACTION_TOGGLE_BROKEN_NOTIFICATION)
                                .build());
                    } catch(ex: Exception) {
                        Timber.e(ex, "Analytics Exception");
                    }
                }, R.drawable.ic_action_broken))


        sideBarRows.add(SideBarAdapter.Div(false))
        sideBarRows.add(getString(R.string.acknowledgements))
        sideBarRows.add(SideBarAdapter.SettingsRow(getString(R.string.timber), getString(R.string.timber_license), object : View.OnClickListener {
            override fun onClick(view: View) {
                val i = Intent(Intent.ACTION_VIEW)
                i.setData(Uri.parse(getString(R.string.timber_url)))
                startActivity(i)
            }
        }))
        sideBarRows.add(SideBarAdapter.SettingsRow(getString(R.string.joda), getString(R.string.joda_license), object : View.OnClickListener {
            override fun onClick(view: View) {
                val i = Intent(Intent.ACTION_VIEW)
                i.setData(Uri.parse(getString(R.string.joda_url)))
                startActivity(i)
            }
        }))

        sideBarRows.add(SideBarAdapter.Div(false))
        sideBarRows.add(SideBarAdapter.SettingsRow(getString(R.string.legal_heading), getString(R.string.legal_blurb), object : View.OnClickListener {
            override fun onClick(view: View) {
            }
        }))


        if (sideBarAdapter == null) {
            sideBarAdapter = SideBarAdapter(this, sideBarRows);
            sideBarAdapter?.setAccentColor(chain.color)
            leftDrawerRecyclerView?.adapter = sideBarAdapter
            leftDrawerRecyclerView?.layoutManager = LinearLayoutManager(this)
        } else {
            sideBarAdapter?.setAccentColor(chain.color)
            sideBarAdapter?.setRows(sideBarRows)
        }

    }

    public override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle?.syncState();
    }

    public override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig);
        drawerToggle?.onConfigurationChanged(newConfig);
    }

    override fun onChainUpdated(chain: Chain) {
        ChainMaster.saveChain(chain)

        //TODO: DO THIS SOMEPLAE SMARTER
        if (chain.chainLength == 0) {
            NotificationMaster.dismissBrokenNotification(chain.id)
            NotificationMaster.dismissReminderNotification(chain.id)
        }
        if (chain.chainContainsToday()) {
            NotificationMaster.dismissBrokenNotification(chain.id)
            NotificationMaster.dismissReminderNotification(chain.id)
            NotificationMaster.scheduleReminderNotification(chain.id)
            NotificationMaster.scheduleBrokenNotification(chain.id)
        }

        //We just refresh sidebar here because the chain object in the adapter is already updated
        refreshSideBar()
        supportInvalidateOptionsMenu()
    }


    fun showGoPro() {
        var gopro = GoProChainFragment()
        gopro.show(supportFragmentManager, FTAG_GO_PRO)
    }


}