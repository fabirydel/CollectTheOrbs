package com.spaceship.objects;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.spaceship.handlers.Fx;
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
	private final float phase = MathUtils.random(0f, 6.28f);
	private int type;
	private boolean velocityChanged;
	private Texture texB; 
	private Sprite texSpriteB;
	private Texture texR; 
	private Sprite texSpriteR;
	private Texture texY; 
	private Sprite texSpriteY;
	private Texture texO; 
	private Sprite texSpriteO;
	
	private Sprite texSelected;
	
	float ratioMotoG = 0.5625f;
	float ratioV = (float) Gdx.graphics.getWidth()/Gdx.graphics.getHeight();
	float x = Math.min(Gdx.graphics.getWidth()/720f, Gdx.graphics.getHeight()/1280f);
	float y = Math.min(Gdx.graphics.getWidth()/720f, Gdx.graphics.getHeight()/1280f);
	
	
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
		texR = game.assetManager.get("res/shapes/redBallParticle.png", Texture.class);
		texY = game.assetManager.get("res/shapes/lifeBall.png", Texture.class);
		texO = game.assetManager.get("res/shapes/objectBall.png", Texture.class);
		
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
		
		if(type >= 0 && type <= 28){
			texSelected = texSpriteB;
			this.type = B_BLUE;
		}else if(type >= 29 && type <= 57){
			texSelected = texSpriteR;
			this.type = B_RED;
		}else if(type >= 58 && type <= 59){
			texSelected = texSpriteY;
			this.type = B_LIFE;
		}else{
			texSelected = texSpriteO;
			this.type = B_OBJECT;
		}
	}
	
	public void changeBlue(){
		if(this.type == B_RED){
			texSelected = texSpriteB;
			this.type = B_BLUE;
		}
	}
	
	public void changeRed(){
		if(this.type == B_BLUE){
			texSelected = texSpriteR;
			this.type = B_RED;
		}
	}
	
	public void reset(){
		ball.setPosition(MathUtils.random((fx(20) + fy(20))/2, Gdx.graphics.getWidth() - (fx(20) + fy(20))/2),
				Gdx.graphics.getHeight() + fxy(20));
		taken = false;
		velocity = game.getPS().velocityBall;
		angle = (int) MathUtils.random(-fxy(250), fxy(250));
		angle2 = angle;
		
		int t = MathUtils.random(0, 60);
		if(t >= 0 && t <= 28){
			texSelected = texSpriteB;
			this.type = B_BLUE;
		}else if(t >= 29 && t <= 57){
			texSelected = texSpriteR;
			this.type = B_RED;
		}else if(t >= 58 && t <= 59){
			texSelected = texSpriteY;
			this.type = B_LIFE;
		}else{
			texSelected = texSpriteO;
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
		texSelected.draw(sb);
	}
	
	/** Comet tail behind the orb (draw between Fx.additiveOn/Off, before the orbs). */
	public void renderTail(SpriteBatch sb) {
		float vx = angle;
		float vy = fxy(velocity);
		float speed = (float) Math.sqrt(vx*vx + vy*vy);
		if(speed < 1f)
			return;
		float r, g, b;
		if(type == B_BLUE){ r = 0.13f; g = 0.83f; b = 0.93f; }
		else if(type == B_RED){ r = 1f; g = 0.23f; b = 0.36f; }
		else if(type == B_LIFE){ r = 1f; g = 0.75f; b = 0.18f; }
		else { r = 0.66f; g = 0.33f; b = 0.97f; }
		
		// a little life: the tail wavers, breathes, and sheds sparkles
		float t = Fx.time();
		float wobble = !Fx.LIVELY ? 0f : (1.6f*MathUtils.sin(t*12f + phase*1.3f) + MathUtils.random(-0.3f, 0.3f))*MathUtils.degreesToRadians;
		float c = MathUtils.cos(wobble), s = MathUtils.sin(wobble);
		float dx = (vx*c - vy*s)/speed;
		float dy = (vx*s + vy*c)/speed;
		float flicker = !Fx.LIVELY ? 1f : 0.94f + 0.05f*MathUtils.sin(t*19f + phase) + MathUtils.random(-0.015f, 0.015f);
		float length = Math.min(fxy(230), speed*0.34f)*flicker;
		float width = fxy(30)*(!Fx.LIVELY ? 1f : 0.94f + 0.06f*MathUtils.sin(t*26f + phase));
		float px = this.getPosition().x;
		float py = this.getPosition().y;
		Fx.tail(sb, false, px, py, dx, dy, length, width, r, g, b, 0.9f);
		Fx.tail(sb, true, px, py, dx, dy, length*0.6f, width*0.45f, 1f - (1f - r)*0.35f, 1f - (1f - g)*0.35f, 1f - (1f - b)*0.35f, 0.85f);
		
		if(Fx.LIVELY && game.getPS().getState() == game.getPS().PLAY && MathUtils.random() < Gdx.graphics.getDeltaTime()*22f){
			float d = MathUtils.random(0.08f, 0.6f)*length;
			float white = MathUtils.random(0f, 0.5f);
			Fx.emit(px + dx*d + MathUtils.random(-0.3f, 0.3f)*width, py + dy*d, MathUtils.random(-22f, 22f)*fxy(1), MathUtils.random(-10f, 26f)*fxy(1),
					MathUtils.random(0.3f, 0.65f), MathUtils.random(3.5f, 7.5f)*fxy(1),
					r + (1f - r)*white, g + (1f - g)*white, b + (1f - b)*white);
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
    	return x*v;
	}
    
    public float fy(float v){
    	return y*v;
	}
    
    public float fxy(float v){
    	return (fx(v) + fy(v))/2;
	}
	
	public void dispose(){
		texB.dispose();
		texR.dispose();
		texY.dispose();
		texO.dispose();
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
