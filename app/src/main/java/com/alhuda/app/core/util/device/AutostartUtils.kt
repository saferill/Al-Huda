package com.alhuda.app.core.util.device

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.net.toUri
import com.alhuda.app.core.util.android.IntentUtils
import kotlin.concurrent.Volatile

object AutostartUtils {
    private const val TAG = "AutostartUtils"

    @Volatile
    private var cache: Intent? = null

    private val AUTOSTART_INTENTS: List<Intent> =
        listOf(

            createIntent(
                "com.miui.securitycenter",
                "com.miui.permcenter.autostart.AutoStartManagementActivity",
            ),

            createIntent(
                "com.letv.android.letvsafe",
                "com.letv.android.letvsafe.AutobootManageActivity",
            ),

            createIntent(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity",
            ),
            createIntent(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity",
            ),
            createIntent(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.optimize.process.ProtectActivity",
            ),

            createIntent(
                "com.coloros.safecenter",
                "com.coloros.safecenter.permission.startup.StartupAppListActivity",
            ),
            createIntent(
                "com.coloros.safecenter",
                "com.coloros.safecenter.startupapp.StartupAppListActivity",
            ),
            createIntent(
                "com.oppo.safe",
                "com.oppo.safe.permission.startup.StartupAppListActivity",
            ),

            createIntent(
                "com.iqoo.secure",
                "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity",
            ),
            createIntent(
                "com.vivo.permissionmanager",
                "com.vivo.permissionmanager.activity.BgStartUpManagerActivity",
            ),
            createIntent(
                "com.iqoo.secure",
                "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager",
            ),

            createIntent(
                "com.oneplus.security",
                "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity",
            ),

            createIntent(
                "com.asus.mobilemanager",
                "com.asus.mobilemanager.autostart.AutoStartActivity",
            ),
            createIntent(
                "com.asus.mobilemanager",
                "com.asus.mobilemanager.entry.FunctionActivity",
            )
                .setData("mobilemanager://function/entry/AutoStart".toUri()),

            createIntent(
                "com.transsion.phonemanager",
                "com.itel.autobootmanager.activity.AutoBootMgrActivity",
            ),
        )

    fun findAutostartIntent(context: Context?): Intent? {
        cache?.let { return it }
        for (intent in AUTOSTART_INTENTS) {
            if (IntentUtils.isAvailableOnDevice(context, intent)) {
                cache = intent
                return intent
            }
        }
        return null
    }

    fun hasAutostartSettings(context: Context): Boolean = findAutostartIntent(context) != null

    fun openAutostartSettings(activity: Activity?) {
        val intent = findAutostartIntent(activity?.applicationContext)
        if (intent == null) {
            Log.w(TAG, "No auto-start settings activity available on this device")
            return
        }
        try {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            IntentUtils.startActivityOnUiThread(activity, intent)
        } catch (e: Exception) {
            Log.e(TAG, "Unable to open auto-start settings: " + IntentUtils.getActivityName(intent), e)
        }
    }

    private fun createIntent(
        pkg: String,
        cls: String,
    ): Intent = Intent().setComponent(ComponentName(pkg, cls))
}
