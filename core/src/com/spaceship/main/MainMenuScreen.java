package com.spaceship.main;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo;

import java.math.BigDecimal;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenEquations;
import aurelienribon.tweenengine.TweenManager;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.sql.DatabaseCursor;
import com.badlogic.gdx.sql.SQLiteGdxException;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.spaceship.handlers.Lightning;
import com.spaceship.handlers.UpgradeBar;
import com.spaceship.handlers.Fx;
import com.spaceship.handlers.ShipPlume;
import com.spaceship.handlers.BitmapFontAccessor;
import com.spaceship.handlers.ImageAccessor;
import com.spaceship.handlers.SpriteAccessor;

public class MainMenuScreen implements Screen {

	final Main game;
	private int state;

	public final int STANDBY = 0;
	public final int LAUNCH = 1;
	public final int SHIPS = 2;
	public final int GALAXIES = 3;
	public final int UPGRADES = 4;
	public final int STATS = 5;
	public final int SETTINGS = 6;
	public final int HELP = 7;
	public final int EXIT = 8;
	public final int LEAVING = 9;

	public final int OFF = 0;
	public final int ON = 1;
	
	private int g;

	private TweenManager tweenManager;

	
	private Texture forceField;
	private Texture forceFieldGlows;
	private Texture lightningBlue;
	private Texture lightningRed;
	private TextureRegion[] ffTextures;
	public Sprite[] ffSprites;
	private TextureRegion[] ffgTextures;
	public Sprite[] ffgSprites;
	private TextureRegion[] lightBTextures;
	public Sprite[] lightBSprites;
	private TextureRegion[] lightRTextures;
	public Sprite[] lightRSprites;
	public Sprite blueCounter;
	private Sprite redCounter;
	private Sprite lifeCounter;
	private Sprite bSoundOn;
	private Sprite bSoundOff;
	public Sprite bShips;
	public Sprite bGalaxies;
	public Sprite bSettings;
	private Sprite bGo;
	private Sprite bExit;
	private Sprite bBack;
	public Sprite bLeaderboard;
	public Sprite bTrophy;
	public Sprite bUpgrades;
	private Sprite bVibrationOn;
	private Sprite bVibrationOff;
	private Sprite bStats;
	public Sprite bHelp;
	private Sprite bControlsOn;
	private Sprite bControlsOff;
	private Sprite bNo;
	private Sprite bYes;
	private Sprite bQuestionEN;
	private Sprite bQuestionES;
	private Sprite bEspanol;
	private Sprite bEnglish;
	public Sprite cover;
	private Sprite upgradeEmpty;
	private Sprite upgradeFull;
	
	private Sprite giftizLogo;
	private Sprite giftizLogoWarning;
	private Sprite giftizLogoBadge;
	
	private Image iSoundOn;
	private Image iSoundOff;
	private Image iVibrationOn;
	private Image iVibrationOff;
	private Image iControlsOn;
	private Image iControlsOff;

	private int shadow;

	public boolean startTrace;
	
	private Sprite[] bgs;
	private Texture bgsBought1;
	private Texture bgsBought2;
	private Texture bgsPrice1;
	private Texture bgsPrice2;
	private TextureRegion[] bgsBoughtTextures1;
	private TextureRegion[] bgsBoughtTextures2;
	private TextureRegion[] bgsPriceTextures1;
	private TextureRegion[] bgsPriceTextures2;
	private Sprite[] bgsBoughtSprite;
	private Sprite[] bgsPriceSprite;
	private Image[] bgsBoughtImage;
	private Image[] bgsPriceImage;

	private Texture shipsPrice1;
	private Texture shipsPrice2;
	private Texture shipsPrice3;
	private Texture shipsPrice4;
	private Texture shipsPrice5;
	private Texture shipsPrice6;
	private Texture shipsPrice7;
	private Texture shipsPrice75;
	private TextureRegion[] shipsPriceTextures1;
	private TextureRegion[] shipsPriceTextures2;
	private TextureRegion[] shipsPriceTextures3;
	private TextureRegion[] shipsPriceTextures4;
	private TextureRegion[] shipsPriceTextures5;
	private TextureRegion[] shipsPriceTextures6;
	private TextureRegion[] shipsPriceTextures7;
	private TextureRegion[] shipsPriceTextures75;
	private Texture shipsBought1;
	private Texture shipsBought2;
	private Texture shipsBought3;
	private Texture shipsBought4;
	private Texture shipsBought5;
	private Texture shipsBought6;
	private Texture shipsBought7;
	private Texture shipsBought75;
	private TextureRegion[] shipsBoughtTextures1;
	private TextureRegion[] shipsBoughtTextures2;
	private TextureRegion[] shipsBoughtTextures3;
	private TextureRegion[] shipsBoughtTextures4;
	private TextureRegion[] shipsBoughtTextures5;
	private TextureRegion[] shipsBoughtTextures6;
	private TextureRegion[] shipsBoughtTextures7;
	private TextureRegion[] shipsBoughtTextures75;
	private Texture ships1;
	private Texture ships2;
	private Texture ships3;
	private Texture ships4;
	private Texture ships5;
	private TextureRegion[] shipsTextures1;
	private TextureRegion[] shipsTextures2;
	private TextureRegion[] shipsTextures3;
	private TextureRegion[] shipsTextures4;
	private TextureRegion[] shipsTextures5;
	private Sprite[] shipsPriceSprites;
	private Sprite[] shipsBoughtSprites;
	private Image[] shipsPriceImage;
	public Image[] shipsBoughtImage;
	public Sprite[] shipsSprites;
	private Texture upgradesPrices;
	private TextureRegion[] upgradesPriceTextures;
	private Sprite[] upgradesPriceSprites;
	private Image[] upgradesPriceImages;
	private Image[] upgradeFullImages;
	private UpgradeBar[] upgradeBars = new UpgradeBar[6];
	private static final int[] UPGRADE_MAX = { 5, 5, 5, 3, 3, 5 };
	private Image[] upgradeEmptyImages;
	private Image padsControlsEN;
	private Image padsControlsES;
	private Image orbsEN;
	private Image itemsEN;
	private Image instEN;
	private Image instES;
	private Image orbsES;
	private Image itemsES;
	private Image iEnglish;
	private Image iEspanol;
	
	public Vector2 v1;
	public Vector2 v2;
	public Vector2 v3;
	public Vector2 v4;
	public Vector2 v5;
	public Vector2 v6;
	
	public float currentBgX;
	public float currentBgY;
	public long lastTimeBg;
	public int lightTime;
	public int musicTime;
	private final ShipPlume menuPlume = new ShipPlume();
	private final Lightning electricity = new Lightning();
	private float counterBaseY, scoreBaseY, mainCounterY, mainScoreY, titleY1, titleY2;
	private float musicFade;
	public long time;
	public int galaxySelected;
	public int forceFieldSelected;
	public int shipSelected;
	public int ships;
	public int galaxies;
	public int upgrades;
	public int games; 
	public int sound;
	public int vibration;
	public int language;
	public int controls;
	public int energyMeter;
	public int energyStart;
	public int shipSpeed;
	public int objectsDuration;
	public int objectsSlots;
	public int objectStart;
	public int bgLoaded;
	public int adHeight;
	public int l1;
	public int l2;
	public int[] galaxiesBought;
	public int[] shipsBought;
	public int[] redsPriceShips;
	public int[] bluesPriceShips;
	public int[] redsPriceGalaxies;
	public int[] bluesPriceGalaxies;
	public int[] upgradesBought;
	public int[] upgradesPrice;
	public boolean[] shipsNew;
	public boolean[] galaxiesNew; 
	public boolean[] upgradesNew; 
	public boolean boughtShip;
	public boolean boughtGalaxy;	
	public boolean boughtUpgrade;	
	public boolean canSelect;
	public boolean load;
	public boolean up1, up2, up3, up4, up5, up6; 
	public boolean firstMMS;
	public boolean drawCounters;
	public boolean drawTrace;
	
	public int spentBlues;
	public int spentReds;
	public int spentLifes;
	public int timePlaying;
	public int totalOrbs;
	public int totalBlues;
	public int totalReds;
	public int totalLifes;
	public int totalObjects;
	public int totalPoints;
	public int[] obj;

	public int soundInMenu;
	public int vibrationInMenu;
	public int controlsInMenu;
	
	public Sound ffChange;
	public Sound buttonSmall;
	public Sound buttonLarge;
	public Sound bought;
	public Sound cant;
	public Sound launch;
	public Sound pre;
	public Music music;

	public BitmapFont greysFontTitle;
	public BitmapFont greysFont;
	public BitmapFont bluesFont;
	public BitmapFont redsFont;
	public BitmapFont lifeFont;
	public int bestScore, redBalls, blueBalls, lifeBalls;
	private DatabaseCursor cursor;

	private Stage stageShips;
	private Table containerShips;
	private Table imgsShips;
	private ScrollPane scrollShips;

	private Stage stageGalaxies;
	private Table containerBgs;
	private Table imgsBgs;
	private ScrollPane scrollBgs;

	private Stage stageUpgrades;
	private Table containerUpg;
	private Table imgsUpg;
	private ScrollPane scrollUpg;

	private Stage stageStats;
	private Table containerSts;
	private Table imgsSts;
	private ScrollPane scrollSts;

	private Stage stageSettings;
	private Table containerSttngs;
	private Table imgsSttngs;
	private ScrollPane scrollSttngs;

	private Stage stageHelp;
	private Table containerHelp;
	private Table imgsHelp;
	private ScrollPane scrollHelp;
	
	Label gamesLabeln;
	Label totalPointsLabeln;
	Label avgPointsLabeln;
	Label totalOrbsLabeln;
	Label totalBluesLabeln;
	Label totalRedsLabeln;
	Label totalLifesLabeln;
	Label totalObjectsLabeln;
	Label spentBluesLabeln;
	Label spentRedsLabeln;
	Label spentLifesLabeln;
	Label timePlayingLabeln;
	Label obj0Labeln;
	Label obj1Labeln;
	Label obj2Labeln;
	Label obj3Labeln;
	Label obj4Labeln;
	Label obj5Labeln;
	Label obj6Labeln;
	Label obj7Labeln;
	Label obj8Labeln;
	Label obj9Labeln;
	
	public Skin skin;

	float ratioMotoG;
	float ratioV;
	float x;
	float y;
	Label sttngsLabel;
	Label soundLabel;
	Label vibrationLabel;
	Label controlsLabel;
	Label languageLabel;
	private Label itemsLabel;
	private Label orbsLabel;
	private Label padsTitleLabel;
	private Label tutoLabel;
	private Label upgLabel;
	private Label bgsLabel;
	private Label shipsLabel;
	private Label speedLabel;
	private Label energyLabel;
	private Label energyStartLabel;
	private Label objectsLabel;
	private Label objectsStartLabel;
	private Label objectsDurationLabel;
	private Label obj6Label;
	private Label obj5Label;
	private Label obj7Label;
	private Label obj8Label;
	private Label obj9Label;
	private Label obj3Label;
	private Label obj4Label;
	private Label obj2Label;
	private Label obj1Label;
	private Label obj0Label;
	private Label timePlayingLabel;
	private Label spentLifesLabel;
	private Label spentRedsLabel;
	private Label spentBluesLabel;
	private Label totalObjectsLabel;
	private Label totalLifesLabel;
	private Label totalRedsLabel;
	private Label totalBluesLabel;
	private Label totalOrbsLabel;
	private Label totalPointLabel;
	private Label avgPointsLabel;
	private Label gamesLabel;
	private Label stsLabel;
	private Label spentLabel;
	private Label spentLabeln;
	
