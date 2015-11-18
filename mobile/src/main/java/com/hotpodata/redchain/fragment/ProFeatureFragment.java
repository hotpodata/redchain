package com.hotpodata.redchain.fragment;

import android.app.Dialog;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.VideoView;

import com.hotpodata.redchain.R;

/**
 * Created by jdrotos on 8/1/15.
 */
public abstract class ProFeatureFragment extends DialogFragment {

    protected ViewGroup mRootC;
    protected TextView mTitleTv;
    protected TextView mExplanationTv;
    protected VideoView mFeatureVideo;
    protected Button mGoProBtn;

    protected Handler mHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_pro_feature, parent, false);
        mRootC = (ViewGroup) v.findViewById(R.id.root_container);
        mTitleTv = (TextView) v.findViewById(R.id.title_tv);
        mExplanationTv = (TextView) v.findViewById(R.id.feature_explanation_tv);
        mFeatureVideo = (VideoView) v.findViewById(R.id.feature_video);
        mGoProBtn = (Button) v.findViewById(R.id.go_pro_btn);

        mRootC.setBackgroundColor(getPrimaryColor());
        mGoProBtn.setTextColor(getPrimaryColor());

        mHandler = new Handler();

        Typeface mRobotoLight = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");

        mTitleTv.setTypeface(mRobotoLight);
        mExplanationTv.setTypeface(mRobotoLight);


        mTitleTv.setText(getString(R.string.pro_feature_title_template, getFeatureName()));
        mExplanationTv.setText(getFeatureBlurb());
        mGoProBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionGoPro();
            }
        });

        mFeatureVideo.setZOrderOnTop(true);
        return v;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        mFeatureVideo.setVideoURI(getVideoUri());
        mFeatureVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if(isResumed() && isAdded() && mFeatureVideo != null){
                            mFeatureVideo.start();
                        }
                    }
                };
                mHandler.postDelayed(runnable, 5000);
            }
        });
        mFeatureVideo.start();
    }

    public abstract String getFeatureName();

    public abstract int getPrimaryColor();

    public abstract String getFeatureBlurb();

    public abstract void actionGoPro();

    public abstract Uri getVideoUri();

}
