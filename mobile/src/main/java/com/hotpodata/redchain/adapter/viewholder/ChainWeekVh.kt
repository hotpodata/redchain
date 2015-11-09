package com.hotpodata.redchain.adapter.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import com.hotpodata.redchain.R
import com.hotpodata.redchain.view.WeekChainView

/**
 * Created by jdrotos on 9/17/15.
 */
class ChainWeekVh(v: View?) : RecyclerView.ViewHolder(v) {
    fun getChainView(): WeekChainView {
        return itemView.findViewById(R.id.weekchainrow) as WeekChainView;
    }
}