package com.spaceship.handlers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

/**
 * Visuals for a speed-up (see SpeedDirector): the charge-up (the engines pull their flames in and sputter while energy is
 * drawn into the ship), then the burst (flash, shockwave ring, streaking stars, flames blasting out). Everything here is
 * additive and pauses when update() is not called.
 */
public class SpeedUpFx {
	private static final int STREAKS = 70;
	private static final float RING_TIME = 0.9f, FLASH_TIME = 0.35f, STREAK_TIME = 1.4f, BG_BOOST_TIME = 1.6f;

	private float burstAge = 99f;            // seconds since the burst
	private final float[] sx = new float[STREAKS], sy = new float[STREAKS], sv = new float[STREAKS];
	private final float[] sl = new float[STREAKS], sw = new float[STREAKS];
	private float ringX, ringY;
	private float emberCarry;

	public void reset() {
		burstAge = 99f;
		emberCarry = 0f;
	}

	/** The speed just jumped: fire the flash, ring and streaks,  */
	public void burst(float shipX, float shipY, int newTier) {
		burstAge = 0f;
		ringX = shipX;
		ringY = shipY;
		float w = Gdx.graphics.getWidth(), h = Gdx.graphics.getHeight();
		for (int i = 0; i < STREAKS; i++) {
			sx[i] = MathUtils.random(0f, w);
			sy[i] = MathUtils.random(-0.1f * h, 1.1f * h);
			sv[i] = h * MathUtils.random(1.6f, 3.4f);
			sl[i] = h * MathUtils.random(0.07f, 0.22f);
			sw[i] = w / 720f * MathUtils.random(3f, 7f);
		}
	}

	/** @param chargeTime seconds into the charge-up, or 0 if not charging */
	public void update(float dt, float shipX, float shipY, float shipSize, float chargeTime, float r, float g, float b) {
		burstAge += dt;
		float h = Gdx.graphics.getHeight();
		for (int i = 0; i < STREAKS; i++)
			sy[i] -= sv[i] * dt;
		// while the engines inhale, embers are pulled in from around the ship toward the nozzles
		if (chargeTime > 0f && chargeTime < SpeedDirector.BURST_AT) {
			float k = chargeTime / SpeedDirector.BURST_AT;
			emberCarry += dt * (35f + 130f * k);
			while (emberCarry >= 1f) {
				emberCarry -= 1f;
				float a = MathUtils.random(0f, MathUtils.PI2);
				float rad = shipSize * MathUtils.random(1.1f, 2.0f);
				float ex = shipX + MathUtils.cos(a) * rad, ey = shipY + MathUtils.sin(a) * rad;
				float tx = shipX, ty = shipY - shipSize * 0.5f;
				float white = MathUtils.random(0f, 0.6f);
				Fx.emit(ex, ey, (tx - ex) * 2.4f, (ty - ey) * 2.4f, MathUtils.random(0.25f, 0.45f), shipSize * MathUtils.random(0.05f, 0.1f),
						r + (1f - r) * white, g + (1f - g) * white, b + (1f - b) * white);
			}
		}
	}

	// ---- plume control (read by the ship's engine flames) ----

	/** Multiplier on the flame length: pulled in while charging, a blast right after the burst. */
	public float plumeLength(float chargeTime) {
		if (burstAge < 2.5f)
			return 1f + 1.7f * (float) Math.exp(-burstAge / 0.45f);
		if (chargeTime <= 0f)
			return 1f;
		if (chargeTime < 2f) {
			float k = chargeTime / 2f;
			return 1f - 0.75f * k * k * (3f - 2f * k);
		}
		float k = (chargeTime - 2f) / (SpeedDirector.BURST_AT - 2f);
		return sputter(chargeTime) ? 0.12f : 0.25f + 0.65f * MathUtils.clamp(k, 0f, 1f);
	}

	public float plumeAlpha(float chargeTime) {
		if (burstAge < 2.5f || chargeTime <= 0f)
			return 1f;
		return chargeTime >= 2f && sputter(chargeTime) ? 0.12f : 1f;
	}

	/** 0..1 extra whiteness of the flame core (white-hot as it ignites). */
	public float plumeWhite(float chargeTime) {
		if (burstAge < 2.5f)
			return 0.9f * (float) Math.exp(-burstAge / 0.6f);
		if (chargeTime <= 0f)
			return 0f;
		return chargeTime < 2f ? 0.4f * chargeTime / 2f : 0.4f + 0.5f * MathUtils.clamp((chargeTime - 2f) / 1.2f, 0f, 1f);
	}

	private static boolean sputter(float t) {
		return (t >= 2.3f && t < 2.5f) || (t >= 2.75f && t < 2.95f);
	}

	/** Extra background speed right after the burst (0 = none): lets the stars rush past, then settle at the new tier's speed. */
	public float backgroundBoost() {
		return burstAge < BG_BOOST_TIME ? (float) Math.exp(-burstAge / 0.7f) : 0f;
	}

	// ---- drawing ----

	/** Call between Fx.additiveOn/Off. */
	public void draw(SpriteBatch sb, float shipX, float shipY, float shipSize, float chargeTime, float r, float g, float b) {
		float w = Gdx.graphics.getWidth(), h = Gdx.graphics.getHeight();
		// energy gathering around the ship while charging
		if (chargeTime > 0f && chargeTime < SpeedDirector.BURST_AT + 0.4f) {
			float k = MathUtils.clamp(chargeTime / SpeedDirector.BURST_AT, 0f, 1f);
			float pulse = 0.65f + 0.35f * MathUtils.sin(chargeTime * (7f + 16f * k));
			float size = shipSize * (2.6f + 1.4f * k) * (0.9f + 0.1f * pulse);
			float a = 0.8f * k * pulse;
			sb.setColor(r, g, b, a);
			sb.draw(Fx.glow(), shipX - size / 2, shipY - size / 2, size, size);
			size *= 0.45f;
			sb.setColor(1f, 1f, 1f, a * 0.6f);
			sb.draw(Fx.glow(), shipX - size / 2, shipY - size / 2, size, size);
		}
		if (burstAge < FLASH_TIME) {
			float k = 1f - burstAge / FLASH_TIME;
			sb.setColor(1f, 1f, 1f, 0.2f * k * k);
			sb.draw(Fx.glow(), shipX - 0.7f * w, shipY - 0.7f * h, 1.4f * w, 1.4f * h);
		}
		if (burstAge < RING_TIME) {
			float k = burstAge / RING_TIME;
			float size = shipSize * (0.8f + 9f * (1f - (1f - k) * (1f - k)));
			sb.setColor(1f - (1f - r) * 0.4f, 1f - (1f - g) * 0.4f, 1f - (1f - b) * 0.4f, 0.85f * (1f - k) * (1f - k));
			sb.draw(Fx.ring(), ringX - size / 2, ringY - size / 2, size, size);
		}
		if (burstAge < STREAK_TIME) {
			float k = burstAge / STREAK_TIME;
			float a = (1f - k) * (1f - k) * 0.75f;
			for (int i = 0; i < STREAKS; i++)
				Fx.tail(sb, false, sx[i], sy[i], 0f, 1f, sl[i], sw[i], 0.85f, 0.92f, 1f, a);
		}
		sb.setColor(1f, 1f, 1f, 1f);
	}
}
