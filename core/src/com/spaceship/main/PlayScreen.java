package com.spaceship.main;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo;

import java.util.ArrayList;
import java.util.Iterator;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenEquations;
import aurelienribon.tweenengine.TweenManager;
import aurelienribon.tweenengine.equations.Sine;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.sql.DatabaseCursor;
import com.badlogic.gdx.sql.SQLiteGdxException;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.spaceship.handlers.Fx;
import com.spaceship.handlers.BitmapFontAccessor;
import com.spaceship.handlers.CircleAccessor;
import com.spaceship.handlers.SpriteAccessor;
import com.spaceship.objects.Ball;
import com.spaceship.objects.Spaceship;

public class PlayScreen implements Screen {

	final Main game;
	private OrthographicCamera camera;
	private TweenManager tweenManager;
	private static int state;
	public static int galaxySelected;

	public final int PLAY = 0;
	public static final int PAUSE = 1;
	public static final int DEAD = 2;
	public static final int DEADLEAVING = 3;
	public static final int PAUSELEAVING = 4;
	public static final int HELP = 5;

	public ArrayList<Ball> random;

	private TextureRegion[] lifeTextures;
	private TextureRegion[] objectsTextures;

	private Sprite[] lifeSprites;
	private Sprite lifeBarSprite;
	private com.spaceship.handlers.EnergyBar energyBar;
	public static Sprite arrowLeft;
	public static Sprite arrowRight;
	private Sprite bHome;
	private Sprite bRetry;
	private Sprite bExit;
	private Sprite bHome2;
	private Sprite bRetry2;
	private Sprite bExit2;
	private Sprite bPlay;
	private Sprite bPause;
	private Sprite bPauseCant;
	private Sprite bSoundOn;
	private Sprite bSoundOff;
	private Sprite bVibrationOn;
	private Sprite bVibrationOff;
	private Sprite bControlsOn;
	private Sprite bControlsOff;
	private Sprite bHelp;
	private Sprite bNo;
	private Sprite bYes;
	private Sprite bQuestionEN;
	private Sprite bQuestionES;
	private Sprite bShare;
	private Sprite[] redParticles;
	private Sprite[] blueParticles;
	private Sprite[] lifeParticles;
	private Sprite[] objectsParticles;
	private Sprite blueCounter;
	private Sprite redCounter;
	private Sprite[] blueWrong;
	private Sprite[] redWrong;
	private Texture objects;
	private Sprite ring1;
	private Sprite ring2;
	private Sprite ring3;
	private Sprite[] objectsSprites1;
	private Sprite[] objectsSprites2;
	private Sprite[] objectsSprites3;
	public static Sprite padVerticalLeft;
	public static Sprite padVerticalRight;
	public static Sprite padHorizontalUp;
	public static Sprite padHorizontalDown;
	public static Sprite padVerticalLeftEsp;
	public static Sprite padVerticalRightEsp;
	public static Sprite padHorizontalUpEsp;
	public static Sprite padHorizontalDownEsp;
	public Sprite cover;

	public static Sprite bgs1;
	public static Sprite bgs2;

	public Sound absorb;
	public Sound error;
	public Sound life;
	public Sound dead;
	public Sound buttonLarge;
	public Sound addScore;
	public Sound shield;
	public Sound object;
	public Sound objectCount;
	public Sound slowDown;
	public Sound chargeSound;
	public Sound boostSound;
	public Sound slowDownReverse;
	public Sound sword;
	public Sound swordReverse;
	public Sound swap;
	public Sound swapReverse;
	public Sound mult;
	public Sound multReverse;

	private Spaceship ship;
	private Array<Ball> dBalls;
	private Ball[] balls;
	private DatabaseCursor cursor;

	public BitmapFont bluesFont;
	public BitmapFont redsFont;
	public BitmapFont lifesFont;
	public BitmapFont bluesFont2;
	public BitmapFont redsFont2;
	public BitmapFont greysFont;
	public BitmapFont greysFont2;
	public BitmapFont redsFont3;
	public BitmapFont bluesFont3;

	public int velocityBg;
	public int velocityBall;
	private final com.spaceship.handlers.SpeedDirector director = new com.spaceship.handlers.SpeedDirector();
	private final com.spaceship.handlers.SpeedUpFx speedFx = new com.spaceship.handlers.SpeedUpFx();
	private boolean chargeSoundStarted;
	public int blues;
	public int reds;
	public int lifes;
	public int finalScore;
	public int bestScore;
	public int ballToAdd;
	public int xx;
	public int yy;
	public int d;
	public int blueP;
	public int redP;
	public int lifeP;
	public int objectsP;
	public int redW;
	public int blueW;
	public int adHeight;
	public int ballSpawnSpeed;
	public int bluesValue;
	public int redsValue;
	public int auxObj;
	public int life1x;
	private static final int POPUPS = 8;
	private final float[] popX = new float[POPUPS], popY = new float[POPUPS], popA = new float[POPUPS];
	private final int[] popValue = new int[POPUPS];
	private final boolean[] popBlue = new boolean[POPUPS];
	private int popNext;
	public int life1y;
	public int deathAngle;
	public int deathAngle2;
	public float redFont3Height;
	public float blueFont3Height;

	public int games;
	public int totalPoints;
	public int avgPoints;
	public int totalOrbs;
	public int totalBlues;
	public int totalReds;
	public int totalLifes;
	public int totalObjects;
	public int timePlaying;
	public int lifesCounter;
	public int objCounter;
	public int[] obj;

	public int barWidth;
	public int object1;
	public int object2;
	public int object3;

	public float currentBgY;

	public long lastVelocityIncrease;
	public long lastBallTime;
	public long timeBetweenBalls;
	public long lastTimeBg;
	public long blueTimer;
	public long redTimer;
	public long meterTimer;
	public long lastMeterTimer;
	public long objectClock;
	public long time;

	public boolean first, firstBall, firstBallHelper, firstDeath, firstMMS, updatedDB;
	public boolean tweenEnd, clicked, canTouch, upBoth, upRed, upBlue, changeBothScore, changeRedScore, changeBlueScore, canTweenRedScore, canTweenBlueScore;
	public boolean redToBlue, blueToRed, blueX2, redX2, slow, blueShield, redShield, blueX3, redX3, goldShield;
	public boolean canIncreaseSpeed, drawLife, objectClockEnable, canPlayRetreat;

	private Stage stageHelp;
	private Table containerHelp;
	private Table imgsHelp;
	private ScrollPane scrollHelp;
	private Label itemsLabel;
	private Label orbsLabel;
	private Label padsTitleLabel;
	private Label tutoLabel;
	private Image padsControlsEN;
	private Image padsControlsES;
	private Image orbsEN;
	private Image itemsEN;
	private Image instEN;
	private Image instES;
	private Image orbsES;
	private Image itemsES;
	private Sprite bBack;
	
//	private ParticleEffect effect;
	
	float ratioMotoG = 0.5625f;
	float ratioV = (float) Gdx.graphics.getWidth()/Gdx.graphics.getHeight();
	float x = Math.min(Gdx.graphics.getWidth()/720f, Gdx.graphics.getHeight()/1280f);
	float y = Math.min(Gdx.graphics.getWidth()/720f, Gdx.graphics.getHeight()/1280f);

	public PlayScreen(final Main gam){
		this.game = gam;

		adHeight = game.getMMS().adHeight;

		// tween stuff
		tweenManager = new TweenManager();
		Tween.setCombinedAttributesLimit(4);
		Tween.registerAccessor(Sprite.class, new SpriteAccessor());
		Tween.registerAccessor(Circle.class, new CircleAccessor());
		Tween.registerAccessor(BitmapFont.class, new BitmapFontAccessor());

		firstMMS = true;

		// create and set the camera
		camera = new OrthographicCamera();
		camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		
//		effect = new ParticleEffect();
//		effect.load(Gdx.files.internal("res/ptest.p"), Gdx.files.internal("res"));
//		effect.setPosition(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2);
//		effect.getEmitters().get(0).getSprite().setSize(20, 45);
//		effect.start();
		
		//load sounds
		absorb = game.assetManager.get("audio/absorb.ogg", Sound.class);
		error = game.assetManager.get("audio/loseLife.ogg", Sound.class);
		life = game.assetManager.get("audio/life.ogg", Sound.class);
		dead = game.assetManager.get("audio/dead.ogg", Sound.class);
		buttonLarge = game.assetManager.get("audio/buttonLarge.ogg", Sound.class);
		addScore = game.assetManager.get("audio/bought.ogg", Sound.class);
		shield = game.assetManager.get("audio/shield.ogg", Sound.class);
		object = game.assetManager.get("audio/object.ogg", Sound.class);
		objectCount = game.assetManager.get("audio/objectCount.ogg", Sound.class);
		slowDown = game.assetManager.get("audio/slow.ogg", Sound.class);
		chargeSound = game.assetManager.get("audio/charge.ogg", Sound.class);
		boostSound = game.assetManager.get("audio/boost.ogg", Sound.class);
		slowDownReverse = game.assetManager.get("audio/slowReverse.ogg", Sound.class);
		sword = game.assetManager.get("audio/sword.ogg", Sound.class);
		swordReverse = game.assetManager.get("audio/swordReverse.ogg", Sound.class);
		swap = game.assetManager.get("audio/swap.ogg", Sound.class);
		swapReverse = game.assetManager.get("audio/swapReverse.ogg", Sound.class);
		mult = game.assetManager.get("audio/mult.ogg", Sound.class);
		multReverse = game.assetManager.get("audio/multReverse.ogg", Sound.class);
		//load fonts
		bluesFont = game.assetManager.get("fonts/prototype128blueWhite.fnt", BitmapFont.class);
		redsFont = game.assetManager.get("fonts/prototype128redBlack.fnt", BitmapFont.class);
		lifesFont = game.assetManager.get("fonts/prototype64yellowOrange.fnt", BitmapFont.class);
		bluesFont2 = game.assetManager.get("fonts/prototype64blueWhite2.fnt", BitmapFont.class);
		redsFont2 = game.assetManager.get("fonts/prototype64redBlack2.fnt", BitmapFont.class);
		greysFont = game.assetManager.get("fonts/prototype96grey.fnt", BitmapFont.class);
		greysFont2 = game.assetManager.get("fonts/prototype96grey2.fnt", BitmapFont.class);
		redsFont3 = game.assetManager.get("fonts/prototype64redBlack.fnt", BitmapFont.class);
		bluesFont3 = game.assetManager.get("fonts/prototype64blueWhite.fnt", BitmapFont.class);
		//load buttons
		bHome = new Sprite(game.assetManager.get("res/gui/bHome.png", Texture.class));
		bRetry = new Sprite(game.assetManager.get("res/gui/bRetry.png", Texture.class));
		bShare = new Sprite(game.assetManager.get("res/gui/bShare.png", Texture.class));
		bExit = new Sprite(game.assetManager.get("res/gui/bExit.png", Texture.class));
		bHome2 = new Sprite(game.assetManager.get("res/gui/bHome.png", Texture.class));
		bRetry2 = new Sprite(game.assetManager.get("res/gui/bRetry.png", Texture.class));
		bExit2 = new Sprite(game.assetManager.get("res/gui/bExit.png", Texture.class));
		bPlay = new Sprite(game.assetManager.get("res/gui/bPlay.png", Texture.class));
		bPause = new Sprite(game.assetManager.get("res/gui/bPause.png", Texture.class));
		bPauseCant = new Sprite(game.assetManager.get("res/gui/bPauseCant.png", Texture.class));
		bSoundOn = new Sprite(game.assetManager.get("res/gui/bSoundOn.png", Texture.class));
		bSoundOff = new Sprite(game.assetManager.get("res/gui/bSoundOff.png", Texture.class));
		bVibrationOn = new Sprite(game.assetManager.get("res/gui/bVibrationOn.png", Texture.class));
		bVibrationOff = new Sprite(game.assetManager.get("res/gui/bVibrationOff.png", Texture.class));
		bControlsOn = new Sprite(game.assetManager.get("res/gui/bControlsOn.png", Texture.class));
		bControlsOff = new Sprite(game.assetManager.get("res/gui/bControlsOff.png", Texture.class));
		bHelp = new Sprite(game.assetManager.get("res/gui/bTutorial.png", Texture.class));
		bNo = new Sprite(game.assetManager.get("res/gui/bNo.png", Texture.class));
		bYes = new Sprite(game.assetManager.get("res/gui/bYes.png", Texture.class));
		bQuestionEN = new Sprite(game.assetManager.get("res/gui/bQuestionEN.png", Texture.class));
		bQuestionES = new Sprite(game.assetManager.get("res/gui/bQuestionES.png", Texture.class));

		padVerticalLeft = new Sprite(game.assetManager.get("res/hud/padsLeft.png", Texture.class));
		padVerticalRight = new Sprite(game.assetManager.get("res/hud/padsRight.png", Texture.class));
		padHorizontalUp = new Sprite(game.assetManager.get("res/hud/padsItem.png", Texture.class));
		padHorizontalDown = new Sprite(game.assetManager.get("res/hud/padsShield.png", Texture.class));
		padVerticalLeftEsp = new Sprite(game.assetManager.get("res/hud/padsIzq.png", Texture.class));
		padVerticalRightEsp = new Sprite(game.assetManager.get("res/hud/padsDer.png", Texture.class));
		padHorizontalUpEsp = new Sprite(game.assetManager.get("res/hud/padsItemEsp.png", Texture.class));
		padHorizontalDownEsp = new Sprite(game.assetManager.get("res/hud/padsShieldEsp.png", Texture.class));

		cover = new Sprite(game.assetManager.get("res/hud/padVertical.png", Texture.class));

		//load help menu
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
		bBack = new Sprite(game.assetManager.get("res/gui/bBack.png", Texture.class));
		bBack.setSize((fx(bBack.getWidth()) + fy(bBack.getHeight()))/2, (fy(bBack.getWidth()) + fy(bBack.getHeight()))/2);
		
		//load objects
		ring1 = new Sprite(game.assetManager.get("res/objects/ring.png", Texture.class));
		ring2 = new Sprite(game.assetManager.get("res/objects/ring.png", Texture.class));
		ring3 = new Sprite(game.assetManager.get("res/objects/ring.png", Texture.class));
		objects = game.assetManager.get("res/objects/objects.png", Texture.class);
		objectsTextures = TextureRegion.split(objects, 160, 160)[0];
		objectsSprites1 = new Sprite[objectsTextures.length];
		objectsSprites2 = new Sprite[objectsTextures.length];
		objectsSprites3 = new Sprite[objectsTextures.length];
		for(int i = 0; i < objectsTextures.length; i++){
			objectsSprites1[i] = new Sprite(objectsTextures[i]);
			objectsSprites2[i] = new Sprite(objectsTextures[i]);
			objectsSprites3[i] = new Sprite(objectsTextures[i]);
		}

		//load counters
		blueCounter = new Sprite(game.assetManager.get("res/shapes/blueCounter.png", Texture.class));
		redCounter = new Sprite(game.assetManager.get("res/shapes/redCounter.png", Texture.class));

		//load arrows
		arrowLeft = new Sprite(game.assetManager.get("res/hud/arrowLeft.png", Texture.class));
		arrowRight = new Sprite(game.assetManager.get("res/hud/arrowRight.png", Texture.class));

		//load particles and wrongs
		redWrong = new Sprite[5];
		blueWrong = new Sprite[5];
		for(int h = 0; h < 5; h++){
			redWrong[h] = new Sprite(game.assetManager.get("res/shapes/redWrong.png", Texture.class));
			redWrong[h].setSize(20, 20);
			redWrong[h].setScale(fxy(20)/20, fxy(20)/20);
			blueWrong[h] = new Sprite(game.assetManager.get("res/shapes/blueWrong.png", Texture.class));
			blueWrong[h].setSize(20, 20);
			blueWrong[h].setScale(fxy(20)/20, fxy(20)/20);
		}
		Fx.init(game.assetManager);
		redParticles = new Sprite[20];
		blueParticles = new Sprite[20];
		lifeParticles = new Sprite[20];
		objectsParticles = new Sprite[20];
		for(int h = 0; h < 20; h++){
			redParticles[h] = new Sprite(Fx.spark(Fx.SPARK_RED));
			redParticles[h].setSize(fxy(16), fxy(16));
			blueParticles[h] = new Sprite(Fx.spark(Fx.SPARK_BLUE));
			blueParticles[h].setSize(fxy(16), fxy(16));
			lifeParticles[h] = new Sprite(Fx.spark(Fx.SPARK_GOLD));
			lifeParticles[h].setSize(fxy(16), fxy(16));
			objectsParticles[h] = new Sprite(Fx.spark(Fx.SPARK_PURPLE));
			objectsParticles[h].setSize(fxy(16), fxy(16));
		}

		//set positions and scales for buttons, objects and arrows
		arrowLeft.setSize(fx(arrowLeft.getWidth()), fy(arrowLeft.getHeight()));
		arrowRight.setSize(fx(arrowRight.getWidth()), fy(arrowRight.getHeight()));
		bHome2.setSize(fx(bHome2.getWidth()), fy(bHome2.getHeight()));
		bRetry2.setSize(fx(bRetry2.getWidth()), fy(bRetry2.getHeight()));
		bExit2.setSize(fx(bExit2.getWidth()), fy(bExit2.getHeight()));
		bHome.setSize(fx(bHome.getWidth()), fy(bHome.getHeight()));
		bRetry.setSize(fx(bRetry.getWidth()), fy(bRetry.getHeight()));
		bShare.setSize(fx(bShare.getWidth()), fy(bShare.getHeight()));
		bExit.setSize(fx(bExit.getWidth()), fy(bExit.getHeight()));
		bPause.setSize((fx(bPause.getWidth()) + fy(bPause.getHeight()))/2, (fx(bPause.getWidth()) + fy(bPause.getHeight()))/2);
		bPauseCant.setSize((fx(bPauseCant.getWidth()) + fy(bPauseCant.getHeight()))/2, (fx(bPauseCant.getWidth()) + fy(bPauseCant.getHeight()))/2);
		bPlay.setSize((fx(bPlay.getWidth()) + fy(bPlay.getHeight()))/2, (fx(bPlay.getWidth()) + fy(bPlay.getHeight()))/2);
		bVibrationOn.setSize((fx(bVibrationOn.getWidth()) + fy(bVibrationOn.getHeight()))/2, (fx(bVibrationOn.getWidth()) + fy(bVibrationOn.getHeight()))/2);
		bVibrationOff.setSize((fx(bVibrationOff.getWidth()) + fy(bVibrationOff.getHeight()))/2, (fx(bVibrationOff.getWidth()) + fy(bVibrationOff.getHeight()))/2);
		bControlsOn.setSize((fx(bControlsOn.getWidth()) + fy(bControlsOn.getHeight()))/2, (fx(bControlsOn.getWidth()) + fy(bControlsOn.getHeight()))/2);
		bControlsOff.setSize((fx(bControlsOff.getWidth()) + fy(bControlsOff.getHeight()))/2, (fx(bControlsOff.getWidth()) + fy(bControlsOff.getHeight()))/2);
		bSoundOn.setSize((fx(bSoundOn.getWidth()) + fy(bSoundOn.getHeight()))/2, (fx(bSoundOn.getWidth()) + fy(bSoundOn.getHeight()))/2);
		bSoundOff.setSize((fx(bSoundOff.getWidth()) + fy(bSoundOff.getHeight()))/2, (fx(bSoundOff.getWidth()) + fy(bSoundOff.getHeight()))/2);
		bHelp.setSize((fx(bHelp.getWidth()) + fy(bHelp.getHeight()))/2, (fx(bHelp.getWidth()) + fy(bHelp.getHeight()))/2);
		bYes.setSize((fx(bYes.getWidth()) + fy(bYes.getHeight()))/2, (fx(bYes.getWidth()) + fy(bYes.getHeight()))/2);
		bNo.setSize((fx(bNo.getWidth()) + fy(bNo.getHeight()))/2, (fx(bNo.getWidth()) + fy(bNo.getHeight()))/2);
		bQuestionEN.setSize(fx(bQuestionEN.getWidth()), fy(bQuestionEN.getHeight()));
		bQuestionES.setSize(fx(bQuestionES.getWidth()), fy(bQuestionES.getHeight()));
		padVerticalLeft.setSize(fx(padVerticalLeft.getWidth()), fy(padVerticalLeft.getHeight()) - adHeight/4);
		padVerticalRight.setSize(fx(padVerticalRight.getWidth()), fy(padVerticalRight.getHeight()) - adHeight/4);
		padHorizontalUp.setSize(fx(padHorizontalUp.getWidth()), fy(padHorizontalUp.getHeight()) - adHeight/8);
		padHorizontalDown.setSize(fx(padHorizontalDown.getWidth()), fy(padHorizontalDown.getHeight()) - adHeight/8);
		padVerticalLeftEsp.setSize(fx(padVerticalLeftEsp.getWidth()), fy(padVerticalLeftEsp.getHeight()) - adHeight/4);
		padVerticalRightEsp.setSize(fx(padVerticalRightEsp.getWidth()), fy(padVerticalRightEsp.getHeight()) - adHeight/4);
		padHorizontalUpEsp.setSize(fx(padHorizontalUpEsp.getWidth()), fy(padHorizontalUpEsp.getHeight()) - adHeight/8);
		padHorizontalDownEsp.setSize(fx(padHorizontalDownEsp.getWidth()), fy(padHorizontalDownEsp.getHeight()) - adHeight/8);
		ring1.setSize((fx(ring1.getWidth()) + fy(ring1.getHeight()))/2, (fx(ring1.getWidth()) + fy(ring1.getHeight()))/2);
		ring2.setSize((fx(0.75f*ring2.getWidth()) + fy(0.75f*ring2.getHeight()))/2, (fx(0.75f*ring2.getWidth()) + fy(0.75f*ring2.getHeight()))/2);
		ring3.setSize((fx(0.75f*ring3.getWidth()) + fy(0.75f*ring3.getHeight()))/2, (fx(0.75f*ring3.getWidth()) + fy(0.75f*ring3.getHeight()))/2);
		ring1.setPosition(ring1.getHeight()/3, Gdx.graphics.getHeight() - 4*ring1.getHeight()/3);
		// the ring art has a 10% transparent margin on each side: keep the same small gap between the visible rings
		float slotGap = 0.12f*ring1.getWidth();
		ring2.setPosition(ring1.getX() + 0.9f*ring1.getWidth() + slotGap - 0.1f*ring2.getWidth(), ring1.getY());
		ring3.setPosition(ring2.getX() + 0.9f*ring2.getWidth() + slotGap - 0.1f*ring3.getWidth(), ring1.getY());
		for(int i = 0; i < objectsSprites1.length; i++){
			objectsSprites1[i].setSize((fx(objectsSprites1[i].getWidth()) + fy(objectsSprites1[i].getHeight()))/2, (fx(objectsSprites1[i].getWidth()) + fy(objectsSprites1[i].getHeight()))/2);
			objectsSprites2[i].setSize((fx(0.75f*objectsSprites2[i].getWidth()) + fy(0.75f*objectsSprites2[i].getHeight()))/2, (fx(0.75f*objectsSprites2[i].getWidth()) + fy(0.75f*objectsSprites2[i].getHeight()))/2);
			objectsSprites3[i].setSize((fx(0.75f*objectsSprites3[i].getWidth()) + fy(0.75f*objectsSprites3[i].getHeight()))/2, (fx(0.75f*objectsSprites3[i].getWidth()) + fy(0.75f*objectsSprites3[i].getHeight()))/2);
			objectsSprites1[i].setPosition(ring1.getX(), ring1.getY());	
			objectsSprites2[i].setPosition(ring2.getX(), ring2.getY());	
			objectsSprites3[i].setPosition(ring3.getX(), ring3.getY());	
		}
		arrowLeft.setColor(1f, 1f, 1f, 0.3f);
		arrowRight.setColor(1f, 1f, 1f, 0.3f);
		blueCounter.setSize((fx(blueCounter.getWidth()) + fy(blueCounter.getHeight()))/2, (fx(blueCounter.getWidth()) + fy(blueCounter.getHeight()))/2);
		redCounter.setSize((fx(redCounter.getWidth()) + fy(redCounter.getHeight()))/2, (fx(redCounter.getWidth()) + fy(redCounter.getHeight()))/2);
		bluesFont2.getData().setScale(x, y);
		redsFont2.getData().setScale(x, y);
		bluesFont2.setColor(1f, 1f, 1f, 0.8f);
		redsFont2.setColor(1f, 1f, 1f, 0.8f);

		//set controls
		padVerticalLeft.setColor(1f, 1f, 1f, 0.225f);
		padVerticalRight.setColor(1f, 1f, 1f, 0.225f);
		padHorizontalUp.setColor(1f, 1f, 1f, 0.225f);
		padHorizontalDown.setColor(1f, 1f, 1f, 0.225f);
		padVerticalLeftEsp.setColor(1f, 1f, 1f, 0.225f);
		padVerticalRightEsp.setColor(1f, 1f, 1f, 0.225f);
		padHorizontalUpEsp.setColor(1f, 1f, 1f, 0.225f);
		padHorizontalDownEsp.setColor(1f, 1f, 1f, 0.225f);
		
		cover.setSize(Gdx.graphics.getWidth()*1.5f, Gdx.graphics.getHeight()*1.5f);
		cover.setColor(0.15f, 0.15f, 0.15f, 0.4f);
		cover.setPosition(-Gdx.graphics.getWidth()*0.25f, -Gdx.graphics.getHeight()*0.25f);
		
		bQuestionEN.setPosition(Gdx.graphics.getWidth()/2 - bQuestionEN.getWidth()/2, Gdx.graphics.getHeight()/2 + (bQuestionEN.getHeight() + 4*bNo.getHeight()/3)/2);
		bQuestionES.setPosition(Gdx.graphics.getWidth()/2 - bQuestionES.getWidth()/2, Gdx.graphics.getHeight()/2 + (bQuestionES.getHeight() + 4*bNo.getHeight()/3)/2);
		bYes.setPosition(Gdx.graphics.getWidth()/2 - 1.75f*bYes.getWidth(), bQuestionEN.getY() - 4*bYes.getHeight()/3);
		bNo.setPosition(Gdx.graphics.getWidth()/2 + 1.75f*bNo.getWidth() - bNo.getWidth(), bQuestionEN.getY() - 4*bNo.getHeight()/3);

		blueCounter.setPosition(Gdx.graphics.getWidth()/2 - 1.25f*blueCounter.getWidth(), Gdx.graphics.getHeight()/8 + 3*adHeight/4);
		redCounter.setPosition(Gdx.graphics.getWidth()/2 + 0.25f*redCounter.getWidth(), Gdx.graphics.getHeight()/8 + 3*adHeight/4);

		// create ship
		ship = new Spaceship(fxy(64), tweenManager, game);
		ship.forceFieldSelected = game.getMMS().forceFieldSelected;

		// creat balls array
		balls = new Ball[50];
		random = new ArrayList<Ball>();
		for(int h = 0; h < balls.length; h++){
			balls[h] = new Ball(MathUtils.random((fx(20) + fy(20))/2, Gdx.graphics.getWidth() - (fx(20) + fy(20))/2),
					Gdx.graphics.getHeight() + fxy(20), (fx(10) + fy(10))/2, MathUtils.random(0, 60), 350, tweenManager, game);
			random.add(balls[h]);
		}

		dBalls = new Array<Ball>();

		obj = new int[10];

	}

