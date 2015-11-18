package com.hotpodata.redchain.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hotpodata.redchain.R;


/**
 * Created by jdrotos on 3/21/15.
 */
public class SideBarHeaderViewHolder extends RecyclerView.ViewHolder {
    public TextView mTitleTv;
    public TextView mSubTitleTv;
    public ImageView mIcon;
    public ViewGroup mContainer;

    public SideBarHeaderViewHolder(View itemView) {
        super(itemView);
        mTitleTv = (TextView) itemView.findViewById(android.R.id.text1);
        mSubTitleTv = (TextView) itemView.findViewById(android.R.id.text2);
        mIcon = (ImageView) itemView.findViewById(R.id.chain_icon);
        mContainer = (ViewGroup) itemView.findViewById(R.id.container);
    }
}
