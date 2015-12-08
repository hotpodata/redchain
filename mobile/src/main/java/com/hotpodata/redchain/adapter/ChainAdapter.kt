package com.hotpodata.redchain.adapter

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.provider.Settings
import android.support.v7.widget.RecyclerView
import android.transition.AutoTransition
import android.transition.Scene
import android.transition.TransitionManager
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.hotpodata.redchain.BuildConfig
import com.hotpodata.redchain.ChainMaster
import com.hotpodata.redchain.R
import com.hotpodata.redchain.adapter.viewholder.*
import com.hotpodata.redchain.data.Chain
import com.hotpodata.redchain.interfaces.ChainUpdateListener
import org.joda.time.Days
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import timber.log.Timber
import java.security.MessageDigest
import java.util.*

/**
 * Created by jdrotos on 9/17/15.
 */
public class ChainAdapter(context: Context, argChain: Chain) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val ctx = context
    val CHAIN_LINK = 0;
    val VERTICAL_LINE = 1;
    val CHAIN_TODAY = 2;
    val CHAIN_FIRST_DAY_MESSAGE = 3;
    val CHAIN_AD = 4

    var chain: Chain
    var rows = ArrayList<Row>()

    val dtformat1 = DateTimeFormat.forPattern("EEEE")
    val dtformat2 = DateTimeFormat.shortDate()
    val dtformat3 = DateTimeFormat.shortTime()

    val interpo: Interpolator;

    val rowMaxTitleSize: Int
    val rowMinTitleSize: Int
    val rowMaxLineSize: Int
    val rowMinLineSize: Int

    var chainUpdateListener: ChainUpdateListener? = null

    init {
        chain = argChain

        rowMaxTitleSize = context.resources.getDimensionPixelSize(R.dimen.row_title_max)
        rowMinTitleSize = context.resources.getDimensionPixelSize(R.dimen.row_title_min)
        rowMaxLineSize = context.resources.getDimensionPixelSize(R.dimen.row_vert_line_max_height)
        rowMinLineSize = context.resources.getDimensionPixelSize(R.dimen.row_vert_line_min_height)
        interpo = DecelerateInterpolator(10f);
        buildRows()
    }

    private fun goToScene(vg: ViewGroup, layoutResId: Int) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            var scene = Scene.getSceneForLayout(vg, layoutResId, ctx)
            TransitionManager.go(scene, AutoTransition());
        } else {
            vg.removeAllViews()
            LayoutInflater.from(ctx).inflate(layoutResId, vg, true)
        }
    }

    @Suppress("DEPRECATION")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        if (holder is ChainTodayWithStatsVh) {
            if (chain.chainContainsToday()) {
                if (holder.statsContainer == null) {
                    goToScene(holder.sceneRoot, R.layout.include_row_chain_today_with_stats_checked)
                    holder.rebindViews()
                }

                holder.xview.setOnClickListener(null)
                holder.xview.isClickable = false
                holder.xview.boxToXPercentage = 1f
                holder.xview.setColors(chain.color, ctx.resources.getColor(R.color.material_grey))
                holder.timeTv?.text = chain.newestDate?.toString(dtformat3)
                holder.currentDayCountTv?.text = "" + chain.chainLength
                holder.currentDayLabelTv?.text = ctx.resources.getQuantityString(R.plurals.days_and_counting, chain.chainLength)
                holder.bestInChainCountTv?.text = "" + chain.longestRun
                holder.bestAllChainsCountTv?.text = "" + ChainMaster.getLongestRunOfAllChains()
            } else {
                if (holder.motivationBlurbTv == null) {
                    goToScene(holder.sceneRoot, R.layout.include_row_chain_today_with_stats_unchecked)
                    holder.rebindViews()
                }
                holder.xview.isClickable = true
                holder.xview.setColors(chain.color, ctx.resources.getColor(R.color.material_grey))
                holder.xview.boxToXPercentage = 0f
                holder.xview.setOnClickListener {
                    //update data
                    chain.addNowToChain();
                    chainUpdateListener?.onChainUpdated(chain)

                    //start anim
                    var start = if (holder.xview.boxToXPercentage == 1f) 1f else 0f;
                    var end = if (holder.xview.boxToXPercentage == 1f) 0f else 1f;

                    holder.xview.boxToXPercentage = start;
                    var animator = ValueAnimator.ofFloat(start, end)
                    animator.addUpdateListener { anim ->
                        holder.xview.boxToXPercentage = anim.animatedFraction
                    }
                    animator.addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            buildRows()
                        }
                    })
                    animator.interpolator = AccelerateDecelerateInterpolator()
                    animator.setDuration(700)
                    animator.start()
                }
            }
        }

        if (holder is ChainLinkVh) {
            var titleHeight = Math.max(rowMinTitleSize, rowMaxTitleSize - position).toFloat()//rowMinTitleSize + (floatDepth * (rowMaxTitleSize - rowMinTitleSize))

            var data = rows[position] as RowChainLink
            //var headingTypeface = rowHeadingTypeface

            holder.itemView.setOnClickListener(null)
            holder.xview.setBox(false)
            holder.xview.setColors(chain.color, ctx.resources.getColor(R.color.material_grey))

            if (holder.tv1.textSize != titleHeight) {
                holder.tv1.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleHeight)
            }

            val dateStr = if (data.dateTime.toLocalDate().plusDays(1).isEqual(LocalDate.now())) {
                ctx.getText(R.string.yesterday)
            } else if (Days.daysBetween(data.dateTime.toLocalDate(), LocalDate.now()).days < 7) {
                data.dateTime.toString(dtformat1)
            } else {
                data.dateTime.toString(dtformat2)
            }

            val timeStr = data.dateTime.toString(dtformat3)

            val depth = chain.chainLength - chain.chainDepth(data.dateTime)
            holder.tv1.text = "" + depth
            holder.tv2.text = "$dateStr\n$timeStr";
        }
        if (holder is VertLineVh) {
            var lineHeight = Math.max(rowMinLineSize, rowMaxLineSize - position).toFloat()
            var data = rows.get(position) as RowChainLine
            if (holder.vertLine.layoutParams != null && holder.vertLine.layoutParams.height != lineHeight.toInt()) {
                holder.vertLine.layoutParams.height = lineHeight.toInt()
                holder.vertLine.layoutParams = holder.vertLine.layoutParams
            }
            holder.vertLine.setBackgroundColor(chain.color)
            holder.vertLine.visibility = if (data.invisible) View.INVISIBLE else View.VISIBLE;
        }
        if (holder is RowFirstDayMessageVh) {
            //nothing to do
        }
        if (holder is ChainAdVh) {
            requestAd(holder.adview)
        }
    }

    override fun getItemCount(): Int {
        return rows.size;
    }

    override fun getItemViewType(position: Int): Int {
        if (rows[position] is RowChainLine) {
            return VERTICAL_LINE;
        } else if (rows.get(position) is RowChainLink) {
            return CHAIN_LINK;
        } else if (rows.get(position) is RowChainToday) {
            return CHAIN_TODAY;
        } else if (rows.get(position) is RowFirstDayMessage) {
            return CHAIN_FIRST_DAY_MESSAGE
        } else if (rows.get(position) is RowChainAd) {
            return CHAIN_AD
        }
        return -1;
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder? {
        var inflater = LayoutInflater.from(parent?.context);

        if (viewType == CHAIN_LINK) {
            var chainView: View? = inflater.inflate(R.layout.card_chain_link_center, parent, false);
            var vh = ChainLinkVh(chainView);
            return vh;
        }
        if (viewType == VERTICAL_LINE) {
            var lineView = inflater.inflate(R.layout.row_vertical_line_center, parent, false);
            return VertLineVh(lineView);
        }
        if (viewType == CHAIN_TODAY) {
            var view = inflater.inflate(R.layout.row_chain_today_with_stats, parent, false)
            var vh = ChainTodayWithStatsVh(view);
            return vh;
        }
        if (viewType == CHAIN_FIRST_DAY_MESSAGE) {
            var messageView = inflater.inflate(R.layout.row_first_day_message, parent, false)
            var vh = RowFirstDayMessageVh(messageView)
            return vh
        }
        if (viewType == CHAIN_AD) {
            var ad = inflater.inflate(R.layout.card_chain_ad, parent, false)
            var vh = ChainAdVh(ad)
            return vh;
        }

        return null;
    }

    public fun updateChain(chn: Chain) {
        chain = chn
        buildRows()

        //NOTE: buildRows() makes some assumptions, when we set a new chain, we should update all rows
        notifyDataSetChanged()
    }

    public fun buildRows() {
        var freshRows = ArrayList<Row>()
        if (chain.chainLength > 0) {
            var dateTimes = ArrayList<LocalDateTime>(chain.dateTimes);
            for (dt in dateTimes) {

                //TODAY has it's own special thing going on
                if (dt.toLocalDate().isBefore(LocalDate.now())) {
                    if (freshRows.size > 0) {
                        freshRows.add(RowChainLine(false))
                    }
                    freshRows.add(RowChainLink(dt))
                }

            }
            if (freshRows.size > 0) {
                freshRows.add(RowChainLine(true))
            }
        }

        if (freshRows.size > 0) {
            freshRows.add(0, RowChainLine(false))
        }
        freshRows.add(0, RowChainToday());
        if (freshRows.size == 1 && chain.chainContainsToday()) {
            freshRows.add(RowFirstDayMessage())
        }
        if (!BuildConfig.IS_PRO) {
            if (chain.chainLength < chain.longestRun) {
                Timber.d("Adding RowChainAd()")
                freshRows.add(1, RowChainLine(false))
                freshRows.add(2, RowChainAd())
            }
        } else {
            Timber.d("Skipping RowChainAd()")
        }

        rows = freshRows

        //TODO: Be smarter about this to get free animations.
        notifyDataSetChanged()
    }

    open class Row() {

    }

    class RowChainLink(dt: LocalDateTime) : Row() {
        val dateTime = dt
    }

    class RowChainLine(invis: Boolean) : Row() {
        val invisible = invis
    }

    class RowChainToday() : Row() {

    }

    class RowFirstDayMessage() : Row() {

    }

    class RowChainAd() : Row() {

    }


    /*
   ADD STUFF
    */

    private fun requestAd(adview: AdView?) {
        if (!BuildConfig.IS_PRO) {
            var adRequest = with(AdRequest.Builder()) {
                if (BuildConfig.IS_DEBUG_BUILD) {
                    addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    var andId = Settings.Secure.getString(ctx.contentResolver, Settings.Secure.ANDROID_ID)
                    var hash = md5(andId).toUpperCase()
                    Timber.d("Adding test device. hash:" + hash)
                    addTestDevice(hash)
                }
                build()
            }
            adview?.loadAd(adRequest);
        }
    }

    private fun md5(s: String): String {
        try {
            var digest = MessageDigest.getInstance("MD5")
            digest.update(s.toByteArray())
            var messageDigest = digest.digest()

            var hexString = StringBuffer()
            for (i in messageDigest.indices) {
                var h = Integer.toHexString(0xFF and messageDigest[i].toInt())
                while (h.length < 2)
                    h = "0" + h
                hexString.append(h)
            }
            return hexString.toString()
        } catch(ex: Exception) {
            Timber.e(ex, "Fail in md5");
        }
        return ""
    }
}