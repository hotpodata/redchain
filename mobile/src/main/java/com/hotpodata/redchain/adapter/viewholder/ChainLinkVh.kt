package com.hotpodata.redchain.adapter.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.hotpodata.redchain.R
import com.hotpodata.redchain.view.XView

/**
 * Created by jdrotos on 9/17/15.
 */
class ChainLinkVh(v: View?) : RecyclerView.ViewHolder(v) {
    val xview: XView
    val tv1: TextView
    val tv2: TextView

    init {
        xview = v?.findViewById(R.id.xview) as XView;
        tv1 = v?.findViewById(R.id.tv_1) as TextView;
        tv2 = v?.findViewById(R.id.tv_2) as TextView;
    }
}