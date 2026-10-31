package com.cartoniq.netmapper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.cartoniq.netmapper.ads.AdManager
import com.cartoniq.netmapper.ui.theme.NetMapperTheme
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NetMapperTheme {
                NetMapperApp()
            }
        }
        setupConsentAndAds()
    }

    private fun setupConsentAndAds() {
        val consent = UserMessagingPlatform.getConsentInformation(this)
        if (consent.canRequestAds()) AdManager.initialize(this)
        consent.requestConsentInfoUpdate(
            this,
            ConsentRequestParameters.Builder().build(),
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(this) { _ ->
                    if (consent.canRequestAds()) AdManager.initialize(this)
                }
            },
            { _ ->
                if (consent.canRequestAds()) AdManager.initialize(this)
            }
        )
    }
}