	public MainMenuScreen(final Main gam) {
		
		game = gam;
		firstMMS = true;
		canSelect = true;
		
		if(Gdx.app.getType() == ApplicationType.Android){
			adHeight = game.getActionResolver().getAdHeight();
		}else
			adHeight = 0;
		

		tweenManager = new TweenManager();
		Tween.setCombinedAttributesLimit(4);
		Tween.registerAccessor(Sprite.class, new SpriteAccessor());
		Tween.registerAccessor(Image.class, new ImageAccessor());
		Tween.registerAccessor(BitmapFont.class, new BitmapFontAccessor());

		ratioMotoG = 0.5625f;
		ratioV = (float) Gdx.graphics.getWidth()/Gdx.graphics.getHeight();
		x = Math.min(Gdx.graphics.getWidth()/720f, Gdx.graphics.getHeight()/1280f);
		y = Math.min(Gdx.graphics.getWidth()/720f, Gdx.graphics.getHeight()/1280f);

		try{
			cursor = game.getDataBase().rawQuery("SELECT sound FROM seleccionsAndNumbers;");
			if(cursor != null && cursor.next())
				sound = Integer.valueOf(cursor.getInt(0));
			cursor = game.getDataBase().rawQuery("SELECT vibration FROM seleccionsAndNumbers;");
			if(cursor != null && cursor.next())
				vibration = Integer.valueOf(cursor.getInt(0));
			cursor = game.getDataBase().rawQuery("SELECT controls FROM seleccionsAndNumbers;");
			if(cursor != null && cursor.next())
				controls = Integer.valueOf(cursor.getInt(0));
			cursor = game.getDataBase().rawQuery("SELECT galaxySelected FROM seleccionsAndNumbers;");
			if(cursor != null && cursor.next())
				galaxySelected = Integer.valueOf(cursor.getInt(0));
			cursor = game.getDataBase().rawQuery("SELECT shipSelected FROM seleccionsAndNumbers;");
			if(cursor != null && cursor.next())
				shipSelected = Integer.valueOf(cursor.getInt(0));
			cursor = game.getDataBase().rawQuery("SELECT ffSelected FROM seleccionsAndNumbers;");
			if(cursor != null && cursor.next())
				forceFieldSelected = Integer.valueOf(cursor.getInt(0));
			cursor = game.getDataBase().rawQuery("SELECT ships FROM seleccionsAndNumbers;");
			if(cursor != null && cursor.next())
				ships = Integer.valueOf(cursor.getInt(0));
			cursor = game.getDataBase().rawQuery("SELECT galaxies FROM seleccionsAndNumbers;");
			if(cursor != null && cursor.next())
				galaxies = Integer.valueOf(cursor.getInt(0));
			cursor = game.getDataBase().rawQuery("SELECT upgrades FROM seleccionsAndNumbers;");
			if(cursor != null && cursor.next())
				upgrades = Integer.valueOf(cursor.getInt(0));
			
			cursor = game.getDataBase().rawQuery("SELECT language FROM seleccionsAndNumbers;");
			if(cursor != null && cursor.next())
				language = Integer.valueOf(cursor.getInt(0));
			cursor.close();
			
			cursor = game.getUpgradesDataBase().rawQuery("SELECT lifesTotal FROM upgrades;");
			if(cursor != null && cursor.next())
				energyMeter = Integer.valueOf(cursor.getInt(0));
			cursor = game.getUpgradesDataBase().rawQuery("SELECT lifesStarts FROM upgrades;");
			if(cursor != null && cursor.next())
				energyStart = Integer.valueOf(cursor.getInt(0));
			cursor = game.getUpgradesDataBase().rawQuery("SELECT shipSpeed FROM upgrades;");
			if(cursor != null && cursor.next())
				shipSpeed = Integer.valueOf(cursor.getInt(0));
			cursor = game.getUpgradesDataBase().rawQuery("SELECT objectsDuration FROM upgrades;");
			if(cursor != null && cursor.next())
				objectsDuration = Integer.valueOf(cursor.getInt(0));
			cursor = game.getUpgradesDataBase().rawQuery("SELECT objectsRoom FROM upgrades;");
			if(cursor != null && cursor.next())
				objectsSlots = Integer.valueOf(cursor.getInt(0));
			cursor = game.getUpgradesDataBase().rawQuery("SELECT objectsStarts FROM upgrades;");
			if(cursor != null && cursor.next())
				objectStart = Integer.valueOf(cursor.getInt(0));
			cursor.close();
		} catch (SQLiteGdxException e1) {
			e1.printStackTrace();
		}

		shipsNew = new boolean[75];
		for(int i = 0; i < 75; i++){
			shipsNew[i] = false;
		}
		galaxiesNew = new boolean[11];
		for(int i = 0; i < 11; i++){
			galaxiesNew[i] = false;
		}
		upgradesBought = new int[18];
		galaxiesBought = new int[11];
		shipsBought = new int[75];
		upgradesPrice = new int[18];
		redsPriceShips = new int[75];
		bluesPriceShips = new int[75];
		redsPriceGalaxies = new int[11];
		bluesPriceGalaxies = new int[11];
		try {
			cursor = game.getShipsDataBase().rawQuery("SELECT bought, reds, blues FROM ships;");
			for(int k = 0; k < 75; k++){
				if(cursor != null && cursor.next()){
					shipsBought[k] = Integer.valueOf(cursor.getInt(0));
					redsPriceShips[k] = Integer.valueOf(cursor.getInt(1));
					bluesPriceShips[k] = Integer.valueOf(cursor.getInt(2));
				}	
			}
			cursor = game.getGalaxiesDataBase().rawQuery("SELECT bought, reds, blues FROM galaxies;");
			for(int k = 0; k < 11; k++){
				if(cursor != null && cursor.next()){
					galaxiesBought[k] = Integer.valueOf(cursor.getInt(0));
					redsPriceGalaxies[k] = Integer.valueOf(cursor.getInt(1));
					bluesPriceGalaxies[k] = Integer.valueOf(cursor.getInt(2));
				}	
			}
			cursor = game.getUpgradesPriceDataBase().rawQuery("SELECT bought, lifeBalls, id FROM upgradesPrice;");
			for(int k = 0; k < 18; k++){
				if(cursor != null && cursor.next()){
					upgradesBought[Integer.valueOf(cursor.getInt(2))] = Integer.valueOf(cursor.getInt(0));
					upgradesPrice[Integer.valueOf(cursor.getInt(2))] = Integer.valueOf(cursor.getInt(1));
				}	
			}
			cursor = game.getDataBase().rawQuery("SELECT lifeBalls FROM seleccionsAndNumbers");
			if(cursor != null && cursor.next())
				lifeBalls = cursor.getInt(0);
			cursor.close();	

		}catch (SQLiteGdxException e) {
			e.printStackTrace();
		}

		//fonts
		bluesFont = game.assetManager.get("fonts/prototype64blueWhite.fnt", BitmapFont.class);
		redsFont = game.assetManager.get("fonts/prototype64redBlack.fnt", BitmapFont.class);
		greysFont = game.assetManager.get("fonts/prototype96grey2.fnt", BitmapFont.class);
		greysFontTitle = game.assetManager.get("fonts/prototype96grey.fnt", BitmapFont.class);
		lifeFont = game.assetManager.get("fonts/prototype64yellowOrange.fnt", BitmapFont.class);
		forceField = game.assetManager.get("res/shapes/forceFields.png", Texture.class);
		ffTextures = TextureRegion.split(forceField, 128, 128)[0];
		ffSprites = new Sprite[ffTextures.length];
		for(int i = 0; i < ffTextures.length; i++){
			ffSprites[i] = new Sprite(ffTextures[i]);
		}
		forceFieldGlows = game.assetManager.get("res/shapes/glows.png", Texture.class);
		ffgTextures = TextureRegion.split(forceFieldGlows, 160, 160)[0];
		ffgSprites = new Sprite[ffgTextures.length];
		for(int i = 0; i < ffgTextures.length; i++){
			ffgSprites[i] = new Sprite(ffgTextures[i]);
		}
		lightningBlue = game.assetManager.get("res/shapes/lightningBlue.png", Texture.class);
		lightBTextures = TextureRegion.split(lightningBlue, 128, 128)[0];
		lightBSprites = new Sprite[lightBTextures.length];
		for(int i = 0; i < lightBTextures.length; i++){
			lightBSprites[i] = new Sprite(lightBTextures[i]);
		}
		lightningRed = game.assetManager.get("res/shapes/lightningRed.png", Texture.class);
		lightRTextures = TextureRegion.split(lightningRed, 128, 128)[0];
		lightRSprites = new Sprite[lightRTextures.length];
		for(int i = 0; i < lightRTextures.length; i++){
			lightRSprites[i] = new Sprite(lightRTextures[i]);
		}
		//galaxies bought
		bgsBought1 = game.assetManager.get("res/bgs/bgsBought1.png", Texture.class);
		bgsBought2 = game.assetManager.get("res/bgs/bgsBought2.png", Texture.class);
		bgsBoughtTextures1 = TextureRegion.split(bgsBought1, 220, 298)[0];
		bgsBoughtTextures2 = TextureRegion.split(bgsBought2, 220, 298)[0];
		bgsBoughtSprite = new Sprite[bgsBoughtTextures1.length + bgsBoughtTextures2.length];
		bgsBoughtImage = new Image[bgsBoughtSprite.length];
		for(int i = 0; i < bgsBoughtSprite.length; i++){
			if(i < 5){
				bgsBoughtSprite[i] = new Sprite(bgsBoughtTextures1[i]);
				bgsBoughtImage[i] = new Image(bgsBoughtSprite[i]);
			}else{
				bgsBoughtSprite[i] = new Sprite(bgsBoughtTextures2[i-5]);
				bgsBoughtImage[i] = new Image(bgsBoughtSprite[i]);
			}
			bgsBoughtImage[i].setSize(fx(bgsBoughtImage[i].getWidth()), 
					fy(bgsBoughtImage[i].getHeight()));
		}
		// galaxies price
		bgsPrice1 = game.assetManager.get("res/bgs/bgsPrice1.png", Texture.class);
		bgsPrice2 = game.assetManager.get("res/bgs/bgsPrice2.png", Texture.class);
		bgsPriceTextures1 = TextureRegion.split(bgsPrice1, 220, 298)[0];
		bgsPriceTextures2 = TextureRegion.split(bgsPrice2, 220, 298)[0];
		bgsPriceSprite = new Sprite[bgsPriceTextures1.length + bgsPriceTextures2.length];
		bgsPriceImage = new Image[bgsPriceSprite.length];
		for(int i = 0; i < bgsPriceSprite.length; i++){
			if(i < 5){
				bgsPriceSprite[i] = new Sprite(bgsPriceTextures1[i]);
				bgsPriceImage[i] = new Image(bgsPriceSprite[i]);
			}else{

				bgsPriceSprite[i] = new Sprite(bgsPriceTextures2[i-5]);
				bgsPriceImage[i] = new Image(bgsPriceSprite[i]);
			}
			bgsPriceImage[i].setSize(fx(bgsPriceImage[i].getWidth()), 
					fy(bgsPriceImage[i].getHeight()));
		}
		//galaxies to use
		bgs = new Sprite[20];
		loadFirstBg();

		//buttons
		bSoundOn = new Sprite(game.assetManager.get("res/gui/bSoundOn120.png", Texture.class));
		bSoundOff = new Sprite(game.assetManager.get("res/gui/bSoundOff120.png", Texture.class));
		bVibrationOn = new Sprite(game.assetManager.get("res/gui/bVibrationOn120.png", Texture.class));
		bVibrationOff = new Sprite(game.assetManager.get("res/gui/bVibrationOff120.png", Texture.class));
		bControlsOn = new Sprite(game.assetManager.get("res/gui/bControlsOn120.png", Texture.class));
		bControlsOff = new Sprite(game.assetManager.get("res/gui/bControlsOff120.png", Texture.class));
		bStats = new Sprite(game.assetManager.get("res/gui/bStats.png", Texture.class));
		bHelp = new Sprite(game.assetManager.get("res/gui/bTutorial.png", Texture.class));
		bShips = new Sprite(game.assetManager.get("res/gui/bShips.png", Texture.class));
		bGalaxies = new Sprite(game.assetManager.get("res/gui/bGalaxies.png", Texture.class));
		bGo = new Sprite(game.assetManager.get("res/gui/bGo.png", Texture.class));
		bExit = new Sprite(game.assetManager.get("res/gui/bExit.png", Texture.class));
		bBack = new Sprite(game.assetManager.get("res/gui/bBack.png", Texture.class));
		bTrophy = new Sprite(game.assetManager.get("res/gui/bTrophy.png", Texture.class));
		bSettings = new Sprite(game.assetManager.get("res/gui/bSettings.png", Texture.class));
		bLeaderboard = new Sprite(game.assetManager.get("res/gui/bLeaderboard.png", Texture.class));
		bUpgrades = new Sprite(game.assetManager.get("res/gui/bUpgrades.png", Texture.class));
		bNo = new Sprite(game.assetManager.get("res/gui/bNo.png", Texture.class));
		bYes = new Sprite(game.assetManager.get("res/gui/bYes.png", Texture.class));
		bQuestionEN = new Sprite(game.assetManager.get("res/gui/bQuestionEN.png", Texture.class));
		bQuestionES = new Sprite(game.assetManager.get("res/gui/bQuestionES.png", Texture.class));
		bEspanol = new Sprite(game.assetManager.get("res/gui/bEspanol.png", Texture.class));
		bEnglish = new Sprite(game.assetManager.get("res/gui/bEnglish.png", Texture.class));
		upgradeEmpty = new Sprite(game.assetManager.get("res/hud/upgradeBarEmpty.png", Texture.class));
		upgradeFull = new Sprite(game.assetManager.get("res/hud/upgradeBarFull.png", Texture.class));
		cover = new Sprite(game.assetManager.get("res/hud/padVertical.png", Texture.class));
		
		giftizLogo = new Sprite(game.assetManager.get("res/giftiz/giftiz_logo.png", Texture.class));
		giftizLogoWarning = new Sprite(game.assetManager.get("res/giftiz/giftiz_logo_warning.png", Texture.class));
		giftizLogoBadge = new Sprite(game.assetManager.get("res/giftiz/giftiz_logo_badge.png", Texture.class));

		blueCounter = new Sprite(game.assetManager.get("res/shapes/blueCounter.png", Texture.class));
		redCounter = new Sprite(game.assetManager.get("res/shapes/redCounter.png", Texture.class));
		lifeCounter = new Sprite(game.assetManager.get("res/shapes/lifeCounter.png", Texture.class));

		
		//ships bought
		shipsBought1 = game.assetManager.get("res/ships/shipsBought1.png", Texture.class);
		shipsBought2 = game.assetManager.get("res/ships/shipsBought2.png", Texture.class);
		shipsBought3 = game.assetManager.get("res/ships/shipsBought3.png", Texture.class);
		shipsBought4 = game.assetManager.get("res/ships/shipsBought4.png", Texture.class);
		shipsBought5 = game.assetManager.get("res/ships/shipsBought5.png", Texture.class);
		shipsBought6 = game.assetManager.get("res/ships/shipsBought6.png", Texture.class);
		shipsBought7 = game.assetManager.get("res/ships/shipsBought7.png", Texture.class);
		shipsBought75 = game.assetManager.get("res/ships/shipsBought75.png", Texture.class);
		shipsBoughtTextures1 = TextureRegion.split(shipsBought1, 149, 149)[0];
		shipsBoughtTextures2 = TextureRegion.split(shipsBought2, 149, 149)[0];
		shipsBoughtTextures3 = TextureRegion.split(shipsBought3, 149, 149)[0];
		shipsBoughtTextures4 = TextureRegion.split(shipsBought4, 149, 149)[0];
		shipsBoughtTextures5 = TextureRegion.split(shipsBought5, 149, 149)[0];
		shipsBoughtTextures6 = TextureRegion.split(shipsBought6, 149, 149)[0];
		shipsBoughtTextures7 = TextureRegion.split(shipsBought7, 149, 149)[0];
		shipsBoughtTextures75 = TextureRegion.split(shipsBought75, 149, 149)[0];

		shipsBoughtSprites = new Sprite[75];
		for(int i = 0; i < 75; i++){
			if(i < 10)
				shipsBoughtSprites[i] = new Sprite(shipsBoughtTextures1[i]);
			else if(i >= 10 && i < 20)
				shipsBoughtSprites[i] = new Sprite(shipsBoughtTextures2[i-10]);
			else if(i >= 20 && i < 30)
				shipsBoughtSprites[i] = new Sprite(shipsBoughtTextures3[i-20]);
			else if(i >= 30 && i < 40)
				shipsBoughtSprites[i] = new Sprite(shipsBoughtTextures4[i-30]);
			else if(i >= 40 && i < 50)
				shipsBoughtSprites[i] = new Sprite(shipsBoughtTextures5[i-40]);	
			else if(i >= 50 && i < 60)
				shipsBoughtSprites[i] = new Sprite(shipsBoughtTextures6[i-50]);	
			else if(i >= 60 && i < 70)
				shipsBoughtSprites[i] = new Sprite(shipsBoughtTextures7[i-60]);	
			else if(i >= 70)
				shipsBoughtSprites[i] = new Sprite(shipsBoughtTextures75[i-70]);	
		}
		shipsBoughtImage = new Image[75];
		for(int i = 0; i < 75; i++){
			shipsBoughtImage[i] = new Image(shipsBoughtSprites[i]);
			shipsBoughtImage[i].setSize((fx(shipsBoughtImage[i].getWidth()) + fy(shipsBoughtImage[i].getHeight()))/2, 
					(fx(shipsBoughtImage[i].getWidth()) + fy(shipsBoughtImage[i].getHeight()))/2);
		}

		//ships price
		shipsPrice1 = game.assetManager.get("res/ships/shipsPrice1.png", Texture.class);
		shipsPrice2 = game.assetManager.get("res/ships/shipsPrice2.png", Texture.class);
		shipsPrice3 = game.assetManager.get("res/ships/shipsPrice3.png", Texture.class);
		shipsPrice4 = game.assetManager.get("res/ships/shipsPrice4.png", Texture.class);
		shipsPrice5 = game.assetManager.get("res/ships/shipsPrice5.png", Texture.class);
		shipsPrice6 = game.assetManager.get("res/ships/shipsPrice6.png", Texture.class);
		shipsPrice7 = game.assetManager.get("res/ships/shipsPrice7.png", Texture.class);
		shipsPrice75 = game.assetManager.get("res/ships/shipsPrice75.png", Texture.class);
		shipsPriceTextures1 = TextureRegion.split(shipsPrice1, 149, 149)[0];
		shipsPriceTextures2 = TextureRegion.split(shipsPrice2, 149, 149)[0];
		shipsPriceTextures3 = TextureRegion.split(shipsPrice3, 149, 149)[0];
		shipsPriceTextures4 = TextureRegion.split(shipsPrice4, 149, 149)[0];
		shipsPriceTextures5 = TextureRegion.split(shipsPrice5, 149, 149)[0];
		shipsPriceTextures6 = TextureRegion.split(shipsPrice6, 149, 149)[0];
		shipsPriceTextures7 = TextureRegion.split(shipsPrice7, 149, 149)[0];
		shipsPriceTextures75 = TextureRegion.split(shipsPrice75, 149, 149)[0];
		shipsPriceSprites = new Sprite[75];
		for(int i = 0; i < 75; i++){
			if(i < 10)
				shipsPriceSprites[i] = new Sprite(shipsPriceTextures1[i]);
			else if(i >= 10 && i < 20)
				shipsPriceSprites[i] = new Sprite(shipsPriceTextures2[i-10]);
			else if(i >= 20 && i < 30)
				shipsPriceSprites[i] = new Sprite(shipsPriceTextures3[i-20]);
			else if(i >= 30 && i < 40)
				shipsPriceSprites[i] = new Sprite(shipsPriceTextures4[i-30]);
			else if(i >= 40 && i < 50)
				shipsPriceSprites[i] = new Sprite(shipsPriceTextures5[i-40]);
			else if(i >= 50 && i < 60)
				shipsPriceSprites[i] = new Sprite(shipsPriceTextures6[i-50]);
			else if(i >= 60 && i < 70)
				shipsPriceSprites[i] = new Sprite(shipsPriceTextures7[i-60]);
			else if(i >= 70)
				shipsPriceSprites[i] = new Sprite(shipsPriceTextures75[i-70]);	
		}
		shipsPriceImage = new Image[75];
		for(int i = 0; i < 75; i++){
			shipsPriceImage[i] = new Image(shipsPriceSprites[i]);
			shipsPriceImage[i].setSize((fx(shipsPriceImage[i].getWidth()) + fy(shipsPriceImage[i].getHeight()))/2, 
					(fx(shipsPriceImage[i].getWidth()) + fy(shipsPriceImage[i].getHeight()))/2);
		}

		//ships to use
		ships1 = game.assetManager.get("res/ships/ships1.png", Texture.class);
		ships2 = game.assetManager.get("res/ships/ships2.png", Texture.class);
		ships3 = game.assetManager.get("res/ships/ships3.png", Texture.class);
		ships4 = game.assetManager.get("res/ships/ships4.png", Texture.class);
		ships5 = game.assetManager.get("res/ships/ships5.png", Texture.class);

		shipsTextures1 = TextureRegion.split(ships1, 128, 128)[0];
		shipsTextures2 = TextureRegion.split(ships2, 128, 128)[0];
		shipsTextures3 = TextureRegion.split(ships3, 128, 128)[0];
		shipsTextures4 = TextureRegion.split(ships4, 128, 128)[0];
		shipsTextures5 = TextureRegion.split(ships5, 128, 128)[0];

		shipsSprites = new Sprite[shipsTextures1.length*5];
		for(int i = 0; i < shipsTextures1.length*5; i++){
			if(i < 15)
				shipsSprites[i] = new Sprite(shipsTextures1[i]);
			else if(i >= 15 && i < 30)
				shipsSprites[i] = new Sprite(shipsTextures2[i-15]);
			else if(i >= 30 && i < 45)
				shipsSprites[i] = new Sprite(shipsTextures3[i-30]);
			else if(i >= 45 && i < 60)
				shipsSprites[i] = new Sprite(shipsTextures4[i-45]);
			else if(i >= 60)
				shipsSprites[i] = new Sprite(shipsTextures5[i-60]);
		}

		padsControlsEN = new Image (new Sprite(game.assetManager.get("res/objects/padsControls.png", Texture.class)));
		padsControlsEN.setSize(fx(padsControlsEN.getWidth()), fy(padsControlsEN.getHeight()));
		padsControlsES = new Image (new Sprite(game.assetManager.get("res/objects/padsControlsEsp.png", Texture.class)));
		padsControlsES.setSize(fx(padsControlsES.getWidth()), fy(padsControlsES.getHeight()));
		orbsEN = new Image (new Sprite(game.assetManager.get("res/objects/orbs.png", Texture.class)));
		orbsEN.setSize(fx(orbsEN.getWidth()), fy(orbsEN.getHeight()));
		orbsES = new Image (new Sprite(game.assetManager.get("res/objects/orbsEsp.png", Texture.class)));
		orbsES.setSize(fx(orbsES.getWidth()), fy(orbsES.getHeight()));
		itemsEN = new Image (new Sprite(game.assetManager.get("res/objects/items.png", Texture.class)));
		itemsEN.setSize(fx(itemsEN.getWidth()), fy(itemsEN.getHeight()));
		itemsES = new Image (new Sprite(game.assetManager.get("res/objects/itemsEsp.png", Texture.class)));
		itemsES.setSize(fx(itemsES.getWidth()), fy(itemsES.getHeight()));
		instEN = new Image (new Sprite(game.assetManager.get("res/objects/inst.png", Texture.class)));
		instEN.setSize(fx(instEN.getWidth()), fy(instEN.getHeight()));
		instES = new Image (new Sprite(game.assetManager.get("res/objects/instEsp.png", Texture.class)));
		instES.setSize(fx(instES.getWidth()), fy(instES.getHeight()));
		
		//upgrades prices
		upgradesPrices = game.assetManager.get("res/upgrades/upgradesPrice.png", Texture.class);
		upgradesPriceTextures = TextureRegion.split(upgradesPrices, 149, 149)[0];
		upgradesPriceSprites = new Sprite[upgradesPriceTextures.length];
		for(int i = 0; i < upgradesPriceTextures.length; i++){
			upgradesPriceSprites[i] = new Sprite(upgradesPriceTextures[i]);
		}
		
		upgradesPriceImages = new Image[18];
		upgradesPriceImages[0] = new Image(upgradesPriceSprites[0]);
		upgradesPriceImages[1] = new Image(upgradesPriceSprites[1]);
		upgradesPriceImages[2] = new Image(upgradesPriceSprites[2]);
		upgradesPriceImages[3] = new Image(upgradesPriceSprites[3]);
		upgradesPriceImages[4] = new Image(upgradesPriceSprites[2]);
		upgradesPriceImages[5] = new Image(upgradesPriceSprites[3]);
		upgradesPriceImages[6] = new Image(upgradesPriceSprites[1]);
		upgradesPriceImages[7] = new Image(upgradesPriceSprites[2]);
		upgradesPriceImages[8] = new Image(upgradesPriceSprites[3]);
		upgradesPriceImages[9] = new Image(upgradesPriceSprites[2]);
		upgradesPriceImages[10] = new Image(upgradesPriceSprites[3]);
		upgradesPriceImages[11] = new Image(upgradesPriceSprites[2]);
		upgradesPriceImages[12] = new Image(upgradesPriceSprites[3]);
		upgradesPriceImages[13] = new Image(upgradesPriceSprites[4]);
		upgradesPriceImages[14] = new Image(upgradesPriceSprites[0]);
		upgradesPriceImages[15] = new Image(upgradesPriceSprites[1]);
		upgradesPriceImages[16] = new Image(upgradesPriceSprites[2]);
		upgradesPriceImages[17] = new Image(upgradesPriceSprites[3]);
		
		for(int i = 0; i < 18; i++){
			upgradesPriceImages[i].setSize((fx(upgradesPriceImages[i].getWidth()) + fy(upgradesPriceImages[i].getHeight()))/2, 
					(fx(upgradesPriceImages[i].getWidth()) + fy(upgradesPriceImages[i].getHeight()))/2);
			upgradesPriceImages[i].setName(String.valueOf(i));
		}

		//audio
		ffChange = game.assetManager.get("audio/change2.ogg", Sound.class);
		buttonSmall = game.assetManager.get("audio/buttonSmall.ogg", Sound.class);
		buttonLarge = game.assetManager.get("audio/buttonLarge.ogg", Sound.class);
		bought = game.assetManager.get("audio/bought.ogg", Sound.class);
		cant = game.assetManager.get("audio/select.ogg", Sound.class);
		launch = game.assetManager.get("audio/launch.ogg", Sound.class);
		pre = game.assetManager.get("audio/pre.ogg", Sound.class);
		music = game.assetManager.get("audio/music.ogg", Music.class);

		//set up variables		
		giftizLogo.setSize(fx(giftizLogo.getWidth()), fy(giftizLogo.getHeight()));
		giftizLogoWarning.setSize(fx(giftizLogoWarning.getWidth()), fy(giftizLogoWarning.getHeight()));
		giftizLogoBadge.setSize(fx(giftizLogoBadge.getWidth()), fy(giftizLogoBadge.getHeight()));
		
		bTrophy.setSize(fx(bTrophy.getWidth()), fy(bTrophy.getHeight()));
		bSettings.setSize(fx(bSettings.getWidth()), fy(bSettings.getHeight()));
		bLeaderboard.setSize(fx(bLeaderboard.getWidth()), fy(bLeaderboard.getHeight()));
		bUpgrades.setSize(fx(bUpgrades.getWidth()), fy(bUpgrades.getHeight()));
		bGo.setSize(fx(bGo.getWidth()), fy(bGo.getHeight()));
		bExit.setSize(fx(bExit.getWidth()), fy(bExit.getHeight()));
		bVibrationOn.setSize((fx(bVibrationOn.getWidth()) + fy(bVibrationOn.getHeight()))/2, (fx(bVibrationOn.getWidth()) + fy(bVibrationOn.getHeight()))/2);
		bVibrationOff.setSize((fx(bVibrationOff.getWidth()) + fy(bVibrationOff.getHeight()))/2, (fx(bVibrationOff.getWidth()) + fy(bVibrationOff.getHeight()))/2);
		bControlsOn.setSize((fx(bControlsOn.getWidth()) + fy(bControlsOn.getHeight()))/2, (fx(bControlsOn.getWidth()) + fy(bControlsOn.getHeight()))/2);
		bControlsOff.setSize((fx(bControlsOff.getWidth()) + fy(bControlsOff.getHeight()))/2, (fx(bControlsOff.getWidth()) + fy(bControlsOff.getHeight()))/2);
		bStats.setSize((fx(bStats.getWidth()) + fy(bStats.getHeight()))/2, (fx(bStats.getWidth()) + fy(bStats.getHeight()))/2);
		bHelp.setSize((fx(bHelp.getWidth()) + fy(bHelp.getHeight()))/2, (fx(bHelp.getWidth()) + fy(bHelp.getHeight()))/2);
		bShips.setSize((fx(bShips.getWidth()) + fy(bShips.getHeight()))/2, (fx(bShips.getWidth()) + fy(bShips.getHeight()))/2);
		bGalaxies.setSize((fx(bGalaxies.getWidth()) + fy(bGalaxies.getHeight()))/2, (fx(bGalaxies.getWidth()) + fy(bGalaxies.getHeight()))/2);
		bSoundOn.setSize((fx(bSoundOn.getWidth()) + fy(bSoundOn.getHeight()))/2, (fx(bSoundOn.getWidth()) + fy(bSoundOn.getHeight()))/2);
		bSoundOff.setSize((fx(bSoundOff.getWidth()) + fy(bSoundOff.getHeight()))/2, (fx(bSoundOff.getWidth()) + fy(bSoundOff.getHeight()))/2);
		bBack.setSize((fx(bBack.getWidth()) + fy(bBack.getHeight()))/2, (fy(bBack.getWidth()) + fy(bBack.getHeight()))/2);
		bYes.setSize((fx(bYes.getWidth()) + fy(bYes.getHeight()))/2, (fx(bYes.getWidth()) + fy(bYes.getHeight()))/2);
		bNo.setSize((fx(bNo.getWidth()) + fy(bNo.getHeight()))/2, (fx(bNo.getWidth()) + fy(bNo.getHeight()))/2);
		bQuestionEN.setSize(fx(bQuestionEN.getWidth()), fy(bQuestionEN.getHeight()));
		bQuestionES.setSize(fx(bQuestionES.getWidth()), fy(bQuestionES.getHeight()));
		bEspanol.setSize(fx(bEspanol.getWidth()), fy(bEspanol.getHeight()));
		bEnglish.setSize(fx(bEnglish.getWidth()), fy(bEnglish.getHeight()));
		upgradeEmpty.setSize(fx(upgradeEmpty.getWidth()), fy(upgradeEmpty.getHeight()));
		upgradeFull.setSize(fx(upgradeFull.getWidth()), fy(upgradeFull.getHeight()));
		blueCounter.setSize((fx(blueCounter.getWidth()) + fy(blueCounter.getHeight()))/2, (fx(blueCounter.getWidth()) + fy(blueCounter.getHeight()))/2);
		redCounter.setSize((fx(redCounter.getWidth()) + fy(redCounter.getHeight()))/2, (fx(redCounter.getWidth()) + fy(redCounter.getHeight()))/2);
		lifeCounter.setSize((fx(lifeCounter.getWidth()) + fy(lifeCounter.getHeight()))/2, (fx(lifeCounter.getWidth()) + fy(lifeCounter.getHeight()))/2);
		greysFontTitle.getData().setScale(x, y);
		bluesFont.getData().setScale(x, y);
		redsFont.getData().setScale(x, y);
		lifeFont.getData().setScale(x, y);
		cover.setSize(Gdx.graphics.getWidth()*1.5f, Gdx.graphics.getHeight()*1.5f);
		cover.setColor(0.12f, 0.12f, 0.12f, 0.75f);
		cover.setPosition(-Gdx.graphics.getWidth()*0.25f, -Gdx.graphics.getHeight()*0.25f);
		
		//button images
		iSoundOn = new Image(bSoundOn);
		iSoundOff = new Image(bSoundOff);
		iVibrationOn = new Image(bVibrationOn);
		iVibrationOff= new Image(bVibrationOff);
		iControlsOn = new Image(bControlsOn);
		iControlsOff = new Image(bControlsOff);
		iEspanol = new Image(bEspanol);
		iEnglish = new Image(bEnglish);
		
		upgradeFullImages = new Image[26];
		upgradeEmptyImages = new Image[26];
		for(int i = 0; i < 26; i++){
			upgradeFullImages[i] = new Image(upgradeFull);
			upgradeEmptyImages[i] = new Image(upgradeEmpty);
		}
		
		iSoundOn.setSize((fx(iSoundOn.getWidth()) + fy(iSoundOn.getHeight()))/2, (fx(iSoundOn.getWidth()) + fy(iSoundOn.getHeight()))/2);
		iSoundOff.setSize((fx(iSoundOff.getWidth()) + fy(iSoundOff.getHeight()))/2, (fx(iSoundOff.getWidth()) + fy(iSoundOff.getHeight()))/2);
		iVibrationOn.setSize((fx(iVibrationOn.getWidth()) + fy(iVibrationOn.getHeight()))/2, (fx(iVibrationOn.getWidth()) + fy(iVibrationOn.getHeight()))/2);
		iVibrationOff.setSize((fx(iVibrationOff.getWidth()) + fy(iVibrationOff.getHeight()))/2, (fx(iVibrationOff.getWidth()) + fy(iVibrationOff.getHeight()))/2);
		iControlsOn.setSize((fx(iControlsOn.getWidth()) + fy(iControlsOn.getHeight()))/2, (fx(iControlsOn.getWidth()) + fy(iControlsOn.getHeight()))/2);
		iControlsOff.setSize((fx(iControlsOff.getWidth()) + fy(iControlsOff.getHeight()))/2, (fx(iControlsOff.getWidth()) + fy(iControlsOff.getHeight()))/2);
		iEnglish.setSize(fx(iEnglish.getWidth()), fy(iEnglish.getHeight()));
		iEspanol.setSize(fx(iEspanol.getWidth()), fy(iEspanol.getHeight()));
		
		counterBaseY = Gdx.graphics.getHeight()/8 + 3*adHeight/4;
		blueCounter.setPosition(Gdx.graphics.getWidth()/2 - 1.25f*blueCounter.getWidth(), Gdx.graphics.getHeight()/8 + 3*adHeight/4);
		redCounter.setPosition(Gdx.graphics.getWidth()/2 + 0.25f*redCounter.getWidth(), Gdx.graphics.getHeight()/8 + 3*adHeight/4);
		lifeCounter.setPosition(Gdx.graphics.getWidth()/2 - (1.25f*lifeCounter.getWidth() + com.spaceship.handlers.FontUtil.bounds(lifeFont, String.valueOf(lifeBalls)).width)/2,
				Gdx.graphics.getHeight()/8 + 3*adHeight/4);

		bQuestionEN.setPosition(Gdx.graphics.getWidth()/2 - bQuestionEN.getWidth()/2, Gdx.graphics.getHeight()/2 + (bQuestionEN.getHeight() + 4*bNo.getHeight()/3)/2);
		bQuestionES.setPosition(Gdx.graphics.getWidth()/2 - bQuestionES.getWidth()/2, Gdx.graphics.getHeight()/2 + (bQuestionES.getHeight() + 4*bNo.getHeight()/3)/2);
		bYes.setPosition(Gdx.graphics.getWidth()/2 - 1.75f*bYes.getWidth(), bQuestionEN.getY() - 4*bYes.getHeight()/3);
		bNo.setPosition(Gdx.graphics.getWidth()/2 + 1.75f*bNo.getWidth() - bNo.getWidth(), bQuestionEN.getY() - 4*bNo.getHeight()/3);
		
		for(int g = 0; g < bgs.length ; g++)
			if(bgs[g] != null)
				bgs[g].setSize(Gdx.graphics.getWidth(), bgs[g].getHeight()*y);
		currentBgX = bgs[galaxySelected*2+1].getWidth();
		currentBgY = (bgs[galaxySelected*2].getHeight() + bgs[galaxySelected*2+1].getHeight())/3;

		skin = new Skin(Gdx.files.internal("ui/menuSkin.json"), new TextureAtlas("ui/atlas.pack"));
		
		obj = new int[10];
		
		shadow = 0;
		startTrace = false;
		
//		loadDataBases();
		
		setUpShipsMenu();
		setUpGalaxiesMenu();		
		setUpUpgradesMenu();	
		setUpStatsMenu();	
		setUpSettingsMenu();
		setUpHelpMenu();
			
	}

