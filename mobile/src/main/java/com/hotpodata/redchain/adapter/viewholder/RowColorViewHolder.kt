package com.hotpodata.redchain.adapter.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View

import com.hotpodata.redchain.view.CircleImageView

/**
 * Created by jdrotos on 1/21/15.
 */
class RowColorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var mIcon: CircleImageView

    init {
        mIcon = itemView.findViewById(android.R.id.icon) as CircleImageView
    }
}
