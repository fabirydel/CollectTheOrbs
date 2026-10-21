package com.spaceship.objects;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.spaceship.handlers.SpriteAccessor;
import com.spaceship.main.Main;

public class Ball extends GameObject {
	
	final Main game;

	public static final int B_BLUE = 0;
	public static final int B_RED = 2;
	public static final int B_LIFE = 4;
	public static final int B_OBJECT = 6;
	
	private TweenManager tweenManager;
	
	private Circle ball; 
	
	public boolean taken;
	
	private int velocity;
	private int angle;
	private int angle2;
	private int type;
	private int shadow;
	private int velRest;
	private boolean velocityChanged;
	private Texture texB; 
	private Sprite texSpriteB;
	private Sprite[] traceB;
	private Texture texR; 
	private Sprite texSpriteR;
	private Sprite[] traceR;
	private Texture texY; 
	private Sprite texSpriteY;
	private Sprite[] traceY;
	private Texture texO; 
	private Sprite texSpriteO;
	private Sprite[] traceO;
	private boolean[] enabled;
	private Texture shadowTexB;
	private Texture shadowTexR;
	private Texture shadowTexY;
	private Texture shadowTexO;
	
	private Sprite texSelected;
	private Sprite[] traceSelected;
	
	float ratioMotoG = 0.5625f;
	float ratioV = (float) Gdx.graphics.getWidth()/Gdx.graphics.getHeight();
	float x = Gdx.graphics.getWidth()/720f;
	float y = Gdx.graphics.getHeight()/1280f;
	
	boolean firstTween[];
	
	public Ball(float x, float y, float radius, float type, int velocity, TweenManager tm, Main gam) {
		super(x, y, radius);
		
		game = gam;
		
		this.tweenManager = tm;
		this.velocity = velocity;	
		this.velocityChanged = false;	
		
		ball = new Circle();
		ball.x = x;
		ball.y = y;
		ball.radius = radius;
		angle = (int) MathUtils.random(-fxy(250), fxy(250));
		angle2 = angle;
		
		taken = false;

		texB = game.assetManager.get("res/shapes/blueBall.png", Texture.class);
		shadowTexB = game.assetManager.get("res/shapes/shadowBlue.png", Texture.class);
		texR = game.assetManager.get("res/shapes/redBallParticle.png", Texture.class);
		shadowTexR = game.assetManager.get("res/shapes/redBallParticle.png", Texture.class);
		texY = game.assetManager.get("res/shapes/lifeBall.png", Texture.class);
		shadowTexY = game.assetManager.get("res/shapes/shadowLife.png", Texture.class);
		texO = game.assetManager.get("res/shapes/objectBall.png", Texture.class);
		shadowTexO = game.assetManager.get("res/shapes/shadowObject.png", Texture.class);
		
		texSpriteB = new Sprite(texB);
		texSpriteR = new Sprite(texR);
		texSpriteY = new Sprite(texY);
		texSpriteO = new Sprite(texO);
		texSpriteB.setSize(20, 20);
		texSpriteB.setScale(fxy(20)/20, fxy(20)/20);
		texSpriteR.setSize(20, 20);
		texSpriteR.setScale(fxy(20)/20, fxy(20)/20);
		texSpriteY.setSize(20, 20);
		texSpriteY.setScale(fxy(20)/20, fxy(20)/20);
		texSpriteO.setSize(20, 20);
		texSpriteO.setScale(fxy(20)/20, fxy(20)/20);
		
		traceB = new Sprite[40];
		traceR = new Sprite[40];
		traceY = new Sprite[40];
		traceO = new Sprite[40];
		traceSelected = new Sprite[40];
		enabled = new boolean[traceB.length];
		firstTween = new boolean[traceB.length];
		for(int i = 0; i < traceB.length; i++){
			traceB[i] = new Sprite(shadowTexB);
			traceB[i].setColor(1f, 1f, 1f, 0.20f);
			traceB[i].setSize(20, 20);
			traceB[i].setScale(fxy(20)/20, fxy(20)/20);
			traceR[i] = new Sprite(shadowTexR);
			traceR[i].setColor(1f, 1f, 1f, 0.20f);
			traceR[i].setSize(20, 20);
			traceR[i].setScale(fxy(20)/20, fxy(20)/20);
			traceY[i] = new Sprite(shadowTexY);
			traceY[i].setColor(1f, 1f, 1f, 0.20f);
			traceY[i].setSize(20, 20);
			traceY[i].setScale(fxy(20)/20, fxy(20)/20);
			traceO[i] = new Sprite(shadowTexO);
			traceO[i].setColor(1f, 1f, 1f, 0.20f);
			traceO[i].setSize(20, 20);
			traceO[i].setScale(fxy(20)/20, fxy(20)/20);
			enabled[i] = false;
			firstTween[i] = true;
		}
		
		shadow = 0;
		velRest = 0;
		
		if(type >= 0 && type <= 28){
			texSelected = texSpriteB;
			for(int i = 0; i < traceB.length; i++){
				traceSelected[i] = traceB[i]; 
			}
			this.type = B_BLUE;
		}else if(type >= 29 && type <= 57){
			texSelected = texSpriteR;
			for(int i = 0; i < traceB.length; i++){
				traceSelected[i] = traceR[i]; 
			}
			this.type = B_RED;
		}else if(type >= 58 && type <= 59){
			texSelected = texSpriteY;
			for(int i = 0; i < traceB.length; i++){
				traceSelected[i] = traceY[i]; 
			}
			this.type = B_LIFE;
		}else{
			texSelected = texSpriteO;
			for(int i = 0; i < traceO.length; i++){
				traceSelected[i] = traceO[i]; 
			}
			this.type = B_OBJECT;
		}
	}
	
