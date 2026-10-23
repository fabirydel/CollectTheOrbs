package com.spaceship.handlers;

public interface ActionResolver {
	  
	public void loadInterstital();
	  
	public void showInterstital();
	
	public int getAdHeight();
	
	public boolean getSignedIn();
	public void logIn();
	public void submitScore(int score);
	public void unlockAchievement(String achievementId);
	public void getLeaderboard();
	public void getAchievements();
	
	public void loadAdBuddiz();
	public void giftizOnResume();
	public void giftizOnPause();
	public void giftizMission();
	
	public void giftizButtonClicked();
	public void giftizDelegate();
	public boolean isGifitzAvailable();
	
	public void share(String str);
	
	public int updateButtonImage();

	/** In-app purchase that removes the ads (Google Play Billing). */
	public boolean isAdsRemoved();
	public void buyRemoveAds();
	public void restorePurchases();
	
}