package com.hotpodata.redchain.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by jdrotos on 1/21/15.
 */
public class RowTextTwoLineViewHolder extends RecyclerView.ViewHolder {
    public TextView mTextOne;
    public TextView mTextTwo;
    public ImageView mIcon;

    public RowTextTwoLineViewHolder(View itemView) {
        super(itemView);
        mTextOne = (TextView) itemView.findViewById(android.R.id.text1);
        mTextTwo = (TextView) itemView.findViewById(android.R.id.text2);
        mIcon = (ImageView) itemView.findViewById(android.R.id.icon);
    }
}
