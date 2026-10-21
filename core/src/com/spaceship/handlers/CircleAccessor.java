package com.spaceship.handlers;

import aurelienribon.tweenengine.TweenAccessor;

import com.badlogic.gdx.math.Circle;

public class CircleAccessor implements TweenAccessor<Circle> {
	public static final int POS_XY = 1;      

	@Override     
	public int getValues(Circle target, int tweenType, float[] returnValues) {
		switch (tweenType) {
		case POS_XY:
			returnValues[0] = target.x;
			returnValues[1] = target.y;
			return 2;
		default: assert false;
			return -1;	
		}
	}
	@Override
	public void setValues(Circle target, int tweenType, float[] newValues) {
		switch (tweenType) {
		case POS_XY:
			target.setPosition(newValues[0], newValues[1]);
			break;
		default: assert false;
		}
	}
}
