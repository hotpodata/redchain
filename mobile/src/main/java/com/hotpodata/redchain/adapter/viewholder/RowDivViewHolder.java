package com.hotpodata.redchain.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.hotpodata.redchain.R;


/**
 * Created by jdrotos on 1/21/15.
 */
public class RowDivViewHolder extends RecyclerView.ViewHolder {
    public View mSpacer;
    public View mDivBar;

    public RowDivViewHolder(View itemView) {
        super(itemView);
        mSpacer = itemView.findViewById(R.id.spacer);
        mDivBar = itemView.findViewById(R.id.divbar);
    }
}
