package com.spaceship.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.sql.DatabaseCursor;
import com.badlogic.gdx.sql.SQLiteGdxException;

public class Splash implements Screen{
	
	final Main game;
	private Texture progressBar;
	private TextureRegion[] progressTextures;
	private Sprite[] progressSprites;
	private Sprite splash;
	private TextureRegion splashTop, splashBottom;
	/** how far the screen reaches below and above the 720x1440 art, in art pixels */
	private float padBottom, padTop;
	/** height of the off-screen light and rain canvases: the art plus both gaps */
	private int canvasH = 1440;
	private TextureRegion[] glowTopRow, glowBottomRow;

	// neon glow layers drawn additively over the unlit base art, in the order of splash_layers.txt
	private static final String[] GLOW_NAMES = {"ring", "puffin", "beak", "name_a", "name_f", "games_a", "games_m", "frame"};
	private static final boolean[] GLOW_GLITCHES = {true, false, true, false, true, false, true, false};
	private Texture[] glowTextures;
	private float[][] glowBounds;
	private float glowTime;
	private float[] glowStrike, glowPhase, glowGlitchTimer, glowGlitchLevel, glowRecover;
	private int[] glowToggles;
	private float dipTimer, dipTime;
	// rain tiles (720x1440, seamless) scrolled along the slant of the streaks, in image px per second
	private static final String[] RAIN_NAMES = {"rain_far", "rain_mid", "rain_near"};
	private static final float[] RAIN_SPEEDS = {450, 800, 1500};
	private Texture[] rainTextures;
	private static final float RAIN_AMBIENT = 0.08f;
	private SpriteBatch fxBatch;
	// neon sounds, following the same events as the light (respecting the game's sound setting)
	private boolean soundOn = true;
	private Music neonHum;
	private Sound neonZap, neonStrike, neonDip;
	private boolean[] glowStruck;
	// storm bed: a quiet rain loop and the odd distant thunder
	private Music stormRain;
	private Sound[] stormThunder;
	private float thunderTimer;
	private FrameBuffer fbRain, fbLight;
	private TextureRegion rainRegion, lightRegion;
	
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
			cursor = game.getDataBase().rawQuery("SELECT sound FROM seleccionsAndNumbers;");
			if(cursor != null && cursor.next())
				soundOn = cursor.getInt(0) == 1;
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
		game.assetManager.load("audio/door.ogg", Sound.class);
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
		game.assetManager.load("audio/charge.ogg", Sound.class);
		game.assetManager.load("audio/boost.ogg", Sound.class);
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
			game.assetManager.load("res/bgs/bgBrownDark1.jpg", Texture.class);
			game.assetManager.load("res/bgs/bgBrownDark2.jpg", Texture.class);			
			break;
		case 2:
			game.assetManager.load("res/bgs/bgPurple1.jpg", Texture.class);
			game.assetManager.load("res/bgs/bgPurple2.jpg", Texture.class);
			break;
		case 3:
			game.assetManager.load("res/bgs/bgBlackRedDark1.jpg", Texture.class);
			game.assetManager.load("res/bgs/bgBlackRedDark2.jpg", Texture.class);
			break;
		case 4:
			game.assetManager.load("res/bgs/bgAquaDark1.jpg", Texture.class);
			game.assetManager.load("res/bgs/bgAquaDark2.jpg", Texture.class);
			break;
		case 5:
			game.assetManager.load("res/bgs/bgPinkDark1.jpg", Texture.class);
			game.assetManager.load("res/bgs/bgPinkDark2.jpg", Texture.class);
			break;
		case 6:
			game.assetManager.load("res/bgs/bgBlueDark1.jpg", Texture.class);
			game.assetManager.load("res/bgs/bgBlueDark2.jpg", Texture.class);
			break;
		case 7:
			game.assetManager.load("res/bgs/bgDark1.jpg", Texture.class);
			game.assetManager.load("res/bgs/bgDark2.jpg", Texture.class);
			break;
		case 8:
			game.assetManager.load("res/bgs/bgBlue1.jpg", Texture.class);
			game.assetManager.load("res/bgs/bgBlue2.jpg", Texture.class);
			break;
		case 9:
			game.assetManager.load("res/bgs/bgGreenBlue1.jpg", Texture.class);
			game.assetManager.load("res/bgs/bgGreenBlue2.jpg", Texture.class);
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
		com.spaceship.handlers.Fx.load(game.assetManager);
		com.spaceship.handlers.EnergyBar.load(game.assetManager);
		com.spaceship.handlers.ShipPlume.load();

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

