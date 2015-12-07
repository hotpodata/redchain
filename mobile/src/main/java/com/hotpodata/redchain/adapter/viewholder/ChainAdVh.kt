package com.hotpodata.redchain.adapter.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.google.android.gms.ads.AdView
import com.hotpodata.redchain.R
import com.hotpodata.redchain.view.XView

/**
 * Created by jdrotos on 9/17/15.
 */
class ChainAdVh(v: View?) : RecyclerView.ViewHolder(v) {
    val adview: AdView

    init {
        adview = v?.findViewById(R.id.adview) as AdView;
    }
}