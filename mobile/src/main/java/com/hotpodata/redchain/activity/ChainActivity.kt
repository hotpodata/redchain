package com.hotpodata.redchain.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.*
import android.support.design.widget.AppBarLayout
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.android.gms.analytics.HitBuilders
import com.hotpodata.redchain.*
import com.hotpodata.redchain.adapter.ChainAdapter
import com.hotpodata.redchain.adapter.SideBarAdapter
import com.hotpodata.redchain.data.Chain
import com.hotpodata.redchain.interfaces.ChainUpdateListener
import com.hotpodata.redchain.service.FreeVersionMigrationService
import com.hotpodata.redchain.utils.IntentUtils
import timber.log.Timber
import java.util.*

/**
 * Created by jdrotos on 9/16/15.
 */
public class ChainActivity : ChainUpdateListener, ChameleonActivity() {

    //Constants
    val FREE_VERSION_PACKAGE_NAME = "com.hotpodata.redchain.free"
    val PRO_VERSION_PACKAGE_NAME = "com.hotpodata.redchain.pro"
    val MIGRATION_SERVICE_NAME = "com.hotpodata.redchain.service.FreeVersionMigrationService"
    val FTAG_GO_PRO = "GO_PRO"
    val PREFS_CHAIN_ACTIVITY = "PREFS_CHAIN_ACTIVITY"
    val PREF_KEY_HAS_MIGRATED = "PREF_KEY_HAS_MIGRATED"
    val PREF_KEY_LAUNCH_COUNT = "PREF_KEY_LAUNCH_COUNT"

    //Migration and routing
    var messenger: Messenger
    var serviceConnection: ServiceConnection
    var isBound = false

    //Views
    var appBarLayout: AppBarLayout? = null
    var toolBar: Toolbar? = null
    var recyclerView: RecyclerView? = null
    var drawerLayout: DrawerLayout? = null
    var drawerToggle: ActionBarDrawerToggle? = null
    var leftDrawerRecyclerView: RecyclerView? = null

    //Adapters
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

