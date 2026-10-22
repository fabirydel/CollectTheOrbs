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
	/** The starting fall speed (PlayScreen.velocityBall): `angle` is the sideways speed in px/s at this fall speed. */
	private static final float BASE_VELOCITY = 350f;
	private final float phase = MathUtils.random(0f, 6.28f);
	private final float loopOffset = MathUtils.random();
	// positions the orb has passed through (newest first): the tail follows this path, so it bends after a bounce
	private static final int TRAIL_POINTS = 26;
	private final float[] trailX = new float[TRAIL_POINTS], trailY = new float[TRAIL_POINTS];
	private int trailCount;
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
		trailCount = 0;
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
		
		// sideways speed grows with the fall speed, so an orb keeps the same angle of descent for its whole fall
		float xP = (float) (angle*(velocity/BASE_VELOCITY)*Gdx.graphics.getDeltaTime());
		
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
		// energy orb: soft edge + corona, every orb loops the same frames from its own phase
		int kind = type == B_BLUE ? 0 : type == B_RED ? 1 : type == B_LIFE ? 2 : 3;
		Fx.additiveOn(sb);
		Fx.drawOrb(sb, texSelected, kind, loopOffset);
		Fx.additiveOff(sb);
	}
	
	/** Comet tail: a ribbon through the orb's recent path with a head that wraps around the orb (draw between Fx.additiveOn/Off, before the orbs). */
	public void renderTail(SpriteBatch sb) {
		float px = this.getPosition().x;
		float py = this.getPosition().y;
		float vx = -angle*(velocity/BASE_VELOCITY);
		float vy = -fxy(velocity);
		float speed = (float) Math.sqrt(vx*vx + vy*vy);
		if(speed < 1f)
			return;
		float spacing = fxy(7);
		
		// record the path: a new point whenever the orb has moved far enough; a jump (respawn) clears it
		if(trailCount > 0){
			float ddx = px - trailX[0], ddy = py - trailY[0];
			float dist = (float) Math.sqrt(ddx*ddx + ddy*ddy);
			if(dist > fxy(90))
				trailCount = 0;
			else if(dist >= spacing){
				int n = Math.min(trailCount + 1, TRAIL_POINTS);
				for(int k = n - 1; k > 0; k--){
					trailX[k] = trailX[k - 1];
					trailY[k] = trailY[k - 1];
				}
				trailX[0] = px;
				trailY[0] = py;
				trailCount = n;
			}
		}
		if(trailCount == 0){
			trailX[0] = px;
			trailY[0] = py;
			trailCount = 1;
		}
		
		float r, g, b;
		int kind;
		if(type == B_BLUE){ r = 0.13f; g = 0.83f; b = 0.93f; kind = Fx.SPARK_BLUE; }
		else if(type == B_RED){ r = 1f; g = 0.23f; b = 0.36f; kind = Fx.SPARK_RED; }
		else if(type == B_LIFE){ r = 1f; g = 0.75f; b = 0.18f; kind = Fx.SPARK_GOLD; }
		else { r = 0.66f; g = 0.33f; b = 0.97f; kind = Fx.SPARK_PURPLE; }
		float hr = 1f - (1f - r)*0.35f, hg = 1f - (1f - g)*0.35f, hb = 1f - (1f - b)*0.35f;
		
		float t = Fx.time();
		float flicker = !Fx.LIVELY ? 1f : 0.94f + 0.05f*MathUtils.sin(t*19f + phase) + MathUtils.random(-0.015f, 0.015f);
		float orbR = fxy(10)*0.9f;
		float dirX = vx/speed, dirY = vy/speed;
		
		// coma: a soft glow that surrounds the head, pushed slightly ahead so the orb sits inside it
		Texture glow = Fx.spark(kind);
		float cs = orbR*4.6f*flicker;
		sb.setColor(r, g, b, 0.28f);
		sb.draw(glow, px + dirX*orbR*0.35f - cs/2, py + dirY*orbR*0.35f - cs/2, cs, cs);
		
		// ribbon through the path as a chain of soft glow blobs: wide where it wraps the head, tapering to nothing
		int n = trailCount;
		float headW = orbR*6.4f*flicker;
		float ax = px + dirX*orbR*0.55f, ay = py + dirY*orbR*0.55f; // start a little ahead of the orb centre
		int sub = 6;
		for(int k = 0; k < n; k++){
			float bx = k == 0 ? px : trailX[k], by = k == 0 ? py : trailY[k];
			for(int m = 0; m < sub; m++){
				float f = m/(float) sub;
				float cx = ax + (bx - ax)*f, cy = ay + (by - ay)*f;
				float u = (k + f)/TRAIL_POINTS;
				float taper = (float) Math.pow(Math.max(1f - u, 0f), 0.75f);
				float fade = (float) Math.pow(Math.max(1f - u, 0f), 1.3f)*(0.3f + 0.7f*Math.min(1f, u/0.16f)); // dimmer at the head so the orb stays visible
				float w = headW*taper;
				sb.setColor(r, g, b, 0.17f*fade);
				sb.draw(glow, cx - w/2, cy - w/2, w, w);
				float wc = w*0.5f;
				sb.setColor(hr, hg, hb, 0.24f*fade);
				sb.draw(glow, cx - wc/2, cy - wc/2, wc, wc);
			}
			ax = bx;
			ay = by;
		}
		sb.setColor(1f, 1f, 1f, 1f);
		
		if(Fx.LIVELY && game.getPS().getState() == game.getPS().PLAY && MathUtils.random() < Gdx.graphics.getDeltaTime()*22f && n > 2){
			int k = MathUtils.random(1, n - 1);
			float white = MathUtils.random(0f, 0.5f);
			Fx.emit(trailX[k] + MathUtils.random(-0.3f, 0.3f)*headW, trailY[k], MathUtils.random(-22f, 22f)*fxy(1), MathUtils.random(-10f, 26f)*fxy(1),
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
