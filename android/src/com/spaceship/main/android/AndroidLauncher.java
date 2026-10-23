package com.spaceship.main.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;
import com.spaceship.handlers.ActionResolver;
import com.spaceship.main.Main;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Interstitial ads (Google AdMob, with the UMP consent form) and the one-time "remove ads" purchase (Google Play Billing).
 * Google Play Games and Giftiz were removed (their SDKs no longer exist or work), so those ActionResolver methods are no-ops.
 */
public class AndroidLauncher extends AndroidApplication implements ActionResolver {

	// the interstitial unit id comes from android/build.gradle: the real one in release, Google's test id in debug
	private static final String REMOVE_ADS_SKU = "remove_ads";
	/** One interstitial every this many game overs. */
	private static final int GAMES_PER_AD = 3;

	private SharedPreferences prefs;
	private volatile boolean adsRemoved;
	private final AtomicBoolean adsInitialized = new AtomicBoolean();
	private InterstitialAd interstitial;
	private boolean loadingAd;
	private int gameOvers;

	private BillingClient billing;
	private ProductDetails removeAdsDetails;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useImmersiveMode = true;

		WindowManager.LayoutParams attrs = getWindow().getAttributes();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
			attrs.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
		}
		getWindow().setAttributes(attrs);

		initialize(new Main(this), config);

		prefs = getSharedPreferences("ads", MODE_PRIVATE);
		adsRemoved = prefs.getBoolean("removed", false);
		setupBilling();
		setupConsentAndAds();
	}

	// ---- ads ----

	private void setupConsentAndAds() {
		ConsentInformation info = UserMessagingPlatform.getConsentInformation(this);
		info.requestConsentInfoUpdate(this, new ConsentRequestParameters.Builder().build(),
				() -> UserMessagingPlatform.loadAndShowConsentFormIfRequired(this, formError -> {
					if (info.canRequestAds())
						initAds();
				}),
				error -> {
					if (info.canRequestAds())
						initAds();
				});
		if (info.canRequestAds())
			initAds();
	}

	private void initAds() {
		if (!adsInitialized.compareAndSet(false, true))
			return;
		MobileAds.initialize(this, status -> loadInterstital());
	}

	@Override
	public void loadInterstital() {
		runOnUiThread(() -> {
			if (adsRemoved || !adsInitialized.get() || interstitial != null || loadingAd)
				return;
			loadingAd = true;
			InterstitialAd.load(this, getString(R.string.admob_interstitial_id), new AdRequest.Builder().build(), new InterstitialAdLoadCallback() {
				@Override
				public void onAdLoaded(InterstitialAd ad) {
					interstitial = ad;
					loadingAd = false;
				}

				@Override
				public void onAdFailedToLoad(LoadAdError error) {
					interstitial = null;
					loadingAd = false;
				}
			});
		});
	}

	@Override
	public void showInterstital() {
		runOnUiThread(() -> {
			if (adsRemoved)
				return;
			gameOvers++;
			if (gameOvers % GAMES_PER_AD != 0 || interstitial == null) {
				loadInterstital();
				return;
			}
			InterstitialAd ad = interstitial;
			interstitial = null;
			ad.setFullScreenContentCallback(new FullScreenContentCallback() {
				@Override
				public void onAdDismissedFullScreenContent() {
					loadInterstital();
				}

				@Override
				public void onAdFailedToShowFullScreenContent(AdError error) {
					loadInterstital();
				}
			});
			ad.show(this);
		});
	}

	// ---- remove ads purchase ----

	private void setupBilling() {
		billing = BillingClient.newBuilder(this)
				.setListener((result, purchases) -> {
					if (result.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null)
						for (Purchase p : purchases)
							handlePurchase(p);
				})
				.enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
				.build();
		connectBilling(null);
	}

	private void connectBilling(Runnable then) {
		if (billing.isReady()) {
			if (then != null)
				then.run();
			return;
		}
		billing.startConnection(new BillingClientStateListener() {
			@Override
			public void onBillingSetupFinished(BillingResult result) {
				if (result.getResponseCode() != BillingClient.BillingResponseCode.OK)
					return;
				queryOwned();
				billing.queryProductDetailsAsync(QueryProductDetailsParams.newBuilder().setProductList(List.of(
						QueryProductDetailsParams.Product.newBuilder().setProductId(REMOVE_ADS_SKU)
								.setProductType(BillingClient.ProductType.INAPP).build())).build(),
						(r, details) -> {
							List<ProductDetails> list = details == null ? null : details.getProductDetailsList();
							if (r.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null && !list.isEmpty())
								removeAdsDetails = list.get(0);
						});
				if (then != null)
					then.run();
			}

			@Override
			public void onBillingServiceDisconnected() {
			}
		});
	}

	private void queryOwned() {
		billing.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(), (r, purchases) -> {
			if (r.getResponseCode() == BillingClient.BillingResponseCode.OK)
				for (Purchase p : purchases)
					handlePurchase(p);
		});
	}

	private void handlePurchase(Purchase purchase) {
		if (!purchase.getProducts().contains(REMOVE_ADS_SKU) || purchase.getPurchaseState() != Purchase.PurchaseState.PURCHASED)
			return;
		if (!purchase.isAcknowledged())
			billing.acknowledgePurchase(AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build(), r -> {
			});
		adsRemoved = true;
		prefs.edit().putBoolean("removed", true).apply();
		runOnUiThread(() -> interstitial = null);
	}

	@Override public boolean isAdsRemoved() { return adsRemoved; }

	@Override
	public void buyRemoveAds() {
		runOnUiThread(() -> connectBilling(() -> {
			if (removeAdsDetails == null)
				return;
			billing.launchBillingFlow(this, BillingFlowParams.newBuilder().setProductDetailsParamsList(List.of(
					BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(removeAdsDetails).build())).build());
		}));
	}

	@Override
	public void restorePurchases() {
		runOnUiThread(() -> connectBilling(this::queryOwned));
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (!hasFocus) return;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			WindowInsetsController controller = getWindow().getInsetsController();
			if (controller != null) {
				controller.hide(WindowInsets.Type.systemBars());
				controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
			}
		}
	}

	@Override public int getAdHeight() { return 0; }

	@Override public boolean getSignedIn() { return false; }
	@Override public void logIn() {}
	@Override public void submitScore(int score) {}
	@Override public void unlockAchievement(String achievementId) {}
	@Override public void getLeaderboard() {}
	@Override public void getAchievements() {}

	@Override public void loadAdBuddiz() {}
	@Override public void giftizOnResume() {}
	@Override public void giftizOnPause() {}
	@Override public void giftizMission() {}
	@Override public void giftizButtonClicked() {}
	@Override public void giftizDelegate() {}
	@Override public boolean isGifitzAvailable() { return false; }
	@Override public int updateButtonImage() { return 0; }

	@Override
	public void share(String str) {
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT, str);
		sendIntent.setType("text/plain");
		startActivity(Intent.createChooser(sendIntent, null));
	}
}