    init {
        //Set up our data migration code
        messenger = genMigrateDataMessenger()
        serviceConnection = genMigrateServiceConnection()
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

        //Data migration from free version
        if (BuildConfig.IS_PRO && !hasMigratedData() && IntentUtils.isAppInstalled(this, FREE_VERSION_PACKAGE_NAME)) {
            //This is the pro version, and the free version is installed
            //Better do some data migration if applicable
            doBindService()
        }

        if (savedInstanceState == null) {
            incrementLaunchCount()
        }


        consumeIntent(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        doUnBindService()
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

        if (!BuildConfig.IS_PRO && IntentUtils.isAppInstalled(this, PRO_VERSION_PACKAGE_NAME)) {
            //This is the free version, and the pro version is installed
            //so we route to the pro version
            Toast.makeText(this, R.string.toast_routing_to_pro_version_msg, Toast.LENGTH_SHORT).show()
            var proIntent = packageManager.getLaunchIntentForPackage(PRO_VERSION_PACKAGE_NAME)
            startActivity(proIntent);
            finish();
            return
        }

        Timber.i("Setting screen name:" + AnalyticsMaster.SCREEN_CHAIN);
        AnalyticsMaster.getTracker(this).setScreenName(AnalyticsMaster.SCREEN_CHAIN);
        AnalyticsMaster.getTracker(this).send(HitBuilders.ScreenViewBuilder().build());

        ChainMaster.expireExpiredChains()
        refreshChain()
    }

    public override fun onCreateOptionsMenu(menu: Menu): Boolean {
        var inflater = menuInflater
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
                    AnalyticsMaster.getTracker(this).send(HitBuilders.EventBuilder()
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
                    AnalyticsMaster.getTracker(this).send(HitBuilders.EventBuilder()
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
                builder.setPositiveButton(R.string.reset) {
                    dialogInterface, i ->
                    var chain = ChainMaster.getSelectedChain()
                    chain.clearDates()
                    ChainMaster.saveChain(chain)
                    refreshChain()
                    drawerLayout?.closeDrawers()

                    try {
                        AnalyticsMaster.getTracker(this).send(HitBuilders.EventBuilder()
                                .setCategory(AnalyticsMaster.CATEGORY_ACTION)
                                .setAction(AnalyticsMaster.ACTION_RESET_CHAIN)
                                .build());
                    } catch(ex: Exception) {
                        Timber.e(ex, "Analytics Exception");
                    }


                }
                builder.setNegativeButton(R.string.cancel) { dialogInterface, i -> dialogInterface.cancel() }
                builder.create().show()
                return true;
            }
            R.id.delete_chain -> {
                var builder = AlertDialog.Builder(this)
                builder.setMessage(R.string.delete_chain_confirm)
                builder.setCancelable(true)
                builder.setPositiveButton(R.string.delete) {
                    dialogInterface, i ->
                    ChainMaster.deleteChain(ChainMaster.selectedChainId)
                    refreshChain()

                    try {
                        AnalyticsMaster.getTracker(this).send(HitBuilders.EventBuilder()
                                .setCategory(AnalyticsMaster.CATEGORY_ACTION)
                                .setAction(AnalyticsMaster.ACTION_DELETE_CHAIN)
                                .build());
                    } catch(ex: Exception) {
                        Timber.e(ex, "Analytics Exception");
                    }

                }
                builder.setNegativeButton(R.string.cancel) { dialogInterface, i -> dialogInterface.cancel() }
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
            recyclerView?.adapter = chainAdapter
            recyclerView?.layoutManager = LinearLayoutManager(this)
        } else {
            chainAdapter?.updateChain(chain)
        }
        supportActionBar.title = chain.title
        setColor(chain.color, true)
        refreshSideBar()
    }

    public fun refreshSideBar() {
        var selectedChain = ChainMaster.getSelectedChain()
        var sideBarRows = ArrayList<Any>()

        var version: String? = null
        try {
            val pInfo = packageManager.getPackageInfo(packageName, 0)
            version = getString(R.string.version_template, pInfo.versionName)
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.e(e, "Version fail")
        }

        sideBarRows.add(SideBarAdapter.SideBarHeading(getString(R.string.app_label), version))
        sideBarRows.add(getString(R.string.chains))
        for (chain in ChainMaster.allChains.values) {
            sideBarRows.add(SideBarAdapter.RowChain(chain, chain.id == ChainMaster.selectedChainId, View.OnClickListener {
                var intent = IntentGenerator.generateIntent(this@ChainActivity, chain.id)
                startActivity(intent)
                try {
                    AnalyticsMaster.getTracker(this@ChainActivity).send(HitBuilders.EventBuilder()
                            .setCategory(AnalyticsMaster.CATEGORY_ACTION)
                            .setAction(AnalyticsMaster.ACTION_SELECT_CHAIN)
                            .build());
                } catch(ex: Exception) {
                    Timber.e(ex, "Analytics Exception");
                }
            }))
            sideBarRows.add(SideBarAdapter.Div(true))
        }
        sideBarRows.add(SideBarAdapter.RowCreateChain(getString(R.string.create_chain), "", View.OnClickListener {
            drawerLayout?.closeDrawers()
            var intent = ChainEditActivity.IntentGenerator.generateNewChainIntent(this@ChainActivity);
            startActivity(intent)
            try {
                AnalyticsMaster.getTracker(this@ChainActivity).send(HitBuilders.EventBuilder()
                        .setCategory(AnalyticsMaster.CATEGORY_ACTION)
                        .setAction(AnalyticsMaster.ACTION_NEW_CHAIN)
                        .build());
            } catch(ex: Exception) {
                Timber.e(ex, "Analytics Exception");
            }
        }, R.drawable.ic_action_new))

        sideBarRows.add(getString(R.string.actions))

        //GO PRO
        if (!BuildConfig.IS_PRO) {
            sideBarRows.add(SideBarAdapter.SettingsRow(getString(R.string.go_pro_action), getString(R.string.go_pro_create_edit_blurb, getString(R.string.app_name)), View.OnClickListener {
                var intent = IntentUtils.goPro(this@ChainActivity)
                startActivity(intent)
                try {
                    AnalyticsMaster.getTracker(this@ChainActivity).send(HitBuilders.EventBuilder()
                            .setCategory(AnalyticsMaster.CATEGORY_ACTION)
                            .setAction(AnalyticsMaster.ACTION_GO_PRO_SIDEBAR)
                            .build());
                } catch(ex: Exception) {
                    Timber.e(ex, "Analytics Exception");
                }
            }, R.drawable.ic_action_go_pro))
            sideBarRows.add(SideBarAdapter.Div(true))
        }
        //RATE US
        sideBarRows.add(SideBarAdapter.SettingsRow(getString(R.string.rate_us), getString(R.string.rate_us_blerb_template, getString(R.string.app_name)), View.OnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            if (BuildConfig.IS_PRO) {
                intent.setData(Uri.parse("market://details?id=com.hotpodata.redchain.pro"))
            } else {
                intent.setData(Uri.parse("market://details?id=com.hotpodata.redchain.free"))
            }
            startActivity(intent)
            try {
                AnalyticsMaster.getTracker(this@ChainActivity).send(HitBuilders.EventBuilder()
                        .setCategory(AnalyticsMaster.CATEGORY_ACTION)
                        .setAction(AnalyticsMaster.ACTION_RATE_APP)
                        .build());
            } catch(ex: Exception) {
                Timber.e(ex, "Analytics Exception");
            }
        }, R.drawable.ic_action_rate))

        //EMAIL
        sideBarRows.add(SideBarAdapter.Div(true))
        sideBarRows.add(SideBarAdapter.SettingsRow(getString(R.string.contact_the_developer), getString(R.string.contact_email_addr_template, getString(R.string.app_name)), View.OnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.setType("message/rfc822")
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.contact_email_addr_template, getString(R.string.app_name))))
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
            try {
                AnalyticsMaster.getTracker(this@ChainActivity).send(HitBuilders.EventBuilder()
                        .setCategory(AnalyticsMaster.CATEGORY_ACTION)
                        .setAction(AnalyticsMaster.ACTION_CONTACT)
                        .build());
            } catch(ex: Exception) {
                Timber.e(ex, "Analytics Exception");
            }
        }, R.drawable.ic_action_mail))

        //TWITTER
        sideBarRows.add(SideBarAdapter.Div(true))
        sideBarRows.add(SideBarAdapter.SettingsRow(getString(R.string.follow_us_on_twitter), getString(R.string.twitter_handle), View.OnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setData(Uri.parse(getString(R.string.twitter_url)))
            startActivity(intent)
            try {
                AnalyticsMaster.getTracker(this@ChainActivity).send(HitBuilders.EventBuilder()
                        .setCategory(AnalyticsMaster.CATEGORY_ACTION)
                        .setAction(AnalyticsMaster.ACTION_TWITTER)
                        .build());
            } catch(ex: Exception) {
                Timber.e(ex, "Analytics Exception");
            }
        }, R.drawable.ic_action_twitter))

        //GITHUB
        sideBarRows.add(SideBarAdapter.Div(true))
        sideBarRows.add(SideBarAdapter.SettingsRow(getString(R.string.fork_redchain_on_github), getString(R.string.github_url), View.OnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setData(Uri.parse(getString(R.string.github_url)))
            startActivity(intent)
            try {
                AnalyticsMaster.getTracker(this@ChainActivity).send(HitBuilders.EventBuilder()
                        .setCategory(AnalyticsMaster.CATEGORY_ACTION)
                        .setAction(AnalyticsMaster.ACTION_TWITTER)
                        .build());
            } catch(ex: Exception) {
                Timber.e(ex, "Analytics Exception");
            }
        }, R.drawable.ic_action_github))

        //WEBSITE
        sideBarRows.add(SideBarAdapter.Div(true))
        sideBarRows.add(SideBarAdapter.SettingsRow(getString(R.string.visit_website), getString(R.string.visit_website_blurb), View.OnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setData(Uri.parse(getString(R.string.website_url)))
            startActivity(intent)
            try {
                AnalyticsMaster.getTracker(this@ChainActivity).send(HitBuilders.EventBuilder()
                        .setCategory(AnalyticsMaster.CATEGORY_ACTION)
                        .setAction(AnalyticsMaster.ACTION_WEBSITE)
                        .build());
            } catch(ex: Exception) {
                Timber.e(ex, "Analytics Exception");
            }
        }, R.drawable.ic_action_web))


        sideBarRows.add(SideBarAdapter.Div(false))
        sideBarRows.add(getString(R.string.notifications_heading))
        sideBarRows.add(SideBarAdapter.SettingsRow(getString(R.string.daily_reminder), if (NotificationMaster.showReminderEnabled()) getString(R.string.enabled) else getString(R.string.disabled),
                View.OnClickListener { view ->
                    NotificationMaster.setShowReminder(!NotificationMaster.showReminderEnabled())
                    refreshSideBar()
                    try {
                        AnalyticsMaster.getTracker(this@ChainActivity).send(HitBuilders.EventBuilder()
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
                        AnalyticsMaster.getTracker(this@ChainActivity).send(HitBuilders.EventBuilder()
                                .setCategory(AnalyticsMaster.CATEGORY_ACTION)
                                .setAction(AnalyticsMaster.ACTION_TOGGLE_BROKEN_NOTIFICATION)
                                .build());
                    } catch(ex: Exception) {
                        Timber.e(ex, "Analytics Exception");
                    }
                }, R.drawable.ic_action_broken))

        sideBarRows.add(SideBarAdapter.Div(false))
        sideBarRows.add(getString(R.string.apps))
        sideBarRows.add(SideBarAdapter.SettingsRow(getString(R.string.baconmasher), getString(R.string.baconmasher_desc), View.OnClickListener {
            try {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setData(Uri.parse("market://details?id=com.hotpodata.baconmasher.free"))
                startActivity(intent)
            } catch(ex: Exception) {
                Timber.e(ex, "Failure to launch market intent")
            }
            try {
                AnalyticsMaster.getTracker(this@ChainActivity).send(HitBuilders.EventBuilder()
                        .setCategory(AnalyticsMaster.CATEGORY_ACTION)
                        .setAction(AnalyticsMaster.ACTION_BACONMASHER)
                        .build());
            } catch(ex: Exception) {
                Timber.e(ex, "Analytics Exception");
            }
        }, R.mipmap.launcher_baconmasher))
        sideBarRows.add(SideBarAdapter.Div(true))
        sideBarRows.add(SideBarAdapter.SettingsRow(getString(R.string.filecat), getString(R.string.filecat_desc), View.OnClickListener {
            try {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setData(Uri.parse("market://details?id=com.hotpodata.filecat.free"))
                startActivity(intent)
            } catch(ex: Exception) {
                Timber.e(ex, "Failure to launch market intent")
            }
            try {
                AnalyticsMaster.getTracker(this@ChainActivity).send(HitBuilders.EventBuilder()
                        .setCategory(AnalyticsMaster.CATEGORY_ACTION)
                        .setAction(AnalyticsMaster.ACTION_FILECAT)
                        .build());
            } catch(ex: Exception) {
                Timber.e(ex, "Analytics Exception");
            }
        }, R.mipmap.launcher_filecat))
        sideBarRows.add(SideBarAdapter.Div(true))
        sideBarRows.add(SideBarAdapter.SettingsRow(getString(R.string.wikicat), getString(R.string.wikicat_desc), View.OnClickListener {
            try {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setData(Uri.parse("market://details?id=com.hotpodata.wikicat.free"))
                startActivity(intent)
            } catch(ex: Exception) {
                Timber.e(ex, "Failure to launch market intent")
            }
            try {
                AnalyticsMaster.getTracker(this@ChainActivity).send(HitBuilders.EventBuilder()
                        .setCategory(AnalyticsMaster.CATEGORY_ACTION)
                        .setAction(AnalyticsMaster.ACTION_WIKICAT)
                        .build());
            } catch(ex: Exception) {
                Timber.e(ex, "Analytics Exception");
            }
        }, R.mipmap.launcher_wikicat))

        sideBarRows.add(SideBarAdapter.Div(false))
        sideBarRows.add(getString(R.string.acknowledgements))
        sideBarRows.add(SideBarAdapter.SettingsRow(getString(R.string.timber), getString(R.string.timber_license), View.OnClickListener {
            val i = Intent(Intent.ACTION_VIEW)
            i.setData(Uri.parse(getString(R.string.timber_url)))
            startActivity(i)
        }))
        sideBarRows.add(SideBarAdapter.SettingsRow(getString(R.string.joda), getString(R.string.joda_license), View.OnClickListener {
            val i = Intent(Intent.ACTION_VIEW)
            i.setData(Uri.parse(getString(R.string.joda_url)))
            startActivity(i)
        }))

        sideBarRows.add(SideBarAdapter.Div(false))
        sideBarRows.add(SideBarAdapter.SettingsRow(getString(R.string.legal_heading), getString(R.string.legal_blurb), View.OnClickListener { }))


        if (sideBarAdapter == null) {
            sideBarAdapter = SideBarAdapter(this, sideBarRows);
            sideBarAdapter?.setAccentColor(selectedChain.color)
            leftDrawerRecyclerView?.adapter = sideBarAdapter
            leftDrawerRecyclerView?.layoutManager = LinearLayoutManager(this)
        } else {
            sideBarAdapter?.setAccentColor(selectedChain.color)
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


    /**
     * DATA MIGRATION STUFF
     */

    fun doBindService(): Boolean {
        try {
            Timber.d("doBindService")
            var component = ComponentName(FREE_VERSION_PACKAGE_NAME, MIGRATION_SERVICE_NAME)
            var intent = Intent()
            intent.setComponent(component)
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            isBound = true
        } catch(e: Exception) {
            Timber.e(e, "doBindService Fail")
        }
        return isBound
    }

    fun doUnBindService() {
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }

    fun genMigrateDataMessenger(): Messenger {
        return Messenger(object : Handler() {
            override public fun handleMessage(msg: Message) {
                Timber.d("Message returned from migration service. Code:" + msg.what)
                if (msg.what == FreeVersionMigrationService.Constants.MSG_FREE_VERSION_DATA) {
                    if (msg.data != null) {
                        Timber.d("Message contains a data bundle.")
                        var importedChain = ChainMaster.chainFromBundle(msg.data)
                        if (importedChain != null) {
                            Timber.d("Data bundle parsed. Imported chain aquired.")
                            //Get the current default chain
                            var currentDefaultChain = ChainMaster.getChain(ChainMaster.DEFAULT_CHAIN_ID)

                            Timber.d("Imported chain - name:" + importedChain.title + " id:" + importedChain.id)
                            Timber.d("Default chain - name:" + currentDefaultChain?.title + " id:" + currentDefaultChain?.id)

                            //We check if the current default chain has any real data
                            //if it doesn't we prepare it to be squashed, otherwise we import differently
                            if (currentDefaultChain != null && getLaunchCount() <= 1 && currentDefaultChain.chainLength <= 1) {
                                Timber.d("currentDefaultChain has limited data")
                                if (currentDefaultChain.chainContainsToday() && !importedChain.chainContainsToday()) {
                                    Timber.d("Adding now to imported chain")
                                    importedChain.addNowToChain()
                                }
                            } else {
                                Timber.d("Existing default chain has too much data. Fudging the imported chain.")
                                importedChain.id = UUID.randomUUID().toString()
                                importedChain.title = resources.getString(R.string.chain_free_version_title_template, importedChain.title)
                            }

                            //We now save (usually squash) the default chain
                            Timber.d("Saving imported chain...")
                            ChainMaster.saveChain(importedChain)
                            Timber.d("Imported chain saved! Updating selected chain...")
                            ChainMaster.setSelectedChain(importedChain.id)
                            Timber.d("Imported chain selected. Id:" + importedChain.id)

                            refreshChain()
                            Toast.makeText(this@ChainActivity, R.string.toast_data_migration, Toast.LENGTH_SHORT).show()
                        }
                        //Important! We don't want to migrate every time
                        setMigratedData(true)
                    }
                    doUnBindService()
                } else {
                    super.handleMessage(msg)
                }
            }
        })
    }

    fun genMigrateServiceConnection(): ServiceConnection {
        return object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName?) {
                Timber.d("onServiceDisconnected")
            }

            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                Timber.d("onServiceConnected")
                try {
                    var serviceMessenger = Messenger(service)
                    var msg = Message.obtain(null, FreeVersionMigrationService.Constants.MSG_REQUEST_FREE_VERSION_DATA)
                    msg.replyTo = messenger
                    serviceMessenger.send(msg)
                    Timber.d("requesting migration data")
                } catch(ex: java.lang.Exception) {
                    Timber.e(ex, "onServiceConnected")
                }
            }
        }
    }

    private fun hasMigratedData(): Boolean {
        var sharedPref = getSharedPreferences(PREFS_CHAIN_ACTIVITY, Context.MODE_PRIVATE);
        var hasMigrated = sharedPref.getBoolean(PREF_KEY_HAS_MIGRATED, false)
        Timber.d("hasMigratedData:" + hasMigrated)
        return hasMigrated
    }

    private fun setMigratedData(dataIsMigrated: Boolean) {
        Timber.d("setMigratedData:" + dataIsMigrated)
        var sharedPref = getSharedPreferences(PREFS_CHAIN_ACTIVITY, Context.MODE_PRIVATE);
        var editor = sharedPref.edit();
        editor.putBoolean(PREF_KEY_HAS_MIGRATED, dataIsMigrated)
        editor.commit();
    }

    private fun getLaunchCount(): Int {
        var sharedPref = getSharedPreferences(PREFS_CHAIN_ACTIVITY, Context.MODE_PRIVATE);
        var launchCount = sharedPref.getInt(PREF_KEY_LAUNCH_COUNT, 0)
        Timber.d("getLaunchCount:" + launchCount)
        return launchCount
    }

    private fun incrementLaunchCount() {
        Timber.d("incrementLaunchCount")
        var sharedPref = getSharedPreferences(PREFS_CHAIN_ACTIVITY, Context.MODE_PRIVATE);
        var editor = sharedPref.edit();
        editor.putInt(PREF_KEY_LAUNCH_COUNT, getLaunchCount() + 1)
        editor.commit();
    }

}