	public void changeBlue(){
		if(this.type == B_RED){
			texSelected = texSpriteB;
			for(int i = 0; i < traceB.length; i++){
				traceSelected[i] = traceB[i]; 
			}
			this.type = B_BLUE;
		}
	}
	
	public void changeRed(){
		if(this.type == B_BLUE){
			texSelected = texSpriteR;
			for(int i = 0; i < traceB.length; i++){
				traceSelected[i] = traceR[i]; 
			}
			this.type = B_RED;
		}
	}
	
	public void reset(){
		ball.setPosition(MathUtils.random((fx(20) + fy(20))/2, Gdx.graphics.getWidth() - (fx(20) + fy(20))/2),
				Gdx.graphics.getHeight() + fxy(20));
		for(int i = 0; i < traceSelected.length; i++){
			enabled[i] = false;
			firstTween[i] = true;
			tweenManager.killTarget(traceSelected[i]);
			traceSelected[i].setColor(1f, 1f, 1f, 0.20f);
		}
		taken = false;
		shadow = 0;
		velRest = 0;
		velocity = game.getPS().velocityBall;
		angle = (int) MathUtils.random(-fxy(250), fxy(250));
		angle2 = angle;
		
		int t = MathUtils.random(0, 60);
		if(t >= 0 && t <= 28){
			texSelected = texSpriteB;
			for(int i = 0; i < traceB.length; i++){
				traceSelected[i] = traceB[i]; 
			}
			this.type = B_BLUE;
		}else if(t >= 29 && t <= 57){
			texSelected = texSpriteR;
			for(int i = 0; i < traceB.length; i++){
				traceSelected[i] = traceR[i]; 
			}
			this.type = B_RED;
		}else if(t >= 58 && t <= 59){
			texSelected = texSpriteY;
			for(int i = 0; i < traceB.length; i++){
				traceSelected[i] = traceY[i]; 
			}
			this.type = B_LIFE;
		}else{
			texSelected = texSpriteO;
			for(int i = 0; i < traceO.length; i++){
				traceSelected[i] = traceO[i]; 
			}
			this.type = B_OBJECT;
		}
	}
	 