	@Override
	public void show(){

		if(firstMMS){
			firstMMS = false;
			game.setScreen(game.getPS());
		}
		
		if(Gdx.app.getType().equals(ApplicationType.Android))
			g = game.getActionResolver().updateButtonImage();

		loadDataBases();
		updateStatsMenu();
		updateSettingsMenuSpent();

		music.setLooping(true);
		musicFade = 0f;
		updateMusicVolume();
		music.play();
		
		if(Gdx.app.getType().equals(ApplicationType.Android) && !game.getActionResolver().getSignedIn() && bestScore == 0)
			Timer.schedule(new Task(){
				@Override
				public void run() {
					game.getActionResolver().logIn();
				}
			}, 0.30f);
		state = STANDBY;

		canSelect = true;
		boughtShip = false;
		boughtGalaxy = false;
		boughtUpgrade = false;
		drawCounters = true;
		drawTrace = true;
		
		up1 = false;
		up2 = false;
		up3 = false;
		up4 = false;
		up5 = false;
		up6 = false;
		
		load = false;
		
		time = TimeUtils.nanoTime();
		
		if(Gdx.app.getType().equals(ApplicationType.Android) && game.getActionResolver().getSignedIn()){
			game.getActionResolver().submitScore(bestScore);
			if (bestScore >= 25) game.getActionResolver().unlockAchievement("CgkIrLf9wZgEEAIQCQ");
			if (bestScore >= 50) game.getActionResolver().unlockAchievement("CgkIrLf9wZgEEAIQCg");
			if (bestScore >= 75) game.getActionResolver().unlockAchievement("CgkIrLf9wZgEEAIQCw");
			if (bestScore >= 100) game.getActionResolver().unlockAchievement("CgkIrLf9wZgEEAIQDA");
			if (bestScore >= 200) game.getActionResolver().unlockAchievement("CgkIrLf9wZgEEAIQDQ");
			if (bestScore >= 300) game.getActionResolver().unlockAchievement("CgkIrLf9wZgEEAIQDg");
			if (ships >= 1) game.getActionResolver().unlockAchievement("CgkIrLf9wZgEEAIQAA");
			if (ships >= 5) game.getActionResolver().unlockAchievement("CgkIrLf9wZgEEAIQAQ");
			if (ships >= 10) game.getActionResolver().unlockAchievement("CgkIrLf9wZgEEAIQAg");
			if (ships >= 25) game.getActionResolver().unlockAchievement("CgkIrLf9wZgEEAIQAw");
			if (ships >= 50) game.getActionResolver().unlockAchievement("CgkIrLf9wZgEEAIQBA");
			if (ships >= 74) game.getActionResolver().unlockAchievement("CgkIrLf9wZgEEAIQBQ");
			if (galaxies >= 1) game.getActionResolver().unlockAchievement("CgkIrLf9wZgEEAIQBg");
			if (galaxies >= 5) game.getActionResolver().unlockAchievement("CgkIrLf9wZgEEAIQBw");
			if (galaxies >= 9) game.getActionResolver().unlockAchievement("CgkIrLf9wZgEEAIQCA");
			if (games >= 1) game.getActionResolver().unlockAchievement("CgkIrLf9wZgEEAIQEQ");
			if (games >= 10) game.getActionResolver().unlockAchievement("CgkIrLf9wZgEEAIQEg");
			if (games >= 25) game.getActionResolver().unlockAchievement("CgkIrLf9wZgEEAIQEw");
			if (games >= 50) game.getActionResolver().unlockAchievement("CgkIrLf9wZgEEAIQFA");
			if (games >= 100) game.getActionResolver().unlockAchievement("CgkIrLf9wZgEEAIQFQ");
			if (games >= 500) game.getActionResolver().unlockAchievement("CgkIrLf9wZgEEAIQFg");
		}
		
		startTrace = true;

		// set up ships
		ffSprites[0].setPosition((Gdx.graphics.getWidth())/2 - ffSprites[0].getScaleX()*ffSprites[0].getWidth()/2, Gdx.graphics.getHeight()/2 - ffSprites[0].getScaleY()*ffSprites[0].getHeight()/2 + 2*adHeight/3 - adHeight/3);
		ffSprites[1].setPosition(Gdx.graphics.getWidth()/2 - ffSprites[1].getScaleX()*ffSprites[1].getWidth()/2, Gdx.graphics.getHeight()/2 - ffSprites[1].getScaleY()*ffSprites[1].getHeight()/2 + 2*adHeight/3 - adHeight/3);
		for(int h = 0; h < lightBSprites.length; h++){
			lightBSprites[h].setPosition(Gdx.graphics.getWidth()/2 - lightBSprites[h].getScaleX()*lightBSprites[h].getWidth()/2, Gdx.graphics.getHeight()/2 - lightBSprites[h].getScaleY()*lightBSprites[h].getHeight()/2 + 2*adHeight/3 - adHeight/3);
			lightRSprites[h].setPosition(Gdx.graphics.getWidth()/2 - lightRSprites[h].getScaleX()*lightRSprites[h].getWidth()/2, Gdx.graphics.getHeight()/2 - lightRSprites[h].getScaleY()*lightRSprites[h].getHeight()/2 + 2*adHeight/3 - adHeight/3);
		}
		ffgSprites[0].setPosition((Gdx.graphics.getWidth())/2 - ffgSprites[0].getScaleX()*ffgSprites[0].getWidth()/2, Gdx.graphics.getHeight()/2 - ffgSprites[0].getScaleY()*ffgSprites[0].getHeight()/2 + 2*adHeight/3 - adHeight/3);
		ffgSprites[1].setPosition(Gdx.graphics.getWidth()/2 - ffgSprites[1].getScaleX()*ffgSprites[1].getWidth()/2, Gdx.graphics.getHeight()/2 - ffgSprites[1].getScaleY()*ffgSprites[1].getHeight()/2 + 2*adHeight/3 - adHeight/3);
		ffgSprites[1].setSize(160, 160);
		for(int k = 0; k < shipsBoughtImage.length; k++){
			shipsSprites[k].setSize((fx(shipsSprites[k].getWidth()) + fy(shipsSprites[k].getHeight()))/2, (fx(shipsSprites[k].getWidth()) + fy(shipsSprites[k].getHeight()))/2);
			shipsSprites[k].setPosition(Gdx.graphics.getWidth()/2 - shipsSprites[k].getWidth()/2, Gdx.graphics.getHeight()/2 - shipsSprites[k].getHeight()/2 + adHeight/3 - adHeight/3);
			shipsSprites[k].setColor(1f, 1f, 1f, 0f);
		}
		shipsSprites[shipSelected].setColor(1f, 1f, 1f, 1f);
		
		//set positions for buttons in MMS
		bSettings.setPosition(shipsSprites[shipSelected].getX() + shipsSprites[shipSelected].getWidth(), 
				shipsSprites[shipSelected].getY() + 1.5f*shipsSprites[shipSelected].getHeight() + adHeight/3);
		bGalaxies.setPosition(shipsSprites[shipSelected].getX() - shipsSprites[shipSelected].getWidth()/1.75f, 
				shipsSprites[shipSelected].getY() - shipsSprites[shipSelected].getHeight() + adHeight/3);
		bShips.setPosition(shipsSprites[shipSelected].getX() - 1.25f*shipsSprites[shipSelected].getWidth(), 
				shipsSprites[shipSelected].getY() + 0.25f*shipsSprites[shipSelected].getHeight() + adHeight/3);
		bUpgrades.setPosition(shipsSprites[shipSelected].getX() - shipsSprites[shipSelected].getWidth()/1.75f, 
				shipsSprites[shipSelected].getY() + 1.5f*shipsSprites[shipSelected].getHeight() + adHeight/3);
		bStats.setPosition(shipsSprites[shipSelected].getX() + 1.7f*shipsSprites[shipSelected].getWidth(), 
				shipsSprites[shipSelected].getY() + 0.25f*shipsSprites[shipSelected].getHeight() + adHeight/3);
		bHelp.setPosition(shipsSprites[shipSelected].getX() + shipsSprites[shipSelected].getWidth(), 
				shipsSprites[shipSelected].getY() - shipsSprites[shipSelected].getHeight() + adHeight/3);
		bGo.setX(shipsSprites[shipSelected].getX() + shipsSprites[shipSelected].getWidth()/2 - bGo.getWidth()/2);
		bExit.setX(shipsSprites[shipSelected].getX() + shipsSprites[shipSelected].getWidth()/2 - bTrophy.getWidth()/2);
		bTrophy.setX(bGalaxies.getX() - bExit.getWidth()/2);
		bLeaderboard.setX(bHelp.getX() + bLeaderboard.getWidth()/2);
		layoutMainMenu();

		giftizLogo.setPosition(bGo.getX() + bGo.getWidth() + bGo.getX()/2 - giftizLogo.getWidth()/2, 
				bGo.getY() + bGo.getHeight()/2 - giftizLogo.getHeight()/2);
		giftizLogoWarning.setPosition(bGo.getX() + bGo.getWidth() + bGo.getX()/2 - giftizLogo.getWidth()/2, 
				bGo.getY() + bGo.getHeight()/2 - giftizLogo.getHeight()/2);
		giftizLogoBadge.setPosition(bGo.getX() + bGo.getWidth() + bGo.getX()/2 - giftizLogo.getWidth()/2, 
				bGo.getY() + bGo.getHeight()/2 - giftizLogo.getHeight()/2);
		
		if(language == ON){
			greysFont.getData().setScale(x, y);
			game.getPS().greysFont.getData().setScale(x, y);
		}else{
			greysFont.getData().setScale(x/1.115f, y/1.115f);
			game.getPS().greysFont.getData().setScale(x/1.115f, y/1.115f);
		}
		
		//set positions for buttons in PlayScreen
		v1 = new Vector2(bUpgrades.getX(), bUpgrades.getY());
		v2 = new Vector2(bShips.getX(), bShips.getY());
		v3 = new Vector2(bGalaxies.getX(), bGalaxies.getY());
		v4 = new Vector2(bHelp.getX(), bHelp.getY());
		v5 = new Vector2(bStats.getX(), bStats.getY());
		v6 = new Vector2(bSettings.getX(), bSettings.getY());
		
		// correct ship trayectory
		for(int k = 0; k < shipsBoughtImage.length; k++){
			shipsSprites[k].setSize(128, 128);
			shipsSprites[k].setScale((fx(128) + fy(128))/2/96, (fx(128) + fy(128))/2/96);
			shipsSprites[k].setPosition(Gdx.graphics.getWidth()/2 - shipsSprites[k].getWidth()/2, Gdx.graphics.getHeight()/2 - shipsSprites[k].getHeight()/2 + 2*adHeight/3 - adHeight/3);
		}
		
		Timeline.createSequence().
		beginParallel().
			push(Tween.to(bgs[galaxySelected*2], SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f).ease(TweenEquations.easeNone)).
			push(Tween.to(bgs[galaxySelected*2+1], SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f).ease(TweenEquations.easeNone)).
			push(Tween.set(ffSprites[0], SpriteAccessor.SCALE).target((fx(ffSprites[0].getWidth()) + fy(ffSprites[0].getHeight()))/2/96, 
				(fx(ffSprites[0].getWidth()) + fy(ffSprites[0].getHeight()))/2/96).ease(TweenEquations.easeInBack)).
			push(Tween.set(ffSprites[0], SpriteAccessor.POS_XY).target(Gdx.graphics.getWidth()/2 - shipsSprites[shipSelected].getWidth()/2, 
				Gdx.graphics.getHeight()/2 - shipsSprites[shipSelected].getHeight()/2 + 2*adHeight/3 - adHeight/3).ease(TweenEquations.easeInBack)).
			push(Tween.set(ffSprites[1], SpriteAccessor.SCALE).target((fx(ffSprites[1].getWidth()) + fy(ffSprites[1].getHeight()))/2/96, 
					(fx(ffSprites[1].getWidth()) + fy(ffSprites[1].getHeight()))/2/96).ease(TweenEquations.easeInBack)).
			push(Tween.set(ffSprites[1], SpriteAccessor.POS_XY).target(Gdx.graphics.getWidth()/2 - shipsSprites[shipSelected].getWidth()/2, 
				Gdx.graphics.getHeight()/2 - shipsSprites[shipSelected].getHeight()/2 + 2*adHeight/3 - adHeight/3).ease(TweenEquations.easeInBack)).
			
			push(Tween.set(ffgSprites[0], SpriteAccessor.SCALE).target((fx(ffgSprites[0].getWidth()) + fy(ffgSprites[0].getHeight()))/2/96, 
				(fx(ffgSprites[0].getWidth()) + fy(ffgSprites[0].getHeight()))/2/96).ease(TweenEquations.easeInBack)).
			push(Tween.set(ffgSprites[0], SpriteAccessor.POS_XY).target(Gdx.graphics.getWidth()/2 - ffgSprites[0].getWidth()/2, 
				Gdx.graphics.getHeight()/2 - ffgSprites[0].getHeight()/2 + 2*adHeight/3 - adHeight/3).ease(TweenEquations.easeInBack)).
			push(Tween.set(ffgSprites[1], SpriteAccessor.SCALE).target((fx(ffgSprites[1].getWidth()) + fy(ffgSprites[1].getHeight()))/2/96, 
				(fx(ffgSprites[1].getWidth()) + fy(ffgSprites[1].getHeight()))/2/96).ease(TweenEquations.easeInBack)).
			push(Tween.set(ffgSprites[1], SpriteAccessor.POS_XY).target(Gdx.graphics.getWidth()/2 - ffgSprites[1].getWidth()/2, 
				Gdx.graphics.getHeight()/2 - ffgSprites[1].getHeight()/2 + 2*adHeight/3 - adHeight/3).ease(TweenEquations.easeInBack)).
					
			push(Tween.set(shipsSprites[shipSelected], SpriteAccessor.SCALE).target((fx(128) + fy(128))/2/96, (fx(128) + fy(128))/2/96).ease(TweenEquations.easeInBack)).
			push(Tween.set(shipsSprites[shipSelected], SpriteAccessor.POS_XY).target(Gdx.graphics.getWidth()/2 - shipsSprites[shipSelected].getWidth()/2, 
				Gdx.graphics.getHeight()/2 - shipsSprites[shipSelected].getHeight()/2 + 2*adHeight/3 - adHeight/3).ease(TweenEquations.easeInBack)).
			push(Tween.to(bGo, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f)).
			push(Tween.to(bExit, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f)).
			push(Tween.to(bTrophy, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f)).
			push(Tween.to(bLeaderboard, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f)).
			push(Tween.to(bUpgrades, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f)).
			push(Tween.to(bShips, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f)).
			push(Tween.to(bGalaxies, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f)).
			push(Tween.to(bStats, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f)).
			push(Tween.to(bHelp, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f)).
			push(Tween.to(giftizLogo, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f)).
			push(Tween.to(giftizLogoWarning, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f)).
			push(Tween.to(giftizLogoBadge, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f)).
			push(Tween.to(greysFont, BitmapFontAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f)).
			push(Tween.to(greysFontTitle, BitmapFontAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f)).
		end().
		start(tweenManager);

		Tween.set(ffSprites[0], SpriteAccessor.SCALE).target((fx(ffSprites[0].getWidth()) + fy(ffSprites[0].getHeight()))/2/96, 
				(fx(ffSprites[0].getWidth()) + fy(ffSprites[0].getHeight()))/2/96).start(tweenManager);
		Tween.set(ffSprites[1], SpriteAccessor.SCALE).target((fx(ffSprites[1].getWidth()) + fy(ffSprites[1].getHeight()))/2/96, 
				(fx(ffSprites[1].getWidth()) + fy(ffSprites[1].getHeight()))/2/96).start(tweenManager);
		Tween.set(ffSprites[0], SpriteAccessor.COLOR).target(1f, 1f, 1f, 1f).start(tweenManager);
		Tween.set(ffSprites[1], SpriteAccessor.COLOR).target(1f, 1f, 1f, 1f).start(tweenManager);
		
		Tween.set(ffgSprites[0], SpriteAccessor.SCALE).target((fx(ffgSprites[0].getWidth()) + fy(ffgSprites[0].getHeight()))/2/120, 
				(fx(ffgSprites[0].getWidth()) + fy(ffgSprites[0].getHeight()))/2/120).start(tweenManager);
		Tween.set(ffgSprites[1], SpriteAccessor.SCALE).target((fx(ffgSprites[1].getWidth()) + fy(ffgSprites[1].getHeight()))/2/120, 
				(fx(ffgSprites[1].getWidth()) + fy(ffgSprites[1].getHeight()))/2/120).start(tweenManager);
	
		for(int h = 0; h < lightBSprites.length; h++){
			Tween.set(lightBSprites[h], SpriteAccessor.SCALE).target((fx(lightBSprites[h].getWidth()) + fy(lightBSprites[h].getHeight()))/2/96, 
					(fx(lightBSprites[h].getWidth()) + fy(lightBSprites[h].getHeight()))/2/96).start(tweenManager);
			Tween.set(lightBSprites[h], SpriteAccessor.POS_XY).target(Gdx.graphics.getWidth()/2 - lightBSprites[h].getWidth()/2, 
						Gdx.graphics.getHeight()/2 - lightBSprites[h].getHeight()/2 + 2*adHeight/3 - adHeight/3).start(tweenManager);
		}
		for(int h = 0; h < lightRSprites.length; h++){
			Tween.set(lightRSprites[h], SpriteAccessor.SCALE).target((fx(lightRSprites[h].getWidth()) + fy(lightRSprites[h].getHeight()))/2/96, 
					(fx(lightRSprites[h].getWidth()) + fy(lightRSprites[h].getHeight()))/2/96).start(tweenManager);
			Tween.set(lightRSprites[h], SpriteAccessor.POS_XY).target(Gdx.graphics.getWidth()/2 - lightRSprites[h].getWidth()/2, 
						Gdx.graphics.getHeight()/2 - lightRSprites[h].getHeight()/2 + 2*adHeight/3 - adHeight/3).start(tweenManager);
		}
		
		Tween.set(ffgSprites[0], SpriteAccessor.COLOR).target(1f, 1f, 1f, 1f).start(tweenManager);
		Tween.set(ffgSprites[1], SpriteAccessor.COLOR).target(1f, 1f, 1f, 1f).start(tweenManager);
	
		Tween.set(bluesFont, BitmapFontAccessor.COLOR).target(1f, 1f, 1f, 1f).start(tweenManager);
		Tween.set(redsFont, BitmapFontAccessor.COLOR).target(1f, 1f, 1f, 1f).start(tweenManager);
		Tween.set(lifeFont, BitmapFontAccessor.COLOR).target(1f, 1f, 1f, 0f).start(tweenManager);
		Tween.set(blueCounter, SpriteAccessor.COLOR).target(1f, 1f, 1f, 1f).start(tweenManager);
		Tween.set(redCounter, SpriteAccessor.COLOR).target(1f, 1f, 1f, 1f).start(tweenManager);
		Tween.set(lifeCounter, SpriteAccessor.COLOR).target(1f, 1f, 1f, 0f).start(tweenManager);

		Tween.set(greysFont, BitmapFontAccessor.COLOR).target(1f, 1f, 1f, 1f).start(tweenManager);
		Tween.set(greysFontTitle, BitmapFontAccessor.COLOR).target(1f, 1f, 1f, 1f).start(tweenManager);
		
		Tween.call(countersCallBack).start(tweenManager);
		Tween.call(glowCallBack).start(tweenManager);

		lastTimeBg = TimeUtils.nanoTime();
		lightTime = 1;
		musicTime = 1;

		stageShips.addAction(moveTo(0, stageShips.getHeight()));
		stageGalaxies.addAction(moveTo(0, stageGalaxies.getHeight()));	
		stageUpgrades.addAction(moveTo(0, stageUpgrades.getHeight()));	
		stageStats.addAction(moveTo(0, stageStats.getHeight()));	
		stageSettings.addAction(moveTo(0, stageSettings.getHeight()));	
		stageHelp.addAction(moveTo(0, stageHelp.getHeight()));	
		
	}

	@Override
	public void render(float delta) {

		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		if(TimeUtils.nanoTime() - time > 1000000000){
			timePlaying = (int) (timePlaying + (TimeUtils.nanoTime() - time)/1000000000);
			time = TimeUtils.nanoTime();	
			timePlayingLabeln.setText(String.valueOf(timePlaying/3600) + ":" + String.valueOf((timePlaying % 3600)/60) + ":" + String.valueOf((timePlaying % 3600) % 60));
			timePlayingLabeln.setAlignment(Align.right);
		}

		if(boughtShip){
			if(Gdx.app.getType().equals(ApplicationType.Android) && game.getActionResolver().getSignedIn()){
				if (ships >= 1) game.getActionResolver().unlockAchievement("CgkIrLf9wZgEEAIQAA");
				if (ships >= 5) game.getActionResolver().unlockAchievement("CgkIrLf9wZgEEAIQAQ");
				if (ships >= 10) game.getActionResolver().unlockAchievement("CgkIrLf9wZgEEAIQAg");
				if (ships >= 25) game.getActionResolver().unlockAchievement("CgkIrLf9wZgEEAIQAw");
				if (ships >= 50) game.getActionResolver().unlockAchievement("CgkIrLf9wZgEEAIQBA");
				if (ships >= 74) game.getActionResolver().unlockAchievement("CgkIrLf9wZgEEAIQBQ");
			}
			boughtShip = false;
		}
		if(boughtGalaxy){
			if(Gdx.app.getType().equals(ApplicationType.Android) && game.getActionResolver().getSignedIn()){
				if (galaxies >= 1) game.getActionResolver().unlockAchievement("CgkIrLf9wZgEEAIQBg");
				if (galaxies >= 5) game.getActionResolver().unlockAchievement("CgkIrLf9wZgEEAIQBw");
				if (galaxies >= 9) game.getActionResolver().unlockAchievement("CgkIrLf9wZgEEAIQCA");
			}
			boughtGalaxy = false;
		}
		if(boughtUpgrade){
			if(Gdx.app.getType().equals(ApplicationType.Android) && game.getActionResolver().getSignedIn()){
				if (upgrades >= 1) game.getActionResolver().unlockAchievement("CgkIrLf9wZgEEAIQFw");
				if (upgrades >= 5) game.getActionResolver().unlockAchievement("CgkIrLf9wZgEEAIQGA");
				if (upgrades >= 10) game.getActionResolver().unlockAchievement("CgkIrLf9wZgEEAIQGQ");
				if (upgrades >= 18) game.getActionResolver().unlockAchievement("CgkIrLf9wZgEEAIQGg");
			}
			boughtUpgrade = false;
		}

		if(musicFade < 1){
			if(musicTime % 40 == 0)
				musicFade = Math.min(1f, musicFade + 0.025f);
			musicTime++;
		}
		updateMusicVolume();

		tweenManager.update(delta);
		
		loadBgsAssets();

		game.batch.begin();

		drawBackground();

		bShips.draw(game.batch);
		bGalaxies.draw(game.batch);
		bSettings.draw(game.batch);
		bGo.draw(game.batch);
		bExit.draw(game.batch);
		bTrophy.draw(game.batch);
		bLeaderboard.draw(game.batch);
		bUpgrades.draw(game.batch);
		bStats.draw(game.batch);
		bHelp.draw(game.batch);
		
		//engine plume
		if(startTrace && drawTrace){
			Fx.init(game.assetManager);
			Fx.additiveOn(game.batch);
			menuPlume.draw(game.batch, shipsSprites[shipSelected], shipSelected, forceFieldSelected, 1f, true);
			Fx.draw(game.batch);
			Fx.additiveOff(game.batch);
		}

		shipsSprites[shipSelected].draw(game.batch);
		ffSprites[forceFieldSelected].draw(game.batch);
		
		if(Fx.CLASSIC_LIGHTNING){
			// original effect (two sprite flashes at random angles), now drawn additively for extra shine
			Fx.additiveOn(game.batch);
			if(lightTime % 50 == 0 && state == STANDBY){
				l1 = MathUtils.random(0, 5);
				l2 = MathUtils.random(0, 5);
				if(l1 == l2)
					l2 = MathUtils.random(0, 5);
				if(forceFieldSelected == 0){
					lightBSprites[l1].setRotation(MathUtils.random(0, 360));
					lightBSprites[l2].setRotation(MathUtils.random(0, 360));
					lightBSprites[l1].draw(game.batch);
				}else{
					lightRSprites[l1].setRotation(MathUtils.random(0, 360));
					lightRSprites[l2].setRotation(MathUtils.random(0, 360));
					lightRSprites[l1].draw(game.batch);
				}
			}
			if(lightTime % 55 == 0 && state == STANDBY)
				if(forceFieldSelected == 0)
					lightBSprites[l2].draw(game.batch);	
				else
					lightRSprites[l2].draw(game.batch);
			Fx.additiveOff(game.batch);
		}else{
			// electricity on the force field
			float dtL = Gdx.graphics.getDeltaTime();
			electricity.update(dtL, state == STANDBY);
			Fx.additiveOn(game.batch);
			electricity.draw(game.batch, ffSprites[forceFieldSelected].getX() + ffSprites[forceFieldSelected].getWidth()/2,
					ffSprites[forceFieldSelected].getY() + ffSprites[forceFieldSelected].getHeight()/2,
					0.45f*ffSprites[forceFieldSelected].getWidth()*ffSprites[forceFieldSelected].getScaleX(), forceFieldSelected);
			Fx.additiveOff(game.batch);
		}
		lightTime++;

		ffgSprites[forceFieldSelected].draw(game.batch);
		
		slideBottomBlock();
		Fx.init(game.assetManager);
		Fx.update(Gdx.graphics.getDeltaTime());
		if(drawCounters){
			// blue counter - blue orb - red orb - red counter (the game-over screen draws this same row)
			drawBlueRedRow(game.batch, String.valueOf(blueBalls), String.valueOf(redBalls), blueCounter.getY(), -1f);
			Fx.drawOrb(game.batch, lifeCounter, 2, 0.66f);
			lifeFont.draw(game.batch, String.valueOf(lifeBalls), lifeCounter.getX() + 1.25f*lifeCounter.getWidth(),
					blueCounter.getY() + 1.2f*com.spaceship.handlers.FontUtil.bounds(lifeFont, String.valueOf(lifeBalls)).height);
		}
		greysFontTitle.draw(game.batch, "Collect The", (Gdx.graphics.getWidth() - com.spaceship.handlers.FontUtil.bounds(greysFontTitle, "Collect The").width)/2, 
				titleY1);
		greysFontTitle.draw(game.batch, "Orbs", (Gdx.graphics.getWidth() - com.spaceship.handlers.FontUtil.bounds(greysFontTitle, "Orbs").width)/2, 
				titleY2);
		if(language == ON)
			greysFont.draw(game.batch, "Best Score: " + bestScore, (Gdx.graphics.getWidth() - 
				com.spaceship.handlers.FontUtil.bounds(greysFont, "Best Score: " + bestScore).width)/2, scoreY);
		else
			greysFont.draw(game.batch, "Mejor Marca: " + bestScore, (Gdx.graphics.getWidth() - 
					com.spaceship.handlers.FontUtil.bounds(greysFont, "Mejor Marca: " + bestScore).width)/2, scoreY);

		 switch (g) {
	        case 1 : giftizLogo.draw(game.batch);
	        	break;
	        case 2 : giftizLogoBadge.draw(game.batch);
	        	break;
	        case 3 : giftizLogoWarning.draw(game.batch);
	        	break;
		 }
		
		if(state == LEAVING){
			cover.draw(game.batch);
			bNo.draw(game.batch);
			bYes.draw(game.batch);
			if(language == ON)
				bQuestionEN.draw(game.batch);
			else
				bQuestionES.draw(game.batch);
		}
		 
		game.batch.end();

		stageShips.act(delta);
		stageShips.draw();
		stageGalaxies.act(delta);
		stageGalaxies.draw();
		stageUpgrades.act(delta);
		stageUpgrades.draw();
		stageStats.act(delta);
		stageStats.draw();
		stageSettings.act(delta);
		stageSettings.draw();
		stageHelp.act(delta);
		stageHelp.draw();
//		containerSts.drawDebug(stageStats);

		//start game
		if(Gdx.input.justTouched()){
			if(Gdx.input.getX() > bGo.getX() && Gdx.input.getX() < bGo.getX() + bGo.getWidth() &&
					Gdx.graphics.getHeight() - Gdx.input.getY() > bGo.getY() && Gdx.graphics.getHeight() - 
					Gdx.input.getY() < bGo.getY() + bGo.getHeight() && state == STANDBY){
				//Play
				state = LAUNCH;
				if(sound == ON)
					buttonLarge.play(0.75f);
				Timer.schedule(new Task(){
					@Override
					public void run(){
						updateDataBases();
						updateTime();
					}
				}, 1.45f);
				Timer.schedule(new Task(){
					@Override
					public void run() {
						if(sound == ON)
							pre.play(0.75f);
					}
				}, 0.35f);
				Timer.schedule(new Task(){
					@Override
					public void run() {
						if(sound == ON)
							launch.play(0.75f);
					}
				}, 0.8f);
				
				Tween.to(bUpgrades, SpriteAccessor.POS_XY, 0.25f).ease(TweenEquations.easeInOutSine).delay(0.50f).
					target(-bUpgrades.getWidth(), Gdx.graphics.getHeight() + bUpgrades.getHeight()).start(tweenManager);
				Tween.to(bShips, SpriteAccessor.POS_XY, 0.25f).ease(TweenEquations.easeInOutSine).delay(0.50f).
					target(-bShips.getWidth(), bShips.getY()).start(tweenManager);
				Tween.to(bGalaxies, SpriteAccessor.POS_XY, 0.25f).ease(TweenEquations.easeInOutSine).delay(0.50f).
					target(-bGalaxies.getWidth(), -bGalaxies.getHeight()).start(tweenManager);
				Tween.to(bHelp, SpriteAccessor.POS_XY, 0.25f).ease(TweenEquations.easeInOutSine).delay(0.50f).
					target(Gdx.graphics.getWidth() + bLeaderboard.getWidth(), -bLeaderboard.getHeight()).start(tweenManager);
				Tween.to(bStats, SpriteAccessor.POS_XY, 0.25f).ease(TweenEquations.easeInOutSine).delay(0.50f).
					target(Gdx.graphics.getWidth() + bTrophy.getWidth(), bTrophy.getY()).start(tweenManager);
				Tween.to(bSettings, SpriteAccessor.POS_XY, 0.25f).ease(TweenEquations.easeInOutSine).delay(0.50f).
					target(Gdx.graphics.getWidth() + bHelp.getWidth(), Gdx.graphics.getHeight() + bHelp.getHeight()).start(tweenManager);
				
				Timeline.createSequence().
				push(Tween.to(bGo, SpriteAccessor.COLOR, 0.15f).target(0.5f, 0.5f, 0.5f, 1f).repeatYoyo(1, 0)).
				beginParallel().
				push(Tween.to(bgs[galaxySelected*2], SpriteAccessor.COLOR, 0.65f).target(1f, 1f, 1f, 0f).delay(0.50f).ease(TweenEquations.easeNone)).
				push(Tween.to(bgs[galaxySelected*2+1], SpriteAccessor.COLOR, 0.65f).target(1f, 1f, 1f, 0f).delay(0.50f).ease(TweenEquations.easeNone)).
				push(Tween.to(ffSprites[forceFieldSelected], SpriteAccessor.SCALE, 1.15f).target(0.05f, 0.05f).ease(TweenEquations.easeInBack)).
				push(Tween.to(ffSprites[forceFieldSelected], SpriteAccessor.POS_XY, 1.15f).target(ffSprites[0].getX(), Gdx.graphics.getHeight() + 128).ease(TweenEquations.easeInBack)).
				push(Tween.to(ffgSprites[forceFieldSelected], SpriteAccessor.SCALE, 1.15f).target(0.05f, 0.05f).ease(TweenEquations.easeInBack)).
				push(Tween.to(ffgSprites[forceFieldSelected], SpriteAccessor.POS_XY, 1.15f).target(ffSprites[0].getX(), Gdx.graphics.getHeight() + 128).ease(TweenEquations.easeInBack)).
				push(Tween.to(shipsSprites[shipSelected], SpriteAccessor.SCALE, 1.15f).target(0.05f, 0.05f).ease(TweenEquations.easeInBack)).
				push(Tween.to(shipsSprites[shipSelected], SpriteAccessor.POS_XY, 1.15f).target(shipsSprites[shipSelected].getX(), Gdx.graphics.getHeight() + 128).ease(TweenEquations.easeInBack)).
				push(Tween.to(bGo, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f)).
				push(Tween.to(bExit, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f)).
				push(Tween.to(giftizLogo, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f)).
				push(Tween.to(giftizLogoWarning, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f)).
				push(Tween.to(giftizLogoBadge, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f)).
				push(Tween.to(bTrophy, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f)).
				push(Tween.to(bLeaderboard, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f)).
				push(Tween.to(bStats, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f)).
				push(Tween.to(redCounter, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, -1f)).
				push(Tween.to(lifeCounter, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, -1f)).
				push(Tween.to(blueCounter, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, -1f)).
				push(Tween.to(redsFont, BitmapFontAccessor.COLOR, 0.25f).target(1f, 1f, 1f, -1f)).
				push(Tween.to(lifeFont, BitmapFontAccessor.COLOR, 0.25f).target(1f, 1f, 1f, -1f)).
				push(Tween.to(bluesFont, BitmapFontAccessor.COLOR, 0.25f).target(1f, 1f, 1f, -1f)).
				push(Tween.to(greysFont, BitmapFontAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f)).
				push(Tween.to(greysFontTitle, BitmapFontAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f)).
				end().
				start(tweenManager);
				
				Timer.schedule(new Task(){
					@Override
					public void run() {
						drawCounters = false;
					}
				}, 0.25f);				
				Timer.schedule(new Task(){
					@Override
					public void run() {
						game.setScreen(game.getPS());
					}
				}, 1.45f);
				dispose();
			}else if(Gdx.input.getX() > bShips.getX() && Gdx.input.getX() < bShips.getX() + bShips.getWidth() && 
					Gdx.graphics.getHeight() - Gdx.input.getY() > bShips.getY() && Gdx.graphics.getHeight() - 
					Gdx.input.getY() < bShips.getY() + bShips.getHeight()){
				//Ships
				if(state == STANDBY){
					if(sound == ON)
						buttonSmall.play();
					state = SHIPS;
					canSelect = true;
					stageShips.addAction(moveTo(0, 0, 0.5f));
					Gdx.input.setInputProcessor(stageShips);
					effectsMenuOn();
				}
			}else if(Gdx.input.getX() > bGalaxies.getX() && Gdx.input.getX() < bGalaxies.getX() + bGalaxies.getWidth() && 
					Gdx.graphics.getHeight() - Gdx.input.getY() > bGalaxies.getY() && Gdx.graphics.getHeight() - 
					Gdx.input.getY() < bGalaxies.getY() + bGalaxies.getHeight()){
				//Galaxies
				if(state == STANDBY){
					if(sound == ON)
						buttonSmall.play();
					state = GALAXIES;
					canSelect = true;
					stageGalaxies.addAction(moveTo(0, 0, 0.5f));
					Gdx.input.setInputProcessor(stageGalaxies);
					effectsMenuOn();
				}	
			}else if(Gdx.input.getX() > bTrophy.getX() && Gdx.input.getX() < bTrophy.getX() + bTrophy.getWidth() && 
					Gdx.graphics.getHeight() - Gdx.input.getY() > bTrophy.getY() && Gdx.graphics.getHeight() - 
					Gdx.input.getY() < bTrophy.getY() + bTrophy.getHeight()){
				//Trophies
				if(state == STANDBY){
					if(sound == ON)
						buttonSmall.play();
					if(Gdx.app.getType().equals(ApplicationType.Android)){
						if(game.getActionResolver().getSignedIn()){ 
							Timer.schedule(new Task(){
								@Override
								public void run() {
									game.getActionResolver().getAchievements();
								}
							}, 0.30f);
						}else{
							Timer.schedule(new Task(){
								@Override
								public void run() {
									game.getActionResolver().logIn();
								}
							}, 0.30f);
						}
						Timer.schedule(new Task(){
							@Override
							public void run() {
								drawTrace = false;
							}
						}, 0.25f);
					}
				}		
			}else if(Gdx.input.getX() > bLeaderboard.getX() && Gdx.input.getX() < bLeaderboard.getX() + bLeaderboard.getWidth() && 
					Gdx.graphics.getHeight() - Gdx.input.getY() > bLeaderboard.getY() && Gdx.graphics.getHeight() - 
					Gdx.input.getY() < bLeaderboard.getY() + bLeaderboard.getHeight()){
				//Leaderboard
				if(state == STANDBY){
					if(sound == ON)
						buttonSmall.play();
					if(Gdx.app.getType().equals(ApplicationType.Android)){
						if (game.getActionResolver().getSignedIn()){ 
							Timer.schedule(new Task(){
								@Override
								public void run() {
									game.getActionResolver().getLeaderboard();
								}
							}, 0.30f);
						}else{ 
							Timer.schedule(new Task(){
								@Override
								public void run() {
									game.getActionResolver().logIn();
								}
							}, 0.30f);
						}	
						Timer.schedule(new Task(){
							@Override
							public void run() {
								drawTrace = false;
							}
						}, 0.25f);
					}	
				}			
			}else if(Gdx.input.getX() > bUpgrades.getX() && Gdx.input.getX() < bUpgrades.getX() + bUpgrades.getWidth() &&
					Gdx.graphics.getHeight() - Gdx.input.getY() > bUpgrades.getY() && Gdx.graphics.getHeight() - 
					Gdx.input.getY() < bUpgrades.getY() + bUpgrades.getHeight() && state == STANDBY){
				//Upgrades
				if(sound == ON)
					buttonSmall.play();
				state = UPGRADES;
				canSelect = true;
				stageUpgrades.addAction(moveTo(0, 0, 0.5f));
				Gdx.input.setInputProcessor(stageUpgrades);
				effectsMenuOn();
			}else if(Gdx.input.getX() > bStats.getX() && Gdx.input.getX() < bStats.getX() + bStats.getWidth() && 
					Gdx.graphics.getHeight() - Gdx.input.getY() > bStats.getY() && Gdx.graphics.getHeight() - 
					Gdx.input.getY() < bStats.getY() + bStats.getHeight()){
				//Stats
				if(state == STANDBY){
					if(sound == ON)
						buttonSmall.play();
					state = STATS;
					canSelect = true;
					stageStats.addAction(moveTo(0, 0, 0.5f));
					Gdx.input.setInputProcessor(stageStats);
					effectsMenuOn();
				}	
			}else if(Gdx.input.getX() > bSettings.getX() && Gdx.input.getX() < bSettings.getX() + bSettings.getWidth() && 
					Gdx.graphics.getHeight() - Gdx.input.getY() > bSettings.getY() && Gdx.graphics.getHeight() - 
					Gdx.input.getY() < bSettings.getY() + bSettings.getHeight()){
				//settings
				if(state == STANDBY){
					if(sound == ON)
						buttonSmall.play();
					state = SETTINGS;
					canSelect = true;
					stageSettings.addAction(moveTo(0, 0, 0.5f));
					Gdx.input.setInputProcessor(stageSettings);
					effectsMenuOn();
				}	
			}else if(Gdx.input.getX() > bHelp.getX() && Gdx.input.getX() < bHelp.getX() + bHelp.getWidth() && 
					Gdx.graphics.getHeight() - Gdx.input.getY() > bHelp.getY() && Gdx.graphics.getHeight() - 
					Gdx.input.getY() < bHelp.getY() + bHelp.getHeight()){
				//help
				if(state == STANDBY){
					if(sound == ON)
						buttonSmall.play();
					state = HELP;
					canSelect = true;
					stageHelp.addAction(moveTo(0, 0, 0.5f));
					Gdx.input.setInputProcessor(stageHelp);
					effectsMenuOn();
				}	
			}else if(Gdx.input.getX() > bExit.getX() && Gdx.input.getX() < bExit.getX() + bExit.getWidth() &&
					Gdx.graphics.getHeight() - Gdx.input.getY() > bExit.getY() && Gdx.graphics.getHeight() - 
					Gdx.input.getY() < bExit.getY() + bExit.getHeight() && state == STANDBY){
				//Leaving
				state = LEAVING;
				if(sound == ON)
					buttonLarge.play(0.75f);
			}else if(Gdx.input.getX() > giftizLogo.getX() && Gdx.input.getX() < giftizLogo.getX() + giftizLogo.getWidth() &&
					Gdx.graphics.getHeight() - Gdx.input.getY() > giftizLogo.getY() && Gdx.graphics.getHeight() - 
					Gdx.input.getY() < giftizLogo.getY() + giftizLogo.getHeight() && state == STANDBY){
				//giftiz
				if(sound == ON)
					buttonSmall.play(0.75f);
				Timer.schedule(new Task(){
						@Override
						public void run() {
							if(Gdx.app.getType().equals(ApplicationType.Android))
								game.getActionResolver().giftizButtonClicked();
						}
					}, 0.5f);
			}else if(state == STANDBY){
				switch(forceFieldSelected){
				case 0 :
					forceFieldSelected = 1;
					if(sound == ON)
						ffChange.play(1f);
					tweenRedBig();
					break;
				case 1 : 
					forceFieldSelected = 0;
					if(sound == ON)
						ffChange.play(1f);
					tweenBlueBig();
					break;
				}
			}	
			if(Gdx.input.getX() > bYes.getX() && Gdx.input.getX() < bYes.getX() + bYes.getWidth() &&
					Gdx.graphics.getHeight() - Gdx.input.getY() > bYes.getY() && Gdx.graphics.getHeight() - 
					Gdx.input.getY() < bYes.getY() + bYes.getHeight() && state == LEAVING){
				//Exit
				state = EXIT;
				if(sound == ON)
					buttonSmall.play();
				Timer.schedule(new Task(){
					@Override
					public void run() {
						updateDataBases();
						updateTime();
						Gdx.app.exit();				  
					}
				}, 0.30f);
			}else if(Gdx.input.getX() > bNo.getX() && Gdx.input.getX() < bNo.getX() + bNo.getWidth() &&
					Gdx.graphics.getHeight() - Gdx.input.getY() > bNo.getY() && Gdx.graphics.getHeight() - 
					Gdx.input.getY() < bNo.getY() + bNo.getHeight() && state == LEAVING){
				state = STANDBY;
				if(sound == ON)
					buttonSmall.play();
			}
		}
	}

