package com.spaceship.objects;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenEquations;
import aurelienribon.tweenengine.TweenManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.badlogic.gdx.math.MathUtils;
import com.spaceship.handlers.CircleAccessor;
import com.spaceship.handlers.Fx;
import com.spaceship.handlers.ShipPlume;
import com.spaceship.handlers.SpriteAccessor;
import com.spaceship.main.Main;
import com.spaceship.main.PlayScreen;

public class Spaceship{

	final Main game;
	
	public static final int STATE_BLUE = 0;
	public static final int STATE_RED = 1;
	public static final int STATE_BLUERED = 2;
	public static final int STATE_REDBLUE = 3;
	public static final int STATE_GOLD = 4;  

	private Circle ship;
	
	private TweenManager tweenManager;

	public Texture forceFieldGlows;
	public TextureRegion[] ffTextures;
	public Sprite[] ffSprites;
	public TextureRegion[] ffgTextures;
	public Sprite[] ffgSprites;
	public Texture forceFields;
	private Texture trace1;
	private Texture trace2;
	private Texture trace3;
	private Texture trace4;
	private Texture trace5;
	private Texture ships1;
	private Texture ships2;
	private Texture ships3;
	private Texture ships4;
	private Texture ships5;
	private TextureRegion[] traceTextures1;
	private TextureRegion[] traceTextures2;
	private TextureRegion[] traceTextures3;
	private TextureRegion[] traceTextures4;
	private TextureRegion[] traceTextures5;
	private TextureRegion[] shipsTextures1;
	private TextureRegion[] shipsTextures2;
	private TextureRegion[] shipsTextures3;
	private TextureRegion[] shipsTextures4;
	private TextureRegion[] shipsTextures5;
	public Sprite[] shipsSprites;
	private Sprite[][] trace;

	private boolean[] enabled;
	private int shadow;
	private final ShipPlume plume = new ShipPlume();
	private Sprite[] shockBlue, shockRed;
	private float shockTime = 1f;
	private int[] shockFrame = new int[3];
	private float shockNext;
	private float prevX = -1;
	private int bgRegion;
	public int forceFieldSelected;
	public int forceFieldSelected2;
	public int shipSelected;
	public int lifes;
	public int adHeight;
	public int speed;
		
	public boolean first, canPlay, shieldCan, shieldCan2, shieldCan3, startTrace;
	
	float ratioMotoG = 0.5625f;
	float ratioV = (float) Gdx.graphics.getWidth()/Gdx.graphics.getHeight();
	float x = Math.min(Gdx.graphics.getWidth()/720f, Gdx.graphics.getHeight()/1280f);
	float y = Math.min(Gdx.graphics.getWidth()/720f, Gdx.graphics.getHeight()/1280f);
	
	public Spaceship(float radius, TweenManager tm, Main gam) {
		
		game = gam;
		
		adHeight = game.getMMS().adHeight;
		
		first = true;
		canPlay = false;
		startTrace = false;
		shieldCan = true;
		shieldCan2 = true;
		shieldCan3 = true;
		
		ship = new Circle();	
		ship.x = Gdx.graphics.getWidth()/2;
		ship.y = Gdx.graphics.getHeight()/5 + adHeight/2;
		ship.radius = radius;
		this.tweenManager = tm;

		forceFieldGlows = game.assetManager.get("res/shapes/glows.png", Texture.class);
		ffgTextures = TextureRegion.split(forceFieldGlows, 160, 160)[0];
		ffgSprites = new Sprite[ffgTextures.length];
		for(int i = 0; i < ffgTextures.length; i++){
			ffgSprites[i] = new Sprite(ffgTextures[i]);
			ffgSprites[i].setSize(160, 160);
			ffgSprites[i].setPosition(ship.x - ffgSprites[i].getWidth()/2, ship.y - ffgSprites[i].getHeight()/2);
		}
		
		forceFields = game.assetManager.get("res/shapes/forceFields.png", Texture.class);
		TextureRegion[] lb = TextureRegion.split(game.assetManager.get("res/shapes/lightningBlue.png", Texture.class), 128, 128)[0];
		TextureRegion[] lr = TextureRegion.split(game.assetManager.get("res/shapes/lightningRed.png", Texture.class), 128, 128)[0];
		shockBlue = new Sprite[lb.length];
		shockRed = new Sprite[lr.length];
		for(int i = 0; i < lb.length; i++){
			shockBlue[i] = new Sprite(lb[i]);
			shockRed[i] = new Sprite(lr[i]);
		}
		ffTextures = TextureRegion.split(forceFields, 128, 128)[0];
		ffSprites = new Sprite[ffTextures.length];
		for(int i = 0; i < ffTextures.length; i++){
			ffSprites[i] = new Sprite(ffTextures[i]);
			ffSprites[i].setSize(128, 128);
			ffSprites[i].setPosition(ship.x - ffSprites[i].getWidth()/2, ship.y - ffSprites[i].getHeight()/2);
			
			
		}
		trace1 = game.assetManager.get("res/ships/trace1.png", Texture.class);
		trace2 = game.assetManager.get("res/ships/trace2.png", Texture.class);
		trace3 = game.assetManager.get("res/ships/trace3.png", Texture.class);
		trace4 = game.assetManager.get("res/ships/trace4.png", Texture.class);
		trace5 = game.assetManager.get("res/ships/trace5.png", Texture.class);
		ships1 = game.assetManager.get("res/ships/ships1.png", Texture.class);
		ships2 = game.assetManager.get("res/ships/ships2.png", Texture.class);
		ships3 = game.assetManager.get("res/ships/ships3.png", Texture.class);
		ships4 = game.assetManager.get("res/ships/ships4.png", Texture.class);
		ships5 = game.assetManager.get("res/ships/ships5.png", Texture.class);
		traceTextures1 = TextureRegion.split(trace1, 128, 128)[0];
		traceTextures2 = TextureRegion.split(trace2, 128, 128)[0];
		traceTextures3 = TextureRegion.split(trace3, 128, 128)[0];
		traceTextures4 = TextureRegion.split(trace4, 128, 128)[0];
		traceTextures5 = TextureRegion.split(trace5, 128, 128)[0];
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

			shipsSprites[i].setPosition(ship.x - shipsSprites[i].getWidth()/2, ship.y - shipsSprites[i].getHeight()/2);
		}
		