	@Override
	public void show(){

		if(!firstMMS){

			if(Gdx.app.getType().equals(ApplicationType.Android))
				game.getActionResolver().loadInterstital();

			loadStats();

			state = PLAY;
			galaxySelected = game.getMMS().galaxySelected;
			cursor = null;
			clicked = false;

			this.first = true;
			this.firstBall = true;
			this.firstBallHelper = true;
			this.firstDeath = true;
			this.canTouch = false;
			this.updatedDB = false;
			upRed = false;
			upBlue = false;
			upBoth = false;
			changeBothScore = false;
			changeRedScore = false;
			changeBlueScore = false;
			canTweenRedScore = true;
			canTweenBlueScore = true;
			canIncreaseSpeed = true;
			drawLife = false;
			java.util.Arrays.fill(popA, 0f);
			
			redToBlue = false;
			blueToRed = false;
			blueX2 = false;
			redX2 = false;
			slow = false;
			blueShield = false;
			redShield = false;
			blueX3 = false;
			redX3 = false;
			goldShield = false;
			objectClockEnable = false;
			canPlayRetreat = false;

			// load galaxy
			loadBgs(galaxySelected);

			// set starting velocity
			director.reset();
			speedFx.reset();
			chargeSoundStarted = false;
			velocityBg = 350;
			velocityBall = 350;

			// set ball spawn frequency
			timeBetweenBalls = 350000000;
			time = TimeUtils.nanoTime();

			// set score counters
			blues = 0;
			reds = 0;
			lifes = 0;
			lifesCounter = 0;
			objCounter = 0;

			// set collecting values
			bluesValue = 1;
			redsValue = 1;

			//set ship stuff
			ship.setLifes(game.getMMS().energyStart);
			if(energyBar != null) energyBar.snap(game.getMMS().energyStart/(float)barWidth);
			ship.shipSelected = game.getMMS().shipSelected;
			ship.setShipScale();
			deathAngle = MathUtils.random(0, 1);
			deathAngle2 = (int) MathUtils.random(-fxy(64), Gdx.graphics.getWidth() + fxy(64));

			//get best score
			try{
				cursor = game.getDataBase().rawQuery("SELECT highScore FROM seleccionsAndNumbers");
				if(cursor.next()) 
					bestScore = Integer.valueOf(cursor.getInt(0));
			}catch (SQLiteGdxException e) {
				e.printStackTrace();
			}

			barWidth = game.getMMS().energyMeter;

			// load life bar
			switch(barWidth){
			case 3:
				lifeBarSprite = new Sprite(game.assetManager.get("res/hud/lifeBar3Lifes.png", Texture.class));
				lifeTextures = TextureRegion.split(game.assetManager.get("res/hud/lifeEnergy3Lifes.png", Texture.class), 104, 36)[0];
				break;
			case 4:
				lifeBarSprite = new Sprite(game.assetManager.get("res/hud/lifeBar4Lifes.png", Texture.class));
				lifeTextures = TextureRegion.split(game.assetManager.get("res/hud/lifeEnergy4Lifes.png", Texture.class), 124, 36)[0];
				break;
			case 5:
				lifeBarSprite = new Sprite(game.assetManager.get("res/hud/lifeBar5Lifes.png", Texture.class));
				lifeTextures = TextureRegion.split(game.assetManager.get("res/hud/lifeEnergy5Lifes.png", Texture.class), 145, 36)[0];
				break;

			}
			lifeSprites = new Sprite[lifeTextures.length];
			for(int i = 0; i < lifeTextures.length; i++){
				lifeSprites[i] = new Sprite(lifeTextures[i]);
			}

			lifeBarSprite.setSize(fx(1.75f*lifeBarSprite.getWidth()), fy(1.75f*lifeBarSprite.getHeight()));
			lifeBarSprite.setPosition(Gdx.graphics.getWidth()/2 - lifeBarSprite.getWidth()/2, Gdx.graphics.getHeight()/15 + adHeight/2);
			for(int k = 0; k < lifeSprites.length; k++){
				lifeSprites[k].setSize(fx(1.75f*lifeSprites[k].getWidth()), 1.75f*fy(lifeSprites[k].getHeight()));
				lifeSprites[k].setPosition(Gdx.graphics.getWidth()/2 - lifeSprites[k].getWidth()/2, Gdx.graphics.getHeight()/15 + adHeight/2);
			}
			energyBar = new com.spaceship.handlers.EnergyBar(game.assetManager, barWidth);
			lastTimeBg = TimeUtils.nanoTime();
			currentBgY = (bgs1.getHeight() + bgs2.getHeight())/3;

			// set ball's velocity increase time
			lastVelocityIncrease = TimeUtils.nanoTime();
			ballSpawnSpeed = 1;

			// set particles counter
			redP = 0;
			blueP = 0;
			lifeP = 0;
			blueW = 0;
			redW = 0;

			//set pause buttons
			bHome2.setColor(1f, 1f, 1f, 1f);
			bRetry2.setColor(1f, 1f, 1f, 1f);
			bExit2.setColor(1f, 1f, 1f, 1f);
			bVibrationOn.setColor(1f, 1f, 1f, 1f);
			bVibrationOff.setColor(1f, 1f, 1f, 1f);
			bControlsOn.setColor(1f, 1f, 1f, 1f);
			bControlsOff.setColor(1f, 1f, 1f, 1f);
			bSoundOff.setColor(1f, 1f, 1f, 1f);
			bSoundOn.setColor(1f, 1f, 1f, 1f);
			bHelp.setColor(1f, 1f, 1f, 1f);

			//set font height
			redFont3Height = Gdx.graphics.getHeight()/8 + 1.2f*com.spaceship.handlers.FontUtil.bounds(bluesFont2, String.valueOf(game.getMMS().redBalls)).height + 3*adHeight/4;
			blueFont3Height = Gdx.graphics.getHeight()/8 + 1.2f*com.spaceship.handlers.FontUtil.bounds(bluesFont2, String.valueOf(game.getMMS().blueBalls)).height + 3*adHeight/4;

			// set particles and wrongs color
			for(int h = 0; h < 20; h++){
				redParticles[h].setColor(1f, 1f, 1f, 0f);
				blueParticles[h].setColor(1f, 1f, 1f, 0f);
				lifeParticles[h].setColor(1f, 1f, 1f, 0f);
				objectsParticles[h].setColor(1f, 1f, 1f, 0f);
			}
			for(int h = 0; h < 5; h++){
				redWrong[h].setColor(1f, 1f, 1f, 0f);
				blueWrong[h].setColor(1f, 1f, 1f, 0f);
			}

			bHome.setPosition(Gdx.graphics.getWidth()/2 - bHome.getWidth()/2, Gdx.graphics.getHeight() + 3*adHeight/4);
			bRetry.setPosition(Gdx.graphics.getWidth()/2 - bRetry.getWidth()/2, Gdx.graphics.getHeight() + 3*adHeight/4);
			bShare.setPosition(Gdx.graphics.getWidth()/2 - bShare.getWidth()/2, Gdx.graphics.getHeight() + 3*adHeight/4);
			bExit.setPosition(Gdx.graphics.getWidth()/2 - bExit.getWidth()/2, Gdx.graphics.getHeight() + 3*adHeight/4);
			bPause.setPosition(Gdx.graphics.getWidth() - bPause.getWidth()*1.5f, Gdx.graphics.getHeight() - bPause.getHeight()*1.5f);
			bPauseCant.setPosition(Gdx.graphics.getWidth() - bPauseCant.getWidth()*1.5f, Gdx.graphics.getHeight() - bPauseCant.getHeight()*1.5f);
			bPlay.setPosition(Gdx.graphics.getWidth() - bPlay.getWidth()*1.5f, Gdx.graphics.getHeight() - bPlay.getHeight()*1.5f);
			bHome2.setPosition(Gdx.graphics.getWidth() + bHome2.getWidth()*2, Gdx.graphics.getHeight()/2 - bHome2.getHeight()/2 + 3*adHeight/4);
			bRetry2.setPosition(bHome2.getX(), bHome2.getY());
			bExit2.setPosition(bHome2.getX(), bHome2.getY());
			bVibrationOn.setPosition(bHome2.getX(), bHome2.getY());
			bVibrationOff.setPosition(bHome2.getX(), bHome2.getY());
			bControlsOn.setPosition(bHome2.getX(), bHome2.getY());
			bControlsOff.setPosition(bHome2.getX(), bHome2.getY());
			bSoundOff.setPosition(bHome2.getX(), bHome2.getY());
			bSoundOn.setPosition(bHome2.getX(), bHome2.getY());
			bHelp.setPosition(bHome2.getX(), bHome2.getY());
			arrowLeft.setPosition(-arrowLeft.getWidth(), Gdx.graphics.getHeight()/15 + adHeight/2);
			arrowRight.setPosition(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()/15 + adHeight/2);
			padVerticalLeft.setPosition(0, (Gdx.graphics.getHeight()/2  - padVerticalLeft.getHeight() - com.spaceship.handlers.FontUtil.bounds(redsFont, "0").height/2)/4 + adHeight);
			padVerticalRight.setPosition(Gdx.graphics.getWidth() - arrowRight.getWidth() - 2*Gdx.graphics.getWidth()/15, (Gdx.graphics.getHeight()/2  - padVerticalLeft.getHeight() - com.spaceship.handlers.FontUtil.bounds(redsFont, "0").height/2)/4 + adHeight);
			padHorizontalDown.setPosition(padVerticalLeft.getX() + padVerticalLeft.getWidth() + (Gdx.graphics.getWidth() - 2*padVerticalLeft.getWidth() - padHorizontalDown.getWidth())/2, 
					(Gdx.graphics.getHeight()/2  - padVerticalLeft.getHeight() - com.spaceship.handlers.FontUtil.bounds(redsFont, "0").height/2)/4 + adHeight);
			padHorizontalUp.setPosition(padVerticalLeft.getX() + padVerticalLeft.getWidth() + (Gdx.graphics.getWidth() - 2*padVerticalLeft.getWidth() - padHorizontalUp.getWidth())/2, 
					padHorizontalDown.getY() + padHorizontalDown.getHeight() + (padVerticalLeft.getHeight() - 2*padHorizontalUp.getHeight())/2);
			padVerticalLeftEsp.setPosition(padVerticalLeft.getX(), padVerticalLeft.getY());
			padVerticalRightEsp.setPosition(padVerticalRight.getX(), padVerticalRight.getY());
			padHorizontalDownEsp.setPosition(padHorizontalDown.getX(), padHorizontalDown.getY());
			padHorizontalUpEsp.setPosition(padHorizontalUp.getX(), padHorizontalUp.getY());
			for(int i = 0; i < objectsSprites1.length; i++){
				objectsSprites1[i].setColor(1f, 1f, 1f, 0f);
				objectsSprites2[i].setColor(1f, 1f, 1f, 0f);
				objectsSprites3[i].setColor(1f, 1f, 1f, 0f);
			}

			//tween play/pause buttons, fonts, background life bar and ship shield
			Timeline.createSequence().
				push(Tween.set(bPause, SpriteAccessor.COLOR).target(1f, 1f, 1f, 0.8f)).
				push(Tween.set(bPauseCant, SpriteAccessor.COLOR).target(1f, 1f, 1f, 0.8f)).
				push(Tween.set(bPlay, SpriteAccessor.COLOR).target(1f, 1f, 1f, 0.8f)).
				push(Tween.set(bluesFont, BitmapFontAccessor.COLOR).target(1f, 1f, 1f, 0f)).
				push(Tween.set(redsFont, BitmapFontAccessor.COLOR).target(1f, 1f, 1f, 0f)).
				push(Tween.set(lifesFont, BitmapFontAccessor.COLOR).target(1f, 1f, 1f, 1f)).
				push(Tween.set(greysFont, BitmapFontAccessor.COLOR).target(1f, 1f, 1f, 0f)).
				push(Tween.set(redsFont, BitmapFontAccessor.COLOR).target(1f, 1f, 1f, 1f)).
				push(Tween.set(greysFont2, BitmapFontAccessor.COLOR).target(1f, 1f, 1f, 0f)).
				push(Tween.set(redsFont2, BitmapFontAccessor.COLOR).target(1f, 1f, 1f, 0f)).
				push(Tween.set(bluesFont2, BitmapFontAccessor.COLOR).target(1f, 1f, 1f, 0f)).
				push(Tween.set(redsFont3, BitmapFontAccessor.COLOR).target(1f, 1f, 1f, 0f)).
				push(Tween.set(bluesFont3, BitmapFontAccessor.COLOR).target(1f, 1f, 1f, 0f)).
				push(Tween.set(blueCounter, SpriteAccessor.COLOR).target(1f, 1f, 1f, 0f)).
				push(Tween.set(redCounter, SpriteAccessor.COLOR).target(1f, 1f, 1f, 0f)).
				push(Tween.set(bgs1, SpriteAccessor.COLOR).target(1f, 1f, 1f, 0f)).
				push(Tween.set(bgs2, SpriteAccessor.COLOR).target(1f, 1f, 1f, 0f)).
				push(Tween.set(lifeSprites[ship.getLifes()], SpriteAccessor.COLOR).target(1f, 1f, 1f, 0f)).
				push(Tween.set(lifeBarSprite, SpriteAccessor.COLOR).target(1f, 1f, 1f, 0f)).
				beginParallel().
					push(Tween.to(bHome, SpriteAccessor.COLOR, 0.75f).target(1f, 1f, 1f, 1f)).
					push(Tween.to(bRetry, SpriteAccessor.COLOR, 0.75f).target(1f, 1f, 1f, 1f)).
					push(Tween.to(bShare, SpriteAccessor.COLOR, 0.75f).target(1f, 1f, 1f, 1f)).
					push(Tween.to(bExit, SpriteAccessor.COLOR, 0.75f).target(1f, 1f, 1f, 1f)).
					push(Tween.to(bluesFont, BitmapFontAccessor.COLOR, 0.75f).target(1f, 1f, 1f, 1f)).
					push(Tween.to(redsFont, BitmapFontAccessor.COLOR, 0.75f).target(1f, 1f, 1f, 1f)).
					push(Tween.to(bgs1, SpriteAccessor.COLOR, 0.75f).target(1f, 1f, 1f, 1f)).
					push(Tween.to(bgs2, SpriteAccessor.COLOR, 0.75f).target(1f, 1f, 1f, 1f)).
					push(Tween.to(lifeSprites[ship.getLifes()], SpriteAccessor.COLOR, 0.75f).target(1f, 1f, 1f, 1f)).
					push(Tween.to(lifeBarSprite, SpriteAccessor.COLOR, 0.75f).target(1f, 1f, 1f, 1f)).
					push(Tween.to(arrowLeft, SpriteAccessor.POS_XY, 0.75f).target(Gdx.graphics.getWidth()/15, arrowRight.getHeight() * 1.5f + adHeight/2)).
					push(Tween.to(arrowRight, SpriteAccessor.POS_XY, 0.75f).target(Gdx.graphics.getWidth() - arrowRight.getWidth() - Gdx.graphics.getWidth()/15, arrowRight.getHeight() * 1.5f + adHeight/2)).
				end().
			start(tweenManager);
			Tween.call(lifeMeter1Callback).start(tweenManager);
			ship.effects();

			for(int j = 0; j < balls.length; j ++){
				balls[j].setPosition(MathUtils.random((fx(20) + fy(20))/2, Gdx.graphics.getWidth() - (fx(20) + fy(20))/2),
						Gdx.graphics.getHeight() + fxy(20));
				balls[j].taken = false;
				balls[j].setVelocity(velocityBall);
			}

			ballToAdd = 0;

			ship.startTrace = true;

			//set objects
			if(game.getMMS().objectStart >= 1){
				object1 = randomObject();
				objectsSprites1[object1].setColor(1f, 1f, 1f, 1f); 
			}else
				object1 = -1;
			if(game.getMMS().objectStart >= 2){
				object2 = randomObject();
				objectsSprites2[object2].setColor(1f, 1f, 1f, 1f); 
			}else
				object2 = -1;
			if(game.getMMS().objectStart == 3){
				object3 = randomObject();
				objectsSprites3[object3].setColor(1f, 1f, 1f, 1f); 
			}else
				object3 = -1;
			ring1.setColor(1f, 1f, 1f, 1f);
			ring2.setColor(1f, 1f, 1f, 1f);
			ring3.setColor(1f, 1f, 1f, 1f);
			
			setUpHelpMenu();
			stageHelp.addAction(moveTo(0, stageHelp.getHeight()));	
		}	
	}

