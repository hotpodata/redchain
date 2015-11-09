package com.hotpodata.redchain

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
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
import android.view.View
import com.google.android.gms.analytics.HitBuilders
import com.hotpodata.redchain.adapter.ChainAdapter
import com.hotpodata.redchain.adapter.SideBarAdapter
import com.hotpodata.redchain.data.Chain
import com.hotpodata.redchain.interfaces.ChainUpdateListener
import org.joda.time.LocalDateTime
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.util.*

/**
 * Created by jdrotos on 9/16/15.
 */
public class ChainActivity : ChainUpdateListener, AppCompatActivity() {

    var appBarLayout: AppBarLayout? = null
    var toolBar: Toolbar? = null
    var recyclerView: RecyclerView? = null
    var drawerLayout: DrawerLayout? = null
    var drawerToggle: ActionBarDrawerToggle? = null
    var leftDrawerRecyclerView: RecyclerView? = null

    var chainAdapter: ChainAdapter? = null
    var sideBarAdapter: SideBarAdapter? = null


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

        refreshChain()
        refreshSideBar()
    }

    public override fun onResume() {
        super.onResume()
        refreshChain()
    }

    public fun refreshChain() {
        var chain = ChainMaster.getCurrentChain()
        if(chainAdapter == null) {
            chainAdapter = ChainAdapter(this, chain)
            chainAdapter?.chainUpdateListener = this
            recyclerView?.setAdapter(chainAdapter)
            recyclerView?.setLayoutManager(LinearLayoutManager(this))
        }else{
            chainAdapter?.updateChain(chain)
        }
        getSupportActionBar().setTitle(chain.title)
    }

    public fun refreshSideBar() {
        var sideBarRows = ArrayList<Any>()
        sideBarRows.add(SideBarAdapter.SideBarHeading())
        sideBarRows.add(getString(R.string.actions))
        sideBarRows.add(SideBarAdapter.SettingsRow(getString(R.string.reset_chain), getString(R.string.reset_chain_blurb),
                View.OnClickListener { view ->
                    var builder = AlertDialog.Builder(this)
                    builder.setMessage(R.string.reset_chain_confirm)
                    builder.setCancelable(true)
                    builder.setPositiveButton(R.string.reset,
                            DialogInterface.OnClickListener {
                                dialogInterface, i ->
                                ChainMaster.resetChain()
                                refreshChain()
                                drawerLayout?.closeDrawers()
                            })
                    builder.setNegativeButton(R.string.cancel, DialogInterface.OnClickListener { dialogInterface, i -> dialogInterface.cancel() })
                    builder.create().show()
                }, R.drawable.ic_action_reset))
        sideBarRows.add(SideBarAdapter.Div(true))
        sideBarRows.add(SideBarAdapter.SettingsRow(getString(R.string.rate_us), getString(R.string.rate_us_blerb_template, getString(R.string.app_name)), object : View.OnClickListener {
            override fun onClick(view: View) {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setData(Uri.parse("market://details?id=com.hotpodata.redchain"))
                startActivity(intent)
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
            }
        }, R.drawable.ic_action_mail))

        sideBarRows.add(SideBarAdapter.Div(false))
        sideBarRows.add(getString(R.string.notifications_heading))
        sideBarRows.add(SideBarAdapter.SettingsRow(getString(R.string.daily_reminder), if (NotificationMaster.showReminderEnabled()) getString(R.string.enabled) else getString(R.string.disabled),
                View.OnClickListener { view ->
                    NotificationMaster.setShowReminder(!NotificationMaster.showReminderEnabled())
                    refreshSideBar()
                }, R.drawable.ic_action_reminder))
        sideBarRows.add(SideBarAdapter.Div(true))
        sideBarRows.add(SideBarAdapter.SettingsRow(getString(R.string.broken_chain), if (NotificationMaster.showBrokenEnabled()) getString(R.string.enabled) else getString(R.string.disabled),
                View.OnClickListener { view ->
                    NotificationMaster.setShowBroken(!NotificationMaster.showBrokenEnabled())
                    refreshSideBar()
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
            sideBarAdapter = SideBarAdapter(sideBarRows);
            leftDrawerRecyclerView?.adapter = sideBarAdapter
            leftDrawerRecyclerView?.layoutManager = LinearLayoutManager(this)
        } else {
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
    }


}