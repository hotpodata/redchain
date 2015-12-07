package com.hotpodata.redchain.adapter.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import com.hotpodata.redchain.R


/**
 * Created by jdrotos on 3/21/15.
 */
class SideBarHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var mTitleTv: TextView
    var mSubTitleTv: TextView
    var mIcon: ImageView
    var mContainer: ViewGroup

    init {
        mTitleTv = itemView.findViewById(android.R.id.text1) as TextView
        mSubTitleTv = itemView.findViewById(android.R.id.text2) as TextView
        mIcon = itemView.findViewById(R.id.chain_icon) as ImageView
        mContainer = itemView.findViewById(R.id.container) as ViewGroup
    }
}
