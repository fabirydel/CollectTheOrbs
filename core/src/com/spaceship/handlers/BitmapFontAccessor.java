package com.spaceship.handlers;

import aurelienribon.tweenengine.TweenAccessor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class BitmapFontAccessor implements TweenAccessor<BitmapFont> {
	
	public static final int COLOR = 1;   
	public static final int SCALE = 2;    

	@Override     
	public int getValues(BitmapFont target, int tweenType, float[] returnValues) {
		switch (tweenType) {
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
	public void setValues(BitmapFont target, int tweenType, float[] newValues) {
		switch (tweenType) {
		case SCALE:
			target.getData().setScale(newValues[0], newValues[1]);
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