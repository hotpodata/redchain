package com.hotpodata.redchain.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.hotpodata.redchain.view.CircleImageView;

/**
 * Created by jdrotos on 1/21/15.
 */
public class RowColorViewHolder extends RecyclerView.ViewHolder {
    public CircleImageView mIcon;

    public RowColorViewHolder(View itemView) {
        super(itemView);
        mIcon = (CircleImageView) itemView.findViewById(android.R.id.icon);
    }
}