	public void moveBall(){
		
		float yP = fxy(velocity*Gdx.graphics.getDeltaTime()); 
		
		float xP = (float) (angle*Gdx.graphics.getDeltaTime());
		
		this.setPosition(this.getPosition().x  - xP, this.getPosition().y -  yP);
		
		if(this.getPosition().x - texSelected.getWidth()/2 <= 0 | this.getPosition().x >= Gdx.graphics.getWidth() - fxy(20)){
			angle = - angle;
			angle2 = - angle2;
		}
		
	}
	
	public void update(float dt) {
	}
	
	public void render(SpriteBatch sb) {	
		ball.setPosition(this.getPosition().x, this.getPosition().y);
		texSelected.setPosition(this.getPosition().x - texSelected.getWidth()/2, this.getPosition().y - texSelected.getHeight()/2);
		
		if(shadow < traceSelected.length)
			traceSelected[shadow].setPosition(texSelected.getX(), texSelected.getY() - velRest);

		texSelected.draw(sb);
		for(int i = 0; i < traceSelected.length; i++){
			if(enabled[i]){
				traceSelected[i].draw(sb);
			}
		}

		if(game.getPS().getState() == game.getPS().PLAY){
			
			if(shadow < traceSelected.length)
				Tween.to(traceSelected[shadow], SpriteAccessor.COLOR, 0.40f).target(1f, 1f, 1f, 0.02f).start(tweenManager);
			if(shadow+1 < traceSelected.length/2){
				shadow++;	
				enabled[shadow] = true;	
				enabled[shadow + traceSelected.length/2] = false;
				tweenManager.killTarget(traceSelected[shadow + traceSelected.length/2]);
				traceSelected[shadow + traceSelected.length/2].setColor(1f, 1f, 1f, 0.20f);
			}else if(shadow+1 < traceSelected.length){
				shadow++;
				enabled[shadow] = true;	
				enabled[shadow - traceSelected.length/2] = false;
				tweenManager.killTarget(traceSelected[shadow - traceSelected.length/2]);
				traceSelected[shadow - traceSelected.length/2].setColor(1f, 1f, 1f, 0.20f);
			}else{
				shadow = 0;
				enabled[shadow] = true;	
				enabled[shadow + traceSelected.length/2] = false;
				tweenManager.killTarget(traceSelected[shadow + traceSelected.length/2]);
				traceSelected[shadow + traceSelected.length/2].setColor(1f, 1f, 1f, 0.20f);
			}
			
		}	
	
	}
	
	public void effectRemove(){
		Tween.to(texSelected, SpriteAccessor.SCALE, 0.25f).target(2.0f, 2.0f).start(tweenManager);
	}
	
	public int getAngle(){
		return angle;
	}
	
	public void setAngle(int a){
		this.angle = a;
	}
	
	public int getType(){
		return type;
	}
	
	public void setVelocity(int v){
		this.velocity = v;
	}
	
	public int getVelocity(){
		return this.velocity;
	}
	
    public Circle getCircle(){
    	return ball;
    }
	
	public float fx(float v){
    	return x*ratioMotoG*v/ratioV;
	}
    
    public float fy(float v){
    	return y*ratioMotoG*v/ratioV;
	}
    
    public float fxy(float v){
    	return (fx(v) + fy(v))/2;
	}
	
	public void dispose(){
		texB.dispose();
		shadowTexB.dispose();
		texR.dispose();
		shadowTexR.dispose();
		texY.dispose();
		shadowTexY.dispose();
		texO.dispose();
		shadowTexO.dispose();
	}

	public boolean isVelocityChanged() {
		return velocityChanged;
	}

	public void setVelocityChanged(boolean velocityChanged) {
		this.velocityChanged = velocityChanged;
	}

	public int getAngle2() {
		return angle2;
	}

	public void setAngle2(int angle2) {
		this.angle2 = angle2;
	}

}