	@Override
	public void resize(int width, int height) {

	}
	
	public void setUpShipsMenu(){
		float shipPad = (3*Gdx.graphics.getWidth()/4 - shipsBoughtImage[0].getWidth()*3)/3;
		stageShips = new Stage();
		containerShips = new Table();
		stageShips.addActor(containerShips);
		containerShips.setBounds(Gdx.graphics.getWidth()/8 , counterBaseY + 1.5f*blueCounter.getHeight(), 
				3*Gdx.graphics.getWidth()/4, Gdx.graphics.getHeight() - 0.225f*Gdx.graphics.getHeight() - adHeight);	
		imgsShips = new Table();
		scrollShips = new ScrollPane(imgsShips);
		scrollShips.setScrollingDisabled(true, false);
		for(int k = 0; k < shipsBoughtImage.length; k++){
			final int i = k;
			if(shipsBought[k] == 0){
				shipsPriceImage[k].setName(String.valueOf(k));
				if(k > 2)
					imgsShips.add(shipsPriceImage[k]).uniform().expandX().padTop(shipPad).width(shipsPriceImage[k].getWidth()).height(shipsPriceImage[k].getHeight());
				else
					imgsShips.add(shipsPriceImage[k]).uniform().expandX().width(shipsPriceImage[k].getWidth()).height(shipsPriceImage[k].getHeight());
				shipsPriceImage[k].addListener(new ClickListener(){
					@Override
					public void clicked(InputEvent event, float x, float y){
						if(canSelect){
							canSelect = false;
							if(Integer.valueOf(redBalls) >= redsPriceShips[Integer.valueOf(event.getListenerActor().getName())] &&
									Integer.valueOf(blueBalls) >= bluesPriceShips[Integer.valueOf(event.getListenerActor().getName())]){
	
								if(sound == ON)
									bought.play();
	
								shipsNew[i] = true;
	
								spentBlues = spentBlues + bluesPriceShips[i]; 
								spentReds = spentReds + redsPriceShips[i]; 
								blueBalls -= bluesPriceShips[i]; 
								redBalls -= redsPriceShips[i];
								shipsBought[i] = 1;
								Tween.set(shipsBoughtImage[i], ImageAccessor.COLOR).target(1f, 1f, 1f, 0f).start(tweenManager);
								Tween.to(shipsPriceImage[i], ImageAccessor.COLOR, 0.1f).target(1f, 1f, 1f, 0f).start(tweenManager);
								Timer.schedule(new Task(){
									@Override
									public void run(){
										imgsShips.getCell(shipsPriceImage[i]).setActor(shipsBoughtImage[i]);
									}
								}, 0.1f);
								Tween.to(shipsBoughtImage[i], ImageAccessor.COLOR, 0.1f).delay(0.1f).target(1f, 1f, 1f, 1f).start(tweenManager);
	
								ships++;
								boughtShip = true;
	
							}else{
								if(sound == ON)
									cant.play(1.5f);
								Tween.to(shipsPriceImage[i], ImageAccessor.COLOR, 0.1f).target(0.5f, 0.5f, 0.5f, 1f).repeatYoyo(1, 0).start(tweenManager);			
							}
							Timer.schedule(new Task(){
								@Override
								public void run(){
									canSelect = true;
								}
							}, 0.25f);
						}	
					}	
				});
				if((k+1)%3 == 0 && k != 0)
					imgsShips.row();
			}
			shipsBoughtImage[k].setName(String.valueOf(k));
			if(shipsBought[k] == 1){
				if(k > 2)
					imgsShips.add(shipsBoughtImage[k]).uniform().expandX().padTop(shipPad).width(shipsBoughtImage[k].getWidth()).height(shipsBoughtImage[k].getHeight());
				else
					imgsShips.add(shipsBoughtImage[k]).uniform().expandX().width(shipsBoughtImage[k].getWidth()).height(shipsBoughtImage[k].getHeight());
			}	
			shipsBoughtImage[k].addListener(new ClickListener(){
				@Override
				public void clicked(InputEvent event, float x, float y){
					if(canSelect){
						canSelect = false;
						Tween.to(shipsBoughtImage[i], ImageAccessor.COLOR, 0.1f).target(0.5f, 0.5f, 0.5f, 1f).repeatYoyo(1, 0).start(tweenManager);
						if(sound == ON)
							buttonSmall.play();
	
						shipSelected = Integer.valueOf(event.getListenerActor().getName());
						Timer.schedule(new Task(){
							@Override
							public void run(){
								state = STANDBY;
								stageShips.addAction(moveTo(0, stageShips.getHeight(), 0.5f));
								effectsMenuOff();
							}
						}, 0.3f);
					}
				}	
			});
			if((k+1)%3 == 0 && k != 0)
				imgsShips.row();
		}
	
		if(language == ON)
			shipsLabel = new Label("Ships", skin);
		else
			shipsLabel = new Label("Naves", skin);
		shipsLabel.setFontScale(x, y);
		shipsLabel.setSize(fx(shipsLabel.getWidth()), fy(shipsLabel.getHeight()));
		
		Image bBackI = new Image(bBack);
		containerShips.add(bBackI).width(bBack.getWidth()).height(bBack.getHeight()).left();
		containerShips.add(shipsLabel).width(shipsLabel.getWidth()).height(shipsLabel.getHeight()).left().expandX().padLeft(shipPad);
		containerShips.row();
		containerShips.add(scrollShips).width(3*Gdx.graphics.getWidth()/4).height(containerShips.getHeight() - bBack.getHeight() - 2*shipPad).padTop(shipPad).colspan(2);
	
		bBackI.addListener(new ClickListener(){
			public void clicked(InputEvent event, float x, float y){
				state = STANDBY;
				if(sound == ON)
					buttonSmall.play();
				stageShips.addAction(moveTo(0, stageShips.getHeight(), 0.5f));
				effectsMenuOff();
				updateStatsMenuSpent();
			}
		});		
	}

	public void setUpGalaxiesMenu(){
		stageGalaxies = new Stage();
		float galaxyPad = (3*Gdx.graphics.getWidth()/4 - bgsBoughtImage[0].getWidth()*2)/2;
		containerBgs = new Table();
		stageGalaxies.addActor(containerBgs);
		containerBgs.setBounds(Gdx.graphics.getWidth()/8 , counterBaseY + 1.5f*blueCounter.getHeight(), 
				3*Gdx.graphics.getWidth()/4, Gdx.graphics.getHeight() - 0.225f*Gdx.graphics.getHeight() - adHeight);	
		imgsBgs = new Table();
		scrollBgs = new ScrollPane(imgsBgs);
		scrollBgs.setScrollingDisabled(true, false);
		for(int k = 0; k < bgsBoughtImage.length; k++){
			final int i = k;
			if(galaxiesBought[k] == 0){
				bgsPriceImage[k].setName(String.valueOf(k));
				if(k > 1)
					imgsBgs.add(bgsPriceImage[k]).uniform().expandX().padTop(galaxyPad).width(bgsPriceImage[k].getWidth()).height(bgsPriceImage[k].getHeight());
				else
					imgsBgs.add(bgsPriceImage[k]).uniform().expandX().width(bgsPriceImage[k].getWidth()).height(bgsPriceImage[k].getHeight());
				bgsPriceImage[k].addListener(new ClickListener(){
					@Override
					public void clicked(InputEvent event, float x, float y){
						if(canSelect){
							canSelect = false;
							if(Integer.valueOf(redBalls) >= redsPriceGalaxies[Integer.valueOf(event.getListenerActor().getName())] &&
									Integer.valueOf(blueBalls) >= bluesPriceGalaxies[Integer.valueOf(event.getListenerActor().getName())]){
	
								if(sound == ON)
									bought.play();
	
								galaxiesNew[i] = true;

								spentBlues = spentBlues + bluesPriceGalaxies[i]; 
								spentReds = spentReds + redsPriceGalaxies[i]; 
								blueBalls -= bluesPriceGalaxies[i]; 
								redBalls -= redsPriceGalaxies[i];
								galaxiesBought[i] = 1;
								Tween.set(bgsBoughtImage[i], ImageAccessor.COLOR).target(1f, 1f, 1f, 0f).start(tweenManager);
								Tween.to(bgsBoughtImage[i], ImageAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f).start(tweenManager);
								Timer.schedule(new Task(){
									@Override
									public void run(){
										imgsBgs.getCell(bgsPriceImage[i]).setActor(bgsBoughtImage[i]);
									}
								}, 0.25f);
								Tween.to(bgsBoughtImage[i], ImageAccessor.COLOR, 0.25f).delay(0.25f).target(1f, 1f, 1f, 1f).start(tweenManager);
	
								galaxies++;
								boughtGalaxy = true;
	
								bgsBoughtImage[i].setName(String.valueOf(i));
								bgsBoughtImage[i].addListener(new ClickListener(){
									@Override
									public void clicked(InputEvent event, float x, float y){
	
										Tween.to(bgsBoughtImage[i], ImageAccessor.COLOR, 0.1f).target(0.5f, 0.5f, 0.5f, 1f).repeatYoyo(1, 0).start(tweenManager);
										if(sound == ON)
											buttonSmall.play();
										bgLoaded = Integer.valueOf(event.getListenerActor().getName());
										loadBgs(bgLoaded);
	
										Timer.schedule(new Task(){
											@Override
											public void run(){
												state = STANDBY;
												stageGalaxies.addAction(moveTo(0, stageGalaxies.getHeight(), 0.5f));
												effectsMenuOff();
											}
										}, 0.3f);
									}	
								});
							}else{
								if(sound == ON)
									cant.play(1.5f);
								Tween.to(bgsPriceImage[i], ImageAccessor.COLOR, 0.1f).target(0.5f, 0.5f, 0.5f, 1f).repeatYoyo(1, 0).start(tweenManager);
							}
							Timer.schedule(new Task(){
								@Override
								public void run(){
									canSelect = true;
								}
							}, 0.25f);
						}
					}	
				});
				if((k+1)%2 == 0 && k != 0)
					imgsBgs.row();
			}else{
				bgsBoughtImage[k].setName(String.valueOf(k));
				if(k > 1)
					imgsBgs.add(bgsBoughtImage[k]).uniform().expandX().padTop(galaxyPad).width(bgsBoughtImage[k].getWidth()).height(bgsBoughtImage[k].getHeight());
				else
					imgsBgs.add(bgsBoughtImage[k]).uniform().expandX().width(bgsBoughtImage[k].getWidth()).height(bgsBoughtImage[k].getHeight());
				bgsBoughtImage[k].addListener(new ClickListener(){
					@Override
					public void clicked(InputEvent event, float x, float y){
						if(canSelect){
							canSelect = false;
							Tween.to(bgsBoughtImage[i], ImageAccessor.COLOR, 0.1f).target(0.5f, 0.5f, 0.5f, 1f).repeatYoyo(1, 0).start(tweenManager);
							if(sound == ON)
								buttonSmall.play();
	
							bgLoaded = Integer.valueOf(event.getListenerActor().getName());
							loadBgs(bgLoaded);
	
							Timer.schedule(new Task(){
								@Override
								public void run(){
									state = STANDBY;
									stageGalaxies.addAction(moveTo(0, stageGalaxies.getHeight(), 0.5f));
									effectsMenuOff();
								}
							}, 0.3f);
						}	
					}	
				});
				if((k+1)%2 == 0 && k != 0)
					imgsBgs.row();
			}	
		}
		
		if(language == ON)
			bgsLabel = new Label("Galaxies", skin);
		else
			bgsLabel = new Label("Galaxias", skin);
		bgsLabel.setFontScale(x, y);
		bgsLabel.setSize(fx(bgsLabel.getWidth()), fy(bgsLabel.getHeight()));
	
		Image bBackIBgs = new Image(bBack);
		containerBgs.add(bBackIBgs).width(bBack.getWidth()).height(bBack.getHeight()).left();
		containerBgs.add(bgsLabel).width(bgsLabel.getWidth()).height(bgsLabel.getHeight()).left().expandX().padLeft(galaxyPad);
		containerBgs.row();
		containerBgs.add(scrollBgs).width(3*Gdx.graphics.getWidth()/4).height(containerShips.getHeight() - bBack.getHeight() - 2*galaxyPad).padTop(galaxyPad).colspan(2);
	
		bBackIBgs.addListener(new ClickListener(){
			public void clicked(InputEvent event, float x, float y){
				state = STANDBY;
				if(sound == ON)
					buttonSmall.play();
				stageGalaxies.addAction(moveTo(0, stageGalaxies.getHeight(), 0.5f));
				effectsMenuOff();
				updateStatsMenuSpent();
			}
		});		
	}
	
