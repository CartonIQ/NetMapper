package com.cartoniq.netmapper.ads

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import java.util.concurrent.atomic.AtomicBoolean

fun Context.findActivity(): Activity? {
    var c: Context = this
    while (c is ContextWrapper) {
        if (c is Activity) return c
        c = c.baseContext
    }
    return null
}

/** AdMob helper. Uses Google TEST ad unit IDs only. */
object AdManager {
    const val APP_ID = "ca-app-pub-9771662959477951~2254244696"
    const val BANNER_ID = "ca-app-pub-9771662959477951/8025893235"
    const val REWARDED_ID = "ca-app-pub-9771662959477951/4254169185"

    /** True once consent allows ad requests and the SDK is initialised. */
    var adsReady by mutableStateOf(false)
        private set

    private val initialized = AtomicBoolean(false)
    private var rewarded: RewardedAd? = null
    private var loading = false

    fun initialize(context: Context) {
        if (!initialized.compareAndSet(false, true)) return
        MobileAds.initialize(context.applicationContext) { }
        adsReady = true
        loadRewarded(context)
    }

    fun loadRewarded(context: Context) {
        if (rewarded != null || loading) return
        loading = true
        RewardedAd.load(
            context.applicationContext,
            REWARDED_ID,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewarded = ad
                    loading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewarded = null
                    loading = false
                }
            }
        )
    }

    /**
     * Shows a rewarded ad and runs [onGranted] when the reward was earned.
     * If no ad is available (offline / no fill) the action runs anyway so the app never blocks.
     */
    fun showRewarded(activity: Activity, onGranted: () -> Unit) {
        val ad = rewarded
        if (ad == null) {
            loadRewarded(activity)
            onGranted()
            return
        }
        var earned = false
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewarded = null
                loadRewarded(activity)
                if (earned) onGranted()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                rewarded = null
                loadRewarded(activity)
                onGranted()
            }
        }
        ad.show(activity, OnUserEarnedRewardListener { earned = true })
    }

    fun runWithReward(context: Context, action: () -> Unit) {
        val activity = context.findActivity()
        if (activity == null) action() else showRewarded(activity, action)
    }
}

@Composable
fun BannerAd(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        contentAlignment = Alignment.Center
    ) {
        if (AdManager.adsReady) {
            AndroidView(
                factory = { ctx ->
                    AdView(ctx).apply {
                        setAdSize(AdSize.BANNER)
                        adUnitId = AdManager.BANNER_ID
                        loadAd(AdRequest.Builder().build())
                    }
                },
                onRelease = { it.destroy() }
            )
        }
    }
}
