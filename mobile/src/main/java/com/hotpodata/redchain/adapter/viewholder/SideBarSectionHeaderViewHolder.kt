package com.hotpodata.redchain.adapter.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView


/**
 * Created by jdrotos on 3/21/15.
 */
class SideBarSectionHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var mTitleTv: TextView

    init {
        mTitleTv = itemView.findViewById(android.R.id.text1) as TextView
    }
}
