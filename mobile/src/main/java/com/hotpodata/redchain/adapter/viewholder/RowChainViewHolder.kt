package com.hotpodata.redchain.adapter.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView

import com.hotpodata.redchain.view.CircleImageView

/**
 * Created by jdrotos on 1/21/15.
 */
class RowChainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var mTextOne: TextView
    var mTextTwo: TextView
    var mIcon: CircleImageView

    init {
        mTextOne = itemView.findViewById(android.R.id.text1) as TextView
        mTextTwo = itemView.findViewById(android.R.id.text2) as TextView
        mIcon = itemView.findViewById(android.R.id.icon) as CircleImageView
    }
}