	public void setUpUpgradesMenu(){
		stageUpgrades = new Stage();
		containerUpg = new Table();
		stageUpgrades.addActor(containerUpg);
		containerUpg.setBounds(Gdx.graphics.getWidth()/8 , counterBaseY + 1.5f*blueCounter.getHeight(), 
				3*Gdx.graphics.getWidth()/4, Gdx.graphics.getHeight() - 0.225f*Gdx.graphics.getHeight() - adHeight);	
		imgsUpg = new Table();
		scrollUpg = new ScrollPane(imgsUpg);
		scrollUpg.setScrollingDisabled(true, false);
		
		if(language == ON)
			upgLabel = new Label("Upgrades", skin);
		else
			upgLabel = new Label("Mejoras", skin);
		upgLabel.setFontScale(x, y);
		upgLabel.setSize(fx(upgLabel.getWidth()), fy(upgLabel.getHeight()));

		speedLabel = new Label("Ship's Speed", skin);
		speedLabel.setFontScale(x/2, y/2);
		speedLabel.setSize(fx(speedLabel.getWidth()/2), fy(speedLabel.getHeight()/2));

		if(language == ON)
			energyLabel = new Label("Energy Bar", skin);
		else
			energyLabel = new Label("Barra de energia", skin);
		energyLabel.setFontScale(x/2.25f, y/2.25f);
		energyLabel.setSize(fx(energyLabel.getWidth()/2), fy(energyLabel.getHeight()/2));

		if(language == ON)
			energyStartLabel = new Label("Starting Energy", skin);
		else
			energyStartLabel = new Label("Energia inicial", skin);
		energyStartLabel.setFontScale(x/2.25f, y/2.25f);
		energyStartLabel.setSize(fx(energyStartLabel.getWidth()/2), fy(energyStartLabel.getHeight()/2));

		if(language == ON){
			objectsLabel = new Label("Item's Slots", skin);
			objectsLabel.setFontScale(x/2, y/2);
		}else{
			objectsLabel = new Label("Lugar para objetos", skin);
			objectsLabel.setFontScale(x/2.25f, y/2.25f);
		}objectsLabel.setSize(fx(objectsLabel.getWidth()/2), fy(objectsLabel.getHeight()/2));

		if(language == ON)
			objectsStartLabel = new Label("Starting Items", skin);
		else
			objectsStartLabel = new Label("Objetos iniciales", skin);
		objectsStartLabel.setFontScale(x/2.25f, y/2.25f);
		objectsStartLabel.setSize(fx(objectsStartLabel.getWidth()/2), fy(objectsStartLabel.getHeight()/2));

		if(language == ON){
			objectsDurationLabel = new Label("Item's Duration", skin);
			objectsDurationLabel.setFontScale(x/2, y/2);
		}else{
			objectsDurationLabel = new Label("Duracion de objetos", skin);
			objectsDurationLabel.setFontScale(x/2.25f, y/2.25f);
		}
		objectsDurationLabel.setSize(fx(objectsDurationLabel.getWidth()/2), fy(objectsDurationLabel.getHeight()/2));

		float upgradePad1 = objectsDurationLabel.getHeight()/8;
		float upgradePad2 = objectsDurationLabel.getHeight()/2.25f;
		int cellCounter = 0;
		
		Image speedPrice = upgradesPriceImages[shipSpeed - 1];
		final Table auxTableSpeed = new Table();
		auxTableSpeed.setSize(speedPrice.getWidth(), upgradeFull.getHeight() + speedLabel.getHeight());
		auxTableSpeed.add(speedLabel).width(speedLabel.getWidth()).height(speedLabel.getHeight()).padTop(upgradePad2).left().colspan(5);
		if(language == OFF){
			speedLabel.setText("Velocidad de nave");
			speedLabel.setAlignment(Align.left);
			speedLabel.setFontScale(x/2.25f, y/2.25f);
		}
		auxTableSpeed.row();
		upgradeBars[0] = new UpgradeBar(game.assetManager, fx(1.55f), shipSpeed/5.0f);
		auxTableSpeed.add(upgradeBars[0]).width(upgradeBars[0].getPrefWidth()).height(upgradeBars[0].getPrefHeight()).left().padTop(upgradePad1).padBottom(upgradePad2);
		imgsUpg.add(auxTableSpeed).expandX().left().padBottom(upgradePad2);
		if(shipSpeed < 5){
			imgsUpg.add(speedPrice).width(Math.abs(imgsUpg.getWidth() - auxTableSpeed.getWidth())).height(Math.abs(imgsUpg.getWidth() - auxTableSpeed.getWidth())).padLeft(speedPrice.getWidth()/5).left().padBottom(upgradePad2);		
		}
		imgsUpg.row();

		final Table auxTableEnergy = new Table();
		auxTableEnergy.setSize(upgradeFull.getWidth()*5, upgradeFull.getHeight() + speedLabel.getHeight());
		auxTableEnergy.add(energyLabel).width(speedLabel.getWidth()).height(speedLabel.getHeight()).padTop(upgradePad2).left().colspan(5);
		auxTableEnergy.row();
		upgradeBars[1] = new UpgradeBar(game.assetManager, fx(1.55f), energyMeter/5.0f);
		auxTableEnergy.add(upgradeBars[1]).width(upgradeBars[1].getPrefWidth()).height(upgradeBars[1].getPrefHeight()).left().padTop(upgradePad1).padBottom(upgradePad2);
		imgsUpg.add(auxTableEnergy).expandX().left().padBottom(upgradePad2);
		if(energyMeter < 5){
			Image energyPrice = upgradesPriceImages[energyMeter + 1];
			imgsUpg.add(energyPrice).width(Math.abs(imgsUpg.getWidth() - auxTableSpeed.getWidth())).height(Math.abs(imgsUpg.getWidth() - auxTableSpeed.getWidth())).padLeft(energyPrice.getWidth()/5).left().padBottom(upgradePad2);
		}
		imgsUpg.row();
		
		final Table auxTableEnergyStart = new Table();
		auxTableEnergyStart.setSize(upgradeFull.getWidth()*5, upgradeFull.getHeight() + speedLabel.getHeight());
		auxTableEnergyStart.add(energyStartLabel).width(speedLabel.getWidth()).height(speedLabel.getHeight()).padTop(upgradePad2).left().colspan(5);
		auxTableEnergyStart.row();
		upgradeBars[2] = new UpgradeBar(game.assetManager, fx(1.55f), energyStart/5.0f);
		auxTableEnergyStart.add(upgradeBars[2]).width(upgradeBars[2].getPrefWidth()).height(upgradeBars[2].getPrefHeight()).left().padTop(upgradePad1).padBottom(upgradePad2);
		imgsUpg.add(auxTableEnergyStart).expandX().left().padBottom(upgradePad2);
		if(energyStart < 5){
			Image energyStartPrice = upgradesPriceImages[energyStart + 4];
			if(energyMeter == energyStart)
				Tween.set(energyStartPrice, ImageAccessor.COLOR).target(0.5f, 0.5f, 0.5f, 1f).start(tweenManager);
			imgsUpg.add(energyStartPrice).width(Math.abs(imgsUpg.getWidth() - auxTableSpeed.getWidth())).height(Math.abs(imgsUpg.getWidth() - auxTableSpeed.getWidth())).padLeft(energyStartPrice.getWidth()/5).left().padBottom(upgradePad2);
		}
		imgsUpg.row();
		
		final Table auxTableObjects = new Table();
		auxTableObjects.setSize(upgradeFull.getWidth()*3, upgradeFull.getHeight() + speedLabel.getHeight());
		auxTableObjects.add(objectsLabel).width(speedLabel.getWidth()).height(speedLabel.getHeight()).padTop(upgradePad2).left().colspan(5);
		auxTableObjects.row();
		upgradeBars[3] = new UpgradeBar(game.assetManager, fx(1.55f), objectsSlots/3.0f);
		auxTableObjects.add(upgradeBars[3]).width(upgradeBars[3].getPrefWidth()).height(upgradeBars[3].getPrefHeight()).left().padTop(upgradePad1).padBottom(upgradePad2);
		imgsUpg.add(auxTableObjects).expandX().left().padBottom(upgradePad2);
		if(objectsSlots < 3){
			Image objectsPrice = upgradesPriceImages[objectsSlots + 8];
			imgsUpg.add(objectsPrice).width(Math.abs(imgsUpg.getWidth() - auxTableSpeed.getWidth())).height(Math.abs(imgsUpg.getWidth() - auxTableSpeed.getWidth())).padLeft(objectsPrice.getWidth()/5).left().padBottom(upgradePad2);	
		}imgsUpg.row();
		
		final Table auxTableObjectsStart = new Table();
		auxTableObjectsStart.setSize(upgradeFull.getWidth()*3, upgradeFull.getHeight() + speedLabel.getHeight());
		auxTableObjectsStart.add(objectsStartLabel).width(speedLabel.getWidth()).height(speedLabel.getHeight()).padTop(upgradePad2).left().colspan(5);
		auxTableObjectsStart.row();
		upgradeBars[4] = new UpgradeBar(game.assetManager, fx(1.55f), objectStart/3.0f);
		auxTableObjectsStart.add(upgradeBars[4]).width(upgradeBars[4].getPrefWidth()).height(upgradeBars[4].getPrefHeight()).left().padTop(upgradePad1).padBottom(upgradePad2);
		imgsUpg.add(auxTableObjectsStart).expandX().left().padBottom(upgradePad2);
		if(objectStart < 3){
			Image objectsStartPrice = upgradesPriceImages[objectStart + 11];
			if(objectsSlots == objectStart)
				Tween.set(objectsStartPrice, ImageAccessor.COLOR).target(0.5f, 0.5f, 0.5f, 1f).start(tweenManager);
			imgsUpg.add(objectsStartPrice).width(Math.abs(imgsUpg.getWidth() - auxTableSpeed.getWidth())).height(Math.abs(imgsUpg.getWidth() - auxTableSpeed.getWidth())).padLeft(objectsStartPrice.getWidth()/5).left().padBottom(upgradePad2);	
		}
		imgsUpg.row();
		
		final Table auxTableObjectsDuration = new Table();
		auxTableObjectsDuration.setSize(upgradeFull.getWidth()*5, upgradeFull.getHeight() + speedLabel.getHeight());
		auxTableObjectsDuration.add(objectsDurationLabel).width(speedLabel.getWidth()).height(speedLabel.getHeight()).padTop(upgradePad2).left().colspan(5);
		auxTableObjectsDuration.row();
		upgradeBars[5] = new UpgradeBar(game.assetManager, fx(1.55f), objectsDuration/5.0f);
		auxTableObjectsDuration.add(upgradeBars[5]).width(upgradeBars[5].getPrefWidth()).height(upgradeBars[5].getPrefHeight()).left().padTop(upgradePad1).padBottom(upgradePad2);
		imgsUpg.add(auxTableObjectsDuration).expandX().left().padBottom(upgradePad2);
		if(objectsDuration < 5){
			Image objectsDurationPrice = upgradesPriceImages[objectsDuration + 13];
			imgsUpg.add(objectsDurationPrice).width(Math.abs(imgsUpg.getWidth() - auxTableSpeed.getWidth())).height(Math.abs(imgsUpg.getWidth() - auxTableSpeed.getWidth())).padLeft(objectsDurationPrice.getWidth()/5).left().padBottom(upgradePad2);
		}
		imgsUpg.row();
	
		Image bBackIUpg = new Image(bBack);
		containerUpg.add(bBackIUpg).width(bBack.getWidth()).height(bBack.getHeight()).left();
		containerUpg.add(upgLabel).width(upgLabel.getWidth()).height(upgLabel.getHeight()).left().expandX().padLeft(upgradePad2);
		containerUpg.row();
		containerUpg.add(scrollUpg).width(3*Gdx.graphics.getWidth()/4).height(containerShips.getHeight() - bBack.getHeight() - 2*upgradePad2).padTop(upgradePad2).colspan(2);
	
		bBackIUpg.addListener(new ClickListener(){
			public void clicked(InputEvent event, float x, float y){
				state = STANDBY;
				if(sound == ON)
					buttonSmall.play();
				stageUpgrades.addAction(moveTo(0, stageUpgrades.getHeight(), 0.5f));
				effectsMenuOff();
				updateStatsMenuSpent();
			}
		});	
		
		for(int k = 0; k < 18; k++){
			final int i = k;
			upgradesPriceImages[i].addListener(new ClickListener(){
				public void clicked(InputEvent event, float x, float y){
					if(canSelect){
						canSelect = false;
						if((i == 7 && energyMeter < 4) || (i == 8 && energyMeter < 5) || (i == 12 && objectsSlots < 2)|| (i == 13 && objectsSlots < 3)){
							
						}else if(Integer.valueOf(lifeBalls) >= upgradesPrice[Integer.valueOf(event.getListenerActor().getName())]){
							
							if(sound == ON)
								bought.play();
								
							upgrades++;
							boughtUpgrade = true;
							spentLifes = spentLifes + upgradesPrice[i];
							lifeBalls -= upgradesPrice[i];
							
							final int des;
							final Table auxTable;
							
							if(i < 4){
								des = 1;
								auxTable = auxTableSpeed;
								shipSpeed++;
								up1 = true;
							}else if(i < 6){
								des = 4;
								auxTable = auxTableEnergy;
								energyMeter++;
								up2 = true;
							}else if(i < 9){
								des = 6;
								auxTable = auxTableEnergyStart;
								energyStart++;
								up3 = true;
							}else if(i < 11){
								des = 7;
								auxTable = auxTableObjects;
								objectsSlots++;
								up4 = true;
							}else if(i < 14){
								des = 7;
								auxTable = auxTableObjectsStart;
								objectStart++;
								up5 = true;
							}else{
								des = 8;
								auxTable = auxTableObjectsDuration;
								objectsDuration++;
								up6 = true;
							}
							
							final int barIndex = i < 4 ? 0 : i < 6 ? 1 : i < 9 ? 2 : i < 11 ? 3 : i < 14 ? 4 : 5;
							final int newLevel = barIndex == 0 ? shipSpeed : barIndex == 1 ? energyMeter : barIndex == 2 ? energyStart : barIndex == 3 ? objectsSlots : barIndex == 4 ? objectStart : objectsDuration;
							upgradeBars[barIndex].setLevel(newLevel/(float) UPGRADE_MAX[barIndex]);
							Tween.to(upgradesPriceImages[i], ImageAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f).start(tweenManager);
							Timer.schedule(new Task(){
								@Override
								public void run(){
									if(i != 3 && i != 5 && i != 8 && i != 10 && i != 13 && i != 17){
										Tween.set(upgradesPriceImages[i + 1], ImageAccessor.COLOR).target(1f, 1f, 1f, 0f).start(tweenManager);
										imgsUpg.getCell(upgradesPriceImages[i]).setActor(upgradesPriceImages[i+1]);
										if(i == 6){
											if(energyMeter >= 4){
												Tween.to(upgradesPriceImages[i + 1], ImageAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f).start(tweenManager);
											}else
												Tween.to(upgradesPriceImages[i + 1], ImageAccessor.COLOR, 0.25f).target(0.5f, 0.5f, 0.5f, 1f).start(tweenManager);
										}else if(i == 7){
											if(energyMeter == 5){
												Tween.to(upgradesPriceImages[i + 1], ImageAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f).start(tweenManager);
											}else
												Tween.to(upgradesPriceImages[i + 1], ImageAccessor.COLOR, 0.25f).target(0.5f, 0.5f, 0.5f, 1f).start(tweenManager);
										}else if(i == 4){
											Tween.to(upgradesPriceImages[i + 1], ImageAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f).start(tweenManager);
											Tween.to(upgradesPriceImages[7], ImageAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f).start(tweenManager);
										}else if(i == 11){
											if(objectsSlots >= 2){
												Tween.to(upgradesPriceImages[i + 1], ImageAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f).start(tweenManager);
											}else
												Tween.to(upgradesPriceImages[i + 1], ImageAccessor.COLOR, 0.25f).target(0.5f, 0.5f, 0.5f, 1f).start(tweenManager);
										}else if(i == 12){
											if(objectsSlots == 3){
												Tween.to(upgradesPriceImages[i + 1], ImageAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f).start(tweenManager);
											}else
												Tween.to(upgradesPriceImages[i + 1], ImageAccessor.COLOR, 0.25f).target(0.5f, 0.5f, 0.5f, 1f).start(tweenManager);
										}else if(i == 9){
											Tween.to(upgradesPriceImages[i + 1], ImageAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f).start(tweenManager);
											Tween.to(upgradesPriceImages[12], ImageAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f).start(tweenManager);
										}else
											Tween.to(upgradesPriceImages[i + 1], ImageAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f).start(tweenManager);
									}else{
										upgradesPriceImages[i].remove();			
										if(i == 5)
											Tween.to(upgradesPriceImages[8], ImageAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f).start(tweenManager);
										if(i == 10)	
											Tween.to(upgradesPriceImages[13], ImageAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f).start(tweenManager);
									}	
								}
							}, 0.25f);
							
						}else{
							if(sound == ON)
								cant.play(1.5f);
							Tween.to(upgradesPriceImages[i], ImageAccessor.COLOR, 0.1f).target(0.5f, 0.5f, 0.5f, 1f).repeatYoyo(1, 0).start(tweenManager);
						}
						Timer.schedule(new Task(){
							@Override
							public void run(){
								canSelect = true;
							}
						}, 0.25f);
					}	
				}
			});		
		}
		
//		containerUpg.debug();
//		imgsUpg.debug();
//		auxTableSpeed.debug();
//		auxTableEnergy.debug();
//		auxTableEnergyStart.debug();
//		auxTableObjects.debug();
//		auxTableObjectsStart.debug();
//		auxTableObjectsDuration.debug();
	}
	
	public void setUpHelpMenu(){
		stageHelp = new Stage();
		containerHelp = new Table();
		stageHelp.addActor(containerHelp);
		containerHelp.setBounds(Gdx.graphics.getWidth()/8 , counterBaseY + 1.5f*blueCounter.getHeight(), 
				3*Gdx.graphics.getWidth()/4, Gdx.graphics.getHeight() - 0.225f*Gdx.graphics.getHeight() - adHeight);	
		imgsHelp = new Table();
		scrollHelp = new ScrollPane(imgsHelp);
		scrollHelp.setScrollingDisabled(true, false);
		
		if(language == ON)
			tutoLabel = new Label("Help", skin);
		else
			tutoLabel = new Label("Ayuda", skin);
		tutoLabel.setFontScale(x, y);
		tutoLabel.setSize(fx(tutoLabel.getWidth()), fy(tutoLabel.getHeight()));

		if(language == ON)
			padsTitleLabel = new Label("Controls", skin);
		else 
			padsTitleLabel = new Label("Controles", skin);
		padsTitleLabel.setFontScale(x/1.5f, y/1.5f);
		padsTitleLabel.setSize(fx(padsTitleLabel.getWidth()/1.5f), fy(padsTitleLabel.getHeight()/1.5f));

		if(language == ON)
			orbsLabel = new Label("Orbs", skin);
		else
			orbsLabel = new Label("Orbes", skin);
		orbsLabel.setFontScale(x/1.5f, y/1.5f);
		orbsLabel.setSize(fx(orbsLabel.getWidth()/1.5f), fy(orbsLabel.getHeight()/1.5f));

		if(language == ON)
			itemsLabel = new Label("Items", skin);
		else
			itemsLabel = new Label("Objetos", skin);
		itemsLabel.setFontScale(x/1.5f, y/1.5f);
		itemsLabel.setSize(fx(itemsLabel.getWidth()/1.5f), fy(itemsLabel.getHeight()/1.5f));

		float helpPad = padsTitleLabel.getHeight()/2.25f;
		
		if(language == ON)
			imgsHelp.add(instEN).padBottom(helpPad).width(containerHelp.getWidth() - helpPad).height(((containerHelp.getWidth() - helpPad)*instEN.getHeight())/instEN.getWidth());
		else
			imgsHelp.add(instES).padBottom(helpPad).width(containerHelp.getWidth() - helpPad).height(((containerHelp.getWidth() - helpPad)*instES.getHeight())/instES.getWidth());
		imgsHelp.row();
		
		imgsHelp.add(padsTitleLabel).height(padsTitleLabel.getHeight()).padBottom(helpPad/2);
		imgsHelp.row();
		if(language == ON)
			imgsHelp.add(padsControlsEN).padBottom(helpPad).width(containerHelp.getWidth() - helpPad).height(((containerHelp.getWidth() - helpPad)*padsControlsEN.getHeight())/padsControlsEN.getWidth());
		else
			imgsHelp.add(padsControlsES).padBottom(helpPad).width(containerHelp.getWidth() - helpPad).height(((containerHelp.getWidth() - helpPad)*padsControlsES.getHeight())/padsControlsES.getWidth());
		imgsHelp.row();
		
		imgsHelp.add(orbsLabel).height(orbsLabel.getHeight()).padBottom(helpPad/2);
		imgsHelp.row();
		if(language == ON)
			imgsHelp.add(orbsEN).padBottom(helpPad).width(containerHelp.getWidth() - helpPad).height(((containerHelp.getWidth() - helpPad)*orbsEN.getHeight())/orbsEN.getWidth());
		else
			imgsHelp.add(orbsES).padBottom(helpPad).width(containerHelp.getWidth() - helpPad).height(((containerHelp.getWidth() - helpPad)*orbsES.getHeight())/orbsES.getWidth());
			imgsHelp.row();
		
		imgsHelp.add(itemsLabel).height(itemsLabel.getHeight()).padBottom(helpPad/2);
		imgsHelp.row();
		if(language == ON)
			imgsHelp.add(itemsEN).padBottom(helpPad).width(containerHelp.getWidth() - helpPad).height(((containerHelp.getWidth() - helpPad)*itemsEN.getHeight())/itemsEN.getWidth());
		else
			imgsHelp.add(itemsES).padBottom(helpPad).width(containerHelp.getWidth() - helpPad).height(((containerHelp.getWidth() - helpPad)*itemsES.getHeight())/itemsES.getWidth());
		imgsHelp.row();
		// the ship art is CC BY 4.0: credit the author (main-menu Help only, not the pause Help)
		Label creditsLabel = new Label("Credits\nShip art by MillionthVector (Alan Guyant), CC BY 4.0, modified.\nmillionthvector.blogspot.com", skin);
		creditsLabel.setFontScale(x/4.5f, y/4.5f);
		creditsLabel.setWrap(true);
		creditsLabel.setAlignment(Align.left);
		imgsHelp.add(creditsLabel).left().fillX().width(containerHelp.getWidth() - helpPad).padBottom(helpPad);
		imgsHelp.row();
		
		Image bBackITuto = new Image(bBack);
		containerHelp.add(bBackITuto).width(bBack.getWidth()).height(bBack.getHeight()).left();
		containerHelp.add(tutoLabel).width(tutoLabel.getWidth()).height(tutoLabel.getHeight()).left().expandX().padLeft(helpPad);
		containerHelp.row();
		containerHelp.add(scrollHelp).width(3*Gdx.graphics.getWidth()/4).height(containerShips.getHeight() - bBack.getHeight() - 2*helpPad).padTop(helpPad).colspan(2);
	
		bBackITuto.addListener(new ClickListener(){
			public void clicked(InputEvent event, float x, float y){
				state = STANDBY;
				if(sound == ON)
					buttonSmall.play();
				stageHelp.addAction(moveTo(0, stageHelp.getHeight(), 0.5f));
				effectsMenuOff();
			}
		});	
		
//		containerHelp.debug();
//		imgsHelp.debug();
	}


	public void updateStatsMenuSpent(){
		spentBluesLabeln.setText(String.valueOf(spentBlues));
		spentBluesLabeln.setAlignment(Align.right);
		spentRedsLabeln.setText(String.valueOf(spentReds));
		spentRedsLabeln.setAlignment(Align.right);
		spentLifesLabeln.setText(String.valueOf(spentLifes));
		spentLifesLabeln.setAlignment(Align.right);
		spentLabeln.setText(String.valueOf(spentLifes + spentReds + spentBlues));
		spentLabeln.setAlignment(Align.right);
	}
	
	public void updateStatsMenu(){
		gamesLabeln.setText(String.valueOf(games));
		gamesLabeln.setAlignment(Align.right);
		totalPointsLabeln.setText(String.valueOf(totalPoints));
		totalPointsLabeln.setAlignment(Align.right);
		
		double r;
		if(games > 0){
		    r = (double)totalPoints/(double)games;
			BigDecimal bd = new BigDecimal(r);
			bd = bd.setScale(1, BigDecimal.ROUND_UP);
			r = bd.doubleValue();
		}else
			r = 0;
		avgPointsLabeln.setText(String.valueOf(r));
		avgPointsLabeln.setAlignment(Align.right);
		
		totalOrbsLabeln.setText(String.valueOf(totalOrbs));
		totalOrbsLabeln.setAlignment(Align.right);
		totalBluesLabeln.setText(String.valueOf(totalBlues));
		totalBluesLabeln.setAlignment(Align.right);
		totalRedsLabeln.setText(String.valueOf(totalReds));
		totalRedsLabeln.setAlignment(Align.right);
		totalLifesLabeln.setText(String.valueOf(totalLifes));
		totalLifesLabeln.setAlignment(Align.right);
		totalObjectsLabeln.setText(String.valueOf(totalObjects));
		totalObjectsLabeln.setAlignment(Align.right);
		timePlayingLabeln.setText(String.valueOf(timePlaying/3600) + ":" + String.valueOf((timePlaying % 3600)/60) + ":" + String.valueOf((timePlaying % 3600) % 60));
		timePlayingLabeln.setAlignment(Align.right);
		obj0Labeln.setText(String.valueOf(obj[0]));
		obj0Labeln.setAlignment(Align.right);
		obj1Labeln.setText(String.valueOf(obj[1]));
		obj1Labeln.setAlignment(Align.right);
		obj2Labeln.setText(String.valueOf(obj[2]));
		obj2Labeln.setAlignment(Align.right);
		obj3Labeln.setText(String.valueOf(obj[3]));
		obj3Labeln.setAlignment(Align.right);
		obj4Labeln.setText(String.valueOf(obj[4]));
		obj4Labeln.setAlignment(Align.right);
		obj5Labeln.setText(String.valueOf(obj[5]));
		obj5Labeln.setAlignment(Align.right);
		obj6Labeln.setText(String.valueOf(obj[6]));
		obj6Labeln.setAlignment(Align.right);
		obj7Labeln.setText(String.valueOf(obj[7]));
		obj7Labeln.setAlignment(Align.right);
		obj8Labeln.setText(String.valueOf(obj[8]));
		obj8Labeln.setAlignment(Align.right);
		obj9Labeln.setText(String.valueOf(obj[9]));
		obj9Labeln.setAlignment(Align.right);
	}
	