	@Override
	public void render(float delta){

		if(firstMMS){
			firstMMS = false;
			game.setScreen(game.getMMS());

			Gdx.gl.glClearColor(0, 0, 0, 0);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			camera.update();
			game.batch.setProjectionMatrix(camera.combined);


		}else{

			Gdx.gl.glClearColor(0, 0, 0, 0);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

			tweenManager.update(delta);

			// tell the camera to update its matrices.
			camera.update();
			// tell the SpriteBatch to render in the coordinate system specified by the camera.
			game.batch.setProjectionMatrix(camera.combined);

			//tween death
			if(state == DEAD){
				if(firstDeath){
					int ad = MathUtils.random(0, 1);
					if(Gdx.app.getType().equals(ApplicationType.Android) && ad == 0){
						Timer.schedule(new Task(){
							@Override
							public void run(){
								if(game.getActionResolver().isGifitzAvailable())
									game.getActionResolver().loadAdBuddiz();
//								else
								
//									game.getActionResolver().showInterstital();
							}
						}, 1.70f);
					}	

					Timer.schedule(new Task(){
						@Override
						public void run(){
							canTouch = true;
						}
					}, 2f);

					dBalls.clear();
					tweenEnd = true;

					if(reds > blues){ 
						finalScore = blues;
						Timer.schedule(new Task(){
							@Override
							public void run(){
								upBlue = true;
								changeBlueScore = true;
								totalBlues = totalBlues + blues;
								if(blues % 2 == 0){
									totalReds = totalReds + blues/2;
									totalOrbs = totalOrbs + reds + blues/2 + lifesCounter + objCounter;
								}else{
									totalReds = totalReds + (blues - 1)/2;
									totalOrbs = totalOrbs + reds + (blues - 1)/2 + lifesCounter + objCounter;
								}
							}
						}, 1.46f);
					}else if(blues > reds){ 
						finalScore = reds;
						Timer.schedule(new Task(){
							@Override
							public void run(){
								upRed = true;
								changeRedScore = true;
								totalReds = totalReds + reds;
								if(reds % 2 == 0){
									totalBlues = totalBlues + reds/2;
									totalOrbs = totalOrbs + reds + reds/2 + lifesCounter + objCounter;
								}else{
									totalBlues = totalBlues + (reds - 1)/2;
									totalOrbs = totalOrbs + reds + (reds - 1)/2 + lifesCounter + objCounter;
								}
							}
						}, 1.46f);
					}else{
						finalScore = reds;
						Timer.schedule(new Task(){
							@Override
							public void run(){
								upBoth = true;
								changeBothScore = true;
								totalBlues = totalBlues + blues;
								totalReds = totalReds + reds;
								totalOrbs = totalOrbs + reds + blues + lifesCounter + objCounter;
							}
						}, 1.46f);
					}
					if(finalScore > bestScore){
						bestScore = finalScore;
						Tween.to(greysFont, BitmapFontAccessor.COLOR, 0.2f).target(1f, 1f, 1f, 0.5f).delay(0.30f).start(tweenManager);
						Tween.call(newHighScoreCallback).delay(0.40f).start(tweenManager);
					}

					games++;
					totalPoints = totalPoints + finalScore;
					avgPoints = totalPoints/games;
					totalLifes = totalLifes + lifesCounter;
					totalObjects = totalObjects + objCounter;

					if(Gdx.app.getType().equals(ApplicationType.Android) && finalScore >= 15)
							game.getActionResolver().giftizMission();
					
					if(Gdx.app.getType().equals(ApplicationType.Android) && game.getActionResolver().getSignedIn()){
						game.getActionResolver().submitScore(finalScore);
						if (finalScore >= 25) game.getActionResolver().unlockAchievement("CgkIrLf9wZgEEAIQCQ");
						if (finalScore >= 50) game.getActionResolver().unlockAchievement("CgkIrLf9wZgEEAIQCg");
						if (finalScore >= 75) game.getActionResolver().unlockAchievement("CgkIrLf9wZgEEAIQCw");
						if (finalScore >= 100) game.getActionResolver().unlockAchievement("CgkIrLf9wZgEEAIQDA");
						if (finalScore >= 200) game.getActionResolver().unlockAchievement("CgkIrLf9wZgEEAIQDQ");
						if (finalScore >= 300) game.getActionResolver().unlockAchievement("CgkIrLf9wZgEEAIQDg");
						if (games >= 1) game.getActionResolver().unlockAchievement("CgkIrLf9wZgEEAIQEQ");
						if (games >= 10) game.getActionResolver().unlockAchievement("CgkIrLf9wZgEEAIQEg");
						if (games >= 25) game.getActionResolver().unlockAchievement("CgkIrLf9wZgEEAIQEw");
						if (games >= 50) game.getActionResolver().unlockAchievement("CgkIrLf9wZgEEAIQFA");
						if (games >= 100) game.getActionResolver().unlockAchievement("CgkIrLf9wZgEEAIQFQ");
						if (games >= 500) game.getActionResolver().unlockAchievement("CgkIrLf9wZgEEAIQFg");
					}

					// tween death arrows, bestScore and life bar
					Tween.to(arrowLeft, SpriteAccessor.POS_XY, 0.75f).target(arrowLeft.getX() - Gdx.graphics.getWidth(), arrowRight.getHeight() * 1.5f + adHeight/2).start(tweenManager);
					Tween.to(arrowRight, SpriteAccessor.POS_XY, 0.75f).target(arrowRight.getX() + Gdx.graphics.getWidth(), arrowRight.getHeight() * 1.5f + adHeight/2).start(tweenManager);
					Tween.to(lifeBarSprite, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f).start(tweenManager);
					Tween.to(greysFont2, BitmapFontAccessor.COLOR, 0.2f).target(1f, 1f, 1f, 1f).delay(1.45f).start(tweenManager);
					Tween.to(redsFont2, BitmapFontAccessor.COLOR, 0.2f).target(1f, 1f, 1f, 1f).delay(1.45f).start(tweenManager);
					Tween.set(redsFont3, BitmapFontAccessor.COLOR).target(1f, 1f, 1f, 0.75f).delay(1.45f).start(tweenManager);
					Tween.set(bluesFont3, BitmapFontAccessor.COLOR).target(1f, 1f, 1f, 0.75f).delay(1.45f).start(tweenManager);
					Tween.to(bluesFont2, BitmapFontAccessor.COLOR, 0.2f).target(1f, 1f, 1f, 1f).delay(1.45f).start(tweenManager);
					Tween.to(blueCounter, SpriteAccessor.COLOR, 0.2f).target(1f, 1f, 1f, 1f).delay(1.45f).start(tweenManager);
					Tween.to(redCounter, SpriteAccessor.COLOR, 0.2f).target(1f, 1f, 1f, 1f).delay(1.45f).start(tweenManager);
					// tween death buttons
					Tween.to(bExit, SpriteAccessor.POS_XY, 1.15f).target(bExit.getX(), Gdx.graphics.getHeight()/2 - 2.5f*bExit.getHeight() + 3*adHeight/4).start(tweenManager);
					Tween.to(bRetry, SpriteAccessor.POS_XY, 1.15f).target(bRetry.getX(), Gdx.graphics.getHeight()/2 - 1.25f*bRetry.getHeight() + 3*adHeight/4).delay(0.25f).start(tweenManager);
					Tween.to(bShare, SpriteAccessor.POS_XY, 1.15f).target(bShare.getX(), Gdx.graphics.getHeight()/2 + 0f*bShare.getHeight() + 3*adHeight/4).delay(0.5f).start(tweenManager);
					Tween.to(bHome, SpriteAccessor.POS_XY, 1.15f).target(bHome.getX(), Gdx.graphics.getHeight()/2 + 1.25f*bRetry.getHeight() + 3*adHeight/4).delay(0.75f).start(tweenManager);
					// tween death counters
					if(reds < blues) 
						Tween.call(winningRedCounterCallback).start(tweenManager);
					else if(blues < reds) 
						Tween.call(winningBlueCounterCallback).start(tweenManager);
					else{
						Tween.call(winningRedCounterCallback).start(tweenManager);
						Tween.call(winningBlueCounterCallback).start(tweenManager);
					}
					//Tween death objects
					Tween.to(ring1, SpriteAccessor.COLOR, 0.2f).target(1f, 1f, 1f, 0f).start(tweenManager);
					Tween.to(ring2, SpriteAccessor.COLOR, 0.2f).target(1f, 1f, 1f, 0f).start(tweenManager);
					Tween.to(ring3, SpriteAccessor.COLOR, 0.2f).target(1f, 1f, 1f, 0f).start(tweenManager);
					if(redToBlue || blueToRed || blueX2 || redX2 || slow || blueShield || redShield || blueX3 || redX3 || goldShield)
						Tween.to(objectsSprites1[auxObj], SpriteAccessor.COLOR, 0.2f).target(1f, 1f, 1f, 0f).start(tweenManager);
					else if(object1 != -1)
						Tween.to(objectsSprites1[object1], SpriteAccessor.COLOR, 0.2f).target(1f, 1f, 1f, 0f).start(tweenManager);
					if(object2 != -1)
						Tween.to(objectsSprites2[object2], SpriteAccessor.COLOR, 0.2f).target(1f, 1f, 1f, 0f).start(tweenManager);
					if(object3 != -1)
						Tween.to(objectsSprites3[object3], SpriteAccessor.COLOR, 0.2f).target(1f, 1f, 1f, 0f).start(tweenManager);

					firstDeath = false;
				}
				ship.effectsDeath();

				if(Gdx.input.justTouched() && canTouch){
					if(Gdx.input.getX() > bHome.getX() && Gdx.input.getX() < bHome.getX() + bHome.getWidth() && 
							Gdx.graphics.getHeight() - Gdx.input.getY() > bHome.getY() && Gdx.graphics.getHeight() - 
							Gdx.input.getY() < bHome.getY() + bHome.getHeight() && !clicked){
						//home	
						clicked = true;
						if(game.getMMS().sound == game.getMMS().ON)
							buttonLarge.play(0.75f);
						tweenEnd = false;
						Timeline.createSequence().
						push(Tween.to(bHome, SpriteAccessor.COLOR, 0.10f).target(0.25f, 0.25f, 0.25f, 1f).repeatYoyo(1, 0)).
						beginParallel().
						push(Tween.to(bgs1, SpriteAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f).delay(0.30f).ease(TweenEquations.easeNone)).
						push(Tween.to(bgs2, SpriteAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f).delay(0.30f).ease(TweenEquations.easeNone)).
						push(Tween.to(bHome, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f)).
						push(Tween.to(bRetry, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f)).
						push(Tween.to(bShare, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f)).
						push(Tween.to(bExit, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f)).
						push(Tween.to(redsFont, BitmapFontAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f)).
						push(Tween.to(bluesFont, BitmapFontAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f)).
						push(Tween.to(greysFont, BitmapFontAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f)).
						push(Tween.to(greysFont2, BitmapFontAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f)).
						push(Tween.to(redsFont2, BitmapFontAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f)).
						push(Tween.to(bluesFont2, BitmapFontAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f)).
						push(Tween.to(blueCounter, SpriteAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f)).
						push(Tween.to(redCounter, SpriteAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f)).
						end().
						start(tweenManager);

						Timer.schedule(new Task(){
							@Override
							public void run(){
								if(!updatedDB){
									updateDatabase();
									updateTime();
									updatedDB = true;
								}
								game.setScreen(game.getMMS());
							}
						}, 1.45f);
						dispose();

					}else if(Gdx.input.getX() > bRetry.getX() && Gdx.input.getX() < bRetry.getX() + bRetry.getWidth() && 
							Gdx.graphics.getHeight() - Gdx.input.getY() > bRetry.getY() && Gdx.graphics.getHeight() - 
							Gdx.input.getY() < bRetry.getY() + bRetry.getHeight() && !clicked){
						//Retry
						clicked = true;
						if(game.getMMS().sound == game.getMMS().ON)
							buttonLarge.play(0.75f);
						tweenEnd = false;
						Timeline.createSequence().
						push(Tween.to(bRetry, SpriteAccessor.COLOR, 0.10f).target(0.25f, 0.25f, 0.25f, 1f).repeatYoyo(1, 0)).
						beginParallel().
						push(Tween.to(bgs1, SpriteAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f).delay(0.30f)).
						push(Tween.to(bgs2, SpriteAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f).delay(0.30f)).
						push(Tween.to(bHome, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f)).
						push(Tween.to(bRetry, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f)).
						push(Tween.to(bShare, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f)).
						push(Tween.to(bExit, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f)).
						push(Tween.to(redsFont, BitmapFontAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f)).
						push(Tween.to(bluesFont, BitmapFontAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f)).
						push(Tween.to(greysFont, BitmapFontAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f)).
						push(Tween.to(greysFont2, BitmapFontAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f)).
						push(Tween.to(redsFont2, BitmapFontAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f)).
						push(Tween.to(bluesFont2, BitmapFontAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f)).
						push(Tween.to(blueCounter, SpriteAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f)).
						push(Tween.to(redCounter, SpriteAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f)).
						end().
						start(tweenManager);

						Timer.schedule(new Task(){
							@Override
							public void run(){
								if(!updatedDB){
									updateDatabase();
									updateTime();
									updatedDB = true;
								}
								if(blues > reds){
									game.getMMS().redBalls = game.getMMS().redBalls + reds; 
									if(reds % 2 == 0)
										game.getMMS().blueBalls = game.getMMS().blueBalls + reds/2;
									else
										game.getMMS().blueBalls = game.getMMS().blueBalls + (reds - 1)/2;
								}else if(reds > blues){
									game.getMMS().blueBalls = game.getMMS().blueBalls + blues; 
									if(blues % 2 == 0)
										game.getMMS().redBalls = game.getMMS().redBalls + blues/2;
									else
										game.getMMS().redBalls = game.getMMS().redBalls + (blues - 1)/2;
								}else{
									game.getMMS().redBalls = game.getMMS().redBalls + reds; 
									game.getMMS().blueBalls = game.getMMS().blueBalls + blues; 
								}
								game.setScreen(game.getPS());
							}
						}, 1.45f);
						dispose();
					}else if(Gdx.input.getX() > bShare.getX() && Gdx.input.getX() < bShare.getX() + bShare.getWidth() && 
							Gdx.graphics.getHeight() - Gdx.input.getY() > bShare.getY() && Gdx.graphics.getHeight() - 
							Gdx.input.getY() < bShare.getY() + bShare.getHeight() && !clicked){
						//Share
						if(game.getMMS().sound == game.getMMS().ON)
							buttonLarge.play(0.75f);
						String msg;
						if(game.getMMS().language == game.getMMS().ON)
							msg = "Check it out! I scored " + finalScore +" points in Collect The Orbs! \n https://play.google.com/store/apps/details?id=com.spaceship.main.android";
						else
							msg = "Mira! Conseguí " + finalScore +" puntos en Collect The Orbs! \n https://play.google.com/store/apps/details?id=com.spaceship.main.android";
						game.getActionResolver().share(msg);
					}else if(Gdx.input.getX() > bExit.getX() && Gdx.input.getX() < bExit.getX() + bExit.getWidth() && 
							Gdx.graphics.getHeight() - Gdx.input.getY() > bExit.getY() && Gdx.graphics.getHeight() - 
							Gdx.input.getY() < bExit.getY() + bExit.getHeight() && !clicked){
						//Exit
						if(game.getMMS().sound == game.getMMS().ON)
							buttonLarge.play(0.75f);
						state = DEADLEAVING;
					}	
				}	
			}

			if(Gdx.input.justTouched() && state == PAUSE){
				if(Gdx.input.getX() > bExit2.getX() && Gdx.input.getX() < bExit2.getX() + bExit2.getWidth() && 
						Gdx.graphics.getHeight() - Gdx.input.getY() > bExit2.getY() && Gdx.graphics.getHeight() - 
						Gdx.input.getY() < bExit2.getY() + bExit2.getHeight() && !clicked){
					//exit2
					if(game.getMMS().sound == game.getMMS().ON)
						buttonLarge.play(0.75f);
					state = PAUSELEAVING;
				}else if(Gdx.input.getX() > bRetry2.getX() && Gdx.input.getX() < bRetry2.getX() + bRetry2.getWidth() && 
						Gdx.graphics.getHeight() - Gdx.input.getY() > bRetry2.getY() && Gdx.graphics.getHeight() - 
						Gdx.input.getY() < bRetry2.getY() + bRetry2.getHeight() && !clicked){
					//retry2
					dBalls.clear();
					clicked = true;
					if(game.getMMS().sound == game.getMMS().ON)
						buttonLarge.play(0.75f);
					Timeline.createSequence().
					push(Tween.to(bRetry2, SpriteAccessor.COLOR, 0.10f).target(0.25f, 0.25f, 0.25f, 1f).repeatYoyo(1, 0)).
					beginParallel().
					push(Tween.to(bgs1, SpriteAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f).delay(0.30f)).
					push(Tween.to(bgs2, SpriteAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f).delay(0.30f)).
					push(Tween.to(bHome2, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f)).
					push(Tween.to(bRetry2, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f)).
					push(Tween.to(bExit2, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f)).
					push(Tween.to(bVibrationOn, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f)).
					push(Tween.to(bVibrationOff, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f)).
					push(Tween.to(bControlsOn, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f)).
					push(Tween.to(bControlsOff, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f)).
					push(Tween.to(bSoundOn, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f)).
					push(Tween.to(bSoundOff, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f)).
					push(Tween.to(bHelp, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f)).
					push(Tween.to(redsFont, BitmapFontAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f)).
					push(Tween.to(bluesFont, BitmapFontAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f)).
					push(Tween.to(greysFont, BitmapFontAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f)).
					push(Tween.to(greysFont2, BitmapFontAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f)).
					push(Tween.to(redsFont2, BitmapFontAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f)).
					push(Tween.to(bluesFont2, BitmapFontAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f)).
					push(Tween.to(blueCounter, SpriteAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f)).
					push(Tween.to(redCounter, SpriteAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f)).
					end().
					start(tweenManager);
					Timer.schedule(new Task(){
						@Override
						public void run(){
							updateTime();
							game.setScreen(game.getPS());
						}
					}, 0.60f);
				}else if(Gdx.input.getX() > bHome2.getX() && Gdx.input.getX() < bHome2.getX() + bHome2.getWidth() && 
						Gdx.graphics.getHeight() - Gdx.input.getY() > bHome2.getY() && Gdx.graphics.getHeight() - 
						Gdx.input.getY() < bHome2.getY() + bHome2.getHeight() && !clicked){
					//home2
					clicked = true;
					dBalls.clear();
					if(game.getMMS().sound == game.getMMS().ON)
						buttonLarge.play(0.75f);
					Timeline.createSequence().
					push(Tween.to(bHome2, SpriteAccessor.COLOR, 0.10f).target(0.25f, 0.25f, 0.25f, 1f).repeatYoyo(1, 0)).
					beginParallel().
					push(Tween.to(bgs1, SpriteAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f).delay(0.30f)).
					push(Tween.to(bgs2, SpriteAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f).delay(0.30f)).
					push(Tween.to(bHome2, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f)).
					push(Tween.to(bRetry2, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f)).
					push(Tween.to(bExit2, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f)).
					push(Tween.to(bVibrationOn, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f)).
					push(Tween.to(bVibrationOff, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f)).
					push(Tween.to(bControlsOn, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f)).
					push(Tween.to(bControlsOff, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f)).
					push(Tween.to(bSoundOn, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f)).
					push(Tween.to(bSoundOff, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f)).
					push(Tween.to(bHelp, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f)).
					push(Tween.to(redsFont, BitmapFontAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f)).
					push(Tween.to(bluesFont, BitmapFontAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f)).
					push(Tween.to(greysFont, BitmapFontAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f)).
					push(Tween.to(greysFont2, BitmapFontAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f)).
					push(Tween.to(redsFont2, BitmapFontAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f)).
					push(Tween.to(bluesFont2, BitmapFontAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f)).
					push(Tween.to(blueCounter, SpriteAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f)).
					push(Tween.to(redCounter, SpriteAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f)).
					end().
					start(tweenManager);

					Timer.schedule(new Task(){
						@Override
						public void run(){
							updateTime();
							game.setScreen(game.getMMS());
						}
					}, 0.60f);
				}else if(Gdx.input.getX() > bSoundOn.getX() && Gdx.input.getX() < bSoundOn.getX() + bSoundOn.getWidth() && 
						Gdx.graphics.getHeight() - Gdx.input.getY() > bSoundOn.getY() && Gdx.graphics.getHeight() - 
						Gdx.input.getY() < bSoundOn.getY() + bSoundOn.getHeight()){
					//Sound
					if(game.getMMS().sound == game.getMMS().ON)
						game.getMMS().sound = game.getMMS().OFF;
					else if(game.getMMS().sound == game.getMMS().OFF){
						game.getMMS().buttonSmall.play();
						game.getMMS().sound = game.getMMS().ON;
					}
					game.getMMS().updateMusicVolume();
				}else if(Gdx.input.getX() > bVibrationOn.getX() && Gdx.input.getX() < bVibrationOn.getX() + bVibrationOn.getWidth() && 
						Gdx.graphics.getHeight() - Gdx.input.getY() > bVibrationOn.getY() && Gdx.graphics.getHeight() - 
						Gdx.input.getY() < bVibrationOn.getY() + bVibrationOn.getHeight()){
					//Vibration
					if(game.getMMS().vibration == game.getMMS().ON){
						if(game.getMMS().sound == game.getMMS().ON)
							game.getMMS().buttonSmall.play();
						game.getMMS().vibration = game.getMMS().OFF;
					}else if(game.getMMS().vibration == game.getMMS().OFF){
						if(game.getMMS().sound == game.getMMS().ON)
							game.getMMS().buttonSmall.play();
						Gdx.input.vibrate(75);
						game.getMMS().vibration = game.getMMS().ON;
					}
				}else if(Gdx.input.getX() > bControlsOn.getX() && Gdx.input.getX() < bControlsOn.getX() + bControlsOn.getWidth() && 
						Gdx.graphics.getHeight() - Gdx.input.getY() > bControlsOn.getY() && Gdx.graphics.getHeight() - 
						Gdx.input.getY() < bControlsOn.getY() + bControlsOn.getHeight()){
					//Controls
					if(game.getMMS().controls == game.getMMS().ON){
						if(game.getMMS().sound == game.getMMS().ON)
							game.getMMS().buttonSmall.play();
						game.getMMS().controls = game.getMMS().OFF;
					}else if(game.getMMS().controls == game.getMMS().OFF){
						if(game.getMMS().sound == game.getMMS().ON)
							game.getMMS().buttonSmall.play();
						game.getMMS().controls = game.getMMS().ON;
					}
				}else if(Gdx.input.getX() > bHelp.getX() && Gdx.input.getX() < bHelp.getX() + bHelp.getWidth() && 
						Gdx.graphics.getHeight() - Gdx.input.getY() > bHelp.getY() && Gdx.graphics.getHeight() - 
						Gdx.input.getY() < bHelp.getY() + bHelp.getHeight()){
					//Help
					if(game.getMMS().sound == game.getMMS().ON)
						game.getMMS().buttonSmall.play();
					Timer.schedule(new Task(){
							@Override
							public void run(){
								state = HELP;;
							}
						}, 0.1f);
					stageHelp.addAction(moveTo(0, 0, 0.5f));
					Gdx.input.setInputProcessor(stageHelp);
					
				}
			}

			if(Gdx.input.justTouched()){
				if(Gdx.input.getX() > bYes.getX() && Gdx.input.getX() < bYes.getX() + bYes.getWidth() &&
						Gdx.graphics.getHeight() - Gdx.input.getY() > bYes.getY() && Gdx.graphics.getHeight() - 
						Gdx.input.getY() < bYes.getY() + bYes.getHeight() && state == PAUSELEAVING){
					//Exit pause
					if(game.getMMS().sound == game.getMMS().ON)
						game.getMMS().buttonSmall.play();
					Tween.to(bYes, SpriteAccessor.COLOR, 0.25f).target(0.25f, 0.25f, 0.25f, 1f).repeatYoyo(1, 0).start(tweenManager);
					Timer.schedule(new Task(){
						@Override
						public void run(){
							updateTime();
							Gdx.app.exit();
						}
					}, 0.5f);
					dispose();	
				}else if(Gdx.input.getX() > bYes.getX() && Gdx.input.getX() < bYes.getX() + bYes.getWidth() &&
						Gdx.graphics.getHeight() - Gdx.input.getY() > bYes.getY() && Gdx.graphics.getHeight() - 
						Gdx.input.getY() < bYes.getY() + bYes.getHeight() && state == DEADLEAVING){
					//Exit dead
					if(game.getMMS().sound == game.getMMS().ON)
						game.getMMS().buttonSmall.play();
					tweenEnd = false;
					Timeline.createSequence().
					push(Tween.to(bExit, SpriteAccessor.COLOR, 0.10f).target(0.25f, 0.25f, 0.25f, 1f).repeatYoyo(1, 0)).
					beginParallel().
					push(Tween.to(bgs1, SpriteAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f).delay(0.30f)).
					push(Tween.to(bgs2, SpriteAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f).delay(0.30f)).
					push(Tween.to(bHome, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f)).
					push(Tween.to(bRetry, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f)).
					push(Tween.to(bShare, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f)).
					push(Tween.to(bExit, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 0f)).
					push(Tween.to(redsFont, BitmapFontAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f)).
					push(Tween.to(bluesFont, BitmapFontAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f)).
					push(Tween.to(greysFont, BitmapFontAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f)).
					push(Tween.to(greysFont2, BitmapFontAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f)).
					push(Tween.to(redsFont2, BitmapFontAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f)).
					push(Tween.to(bluesFont2, BitmapFontAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f)).
					push(Tween.to(blueCounter, SpriteAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f)).
					push(Tween.to(redCounter, SpriteAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f)).
					end().
					start(tweenManager);
	
					Timer.schedule(new Task(){
						@Override
						public void run(){
							if(!updatedDB){
								updateDatabase();
								updateTime();
								updatedDB = true;
							}
							Gdx.app.exit();
						}
					}, 1.45f);
					dispose();		
				}else if(Gdx.input.getX() > bNo.getX() && Gdx.input.getX() < bNo.getX() + bNo.getWidth() &&
						Gdx.graphics.getHeight() - Gdx.input.getY() > bNo.getY() && Gdx.graphics.getHeight() - 
						Gdx.input.getY() < bNo.getY() + bNo.getHeight() && state == DEADLEAVING){
					state = DEAD;
					if(game.getMMS().sound == game.getMMS().ON)
						game.getMMS().buttonSmall.play();
				}else if(Gdx.input.getX() > bNo.getX() && Gdx.input.getX() < bNo.getX() + bNo.getWidth() &&
						Gdx.graphics.getHeight() - Gdx.input.getY() > bNo.getY() && Gdx.graphics.getHeight() - 
						Gdx.input.getY() < bNo.getY() + bNo.getHeight() && state == PAUSELEAVING){
					state = PAUSE;
					if(game.getMMS().sound == game.getMMS().ON)
						game.getMMS().buttonSmall.play();
				}
			}	

			if(Gdx.input.justTouched() && !objectClockEnable){
				if(Gdx.input.getX() > bPause.getX() && Gdx.graphics.getHeight() - Gdx.input.getY() > bPause.getY() && !clicked && ship.canPlay){
					clicked = true;
					if(game.getMMS().sound == game.getMMS().ON)
						game.getMMS().buttonSmall.play();
					if(state == PLAY){
						state = PAUSE;
						Tween.to(bRetry2, SpriteAccessor.POS_XY, 0.25f).ease(TweenEquations.easeInOutSine).target(game.getMMS().v1.x, game.getMMS().v1.y).start(tweenManager);
						Tween.to(bHome2, SpriteAccessor.POS_XY, 0.25f).delay(0.15f).ease(TweenEquations.easeInOutSine).target(game.getMMS().v2.x, game.getMMS().v2.y).start(tweenManager);
						Tween.to(bExit2, SpriteAccessor.POS_XY, 0.25f).delay(0.30f).ease(TweenEquations.easeInOutSine).target(game.getMMS().v3.x, game.getMMS().v3.y).start(tweenManager);
						Tween.to(bVibrationOn, SpriteAccessor.POS_XY, 0.25f).delay(0.45f).ease(TweenEquations.easeInOutSine).target(game.getMMS().v4.x, game.getMMS().v4.y).start(tweenManager);
						Tween.to(bVibrationOff, SpriteAccessor.POS_XY, 0.25f).delay(0.45f).ease(TweenEquations.easeInOutSine).target(game.getMMS().v4.x, game.getMMS().v4.y).start(tweenManager);
						Tween.to(bControlsOn, SpriteAccessor.POS_XY, 0.25f).delay(0.60f).ease(TweenEquations.easeInOutSine).target(game.getMMS().v5.x, game.getMMS().v5.y).start(tweenManager);
						Tween.to(bControlsOff, SpriteAccessor.POS_XY, 0.25f).delay(0.60f).ease(TweenEquations.easeInOutSine).target(game.getMMS().v5.x, game.getMMS().v5.y).start(tweenManager);
						Tween.to(bSoundOn, SpriteAccessor.POS_XY, 0.25f).delay(0.75f).ease(TweenEquations.easeInOutSine).target(game.getMMS().v6.x, game.getMMS().v6.y).start(tweenManager);
						Tween.to(bSoundOff, SpriteAccessor.POS_XY, 0.25f).delay(0.75f).ease(TweenEquations.easeInOutSine).target(game.getMMS().v6.x, game.getMMS().v6.y).start(tweenManager);				
						Tween.to(bHelp, SpriteAccessor.POS_XY, 0.25f).delay(0.90f).ease(TweenEquations.easeInOutSine).target(Gdx.graphics.getWidth()/2 - bHelp.getWidth()/2, game.getMMS().v2.y).start(tweenManager);				

						Timer.schedule(new Task(){
							@Override
							public void run(){
								clicked = false;
							}
						}, 1.15f);
					}else if(state == PAUSE){
						float x = Gdx.graphics.getWidth() + bHome2.getWidth()*2;
						float y = Gdx.graphics.getHeight()/2 - bHome2.getHeight()/2 + 3*adHeight/4;
						Tween.to(bSoundOn, SpriteAccessor.POS_XY, 0.25f).ease(TweenEquations.easeInOutSine).target(x, y).start(tweenManager);
						Tween.to(bSoundOff, SpriteAccessor.POS_XY, 0.25f).ease(TweenEquations.easeInOutSine).target(x, y).start(tweenManager);				
						Tween.to(bControlsOn, SpriteAccessor.POS_XY, 0.25f).delay(0.15f).ease(TweenEquations.easeInOutSine).target(x, y).start(tweenManager);
						Tween.to(bControlsOff, SpriteAccessor.POS_XY, 0.25f).delay(0.15f).ease(TweenEquations.easeInOutSine).target(x, y).start(tweenManager);
						Tween.to(bVibrationOn, SpriteAccessor.POS_XY, 0.25f).delay(0.30f).ease(TweenEquations.easeInOutSine).target(x, y).start(tweenManager);
						Tween.to(bVibrationOff, SpriteAccessor.POS_XY, 0.25f).delay(0.30f).ease(TweenEquations.easeInOutSine).target(x, y).start(tweenManager);
						Tween.to(bExit2, SpriteAccessor.POS_XY, 0.25f).delay(0.45f).ease(TweenEquations.easeInOutSine).target(x, y).start(tweenManager);
						Tween.to(bHome2, SpriteAccessor.POS_XY, 0.25f).delay(0.60f).ease(TweenEquations.easeInOutSine).target(x, y).start(tweenManager);
						Tween.to(bRetry2, SpriteAccessor.POS_XY, 0.25f).delay(0.75f).ease(TweenEquations.easeInOutSine).target(x, y).start(tweenManager);
						Tween.to(bHelp, SpriteAccessor.POS_XY, 0.25f).delay(0.90f).ease(TweenEquations.easeInOutSine).target(x, y).start(tweenManager);

						Timer.schedule(new Task(){
							@Override
							public void run(){
								clicked = false;
								state = PLAY;
							}
						}, 1.15f);
					}
				}
			}	

			if(redToBlue){
				for(Ball ball : dBalls){
					ball.changeBlue();
				}	
			}if(blueToRed){
				for(Ball ball : dBalls){
					ball.changeRed();
				}	
			}
			if(object1 != -1 && state == PLAY){
				if(Gdx.input.justTouched() && (Gdx.input.getX() >= PlayScreen.padVerticalLeft.getX() + PlayScreen.padVerticalLeft.getWidth()) && (Gdx.input.getX() <= PlayScreen.padVerticalRight.getX() && 
						Gdx.graphics.getHeight() - Gdx.input.getY() > PlayScreen.padHorizontalDown.getY() + PlayScreen.padHorizontalDown.getHeight()) && 
						Gdx.graphics.getHeight() - Gdx.input.getY() < PlayScreen.padHorizontalUp.getY() + PlayScreen.padHorizontalUp.getHeight() && !clicked 
						|| (Gdx.input.getX(1) >= PlayScreen.padVerticalLeft.getX() + PlayScreen.padVerticalLeft.getWidth()) && (Gdx.input.getX(1) <= PlayScreen.padVerticalRight.getX() && 
						Gdx.graphics.getHeight() - Gdx.input.getY(1) > PlayScreen.padHorizontalDown.getY() + PlayScreen.padHorizontalDown.getHeight()) && 
						Gdx.graphics.getHeight() - Gdx.input.getY(1) < PlayScreen.padHorizontalUp.getY() + PlayScreen.padHorizontalUp.getHeight() && !clicked){
					if(object1 == 0){
						if(game.getMMS().sound == game.getMMS().ON)
							swap.play(0.9f);
						blueToRed = true;
					}else if(object1 == 1){
						if(game.getMMS().sound == game.getMMS().ON)
							swap.play(0.65f);
						redToBlue = true;
					}else if(object1 == 2){
						if(game.getMMS().sound == game.getMMS().ON)
							mult.play(0.65f);
						blueX2 = true;
						bluesValue = 2;
					}else if(object1 == 3){
						if(game.getMMS().sound == game.getMMS().ON)
							mult.play(0.65f);
						redX2 = true;
						redsValue = 2;
					}else if(object1 == 4){
						slow = true;
						if(game.getMMS().sound == game.getMMS().ON)
							slowDown.play(0.75f);
						ballSpawnSpeed = 2;
						for(Ball ball : balls){
							ball.setVelocity(currentBallVelocity());
							ball.setVelocityChanged(true);
						}	
					}else if(object1 == 5){
						if(game.getMMS().sound == game.getMMS().ON)
							sword.play(0.80f);
						blueShield = true;
						ship.forceFieldSelected = 3;
					}else if(object1 == 6){
						if(game.getMMS().sound == game.getMMS().ON)
							sword.play(0.80f);
						redShield = true;
						ship.forceFieldSelected = 2;
					}else if(object1 == 7){
						if(game.getMMS().sound == game.getMMS().ON)
							mult.play(0.65f);
						blueX3 = true;
						bluesValue = 3;
					}else if(object1 == 8){
						if(game.getMMS().sound == game.getMMS().ON)
							mult.play(0.65f);
						redX3 = true;
						redsValue = 3;
					}else if(object1 == 9){
						if(game.getMMS().sound == game.getMMS().ON)
							sword.play(0.80f);
						goldShield = true;
						ship.forceFieldSelected = 4;
					}
//					obj[object1]++;
					auxObj = object1;
					object1 = -1;
					objectClock = TimeUtils.nanoTime();
					objectClockEnable = true;
					canPlayRetreat = true;
					Tween.call(objectCallback).start(tweenManager);
				}
			}
			
			if(canPlayRetreat && TimeUtils.nanoTime() - objectClock > 2000000000f*game.getMMS().objectsDuration){
				canPlayRetreat = false;
				if(slow)
					Timer.schedule(new Task(){
						@Override
						public void run(){
							if(game.getMMS().sound == game.getMMS().ON && state != DEAD )
								slowDownReverse.play(0.70f);
						}
					}, 2.20f);
				else if(redShield || blueShield || goldShield)
					Timer.schedule(new Task(){
						@Override
						public void run(){
							if(game.getMMS().sound == game.getMMS().ON && state != DEAD )
								swordReverse.play(0.80f);
						}
					}, 2.475f);
				else if(redToBlue || blueToRed)
					Timer.schedule(new Task(){
						@Override
						public void run(){
							if(game.getMMS().sound == game.getMMS().ON && state != DEAD )
								swapReverse.play(0.9f);
						}
					}, 2.425f);
				else if(blueX2 || blueX3 || redX2 || redX3)
					Timer.schedule(new Task(){
						@Override
						public void run(){
							if(game.getMMS().sound == game.getMMS().ON && state != DEAD )
								multReverse.play(0.65f);
						}
					}, 2.75f);
			}

			if(objectClockEnable && TimeUtils.nanoTime() - objectClock > 3000000000f + 2000000000f*game.getMMS().objectsDuration){
				redToBlue = false;
				blueToRed = false;
				if(slow){
					slow = false;
					for(Ball ball : balls){
						ball.setVelocity(currentBallVelocity());
						ball.setVelocityChanged(false);
					}
					ballSpawnSpeed = 1;
				}
				if(redShield){
					redShield = false;
					ship.forceFieldSelected = 0;								
				}
				if(blueShield){
					blueShield = false;
					ship.forceFieldSelected = 1;								
				}
				if(goldShield){
					goldShield = false;
					ship.forceFieldSelected = ship.forceFieldSelected2;								
				}
				if(blueX2 || blueX3){
					bluesValue = 1;
					blueX2 = false;
					blueX3 = false;
				}	
				if(redX2 || redX3){
					redsValue = 1;
					redX2 = false;
					redX3 = false;
				}	
				objectClockEnable = false;
			}

			game.batch.begin();

			drawBackground();

			if(state != HELP){
				// draw arrows
				if(first){
					Timer.schedule(new Task(){
						@Override
						public void run(){
							arrowLeft.setPosition(Gdx.graphics.getWidth() / 15, arrowRight.getHeight() * 1.5f + adHeight/2);
							arrowRight.setPosition(Gdx.graphics.getWidth() - arrowRight.getWidth() - Gdx.graphics.getWidth() / 15, arrowRight.getHeight() * 1.5f + adHeight/2);
							first = false;
						}
					}, 0.75f);
				}else if (state != PlayScreen.DEAD && state != DEADLEAVING){
					arrowLeft.setPosition(Gdx.graphics.getWidth() / 15, arrowRight.getHeight() * 1.5f + adHeight/2);
					arrowRight.setPosition(Gdx.graphics.getWidth() - arrowRight.getWidth() - Gdx.graphics.getWidth() / 15, arrowRight.getHeight() * 1.5f + adHeight/2);
				}
				arrowLeft.draw(game.batch);
				arrowRight.draw(game.batch);
	
				//draw score counters
				redsFont.draw(game.batch, Integer.toString(reds),Gdx.graphics.getWidth() - 20 - com.spaceship.handlers.FontUtil.bounds(redsFont, Integer.toString(reds)).width, Gdx.graphics.getHeight()/2 + com.spaceship.handlers.FontUtil.bounds(redsFont, Integer.toString(reds)).height/2 + 3*adHeight/4);		
				bluesFont.draw(game.batch, Integer.toString(blues), 20, Gdx.graphics.getHeight()/2 + com.spaceship.handlers.FontUtil.bounds(bluesFont, Integer.toString(blues)).height/2 + 3*adHeight/4);
				redsFont.getData().setScale(x, y);
				bluesFont.getData().setScale(x, y);
				if(upRed && reds > 0){
					redsFont3.draw(game.batch, "+" + Integer.toString(reds),redCounter.getX() + 1.25f*redCounter.getWidth() + com.spaceship.handlers.FontUtil.bounds(redsFont3, Integer.toString(game.getMMS().redBalls/10)).width - com.spaceship.handlers.FontUtil.bounds(redsFont3, "+").width, redFont3Height);		
					redsFont3.getData().setScale(x, y);
					if(redsFont3.getColor().a > 0){
						redFont3Height = redFont3Height + 2.25f;
						Tween.set(redsFont3, BitmapFontAccessor.COLOR).target(1f, 1f, 1f, redsFont3.getColor().a - 0.01f).start(tweenManager);
						if(canTweenRedScore){
							if(game.getMMS().sound == game.getMMS().ON)
								addScore.play();
							canTweenRedScore = false;
						}	
					}
					if(reds > 1){
						if(reds % 2 == 0)
							bluesFont3.draw(game.batch, "+" + Integer.toString(reds/2), blueCounter.getX() - com.spaceship.handlers.FontUtil.bounds(bluesFont3, "+" + Integer.toString(reds/2)).width - blueCounter.getWidth()/4, blueFont3Height);		
						else
							bluesFont3.draw(game.batch, "+" + Integer.toString((reds - 1)/2), blueCounter.getX() - com.spaceship.handlers.FontUtil.bounds(bluesFont3, "+" + Integer.toString((reds - 1)/2)).width - blueCounter.getWidth()/4, blueFont3Height);		
						bluesFont3.getData().setScale(x, y);
						if(bluesFont3.getColor().a > 0){
							blueFont3Height = blueFont3Height + 2.25f;
							Tween.set(bluesFont3, BitmapFontAccessor.COLOR).target(1f, 1f, 1f, bluesFont3.getColor().a - 0.01f).start(tweenManager);
							if(canTweenBlueScore){
								if(game.getMMS().sound == game.getMMS().ON)
									addScore.play();	
								canTweenBlueScore = false;
							}	
						}
					}
				}
				if(upBlue && blues > 0){
					bluesFont3.draw(game.batch, "+" + Integer.toString(blues), blueCounter.getX() - com.spaceship.handlers.FontUtil.bounds(bluesFont3, "+" + Integer.toString(blues)).width - blueCounter.getWidth()/4, blueFont3Height);		
					bluesFont3.getData().setScale(x, y);
					if(bluesFont3.getColor().a > 0){
						blueFont3Height = blueFont3Height + 2.25f;
						Tween.set(bluesFont3, BitmapFontAccessor.COLOR).target(1f, 1f, 1f, bluesFont3.getColor().a - 0.01f).start(tweenManager);
						if(canTweenBlueScore){
							if(game.getMMS().sound == game.getMMS().ON)
								addScore.play();	
							canTweenBlueScore = false;
						}	
					}
					if(blues > 1){
						if(blues % 2 == 0)
							redsFont3.draw(game.batch, "+" + Integer.toString(blues/2),redCounter.getX() + 1.25f*redCounter.getWidth() + com.spaceship.handlers.FontUtil.bounds(redsFont3, Integer.toString(game.getMMS().redBalls/10)).width - com.spaceship.handlers.FontUtil.bounds(redsFont3, "+").width, redFont3Height);		
						else
							redsFont3.draw(game.batch, "+" + Integer.toString((blues - 1)/2),redCounter.getX() + 1.25f*redCounter.getWidth() + com.spaceship.handlers.FontUtil.bounds(redsFont3, Integer.toString(game.getMMS().redBalls/10)).width - com.spaceship.handlers.FontUtil.bounds(redsFont3, "+").width, redFont3Height);		
						redsFont3.getData().setScale(x, y);
						if(redsFont3.getColor().a > 0){
							redFont3Height = redFont3Height + 2.25f;
							Tween.set(redsFont3, BitmapFontAccessor.COLOR).target(1f, 1f, 1f, redsFont3.getColor().a - 0.01f).start(tweenManager);
							if(canTweenRedScore){
								if(game.getMMS().sound == game.getMMS().ON)
									addScore.play();
								canTweenRedScore = false;
							}	
						}
					}
				}
				if(upBoth && reds > 0){
					redsFont3.draw(game.batch, "+" + Integer.toString(reds),redCounter.getX() + 1.25f*redCounter.getWidth() + com.spaceship.handlers.FontUtil.bounds(redsFont3, Integer.toString(game.getMMS().redBalls/10)).width - com.spaceship.handlers.FontUtil.bounds(redsFont3, "+").width, redFont3Height);		
					redsFont3.getData().setScale(x, y);
					if(redsFont3.getColor().a > 0){
						redFont3Height = redFont3Height + 2.25f;
						Tween.set(redsFont3, BitmapFontAccessor.COLOR).target(1f, 1f, 1f, redsFont3.getColor().a - 0.01f).start(tweenManager);
						if(canTweenRedScore){
							if(game.getMMS().sound == game.getMMS().ON)
								addScore.play();
							canTweenRedScore = false;
						}	
					}
					bluesFont3.draw(game.batch, "+" + Integer.toString(blues), blueCounter.getX() - com.spaceship.handlers.FontUtil.bounds(bluesFont3, "+" + Integer.toString(blues)).width - blueCounter.getWidth()/4, blueFont3Height);		
					bluesFont3.getData().setScale(x, y);
					if(bluesFont3.getColor().a > 0){
						blueFont3Height = blueFont3Height + 2.25f;
						Tween.set(bluesFont3, BitmapFontAccessor.COLOR).target(1f, 1f, 1f, bluesFont3.getColor().a - 0.01f).start(tweenManager);
						if(canTweenBlueScore){
							if(game.getMMS().sound == game.getMMS().ON)
								addScore.play();	
							canTweenBlueScore = false;
						}	
					}
				}
	
				drawCatchPopups();
				if(drawLife){
					if(goldShield)
						lifesFont.draw(game.batch, "+2", life1x, life1y);
					else
						lifesFont.draw(game.batch, "+1", life1x, life1y);
					lifesFont.getData().setScale(x, y);
					if(lifesFont.getColor().a > 0){
						life1y = (int) (life1y + 2.5f);
						Tween.set(lifesFont, BitmapFontAccessor.COLOR).target(1f, 1f, 1f, lifesFont.getColor().a - 0.025f).start(tweenManager);
					}else{
						drawLife = false;
						Tween.set(lifesFont, BitmapFontAccessor.COLOR).target(1f, 1f, 1f, 1f).start(tweenManager);	
					}
				}
	
				// draw ship
				if(state == PLAY){
					ship.update();
					Fx.update(Gdx.graphics.getDeltaTime());
				}
				ship.render(game.batch);
				{
					float[] sc = shieldColor();
					Sprite shs = ship.shipsSprites[ship.shipSelected];
					Fx.additiveOn(game.batch);
					speedFx.draw(game.batch, shipCenterX(), shipCenterY(), shs.getWidth()*shs.getScaleX(), director.getChargeTime(), sc[0], sc[1], sc[2]);
					Fx.additiveOff(game.batch);
				}
	
				// draw balls
				if(state != PlayScreen.DEAD){
					Fx.additiveOn(game.batch);
					for(Ball ball : dBalls){
						ball.renderTail(game.batch);
					}
					Fx.additiveOff(game.batch);
					for(Ball ball : dBalls){
						ball.render(game.batch);
					}			
					Fx.additiveOn(game.batch);
					for(int h = 0; h < 20; h++){
						redParticles[h].draw(game.batch);
						blueParticles[h].draw(game.batch);
						lifeParticles[h].draw(game.batch);
						objectsParticles[h].draw(game.batch);
					}
					Fx.draw(game.batch);
					Fx.additiveOff(game.batch);
					for(int h = 0; h < 5; h++){
						redWrong[h].draw(game.batch);
						blueWrong[h].draw(game.batch);
					}
				}
	
				drawLifeMeter();
	
				// draw objects
				ring1.draw(game.batch);
				if(state != DEAD)
					for(int i = 0; i < objectsSprites1.length; i++){
						objectsSprites1[i].draw(game.batch);
					}
				if(game.getMMS().objectsSlots == 2){
					ring2.draw(game.batch);
					if(state != DEAD)
						for(int i = 0; i < objectsSprites2.length; i++){
							objectsSprites2[i].draw(game.batch);
						}
				}else if(game.getMMS().objectsSlots == 3){
					ring2.draw(game.batch);
					ring3.draw(game.batch);
					if(state != DEAD)
						for(int i = 0; i < objectsSprites1.length; i++){
							objectsSprites2[i].draw(game.batch);
							objectsSprites3[i].draw(game.batch);
						}
				}
	
				if(state == PlayScreen.DEAD){
					bHome.draw(game.batch);
					bRetry.draw(game.batch);
					bShare.draw(game.batch);
					bExit.draw(game.batch);
					if(game.getMMS().language == game.getMMS().ON){
						greysFont.draw(game.batch, "    New    \nHighScore", (Gdx.graphics.getWidth()/2 - com.spaceship.handlers.FontUtil.bounds(greysFont, "HighScore").width/2),
							Gdx.graphics.getHeight() - (Gdx.graphics.getHeight()/2 - 2f*bExit.getHeight() - Gdx.graphics.getHeight()/10)/2 + 3*adHeight/4);
					
						greysFont2.draw(game.batch, "Best Score: " + bestScore, (Gdx.graphics.getWidth() - com.spaceship.handlers.FontUtil.bounds(greysFont2, "Best Score: " + bestScore).width)/2, 
								Gdx.graphics.getHeight()/10 + 3*adHeight/4);
					}else{
						greysFont.draw(game.batch, "     Nueva   \nMejor Marca", (Gdx.graphics.getWidth()/2 - com.spaceship.handlers.FontUtil.bounds(greysFont, "Mejor Marca").width/2),
								Gdx.graphics.getHeight() - (Gdx.graphics.getHeight()/2 - 2f*bExit.getHeight() - Gdx.graphics.getHeight()/10)/2 + 3*adHeight/4);
						greysFont2.draw(game.batch, "Mejor Marca: " + bestScore, (Gdx.graphics.getWidth() - com.spaceship.handlers.FontUtil.bounds(greysFont2, "Mejor Marca: " + bestScore).width)/2, 
								Gdx.graphics.getHeight()/10 + 3*adHeight/4);
					}
					greysFont.getData().setScale(x, y);
					// the counter row is the main menu's own (same orbs, same animation, same spacing); the animation clock only runs by itself while playing
					if(state == DEAD || state == DEADLEAVING)
						Fx.update(Gdx.graphics.getDeltaTime());
					MainMenuScreen mm = game.getMMS();
					String blueText = null, redText = null;
					if(changeRedScore){
						redText = String.valueOf(mm.redBalls + reds);
						blueText = String.valueOf(mm.blueBalls + (reds % 2 == 0 ? reds/2 : (reds - 1)/2));
					}
					if(changeBlueScore){
						blueText = String.valueOf(mm.blueBalls + blues);
						redText = String.valueOf(mm.redBalls + (blues % 2 == 0 ? blues/2 : (blues - 1)/2));
					}
					if(changeBothScore){
						blueText = String.valueOf(mm.blueBalls + blues);
						redText = String.valueOf(mm.redBalls + reds);
					}
					mm.drawBlueRedRow(game.batch, blueText, redText, mm.getCounterBaseY(), blueCounter.getColor().a);
				}
	
				// draw pads
				if(game.getMMS().controls == game.getMMS().ON && state != DEAD && state != DEADLEAVING){
					if(game.getMMS().language == game.getMMS().ON){
						padVerticalLeft.draw(game.batch);
						padVerticalRight.draw(game.batch);
						padHorizontalUp.draw(game.batch);
						padHorizontalDown.draw(game.batch);
					}else{
						padVerticalLeftEsp.draw(game.batch);
						padVerticalRightEsp.draw(game.batch);
						padHorizontalUpEsp.draw(game.batch);
						padHorizontalDownEsp.draw(game.batch);
					}
				}	
			}
			//draw play/pause
			if(state == PLAY){
				if(!objectClockEnable)
					bPause.draw(game.batch);
				else
					bPauseCant.draw(game.batch);
			}else if(state == PAUSE){
				cover.draw(game.batch);
				bPlay.draw(game.batch);
				bHome2.draw(game.batch);
				bRetry2.draw(game.batch);
				bExit2.draw(game.batch);
				bHelp.draw(game.batch);
				if(game.getMMS().vibration == game.getMMS().ON)
					bVibrationOn.draw(game.batch);
				else
					bVibrationOff.draw(game.batch);
				if(game.getMMS().controls == game.getMMS().ON)
					bControlsOn.draw(game.batch);
				else
					bControlsOff.draw(game.batch);
				if(game.getMMS().sound == game.getMMS().ON)
					bSoundOn.draw(game.batch);
				else
					bSoundOff.draw(game.batch);
			}	
			
			if(state == DEADLEAVING){
				bNo.draw(game.batch);
				bYes.draw(game.batch);
				if(game.getMMS().language == game.getMMS().ON)
					bQuestionEN.draw(game.batch);
				else
					bQuestionES.draw(game.batch);
			}else if(state == PAUSELEAVING){
				cover.draw(game.batch);
				bNo.draw(game.batch);
				bYes.draw(game.batch);
				if(game.getMMS().language == game.getMMS().ON)
					bQuestionEN.draw(game.batch);
				else
					bQuestionES.draw(game.batch);
			}
			
//			effect.draw(game.batch, delta);

			game.batch.end();

			stageHelp.act(delta);
			stageHelp.draw();

			if(state == PLAY){
				// the speed director owns the run's speed, the spawn rate and the charge-up before each speed tier
				updateSpeedDirector();

				// move the balls, remove any that hit the ship
				Iterator<Ball> iter = dBalls.iterator();
				while (iter.hasNext()){
					Ball ball = iter.next();
					ball.moveBall();
					if(shouldRemoveBall(ball)){
						iter.remove();
						ball.setPosition(MathUtils.random((fx(20) + fy(20))/2, Gdx.graphics.getWidth() - (fx(20) + fy(20))/2),
								Gdx.graphics.getHeight() + fxy(20));
						ball.reset();
					}	
				}
			}

		}
	}

