package com.spaceship.objects;

import com.badlogic.gdx.math.Vector2;

public class GameObject {

	protected float width;
	protected float height;
	
	protected final Vector2 position;
	
	protected float radius;

	public GameObject (float x, float y, float radius) {
		this.position = new Vector2(x, y);	
		this.radius = radius;
	}
	
	public Vector2 getPosition(){
		return position;
	}
	
	public float getRadius(){
		return radius;
	}
	
	public void setPosition(float x, float y){
		this.position.x = x;
		this.position.y = y;
	}
	
	public void setX(float x){
		this.position.x = x;
	}
	
	public void setY(float y){
		this.position.y = y;
	}
	public float getWidth() { return width; }
	public float getHeight() { return height; }
}
