package com.hotpodata.redchain.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by jdrotos on 1/21/15.
 */
public class RowTextOneLineViewHolder extends RecyclerView.ViewHolder {
    public TextView mTextOne;
    public ImageView mIcon;

    public RowTextOneLineViewHolder(View itemView) {
        super(itemView);
        mTextOne = (TextView) itemView.findViewById(android.R.id.text1);
        mIcon = (ImageView) itemView.findViewById(android.R.id.icon);
    }
}