	@Override
	public void resize(int width, int height) {
	}

	private void spawnBall(){
		if(firstBall){
			if(firstBallHelper){
				firstBallHelper = false;
				Timer.schedule(new Task(){
					@Override
					public void run(){
						if(!balls[ballToAdd].taken){
							balls[ballToAdd].setVelocity(currentBallVelocity());
							dBalls.add(balls[ballToAdd]);
							balls[ballToAdd].taken = true;
						}	
						ballToAdd++;
						lastBallTime = TimeUtils.nanoTime();
						firstBall = false;
					}
				}, 0.80f);
			}else {}
		}else{
			if(!balls[ballToAdd].taken){
				balls[ballToAdd].setVelocity(currentBallVelocity());
				dBalls.add(balls[ballToAdd]);
				balls[ballToAdd].taken = true;
			}	
			if(ballToAdd < balls.length-1)
				ballToAdd++;
			else{
				ballToAdd = 0;
			}	
			lastBallTime = TimeUtils.nanoTime();
		}
	}

	public boolean shouldRemoveBall(Ball ball){
		boolean hit = false;
		if(ship.getCircle().overlaps(ball.getCircle())){	
			hit = true;
			ship.shieldImpact(ball.getPosition().x, ball.getPosition().y);
			if(ship.getState() == Spaceship.STATE_BLUE || ship.getState() == Spaceship.STATE_BLUERED){
				if (ball.getType() == Ball.B_BLUE){
					if(game.getMMS().sound == game.getMMS().ON)
						absorb.play(0.90f);
					if(ship.getState() == Spaceship.STATE_BLUE)
						ship.tweenBlueFF();
					else
						ship.tweenBlueRedFF();
					blues = blues + bluesValue;
						addCatchPopup(ball, bluesValue, true);
					for(int h = 0; h < 5; h++){
						blueParticles[h + blueP].setPosition(ball.getPosition().x, ball.getPosition().y);
						blueParticles[h + blueP].setColor(1f, 1f, 1f, 1f);
						d = MathUtils.random(0, 1);
						if(d == 0) xx = MathUtils.random(-45, -25);
						else xx = MathUtils.random(25, 45);
						d = MathUtils.random(0, 1);
						if(d == 0) yy = MathUtils.random(-45, -25);
						else yy = MathUtils.random(25, 45);
						Tween.to(blueParticles[h + blueP], SpriteAccessor.COLOR, 0.35f).target(1f, 1f, 1f, 0f).start(tweenManager);
						Tween.to(blueParticles[h + blueP], SpriteAccessor.POS_XY, 0.35f).
						target(blueParticles[h + blueP].getX() + xx, blueParticles[h + blueP].getY() + yy).start(tweenManager);
					}
					if(blueP != 15)	blueP = blueP + 5;
					else blueP = 0;					
				}else if(ball.getType() == Ball.B_RED){
					if(game.getMMS().vibration == game.getMMS().ON)
						Gdx.input.vibrate(75);
					if(game.getMMS().sound == game.getMMS().ON)
						if(ship.getState() == Spaceship.STATE_BLUE)
							error.play(0.85f);
						else
							shield.play();
					ship.tweenBlackFF();
					redWrong[redW].setPosition(ball.getPosition().x - 10, ball.getPosition().y);
					redWrong[redW].setColor(1f, 1f, 1f, 1f);
					Tween.to(redWrong[redW], SpriteAccessor.COLOR, 0.35f).target(1f, 1f, 1f, 0f).start(tweenManager);
					Tween.to(redWrong[redW], SpriteAccessor.SCALE, 0.35f).target(2, 2).repeatYoyo(1, 0).start(tweenManager);
					if(redW != 4) redW++;
					else redW = 0;
					if(!redShield){
						if(ship.getLifes() > 0){
							ship.setLifes(ship.getLifes() - 1);
							if(ship.getLifes() > 1) 
								Tween.call(lifeMeterChangeCallback).start(tweenManager);
						}else{ 
							state = PlayScreen.DEAD;
							if(game.getMMS().sound == game.getMMS().ON)
								dead.play(0.35f);
						}	
					}	
				}
			}else if(ship.getState() == Spaceship.STATE_RED || ship.getState() == Spaceship.STATE_REDBLUE){
				if(ball.getType() == Ball.B_RED){
					if(game.getMMS().sound == game.getMMS().ON)
						absorb.play(0.90f);
					if(ship.getState() == Spaceship.STATE_RED)
						ship.tweenRedFF();
					else
						ship.tweenRedBlueFF();
					reds = reds + redsValue;
						addCatchPopup(ball, redsValue, false);
					for(int h = 0; h < 5; h++){
						redParticles[h + redP].setPosition(ball.getPosition().x, ball.getPosition().y);
						redParticles[h + redP].setColor(1f, 1f, 1f, 1f);
						d = MathUtils.random(0, 1);
						if(d == 0) xx = MathUtils.random(-45, -25);
						else xx = MathUtils.random(25, 45);
						d = MathUtils.random(0, 1);
						if(d == 0) yy = MathUtils.random(-45, -25);
						else yy = MathUtils.random(25, 45);
						Tween.to(redParticles[h + redP], SpriteAccessor.COLOR, 0.35f).target(1f, 1f, 1f, 0f).start(tweenManager);
						Tween.to(redParticles[h + redP], SpriteAccessor.POS_XY, 0.35f).
						target(redParticles[h + redP].getX() + xx, redParticles[h + redP].getY() + yy).start(tweenManager);
					}
					if(redP != 15)	redP = redP + 5;
					else redP = 0;
				}else if(ball.getType() == Ball.B_BLUE){
					if(game.getMMS().vibration == game.getMMS().ON)
						Gdx.input.vibrate(75);
					if(game.getMMS().sound == game.getMMS().ON)
						if(ship.getState() == Spaceship.STATE_RED)
							error.play(0.85f);
						else
							shield.play();
					ship.tweenBlackFF();
					blueWrong[blueW].setPosition(ball.getPosition().x-10, ball.getPosition().y);
					blueWrong[blueW].setColor(1f, 1f, 1f, 1f);
					Tween.to(blueWrong[blueW], SpriteAccessor.COLOR, 0.35f).target(1f, 1f, 1f, 0f).start(tweenManager);
					Tween.to(blueWrong[blueW], SpriteAccessor.SCALE, 0.35f).target(2, 2).repeatYoyo(1, 0).start(tweenManager);
					if(blueW != 4) blueW++;
					else blueW = 0;
					if(!blueShield){
						if(ship.getLifes() > 0){
							ship.setLifes(ship.getLifes() - 1);
							if(ship.getLifes() > 1) 
								Tween.call(lifeMeterChangeCallback).start(tweenManager);
						}else{ 
							state = PlayScreen.DEAD;
							if(game.getMMS().sound == game.getMMS().ON)
								dead.play(0.35f);
						}	
					}
				}
			}else if(ship.getState() == Spaceship.STATE_GOLD){
				if(ball.getType() == Ball.B_RED){
					if(game.getMMS().sound == game.getMMS().ON)
						absorb.play(0.90f);
					ship.tweenGoldFF();
					reds = reds + redsValue;
						addCatchPopup(ball, redsValue, false);
					for(int h = 0; h < 5; h++){
						redParticles[h + redP].setPosition(ball.getPosition().x, ball.getPosition().y);
						redParticles[h + redP].setColor(1f, 1f, 1f, 1f);
						d = MathUtils.random(0, 1);
						if(d == 0) xx = MathUtils.random(-45, -25);
						else xx = MathUtils.random(25, 45);
						d = MathUtils.random(0, 1);
						if(d == 0) yy = MathUtils.random(-45, -25);
						else yy = MathUtils.random(25, 45);
						Tween.to(redParticles[h + redP], SpriteAccessor.COLOR, 0.35f).target(1f, 1f, 1f, 0f).start(tweenManager);
						Tween.to(redParticles[h + redP], SpriteAccessor.POS_XY, 0.35f).
						target(redParticles[h + redP].getX() + xx, redParticles[h + redP].getY() + yy).start(tweenManager);
					}
					if(redP != 15)	redP = redP + 5;
					else redP = 0;
				}else if(ball.getType() == Ball.B_BLUE){
					if(game.getMMS().sound == game.getMMS().ON)
						absorb.play(0.90f);
					ship.tweenGoldFF();
					blues = blues + bluesValue;
						addCatchPopup(ball, bluesValue, true);
					for(int h = 0; h < 5; h++){
						blueParticles[h + blueP].setPosition(ball.getPosition().x, ball.getPosition().y);
						blueParticles[h + blueP].setColor(1f, 1f, 1f, 1f);
						d = MathUtils.random(0, 1);
						if(d == 0) xx = MathUtils.random(-45, -25);
						else xx = MathUtils.random(25, 45);
						d = MathUtils.random(0, 1);
						if(d == 0) yy = MathUtils.random(-45, -25);
						else yy = MathUtils.random(25, 45);
						Tween.to(blueParticles[h + blueP], SpriteAccessor.COLOR, 0.35f).target(1f, 1f, 1f, 0f).start(tweenManager);
						Tween.to(blueParticles[h + blueP], SpriteAccessor.POS_XY, 0.35f).
						target(blueParticles[h + blueP].getX() + xx, blueParticles[h + blueP].getY() + yy).start(tweenManager);
					}
					if(blueP != 15)	blueP = blueP + 5;
					else blueP = 0;			
				}
			}
			if(ball.getType() == Ball.B_LIFE){
				if(game.getMMS().sound == game.getMMS().ON)
					life.play(0.75f);
				lifes++;
				lifesCounter++;
				ship.tweenYellowFF();
				ship.goldFlash();
				if(ship.getLifes() < game.getMMS().energyMeter) 
					ship.setLifes(ship.getLifes() + 1);
				if(ship.getLifes() > 1) 
					Tween.call(lifeMeterChangeCallback).start(tweenManager);	
				for(int h = 0; h < 5; h++){
					lifeParticles[lifeP].setPosition(ball.getPosition().x, ball.getPosition().y);
					lifeParticles[h + lifeP].setColor(1f, 1f, 1f, 1f);
					d = MathUtils.random(0, 1);
					if(d == 0) xx = MathUtils.random(-45, -25);
					else xx = MathUtils.random(25, 45);
					d = MathUtils.random(0, 1);
					if(d == 0) yy = MathUtils.random(-45, -25);
					else yy = MathUtils.random(25, 45);
					Tween.to(lifeParticles[h + lifeP], SpriteAccessor.COLOR, 0.35f).target(1f, 1f, 1f, 0f).start(tweenManager);
					Tween.to(lifeParticles[h + lifeP], SpriteAccessor.POS_XY, 0.35f).
					target(lifeParticles[h + lifeP].getX() + xx, lifeParticles[h + lifeP].getY() + yy).start(tweenManager);
				}
				if(lifeP != 15)	lifeP = lifeP + 5;
				else lifeP = 0;				
				life1y = (int) (ball.getPosition().y + com.spaceship.handlers.FontUtil.bounds(lifesFont, "+1").height);
				life1x = (int) ball.getPosition().x;
				drawLife = true;
			}
			if(ball.getType() == Ball.B_OBJECT){
				if(game.getMMS().sound == game.getMMS().ON)
					object.play(0.75f);
				objCounter++;
				ship.tweenObjectFF();	
				if(object1 == -1 && !(redToBlue || blueToRed || blueX2 || redX2 || slow || blueShield || redShield || blueX3 || redX3 || goldShield)){
					object1 = randomObject();
					obj[object1]++;
					objectsSprites1[object1].setColor(1f, 1f, 1f, 1f); 
				}else if(game.getMMS().objectsSlots > 1 && object2 == -1){
					object2 = randomObject();
					obj[object2]++;
					objectsSprites2[object2].setColor(1f, 1f, 1f, 1f); 
				}else if(game.getMMS().objectsSlots > 2 && object2 != -1 && object3 == -1){
					object3 = randomObject();
					obj[object3]++;
					objectsSprites3[object3].setColor(1f, 1f, 1f, 1f); 
				}
				for(int h = 0; h < 5; h++){
					objectsParticles[h + objectsP].setPosition(ball.getPosition().x, ball.getPosition().y);
					objectsParticles[h + objectsP].setColor(1f, 1f, 1f, 1f);
					d = MathUtils.random(0, 1);
					if(d == 0) xx = MathUtils.random(-45, -25);
					else xx = MathUtils.random(25, 45);
					d = MathUtils.random(0, 1);
					if(d == 0) yy = MathUtils.random(-45, -25);
					else yy = MathUtils.random(25, 45);
					Tween.to(objectsParticles[h + objectsP], SpriteAccessor.COLOR, 0.35f).target(1f, 1f, 1f, 0f).start(tweenManager);
					Tween.to(objectsParticles[h + objectsP], SpriteAccessor.POS_XY, 0.35f).
					target(objectsParticles[h + objectsP].getX() + xx, objectsParticles[h + objectsP].getY() + yy).start(tweenManager);
				}
				if(objectsP != 15)	objectsP = objectsP + 5;
				else objectsP = 0;
			}
		}
		return (hit || ball.getPosition().y + 20 <= 0);
	}

