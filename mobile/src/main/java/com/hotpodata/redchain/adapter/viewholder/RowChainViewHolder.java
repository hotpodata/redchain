package com.hotpodata.redchain.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hotpodata.redchain.view.CircleImageView;

/**
 * Created by jdrotos on 1/21/15.
 */
public class RowChainViewHolder extends RecyclerView.ViewHolder {
    public TextView mTextOne;
    public TextView mTextTwo;
    public CircleImageView mIcon;

    public RowChainViewHolder(View itemView) {
        super(itemView);
        mTextOne = (TextView) itemView.findViewById(android.R.id.text1);
        mTextTwo = (TextView) itemView.findViewById(android.R.id.text2);
        mIcon = (CircleImageView) itemView.findViewById(android.R.id.icon);
    }
}
