package com.spaceship.handlers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

/**
 * Life on top of the scrolling galaxy picture: three layers of twinkling stars that drift at different speeds (nearer =
 * bigger and faster, so the background gains depth), a few huge faint dust clouds that wander slowly, and a shooting
 * star now and then. Everything is drawn additively with the shared glow dot, in screen pixels.
 */
public class Starfield {
	private static final int LAYERS = 3;
	private static final int[] COUNT = { 46, 24, 8 };
	/** star size in 360ths of the screen width */
	private static final float[] SIZE = { 2.6f, 3.8f, 5.4f };
	private static final float[] ALPHA = { 0.45f, 0.55f, 0.65f };
	/** how much faster than the galaxy picture each layer scrolls (the picture is the farthest thing) */
	private static final float[] PARALLAX = { 0.12f, 0.30f, 0.65f };
	/** downward drift in screen heights per second when the picture stands still */
	private static final float[] DRIFT = { 0.004f, 0.008f, 0.016f };

	private final float[][] sx = new float[LAYERS][], sy = new float[LAYERS][], phase = new float[LAYERS][], rate = new float[LAYERS][], scale = new float[LAYERS][];
	private final int[][] tint = new int[LAYERS][];
	private static final float[][] TINTS = { { 1f, 1f, 1f }, { 0.75f, 0.86f, 1f }, { 1f, 0.9f, 0.76f }, { 0.86f, 0.8f, 1f } };

	private static final int CLOUDS = 3;
	private final float[] cx = new float[CLOUDS], cy = new float[CLOUDS], cs = new float[CLOUDS], cp = new float[CLOUDS];
	private static final float[][] CLOUD_TINT = { { 0.45f, 0.6f, 1f }, { 0.8f, 0.5f, 1f }, { 0.5f, 0.9f, 1f } };

	// the shooting star
	private float nextShot, shotT = -1f, shotX, shotY, shotDx, shotDy, shotLife;

	private float time;
	/** 0..1: the stars stretch into streaks, the nearer the longer (the rush right after a speed-up) */
	public float warp;

	public Starfield() {
		for (int l = 0; l < LAYERS; l++) {
			int n = COUNT[l];
			sx[l] = new float[n];
			sy[l] = new float[n];
			phase[l] = new float[n];
			rate[l] = new float[n];
			scale[l] = new float[n];
			tint[l] = new int[n];
			for (int i = 0; i < n; i++) {
				sx[l][i] = MathUtils.random();
				sy[l][i] = MathUtils.random();
				phase[l][i] = MathUtils.random(MathUtils.PI2);
				rate[l][i] = MathUtils.random(0.6f, 2.4f);
				scale[l][i] = MathUtils.random(0.6f, 1.25f);
				tint[l][i] = MathUtils.random(3) == 0 ? MathUtils.random(1, 3) : 0;
			}
		}
		for (int i = 0; i < CLOUDS; i++) {
			cx[i] = MathUtils.random();
			cy[i] = (i + MathUtils.random())/CLOUDS;
			cs[i] = MathUtils.random(0.9f, 1.5f);
			cp[i] = MathUtils.random(MathUtils.PI2);
		}
		nextShot = MathUtils.random(4f, 9f);
	}