	public int randomObject(){
		int o = -1;
		int oAux = MathUtils.random(0, 49);
		if(oAux >= 0 && oAux <= 6)
			o = 0;
		else if(oAux >= 7 && oAux <= 13)
			o = 1;
		else if(oAux >= 14 && oAux <= 17)
			o = 2;
		else if(oAux >= 18 && oAux <= 21)
			o = 3;
		else if(oAux >= 22 && oAux <= 28)
			o = 4;
		else if(oAux >= 29 && oAux <= 34)
			o = 5;
		else if(oAux >= 35 && oAux <= 40)
			o = 6;
		else if(oAux >= 41 && oAux <= 43)
			o = 7;
		else if(oAux >= 44 && oAux <= 46)
			o = 8;
		else if(oAux >= 47 && oAux <= 49)
			o = 9;
		return o;
	}

	public void drawBackground(){
		// draw the second background
		bgs1.setPosition(-(bgs2.getWidth()-Gdx.graphics.getWidth())/2, currentBgY);
		bgs1.draw(game.batch);
		bgs2.setPosition(-(bgs2.getWidth()-Gdx.graphics.getWidth())/2, currentBgY - bgs2.getHeight());
		bgs2.draw(game.batch);
		// draw the first background
		bgs1.setPosition(-(bgs2.getWidth()-Gdx.graphics.getWidth())/2, 
				currentBgY - bgs1.getHeight() - bgs2.getHeight());
		bgs1.draw(game.batch);
		bgs2.setPosition(-(bgs2.getWidth()-Gdx.graphics.getWidth())/2, 
				currentBgY - bgs1.getHeight() - 2*bgs2.getHeight());
		bgs2.draw(game.batch);
		if(state == PLAY){
			// move the separator: speed follows the orbs' speed, frame-rate independent
			currentBgY -= backgroundSpeed(velocityBg)*Gdx.graphics.getDeltaTime();
		}
		// if the separator reaches the screen edge, move it back to the first position
		if(currentBgY <= bgs1.getHeight() - Gdx.graphics.getDeltaTime()) currentBgY = bgs1.getHeight() + 2*bgs2.getHeight();
	}

