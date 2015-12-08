package com.hotpodata.redchain.adapter.viewholder

import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.hotpodata.redchain.R
import com.hotpodata.redchain.view.XView

/**
 * Created by jdrotos on 9/17/15.
 */
class ChainTodayWithStatsVh(val v: View) : RecyclerView.ViewHolder(v) {
    var cardview: CardView
    var sceneRoot: ViewGroup
    var contentContainer: ViewGroup
    var xview: XView
    var todayTitleTv: TextView

    //Unchecked mode
    var motivationBlurbTv: TextView?

    //Checked mode
    var timeTv: TextView?
    var statsContainer: ViewGroup?
    var currentDayCountTv: TextView?
    var currentDayLabelTv: TextView?
    var bestInChainCountTv: TextView?
    var bestAllChainsCountTv: TextView?

    init {
        cardview = v.findViewById(R.id.cardview) as CardView
        sceneRoot = v.findViewById(R.id.scene_root) as ViewGroup
        contentContainer = v.findViewById(R.id.content_container) as ViewGroup
        xview = v.findViewById(R.id.xview) as XView
        todayTitleTv = v.findViewById(R.id.today_title_tv) as TextView

        motivationBlurbTv = v.findViewById(R.id.motivation_blurb) as TextView?

        statsContainer = v.findViewById(R.id.stats_container) as ViewGroup?
        timeTv = v.findViewById(R.id.time) as TextView?
        currentDayCountTv = v.findViewById(R.id.current_day_count_tv) as TextView?
        currentDayLabelTv = v.findViewById(R.id.current_day_count_label_tv) as TextView?
        bestInChainCountTv = v.findViewById(R.id.best_in_chain_tv) as TextView?
        bestAllChainsCountTv = v.findViewById(R.id.all_chains_best_count_tv) as TextView?

    }

    fun rebindViews(){
        cardview = v.findViewById(R.id.cardview) as CardView
        sceneRoot = v.findViewById(R.id.scene_root) as ViewGroup
        contentContainer = v.findViewById(R.id.content_container) as ViewGroup
        xview = v.findViewById(R.id.xview) as XView
        todayTitleTv = v.findViewById(R.id.today_title_tv) as TextView

        motivationBlurbTv = v.findViewById(R.id.motivation_blurb) as TextView?

        statsContainer = v.findViewById(R.id.stats_container) as ViewGroup?
        timeTv = v.findViewById(R.id.time) as TextView?
        currentDayCountTv = v.findViewById(R.id.current_day_count_tv) as TextView?
        currentDayLabelTv = v.findViewById(R.id.current_day_count_label_tv) as TextView?
        bestInChainCountTv = v.findViewById(R.id.best_in_chain_tv) as TextView?
        bestAllChainsCountTv = v.findViewById(R.id.all_chains_best_count_tv) as TextView?
    }
}