	public void setUpStatsMenu(){
		stageStats = new Stage();
		containerSts = new Table();
		stageStats.addActor(containerSts);
		containerSts.setBounds(blueCounter.getX(), counterBaseY + 1.5f*blueCounter.getHeight(), 
				redCounter.getX() + redCounter.getWidth() - blueCounter.getX(), Gdx.graphics.getHeight() - 0.225f*Gdx.graphics.getHeight() - adHeight);	
		imgsSts = new Table();
		scrollSts = new ScrollPane(imgsSts);
		scrollSts.setScrollingDisabled(true, false);
		
		if(language == ON){
			stsLabel = new Label("Stats", skin);
			stsLabel.setFontScale(x, y);
			stsLabel.setSize(fx(stsLabel.getWidth()), fy(stsLabel.getHeight()));
			
			gamesLabel = new Label("Games Played: ", skin);
			gamesLabel.setFontScale(x/2, y/2);
			gamesLabel.setSize(fx(gamesLabel.getWidth()/2), fy(gamesLabel.getHeight()/2));
			gamesLabeln = new Label(String.valueOf(games), skin);
			gamesLabeln.setFontScale(x/2, y/2);
			gamesLabeln.setSize(fx(gamesLabeln.getWidth()/2), fy(gamesLabeln.getHeight()/2));
			
			totalPointLabel = new Label("Points scored: ", skin);
			totalPointLabel.setFontScale(x/2, y/2);
			totalPointLabel.setSize(fx(totalPointLabel.getWidth()/2), fy(totalPointLabel.getHeight()/2));
			totalPointsLabeln = new Label(String.valueOf(totalPoints), skin);
			totalPointsLabeln.setFontScale(x/2, y/2);
			totalPointsLabeln.setSize(fx(totalPointsLabeln.getWidth()/2), fy(totalPointsLabeln.getHeight()/2));
			
			avgPointsLabel = new Label("Points/game avg: ", skin);
			avgPointsLabel.setFontScale(x/2, y/2);
			avgPointsLabel.setSize(fx(avgPointsLabel.getWidth()/2), fy(avgPointsLabel.getHeight()/2));
			if(games > 0){
				double r = (double)totalPoints/(double)games;
				BigDecimal bd = new BigDecimal(r);
				bd = bd.setScale(1, BigDecimal.ROUND_UP);
				r = bd.doubleValue();
				avgPointsLabeln = new Label(String.valueOf((double) r), skin);
			}
				avgPointsLabeln = new Label(String.valueOf(0), skin);
			avgPointsLabeln.setFontScale(x/2, y/2);
			avgPointsLabeln.setSize(fx(avgPointsLabeln.getWidth()/2), fy(avgPointsLabeln.getHeight()/2));
			
			totalOrbsLabel = new Label("Orbs collected: ", skin);
			totalOrbsLabel.setFontScale(x/2, y/2);
			totalOrbsLabel.setSize(fx(totalOrbsLabel.getWidth()/2), fy(totalOrbsLabel.getHeight()/2));
			totalOrbsLabeln = new Label(String.valueOf(totalOrbs), skin);
			totalOrbsLabeln.setFontScale(x/2, y/2);
			totalOrbsLabeln.setSize(fx(totalOrbsLabeln.getWidth()/2), fy(totalOrbsLabeln.getHeight()/2));
			
			totalBluesLabel = new Label("   Blue orbs: ", skin);
			totalBluesLabel.setFontScale(x/2, y/2);
			totalBluesLabel.setSize(fx(totalBluesLabel.getWidth()/2), fy(totalBluesLabel.getHeight()/2));
			totalBluesLabeln = new Label(String.valueOf(totalBlues), skin);
			totalBluesLabeln.setFontScale(x/2, y/2);
			totalBluesLabeln.setSize(fx(totalBluesLabeln.getWidth()/2), fy(totalBluesLabeln.getHeight()/2));
			
			totalRedsLabel = new Label("   Red orbs: ", skin);
			totalRedsLabel.setFontScale(x/2, y/2);
			totalRedsLabel.setSize(fx(totalRedsLabel.getWidth()/2), fy(totalRedsLabel.getHeight()/2));
			totalRedsLabeln = new Label(String.valueOf(totalReds), skin);
			totalRedsLabeln.setFontScale(x/2, y/2);
			totalRedsLabeln.setSize(fx(totalRedsLabeln.getWidth()/2), fy(totalRedsLabeln.getHeight()/2));
			
			totalLifesLabel = new Label("   Energy orbs: ", skin);
			totalLifesLabel.setFontScale(x/2, y/2);
			totalLifesLabel.setSize(fx(totalLifesLabel.getWidth()/2), fy(totalLifesLabel.getHeight()/2));
			totalLifesLabeln = new Label(String.valueOf(totalLifes), skin);
			totalLifesLabeln.setFontScale(x/2, y/2);
			totalLifesLabeln.setSize(fx(totalLifesLabeln.getWidth()/2), fy(totalLifesLabeln.getHeight()/2));
			
			totalObjectsLabel = new Label("   Object orbs: ", skin);
			totalObjectsLabel.setFontScale(x/2, y/2);
			totalObjectsLabel.setSize(fx(totalObjectsLabel.getWidth()/2), fy(totalObjectsLabel.getHeight()/2));
			totalObjectsLabeln = new Label(String.valueOf(totalObjects), skin);
			totalObjectsLabeln.setFontScale(x/2, y/2);
			totalObjectsLabeln.setSize(fx(totalObjectsLabeln.getWidth()/2), fy(totalObjectsLabeln.getHeight()/2));

			spentLabel = new Label("Orbs spent: ", skin);
			spentLabel.setFontScale(x/2, y/2);
			spentLabel.setSize(fx(spentLabel.getWidth()/2), fy(spentLabel.getHeight()/2));
			spentLabeln = new Label(String.valueOf(spentBlues + spentReds + spentLifes), skin);
			spentLabeln.setFontScale(x/2, y/2);
			spentLabeln.setSize(fx(spentLabeln.getWidth()/2), fy(spentLabeln.getHeight()/2));
			
			spentBluesLabel = new Label("   Blue orbs: ", skin);
			spentBluesLabel.setFontScale(x/2, y/2);
			spentBluesLabel.setSize(fx(spentBluesLabel.getWidth()/2), fy(spentBluesLabel.getHeight()/2));
			spentBluesLabeln = new Label(String.valueOf(spentBlues), skin);
			spentBluesLabeln.setFontScale(x/2, y/2);
			spentBluesLabeln.setSize(fx(spentBluesLabeln.getWidth()/2), fy(spentBluesLabeln.getHeight()/2));
			
			spentRedsLabel = new Label("   Red orbs:", skin);
			spentRedsLabel.setFontScale(x/2, y/2);
			spentRedsLabel.setSize(fx(spentRedsLabel.getWidth()/2), fy(spentRedsLabel.getHeight()/2));
			spentRedsLabeln = new Label(String.valueOf(spentReds), skin);
			spentRedsLabeln.setFontScale(x/2, y/2);
			spentRedsLabeln.setSize(fx(spentRedsLabeln.getWidth()/2), fy(spentRedsLabeln.getHeight()/2));
			
			spentLifesLabel = new Label("   Energy orbs:", skin);
			spentLifesLabel.setFontScale(x/2, y/2);
			spentLifesLabel.setSize(fx(spentLifesLabel.getWidth()/2), fy(spentLifesLabel.getHeight()/2));
			spentLifesLabeln = new Label(String.valueOf(spentLifes), skin);
			spentLifesLabeln.setFontScale(x/2, y/2);
			spentLifesLabeln.setSize(fx(spentLifesLabeln.getWidth()/2), fy(spentLifesLabeln.getHeight()/2));
			
			timePlayingLabel = new Label("Time playing:", skin);
			timePlayingLabel.setFontScale(x/2, y/2);
			timePlayingLabel.setSize(fx(timePlayingLabel.getWidth()/2), fy(timePlayingLabel.getHeight()/2));
			timePlayingLabeln = new Label(String.valueOf(timePlaying/3600) + ":" + String.valueOf((timePlaying % 3600)/60) + ":" + String.valueOf((timePlaying % 3600) % 60), skin);
			timePlayingLabeln.setFontScale(x/2, y/2);
			timePlayingLabeln.setSize(fx(timePlayingLabeln.getWidth()/2), fy(timePlayingLabeln.getHeight()/2));
	
			obj0Label = new Label("      Red-to-blue:", skin);
			obj0Label.setFontScale(x/2, y/2);
			obj0Label.setSize(fx(obj0Label.getWidth()/2), fy(obj0Label.getHeight()/2));
			obj0Labeln = new Label(String.valueOf(obj[0]), skin);
			obj0Labeln.setFontScale(x/2, y/2);
			obj0Labeln.setSize(fx(obj0Labeln.getWidth()/2), fy(obj0Labeln.getHeight()/2));
			
			obj1Label = new Label("      Blue-to-red:", skin);
			obj1Label.setFontScale(x/2, y/2);
			obj1Label.setSize(fx(obj1Label.getWidth()/2), fy(obj1Label.getHeight()/2));
			obj1Labeln = new Label(String.valueOf(obj[1]), skin);
			obj1Labeln.setFontScale(x/2, y/2);
			obj1Labeln.setSize(fx(obj1Labeln.getWidth()/2), fy(obj1Labeln.getHeight()/2));
			 
			obj2Label = new Label("      Blue orbs X2:", skin);
			obj2Label.setFontScale(x/2, y/2);
			obj2Label.setSize(fx(obj2Label.getWidth()/2), fy(obj2Label.getHeight()/2));
			obj2Labeln = new Label(String.valueOf(obj[2]), skin);
			obj2Labeln.setFontScale(x/2, y/2);
			obj2Labeln.setSize(fx(obj2Labeln.getWidth()/2), fy(obj2Labeln.getHeight()/2));
			
			obj3Label = new Label("      Red orbs X2:", skin);
			obj3Label.setFontScale(x/2, y/2);
			obj3Label.setSize(fx(obj3Label.getWidth()/2), fy(obj3Label.getHeight()/2));
			obj3Labeln = new Label(String.valueOf(obj[3]), skin);
			obj3Labeln.setFontScale(x/2, y/2);
			obj3Labeln.setSize(fx(obj3Labeln.getWidth()/2), fy(obj3Labeln.getHeight()/2));
			
			obj4Label = new Label("      Slow time:", skin);
			obj4Label.setFontScale(x/2, y/2);
			obj4Label.setSize(fx(obj4Label.getWidth()/2), fy(obj4Label.getHeight()/2));
			obj4Labeln = new Label(String.valueOf(obj[4]), skin);
			obj4Labeln.setFontScale(x/2, y/2);
			obj4Labeln.setSize(fx(obj4Labeln.getWidth()/2), fy(obj4Labeln.getHeight()/2));
			
			obj5Label = new Label("      Blue shield:", skin);
			obj5Label.setFontScale(x/2, y/2);
			obj5Label.setSize(fx(obj5Label.getWidth()/2), fy(obj5Label.getHeight()/2));
			obj5Labeln = new Label(String.valueOf(obj[5]), skin);
			obj5Labeln.setFontScale(x/2, y/2);
			obj5Labeln.setSize(fx(obj5Labeln.getWidth()/2), fy(obj5Labeln.getHeight()/2));
			
			obj6Label = new Label("      Red shield:", skin);
			obj6Label.setFontScale(x/2, y/2);
			obj6Label.setSize(fx(obj6Label.getWidth()/2), fy(obj6Label.getHeight()/2));
			obj6Labeln = new Label(String.valueOf(obj[6]), skin);
			obj6Labeln.setFontScale(x/2, y/2);
			obj6Labeln.setSize(fx(obj6Labeln.getWidth()/2), fy(obj6Labeln.getHeight()/2));
			
			obj7Label = new Label("      Blue orbs X3:", skin);
			obj7Label.setFontScale(x/2, y/2);
			obj7Label.setSize(fx(obj7Label.getWidth()/2), fy(obj7Label.getHeight()/2));
			obj7Labeln = new Label(String.valueOf(obj[7]), skin);
			obj7Labeln.setFontScale(x/2, y/2);
			obj7Labeln.setSize(fx(obj7Labeln.getWidth()/2), fy(obj7Labeln.getHeight()/2));
			
			obj8Label = new Label("      Red orbs X3:", skin);
			obj8Label.setFontScale(x/2, y/2);
			obj8Label.setSize(fx(obj8Label.getWidth()/2), fy(obj8Label.getHeight()/2));
			obj8Labeln = new Label(String.valueOf(obj[8]), skin);
			obj8Labeln.setFontScale(x/2, y/2);
			obj8Labeln.setSize(fx(obj8Labeln.getWidth()/2), fy(obj8Labeln.getHeight()/2));
	
			obj9Label = new Label("      Gold shield:", skin);
			obj9Label.setFontScale(x/2, y/2);
			obj9Label.setSize(fx(obj9Label.getWidth()/2), fy(obj9Label.getHeight()/2));
			obj9Labeln = new Label(String.valueOf(obj[9]), skin);
			obj9Labeln.setFontScale(x/2, y/2);
			obj9Labeln.setSize(fx(obj9Labeln.getWidth()/2), fy(obj9Labeln.getHeight()/2));
		}else{
			stsLabel = new Label("Estadisticas", skin);
			stsLabel.setFontScale(x/1.25f, y/1.25f);
			stsLabel.setSize(fx(stsLabel.getWidth()/1.25f), fy(stsLabel.getHeight()));
			
			gamesLabel = new Label("Partidas jugadas: ", skin);
			gamesLabel.setFontScale(x/2.25f, y/2.25f);
			gamesLabel.setSize(fx(gamesLabel.getWidth()/2), fy(gamesLabel.getHeight()/2));
			gamesLabeln = new Label(String.valueOf(games), skin);
			gamesLabeln.setFontScale(x/2, y/2);
			gamesLabeln.setSize(fx(gamesLabeln.getWidth()/2), fy(gamesLabeln.getHeight()/2));
			
			totalPointLabel = new Label("Total de puntos: ", skin);
			totalPointLabel.setFontScale(x/2.25f, y/2.25f);
			totalPointLabel.setSize(fx(totalPointLabel.getWidth()/2), fy(totalPointLabel.getHeight()/2));
			totalPointsLabeln = new Label(String.valueOf(totalPoints), skin);
			totalPointsLabeln.setFontScale(x/2, y/2);
			totalPointsLabeln.setSize(fx(totalPointsLabeln.getWidth()/2), fy(totalPointsLabeln.getHeight()/2));
			
			avgPointsLabel = new Label("Prom. pts/partidas: ", skin);
			avgPointsLabel.setFontScale(x/2.25f, y/2.25f);
			avgPointsLabel.setSize(fx(avgPointsLabel.getWidth()/2), fy(avgPointsLabel.getHeight()/2));
			if(games > 0){
				double r = (double)totalPoints/(double)games;
				BigDecimal bd = new BigDecimal(r);
				bd = bd.setScale(1, BigDecimal.ROUND_UP);
				r = bd.doubleValue();
				avgPointsLabeln = new Label(String.valueOf((double) r), skin);
			}
				avgPointsLabeln = new Label(String.valueOf(0), skin);
			avgPointsLabeln.setFontScale(x/2, y/2);
			avgPointsLabeln.setSize(fx(avgPointsLabeln.getWidth()/2), fy(avgPointsLabeln.getHeight()/2));
			
			totalOrbsLabel = new Label("Orbes conseguidos: ", skin);
			totalOrbsLabel.setFontScale(x/2.25f, y/2.25f);
			totalOrbsLabel.setSize(fx(totalOrbsLabel.getWidth()/2), fy(totalOrbsLabel.getHeight()/2));
			totalOrbsLabeln = new Label(String.valueOf(totalOrbs), skin);
			totalOrbsLabeln.setFontScale(x/2, y/2);
			totalOrbsLabeln.setSize(fx(totalOrbsLabeln.getWidth()/2), fy(totalOrbsLabeln.getHeight()/2));
			
			totalBluesLabel = new Label("   Orbes azules: ", skin);
			totalBluesLabel.setFontScale(x/2.25f, y/2.25f);
			totalBluesLabel.setSize(fx(totalBluesLabel.getWidth()/2), fy(totalBluesLabel.getHeight()/2));
			totalBluesLabeln = new Label(String.valueOf(totalBlues), skin);
			totalBluesLabeln.setFontScale(x/2, y/2);
			totalBluesLabeln.setSize(fx(totalBluesLabeln.getWidth()/2), fy(totalBluesLabeln.getHeight()/2));
			
			totalRedsLabel = new Label("   Orbes rojos: ", skin);
			totalRedsLabel.setFontScale(x/2.25f, y/2.25f);
			totalRedsLabel.setSize(fx(totalRedsLabel.getWidth()/2), fy(totalRedsLabel.getHeight()/2));
			totalRedsLabeln = new Label(String.valueOf(totalReds), skin);
			totalRedsLabeln.setFontScale(x/2, y/2);
			totalRedsLabeln.setSize(fx(totalRedsLabeln.getWidth()/2), fy(totalRedsLabeln.getHeight()/2));
			
			totalLifesLabel = new Label("   Orbes de energia: ", skin);
			totalLifesLabel.setFontScale(x/2.25f, y/2.25f);
			totalLifesLabel.setSize(fx(totalLifesLabel.getWidth()/2), fy(totalLifesLabel.getHeight()/2));
			totalLifesLabeln = new Label(String.valueOf(totalLifes), skin);
			totalLifesLabeln.setFontScale(x/2, y/2);
			totalLifesLabeln.setSize(fx(totalLifesLabeln.getWidth()/2), fy(totalLifesLabeln.getHeight()/2));
			
			totalObjectsLabel = new Label("   Orbes de objetos: ", skin);
			totalObjectsLabel.setFontScale(x/2.25f, y/2.25f);
			totalObjectsLabel.setSize(fx(totalObjectsLabel.getWidth()/2), fy(totalObjectsLabel.getHeight()/2));
			totalObjectsLabeln = new Label(String.valueOf(totalObjects), skin);
			totalObjectsLabeln.setFontScale(x/2, y/2);
			totalObjectsLabeln.setSize(fx(totalObjectsLabeln.getWidth()/2), fy(totalObjectsLabeln.getHeight()/2));
			
			spentLabel = new Label("Orbes gastados: ", skin);
			spentLabel.setFontScale(x/2.25f, y/2.25f);
			spentLabel.setSize(fx(spentLabel.getWidth()/2), fy(spentLabel.getHeight()/2));
			spentLabeln = new Label(String.valueOf(spentBlues + spentReds + spentLifes), skin);
			spentLabeln.setFontScale(x/2, y/2);
			spentLabeln.setSize(fx(spentLabeln.getWidth()/2), fy(spentLabeln.getHeight()/2));
			
			spentBluesLabel = new Label("   Orbes azules: ", skin);
			spentBluesLabel.setFontScale(x/2.25f, y/2.25f);
			spentBluesLabel.setSize(fx(spentBluesLabel.getWidth()/2), fy(spentBluesLabel.getHeight()/2));
			spentBluesLabeln = new Label(String.valueOf(spentBlues), skin);
			spentBluesLabeln.setFontScale(x/2, y/2);
			spentBluesLabeln.setSize(fx(spentBluesLabeln.getWidth()/2), fy(spentBluesLabeln.getHeight()/2));
			
			spentRedsLabel = new Label("   Orbes rojos: ", skin);
			spentRedsLabel.setFontScale(x/2.25f, y/2.25f);
			spentRedsLabel.setSize(fx(spentRedsLabel.getWidth()/2), fy(spentRedsLabel.getHeight()/2));
			spentRedsLabeln = new Label(String.valueOf(spentReds), skin);
			spentRedsLabeln.setFontScale(x/2, y/2);
			spentRedsLabeln.setSize(fx(spentRedsLabeln.getWidth()/2), fy(spentRedsLabeln.getHeight()/2));
			
			spentLifesLabel = new Label("   Orbes de energia: ", skin);
			spentLifesLabel.setFontScale(x/2.25f, y/2.25f);
			spentLifesLabel.setSize(fx(spentLifesLabel.getWidth()/2), fy(spentLifesLabel.getHeight()/2));
			spentLifesLabeln = new Label(String.valueOf(spentLifes), skin);
			spentLifesLabeln.setFontScale(x/2, y/2);
			spentLifesLabeln.setSize(fx(spentLifesLabeln.getWidth()/2), fy(spentLifesLabeln.getHeight()/2));
			
			timePlayingLabel = new Label("Tiempo jugando:", skin);
			timePlayingLabel.setFontScale(x/2.25f, y/2.25f);
			timePlayingLabel.setSize(fx(timePlayingLabel.getWidth()/2), fy(timePlayingLabel.getHeight()/2));
			timePlayingLabeln = new Label(String.valueOf(timePlaying/3600) + ":" + String.valueOf((timePlaying % 3600)/60) + ":" + String.valueOf((timePlaying % 3600) % 60), skin);
			timePlayingLabeln.setFontScale(x/2, y/2);
			timePlayingLabeln.setSize(fx(timePlayingLabeln.getWidth()/2), fy(timePlayingLabeln.getHeight()/2));

			obj0Label = new Label("      Rojo-a-azul:", skin);
			obj0Label.setFontScale(x/2.25f, y/2.25f);
			obj0Label.setSize(fx(obj0Label.getWidth()/2), fy(obj0Label.getHeight()/2));
			obj0Labeln = new Label(String.valueOf(obj[0]), skin);
			obj0Labeln.setFontScale(x/2, y/2);
			obj0Labeln.setSize(fx(obj0Labeln.getWidth()/2), fy(obj0Labeln.getHeight()/2));
			
			obj1Label = new Label("      Azul-a-rojo:", skin);
			obj1Label.setFontScale(x/2.25f, y/2.25f);
			obj1Label.setSize(fx(obj1Label.getWidth()/2), fy(obj1Label.getHeight()/2));
			obj1Labeln = new Label(String.valueOf(obj[1]), skin);
			obj1Labeln.setFontScale(x/2, y/2);
			obj1Labeln.setSize(fx(obj1Labeln.getWidth()/2), fy(obj1Labeln.getHeight()/2));
			 
			obj2Label = new Label("      Orbes azules X2:", skin);
			obj2Label.setFontScale(x/2.25f, y/2.25f);
			obj2Label.setSize(fx(obj2Label.getWidth()/2), fy(obj2Label.getHeight()/2));
			obj2Labeln = new Label(String.valueOf(obj[2]), skin);
			obj2Labeln.setFontScale(x/2, y/2);
			obj2Labeln.setSize(fx(obj2Labeln.getWidth()/2), fy(obj2Labeln.getHeight()/2));
			
			obj3Label = new Label("      Orbes rojos X2:", skin);
			obj3Label.setFontScale(x/2.25f, y/2.25f);
			obj3Label.setSize(fx(obj3Label.getWidth()/2), fy(obj3Label.getHeight()/2));
			obj3Labeln = new Label(String.valueOf(obj[3]), skin);
			obj3Labeln.setFontScale(x/2, y/2);
			obj3Labeln.setSize(fx(obj3Labeln.getWidth()/2), fy(obj3Labeln.getHeight()/2));
			
			obj4Label = new Label("      Tiempo lento:", skin);
			obj4Label.setFontScale(x/2.25f, y/2.25f);
			obj4Label.setSize(fx(obj4Label.getWidth()/2), fy(obj4Label.getHeight()/2));
			obj4Labeln = new Label(String.valueOf(obj[4]), skin);
			obj4Labeln.setFontScale(x/2, y/2);
			obj4Labeln.setSize(fx(obj4Labeln.getWidth()/2), fy(obj4Labeln.getHeight()/2));
			
			obj5Label = new Label("      Escudo azul:", skin);
			obj5Label.setFontScale(x/2.25f, y/2.25f);
			obj5Label.setSize(fx(obj5Label.getWidth()/2), fy(obj5Label.getHeight()/2));
			obj5Labeln = new Label(String.valueOf(obj[5]), skin);
			obj5Labeln.setFontScale(x/2, y/2);
			obj5Labeln.setSize(fx(obj5Labeln.getWidth()/2), fy(obj5Labeln.getHeight()/2));
			
			obj6Label = new Label("      Escudo rojo:", skin);
			obj6Label.setFontScale(x/2.25f, y/2.25f);
			obj6Label.setSize(fx(obj6Label.getWidth()/2), fy(obj6Label.getHeight()/2));
			obj6Labeln = new Label(String.valueOf(obj[6]), skin);
			obj6Labeln.setFontScale(x/2, y/2);
			obj6Labeln.setSize(fx(obj6Labeln.getWidth()/2), fy(obj6Labeln.getHeight()/2));
			
			obj7Label = new Label("      Orbes azules X3:", skin);
			obj7Label.setFontScale(x/2.25f, y/2.25f);
			obj7Label.setSize(fx(obj7Label.getWidth()/2), fy(obj7Label.getHeight()/2));
			obj7Labeln = new Label(String.valueOf(obj[7]), skin);
			obj7Labeln.setFontScale(x/2, y/2);
			obj7Labeln.setSize(fx(obj7Labeln.getWidth()/2), fy(obj7Labeln.getHeight()/2));
			
			obj8Label = new Label("      Orbes rojos X3:", skin);
			obj8Label.setFontScale(x/2.25f, y/2.25f);
			obj8Label.setSize(fx(obj8Label.getWidth()/2), fy(obj8Label.getHeight()/2));
			obj8Labeln = new Label(String.valueOf(obj[8]), skin);
			obj8Labeln.setFontScale(x/2, y/2);
			obj8Labeln.setSize(fx(obj8Labeln.getWidth()/2), fy(obj8Labeln.getHeight()/2));

			obj9Label = new Label("      Escudo dorado:", skin);
			obj9Label.setFontScale(x/2.25f, y/2.25f);
			obj9Label.setSize(fx(obj9Label.getWidth()/2), fy(obj9Label.getHeight()/2));
			obj9Labeln = new Label(String.valueOf(obj[9]), skin);
			obj9Labeln.setFontScale(x/2, y/2);
			obj9Labeln.setSize(fx(obj9Labeln.getWidth()/2), fy(obj9Labeln.getHeight()/2));
		}
		
		imgsSts.add(gamesLabel).width(gamesLabel.getWidth()).height(gamesLabel.getHeight()).padTop(0).left().uniformX().expandX();
		imgsSts.add(gamesLabeln).width(gamesLabeln.getWidth()).height(gamesLabeln.getHeight()).padTop(0).right();
		imgsSts.row();
		imgsSts.add(timePlayingLabel).width(timePlayingLabel.getWidth()).height(timePlayingLabel.getHeight()).padTop(0).left().uniformX().expandX();
		imgsSts.add(timePlayingLabeln).width(timePlayingLabeln.getWidth()).height(timePlayingLabeln.getHeight()).padTop(0).right();
		imgsSts.row();
		imgsSts.add(totalPointLabel).width(totalPointLabel.getWidth()).height(totalPointLabel.getHeight()).padTop(0).left().uniformX().expandX();
		imgsSts.add(totalPointsLabeln).width(totalPointsLabeln.getWidth()).height(totalPointsLabeln.getHeight()).padTop(0).right();
		imgsSts.row();
		imgsSts.add(avgPointsLabel).width(avgPointsLabel.getWidth()).height(avgPointsLabel.getHeight()).padTop(0).left().uniformX().expandX();
		imgsSts.add(avgPointsLabeln).width(avgPointsLabeln.getWidth()).height(avgPointsLabeln.getHeight()).padTop(0).right();
		imgsSts.row();
		imgsSts.add(spentLabel).width(spentLabel.getWidth()).height(spentLabel.getHeight()).padTop(0).left().uniformX().expandX();
		imgsSts.add(spentLabeln).width(spentLabeln.getWidth()).height(spentLabeln.getHeight()).padTop(0).right();
		imgsSts.row();
		imgsSts.add(spentBluesLabel).width(spentBluesLabel.getWidth()).height(spentBluesLabel.getHeight()).padTop(0).left().uniformX().expandX();
		imgsSts.add(spentBluesLabeln).width(spentBluesLabeln.getWidth()).height(spentBluesLabeln.getHeight()).padTop(0).right();
		imgsSts.row();
		imgsSts.add(spentRedsLabel).width(spentRedsLabel.getWidth()).height(spentRedsLabel.getHeight()).padTop(0).left().uniformX().expandX();
		imgsSts.add(spentRedsLabeln).width(spentRedsLabeln.getWidth()).height(spentRedsLabeln.getHeight()).padTop(0).right();
		imgsSts.row();
		imgsSts.add(spentLifesLabel).width(spentLifesLabel.getWidth()).height(spentLifesLabel.getHeight()).padTop(0).left().uniformX().expandX();
		imgsSts.add(spentLifesLabeln).width(spentLifesLabeln.getWidth()).height(spentLifesLabeln.getHeight()).padTop(0).right();
		imgsSts.row();
		imgsSts.add(totalOrbsLabel).width(totalOrbsLabel.getWidth()).height(totalOrbsLabel.getHeight()).padTop(0).left().uniformX().expandX();
		imgsSts.add(totalOrbsLabeln).width(totalOrbsLabeln.getWidth()).height(totalOrbsLabeln.getHeight()).padTop(0).right();
		imgsSts.row();
		imgsSts.add(totalBluesLabel).width(totalBluesLabel.getWidth()).height(totalBluesLabel.getHeight()).padTop(0).left().uniformX().expandX();
		imgsSts.add(totalBluesLabeln).width(totalBluesLabeln.getWidth()).height(totalBluesLabeln.getHeight()).padTop(0).right();
		imgsSts.row();
		imgsSts.add(totalRedsLabel).width(totalRedsLabel.getWidth()).height(totalRedsLabel.getHeight()).padTop(0).left().uniformX().expandX();
		imgsSts.add(totalRedsLabeln).width(totalRedsLabeln.getWidth()).height(totalRedsLabeln.getHeight()).padTop(0).right();
		imgsSts.row();
		imgsSts.add(totalLifesLabel).width(totalLifesLabel.getWidth()).height(totalLifesLabel.getHeight()).padTop(0).left().uniformX().expandX();
		imgsSts.add(totalLifesLabeln).width(totalLifesLabeln.getWidth()).height(totalLifesLabeln.getHeight()).padTop(0).right();
		imgsSts.row();
		imgsSts.add(totalObjectsLabel).width(totalObjectsLabel.getWidth()).height(totalObjectsLabel.getHeight()).padTop(0).left().uniformX().expandX();
		imgsSts.add(totalObjectsLabeln).width(totalObjectsLabeln.getWidth()).height(totalObjectsLabeln.getHeight()).padTop(0).right();
		imgsSts.row();
		imgsSts.add(obj0Label).width(obj0Label.getWidth()).height(obj0Label.getHeight()).padTop(0).left().uniformX().expandX();
		imgsSts.add(obj0Labeln).width(obj0Labeln.getWidth()).height(obj0Labeln.getHeight()).padTop(0).right();
		imgsSts.row();
		imgsSts.add(obj1Label).width(obj1Label.getWidth()).height(obj1Label.getHeight()).padTop(0).left().uniformX().expandX();
		imgsSts.add(obj1Labeln).width(obj1Labeln.getWidth()).height(obj1Labeln.getHeight()).padTop(0).right();
		imgsSts.row();
		imgsSts.add(obj2Label).width(obj2Label.getWidth()).height(obj2Label.getHeight()).padTop(0).left().uniformX().expandX();
		imgsSts.add(obj2Labeln).width(obj2Labeln.getWidth()).height(obj2Labeln.getHeight()).padTop(0).right();
		imgsSts.row();
		imgsSts.add(obj3Label).width(obj3Label.getWidth()).height(obj3Label.getHeight()).padTop(0).left().uniformX().expandX();
		imgsSts.add(obj3Labeln).width(obj3Labeln.getWidth()).height(obj3Labeln.getHeight()).padTop(0).right();
		imgsSts.row();
		imgsSts.add(obj4Label).width(obj4Label.getWidth()).height(obj4Label.getHeight()).padTop(0).left().uniformX().expandX();
		imgsSts.add(obj4Labeln).width(obj4Labeln.getWidth()).height(obj4Labeln.getHeight()).padTop(0).right();
		imgsSts.row();
		imgsSts.add(obj5Label).width(obj5Label.getWidth()).height(obj5Label.getHeight()).padTop(0).left().uniformX().expandX();
		imgsSts.add(obj5Labeln).width(obj5Labeln.getWidth()).height(obj5Labeln.getHeight()).padTop(0).right();
		imgsSts.row();
		imgsSts.add(obj6Label).width(obj6Label.getWidth()).height(obj6Label.getHeight()).padTop(0).left().uniformX().expandX();
		imgsSts.add(obj6Labeln).width(obj6Labeln.getWidth()).height(obj6Labeln.getHeight()).padTop(0).right();
		imgsSts.row();
		imgsSts.add(obj7Label).width(obj7Label.getWidth()).height(obj7Label.getHeight()).padTop(0).left().uniformX().expandX();
		imgsSts.add(obj7Labeln).width(obj7Labeln.getWidth()).height(obj7Labeln.getHeight()).padTop(0).right();
		imgsSts.row();
		imgsSts.add(obj8Label).width(obj8Label.getWidth()).height(obj8Label.getHeight()).padTop(0).left().uniformX().expandX();
		imgsSts.add(obj8Labeln).width(obj8Labeln.getWidth()).height(obj8Labeln.getHeight()).padTop(0).right();
		imgsSts.row();
		imgsSts.add(obj9Label).width(obj9Label.getWidth()).height(obj9Label.getHeight()).padTop(0).left().uniformX().expandX();
		imgsSts.add(obj9Labeln).width(obj9Labeln.getWidth()).height(obj9Labeln.getHeight()).padTop(0).right();
		imgsSts.row();

		float statsPad = stsLabel.getHeight()/2.25f;
		
	   	Image bBackISts = new Image(bBack);
	   	if(language == OFF){
	   		containerSts.add(bBackISts).width(bBack.getWidth()).height(bBack.getHeight()).left();
			containerSts.add(stsLabel).width(stsLabel.getWidth()).height(stsLabel.getHeight()).left().expandX().padLeft(statsPad);
	   	}else{
	   		containerSts.add(bBackISts).width(bBack.getWidth()).height(bBack.getHeight()).left();
			containerSts.add(stsLabel).width(stsLabel.getWidth()).height(stsLabel.getHeight()).left().expandX().padLeft(statsPad);
	   	}
		containerSts.row();
		containerSts.add(scrollSts).width(3*Gdx.graphics.getWidth()/4).height(containerShips.getHeight() - bBack.getHeight() - 2*statsPad).padTop(statsPad).colspan(2);
	
		bBackISts.addListener(new ClickListener(){
			public void clicked(InputEvent event, float x, float y){
				state = STANDBY;
				if(sound == ON)
					buttonSmall.play();
				stageStats.addAction(moveTo(0, stageStats.getHeight(), 0.5f));
				effectsMenuOff();
			}
		});	

//		containerSts.debug();
//		imgsSts.debug();
	}
	