	/** Orb fall speed right now: the run's speed, halved while the slow item is active. */
	public int currentBallVelocity(){
		return slow ? velocityBall/2 : velocityBall;
	}

	private float[] shieldColor(){
		int st = ship.getState();
		if(st == Spaceship.STATE_RED || st == Spaceship.STATE_REDBLUE)
			return new float[]{1f, 0.23f, 0.36f};
		if(st == Spaceship.STATE_GOLD)
			return new float[]{1f, 0.75f, 0.18f};
		return new float[]{0.13f, 0.83f, 0.93f};
	}

	private float shipCenterX(){
		Sprite sp = ship.shipsSprites[ship.shipSelected];
		return sp.getX() + sp.getWidth()/2;
	}

	private float shipCenterY(){
		Sprite sp = ship.shipsSprites[ship.shipSelected];
		return sp.getY() + sp.getHeight()/2;
	}

	/** Advances the speed director (play state only) and reacts to its events: orb spawning, the charge-up and the burst. */
	private void updateSpeedDirector(){
		float dt = Gdx.graphics.getDeltaTime();
		if(debugKeepAlive && ship.getLifes() < barWidth)
			ship.setLifes(barWidth);
		director.update(dt);
		velocityBall = Math.round(director.speed());
		velocityBg = slow ? Math.round(velocityBall*0.6f) : velocityBall;

		float ct = director.getChargeTime();
		float[] c = shieldColor();
		Sprite sp = ship.shipsSprites[ship.shipSelected];
		float size = sp.getWidth()*sp.getScaleX();
		speedFx.update(dt, shipCenterX(), shipCenterY(), size, ct, c[0], c[1], c[2]);

		if(ct > 0f && !chargeSoundStarted){
			chargeSoundStarted = true;
			if(game.getMMS().sound == game.getMMS().ON)
				chargeSound.play(0.8f);
			if(game.getMMS().vibration == game.getMMS().ON)
				vibrateRamp();
		}
		if(director.consumeBurst()){
			speedFx.burst(shipCenterX(), shipCenterY(), director.getTier());
			for(Ball ball : balls)
				ball.setVelocity(currentBallVelocity());
			if(game.getMMS().sound == game.getMMS().ON)
				boostSound.play(0.9f);
			if(game.getMMS().vibration == game.getMMS().ON)
				Gdx.input.vibrate(140);
		}
		if(director.consumeResume())
			chargeSoundStarted = false;
		ship.setChargeFx(speedFx.plumeLength(ct), speedFx.plumeAlpha(ct), speedFx.plumeWhite(ct));

		// orbs spawn on the director's schedule (the charge-up doesn't interrupt it)
		if(TimeUtils.nanoTime() - lastBallTime > director.spawnGap()*ballSpawnSpeed*1000000000d)
			spawnBall();
	}

