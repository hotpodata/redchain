package com.hotpodata.redchain.adapter.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.hotpodata.redchain.R

/**
 * Created by jdrotos on 11/23/15.
 */
class RowFirstDayMessageVh(v: View?) : RecyclerView.ViewHolder(v) {
    val tv1: TextView

    init {
        tv1 = v?.findViewById(android.R.id.text1) as TextView;
    }
}