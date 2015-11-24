package com.hotpodata.redchain.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import timber.log.Timber

/**
 * Created by jdrotos on 11/18/15.
 */
object IntentUtils {

    public fun goPro(ctx: Context): Intent {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setData(Uri.parse("market://details?id=com.hotpodata.redchain.pro"))
        return intent
    }

    public fun isAppInstalled(context: Context, packageName: String): Boolean {
        Timber.d("isAppInstalled:" + packageName)
        var installed = false;
        try {
            var pm = context.packageManager;
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (e: PackageManager.NameNotFoundException) {
            installed = false;
        }
        return installed;
    }


}