	public void setUpSettingsMenu(){
		stageSettings = new Stage();
		containerSttngs = new Table();
		stageSettings.addActor(containerSttngs);
		containerSttngs.setBounds(Gdx.graphics.getWidth()/8 , counterBaseY + 1.5f*blueCounter.getHeight(), 
				3*Gdx.graphics.getWidth()/4, Gdx.graphics.getHeight() - 0.225f*Gdx.graphics.getHeight() - adHeight);	
		imgsSttngs = new Table();
		scrollSttngs = new ScrollPane(imgsSttngs);
		scrollSttngs.setScrollingDisabled(true, false);
		
		if(language == ON)
			sttngsLabel = new Label("Settings", skin);
		else
			sttngsLabel = new Label("Opciones", skin);
		sttngsLabel.setFontScale(x, y);
		sttngsLabel.setSize(fx(sttngsLabel.getWidth()), fy(sttngsLabel.getHeight()));

		if(language == ON)
			soundLabel = new Label("Sound:", skin);
		else
			soundLabel = new Label("Sonido:", skin);
		soundLabel.setFontScale(3*x/4, 3*y/4);
		soundLabel.setSize(fx(3*soundLabel.getWidth()/4), fy(3*soundLabel.getHeight()/4));
		
		if(language == ON)
			vibrationLabel = new Label("Vibration:", skin);
		else
			vibrationLabel = new Label("Vibracion:", skin);
		vibrationLabel.setFontScale(3*x/4, 3*y/4);
		vibrationLabel.setSize(fx(3*vibrationLabel.getWidth()/4), fy(3*vibrationLabel.getHeight()/4));

		if(language == ON)
			controlsLabel = new Label("Controls:", skin);
		else
			controlsLabel = new Label("Controles:", skin);
		controlsLabel.setFontScale(3*x/4, 3*y/4);
		controlsLabel.setSize(fx(3*controlsLabel.getWidth()/4), fy(3*controlsLabel.getHeight()/4));

		if(language == ON)
			languageLabel = new Label("Language:", skin);
		else
			languageLabel = new Label("Idioma:", skin);
		languageLabel.setFontScale(3*x/4, 3*y/4);
		languageLabel.setSize(fx(3*languageLabel.getWidth()/4), fy(3*languageLabel.getHeight()/4));
		
		imgsSttngs.add(soundLabel).width(soundLabel.getWidth()).height(soundLabel.getHeight()).padBottom(iSoundOn.getHeight()/4).left().uniformX().expandX();
		if(sound == ON){
			imgsSttngs.add(iSoundOn).width(iSoundOn.getWidth()).height(iSoundOn.getHeight()).padBottom(iSoundOn.getHeight()/4).right();
			soundInMenu = ON;
		}else{
			imgsSttngs.add(iSoundOff).width(iSoundOff.getWidth()).height(iSoundOff.getHeight()).padBottom(iSoundOn.getHeight()/4).right();	
			soundInMenu = OFF;
		}
		imgsSttngs.row();
		imgsSttngs.add(vibrationLabel).width(vibrationLabel.getWidth()).height(vibrationLabel.getHeight()).padBottom(iSoundOn.getHeight()/4).left().uniformX().expandX();
		if(vibration == ON){
			imgsSttngs.add(iVibrationOn).width(iVibrationOn.getWidth()).height(iVibrationOn.getHeight()).padBottom(iSoundOn.getHeight()/4).right();
			vibrationInMenu = ON;
		}else{
			imgsSttngs.add(iVibrationOff).width(iVibrationOff.getWidth()).height(iVibrationOff.getHeight()).padBottom(iSoundOn.getHeight()/4).right();	
			vibrationInMenu = OFF;
		}
		imgsSttngs.row();
		imgsSttngs.add(controlsLabel).width(controlsLabel.getWidth()).height(controlsLabel.getHeight()).left().uniformX().expandX();
		if(controls == ON){
			imgsSttngs.add(iControlsOn).width(iControlsOn.getWidth()).height(iControlsOn.getHeight()).padBottom(iSoundOn.getHeight()/4).right();
			controlsInMenu = ON;
		}else{
			imgsSttngs.add(iControlsOff).width(iControlsOff.getWidth()).height(iControlsOff.getHeight()).padBottom(iSoundOn.getHeight()/4).right();	
			controlsInMenu = OFF;
		}
		imgsSttngs.row();
		imgsSttngs.add(languageLabel).width(languageLabel.getWidth()).height(languageLabel.getHeight()).padBottom(iSoundOn.getHeight()/4).left().uniformX().expandX();
		if(language == ON){
			imgsSttngs.add(iEnglish).width(iEnglish.getWidth()).height(iEnglish.getHeight()).padBottom(iSoundOn.getHeight()/4).right();
		}else{
			imgsSttngs.add(iEspanol).width(iEspanol.getWidth()).height(iEspanol.getHeight()).padBottom(iSoundOn.getHeight()/4).right();
		}
		if(game.getActionResolver() != null){
			// one-time purchase that removes the ads (Android only: the resolver is null on the desktop)
			imgsSttngs.row();
			final Label removeAdsLabel = new Label(language == ON ? "Remove ads" : "Quitar anuncios", skin){
				@Override
				public void act(float delta){
					super.act(delta);
					if(game.getActionResolver().isAdsRemoved() && !getText().toString().startsWith("Ads removed") && !getText().toString().startsWith("Sin anuncios"))
						setText(language == ON ? "Ads removed" : "Sin anuncios");
				}
			};
			removeAdsLabel.setFontScale(3*x/4, 3*y/4);
			removeAdsLabel.setAlignment(Align.left);
			removeAdsLabel.addListener(new ClickListener(){
				public void clicked(InputEvent event, float x, float y){
					if(!game.getActionResolver().isAdsRemoved()){
						buttonSmall.play();
						game.getActionResolver().buyRemoveAds();
					}
				}
			});
			final Label restoreLabel = new Label(language == ON ? "Restore purchase" : "Restaurar compra", skin);
			restoreLabel.setFontScale(x/2.2f, y/2.2f);
			restoreLabel.setAlignment(Align.left);
			restoreLabel.addListener(new ClickListener(){
				public void clicked(InputEvent event, float x, float y){
					game.getActionResolver().restorePurchases();
				}
			});
			imgsSttngs.add(removeAdsLabel).left().padTop(iSoundOn.getHeight()/4).padBottom(iSoundOn.getHeight()/8);
			imgsSttngs.row();
			imgsSttngs.add(restoreLabel).left();
		}
	
		iSoundOn.addListener(new ClickListener(){
			public void clicked(InputEvent event, float x, float y){
				sound = OFF;
				updateMusicVolume();
				soundInMenu = OFF;
				imgsSttngs.getCell(iSoundOn).setActor(iSoundOff);
			}
		});	
		iSoundOff.addListener(new ClickListener(){
			public void clicked(InputEvent event, float x, float y){
				buttonSmall.play();
				sound = ON;
				updateMusicVolume();
				soundInMenu = ON;
				imgsSttngs.getCell(iSoundOff).setActor(iSoundOn);
			}
		});	
		iVibrationOn.addListener(new ClickListener(){
			public void clicked(InputEvent event, float x, float y){
				if(sound == ON)
					buttonSmall.play();
				vibration = OFF;
				vibrationInMenu = OFF;
				imgsSttngs.getCell(iVibrationOn).setActor(iVibrationOff);
			}
		});	
		iVibrationOff.addListener(new ClickListener(){
			public void clicked(InputEvent event, float x, float y){
				if(sound == ON)
					buttonSmall.play();
				Gdx.input.vibrate(75);
				vibration = ON;
				vibrationInMenu = ON;
				imgsSttngs.getCell(iVibrationOff).setActor(iVibrationOn);
			}
		});	
		iControlsOn.addListener(new ClickListener(){
			public void clicked(InputEvent event, float x, float y){
				if(sound == ON)
					buttonSmall.play();
				controls = OFF;
				controlsInMenu = OFF;
				imgsSttngs.getCell(iControlsOn).setActor(iControlsOff);
			}
		});	
		iControlsOff.addListener(new ClickListener(){
			public void clicked(InputEvent event, float x, float y){
				if(sound == ON)
					buttonSmall.play();
				controls = ON;
				controlsInMenu = ON;
				imgsSttngs.getCell(iControlsOff).setActor(iControlsOn);
			}
		});	
		iEnglish.addListener(new ClickListener(){
			public void clicked(InputEvent event, float x, float y){
				if(sound == ON)
					buttonSmall.play();
				language = OFF;
				updateLanguage();
				imgsSttngs.getCell(iEnglish).setActor(iEspanol);
			}
		});	
		iEspanol.addListener(new ClickListener(){
			public void clicked(InputEvent event, float x, float y){
				if(sound == ON)
					buttonSmall.play();
				language = ON;
				updateLanguage();
				imgsSttngs.getCell(iEspanol).setActor(iEnglish);
			}
		});	
		
		float sttngsPad = sttngsLabel.getHeight()/2.25f;
		
		 Image bBackIUpg = new Image(bBack);
		 containerSttngs.add(bBackIUpg).width(bBack.getWidth()).height(bBack.getHeight()).left();
		 containerSttngs.add(sttngsLabel).width(sttngsLabel.getWidth()).height(sttngsLabel.getHeight()).left().expandX().padLeft(sttngsPad);
		 containerSttngs.row();
		 containerSttngs.add(scrollSttngs).width(3*Gdx.graphics.getWidth()/4).height(containerShips.getHeight() - bBack.getHeight() - 2*sttngsPad).padTop(sttngsPad).colspan(2);
		
		bBackIUpg.addListener(new ClickListener(){
			public void clicked(InputEvent event, float x, float y){
				state = STANDBY;
				if(sound == ON)
					buttonSmall.play();
				stageSettings.addAction(moveTo(0, stageSettings.getHeight(), 0.5f));
				effectsMenuOff();
			}
		});	
		
	}
	
	public void updateLanguage(){
		if(language == OFF){
			greysFont.getData().setScale(x/1.115f, y/1.115f);
			game.getPS().greysFont.getData().setScale(x/1.115f, y/1.115f);
			
			sttngsLabel.setText("Opciones");
			sttngsLabel.setAlignment(Align.left);
			soundLabel.setText("Sonido:");
			soundLabel.setAlignment(Align.left);
			vibrationLabel.setText("Vibracion:");
			vibrationLabel.setAlignment(Align.left);
			controlsLabel.setText("Controles:");
			controlsLabel.setAlignment(Align.left);
			languageLabel.setText("Idioma:");
			languageLabel.setAlignment(Align.left);
			
			itemsLabel.setText("Objetos");
			itemsLabel.setAlignment(Align.left);
			padsTitleLabel.setText("Controles");
			padsTitleLabel.setAlignment(Align.left);
			tutoLabel.setText("Ayuda");
			tutoLabel.setAlignment(Align.left);
			orbsLabel.setText("Orbes");
			orbsLabel.setAlignment(Align.left);
			imgsHelp.getCell(padsControlsEN).setActor(padsControlsES);
			imgsHelp.getCell(orbsEN).setActor(orbsES).height(((containerHelp.getWidth() - padsTitleLabel.getHeight()/2.25f)*orbsES.getHeight())/orbsES.getWidth());;
			imgsHelp.getCell(itemsEN).setActor(itemsES);
			imgsHelp.getCell(instEN).setActor(instES);
			
			shipsLabel.setText("Naves");
			shipsLabel.setAlignment(Align.left);
			
			bgsLabel.setText("Galaxias");
			bgsLabel.setAlignment(Align.left);
			
			upgLabel.setText("Mejoras");
			upgLabel.setAlignment(Align.left);
			speedLabel.setText("Velocidad de nave");
			speedLabel.setAlignment(Align.left);
			speedLabel.setFontScale(x/2.25f, y/2.25f);
			energyLabel.setText("Barra de energia");
			energyLabel.setAlignment(Align.left);
			energyLabel.setFontScale(x/2.25f, y/2.25f);
			energyStartLabel.setText("Energia inicial");
			energyStartLabel.setAlignment(Align.left);
			energyStartLabel.setFontScale(x/2.25f, y/2.25f);
			objectsLabel.setText("Lugar para objetos");
			objectsLabel.setAlignment(Align.left);
			objectsLabel.setFontScale(x/2.25f, y/2.25f);
			objectsStartLabel.setText("Objetos iniciales");
			objectsStartLabel.setAlignment(Align.left);
			objectsStartLabel.setFontScale(x/2.25f, y/2.25f);
			objectsDurationLabel.setText("Duracion de objetos");
			objectsDurationLabel.setAlignment(Align.left);
			objectsDurationLabel.setFontScale(x/2.25f, y/2.25f);

			stsLabel.setText("Estadisticas");
			stsLabel.setFontScale(x/1.25f, y/1.25f);
			stsLabel.setAlignment(Align.left);
			gamesLabel.setText("Partidas jugadas:");
			gamesLabel.setAlignment(Align.left);
			gamesLabel.setFontScale(x/2.25f, y/2.25f);
			timePlayingLabel.setText("Tiempo jugando:");
			timePlayingLabel.setAlignment(Align.left);
			timePlayingLabel.setFontScale(x/2.25f, y/2.25f);
			totalPointLabel.setText("Total de puntos:");
			totalPointLabel.setAlignment(Align.left);
			totalPointLabel.setFontScale(x/2.25f, y/2.25f);
			avgPointsLabel.setText("Prom. pts/partidas:");
			avgPointsLabel.setAlignment(Align.left);
			avgPointsLabel.setFontScale(x/2.25f, y/2.25f);
			spentLabel.setText("Orbes gastados:");
			spentLabel.setAlignment(Align.left);
			spentLabel.setFontScale(x/2.25f, y/2.25f);
			spentBluesLabel.setText("   Orbes azules:");
			spentBluesLabel.setAlignment(Align.left);
			spentBluesLabel.setFontScale(x/2.25f, y/2.25f);
			spentRedsLabel.setText("   Orbes rojos:");
			spentRedsLabel.setAlignment(Align.left);
			spentRedsLabel.setFontScale(x/2.25f, y/2.25f);
			spentLifesLabel.setText("   Orbes de energia:");
			spentLifesLabel.setAlignment(Align.left);
			spentLifesLabel.setFontScale(x/2.25f, y/2.25f);
			totalOrbsLabel.setText("Orbes conseguidas:");
			totalOrbsLabel.setAlignment(Align.left);
			totalOrbsLabel.setFontScale(x/2.25f, y/2.25f);
			totalBluesLabel.setText("   Orbes azules:");
			totalBluesLabel.setAlignment(Align.left);
			totalBluesLabel.setFontScale(x/2.25f, y/2.25f);
			totalRedsLabel.setText("   Orbes rojos:");
			totalRedsLabel.setAlignment(Align.left);
			totalRedsLabel.setFontScale(x/2.25f, y/2.25f);
			totalLifesLabel.setText("   Orbes de energia:");
			totalLifesLabel.setAlignment(Align.left);
			totalLifesLabel.setFontScale(x/2.25f, y/2.25f);
			totalObjectsLabel.setText("   Orbes de objetos:");
			totalObjectsLabel.setAlignment(Align.left);
			totalObjectsLabel.setFontScale(x/2.25f, y/2.25f);
			obj0Label.setText("      Rojo-a-azul:");
			obj0Label.setAlignment(Align.left);
			obj0Label.setFontScale(x/2.25f, y/2.25f);
			obj1Label.setText("      Azul-a-rojo:");
			obj1Label.setAlignment(Align.left);
			obj1Label.setFontScale(x/2.25f, y/2.25f);
			obj2Label.setText("      Orbes Azules X2:");
			obj2Label.setAlignment(Align.left);
			obj2Label.setFontScale(x/2.25f, y/2.25f);
			obj3Label.setText("      Orbes Rojos X2:");
			obj3Label.setAlignment(Align.left);
			obj3Label.setFontScale(x/2.25f, y/2.25f);
			obj4Label.setText("      Tiempo lento:");
			obj4Label.setAlignment(Align.left);
			obj4Label.setFontScale(x/2.25f, y/2.25f);
			obj5Label.setText("      Escudo Azul:");
			obj5Label.setAlignment(Align.left);
			obj5Label.setFontScale(x/2.25f, y/2.25f);
			obj6Label.setText("      Escudo Rojo:");
			obj6Label.setAlignment(Align.left);
			obj6Label.setFontScale(x/2.25f, y/2.25f);
			obj7Label.setText("      Orbes Azules X3:");
			obj7Label.setAlignment(Align.left);
			obj7Label.setFontScale(x/2.25f, y/2.25f);
			obj8Label.setText("      Orbes Rojos X3:");
			obj8Label.setAlignment(Align.left);
			obj8Label.setFontScale(x/2.25f, y/2.25f);
			obj9Label.setText("      Escudo dorado:");
			obj9Label.setAlignment(Align.left);
			obj9Label.setFontScale(x/2.25f, y/2.25f);
		}else{
			greysFont.getData().setScale(x, y);
			game.getPS().greysFont.getData().setScale(x, y);
		
			sttngsLabel.setText("Settings");
			sttngsLabel.setAlignment(Align.left);
			soundLabel.setText("Sound:");
			soundLabel.setAlignment(Align.left);
			vibrationLabel.setText("Vibration:");
			vibrationLabel.setAlignment(Align.left);
			controlsLabel.setText("Controls:");
			controlsLabel.setAlignment(Align.left);
			languageLabel.setText("Language:");
			languageLabel.setAlignment(Align.left);
			
			itemsLabel.setText("Items");
			itemsLabel.setAlignment(Align.left);
			padsTitleLabel.setText("Controls");
			padsTitleLabel.setAlignment(Align.left);
			tutoLabel.setText("Help");
			tutoLabel.setAlignment(Align.left);
			orbsLabel.setText("Orbs");
			orbsLabel.setAlignment(Align.left);
			imgsHelp.getCell(padsControlsES).setActor(padsControlsEN);
			imgsHelp.getCell(orbsES).setActor(orbsEN).height(((containerHelp.getWidth() - padsTitleLabel.getHeight()/2.25f)*orbsEN.getHeight())/orbsEN.getWidth());
			imgsHelp.getCell(itemsES).setActor(itemsEN);
			imgsHelp.getCell(instES).setActor(instEN);
			
			shipsLabel.setText("Ships");
			shipsLabel.setAlignment(Align.left);
			
			bgsLabel.setText("Galaxies");
			bgsLabel.setAlignment(Align.left);
			
			upgLabel.setText("Upgrades");
			upgLabel.setAlignment(Align.left);
			speedLabel.setText("Ship's speed");
			speedLabel.setAlignment(Align.left);
			speedLabel.setFontScale(x/2, y/2);
			energyLabel.setText("Energy bar");
			energyLabel.setAlignment(Align.left);
			energyLabel.setFontScale(x/2, y/2);
			energyStartLabel.setText("Starting energy");
			energyStartLabel.setAlignment(Align.left);
			energyStartLabel.setFontScale(x/2, y/2);
			objectsLabel.setText("Object's slots");
			objectsLabel.setAlignment(Align.left);
			objectsLabel.setFontScale(x/2, y/2);
			objectsStartLabel.setText("Starting objects");
			objectsStartLabel.setAlignment(Align.left);
			objectsStartLabel.setFontScale(x/2, y/2);
			objectsDurationLabel.setText("Object's duration");
			objectsDurationLabel.setAlignment(Align.left);
			objectsDurationLabel.setFontScale(x/2, y/2);

			stsLabel.setText("Stats");
			stsLabel.setAlignment(Align.left);
			stsLabel.setFontScale(x, y);
			gamesLabel.setText("Games played:");
			gamesLabel.setAlignment(Align.left);
			gamesLabel.setFontScale(x/2, y/2);
			timePlayingLabel.setText("Time playing:");
			timePlayingLabel.setAlignment(Align.left);
			timePlayingLabel.setFontScale(x/2, y/2);
			totalPointLabel.setText("Points scored:");
			totalPointLabel.setAlignment(Align.left);
			totalPointLabel.setFontScale(x/2, y/2);
			avgPointsLabel.setText("Points/game avg:");
			avgPointsLabel.setAlignment(Align.left);
			avgPointsLabel.setFontScale(x/2, y/2);
			spentLabel.setText("Orbs spent:");
			spentLabel.setAlignment(Align.left);
			spentLabel.setFontScale(x/2, y/2);
			spentBluesLabel.setText("   Blue orbs:");
			spentBluesLabel.setAlignment(Align.left);
			spentBluesLabel.setFontScale(x/2, y/2);
			spentRedsLabel.setText("   Red orbs:");
			spentRedsLabel.setAlignment(Align.left);
			spentRedsLabel.setFontScale(x/2, y/2);
			spentLifesLabel.setText("   Energy orbs:");
			spentLifesLabel.setAlignment(Align.left);
			spentLifesLabel.setFontScale(x/2, y/2);
			totalOrbsLabel.setText("Orbs collected:");
			totalOrbsLabel.setAlignment(Align.left);
			totalOrbsLabel.setFontScale(x/2, y/2);
			totalBluesLabel.setText("   Blue orbs:");
			totalBluesLabel.setAlignment(Align.left);
			totalBluesLabel.setFontScale(x/2, y/2);
			totalRedsLabel.setText("   Red orbs:");
			totalRedsLabel.setAlignment(Align.left);
			totalRedsLabel.setFontScale(x/2, y/2);
			totalLifesLabel.setText("   Energy orbs:");
			totalLifesLabel.setAlignment(Align.left);
			totalLifesLabel.setFontScale(x/2, y/2);
			totalObjectsLabel.setText("   Object orbs:");
			totalObjectsLabel.setAlignment(Align.left);
			totalObjectsLabel.setFontScale(x/2, y/2);
			obj0Label.setText("      Red-to-blue:");
			obj0Label.setAlignment(Align.left);
			obj0Label.setFontScale(x/2, y/2);
			obj1Label.setText("      Blue-to-red:");
			obj1Label.setAlignment(Align.left);
			obj1Label.setFontScale(x/2, y/2);
			obj2Label.setText("      Blue orbs X2:");
			obj2Label.setAlignment(Align.left);
			obj2Label.setFontScale(x/2, y/2);
			obj3Label.setText("      Red orbs X2:");
			obj3Label.setAlignment(Align.left);
			obj3Label.setFontScale(x/2, y/2);
			obj4Label.setText("      Slow time:");
			obj4Label.setAlignment(Align.left);
			obj4Label.setFontScale(x/2, y/2);
			obj5Label.setText("      Blue shield:");
			obj5Label.setAlignment(Align.left);
			obj5Label.setFontScale(x/2, y/2);
			obj6Label.setText("      Red shield:");
			obj6Label.setAlignment(Align.left);
			obj6Label.setFontScale(x/2, y/2);
			obj7Label.setText("      Blue orbs X3:");
			obj7Label.setAlignment(Align.left);
			obj7Label.setFontScale(x/2, y/2);
			obj8Label.setText("      Red orbs X2:");
			obj8Label.setAlignment(Align.left);
			obj8Label.setFontScale(x/2, y/2);
			obj9Label.setText("      Gold shield:");
			obj9Label.setAlignment(Align.left);
			obj9Label.setFontScale(x/2, y/2);
		}
	}
	
	public void updateSettingsMenuSpent(){
		if(sound == ON && soundInMenu == OFF){
			imgsSttngs.getCell(iSoundOff).setActor(iSoundOn);
			soundInMenu = ON;
		}else if(sound == OFF && soundInMenu == ON){
			imgsSttngs.getCell(iSoundOn).setActor(iSoundOff);
			soundInMenu = OFF;
		}	
		if(vibration == ON && vibrationInMenu == OFF){
			imgsSttngs.getCell(iVibrationOff).setActor(iVibrationOn);
			vibrationInMenu = ON;
		}else if(vibration == OFF && vibrationInMenu == ON){
			imgsSttngs.getCell(iVibrationOn).setActor(iVibrationOff);
			vibrationInMenu = OFF;
		}	
		if(controls == ON && controlsInMenu == OFF){
			imgsSttngs.getCell(iControlsOff).setActor(iControlsOn);
			controlsInMenu = ON;
		}else if(controls == OFF && controlsInMenu == ON){
			imgsSttngs.getCell(iControlsOn).setActor(iControlsOff);
			controlsInMenu = OFF;
		}	
	}
	
	public void effectsMenuOff(){
		Tween.to(giftizLogo, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f).start(tweenManager);
		Tween.to(giftizLogoWarning, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f).start(tweenManager);
		Tween.to(giftizLogoBadge, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f).start(tweenManager);
		Tween.to(bShips, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f).start(tweenManager);
		Tween.to(bTrophy, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f).start(tweenManager);
		Tween.to(bLeaderboard, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f).start(tweenManager);
		Tween.to(bUpgrades, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f).start(tweenManager);
		Tween.to(bGalaxies, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f).start(tweenManager);
		Tween.to(bSettings, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f).start(tweenManager);
		Tween.to(bExit, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f).start(tweenManager);
		Tween.to(bStats, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f).start(tweenManager);
		Tween.to(bHelp, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f).start(tweenManager);
		Tween.to(bGo, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f).delay(0.15f).start(tweenManager);
		Tween.to(shipsSprites[shipSelected], SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f).start(tweenManager);
		Tween.to(ffSprites[forceFieldSelected], SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f).start(tweenManager);
		Tween.to(ffgSprites[forceFieldSelected], SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f).start(tweenManager);
		Tween.to(greysFontTitle, BitmapFontAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f).delay(0.25f).start(tweenManager);
		Timer.schedule(new Task(){
			@Override
			public void run() {
				drawTrace = true;
			}
		}, 0.15f);
	}

	public void effectsMenuOn(){
		Tween.to(bUpgrades, SpriteAccessor.COLOR, 0.15f).target(1f, 1f, 1f, 0f).delay(0.25f).start(tweenManager);
		Tween.to(bLeaderboard, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f).delay(0.25f).start(tweenManager);
		Tween.to(bTrophy, SpriteAccessor.COLOR, 0.20f).target(1f, 1f, 1f, 0f).delay(0.25f).start(tweenManager);
		Tween.to(bShips, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f).delay(0.25f).start(tweenManager);
		Tween.to(bGalaxies, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f).delay(0.25f).start(tweenManager);
		Tween.to(bSettings, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f).delay(0.25f).start(tweenManager);
		Tween.to(bGo, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f).delay(0.15f).start(tweenManager);
		Tween.to(giftizLogo, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f).delay(0.15f).start(tweenManager);
		Tween.to(giftizLogoWarning, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f).delay(0.15f).start(tweenManager);
		Tween.to(giftizLogoBadge, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f).delay(0.15f).start(tweenManager);
		Tween.to(bExit, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f).delay(0.25f).start(tweenManager);
		Tween.to(bStats, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f).delay(0.25f).start(tweenManager);
		Tween.to(bHelp, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f).delay(0.25f).start(tweenManager);
		Tween.to(shipsSprites[shipSelected], SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f).delay(0.20f).start(tweenManager);
		Tween.to(ffSprites[forceFieldSelected], SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f).delay(0.20f).start(tweenManager);
		Tween.to(ffgSprites[forceFieldSelected], SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f).delay(0.20f).start(tweenManager);
		Tween.to(greysFontTitle, BitmapFontAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f).start(tweenManager);
		Timer.schedule(new Task(){
			@Override
			public void run() {
				drawTrace = false;
			}
		}, 0.25f);
	}

