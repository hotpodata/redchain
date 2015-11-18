package com.hotpodata.redchain.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.hotpodata.redchain.BuildConfig

/**
 * Created by jdrotos on 11/18/15.
 */
object IntentUtils {

    public fun goPro(ctx: Context): Intent {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setData(Uri.parse("market://details?id=com.hotpodata.redchain.pro"))
        return intent
    }


}