	/** Engine rumble that builds during the charge-up: short pulses that come faster and last longer. */
	private void vibrateRamp(){
		float t = 0f;
		float gap = 0.30f;
		int pulse = 22;
		while(t < com.spaceship.handlers.SpeedDirector.BURST_AT - 0.15f){
			final int ms = pulse;
			Timer.schedule(new Task(){
				@Override
				public void run(){
					if(state == PLAY && game.getMMS().vibration == game.getMMS().ON)
						Gdx.input.vibrate(ms);
				}
			}, t);
			t += gap;
			gap = Math.max(0.09f, gap*0.82f);
			pulse = Math.min(70, pulse + 5);
		}
	}

	private boolean debugKeepAlive;

	/** Test hook: keep the ship at full energy (dev driver). */
	public void debugKeepAlive(boolean on){
		debugKeepAlive = on;
	}

	/** Test hook: start the charge-up now (dev driver). */
	public void debugCharge(){
		director.debugCharge();
	}

	/** Test hook: jump straight to a speed tier (dev driver). */
	public void debugSetTier(int t){
		director.debugSetTier(t);
	}

	/**
	 * Background scroll speed in px/s for an orb speed. This is the old step table (2..17 px per 25 ms tick up to orb speed
	 * 850) as a smooth curve, and past 850, where the table used to stop at 18, it keeps growing at the same rate, so the
	 * background keeps pace with the orbs until they reach their top speed.
	 */
	/** Overall background speed relative to the orbs: 1 = the original table's curve (always well below the orbs' own speed). */
	private static final float BACKGROUND_SPEED_MULTIPLIER = 1f;

	public float backgroundSpeed(float v){
		float step;
		if(v <= 400) step = 2f;
		else if(v <= 700) step = 2f + (v - 400f)/25f;
		else step = 14f + (v - 700f)/50f;
		return step*40f*BACKGROUND_SPEED_MULTIPLIER*(1f + 1.6f*speedFx.backgroundBoost());
	}

	/** 0 at the starting orb speed, 1 at the top speed: how fast the run feels (drives the ship's engine trail). */
	public float getSpeedFactor(){
		return MathUtils.clamp((velocityBg - 350f)/900f, 0f, 1f);
	}

	public void loadBgs(int t){
		switch(t){
		case 0:
			bgs1 = new Sprite(game.assetManager.get("res/bgs/bgBlack1.png", Texture.class));
			bgs2 = new Sprite(game.assetManager.get("res/bgs/bgBlack2.png", Texture.class));
			break;
		case 1:
			bgs1 = new Sprite(game.assetManager.get("res/bgs/bgBrownDark1.png", Texture.class));
			bgs2 = new Sprite(game.assetManager.get("res/bgs/bgBrownDark2.png", Texture.class));
			break;
		case 2:
			bgs1 = new Sprite(game.assetManager.get("res/bgs/bgPurple1.png", Texture.class));
			bgs2 = new Sprite(game.assetManager.get("res/bgs/bgPurple2.png", Texture.class));
			break;
		case 3:
			bgs1 = new Sprite(game.assetManager.get("res/bgs/bgBlackRedDark1.png", Texture.class));
			bgs2 = new Sprite(game.assetManager.get("res/bgs/bgBlackRedDark2.png", Texture.class));
			break;
		case 4:
			bgs1 = new Sprite(game.assetManager.get("res/bgs/bgAquaDark1.png", Texture.class));
			bgs2= new Sprite(game.assetManager.get("res/bgs/bgAquaDark2.png", Texture.class));
			break;
		case 5:
			bgs1 = new Sprite(game.assetManager.get("res/bgs/bgPinkDark1.png", Texture.class));
			bgs2 = new Sprite(game.assetManager.get("res/bgs/bgPinkDark2.png", Texture.class));
			break;
		case 6:
			bgs1 = new Sprite(game.assetManager.get("res/bgs/bgBlueDark1.png", Texture.class));
			bgs2 = new Sprite(game.assetManager.get("res/bgs/bgBlueDark2.png", Texture.class));
			break;
		case 7:
			bgs1 = new Sprite(game.assetManager.get("res/bgs/bgDark1.png", Texture.class));
			bgs2 = new Sprite(game.assetManager.get("res/bgs/bgDark2.png", Texture.class));
			break;
		case 8:
			bgs1 = new Sprite(game.assetManager.get("res/bgs/bgBlue1.png", Texture.class));
			bgs2 = new Sprite(game.assetManager.get("res/bgs/bgBlue2.png", Texture.class));
			break;
		case 9:
			bgs1 = new Sprite(game.assetManager.get("res/bgs/bgGreenBlue1.png", Texture.class));
			bgs2 = new Sprite(game.assetManager.get("res/bgs/bgGreenBlue2.png", Texture.class));
			break;
		}
		bgs1.setSize(Gdx.graphics.getWidth(), bgs1.getHeight()*y);
		bgs2.setSize(Gdx.graphics.getWidth(), bgs2.getHeight()*y);
	}

	private void addCatchPopup(Ball ball, int value, boolean blue){
		if(value <= 1) return;
		popX[popNext] = ball.getPosition().x;
		popY[popNext] = ball.getPosition().y + com.spaceship.handlers.FontUtil.bounds(lifesFont, "+1").height;
		popA[popNext] = 1f;
		popValue[popNext] = value;
		popBlue[popNext] = blue;
		popNext = (popNext + 1) % POPUPS;
	}

	private void drawCatchPopups(){
		for(int i = 0; i < POPUPS; i++){
			if(popA[i] <= 0) continue;
			BitmapFont font = popBlue[i] ? bluesFont3 : redsFont3;
			float oldA = font.getColor().a;
			font.setColor(1f, 1f, 1f, popA[i]);
			font.getData().setScale(x, y);
			font.draw(game.batch, "+" + popValue[i], popX[i], popY[i]);
			font.setColor(1f, 1f, 1f, oldA);
			popY[i] += 2.5f;
			popA[i] -= 0.025f;
		}
	}

	public void drawLifeMeter(){
		// gold liquid in a glass tube: eases toward the current energy; the frame sprite (lifeBarSprite) draws on top
		energyBar.update(Gdx.graphics.getDeltaTime(), ship.getLifes()/(float)barWidth);
		energyBar.draw(game.batch, lifeBarSprite, lifeSprites[ship.getLifes()].getColor());
		lifeBarSprite.draw(game.batch);
		speedFx.drawChevrons(game.batch, lifeBarSprite.getX() + lifeBarSprite.getWidth()/2, lifeBarSprite.getY() - fxy(22) - fxy(8),
				fxy(22), director.getTier(), lifeBarSprite.getColor().a);
	}
	private final TweenCallback newHighScoreCallback = new TweenCallback() {
		public void onEvent(int type, BaseTween<?> source) {
			if(tweenEnd)
				Tween.to(greysFont, BitmapFontAccessor.COLOR, 0.2f).target(1f, 1f, 1f, 1f).ease(Sine.INOUT)
				.repeatYoyo(1, 0).setCallback(newHighScoreCallback).start(tweenManager);
		}
	};

