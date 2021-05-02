package com.nims.hsketch

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import java.util.*

class AdOpenManager(adApplication: AdApplication): LifecycleObserver, ActivityLifecycleCallbacks {
    private val mAdApplication           : AdApplication = adApplication
    private var mAppOpenAd               : AppOpenAd?    = null
    private lateinit var mLoadCallback   : AppOpenAd.AppOpenAdLoadCallback
    private lateinit var mCurrentActivity: Activity
    private var mLoadTime                : Long          = 0L

    companion object {
        private val TAG                                  = "AdOpenManager"
        private val mAdOpeningId                         = BuildConfig.ADMOB_OPENING_ID
        private var mIsShowingAd                         = false
    }

    init {
        mAdApplication.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    private fun showAdIfAvailable(){
        if (!mIsShowingAd && isAdAvailable()) {
            Log.d(TAG, "Will show ad.");


            val fullScreenContentCallback = object :FullScreenContentCallback()
            {
                override fun onAdDismissedFullScreenContent() {
                    mAppOpenAd   = null
                    mIsShowingAd = false
                    fetchAd()
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {}

                override fun onAdShowedFullScreenContent() {
                    mIsShowingAd = true
                }
            }

            mAppOpenAd?.fullScreenContentCallback = fullScreenContentCallback
            mAppOpenAd?.show(mCurrentActivity)

        } else {
            Log.d(TAG, "Can not show ad.")
            fetchAd()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    open fun onStart(){
        showAdIfAvailable()
        Log.d(TAG, "onStart")
    }

    open fun fetchAd(){
        if (isAdAvailable()) return
        mLoadCallback = object :AppOpenAd.AppOpenAdLoadCallback(){
            override fun onAdLoaded(ad: AppOpenAd) {
                mAppOpenAd = ad
                mLoadTime  = Date().time

                Log.d(TAG, "App-Open Ad Loaded")
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
            }
        }
        val request = getAdRequest()
        AppOpenAd.load(mAdApplication, mAdOpeningId, request, AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, mLoadCallback)
    }

    private fun getAdRequest(): AdRequest{
        return AdRequest.Builder().build()
    }

    open fun isAdAvailable(): Boolean{
        return mAppOpenAd != null && wasLoadTimeLessThanNHoursAgo()
    }

    private fun wasLoadTimeLessThanNHoursAgo(): Boolean{
        val dateDifference         = Date().time - mLoadTime
        val numMilliSecondsPerHour = 3600000
        return (dateDifference < (numMilliSecondsPerHour * 4.toLong()))
    }

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {

    }

    override fun onActivityStarted(activity: Activity) {
        mCurrentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        mCurrentActivity = activity
    }

    override fun onActivityPaused(p0: Activity) {}

    override fun onActivityStopped(p0: Activity) {}

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {}

    override fun onActivityDestroyed(p0: Activity) {}

}