		shipSelected = game.getMMS().shipSelected;
		speed = 600 + 100*game.getMMS().shipSpeed;
		
		if(Gdx.graphics.getHeight()/7 % 2 == 0)
			trace = new Sprite[traceTextures1.length*5][Gdx.graphics.getHeight()/7];
		else
			trace = new Sprite[traceTextures1.length*5][Gdx.graphics.getHeight()/7+1];
		enabled = new boolean[trace[1].length];
		for(int j = 0; j < traceTextures1.length*5; j++){
			if(j < 15){
				for(int i = 0; i < trace[1].length; i++){
					trace[j][i] = new Sprite(traceTextures1[j]);
					trace[j][i].setColor(1f,  1f,  1f,  0.50f);
					trace[j][i].setSize(128, 128);
					trace[j][i].setScale((fx(128) + fy(128))/2/96, (fx(128) + fy(128))/2/96);
					enabled[i] = false;
				}
			}else if(j < 30){
				for(int i = 0; i < trace[1].length; i++){
					trace[j][i] = new Sprite(traceTextures2[j - 15]);
					trace[j][i].setColor(1f,  1f,  1f,  0.50f);
					trace[j][i].setSize(128, 128);
					trace[j][i].setScale((fx(128) + fy(128))/2/96, (fx(128) + fy(128))/2/96);
					enabled[i] = false;
				}
			}else if(j < 45){
				for(int i = 0; i < trace[1].length; i++){
					trace[j][i] = new Sprite(traceTextures3[j - 30]);
					trace[j][i].setColor(1f,  1f,  1f,  0.50f);
					trace[j][i].setSize(128, 128);
					trace[j][i].setScale((fx(128) + fy(128))/2/96, (fx(128) + fy(128))/2/96);
					enabled[i] = false;
				}
			}else if(j < 60){
				for(int i = 0; i < trace[1].length; i++){
					trace[j][i] = new Sprite(traceTextures4[j - 45]);
					trace[j][i].setColor(1f,  1f,  1f,  0.50f);
					trace[j][i].setSize(128, 128);
					trace[j][i].setScale((fx(128) + fy(128))/2/96, (fx(128) + fy(128))/2/96);
					enabled[i] = false;
				}
			}else{
				for(int i = 0; i < trace[1].length; i++){
					trace[j][i] = new Sprite(traceTextures5[j - 60]);
					trace[j][i].setColor(1f,  1f,  1f,  0.50f);
					trace[j][i].setSize(128, 128);
					trace[j][i].setScale((fx(128) + fy(128))/2/96, (fx(128) + fy(128))/2/96);
					enabled[i] = false;
				}
			}
		}	

