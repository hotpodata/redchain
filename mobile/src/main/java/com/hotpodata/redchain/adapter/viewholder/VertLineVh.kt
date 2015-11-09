package com.hotpodata.redchain.adapter.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import com.hotpodata.redchain.R
import com.hotpodata.redchain.view.WeekChainView

/**
 * Created by jdrotos on 9/17/15.
 */
class VertLineVh(v: View?) : RecyclerView.ViewHolder(v) {
    val vertLine : View

    init{
        vertLine = itemView.findViewById(R.id.vert_line);
    }
}