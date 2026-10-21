package com.spaceship.handlers;

import aurelienribon.tweenengine.TweenAccessor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

public class ImageAccessor implements TweenAccessor<Image> {
	
	public static final int POS_XY = 1;  
	public static final int ROT = 2;
	public static final int COLOR = 3;   
	public static final int SCALE = 4; 
	public static final int SKEW_X2X3 = 5;     

	@Override     
	public int getValues(Image target, int tweenType, float[] returnValues) {
		switch (tweenType) {
		case POS_XY:
			returnValues[0] = target.getX();
			returnValues[1] = target.getY();
			return 2;
		case ROT:
			returnValues[0] = target.getRotation();
			return 1;	
		case SCALE:
			returnValues[0] = target.getScaleX();
			returnValues[1] = target.getScaleY();
			return 2;		
		case COLOR:
			returnValues[0] = target.getColor().r;
			returnValues[1] = target.getColor().g;
			returnValues[2] = target.getColor().b;
			returnValues[3] = target.getColor().a;
			return 4;
		default: assert false;
			return -1;	
		}
	}
	@Override
	public void setValues(Image target, int tweenType, float[] newValues) {
		switch (tweenType) {
		case POS_XY:
			target.setPosition(newValues[0], newValues[1]);
			break;
		case ROT:
			target.setRotation(newValues[0]);
			break;
		case SCALE:
			target.setScale(newValues[0], newValues[1]);
			break;
		case COLOR:
			Color c = target.getColor();
			c.set(newValues[0], newValues[1], newValues[2], newValues[3]);
			target.setColor(c);
			break;
		default: assert false;
		}
	}
}


