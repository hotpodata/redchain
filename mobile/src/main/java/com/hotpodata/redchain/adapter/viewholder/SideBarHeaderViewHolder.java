package com.hotpodata.redchain.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;


/**
 * Created by jdrotos on 3/21/15.
 */
public class SideBarHeaderViewHolder extends RecyclerView.ViewHolder {
    public TextView mTitleTv;

    public SideBarHeaderViewHolder(View itemView) {
        super(itemView);
        mTitleTv = (TextView) itemView.findViewById(android.R.id.text1);
    }
}
