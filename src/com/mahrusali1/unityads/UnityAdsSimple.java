package com.mahrusali1.unityads;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.AndroidViewComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.errors.YailRuntimeError;

import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.services.banners.BannerErrorInfo;
import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.UnityBannerSize;

@DesignerComponent(
    version = 1,
    description = "Simple Unity Ads extension for MIT App Inventor. Banner + initialization.",
    category = ComponentCategory.EXTENSION,
    nonVisible = true,
    iconName = "images/extension.png"
)
@SimpleObject(external = true)
@UsesPermissions(permissionNames = "android.permission.INTERNET")
public class UnityAdsSimple extends AndroidNonvisibleComponent
        implements BannerView.IListener {

    private final Context context;
    private final Activity activity;

    private String gameId = "";
    private boolean testMode = false;

    private boolean initializationStarted = false;
    private BannerView bannerView;

    public UnityAdsSimple(ComponentContainer container) {
        super(container.$form());
        context = container.$context();
        activity = (Activity) container.$context();
    }

    @DesignerProperty(
        editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
        defaultValue = ""
    )
    @SimpleProperty
    public void GameId(String value) {
        gameId = value == null ? "" : value.trim();
    }

    @SimpleProperty
    public String GameId() {
        return gameId;
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

    @SimpleEvent
    public void InitializationComplete() {
        EventDispatcher.dispatchEvent(this, "InitializationComplete");
    }

    @SimpleEvent
    public void InitializationFailed(String message) {
        EventDispatcher.dispatchEvent(this, "InitializationFailed", message);
    }

    @SimpleEvent
    public void BannerLoaded(String adUnitId) {
        EventDispatcher.dispatchEvent(this, "BannerLoaded", adUnitId);
    }

    @SimpleEvent
    public void BannerFailedToLoad(String adUnitId, String error) {
        EventDispatcher.dispatchEvent(this, "BannerFailedToLoad", adUnitId, error);
    }

    @SimpleEvent
    public void BannerClicked(String adUnitId) {
        EventDispatcher.dispatchEvent(this, "BannerClicked", adUnitId);
    }

    @SimpleEvent
    public void BannerLeftApplication(String adUnitId) {
        EventDispatcher.dispatchEvent(this, "BannerLeftApplication", adUnitId);
    }

    @SimpleFunction
    public void Initialize() {
        if (gameId.length() == 0) {
            InitializationFailed("GameId is empty.");
            return;
        }

        // Prevent this extension instance from starting initialization twice.
        if (initializationStarted) {
            return;
        }

        // If another component has already initialized Unity Ads, do not call
        // initialize() again. The SDK is process-wide.
        if (UnityAds.isInitialized()) {
            initializationStarted = true;
            InitializationComplete();
            return;
        }

        initializationStarted = true;

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

    @SimpleFunction
    public void LoadBannerAd(String adUnitId, Object size) {
        if (!UnityAds.isInitialized()) {
            throw new YailRuntimeError(
                "Unity Ads is not initialized. Call Initialize first.",
                "RuntimeError"
            );
        }

        if (adUnitId == null || adUnitId.trim().length() == 0) {
            throw new YailRuntimeError(
                "Ad Unit ID is empty.",
                "RuntimeError"
            );
        }

        if (!(size instanceof UnityBannerSize)) {
            throw new YailRuntimeError(
                "Size not found.",
                "RuntimeError"
            );
        }

        bannerView = new BannerView(
            activity,
            adUnitId,
            (UnityBannerSize) size
        );
        bannerView.setListener(this);
        bannerView.load();
    }

    @SimpleFunction
    public void ShowBannerAd(AndroidViewComponent in) {
        if (bannerView == null) {
            throw new YailRuntimeError(
                "Banner has not been loaded.",
                "RuntimeError"
            );
        }

        ViewGroup viewGroup = (ViewGroup) in.getView();

        if (bannerView.getParent() != null) {
            ((ViewGroup) bannerView.getParent()).removeView(bannerView);
        }

        viewGroup.addView(bannerView);
    }

    @SimpleProperty
    public Object DynamicSize() {
        return UnityBannerSize.getDynamicSize(context);
    }

    @SimpleFunction
    public Object CustomSize(int width, int height) {
        return new UnityBannerSize(width, height);
    }

    @SimpleProperty
    public boolean IsInitialized() {
        return UnityAds.isInitialized();
    }

    @Override
    public void onBannerLoaded(BannerView bannerView) {
        this.bannerView = bannerView;
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
    public void onBannerLeftApplication(BannerView bannerView) {
        BannerLeftApplication(bannerView.getPlacementId());
    }
}
