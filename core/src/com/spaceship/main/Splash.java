package com.spaceship.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.sql.DatabaseCursor;
import com.badlogic.gdx.sql.SQLiteGdxException;

public class Splash implements Screen{
	
	final Main game;
	private Texture progressBar;
	private TextureRegion[] progressTextures;
	private Sprite[] progressSprites;
	private Sprite splash;
	
	int level;
	int galaxySelected;

	private DatabaseCursor cursor;
	
	float ratioMotoG;
	float ratioV;
	float x;
	float y;
	
	public Splash(final Main gam) {
		this.game = gam;
		
    	ratioMotoG = 0.5625f;
    	ratioV = (float) Gdx.graphics.getWidth()/Gdx.graphics.getHeight();
    	x = Math.min(Gdx.graphics.getWidth()/720f, Gdx.graphics.getHeight()/1280f);
    	y = Math.min(Gdx.graphics.getWidth()/720f, Gdx.graphics.getHeight()/1280f);
    	
    	try{
			cursor = game.getDataBase().rawQuery("SELECT galaxySelected FROM seleccionsAndNumbers;");
			if(cursor != null && cursor.next())
				galaxySelected = Integer.valueOf(cursor.getInt(0));
			cursor.close();
		}catch (SQLiteGdxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		game.assetManager.load("audio/absorb.ogg", Sound.class);
		game.assetManager.load("audio/loseLife.ogg", Sound.class);
		game.assetManager.load("audio/life.ogg", Sound.class);
		game.assetManager.load("audio/change2.ogg", Sound.class);
		game.assetManager.load("audio/bought.ogg", Sound.class);
		game.assetManager.load("audio/buttonSmall.ogg", Sound.class);
		game.assetManager.load("audio/buttonLarge.ogg", Sound.class);
		game.assetManager.load("audio/select.ogg", Sound.class);
		game.assetManager.load("audio/launch.ogg", Sound.class);
		game.assetManager.load("audio/pre.ogg", Sound.class);
		game.assetManager.load("audio/dead.ogg", Sound.class);
		game.assetManager.load("audio/shield.ogg", Sound.class);
		game.assetManager.load("audio/object.ogg", Sound.class);
		game.assetManager.load("audio/objectCount.ogg", Sound.class);
		game.assetManager.load("audio/slow.ogg", Sound.class);
		game.assetManager.load("audio/slowReverse.ogg", Sound.class);
		game.assetManager.load("audio/sword.ogg", Sound.class);
		game.assetManager.load("audio/swordReverse.ogg", Sound.class);
		game.assetManager.load("audio/swap.ogg", Sound.class);
		game.assetManager.load("audio/swapReverse.ogg", Sound.class);
		game.assetManager.load("audio/mult.ogg", Sound.class);
		game.assetManager.load("audio/multReverse.ogg", Sound.class);
		game.assetManager.load("audio/music.ogg", Music.class);

		game.assetManager.load("fonts/prototype64redBlack.fnt", BitmapFont.class);
		game.assetManager.load("fonts/prototype64blueWhite.fnt", BitmapFont.class);
		game.assetManager.load("fonts/prototype64redBlack2.fnt", BitmapFont.class);
		game.assetManager.load("fonts/prototype64blueWhite2.fnt", BitmapFont.class);
		game.assetManager.load("fonts/prototype64yellowOrange.fnt", BitmapFont.class);
		game.assetManager.load("fonts/prototype128redBlack.fnt", BitmapFont.class);
		game.assetManager.load("fonts/prototype128redBlack2.fnt", BitmapFont.class);
		game.assetManager.load("fonts/prototype128blueWhite.fnt", BitmapFont.class);
		game.assetManager.load("fonts/prototype128blueWhite2.fnt", BitmapFont.class);
		game.assetManager.load("fonts/prototype96grey.fnt", BitmapFont.class);
		game.assetManager.load("fonts/prototype96grey2.fnt", BitmapFont.class);

		game.assetManager.load("res/bgs/bgsBought1.png", Texture.class);
		game.assetManager.load("res/bgs/bgsBought2.png", Texture.class);
		game.assetManager.load("res/bgs/bgsPrice1.png", Texture.class);
		game.assetManager.load("res/bgs/bgsPrice2.png", Texture.class);
		switch(galaxySelected){
		case 0:
			game.assetManager.load("res/bgs/bgBlack1.png", Texture.class);
			game.assetManager.load("res/bgs/bgBlack2.png", Texture.class);
			break;
		case 1:
			game.assetManager.load("res/bgs/bgBrownDark1.png", Texture.class);
			game.assetManager.load("res/bgs/bgBrownDark2.png", Texture.class);			
			break;
		case 2:
			game.assetManager.load("res/bgs/bgPurple1.png", Texture.class);
			game.assetManager.load("res/bgs/bgPurple2.png", Texture.class);
			break;
		case 3:
			game.assetManager.load("res/bgs/bgBlackRedDark1.png", Texture.class);
			game.assetManager.load("res/bgs/bgBlackRedDark2.png", Texture.class);
			break;
		case 4:
			game.assetManager.load("res/bgs/bgAquaDark1.png", Texture.class);
			game.assetManager.load("res/bgs/bgAquaDark2.png", Texture.class);
			break;
		case 5:
			game.assetManager.load("res/bgs/bgPinkDark1.png", Texture.class);
			game.assetManager.load("res/bgs/bgPinkDark2.png", Texture.class);
			break;
		case 6:
			game.assetManager.load("res/bgs/bgBlueDark1.png", Texture.class);
			game.assetManager.load("res/bgs/bgBlueDark2.png", Texture.class);
			break;
		case 7:
			game.assetManager.load("res/bgs/bgDark1.png", Texture.class);
			game.assetManager.load("res/bgs/bgDark2.png", Texture.class);
			break;
		case 8:
			game.assetManager.load("res/bgs/bgBlue1.png", Texture.class);
			game.assetManager.load("res/bgs/bgBlue2.png", Texture.class);
			break;
		case 9:
			game.assetManager.load("res/bgs/bgGreenBlue1.png", Texture.class);
			game.assetManager.load("res/bgs/bgGreenBlue2.png", Texture.class);
			break;
		}
		
		game.assetManager.load("res/hud/arrowLeft.png", Texture.class);
		game.assetManager.load("res/hud/arrowRight.png", Texture.class);
		game.assetManager.load("res/hud/lifeBar3Lifes.png", Texture.class);
		game.assetManager.load("res/hud/lifeEnergy3Lifes.png", Texture.class);
		game.assetManager.load("res/hud/lifeBar4Lifes.png", Texture.class);
		game.assetManager.load("res/hud/lifeEnergy4Lifes.png", Texture.class);
		game.assetManager.load("res/hud/lifeBar5Lifes.png", Texture.class);
		game.assetManager.load("res/hud/lifeEnergy5Lifes.png", Texture.class);
		game.assetManager.load("res/hud/upgradeBarEmpty.png", Texture.class);
		game.assetManager.load("res/hud/upgradeBarFull.png", Texture.class);
		game.assetManager.load("res/hud/padsRight.png", Texture.class);
		game.assetManager.load("res/hud/padsLeft.png", Texture.class);
		game.assetManager.load("res/hud/padsShield.png", Texture.class);
		game.assetManager.load("res/hud/padsItem.png", Texture.class);
		game.assetManager.load("res/hud/padsDer.png", Texture.class);
		game.assetManager.load("res/hud/padsIzq.png", Texture.class);
		game.assetManager.load("res/hud/padsShieldEsp.png", Texture.class);
		game.assetManager.load("res/hud/padsItemEsp.png", Texture.class);
		game.assetManager.load("res/hud/padVertical.png", Texture.class);
		
		game.assetManager.load("res/shapes/forceFields.png", Texture.class);
		game.assetManager.load("res/shapes/blueBall.png", Texture.class);
		game.assetManager.load("res/shapes/redBall.png", Texture.class);
		game.assetManager.load("res/shapes/objectBall.png", Texture.class);
		game.assetManager.load("res/shapes/blueWrong.png", Texture.class);
		game.assetManager.load("res/shapes/redWrong.png", Texture.class);
		game.assetManager.load("res/shapes/lifeBall.png", Texture.class);
		game.assetManager.load("res/shapes/lifeCounter.png", Texture.class);
		game.assetManager.load("res/shapes/shadowBlue.png", Texture.class);
		game.assetManager.load("res/shapes/shadowRed.png", Texture.class);
		game.assetManager.load("res/shapes/shadowLife.png", Texture.class);
		game.assetManager.load("res/shapes/shadowObject.png", Texture.class);
		game.assetManager.load("res/shapes/blueCounter.png", Texture.class);
		game.assetManager.load("res/shapes/redCounter.png", Texture.class);
		game.assetManager.load("res/shapes/redBallParticle.png", Texture.class);
		game.assetManager.load("res/shapes/glows.png", Texture.class);
		game.assetManager.load("res/shapes/lightningBlue.png", Texture.class);
		game.assetManager.load("res/shapes/lightningRed.png", Texture.class);

		game.assetManager.load("res/upgrades/upgradesPrice.png", Texture.class);

		game.assetManager.load("res/objects/objects.png", Texture.class);
		game.assetManager.load("res/objects/ring.png", Texture.class);
		game.assetManager.load("res/objects/padsControls.png", Texture.class);
		game.assetManager.load("res/objects/padsControlsEsp.png", Texture.class);
		game.assetManager.load("res/objects/orbs.png", Texture.class);
		game.assetManager.load("res/objects/items.png", Texture.class);
		game.assetManager.load("res/objects/orbsEsp.png", Texture.class);
		game.assetManager.load("res/objects/itemsEsp.png", Texture.class);
		game.assetManager.load("res/objects/inst.png", Texture.class);
		game.assetManager.load("res/objects/instEsp.png", Texture.class);
		
		game.assetManager.load("res/ships/shipsPrice1.png", Texture.class);
		game.assetManager.load("res/ships/shipsPrice2.png", Texture.class);
		game.assetManager.load("res/ships/shipsPrice3.png", Texture.class);
		game.assetManager.load("res/ships/shipsPrice4.png", Texture.class);
		game.assetManager.load("res/ships/shipsPrice5.png", Texture.class);
		game.assetManager.load("res/ships/shipsPrice6.png", Texture.class);
		game.assetManager.load("res/ships/shipsPrice7.png", Texture.class);
		game.assetManager.load("res/ships/shipsPrice75.png", Texture.class);
		game.assetManager.load("res/ships/shipsBought1.png", Texture.class);
		game.assetManager.load("res/ships/shipsBought2.png", Texture.class);
		game.assetManager.load("res/ships/shipsBought3.png", Texture.class);
		game.assetManager.load("res/ships/shipsBought4.png", Texture.class);
		game.assetManager.load("res/ships/shipsBought5.png", Texture.class);
		game.assetManager.load("res/ships/shipsBought6.png", Texture.class);
		game.assetManager.load("res/ships/shipsBought7.png", Texture.class);
		game.assetManager.load("res/ships/shipsBought75.png", Texture.class);
		game.assetManager.load("res/ships/ships1.png", Texture.class);
		game.assetManager.load("res/ships/ships2.png", Texture.class);
		game.assetManager.load("res/ships/ships3.png", Texture.class);
		game.assetManager.load("res/ships/ships4.png", Texture.class);
		game.assetManager.load("res/ships/ships5.png", Texture.class);
		game.assetManager.load("res/ships/trace1.png", Texture.class);
		game.assetManager.load("res/ships/trace2.png", Texture.class);
		game.assetManager.load("res/ships/trace3.png", Texture.class);
		game.assetManager.load("res/ships/trace4.png", Texture.class);
		game.assetManager.load("res/ships/trace5.png", Texture.class);
		
		game.assetManager.load("res/gui/bPlay.png", Texture.class);
		game.assetManager.load("res/gui/bPause.png", Texture.class);
		game.assetManager.load("res/gui/bPauseCant.png", Texture.class);
		game.assetManager.load("res/gui/bShips.png", Texture.class);
		game.assetManager.load("res/gui/bGalaxies.png", Texture.class);
		game.assetManager.load("res/gui/bSettings.png", Texture.class);
		game.assetManager.load("res/gui/bGo.png", Texture.class);
		game.assetManager.load("res/gui/bBack.png", Texture.class);
		game.assetManager.load("res/gui/bExit.png", Texture.class);
		game.assetManager.load("res/gui/bRetry.png", Texture.class);
		game.assetManager.load("res/gui/bHome.png", Texture.class);
		game.assetManager.load("res/gui/bTrophy.png", Texture.class);
		game.assetManager.load("res/gui/bLeaderboard.png", Texture.class);
		game.assetManager.load("res/gui/bTutorial.png", Texture.class);
		game.assetManager.load("res/gui/bUpgrades.png", Texture.class);
		game.assetManager.load("res/gui/bStats.png", Texture.class);
		game.assetManager.load("res/gui/bControlsOff.png", Texture.class);
		game.assetManager.load("res/gui/bControlsOn.png", Texture.class);
		game.assetManager.load("res/gui/bSoundOn.png", Texture.class);
		game.assetManager.load("res/gui/bSoundOff.png", Texture.class);
		game.assetManager.load("res/gui/bVibrationOff.png", Texture.class);
		game.assetManager.load("res/gui/bVibrationOn.png", Texture.class);
		game.assetManager.load("res/gui/bControlsOff120.png", Texture.class);
		game.assetManager.load("res/gui/bControlsOn120.png", Texture.class);
		game.assetManager.load("res/gui/bSoundOn120.png", Texture.class);
		game.assetManager.load("res/gui/bSoundOff120.png", Texture.class);
		game.assetManager.load("res/gui/bVibrationOff120.png", Texture.class);
		game.assetManager.load("res/gui/bVibrationOn120.png", Texture.class);
		game.assetManager.load("res/gui/bYes.png", Texture.class);
		game.assetManager.load("res/gui/bNo.png", Texture.class);
		game.assetManager.load("res/gui/bQuestionEN.png", Texture.class);
		game.assetManager.load("res/gui/bQuestionES.png", Texture.class);
		game.assetManager.load("res/gui/bEnglish.png", Texture.class);
		game.assetManager.load("res/gui/bEspanol.png", Texture.class);
		game.assetManager.load("res/gui/bShare.png", Texture.class);

		game.assetManager.load("res/giftiz/giftiz_logo_badge.png", Texture.class);
		game.assetManager.load("res/giftiz/giftiz_logo_warning.png", Texture.class);
		game.assetManager.load("res/giftiz/giftiz_logo.png", Texture.class);
		
	}

	@Override
	public void render(float delta) {
		if(game.assetManager.update()){
			game.setMMS(new MainMenuScreen(game));
			game.setPS(new PlayScreen(game));
			game.setScreen(game.getMMS());
		}else{
			
			Gdx.gl.glClearColor(0, 0, 0, 0);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			
			float p = 100*game.assetManager.getProgress();
			if(p < 5) level = 0;
			else if(p >= 5 && p < 10) level = 1;
			else if(p >= 10 && p < 15) level = 2;
			else if(p >= 15 && p < 20) level = 3;
			else if(p >= 20 && p < 25) level = 4;
			else if(p >= 25 && p < 30) level = 5;
			else if(p >= 30 && p < 35) level = 6;
			else if(p >= 35 && p < 40) level = 7;
			else if(p >= 40 && p < 45) level = 8;
			else if(p >= 45 && p < 50) level = 9;
			else if(p >= 50 && p < 55) level = 10;
			else if(p >= 55 && p < 60) level = 11;
			else if(p >= 60 && p < 65) level = 12;
			else if(p >= 65 && p < 70) level = 13;
			else if(p >= 70 && p < 75) level = 14;
			else if(p >= 75 && p < 80) level = 15;
			else if(p >= 80 && p < 85) level = 16;
			else if(p >= 85 && p < 90) level = 17;
			else if(p >= 90 && p < 95) level = 18;
			else if(p >= 95 && p < 100) level = 19;
			else if(p == 100) level = 20;

			game.batch.begin();

			splash.draw(game.batch);
			
			progressSprites[level].draw(game.batch);
			game.batch.end();
		}
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void show() {

		splash = new Sprite(game.assetManager.get("res/splash.png", Texture.class));
		splash.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		progressBar = game.assetManager.get("res/hud/progress_bar.png", Texture.class);
		progressTextures = TextureRegion.split(progressBar, 143, 36)[0];
		progressSprites = new Sprite[progressTextures.length];
		for(int i = 0; i < progressTextures.length; i++){
			progressSprites[i] = new Sprite(progressTextures[i]);
			progressSprites[i].setSize(fx(1.75f*progressSprites[i].getWidth()), fy(1.75f*progressSprites[i].getHeight()));
			progressSprites[i].setPosition(Gdx.graphics.getWidth()/2 - progressSprites[0].getWidth()/2,
					Gdx.graphics.getHeight()/3 - progressSprites[0].getHeight()/2);
		}
		
		
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		if(Gdx.app.getType().equals(ApplicationType.Android))
			game.getActionResolver().giftizOnPause();
	}

	@Override
	public void resume() {
		if(Gdx.app.getType().equals(ApplicationType.Android))
			game.getActionResolver().giftizOnResume();
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}	
	
	public float fx(float v){
    	return x*v;
	}
    
    public float fy(float v){
    	return y*v;
	}
    
    public float fxy(float v){
    	return (fx(v) + fy(v))/2;
    }
}
