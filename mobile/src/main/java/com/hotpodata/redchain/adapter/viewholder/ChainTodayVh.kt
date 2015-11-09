package com.hotpodata.redchain.adapter.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.hotpodata.redchain.R
import com.hotpodata.redchain.view.WeekChainView
import com.hotpodata.redchain.view.XView

/**
 * Created by jdrotos on 9/17/15.
 */
class ChainTodayVh(v: View?) : RecyclerView.ViewHolder(v) {
    val xview: XView
    val tv1: TextView
    val tv2: TextView
    val tv3: TextView
    val tv4: TextView
    val afterChecked: ViewGroup

    init {
        xview = v?.findViewById(R.id.xview) as XView;
        tv1 = v?.findViewById(R.id.tv_1) as TextView;
        tv2 = v?.findViewById(R.id.tv_2) as TextView;
        tv3 = v?.findViewById(R.id.tv_3) as TextView;
        tv4 = v?.findViewById(R.id.tv_4) as TextView;
        afterChecked = v?.findViewById(R.id.after_checked_container) as ViewGroup;
    }
}