			renderLight(delta);
			game.batch.begin();

			// the art keeps its proportions (scaled to the screen width) and sits so its loading-bar slot lines up
			// with the bar; on taller screens the wall continues above and below it as a mirror image of the art's own
			// edge band (the wall's grain carries on, with no seam), and the light and the rain cover the whole screen
			if(splash.getY() > 0) game.batch.draw(splashBottom, 0, 0, Gdx.graphics.getWidth(), splash.getY());
			float splashTopY = splash.getY() + splash.getHeight();
			if(splashTopY < Gdx.graphics.getHeight()) game.batch.draw(splashTop, 0, splashTopY, Gdx.graphics.getWidth(), Gdx.graphics.getHeight() - splashTopY);
			splash.draw(game.batch);
			drawGlow();
			
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

		Texture splashTexture = game.assetManager.get("res/splash.png", Texture.class);
		splash = new Sprite(splashTexture);
		float splashScale = Gdx.graphics.getWidth()/(float)splashTexture.getWidth();
		splash.setSize(Gdx.graphics.getWidth(), splashTexture.getHeight()*splashScale);
		// the loading bar slot is 2/3 of the way down the art (960 of 1440 px)
		splash.setPosition(0, Gdx.graphics.getHeight()/3f - (splashTexture.getHeight() - 960)*splashScale);
		// how far the screen reaches below and above the art, in art pixels (0 when the art already covers it)
		padBottom = Math.max(0f, splash.getY()/splashScale);
		padTop = Math.max(0f, (Gdx.graphics.getHeight() - (splash.getY() + splash.getHeight()))/splashScale);
		// the bands that fill those gaps: the art's own top and bottom bands, mirrored
		int bandTop = Math.min(splashTexture.getHeight(), Math.max(1, MathUtils.ceil(padTop)));
		int bandBottom = Math.min(splashTexture.getHeight(), Math.max(1, MathUtils.ceil(padBottom)));
		splashTop = new TextureRegion(splashTexture, 0, 0, splashTexture.getWidth(), bandTop);
		splashTop.flip(false, true);
		splashBottom = new TextureRegion(splashTexture, 0, splashTexture.getHeight() - bandBottom, splashTexture.getWidth(), bandBottom);
		splashBottom.flip(false, true);
		loadGlow();
		progressBar = game.assetManager.get("res/hud/progress_bar.png", Texture.class);
		// the sheet holds 21 frames of 286x72 (2x of the 143x36 logical size) in a grid, read row by row
		TextureRegion[][] grid = TextureRegion.split(progressBar, 286, 72);
		progressTextures = new TextureRegion[21];
		for(int i = 0; i < progressTextures.length; i++){
			progressTextures[i] = grid[i / grid[0].length][i % grid[0].length];
		}
		progressSprites = new Sprite[progressTextures.length];
		for(int i = 0; i < progressTextures.length; i++){
			progressSprites[i] = new Sprite(progressTextures[i]);
			progressSprites[i].setSize(fx(1.75f*143), fx(1.75f*36));
			progressSprites[i].setPosition(Gdx.graphics.getWidth()/2 - progressSprites[0].getWidth()/2,
					Gdx.graphics.getHeight()/3 - progressSprites[0].getHeight()/2);
		}
		
		
	}

	@Override
	public void hide() {
		if(glowTextures != null){
			for(Texture t : glowTextures) t.dispose();
			glowTextures = null;
		}
		if(neonHum != null){
			neonHum.stop();
			neonHum.dispose();
			neonZap.dispose();
			neonStrike.dispose();
			neonDip.dispose();
			neonHum = null;
		}
		if(stormRain != null){
			stormRain.stop();
			stormRain.dispose();
			for(Sound t : stormThunder) t.dispose();
			stormRain = null;
			stormThunder = null;
		}
		if(fxBatch != null){
			fxBatch.dispose();
			fbRain.dispose();
			fbLight.dispose();
			fxBatch = null;
		}
		if(rainTextures != null){
			for(Texture t : rainTextures) t.dispose();
			rainTextures = null;
		}
	}

	private void loadGlow(){
		int n = GLOW_NAMES.length;
		glowTextures = new Texture[n];
		glowBounds = new float[n][];
		String[] lines = Gdx.files.internal("res/splash_layers.txt").readString().split("\\n");
		for(int i = 0; i < n; i++){
			glowTextures[i] = new Texture(Gdx.files.internal("res/splash/" + GLOW_NAMES[i] + ".png"));
			glowTextures[i].setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
			for(String line : lines){
				String[] f = line.trim().split(" ");
				if(f[0].equals(GLOW_NAMES[i])){
					glowBounds[i] = new float[]{Float.parseFloat(f[1]), Float.parseFloat(f[2]), Float.parseFloat(f[3]), Float.parseFloat(f[4])};
				}
			}
		}
		rainTextures = new Texture[RAIN_NAMES.length];
		for(int i = 0; i < rainTextures.length; i++){
			rainTextures[i] = new Texture(Gdx.files.internal("res/splash/" + RAIN_NAMES[i] + ".png"));
			rainTextures[i].setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		}
		glowStruck = new boolean[n];
		if(soundOn){
			neonHum = Gdx.audio.newMusic(Gdx.files.internal("audio/neon_hum.wav"));
			neonHum.setLooping(true);
			neonHum.setVolume(0);
			neonHum.play();
			neonZap = Gdx.audio.newSound(Gdx.files.internal("audio/neon_zap.wav"));
			neonStrike = Gdx.audio.newSound(Gdx.files.internal("audio/neon_strike.wav"));
			neonDip = Gdx.audio.newSound(Gdx.files.internal("audio/neon_dip.wav"));
			stormRain = Gdx.audio.newMusic(Gdx.files.internal("audio/storm_rain.wav"));
			stormRain.setLooping(true);
			stormRain.setVolume(0.22f);
			stormRain.play();
			stormThunder = new Sound[]{Gdx.audio.newSound(Gdx.files.internal("audio/storm_thunder1.wav")),
					Gdx.audio.newSound(Gdx.files.internal("audio/storm_thunder2.wav"))};
			thunderTimer = 3.5f + (float)Math.random()*3f;
		}
		glowStrike = new float[n];
		glowPhase = new float[n];
		glowGlitchTimer = new float[n];
		glowGlitchLevel = new float[n];
		glowRecover = new float[n];
		glowToggles = new int[n];
		for(int i = 0; i < n; i++){
			// power-up: each tube strikes on at its own moment, stuttering before it holds
			glowStrike[i] = 0.25f + 0.2f*i + (float)Math.random()*0.15f;
			glowPhase[i] = (float)Math.random()*6.28f;
			glowGlitchTimer[i] = 1.5f + (float)Math.random()*2f;
			glowGlitchLevel[i] = 1;
		}
		dipTimer = 4f + (float)Math.random()*3f;
		dipTime = -1;
		glowTime = 0;
	}

	private float strikeOn(int i){
		float d = glowTime - glowStrike[i];
		if(d < 0) return 0;
		if(d > 0.5f) return 1;
		// stutter: on-off-on-off-on
		int step = (int)(d/0.1f);
		return step % 2 == 0 ? 1 : 0.15f;
	}

	private void updateGlow(float delta){
		glowTime += delta;
		for(int i = 0; i < GLOW_NAMES.length; i++){
			if(!glowStruck[i] && glowTime >= glowStrike[i]){
				glowStruck[i] = true;
				if(i == 0) playNeon(neonStrike, 1);
				else playNeon(neonZap, 0.35f);
			}
		}
		if(stormThunder != null){
			thunderTimer -= delta;
			if(thunderTimer <= 0){
				stormThunder[(int)(Math.random()*stormThunder.length)].play(0.45f + 0.3f*(float)Math.random());
				thunderTimer = 7f + (float)Math.random()*8f;
			}
		}
		if(neonHum != null){
			float sum = 0;
			for(int i = 0; i < GLOW_NAMES.length; i++) sum += glowAlpha(i);
			neonHum.setVolume(0.3f*sum/GLOW_NAMES.length);
		}
		for(int i = 0; i < GLOW_NAMES.length; i++){
			if(!GLOW_GLITCHES[i] || glowTime < glowStrike[i] + 0.6f) continue;
			glowGlitchTimer[i] -= delta;
			if(glowGlitchTimer[i] > 0) continue;
			if(glowToggles[i] == 0) glowToggles[i] = 3 + (int)(Math.random()*4);
			glowToggles[i]--;
			if(glowToggles[i] == 0){
				glowGlitchLevel[i] = 1;
				glowGlitchTimer[i] = 0.4f + (float)Math.random()*1.6f;
			}else{
				glowGlitchLevel[i] = glowGlitchLevel[i] == 1 ? 0.15f + (float)Math.random()*0.25f : 1;
				if(glowGlitchLevel[i] < 1) playNeon(neonZap, 0.5f);
				glowGlitchTimer[i] = 0.04f + (float)Math.random()*0.05f;
			}
		}
		if(dipTime >= 0){
			dipTime += delta;
			if(dipTime > 2f) dipTime = -1;
		}else{
			dipTimer -= delta;
			if(dipTimer <= 0 && glowTime > 3f){
				dipTime = 0;
				playNeon(neonDip, 0.8f);
				dipTimer = 3f + (float)Math.random()*3f;
				for(int i = 0; i < GLOW_NAMES.length; i++) glowRecover[i] = 0.12f + 0.15f*(float)Math.random() + 0.08f*i;
			}
		}
	}

	private float dip(int i){
		if(dipTime < 0 || dipTime > glowRecover[i] + 0.2f) return 1;
		if(dipTime < 0.12f) return 0.3f;
		if(dipTime < glowRecover[i]) return 0.3f;
		return 0.3f + 0.7f*(dipTime - glowRecover[i])/0.2f;
	}

	private void playNeon(Sound sound, float volume){
		if(sound != null) sound.play(volume);
	}

	private float glowAlpha(int i){
		float breath = 0.92f + 0.08f*(float)Math.sin(glowTime*1.6f + glowPhase[i]);
		return Math.min(1, strikeOn(i)*breath*glowGlitchLevel[i]*dip(i));
	}

	// the rain is lit by the neon: the glow layers (already soft halos) are multiplied by the falling rain streaks
	// in image space (720x1440 buffers), so the streaks shine only where a tube is bright and flicker with it
	private void renderLight(float delta){
		if(glowTextures == null) return;
		updateGlow(delta);
		if(fxBatch == null){
			// the canvases cover the whole screen: the art (720x1440) plus the gaps below and above it
			canvasH = 1440 + MathUtils.ceil(padBottom) + MathUtils.ceil(padTop);
			fxBatch = new SpriteBatch();
			fxBatch.setProjectionMatrix(new com.badlogic.gdx.math.Matrix4().setToOrtho2D(0, 0, 720, canvasH));
			fbRain = new FrameBuffer(Pixmap.Format.RGB888, 720, canvasH, false);
			fbLight = new FrameBuffer(Pixmap.Format.RGB888, 720, canvasH, false);
			// a glow that reaches the art's top or bottom edge carries on into the gap as a mirror image of its own edge
			// band, like the wall under it (stretching its edge row instead would smear its grain into streaks)
			glowTopRow = new TextureRegion[glowTextures.length];
			glowBottomRow = new TextureRegion[glowTextures.length];
			for(int i = 0; i < glowTextures.length; i++){
				if(glowBounds[i] == null) continue;
				Texture t = glowTextures[i];
				float rowsPerArtPixel = t.getHeight()/glowBounds[i][3];
				if(glowBounds[i][1] <= 0.5f && padTop > 0){
					int rows = Math.min(t.getHeight(), Math.max(1, Math.round(MathUtils.ceil(padTop)*rowsPerArtPixel)));
					glowTopRow[i] = new TextureRegion(t, 0, 0, t.getWidth(), rows);
					glowTopRow[i].flip(false, true);
				}
				if(glowBounds[i][1] + glowBounds[i][3] >= 1439.5f && padBottom > 0){
					int rows = Math.min(t.getHeight(), Math.max(1, Math.round(MathUtils.ceil(padBottom)*rowsPerArtPixel)));
					glowBottomRow[i] = new TextureRegion(t, 0, t.getHeight() - rows, t.getWidth(), rows);
					glowBottomRow[i].flip(false, true);
				}
			}
			rainRegion = new TextureRegion(fbRain.getColorBufferTexture());
			rainRegion.flip(false, true);
			lightRegion = new TextureRegion(fbLight.getColorBufferTexture());
			lightRegion.flip(false, true);
		}
		fbRain.begin();
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		fxBatch.begin();
		fxBatch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE);
		for(int i = 0; i < rainTextures.length; i++){
			float oy = (glowTime*RAIN_SPEEDS[i]) % 1440;
			float ox = (oy*0.08f) % 720;
			for(int tx = -1; tx <= 0; tx++){
				for(int ty = -2; ty <= 0; ty++){		// one more tile up: the canvas can be taller than one rain tile
					fxBatch.draw(rainTextures[i], ox + tx*720, -(oy + ty*1440), 720, 1440);
				}
			}
		}
		fxBatch.end();
		fbRain.end();
		fbLight.begin();
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		fxBatch.begin();
		fxBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
		for(int i = 0; i < glowTextures.length; i++){
			if(glowBounds[i] == null) continue;
			float a = glowAlpha(i);
			if(a <= 0.01f) continue;
			float[] b = glowBounds[i];
			fxBatch.setColor(1, 1, 1, a);
			float pb = MathUtils.ceil(padBottom);
			fxBatch.draw(glowTextures[i], b[0], pb + 1440 - (b[1] + b[3]), b[2], b[3]);
			if(glowTopRow[i] != null && canvasH - (pb + 1440) > 0) fxBatch.draw(glowTopRow[i], b[0], pb + 1440, b[2], canvasH - (pb + 1440));
			if(glowBottomRow[i] != null && pb > 0) fxBatch.draw(glowBottomRow[i], b[0], 0, b[2], pb);
		}
		fxBatch.setColor(1, 1, 1, 1);
		fxBatch.flush();
		fxBatch.setBlendFunction(GL20.GL_ZERO, GL20.GL_SRC_COLOR);
		fxBatch.draw(rainRegion, 0, 0, 720, canvasH);
		fxBatch.end();
		fbLight.end();
	}

	private void drawGlow(){
		if(glowTextures == null || fxBatch == null) return;
		float s = splash.getWidth()/720f;
		game.batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
		for(int i = 0; i < glowTextures.length; i++){
			if(glowBounds[i] == null) continue;
			float a = glowAlpha(i);
			if(a <= 0.01f) continue;
			float[] b = glowBounds[i];
			game.batch.setColor(1, 1, 1, a);
			game.batch.draw(glowTextures[i], b[0]*s, splash.getY() + (1440 - (b[1] + b[3]))*s, b[2]*s, b[3]*s);
			// the glow carries on into the gaps above and below the art
			float artTop = splash.getY() + 1440*s;
			if(glowTopRow[i] != null && Gdx.graphics.getHeight() > artTop) game.batch.draw(glowTopRow[i], b[0]*s, artTop, b[2]*s, Gdx.graphics.getHeight() - artTop);
			if(glowBottomRow[i] != null && splash.getY() > 0) game.batch.draw(glowBottomRow[i], b[0]*s, 0, b[2]*s, splash.getY());
		}
		// lit rain, plus a faint ambient trace of the streaks so the rain reads between the tubes too (both canvases
		// start below the art by the bottom gap and cover the whole screen)
		float canvasY = splash.getY() - MathUtils.ceil(padBottom)*s;
		game.batch.setColor(1, 1, 1, 1);
		game.batch.draw(lightRegion, 0, canvasY, 720*s, canvasH*s);
		game.batch.setColor(0.55f, 0.65f, 0.85f, RAIN_AMBIENT);
		game.batch.draw(rainRegion, 0, canvasY, 720*s, canvasH*s);
		game.batch.setColor(1, 1, 1, 1);
		game.batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
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
