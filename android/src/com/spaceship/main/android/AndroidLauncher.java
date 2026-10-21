package com.spaceship.main.android;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.spaceship.handlers.ActionResolver;
import com.spaceship.main.Main;

/**
 * Ads, Google Play Games and Giftiz were removed (their SDKs no longer exist or work), so the
 * corresponding ActionResolver methods are no-ops. Re-implement them here when adding new SDKs.
 */
public class AndroidLauncher extends AndroidApplication implements ActionResolver {

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

	@Override public void loadInterstital() {}
	@Override public void showInterstital() {}
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