	/** @param scroll how fast the galaxy picture is moving down, in pixels per second (0 when it stands still) */
	public void update(float dt, float scroll) {
		if (dt > 0.1f)
			dt = 0.1f;
		time += dt;
		float h = Gdx.graphics.getHeight();
		for (int l = 0; l < LAYERS; l++) {
			float v = (scroll*PARALLAX[l])/h + DRIFT[l];
			for (int i = 0; i < sy[l].length; i++) {
				sy[l][i] -= v*dt;
				if (sy[l][i] < -0.03f) {
					sy[l][i] += 1.06f;
					sx[l][i] = MathUtils.random();
				}
			}
		}
		for (int i = 0; i < CLOUDS; i++) {
			cy[i] -= ((scroll*0.06f)/h + 0.0025f)*dt;
			if (cy[i] < -0.6f) {
				cy[i] += 2.2f;
				cx[i] = MathUtils.random();
			}
		}
		if (shotT >= 0f) {
			shotT += dt;
			if (shotT > shotLife)
				shotT = -1f;
		} else {
			nextShot -= dt;
			if (nextShot <= 0f) {
				nextShot = MathUtils.random(7f, 16f);
				boolean right = MathUtils.randomBoolean();
				shotX = right ? MathUtils.random(-0.1f, 0.5f) : MathUtils.random(0.5f, 1.1f);
				shotY = MathUtils.random(0.55f, 1.05f);
				float ang = MathUtils.random(18f, 38f)*MathUtils.degreesToRadians;
				shotDx = (right ? 1f : -1f)*MathUtils.cos(ang);
				shotDy = -MathUtils.sin(ang);
				shotLife = MathUtils.random(0.55f, 0.85f);
				shotT = 0f;
			}
		}
	}

	/** Draw over the galaxy picture, inside the batch. alpha fades the whole field with the picture. */
	public void draw(SpriteBatch sb, float alpha) {
		if (alpha <= 0.01f || Fx.glow() == null)
			return;
		float w = Gdx.graphics.getWidth(), h = Gdx.graphics.getHeight();
		float unit = w/360f;
		Fx.additiveOn(sb);
		// dust clouds
		for (int i = 0; i < CLOUDS; i++) {
			float s = cs[i]*w*1.5f;
			float x = (cx[i] + 0.08f*MathUtils.sin(time*0.05f + cp[i]))*w, y = cy[i]*h;
			float a = alpha*(0.055f + 0.02f*MathUtils.sin(time*0.21f + cp[i]));
			float[] c = CLOUD_TINT[i % CLOUD_TINT.length];
			sb.setColor(c[0], c[1], c[2], a);
			sb.draw(Fx.glow(), x - s/2, y - s/2, s, s);
		}
		// stars
		for (int l = 0; l < LAYERS; l++) {
			for (int i = 0; i < sx[l].length; i++) {
				float tw = 0.62f + 0.38f*MathUtils.sin(time*rate[l][i] + phase[l][i]);
				float s = SIZE[l]*scale[l][i]*unit;
				float[] c = TINTS[tint[l][i]];
				float x = sx[l][i]*w, y = sy[l][i]*h;
				sb.setColor(c[0], c[1], c[2], alpha*ALPHA[l]*tw);
				if (warp > 0.02f) {
					// a streak trailing upward (the stars rush down)
					float len = s*(1f + warp*(6f + 9f*l));
					sb.draw(Fx.glow(), x - s/2, y - s/2, s, len);
				} else
					sb.draw(Fx.glow(), x - s/2, y - s/2, s, s);
				if (l == LAYERS - 1) {		// the nearest stars get a hot core
					sb.setColor(1f, 1f, 1f, alpha*0.5f*tw);
					sb.draw(Fx.glow(), x - s*0.22f, y - s*0.22f, s*0.44f, s*0.44f);
				}
			}
		}
		// shooting star
		if (shotT >= 0f) {
			float k = shotT/shotLife;
			float travel = k*0.75f*h;
			float x = shotX*w + shotDx*travel, y = shotY*h + shotDy*travel;
			float a = alpha*(float) Math.sin(Math.min(1f, k*1.15f)*Math.PI)*0.9f;
			float len = 70f*unit*(0.4f + 0.6f*(float) Math.sin(k*Math.PI));
			Fx.tail(sb, false, x, y, -shotDx, -shotDy, len, 5f*unit, 0.8f, 0.9f, 1f, a);
			Fx.tail(sb, true, x, y, -shotDx, -shotDy, len*0.6f, 2.4f*unit, 1f, 1f, 1f, a);
			float hs = 7f*unit;
			sb.setColor(1f, 1f, 1f, a);
			sb.draw(Fx.glow(), x - hs/2, y - hs/2, hs, hs);
		}
		Fx.additiveOff(sb);
	}
}
