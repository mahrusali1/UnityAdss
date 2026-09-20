package com.mahrusali1.unityads;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;

import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.AndroidViewComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.errors.YailRuntimeError;

import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.services.banners.BannerErrorInfo;
import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.UnityBannerSize;

public class UnityAdsSimple extends AndroidNonvisibleComponent
        implements IUnityAdsLoadListener, IUnityAdsShowListener, BannerView.IListener {

    private final Context context;
    private final Activity activity;

    private String gameId = "";
    private boolean testMode = false;
    private BannerView theBannerView;

    public UnityAdsSimple(ComponentContainer container) {
        super(container.$form());
        context = container.$context();
        activity = (Activity) container.$context();
    }

    @DesignerProperty(
            editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
            defaultValue = "False"
    )
    @SimpleProperty
    public void TestMode(boolean value) {
        testMode = value;
    }

    @SimpleProperty
    public boolean TestMode() {
        return testMode;
    }

    @DesignerProperty
    @SimpleProperty
    public void GameId(String value) {
        gameId = value;
    }

    @SimpleProperty
    public String GameId() {
        return gameId;
    }

    // =========================
    // INITIALIZATION
    // =========================

    @SimpleEvent
    public void InitializationComplete() {
        EventDispatcher.dispatchEvent(this, "InitializationComplete");
    }

    @SimpleEvent
    public void InitializationFailed(String message) {
        EventDispatcher.dispatchEvent(this, "InitializationFailed", message);
    }

    @SimpleFunction
    public void Initialize() {
        UnityAds.initialize(
                context,
                gameId,
                testMode,
                new IUnityAdsInitializationListener() {
                    @Override
                    public void onInitializationComplete() {
                        InitializationComplete();
                    }

                    @Override
                    public void onInitializationFailed(
                            UnityAds.UnityAdsInitializationError error,
                            String message) {
                        InitializationFailed(message);
                    }
                }
        );
    }

    // =========================
    // INTERSTITIAL
    // =========================

    @SimpleEvent
    public void AdLoaded(String adUnitId) {
        EventDispatcher.dispatchEvent(this, "AdLoaded", adUnitId);
    }

    @SimpleEvent
    public void AdFailedToLoad(String adUnitId, String message) {
        EventDispatcher.dispatchEvent(this, "AdFailedToLoad", adUnitId, message);
    }

    @SimpleEvent
    public void AdShowFailed(String adUnitId, String message) {
        EventDispatcher.dispatchEvent(this, "AdShowFailed", adUnitId, message);
    }

    @SimpleEvent
    public void AdShowStarted(String adUnitId) {
        EventDispatcher.dispatchEvent(this, "AdShowStarted", adUnitId);
    }

    @SimpleEvent
    public void AdShowClicked(String adUnitId) {
        EventDispatcher.dispatchEvent(this, "AdShowClicked", adUnitId);
    }

    @SimpleEvent
    public void AdShowCompleted(String adUnitId) {
        EventDispatcher.dispatchEvent(this, "AdShowCompleted", adUnitId);
    }

    @SimpleEvent
    public void AdShowSkipped(String adUnitId) {
        EventDispatcher.dispatchEvent(this, "AdShowSkipped", adUnitId);
    }

    @SimpleFunction
    public void LoadInterstitialAd(String adUnitId) {
        UnityAds.load(adUnitId, this);
    }

    @SimpleFunction
    public void ShowInterstitialAd(String adUnitId) {
        UnityAds.show(activity, adUnitId, this);
    }

    // =========================
    // REWARDED
    // =========================

    @SimpleFunction
    public void LoadRewardedAd(String adUnitId) {
        UnityAds.load(adUnitId, this);
    }

    @SimpleFunction
    public void ShowRewardedAd(String adUnitId) {
        UnityAds.show(
                activity,
                adUnitId,
                new UnityAds.UnityAdsShowOptions(),
                this
        );
    }

    // =========================
    // BANNER
    // =========================

    @SimpleEvent
    public void BannerFailedToLoad(String adUnitId, String error) {
        EventDispatcher.dispatchEvent(
                this,
                "BannerFailedToLoad",
                adUnitId,
                error
        );
    }

    @SimpleEvent
    public void BannerClicked(String adUnitId) {
        EventDispatcher.dispatchEvent(this, "BannerClicked", adUnitId);
    }

    @SimpleEvent
    public void BannerLoaded(String adUnitId) {
        EventDispatcher.dispatchEvent(this, "BannerLoaded", adUnitId);
    }

    @SimpleEvent
    public void BannerLeftApplication(String adUnitId) {
        EventDispatcher.dispatchEvent(
                this,
                "BannerLeftApplication",
                adUnitId
        );
    }

    @SimpleFunction
    public void LoadBannerAd(String adUnitId, Object size) {
        if (size instanceof UnityBannerSize) {
            theBannerView = new BannerView(
                    activity,
                    adUnitId,
                    (UnityBannerSize) size
            );

            theBannerView.setListener(this);
            theBannerView.load();

        } else {
            throw new YailRuntimeError(
                    "Size not found",
                    "RuntimeError"
            );
        }
    }

    @SimpleFunction
    public void ShowBannerAd(AndroidViewComponent in) {
        if (theBannerView == null) {
            throw new YailRuntimeError(
                    "Banner has not been loaded",
                    "RuntimeError"
            );
        }

        ViewGroup viewGroup = (ViewGroup) in.getView();

        if (theBannerView.getParent() != null) {
            ((ViewGroup) theBannerView.getParent())
                    .removeView(theBannerView);
        }

        viewGroup.addView(theBannerView);
    }

    @SimpleFunction
    public Object CustomSize(int width, int height) {
        return new UnityBannerSize(width, height);
    }

    @SimpleProperty
    public Object DynamicSize() {
        return UnityBannerSize.getDynamicSize(context);
    }

    @SimpleFunction
    public Object NormalSize() {
        return new UnityBannerSize(320, 50);
    }

    // =========================
    // UNITY ADS CALLBACKS
    // =========================

    @Override
    public void onUnityAdsAdLoaded(String adUnitId) {
        AdLoaded(adUnitId);
    }

    @Override
    public void onUnityAdsFailedToLoad(
            String adUnitId,
            UnityAds.UnityAdsLoadError error,
            String message) {

        AdFailedToLoad(adUnitId, message);
    }

    @Override
    public void onUnityAdsShowFailure(
            String adUnitId,
            UnityAds.UnityAdsShowError error,
            String message) {

        AdShowFailed(adUnitId, message);
    }

    @Override
    public void onUnityAdsShowStart(String adUnitId) {
        AdShowStarted(adUnitId);
    }

    @Override
    public void onUnityAdsShowClick(String adUnitId) {
        AdShowClicked(adUnitId);
    }

    @Override
    public void onUnityAdsShowComplete(
            String adUnitId,
            UnityAds.UnityAdsShowCompletionState state) {

        if (state ==
                UnityAds.UnityAdsShowCompletionState.SKIPPED) {

            AdShowSkipped(adUnitId);

        } else {

            AdShowCompleted(adUnitId);
        }
    }

    // =========================
    // BANNER CALLBACKS
    // =========================

    @Override
    public void onBannerLoaded(BannerView bannerView) {
        theBannerView = bannerView;
        BannerLoaded(bannerView.getPlacementId());
    }

    @Override
    public void onBannerClick(BannerView bannerView) {
        BannerClicked(bannerView.getPlacementId());
    }

    @Override
    public void onBannerFailedToLoad(
            BannerView bannerView,
            BannerErrorInfo errorInfo) {

        BannerFailedToLoad(
                bannerView.getPlacementId(),
                errorInfo.errorMessage
        );
    }

    @Override
    public void onBannerLeftApplication(
            BannerView bannerView) {

        BannerLeftApplication(
                bannerView.getPlacementId()
        );
    }
}
