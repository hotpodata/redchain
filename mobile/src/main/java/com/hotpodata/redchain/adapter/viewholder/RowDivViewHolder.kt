package com.hotpodata.redchain.adapter.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View

import com.hotpodata.redchain.R


/**
 * Created by jdrotos on 1/21/15.
 */
class RowDivViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var mSpacer: View
    var mDivBar: View

    init {
        mSpacer = itemView.findViewById(R.id.spacer)
        mDivBar = itemView.findViewById(R.id.divbar)
    }
}