	private final TweenCallback winningRedCounterCallback = new TweenCallback() {
		public void onEvent(int type, BaseTween<?> source) {
			if(tweenEnd)
				Tween.to(redsFont, BitmapFontAccessor.COLOR, 0.2f).target(1f, 1f, 1f, 1f).ease(Sine.INOUT)
				.repeatYoyo(1, 0).setCallback(winningRedCounterCallback).start(tweenManager);
		}
	};

	private final TweenCallback winningBlueCounterCallback = new TweenCallback() {
		public void onEvent(int type, BaseTween<?> source) {
			if(tweenEnd)
				Tween.to(bluesFont, BitmapFontAccessor.COLOR, 0.2f).target(1f, 1f, 1f, 1f).ease(Sine.INOUT)
				.repeatYoyo(1, 0).setCallback(winningBlueCounterCallback).start(tweenManager);
		}
	};

	private final TweenCallback lifeMeterChangeCallback = new TweenCallback() {
		public void onEvent(int type, BaseTween<?> source) {
			Tween.to(lifeSprites[ship.getLifes()], SpriteAccessor.COLOR, 0.15f).target(1f, 0.2f, 0.05f, 1f)
			.repeatYoyo(1, 0).ease(Sine.INOUT).start(tweenManager);
		}
	};

	private final TweenCallback lifeMeter1Callback = new TweenCallback() {
		public void onEvent(int type, BaseTween<?> source) {
			Tween.to(lifeSprites[1], SpriteAccessor.COLOR, 0.2f).target(1f, 0.05f, 0.05f, 1f).ease(Sine.INOUT)
			.repeatYoyo(1, 0).setCallback(lifeMeter1Callback).start(tweenManager);
		}
	};

	private final TweenCallback objectCallback = new TweenCallback() {
		public void onEvent(int type, BaseTween<?> source) {	
			if(objectClockEnable && TimeUtils.nanoTime() - objectClock < 2000000000f*game.getMMS().objectsDuration){
				Tween.to(objectsSprites1[auxObj], SpriteAccessor.COLOR, 0.4f).target(1f, 1f, 1f, 0f).repeatYoyo(1, 0).
				setCallback(objectCallback).start(tweenManager);
				if(game.getMMS().sound == game.getMMS().ON && state == PLAY)
					objectCount.play();
			}else if(objectClockEnable && TimeUtils.nanoTime() - objectClock > 2000000000f*game.getMMS().objectsDuration){
				Tween.to(objectsSprites1[auxObj], SpriteAccessor.COLOR, 0.15f).target(1f, 1f, 1f, 0f).repeatYoyo(1, 0).
				setCallback(objectCallback).start(tweenManager);
				if(game.getMMS().sound == game.getMMS().ON && state == PLAY)
					objectCount.play();
			}else{
				if(auxObj != object2)
					Tween.set(objectsSprites1[auxObj], SpriteAccessor.COLOR).target(1f, 1f, 1f, 0f).start(tweenManager);
				if(object2 != -1){
					object1 = object2;
					for(int i = 0; i < 10; i++){
						if(i == object1)
							objectsSprites1[i].setColor(1f, 1f, 1f, 1f);
						else
							objectsSprites1[i].setColor(1f, 1f, 1f, 0f);
					}
					objectsSprites2[object2].setColor(1f, 1f, 1f, 0f);
					object2 = -1;
				}
				if(object3 != -1){
					object2 = object3;
					objectsSprites2[object2].setColor(1f, 1f, 1f, 1f);
					objectsSprites3[object3].setColor(1f, 1f, 1f, 0f);
					object3 = -1;
				}
			}	
		}
	};

	public void updateDatabase(){
		int tempB = 0;
		int tempR = 0;
		try{
			if(finalScore == bestScore){
				game.getDataBase().execSQL("UPDATE seleccionsAndNumbers SET highScore=" + finalScore + ";");
			}
			cursor = game.getDataBase().rawQuery("SELECT blueBalls FROM seleccionsAndNumbers;");
			if(cursor != null && cursor.next())
				tempB = Integer.valueOf(cursor.getInt(0));
			cursor = game.getDataBase().rawQuery("SELECT redBalls FROM seleccionsAndNumbers;");
			if(cursor != null && cursor.next())
				tempR = Integer.valueOf(cursor.getInt(0));
		
			if(reds > blues){ 
				tempB += blues;
				game.getDataBase().execSQL("UPDATE seleccionsAndNumbers SET blueBalls=" + tempB + ";");
				if(blues % 2 == 0)
					tempR += blues/2;
				else
					tempR += (blues - 1)/2;
				game.getDataBase().execSQL("UPDATE seleccionsAndNumbers SET redBalls=" + tempR + ";");
			}else if(blues > reds){
				tempR += reds;
				game.getDataBase().execSQL("UPDATE seleccionsAndNumbers SET redBalls=" + tempR + ";");
				if(reds % 2 == 0)
					tempB += reds/2;
				else
					tempB += (reds - 1)/2;
				game.getDataBase().execSQL("UPDATE seleccionsAndNumbers SET blueBalls=" + tempB + ";");	
			}else{
				tempR += reds;
				tempB += blues;
				game.getDataBase().execSQL("UPDATE seleccionsAndNumbers SET blueBalls=" + tempB + ";");
				game.getDataBase().execSQL("UPDATE seleccionsAndNumbers SET redBalls=" + tempR + ";");
			}
			cursor = game.getDataBase().rawQuery("SELECT lifeBalls FROM seleccionsAndNumbers;");
			if(cursor != null && cursor.next()){
				int tempL = Integer.valueOf(cursor.getInt(0)) + lifes;
				game.getDataBase().execSQL("UPDATE seleccionsAndNumbers SET lifeBalls=" + tempL + ";");
			}	
			cursor.close();
			game.getDataBase().execSQL("UPDATE seleccionsAndNumbers SET sound=" + game.getMMS().sound + ";");
			game.getDataBase().execSQL("UPDATE seleccionsAndNumbers SET vibration=" + game.getMMS().vibration + ";");
			game.getDataBase().execSQL("UPDATE seleccionsAndNumbers SET controls=" + game.getMMS().controls + ";");
			game.getStatsDataBase().execSQL("UPDATE stats SET games=" + games + ";");
			game.getStatsDataBase().execSQL("UPDATE stats SET totalPoints=" + totalPoints + ";");
			game.getStatsDataBase().execSQL("UPDATE stats SET avgPoints=" + avgPoints + ";");
			game.getStatsDataBase().execSQL("UPDATE stats SET totalOrbs=" + totalOrbs + ";");
			game.getStatsDataBase().execSQL("UPDATE stats SET totalBlues=" + totalBlues + ";");
			game.getStatsDataBase().execSQL("UPDATE stats SET totalReds=" + totalReds + ";");
			game.getStatsDataBase().execSQL("UPDATE stats SET totalLifes=" + totalLifes + ";");
			game.getStatsDataBase().execSQL("UPDATE stats SET totalObjects=" + totalObjects + ";");
			for(int i = 0; i < 10; i++){
				game.getStatsDataBase().execSQL("UPDATE stats SET ob" + String.valueOf(i) + "=" + obj[i] + ";");
			}
		}catch (SQLiteGdxException e){
			e.printStackTrace();
		}
	}
	
	private void updateTime(){
		timePlaying = (int) (timePlaying + (TimeUtils.nanoTime() - time)/1000000000);
		try {
			game.getStatsDataBase().execSQL("UPDATE stats SET timePlaying=" + timePlaying + ";");
		} catch (SQLiteGdxException e) {
			e.printStackTrace();
		}
	}

	private void loadStats() {
		try {
			game.getDataBase().execSQL("UPDATE seleccionsAndNumbers SET ffSelected=" + ship.forceFieldSelected + ";");
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
		}catch (SQLiteGdxException e1) {
			e1.printStackTrace();
		}
	}
	
	public void setUpHelpMenu(){
		stageHelp = new Stage();
		containerHelp = new Table();
		stageHelp.addActor(containerHelp);
		containerHelp.setBounds(Gdx.graphics.getWidth()/8 , game.getMMS().getCounterBaseY() + 1.5f*game.getMMS().blueCounter.getHeight(), 
				3*Gdx.graphics.getWidth()/4, Gdx.graphics.getHeight() - 0.225f*Gdx.graphics.getHeight() - adHeight);
		imgsHelp = new Table();
		scrollHelp = new ScrollPane(imgsHelp);
		scrollHelp.setScrollingDisabled(true, false);
		
		if(game.getMMS().language == game.getMMS().ON)
			tutoLabel = new Label("Help", game.getMMS().skin);
		else
			tutoLabel = new Label("Ayuda", game.getMMS().skin);
		tutoLabel.setFontScale(x, y);
		tutoLabel.setSize(fx(tutoLabel.getWidth()), fy(tutoLabel.getHeight()));

		if(game.getMMS().language == game.getMMS().ON)
			padsTitleLabel = new Label("Controls", game.getMMS().skin);
		else 
			padsTitleLabel = new Label("Controles", game.getMMS().skin);
		padsTitleLabel.setFontScale(x/1.5f, y/1.5f);
		padsTitleLabel.setSize(fx(padsTitleLabel.getWidth()/1.5f), fy(padsTitleLabel.getHeight()/1.5f));

		if(game.getMMS().language == game.getMMS().ON)
			orbsLabel = new Label("Orbs",game.getMMS(). skin);
		else
			orbsLabel = new Label("Orbes", game.getMMS().skin);
		orbsLabel.setFontScale(x/1.5f, y/1.5f);
		orbsLabel.setSize(fx(orbsLabel.getWidth()/1.5f), fy(orbsLabel.getHeight()/1.5f));

		if(game.getMMS().language == game.getMMS().ON)
			itemsLabel = new Label("Items", game.getMMS().skin);
		else
			itemsLabel = new Label("Objetos", game.getMMS().skin);
		itemsLabel.setFontScale(x/1.5f, y/1.5f);
		itemsLabel.setSize(fx(itemsLabel.getWidth()/1.5f), fy(itemsLabel.getHeight()/1.5f));

		float helpPad = padsTitleLabel.getHeight()/2.25f;
		
		if(game.getMMS().language == game.getMMS().ON)
			imgsHelp.add(instEN).padBottom(helpPad).width(containerHelp.getWidth() - helpPad).height(((containerHelp.getWidth() - helpPad)*instEN.getHeight())/instEN.getWidth());
		else
			imgsHelp.add(instES).padBottom(helpPad).width(containerHelp.getWidth() - helpPad).height(((containerHelp.getWidth() - helpPad)*instES.getHeight())/instES.getWidth());
		imgsHelp.row();
		
		imgsHelp.add(padsTitleLabel).height(padsTitleLabel.getHeight()).padBottom(helpPad/2);
		imgsHelp.row();
		if(game.getMMS().language == game.getMMS().ON)
			imgsHelp.add(padsControlsEN).padBottom(helpPad).width(containerHelp.getWidth() - helpPad).height(((containerHelp.getWidth() - helpPad)*padsControlsEN.getHeight())/padsControlsEN.getWidth());
		else
			imgsHelp.add(padsControlsES).padBottom(helpPad).width(containerHelp.getWidth() - helpPad).height(((containerHelp.getWidth() - helpPad)*padsControlsES.getHeight())/padsControlsES.getWidth());
		imgsHelp.row();
		
		imgsHelp.add(orbsLabel).height(orbsLabel.getHeight()).padBottom(helpPad/2);
		imgsHelp.row();
		if(game.getMMS().language == game.getMMS().ON)
			imgsHelp.add(orbsEN).padBottom(helpPad).width(containerHelp.getWidth() - helpPad).height(((containerHelp.getWidth() - helpPad)*orbsEN.getHeight())/orbsEN.getWidth());
		else
			imgsHelp.add(orbsES).padBottom(helpPad).width(containerHelp.getWidth() - helpPad).height(((containerHelp.getWidth() - helpPad)*orbsES.getHeight())/orbsES.getWidth());
			imgsHelp.row();
		
		imgsHelp.add(itemsLabel).height(itemsLabel.getHeight()).padBottom(helpPad/2);
		imgsHelp.row();
		if(game.getMMS().language == game.getMMS().ON)
			imgsHelp.add(itemsEN).padBottom(helpPad).width(containerHelp.getWidth() - helpPad).height(((containerHelp.getWidth() - helpPad)*itemsEN.getHeight())/itemsEN.getWidth());
		else
			imgsHelp.add(itemsES).padBottom(helpPad).width(containerHelp.getWidth() - helpPad).height(((containerHelp.getWidth() - helpPad)*itemsES.getHeight())/itemsES.getWidth());
		imgsHelp.row();
		
		Image bBackITuto = new Image(bBack);
		containerHelp.add(bBackITuto).width(bBack.getWidth()).height(bBack.getHeight()).left();
		containerHelp.add(tutoLabel).width(tutoLabel.getWidth()).height(tutoLabel.getHeight()).left().expandX().padLeft(helpPad);
		containerHelp.row();
		containerHelp.add(scrollHelp).width(3*Gdx.graphics.getWidth()/4).height(containerHelp.getHeight() - bBack.getHeight() - 2*helpPad).padTop(helpPad).colspan(2);
	
		bBackITuto.addListener(new ClickListener(){
			public void clicked(InputEvent event, float x, float y){
				Timer.schedule(new Task(){
					@Override
					public void run(){
						state = PAUSE;
					}
				}, 0.35f);
				if(game.getMMS().sound == game.getMMS().ON)
					game.getMMS().buttonSmall.play();
				stageHelp.addAction(moveTo(0, stageHelp.getHeight(), 0.5f));
				Tween.to(bExit2, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f).start(tweenManager);
				Tween.to(bRetry2, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f).start(tweenManager);
				Tween.to(bHome2, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f).start(tweenManager);
				Tween.to(bSoundOn, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f).start(tweenManager);
				Tween.to(bSoundOff, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f).start(tweenManager);
				Tween.to(bVibrationOn, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f).start(tweenManager);
				Tween.to(bVibrationOff, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f).start(tweenManager);
				Tween.to(bControlsOn, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f).start(tweenManager);
				Tween.to(bControlsOff, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f).start(tweenManager);
				Tween.to(bHelp, SpriteAccessor.COLOR, 0.25f).target(1f, 1f, 1f, 1f).start(tweenManager);
			}
		});	
		
//		containerHelp.debug();
//		imgsHelp.debug();
	}

	public int getState() {
		return state;
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
	public void hide(){
		tweenManager.killTarget(ship.shipsSprites[game.getMMS().shipSelected]);
		tweenManager.killTarget(ship.ffSprites[ship.forceFieldSelected]);
		if(lifeSprites != null){
			tweenManager.killTarget(lifeSprites[1]);
			tweenManager.killTarget(lifeSprites[2]);
			lifeSprites[1].setColor(1f, 1f, 1f, 1f);
			lifeSprites[2].setColor(1f, 1f, 1f, 1f);
		}	
		ship.Restore();
	}

	@Override
	public void pause(){
		if(state == DEAD && !updatedDB){
			updateDatabase();
			updateTime();
			updatedDB = true;
		}
		if(state != DEAD){
			state = PAUSE;
			Tween.set(bRetry2, SpriteAccessor.POS_XY).ease(TweenEquations.easeInOutSine).target(game.getMMS().v1.x, game.getMMS().v1.y).start(tweenManager);
			Tween.set(bHome2, SpriteAccessor.POS_XY).ease(TweenEquations.easeInOutSine).target(game.getMMS().v2.x, game.getMMS().v2.y).start(tweenManager);
			Tween.set(bExit2, SpriteAccessor.POS_XY).ease(TweenEquations.easeInOutSine).target(game.getMMS().v3.x, game.getMMS().v3.y).start(tweenManager);
			Tween.set(bVibrationOn, SpriteAccessor.POS_XY).ease(TweenEquations.easeInOutSine).target(game.getMMS().v4.x, game.getMMS().v4.y).start(tweenManager);
			Tween.set(bVibrationOff, SpriteAccessor.POS_XY).ease(TweenEquations.easeInOutSine).target(game.getMMS().v4.x, game.getMMS().v4.y).start(tweenManager);
			Tween.set(bControlsOn, SpriteAccessor.POS_XY).ease(TweenEquations.easeInOutSine).target(game.getMMS().v5.x, game.getMMS().v5.y).start(tweenManager);
			Tween.set(bControlsOff, SpriteAccessor.POS_XY).ease(TweenEquations.easeInOutSine).target(game.getMMS().v5.x, game.getMMS().v5.y).start(tweenManager);
			Tween.set(bSoundOn, SpriteAccessor.POS_XY).ease(TweenEquations.easeInOutSine).target(game.getMMS().v6.x, game.getMMS().v6.y).start(tweenManager);
			Tween.set(bSoundOff, SpriteAccessor.POS_XY).ease(TweenEquations.easeInOutSine).target(game.getMMS().v6.x, game.getMMS().v6.y).start(tweenManager);				
		}	
		if(Gdx.app.getType().equals(ApplicationType.Android))
			game.getActionResolver().giftizOnPause();
	}

	@Override
	public void resume() {
		if(Gdx.app.getType().equals(ApplicationType.Android))
			game.getActionResolver().giftizOnResume();
	}

	@Override
	public void dispose(){

		//		absorb.dispose();
		//		error.dispose();
		//		life.dispose();
		//		button.dispose();
		//		ship.dispose();
		//		for (Ball ball : balls)
		//			ball.dispose();
		//
		//		bluesFont.dispose();
		//		redsFont.dispose();
		//		greysFont.dispose();
		//		greysFont2.dispose();

	} 

}
