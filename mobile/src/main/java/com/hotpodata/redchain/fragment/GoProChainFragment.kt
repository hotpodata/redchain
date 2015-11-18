package com.hotpodata.redchain.fragment

import android.net.Uri
import com.google.android.gms.analytics.HitBuilders
import com.hotpodata.redchain.AnalyticsMaster
import com.hotpodata.redchain.ChainMaster
import com.hotpodata.redchain.R
import com.hotpodata.redchain.utils.IntentUtils
import timber.log.Timber

/**
 * Created by jdrotos on 11/18/15.
 */
class GoProChainFragment: ProFeatureFragment() {

    override fun onResume(){
        super.onResume()
        Timber.i("Setting screen name:" + AnalyticsMaster.SCREEN_PRO_FEATURES);
        AnalyticsMaster.getTracker(getActivity())?.setScreenName(AnalyticsMaster.SCREEN_PRO_FEATURES);
        AnalyticsMaster.getTracker(getActivity())?.send(HitBuilders.ScreenViewBuilder().build());
    }

    override fun getFeatureName(): String? {
        return resources.getString(R.string.go_pro_create_edit)
    }

    override fun getPrimaryColor(): Int {
        return ChainMaster.getSelectedChain().color
    }

    override fun getFeatureBlurb(): String? {
        return resources.getString(R.string.go_pro_create_edit_blurb)
    }

    override fun actionGoPro() {
        var intent = IntentUtils.goPro(getActivity())
        startActivity(intent)
        try {
            AnalyticsMaster.getTracker(getActivity())?.send(HitBuilders.EventBuilder()
                    .setCategory(AnalyticsMaster.CATEGORY_ACTION)
                    .setAction(AnalyticsMaster.ACTION_GO_PRO_FRAGMENT)
                    .build());
        } catch(ex: Exception) {
            Timber.e(ex, "Analytics Exception");
        }
    }

    override fun getVideoUri(): Uri? {
        var uri = "android.resource://" + getActivity().getPackageName() + "/" + R.raw.go_pro_create
        return Uri.parse(uri)
    }
}