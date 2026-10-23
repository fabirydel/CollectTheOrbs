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
import com.google.android.gms.games.PlayGames;
import com.google.android.gms.games.PlayGamesSdk;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;
import com.spaceship.handlers.ActionResolver;
import com.spaceship.main.Main;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Interstitial ads (Google AdMob, with the UMP consent form), Google Play Games and the one-time "remove ads" purchase (Google Play Billing).
 * Giftiz was removed (its SDK no longer exists), so its ActionResolver methods are no-ops.
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

	/** The game code unlocks achievements by their old (2014) Play Games ids: map those to the current ids in res/values/games_ids.xml. */
	private static final String[][] ACHIEVEMENTS = {
			{"CgkIrLf9wZgEEAIQCQ", "achievement_score_25"}, {"CgkIrLf9wZgEEAIQCg", "achievement_score_50"},
			{"CgkIrLf9wZgEEAIQCw", "achievement_score_75"}, {"CgkIrLf9wZgEEAIQDA", "achievement_score_100"},
			{"CgkIrLf9wZgEEAIQDQ", "achievement_score_200"}, {"CgkIrLf9wZgEEAIQDg", "achievement_score_300"},
			{"CgkIrLf9wZgEEAIQAA", "achievement_ships_1"}, {"CgkIrLf9wZgEEAIQAQ", "achievement_ships_5"},
			{"CgkIrLf9wZgEEAIQAg", "achievement_ships_10"}, {"CgkIrLf9wZgEEAIQAw", "achievement_ships_25"},
			{"CgkIrLf9wZgEEAIQBA", "achievement_ships_50"}, {"CgkIrLf9wZgEEAIQBQ", "achievement_ships_74"},
			{"CgkIrLf9wZgEEAIQBg", "achievement_galaxies_1"}, {"CgkIrLf9wZgEEAIQBw", "achievement_galaxies_5"},
			{"CgkIrLf9wZgEEAIQCA", "achievement_galaxies_9"},
			{"CgkIrLf9wZgEEAIQEQ", "achievement_games_1"}, {"CgkIrLf9wZgEEAIQEg", "achievement_games_10"},
			{"CgkIrLf9wZgEEAIQEw", "achievement_games_25"}, {"CgkIrLf9wZgEEAIQFA", "achievement_games_50"},
			{"CgkIrLf9wZgEEAIQFQ", "achievement_games_100"}, {"CgkIrLf9wZgEEAIQFg", "achievement_games_500"},
			{"CgkIrLf9wZgEEAIQFw", "achievement_upgrades_1"}, {"CgkIrLf9wZgEEAIQGA", "achievement_upgrades_5"},
			{"CgkIrLf9wZgEEAIQGQ", "achievement_upgrades_10"}, {"CgkIrLf9wZgEEAIQGg", "achievement_upgrades_18"},
	};
	private static final int RC_GAMES_UI = 9001;
	private final Map<String, String> achievementIds = new HashMap<>();
	private volatile boolean gamesSignedIn;

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
		setupGames();
		setupConsentAndAds();
	}

	// ---- Google Play Games (sign-in, leaderboard, achievements) ----

	private String gamesId(String resName) {
		int res = getResources().getIdentifier(resName, "string", getPackageName());
		String id = res == 0 ? "" : getString(res);
		return id.isEmpty() || id.equals("TODO") ? null : id;
	}

	private void setupGames() {
		for (String[] a : ACHIEVEMENTS) {
			String id = gamesId(a[1]);
			if (id != null)
				achievementIds.put(a[0], id);
		}
		PlayGamesSdk.initialize(this);
		refreshGamesSignIn();
	}

	private void refreshGamesSignIn() {
		PlayGames.getGamesSignInClient(this).isAuthenticated().addOnCompleteListener(task ->
				gamesSignedIn = task.isSuccessful() && task.getResult().isAuthenticated());
	}

	@Override public boolean getSignedIn() { return gamesSignedIn; }

	@Override
	public void logIn() {
		runOnUiThread(() -> PlayGames.getGamesSignInClient(this).signIn().addOnCompleteListener(task ->
				gamesSignedIn = task.isSuccessful() && task.getResult().isAuthenticated()));
	}

	@Override
	public void submitScore(int score) {
		String id = gamesId("leaderboard_high_score");
		if (gamesSignedIn && id != null)
			PlayGames.getLeaderboardsClient(this).submitScore(id, score);
	}

	@Override
	public void unlockAchievement(String achievementId) {
		String id = achievementIds.get(achievementId);
		if (gamesSignedIn && id != null)
			PlayGames.getAchievementsClient(this).unlock(id);
	}

	@Override
	public void getLeaderboard() {
		String id = gamesId("leaderboard_high_score");
		if (id == null)
			return;
		runOnUiThread(() -> PlayGames.getLeaderboardsClient(this).getLeaderboardIntent(id)
				.addOnSuccessListener(intent -> startActivityForResult(intent, RC_GAMES_UI)));
	}

	@Override
	public void getAchievements() {
		runOnUiThread(() -> PlayGames.getAchievementsClient(this).getAchievementsIntent()
				.addOnSuccessListener(intent -> startActivityForResult(intent, RC_GAMES_UI)));
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
			if (r.getResponseCode() != BillingClient.BillingResponseCode.OK)
				return; // offline or billing unavailable: keep what we knew
			// ownership follows what Google Play reports, so a refunded purchase brings the ads back
			boolean owned = false;
			for (Purchase p : purchases)
				if (p.getProducts().contains(REMOVE_ADS_SKU) && p.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
					owned = true;
					handlePurchase(p);
				}
			if (!owned) {
				adsRemoved = false;
				prefs.edit().putBoolean("removed", false).apply();
				loadInterstital();
			}
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