		shadow = 0;
		bgRegion = 0;
		
	}
	
	public void setShipScale(){
		for(int i = 0; i < ffSprites.length; i++){

			ffSprites[i].setScale((fx(ffSprites[i].getWidth()) + fy(ffSprites[i].getHeight()))/2/128, 
					(fx(ffSprites[i].getWidth()) + fy(ffSprites[i].getHeight()))/2/128);
			ffgSprites[i].setScale((fx(ffgSprites[i].getWidth()) + fy(ffgSprites[i].getHeight()))/2/160, 
					(fx(ffgSprites[i].getWidth()) + fy(ffgSprites[i].getHeight()))/2/160);
		}
		shipsSprites[shipSelected].setScale((fx(128) + fy(128))/2/128, (fx(128) + fy(128))/2/128);
	}
	
	public void effects(){
		Timeline.createSequence().
		push(Tween.set(ffSprites[forceFieldSelected], SpriteAccessor.SCALE).target(0.05f, 0.05f)).
		push(Tween.set(ffgSprites[forceFieldSelected], SpriteAccessor.SCALE).target(0.05f, 0.05f)).
		push(Tween.set(shipsSprites[shipSelected], SpriteAccessor.SCALE).target(0.05f, 0.05f)).
		push(Tween.set(ship, CircleAccessor.POS_XY).target(ship.x, -ship.radius*2)).
			
		beginParallel().
			push(Tween.to(ffSprites[forceFieldSelected], SpriteAccessor.SCALE, 1.15f).target((fx(ffSprites[forceFieldSelected].getWidth()) + fy(ffSprites[forceFieldSelected].getHeight()))/2/128, 
				(fx(ffSprites[forceFieldSelected].getWidth()) + fy(ffSprites[forceFieldSelected].getHeight()))/2/128)).
			push(Tween.to(ffgSprites[forceFieldSelected], SpriteAccessor.SCALE, 1.15f).target((fx(ffgSprites[forceFieldSelected].getWidth()) + fy(ffgSprites[forceFieldSelected].getHeight()))/2/160, 
				(fx(ffgSprites[forceFieldSelected].getWidth()) + fy(ffgSprites[forceFieldSelected].getHeight()))/2/160)).
			push(Tween.to(shipsSprites[shipSelected], SpriteAccessor.SCALE, 1.15f).target((fx(shipsSprites[shipSelected].getWidth()) + fy(shipsSprites[shipSelected].getHeight()))/2/128, 
				(fx(shipsSprites[shipSelected].getWidth()) + fy(shipsSprites[shipSelected].getHeight()))/2/128)).
				
			push(Tween.to(ship, CircleAccessor.POS_XY, 1.15f).target(ship.x, Gdx.graphics.getHeight()/5 + adHeight/2)).
				
		end().
		start(tweenManager);
		
		Timer.schedule(new Task(){
			@Override
			public void run(){
				canPlay = true;
			}
		}, 1.15f);
		
		for(int i = 0; i < trace[1].length; i++){
			Tween.set(trace[shipSelected][i], SpriteAccessor.SCALE).target((fx(trace[shipSelected][i].getWidth()) + fy(trace[shipSelected][i].getHeight()))/2/128, 
					(fx(trace[shipSelected][i].getWidth()) + fy(trace[shipSelected][i].getHeight()))/2/128).start(tweenManager);
		}
		
	}
	
	public void effectsDeath(){
		Tween.to(shipsSprites[shipSelected], SpriteAccessor.COLOR, 0.50f).target(0f, 0f, 0f, 1f).start(tweenManager);
		if(game.getPS().deathAngle == 0)
			Tween.to(shipsSprites[shipSelected], SpriteAccessor.ROT, 0.75f).target(720f).start(tweenManager);
		else
			Tween.to(shipsSprites[shipSelected], SpriteAccessor.ROT, 0.75f).target(-720f).start(tweenManager);
		Tween.to(shipsSprites[shipSelected], SpriteAccessor.POS_XY, 1.15f).target(game.getPS().deathAngle2, -2*Gdx.graphics.getHeight()/5  + adHeight/2).start(tweenManager);
		Tween.to(ffSprites[forceFieldSelected], SpriteAccessor.SCALE, 0.50f).target(fxy(5), fxy(5)).start(tweenManager);
		Tween.to(ffSprites[forceFieldSelected], SpriteAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f).start(tweenManager);
		Tween.to(ffgSprites[forceFieldSelected], SpriteAccessor.SCALE, 0.50f).target(fxy(5), fxy(5)).start(tweenManager);
		Tween.to(ffgSprites[forceFieldSelected], SpriteAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 0f).start(tweenManager);
		canPlay = false;
	}
	
	public void Restore(){
		ship.x = Gdx.graphics.getWidth()/2;
		shipsSprites[shipSelected].setRotation(0f);
		shipsSprites[shipSelected].setColor(1f, 1f, 1f, 1f);
		ffSprites[forceFieldSelected].setColor(1f, 1f, 1f, 1f);
		Tween.to(ffgSprites[forceFieldSelected], SpriteAccessor.COLOR, 0.50f).target(1f, 1f, 1f, 1f).start(tweenManager);
		
		for(int i = 0; i < trace[1].length; i++){
			enabled[i] = false;
			tweenManager.killTarget(trace[shipSelected][i]);
			trace[shipSelected][i].setColor(1f, 1f, 1f, 0.35f);
		}
		shadow = 0;
		enabled[0] = true;
		startTrace = false;
		shieldCan = true;
		canPlay = false;
	}
	
	public float getRadius(){
		return ship.radius;
	}
	
	public int getState(){
		return forceFieldSelected;
	}
	
	public void setState(int s){
		forceFieldSelected = s;
	}
	
	public int getLifes(){
		return lifes;
	}
	
	public void setLifes(int l){
		lifes = l;
	}
	
	public void update(){
		if(canPlay){
		// finger
			if (Gdx.input.isTouched(0)){
				if(Gdx.input.getX() < PlayScreen.padVerticalLeft.getX() + PlayScreen.padVerticalLeft.getWidth() &&
						Gdx.graphics.getHeight() - Gdx.input.getY() < PlayScreen.padVerticalLeft.getY() + PlayScreen.padVerticalLeft.getHeight()){
					Tween.call(arrowLeftCallback).start(tweenManager);
					if(shipSelected == 54)
						Tween.set(shipsSprites[shipSelected], SpriteAccessor.ROT).target(shipsSprites[shipSelected].getRotation()-3f).start(tweenManager);
					ship.x = (ship.x - fxy(speed) * Gdx.graphics.getDeltaTime());
				}	
				else if(Gdx.input.getX() > PlayScreen.padVerticalRight.getX() &&
						Gdx.graphics.getHeight() - Gdx.input.getY() < PlayScreen.padVerticalRight.getY() + PlayScreen.padVerticalRight.getHeight()){
					Tween.call(arrowRightCallback).start(tweenManager);	
					if(shipSelected == 54)
						Tween.set(shipsSprites[shipSelected], SpriteAccessor.ROT).target(shipsSprites[shipSelected].getRotation()+3f).start(tweenManager);
					ship.x = (ship.x + fxy(speed) * Gdx.graphics.getDeltaTime());
					if(bgRegion > -(PlayScreen.bgs2.getWidth()-Gdx.graphics.getWidth())/2){
						bgRegion -= 1.5;
					}	
				}else if((Gdx.input.getX() >= PlayScreen.padVerticalLeft.getX() + PlayScreen.padVerticalLeft.getWidth()) && 
						(Gdx.input.getX() <= PlayScreen.padVerticalRight.getX() && Gdx.graphics.getHeight() - Gdx.input.getY() < PlayScreen.padHorizontalDown.getY() + PlayScreen.padHorizontalDown.getHeight()) && Gdx.input.justTouched() && shieldCan && shieldCan2){
					if(forceFieldSelected == STATE_BLUE){ 
						tweenRedBig();
						if(game.getMMS().sound == game.getMMS().ON)
							game.getMMS().ffChange.play(1f);
						forceFieldSelected = STATE_RED;
						forceFieldSelected2 = STATE_RED;
					}	
					else if(forceFieldSelected == STATE_RED){ 
						tweenBlueBig();
						if(game.getMMS().sound == game.getMMS().ON)
							game.getMMS().ffChange.play(1f);
						forceFieldSelected = STATE_BLUE;
						forceFieldSelected2 = STATE_BLUE;
					}
					shieldCan = false;
					shieldCan2 = false;
				}
			}else{
				shieldCan = true;
				shieldCan2 = true;
			}	
			
			//second finger
			if(Gdx.input.isTouched(1)){
				if(Gdx.input.getX(1) < PlayScreen.padVerticalLeft.getX() + PlayScreen.padVerticalLeft.getWidth() &&
						Gdx.graphics.getHeight() - Gdx.input.getY(1) < PlayScreen.padVerticalLeft.getY() + PlayScreen.padVerticalLeft.getHeight()){
					Tween.call(arrowLeftCallback).start(tweenManager);		
					if(shipSelected == 54)
						Tween.set(shipsSprites[shipSelected], SpriteAccessor.ROT).target(shipsSprites[shipSelected].getRotation() - 3f).start(tweenManager);
					ship.x = (ship.x - fxy(speed) * Gdx.graphics.getDeltaTime());
				}
				else if(Gdx.input.getX(1) > PlayScreen.padVerticalRight.getX() &&
						Gdx.graphics.getHeight() - Gdx.input.getY(1) < PlayScreen.padVerticalRight.getY() + PlayScreen.padVerticalRight.getHeight()){
					Tween.call(arrowRightCallback).start(tweenManager);
					if(shipSelected == 54)
						Tween.set(shipsSprites[shipSelected], SpriteAccessor.ROT).target(shipsSprites[shipSelected].getRotation() + 3f).start(tweenManager);
					ship.x = (ship.x + fxy(speed) * Gdx.graphics.getDeltaTime());
				}else if((Gdx.input.getX(1) >= PlayScreen.padVerticalLeft.getX() + PlayScreen.padVerticalLeft.getWidth()) && 
						(Gdx.input.getX(1) <= PlayScreen.padVerticalRight.getX() && 
						Gdx.graphics.getHeight() - Gdx.input.getY(1) < PlayScreen.padHorizontalDown.getY() + PlayScreen.padHorizontalDown.getHeight()) && Gdx.input.justTouched() && shieldCan && shieldCan2){
					if(shieldCan && shieldCan3){	
						if(forceFieldSelected == STATE_BLUE){ 
							if(game.getMMS().sound == game.getMMS().ON)
								game.getMMS().ffChange.play(1f);
							tweenRedBig();
							forceFieldSelected = STATE_RED;
							forceFieldSelected2 = STATE_RED;
						}	
						else if(forceFieldSelected == STATE_RED){ 
							if(game.getMMS().sound == game.getMMS().ON)
								game.getMMS().ffChange.play(1f);
							tweenBlueBig();
							forceFieldSelected = STATE_BLUE;
							forceFieldSelected2 = STATE_BLUE;
						}
					}	
					shieldCan = false;
					shieldCan3 = false;
				}
			}else{
				shieldCan = true;
				shieldCan3 = true;
			}
	
			//desktop controls
			if (Gdx.input.isKeyPressed(Keys.LEFT))
				ship.x = (ship.x - fxy(speed) * Gdx.graphics.getDeltaTime());
			if (Gdx.input.isKeyPressed(Keys.RIGHT))
				ship.x = (ship.x + fxy(speed) * Gdx.graphics.getDeltaTime());
		}
		// make sure the ship stays within the screen bounds
		if (ship.x - fxy(64) < 0){
			ship.x = fxy(64);
		}	
		if(ship.x > Gdx.graphics.getWidth() - fxy(64)){
			ship.x = Gdx.graphics.getWidth() - fxy(64);
		}	
	}
	
	public void render(SpriteBatch sb) {
		if(game.getPS().getState() != PlayScreen.DEAD && game.getPS().getState() != PlayScreen.DEADLEAVING){
			shipsSprites[shipSelected].setPosition(ship.x - shipsSprites[shipSelected].getWidth()/2, ship.y - shipsSprites[shipSelected].getHeight()/2);
			ffSprites[forceFieldSelected].setPosition(ship.x - ffSprites[forceFieldSelected].getWidth()/2, ship.y - ffSprites[forceFieldSelected].getHeight()/2);
			ffgSprites[forceFieldSelected].setPosition(ship.x - ffgSprites[forceFieldSelected].getWidth()/2, ship.y - ffgSprites[forceFieldSelected].getHeight()/2);
			if(startTrace){
				float dt = Gdx.graphics.getDeltaTime();
				if(prevX >= 0 && dt > 0)
					plume.setLateral((ship.x - prevX)/(dt*fxy(speed)));
				prevX = ship.x;
				plume.setSpeed(game.getPS().getSpeedFactor());
				Fx.additiveOn(sb);
				plume.draw(sb, shipsSprites[shipSelected], shipSelected, forceFieldSelected, 1f, game.getPS().getState() == game.getPS().PLAY);
				Fx.additiveOff(sb);
			}else{
				prevX = -1;
			}
		}
		ship.setPosition(ship.x, ship.y);
		shipsSprites[shipSelected].draw(sb);
		ffSprites[forceFieldSelected].draw(sb);
		ffgSprites[forceFieldSelected].draw(sb);
		drawShock(sb);
	}
	
	/** Electricity across the force field for a moment after a wrong-colored orb hits it. */
	private void drawShock(SpriteBatch sb){
		if(shockTime >= 0.34f)
			return;
		float dt = Gdx.graphics.getDeltaTime();
		shockTime += dt;
		Sprite[] set = forceFieldSelected == 0 ? shockBlue : shockRed;
		if(shockTime >= shockNext){
			shockNext = shockTime + 0.045f;
			for(int k = 0; k < shockFrame.length; k++){
				shockFrame[k] = MathUtils.random(0, set.length - 1);
				set[shockFrame[k]].setRotation(MathUtils.random(0, 360));
			}
		}
		float fade = shockTime < 0.2f ? 1f : Math.max(0f, 1f - (shockTime - 0.2f)/0.14f);
		float scale = ffSprites[forceFieldSelected].getScaleX()*1.05f;
		Fx.additiveOn(sb);
		for(int k = 0; k < shockFrame.length; k++){
			Sprite b = set[shockFrame[k]];
			b.setScale(scale);
			b.setPosition(ship.x - b.getWidth()/2, ship.y - b.getHeight()/2);
			b.setColor(1f, 1f, 1f, fade*(k == 0 ? 1f : 0.85f));
			b.draw(sb);
		}
		Fx.additiveOff(sb);
	}
	
	private final TweenCallback arrowLeftCallback = new TweenCallback() {
		public void onEvent(int type, BaseTween<?> source) {	
			Tween.to(PlayScreen.arrowLeft, SpriteAccessor.COLOR, 0.1f).target(1f, 1f, 1f, 1f).start(tweenManager);
			Tween.to(PlayScreen.arrowLeft, SpriteAccessor.COLOR, 0.1f).target(1f, 1f, 1f, 0.3f).delay(0.025f).start(tweenManager);
		}
	};
	
	private final TweenCallback arrowRightCallback = new TweenCallback() {
		public void onEvent(int type, BaseTween<?> source) {	
			Tween.to(PlayScreen.arrowRight, SpriteAccessor.COLOR, 0.1f).target(1f, 1f, 1f, 1f).start(tweenManager);
			Tween.to(PlayScreen.arrowRight, SpriteAccessor.COLOR, 0.1f).target(1f, 1f, 1f, 0.3f).delay(0.025f).start(tweenManager);
		}
	};
	
	public void tweenBlueBig(){
		Tween.set(ffSprites[0], SpriteAccessor.SCALE).target(ffSprites[0].getScaleX(), ffSprites[0].getScaleY())
		.ease(TweenEquations.easeInOutSine).start(tweenManager);
		Tween.set(ffSprites[0], SpriteAccessor.COLOR).target(1f, 1f, 1f, 1f)
		.ease(TweenEquations.easeNone).start(tweenManager);
		Tween.to(ffSprites[0], SpriteAccessor.SCALE, 0.115f).target(ffSprites[0].getScaleX()*2, ffSprites[0].getScaleY()*2)
		.ease(TweenEquations.easeInOutSine).start(tweenManager);
		Tween.to(ffSprites[0], SpriteAccessor.COLOR, 0.125f).target(1f, 1f, 1f, 0f)
		.ease(TweenEquations.easeNone).start(tweenManager);
		Tween.set(ffSprites[0], SpriteAccessor.SCALE).target(ffSprites[0].getScaleX(), ffSprites[0].getScaleY()).delay(0.1251f)
		.ease(TweenEquations.easeInOutSine).start(tweenManager);
		Tween.set(ffSprites[0], SpriteAccessor.COLOR).target(1f, 1f, 1f, 1f).delay(0.125f)
		.ease(TweenEquations.easeNone).start(tweenManager);
	}
	
	public void tweenRedBig(){
		Tween.set(ffSprites[1], SpriteAccessor.SCALE).target(ffSprites[1].getScaleX(), ffSprites[1].getScaleY())
		.ease(TweenEquations.easeInOutSine).start(tweenManager);
		Tween.set(ffSprites[1], SpriteAccessor.COLOR).target(1f, 1f, 1f, 1f)
		.ease(TweenEquations.easeNone).start(tweenManager);
		Tween.to(ffSprites[1], SpriteAccessor.SCALE, 0.125f).target(ffSprites[1].getScaleX()*2, ffSprites[1].getScaleY()*2)
		.ease(TweenEquations.easeInOutSine).start(tweenManager);
		Tween.to(ffSprites[1], SpriteAccessor.COLOR, 0.125f).target(1f, 1f, 1f, 0f)
		.ease(TweenEquations.easeNone).start(tweenManager);
		Tween.set(ffSprites[1], SpriteAccessor.SCALE).target(ffSprites[1].getScaleX(), ffSprites[1].getScaleY()).delay(0.125f)
		.ease(TweenEquations.easeInOutSine).start(tweenManager);
		Tween.set(ffSprites[1], SpriteAccessor.COLOR).target(1f, 1f, 1f, 1f).delay(0.125f)
		.ease(TweenEquations.easeNone).start(tweenManager);
	}
	
	public void tweenBlueFF(){
		Tween.set(ffSprites[0], SpriteAccessor.SCALE).target((fx(ffSprites[0].getWidth()) + fy(ffSprites[0].getHeight()))/2/128, 
				(fx(ffSprites[0].getWidth()) + fy(ffSprites[0].getHeight()))/2/128)
		.ease(TweenEquations.easeInOutSine).start(tweenManager);
		Tween.to(ffSprites[0], SpriteAccessor.SCALE, 0.075f).target(ffgSprites[0].getScaleX()*1.15f, ffgSprites[0].getScaleY()*1.15f).repeatYoyo(1, 0)
		.ease(TweenEquations.easeInOutSine).start(tweenManager);
		Tween.set(ffgSprites[0], SpriteAccessor.SCALE).target((fx(ffgSprites[0].getWidth()) + fy(ffgSprites[0].getHeight()))/2/160, 
				(fx(ffgSprites[0].getWidth()) + fy(ffgSprites[0].getHeight()))/2/160)
		.ease(TweenEquations.easeInOutSine).start(tweenManager);
		Tween.to(ffgSprites[0], SpriteAccessor.SCALE, 0.075f).target(ffgSprites[0].getScaleX()*1.15f, ffgSprites[0].getScaleY()*1.15f).repeatYoyo(1, 0)
		.ease(TweenEquations.easeInOutSine).start(tweenManager);
	}
	
	public void tweenRedFF(){
		Tween.set(ffSprites[1], SpriteAccessor.SCALE).target((fx(ffSprites[1].getWidth()) + fy(ffSprites[1].getHeight()))/2/128, 
				(fx(ffSprites[1].getWidth()) + fy(ffSprites[1].getHeight()))/2/128)
		.ease(TweenEquations.easeInOutSine).start(tweenManager);
		Tween.to(ffSprites[1], SpriteAccessor.SCALE, 0.075f).target(ffgSprites[1].getScaleX()*1.15f, ffgSprites[1].getScaleY()*1.15f).repeatYoyo(1, 0)
		.ease(TweenEquations.easeInOutSine).start(tweenManager);
		Tween.set(ffgSprites[1], SpriteAccessor.SCALE).target((fx(ffgSprites[1].getWidth()) + fy(ffgSprites[1].getHeight()))/2/160, 
				(fx(ffgSprites[1].getWidth()) + fy(ffgSprites[1].getHeight()))/2/160)
		.ease(TweenEquations.easeInOutSine).start(tweenManager);
		Tween.to(ffgSprites[1], SpriteAccessor.SCALE, 0.075f).target(ffgSprites[1].getScaleX()*1.15f, ffgSprites[1].getScaleY()*1.15f).repeatYoyo(1, 0)
		.ease(TweenEquations.easeInOutSine).start(tweenManager);
	}
	
	public void tweenRedBlueFF(){
		Tween.set(ffSprites[3], SpriteAccessor.SCALE).target((fx(ffSprites[3].getWidth()) + fy(ffSprites[3].getHeight()))/2/128, 
				(fx(ffSprites[3].getWidth()) + fy(ffSprites[3].getHeight()))/2/128)
		.ease(TweenEquations.easeInOutSine).start(tweenManager);
		Tween.to(ffSprites[3], SpriteAccessor.SCALE, 0.075f).target(ffgSprites[3].getScaleX()*1.15f, ffgSprites[3].getScaleY()*1.15f).repeatYoyo(1, 0)
		.ease(TweenEquations.easeInOutSine).start(tweenManager);
		Tween.set(ffgSprites[3], SpriteAccessor.SCALE).target((fx(ffgSprites[3].getWidth()) + fy(ffgSprites[3].getHeight()))/2/160, 
				(fx(ffgSprites[3].getWidth()) + fy(ffgSprites[3].getHeight()))/2/160)
		.ease(TweenEquations.easeInOutSine).start(tweenManager);
		Tween.to(ffgSprites[3], SpriteAccessor.SCALE, 0.075f).target(ffgSprites[3].getScaleX()*1.15f, ffgSprites[3].getScaleY()*1.15f).repeatYoyo(1, 0)
		.ease(TweenEquations.easeInOutSine).start(tweenManager);
	}
	
	public void tweenBlueRedFF(){
		Tween.set(ffSprites[2], SpriteAccessor.SCALE).target((fx(ffSprites[2].getWidth()) + fy(ffSprites[2].getHeight()))/2/128, 
				(fx(ffSprites[2].getWidth()) + fy(ffSprites[2].getHeight()))/2/128)
		.ease(TweenEquations.easeInOutSine).start(tweenManager);
		Tween.to(ffSprites[2], SpriteAccessor.SCALE, 0.075f).target(ffgSprites[2].getScaleX()*1.15f, ffgSprites[2].getScaleY()*1.15f).repeatYoyo(1, 0)
		.ease(TweenEquations.easeInOutSine).start(tweenManager);
		Tween.set(ffgSprites[2], SpriteAccessor.SCALE).target((fx(ffgSprites[2].getWidth()) + fy(ffgSprites[2].getHeight()))/2/160, 
				(fx(ffgSprites[2].getWidth()) + fy(ffgSprites[2].getHeight()))/2/160)
		.ease(TweenEquations.easeInOutSine).start(tweenManager);
		Tween.to(ffgSprites[2], SpriteAccessor.SCALE, 0.075f).target(ffgSprites[2].getScaleX()*1.15f, ffgSprites[2].getScaleY()*1.15f).repeatYoyo(1, 0)
		.ease(TweenEquations.easeInOutSine).start(tweenManager);
	}
	
	public void tweenGoldFF(){
		Tween.set(ffSprites[4], SpriteAccessor.SCALE).target((fx(ffSprites[2].getWidth()) + fy(ffSprites[4].getHeight()))/2/128, 
				(fx(ffSprites[4].getWidth()) + fy(ffSprites[4].getHeight()))/2/128)
		.ease(TweenEquations.easeInOutSine).start(tweenManager);
		Tween.to(ffSprites[4], SpriteAccessor.SCALE, 0.075f).target(ffgSprites[4].getScaleX()*1.15f, ffgSprites[4].getScaleY()*1.15f).repeatYoyo(1, 0)
		.ease(TweenEquations.easeInOutSine).start(tweenManager);
		Tween.set(ffgSprites[4], SpriteAccessor.SCALE).target((fx(ffgSprites[4].getWidth()) + fy(ffgSprites[4].getHeight()))/2/160, 
				(fx(ffgSprites[4].getWidth()) + fy(ffgSprites[2].getHeight()))/2/160)
		.ease(TweenEquations.easeInOutSine).start(tweenManager);
		Tween.to(ffgSprites[4], SpriteAccessor.SCALE, 0.075f).target(ffgSprites[4].getScaleX()*1.15f, ffgSprites[4].getScaleY()*1.15f).repeatYoyo(1, 0)
		.ease(TweenEquations.easeInOutSine).start(tweenManager);
	}
	
	public void tweenObjectFF(){
		Tween.set(ffSprites[forceFieldSelected], SpriteAccessor.SCALE).target((fx(ffSprites[forceFieldSelected].getWidth()) + fy(ffSprites[forceFieldSelected].getHeight()))/2/128, 
				(fx(ffSprites[forceFieldSelected].getWidth()) + fy(ffSprites[forceFieldSelected].getHeight()))/2/128)
		.ease(TweenEquations.easeInOutSine).start(tweenManager);
		Tween.to(ffSprites[forceFieldSelected], SpriteAccessor.SCALE, 0.075f).target(ffgSprites[forceFieldSelected].getScaleX()*1.15f, ffgSprites[forceFieldSelected].getScaleY()*1.15f).repeatYoyo(1, 0)
		.ease(TweenEquations.easeInOutSine).start(tweenManager);
		Tween.set(ffgSprites[forceFieldSelected], SpriteAccessor.SCALE).target((fx(ffgSprites[forceFieldSelected].getWidth()) + fy(ffgSprites[forceFieldSelected].getHeight()))/2/160, 
				(fx(ffgSprites[forceFieldSelected].getWidth()) + fy(ffgSprites[2].getHeight()))/2/160)
		.ease(TweenEquations.easeInOutSine).start(tweenManager);
		Tween.to(ffgSprites[forceFieldSelected], SpriteAccessor.SCALE, 0.075f).target(ffgSprites[forceFieldSelected].getScaleX()*1.15f, ffgSprites[forceFieldSelected].getScaleY()*1.15f).repeatYoyo(1, 0)
		.ease(TweenEquations.easeInOutSine).start(tweenManager);
	}
	
	public void tweenBlackFF(){
		shockTime = 0f;
		shockNext = 0f;
		Tween.set(ffSprites[forceFieldSelected], SpriteAccessor.SCALE).target((fx(ffSprites[forceFieldSelected].getWidth()) + fy(ffSprites[forceFieldSelected].getHeight()))/2/128, 
				(fx(ffSprites[forceFieldSelected].getWidth()) + fy(ffSprites[forceFieldSelected].getHeight()))/2/128)
		.ease(TweenEquations.easeInOutSine).start(tweenManager);
		Tween.to(ffSprites[forceFieldSelected], SpriteAccessor.SCALE, 0.075f).target(0.65f*ffgSprites[forceFieldSelected].getScaleX()*1.15f, ffgSprites[forceFieldSelected].getScaleY()*0.65f).repeatYoyo(1, 0)
		.ease(TweenEquations.easeInOutSine).start(tweenManager);
		Tween.set(ffgSprites[forceFieldSelected], SpriteAccessor.SCALE).target((fx(ffgSprites[forceFieldSelected].getWidth()) + fy(ffgSprites[forceFieldSelected].getHeight()))/2/160, 
				(fx(ffgSprites[forceFieldSelected].getWidth()) + fy(ffgSprites[forceFieldSelected].getHeight()))/2/160)
		.ease(TweenEquations.easeInOutSine).start(tweenManager);
		Tween.to(ffgSprites[forceFieldSelected], SpriteAccessor.SCALE, 0.075f).target(0.65f*ffgSprites[forceFieldSelected].getScaleX()*1.15f, ffgSprites[forceFieldSelected].getScaleY()*0.65f).repeatYoyo(1, 0)
		.ease(TweenEquations.easeInOutSine).start(tweenManager);
	}
	
	public void tweenYellowFF(){
		Tween.set(ffSprites[forceFieldSelected], SpriteAccessor.SCALE).target((fx(ffSprites[forceFieldSelected].getWidth()) + fy(ffSprites[forceFieldSelected].getHeight()))/2/128, 
				(fx(ffSprites[forceFieldSelected].getWidth()) + fy(ffSprites[forceFieldSelected].getHeight()))/2/128)
		.ease(TweenEquations.easeInOutSine).start(tweenManager);
		Tween.to(ffSprites[forceFieldSelected], SpriteAccessor.SCALE, 0.075f).target(ffgSprites[forceFieldSelected].getScaleX()*1.15f, ffgSprites[forceFieldSelected].getScaleY()*1.15f).repeatYoyo(1, 0)
		.ease(TweenEquations.easeInOutSine).start(tweenManager);
		Tween.set(ffgSprites[forceFieldSelected], SpriteAccessor.SCALE).target((fx(ffgSprites[forceFieldSelected].getWidth()) + fy(ffgSprites[forceFieldSelected].getHeight()))/2/160, 
				(fx(ffgSprites[forceFieldSelected].getWidth()) + fy(ffgSprites[2].getHeight()))/2/160)
		.ease(TweenEquations.easeInOutSine).start(tweenManager);
		Tween.to(ffgSprites[forceFieldSelected], SpriteAccessor.SCALE, 0.075f).target(ffgSprites[forceFieldSelected].getScaleX()*1.15f, ffgSprites[forceFieldSelected].getScaleY()*1.15f).repeatYoyo(1, 0)
		.ease(TweenEquations.easeInOutSine).start(tweenManager);
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

    public Circle getCircle(){
    	return ship;
    }
    
	public void dispose(){
		forceFields.dispose();
		ships1.dispose();
		ships2.dispose();
		ships3.dispose();
		ships4.dispose();
		ships5.dispose();
	}
	
}