	public void updateDataBases(){
		try {
			game.getDataBase().execSQL("UPDATE seleccionsAndNumbers SET ffSelected=" + forceFieldSelected + ";");
			game.getDataBase().execSQL("UPDATE seleccionsAndNumbers SET shipSelected=" + shipSelected + ";");
			game.getDataBase().execSQL("UPDATE seleccionsAndNumbers SET galaxySelected=" + galaxySelected + ";");
			game.getDataBase().execSQL("UPDATE seleccionsAndNumbers SET sound=" + sound + ";");
			game.getDataBase().execSQL("UPDATE seleccionsAndNumbers SET vibration=" + vibration + ";");
			game.getDataBase().execSQL("UPDATE seleccionsAndNumbers SET controls=" + controls + ";");
			game.getDataBase().execSQL("UPDATE seleccionsAndNumbers SET language=" + language + ";");

			game.getDataBase().execSQL("UPDATE seleccionsAndNumbers SET redBalls=" + redBalls + ";");
			game.getDataBase().execSQL("UPDATE seleccionsAndNumbers SET blueBalls=" + blueBalls + ";");
			game.getDataBase().execSQL("UPDATE seleccionsAndNumbers SET lifeBalls=" + lifeBalls + ";");

			game.getDataBase().execSQL("UPDATE seleccionsAndNumbers SET ships=" + ships + ";");
			game.getDataBase().execSQL("UPDATE seleccionsAndNumbers SET galaxies=" + galaxies + ";");
			game.getDataBase().execSQL("UPDATE seleccionsAndNumbers SET upgrades=" + upgrades + ";");

			game.getStatsDataBase().execSQL("UPDATE stats SET spentBlues=" + spentBlues + ";");
			game.getStatsDataBase().execSQL("UPDATE stats SET spentReds=" + spentReds + ";");
			game.getStatsDataBase().execSQL("UPDATE stats SET spentLifes=" + spentLifes + ";");

			for(int i = 0; i < 75; i++){
				if(shipsNew[i] == true)
					game.getShipsDataBase().execSQL("UPDATE ships SET bought=" + 1 + " WHERE id =" + i + ";");
			}
			for(int i = 0; i < 11; i++){
				if(galaxiesNew[i] == true)
					game.getGalaxiesDataBase().execSQL("UPDATE galaxies SET bought=" + 1 + " WHERE id =" + i + ";");
			}
			if(up1){
				game.getUpgradesDataBase().execSQL("UPDATE upgrades SET shipSpeed=" + shipSpeed + ";");
				up1 = false;
			}
			if(up2){
				game.getUpgradesDataBase().execSQL("UPDATE upgrades SET lifesTotal=" + energyMeter + ";");
				up2 = false;
			}
			if(up3){
				game.getUpgradesDataBase().execSQL("UPDATE upgrades SET lifesStarts=" + energyStart + ";");
				up3 = false;
			}
			if(up4){
				game.getUpgradesDataBase().execSQL("UPDATE upgrades SET objectsRoom=" + objectsSlots + ";");
				up4 = false;
			}
			if(up5){
				game.getUpgradesDataBase().execSQL("UPDATE upgrades SET objectsStarts=" + objectStart + ";");
				up5 = false;
			}
			if(up6){
				game.getUpgradesDataBase().execSQL("UPDATE upgrades SET objectsDuration=" + objectsDuration + ";");
				up6 = false;
			}	
		} catch (SQLiteGdxException e) {
			e.printStackTrace();
		}
	}
	
	public void loadDataBases(){
		try {
			cursor = game.getDataBase().rawQuery("SELECT highScore FROM seleccionsAndNumbers");
			if(cursor != null && cursor.next())
				bestScore = cursor.getInt(0);
			cursor = game.getDataBase().rawQuery("SELECT lifeBalls FROM seleccionsAndNumbers");
			if(cursor != null && cursor.next())
				lifeBalls = cursor.getInt(0);
			cursor = game.getDataBase().rawQuery("SELECT blueBalls FROM seleccionsAndNumbers");
			if(cursor != null && cursor.next())
				blueBalls = cursor.getInt(0);
			cursor = game.getDataBase().rawQuery("SELECT redBalls FROM seleccionsAndNumbers");
			if(cursor != null && cursor.next())
				redBalls = cursor.getInt(0);
			cursor = game.getDataBase().rawQuery("SELECT ffSelected FROM seleccionsAndNumbers;");
			if(cursor != null && cursor.next())
				forceFieldSelected = Integer.valueOf(cursor.getInt(0));
			cursor.close();
			
			cursor = game.getStatsDataBase().rawQuery("SELECT spentBlues FROM stats");
			if(cursor != null && cursor.next()) 
				spentBlues = Integer.valueOf(cursor.getInt(0));
			cursor = game.getStatsDataBase().rawQuery("SELECT spentReds FROM stats");
			if(cursor != null && cursor.next()) 
				spentReds = Integer.valueOf(cursor.getInt(0));
			cursor = game.getStatsDataBase().rawQuery("SELECT spentLifes FROM stats");
			if(cursor != null && cursor.next()) 
				spentLifes = Integer.valueOf(cursor.getInt(0));
			cursor = game.getStatsDataBase().rawQuery("SELECT games FROM stats");
			if(cursor != null && cursor.next()) 
				games = Integer.valueOf(cursor.getInt(0));
			cursor = game.getStatsDataBase().rawQuery("SELECT totalPoints FROM stats");
			if(cursor != null && cursor.next()) 
				totalPoints = Integer.valueOf(cursor.getInt(0));
			cursor = game.getStatsDataBase().rawQuery("SELECT totalOrbs FROM stats");
			if(cursor != null && cursor.next()) 
				totalOrbs = Integer.valueOf(cursor.getInt(0));
			cursor = game.getStatsDataBase().rawQuery("SELECT totalBlues FROM stats");
			if(cursor != null && cursor.next()) 
				totalBlues = Integer.valueOf(cursor.getInt(0));
			cursor = game.getStatsDataBase().rawQuery("SELECT totalReds FROM stats");
			if(cursor != null && cursor.next()) 
				totalReds = Integer.valueOf(cursor.getInt(0));
			cursor = game.getStatsDataBase().rawQuery("SELECT totalLifes FROM stats");
			if(cursor != null && cursor.next()) 
				totalLifes = Integer.valueOf(cursor.getInt(0));
			cursor = game.getStatsDataBase().rawQuery("SELECT totalObjects FROM stats");
			if(cursor != null && cursor.next()) 
				totalObjects = Integer.valueOf(cursor.getInt(0));
			for(int i = 0; i < 10; i++){
				cursor = game.getStatsDataBase().rawQuery("SELECT ob" + String.valueOf(i) + " FROM stats");
				if(cursor != null && cursor.next()) 
					obj[i] = Integer.valueOf(cursor.getInt(0));				
			}
			cursor = game.getStatsDataBase().rawQuery("SELECT timePlaying FROM stats");
			if(cursor != null && cursor.next()) 
				timePlaying = Integer.valueOf(cursor.getInt(0));
			cursor.close();
		} catch (SQLiteGdxException e) {
			e.printStackTrace();
		}
	}
	
	private void updateTime(){
		try {
			game.getStatsDataBase().execSQL("UPDATE stats SET timePlaying=" + timePlaying + ";");
		} catch (SQLiteGdxException e) {
			e.printStackTrace();
		}
	}

	public void loadBgs(int t){
		switch(t){
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
		load = true;
	}
	
	private void loadFirstBg(){
		switch(galaxySelected){
		case 0:
			bgs[0] = new Sprite(game.assetManager.get("res/bgs/bgBlack1.png", Texture.class));
			bgs[1] = new Sprite(game.assetManager.get("res/bgs/bgBlack2.png", Texture.class));
			break;
		case 1:
			bgs[2] = new Sprite(game.assetManager.get("res/bgs/bgBrownDark1.png", Texture.class));
			bgs[3] = new Sprite(game.assetManager.get("res/bgs/bgBrownDark2.png", Texture.class));
			break;
		case 2:
			bgs[4] = new Sprite(game.assetManager.get("res/bgs/bgPurple1.png", Texture.class));
			bgs[5] = new Sprite(game.assetManager.get("res/bgs/bgPurple2.png", Texture.class));
			break;
		case 3:
			bgs[6] = new Sprite(game.assetManager.get("res/bgs/bgBlackRedDark1.png", Texture.class));
			bgs[7] = new Sprite(game.assetManager.get("res/bgs/bgBlackRedDark2.png", Texture.class));
			break;
		case 4:
			bgs[8] = new Sprite(game.assetManager.get("res/bgs/bgAquaDark1.png", Texture.class));
			bgs[9] = new Sprite(game.assetManager.get("res/bgs/bgAquaDark2.png", Texture.class));
			break;
		case 5:
			bgs[10] = new Sprite(game.assetManager.get("res/bgs/bgPinkDark1.png", Texture.class));
			bgs[11] = new Sprite(game.assetManager.get("res/bgs/bgPinkDark2.png", Texture.class));
			break;
		case 6:
			bgs[12] = new Sprite(game.assetManager.get("res/bgs/bgBlueDark1.png", Texture.class));
			bgs[13] = new Sprite(game.assetManager.get("res/bgs/bgBlueDark2.png", Texture.class));
			break;
		case 7:
			bgs[14] = new Sprite(game.assetManager.get("res/bgs/bgDark1.png", Texture.class));
			bgs[15] = new Sprite(game.assetManager.get("res/bgs/bgDark2.png", Texture.class));
			break;
		case 8:
			bgs[16] = new Sprite(game.assetManager.get("res/bgs/bgBlue1.png", Texture.class));
			bgs[17] = new Sprite(game.assetManager.get("res/bgs/bgBlue2.png", Texture.class));
			break;
		case 9:
			bgs[18] = new Sprite(game.assetManager.get("res/bgs/bgGreenBlue1.png", Texture.class));
			bgs[19] = new Sprite(game.assetManager.get("res/bgs/bgGreenBlue2.png", Texture.class));
			break;
		}
	}

	private void loadBgsAssets() {
		if(load){
			if(game.assetManager.update()){
				switch(bgLoaded){
				case 0:
					bgs[0] = new Sprite(game.assetManager.get("res/bgs/bgBlack1.png", Texture.class));
					bgs[1] = new Sprite(game.assetManager.get("res/bgs/bgBlack2.png", Texture.class));
					break;
				case 1:
					bgs[2] = new Sprite(game.assetManager.get("res/bgs/bgBrownDark1.png", Texture.class));
					bgs[3] = new Sprite(game.assetManager.get("res/bgs/bgBrownDark2.png", Texture.class));
					break;
				case 2:
					bgs[4] = new Sprite(game.assetManager.get("res/bgs/bgPurple1.png", Texture.class));
					bgs[5] = new Sprite(game.assetManager.get("res/bgs/bgPurple2.png", Texture.class));
					break;
				case 3:
					bgs[6] = new Sprite(game.assetManager.get("res/bgs/bgBlackRedDark1.png", Texture.class));
					bgs[7] = new Sprite(game.assetManager.get("res/bgs/bgBlackRedDark2.png", Texture.class));
					break;
				case 4:
					bgs[8] = new Sprite(game.assetManager.get("res/bgs/bgAquaDark1.png", Texture.class));
					bgs[9] = new Sprite(game.assetManager.get("res/bgs/bgAquaDark2.png", Texture.class));
					break;
				case 5:
					bgs[10] = new Sprite(game.assetManager.get("res/bgs/bgPinkDark1.png", Texture.class));
					bgs[11] = new Sprite(game.assetManager.get("res/bgs/bgPinkDark2.png", Texture.class));
					break;
				case 6:
					bgs[12] = new Sprite(game.assetManager.get("res/bgs/bgBlueDark1.png", Texture.class));
					bgs[13] = new Sprite(game.assetManager.get("res/bgs/bgBlueDark2.png", Texture.class));
					break;
				case 7:
					bgs[14] = new Sprite(game.assetManager.get("res/bgs/bgDark1.png", Texture.class));
					bgs[15] = new Sprite(game.assetManager.get("res/bgs/bgDark2.png", Texture.class));
					break;
				case 8:
					bgs[16] = new Sprite(game.assetManager.get("res/bgs/bgBlue1.png", Texture.class));
					bgs[17] = new Sprite(game.assetManager.get("res/bgs/bgBlue2.png", Texture.class));
					break;
				case 9:
					bgs[18] = new Sprite(game.assetManager.get("res/bgs/bgGreenBlue1.png", Texture.class));
					bgs[19] = new Sprite(game.assetManager.get("res/bgs/bgGreenBlue2.png", Texture.class));
					break;
				}
				galaxySelected = bgLoaded;
				bgs[bgLoaded*2].setSize(Gdx.graphics.getWidth(), bgs[bgLoaded*2].getHeight()*y);
				bgs[bgLoaded*2 + 1].setSize(Gdx.graphics.getWidth(), bgs[bgLoaded*2 + 1].getHeight()*y);
				load = false;
			}
		}		
	}
	
	public void drawBackground(){
		// draw the second background
		bgs[galaxySelected*2].setPosition(currentBgX - bgs[galaxySelected*2+1].getWidth(), currentBgY);
		bgs[galaxySelected*2].draw(game.batch);
		bgs[galaxySelected*2+1].setPosition(currentBgX - bgs[galaxySelected*2].getWidth(), currentBgY - bgs[galaxySelected*2+1].getHeight());
		bgs[galaxySelected*2+1].draw(game.batch);
		// draw the first background
		bgs[galaxySelected*2].setPosition(currentBgX - bgs[galaxySelected*2+1].getWidth(), 
				currentBgY - bgs[galaxySelected*2].getHeight() - bgs[galaxySelected*2+1].getHeight());
		bgs[galaxySelected*2].draw(game.batch);
		bgs[galaxySelected*2+1].setPosition(currentBgX - bgs[galaxySelected*2].getWidth(), 
				currentBgY - bgs[galaxySelected*2].getHeight() - 2*bgs[galaxySelected*2+1].getHeight());
		bgs[galaxySelected*2+1].draw(game.batch);
		if(TimeUtils.nanoTime() - lastTimeBg > 25000000 && state != LAUNCH){
			// move the separator
			currentBgY -= fy(2);
			// set the current time to lastTimeBg
			lastTimeBg = TimeUtils.nanoTime();
		}		
		// if the separator reaches the screen edge, move it back to the first position
		if(currentBgY <= bgs[galaxySelected*2].getHeight() - Gdx.graphics.getDeltaTime()) currentBgY = bgs[galaxySelected*2].getHeight() + 2*bgs[galaxySelected*2+1].getHeight();
		if(currentBgX <= 0) currentBgX = bgs[galaxySelected*2].getWidth();
	}

	public void tweenBlueBig(){
		Tween.to(ffSprites[0], SpriteAccessor.SCALE, 0.125f).target(ffSprites[0].getScaleX()*3, ffSprites[0].getScaleY()*3).ease(TweenEquations.easeInOutSine).start(tweenManager);
		Tween.to(ffSprites[0], SpriteAccessor.COLOR, 0.1f).target(1f, 1f, 1f, 0f).start(tweenManager);
		Tween.set(ffSprites[0], SpriteAccessor.SCALE).target((fx(ffSprites[0].getWidth()) + fy(ffSprites[0].getHeight()))/2/96, 
				(fx(ffSprites[0].getWidth()) + fy(ffSprites[0].getHeight()))/2/96).delay(0.125f).ease(TweenEquations.easeInOutSine).start(tweenManager);
		Tween.set(ffSprites[0], SpriteAccessor.COLOR).target(1f, 1f, 1f, 1f).delay(0.125f).start(tweenManager);
	}

	public void tweenRedBig(){
		Tween.to(ffSprites[1], SpriteAccessor.SCALE, 0.125f).target(ffSprites[1].getScaleX()*3, ffSprites[1].getScaleX()*3).ease(TweenEquations.easeInOutSine).start(tweenManager);
		Tween.to(ffSprites[1], SpriteAccessor.COLOR, 0.1f).target(1f, 1f, 1f, 0f).start(tweenManager);
		Tween.set(ffSprites[1], SpriteAccessor.SCALE).target((fx(ffSprites[1].getWidth()) + fy(ffSprites[1].getHeight()))/2/96, 
				(fx(ffSprites[1].getWidth()) + fy(ffSprites[1].getHeight()))/2/96).delay(0.125f).ease(TweenEquations.easeInOutSine).start(tweenManager);
		Tween.set(ffSprites[1], SpriteAccessor.COLOR).target(1f, 1f, 1f, 1f).delay(0.125f).start(tweenManager);
	}	
	
	private final TweenCallback glowCallBack = new TweenCallback() {
		public void onEvent(int type, BaseTween<?> source) {
			float t = MathUtils.random(0.1f, 0.5f);
			if(state != LAUNCH){
				Timeline.createSequence().
					push(Tween.to(ffgSprites[forceFieldSelected], SpriteAccessor.SCALE, t).target(ffgSprites[forceFieldSelected].getScaleX()*1.032f, ffgSprites[forceFieldSelected].getScaleX()*1.032f).ease(TweenEquations.easeNone)).
					push(Tween.set(ffgSprites[forceFieldSelected], SpriteAccessor.SCALE).target(ffgSprites[forceFieldSelected].getScaleX(), ffgSprites[forceFieldSelected].getScaleY()).ease(TweenEquations.easeNone)).
					setCallback(glowCallBack).
				start(tweenManager);
			}
		}
	};
	
	private final TweenCallback countersCallBack = new TweenCallback() {
		public void onEvent(int type, BaseTween<?> source) {
			Timeline.createSequence().
			pushPause(1).
			beginParallel().
				push(Tween.to(blueCounter, SpriteAccessor.COLOR, 0.5f).target(1f, 1f, 1f, 0f)).
				push(Tween.to(redCounter, SpriteAccessor.COLOR, 0.5f).target(1f, 1f, 1f, 0f)).
				push(Tween.to(redsFont, BitmapFontAccessor.COLOR, 0.5f).target(1f, 1f, 1f, 0f)).
				push(Tween.to(bluesFont, BitmapFontAccessor.COLOR, 0.5f).target(1f, 1f, 1f, 0f)).
			end().
			beginParallel().
				push(Tween.to(lifeCounter, SpriteAccessor.COLOR, 0.5f).target(1f, 1f, 1f, 1f)).
				push(Tween.to(lifeFont, BitmapFontAccessor.COLOR, 0.5f).target(1f, 1f, 1f, 1f)).
			end().
			pushPause(1).
			beginParallel().
				push(Tween.to(lifeCounter, SpriteAccessor.COLOR, 0.5f).target(1f, 1f, 1f, 0f)).
				push(Tween.to(lifeFont, BitmapFontAccessor.COLOR, 0.5f).target(1f, 1f, 1f, 0f)).
			end().
			beginParallel().
				push(Tween.to(blueCounter, SpriteAccessor.COLOR, 0.5f).target(1f, 1f, 1f, 1f)).
				push(Tween.to(redCounter, SpriteAccessor.COLOR, 0.5f).target(1f, 1f, 1f, 1f)).
				push(Tween.to(redsFont, BitmapFontAccessor.COLOR, 0.5f).target(1f, 1f, 1f, 1f)).
				push(Tween.to(bluesFont, BitmapFontAccessor.COLOR, 0.5f).target(1f, 1f, 1f, 1f)).
			end().
			setCallback(countersCallBack).
			start(tweenManager);
		}
	};
	
	/** Android Back: close the open sub-screen like its back arrow does; on the main screen, leave the game. */
	public void onBack(){
		Stage stage;
		boolean spent = false;
		switch(state){
			case SHIPS: stage = stageShips; spent = true; break;
			case GALAXIES: stage = stageGalaxies; spent = true; break;
			case UPGRADES: stage = stageUpgrades; spent = true; break;
			case STATS: stage = stageStats; break;
			case SETTINGS: stage = stageSettings; break;
			case HELP: stage = stageHelp; break;
			case STANDBY: Gdx.app.exit(); return;
			default: return;
		}
		state = STANDBY;
		if(sound == ON)
			buttonSmall.play();
		stage.addAction(moveTo(0, stage.getHeight(), 0.5f));
		effectsMenuOff();
		if(spent)
			updateStatsMenuSpent();
	}

	/** The music follows the Sound setting: silent when it is OFF, otherwise the faded-in level. */
	public void updateMusicVolume(){
		music.setVolume(sound == ON ? musicFade : 0f);
	}

	/**
	 * Stacks the main menu around the ring with one equal gap between title, PLAY, ring, bottom
	 * row, counters and best score, so it stays compact on tall screens. The sub-menus keep the
	 * full-height layout (counterBaseY/scoreBaseY); the counters and score slide between the two.
	 */
	/**
	 * The counter row: blue number, animated blue orb, animated red orb, red number, with the number-to-orb gaps the
	 * main menu uses. Used by the main menu and by the game-over screen so both always match.
	 * @param y bottom of the orbs; alpha &lt; 0 keeps the sprites' and fonts' own colors, otherwise the row is drawn at that alpha
	 * @param blueText / redText numbers to show (null skips that number)
	 */
	public void drawBlueRedRow(com.badlogic.gdx.graphics.g2d.SpriteBatch b, String blueText, String redText, float y, float alpha){
		float oldBlueY = blueCounter.getY(), oldRedY = redCounter.getY();
		com.badlogic.gdx.graphics.Color oldBlue = new com.badlogic.gdx.graphics.Color(blueCounter.getColor());
		com.badlogic.gdx.graphics.Color oldRed = new com.badlogic.gdx.graphics.Color(redCounter.getColor());
		com.badlogic.gdx.graphics.Color oldBluesFont = new com.badlogic.gdx.graphics.Color(bluesFont.getColor());
		com.badlogic.gdx.graphics.Color oldRedsFont = new com.badlogic.gdx.graphics.Color(redsFont.getColor());
		blueCounter.setY(y);
		redCounter.setY(y);
		if(alpha >= 0f){
			blueCounter.setColor(1f, 1f, 1f, alpha);
			redCounter.setColor(1f, 1f, 1f, alpha);
			bluesFont.setColor(1f, 1f, 1f, alpha);
			redsFont.setColor(1f, 1f, 1f, alpha);
		}
		Fx.init(game.assetManager);
		Fx.drawOrb(b, blueCounter, 0, 0f);
		Fx.drawOrb(b, redCounter, 1, 0.33f);
		if(blueText != null){
			float w = com.spaceship.handlers.FontUtil.bounds(bluesFont, blueText).width;
			float h = com.spaceship.handlers.FontUtil.bounds(bluesFont, blueText).height;
			bluesFont.draw(b, blueText, blueCounter.getX() - w - blueCounter.getWidth()/4, y + 1.2f*h);
		}
		if(redText != null){
			float h = com.spaceship.handlers.FontUtil.bounds(redsFont, redText).height;
			redsFont.draw(b, redText, redCounter.getX() + 1.25f*redCounter.getWidth(), y + 1.2f*h);
		}
		blueCounter.setY(oldBlueY);
		redCounter.setY(oldRedY);
		blueCounter.setColor(oldBlue);
		redCounter.setColor(oldRed);
		bluesFont.setColor(oldBluesFont);
		redsFont.setColor(oldRedsFont);
	}

	/** Bottom anchor of the full-height sub-menu layout (the counters' resting y before the compact main-menu stack moves them up). */
	public float getCounterBaseY(){
		return counterBaseY;
	}

	private void layoutMainMenu(){
		float gap = 0.7f*bUpgrades.getHeight();
		float ringTop = bUpgrades.getY() + bUpgrades.getHeight();
		float ringBottom = bGalaxies.getY();

		bGo.setY(ringTop + gap);
		float titleH = com.spaceship.handlers.FontUtil.bounds(greysFontTitle, "Collect The Orbs").height;
		float titleBottom = bGo.getY() + bGo.getHeight() + gap;
		titleY2 = titleBottom + titleH;
		titleY1 = titleY2 + 1.5f*titleH;

		float rowY = ringBottom - gap - bExit.getHeight();
		bExit.setY(rowY);
		bTrophy.setY(rowY);
		bLeaderboard.setY(rowY);

		mainCounterY = rowY - gap - blueCounter.getHeight();
		mainScoreY = mainCounterY - gap;
		scoreBaseY = 1.5f*com.spaceship.handlers.FontUtil.bounds(greysFont, "Best Score").height + adHeight;
		setCounterY(mainCounterY);
		scoreY = mainScoreY;
	}

	private float scoreY;

	private void setCounterY(float y){
		blueCounter.setY(y);
		redCounter.setY(y);
		lifeCounter.setY(y);
	}

	private void slideBottomBlock(){
		boolean compact = state == STANDBY || state == LAUNCH || state == EXIT || state == LEAVING;
		setCounterY(blueCounter.getY() + ((compact ? mainCounterY : counterBaseY) - blueCounter.getY())*0.2f);
		scoreY += ((compact ? mainScoreY : scoreBaseY) - scoreY)*0.2f;
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

	@Override
	public void hide() {
		tweenManager.killTarget(blueCounter);
		tweenManager.killTarget(redCounter);
		tweenManager.killTarget(lifeCounter);
		tweenManager.killTarget(bluesFont);
		tweenManager.killTarget(redsFont);
		tweenManager.killTarget(lifeFont);
		shadow = 0;
		startTrace = false;
	}

	@Override
	public void pause() {
		if(Gdx.app.getType().equals(ApplicationType.Android))
			game.getActionResolver().giftizOnPause();
	}

	@Override
	public void resume() {
		if(Gdx.app.getType().equals(ApplicationType.Android)){
			game.getActionResolver().giftizOnResume();
			g = game.getActionResolver().updateButtonImage();
		}	
	}
	
	@Override
	public void dispose() {
		//	    forceField.dispose();
		//		bgsBought.dispose();
		//		bgsPrice.dispose();
		//		shipsPrice1.dispose();
		//		shipsPrice2.dispose();
		//		shipsPrice3.dispose();
		//		shipsPrice4.dispose();
		//		shipsPrice5.dispose();
		//		shipsBought1.dispose();
		//		shipsBought2.dispose();
		//		shipsBought3.dispose();
		//		shipsBought4.dispose();
		//		shipsBought5.dispose();
		//		ships1.dispose();
		//		ships2.dispose();
		//		ships3.dispose();
		//		ships4.dispose();
		//		ships5.dispose();
		//		ffChange.dispose();
		//		launch.dispose();
		//		button3.dispose();
		//		button32.dispose();
		//		
		//		greysFontTitle.dispose();
		//		greysFont.dispose();
		//		bluesFont.dispose();
		//		redsFont.dispose();
		//		stageShips.dispose();
		//		stageGalaxies.dispose();
	}

}
