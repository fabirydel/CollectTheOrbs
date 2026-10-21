package com.spaceship.handlers;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

/**
 * Electricity on the main-menu force field: bolts crawl along the shield surface, jump across it
 * and never leave the shield. Each bolt keeps its endpoints but re-jitters its shape many times a
 * second, so it crackles. Bolts stay inside the shield. Drawn additively in the shield's color with a white-hot core.
 */
public class Lightning {
	private static final int MAXB = 12, MAXP = 20;
	private static final float[][] COLORS = {
			{ 0.13f, 0.83f, 0.93f }, { 1.00f, 0.23f, 0.36f }, { 0.13f, 0.83f, 0.93f }, { 1.00f, 0.23f, 0.36f }, { 1.00f, 0.75f, 0.18f } };

	private final int[] type = new int[MAXB];
	private final float[] a0 = new float[MAXB], span = new float[MAXB], age = new float[MAXB], life = new float[MAXB], thick = new float[MAXB];
	private final int[] seed = new int[MAXB];
	private int count;
	private float spawnAccum;
	private int rng;

	private final float[] px = new float[MAXP], py = new float[MAXP];

	private float rnd() {
		rng = rng * 1664525 + 1013904223;
		return ((rng >>> 8) & 0xFFFF) / 65535f;
	}

	public void clear() {
		count = 0;
	}

	public void update(float dt, boolean active) {
		for (int i = count - 1; i >= 0; i--) {
			age[i] += dt;
			if (age[i] >= life[i] || !active) {
				count--;
				type[i] = type[count]; a0[i] = a0[count]; span[i] = span[count]; age[i] = age[count];
				life[i] = life[count]; thick[i] = thick[count]; seed[i] = seed[count];
			}
		}
		if (!active)
			return;
		spawnAccum += dt * 2.3f;
		while (spawnAccum >= 1f) {
			spawnAccum -= 1f;
			int burst = MathUtils.random() < 0.07f ? 2 : 1;
			for (int k = 0; k < burst && count < MAXB; k++) {
				int i = count++;
				float roll = MathUtils.random();
				type[i] = roll < 0.75f ? 0 : 1;
				a0[i] = MathUtils.random(0f, MathUtils.PI2);
				span[i] = type[i] == 0 ? MathUtils.random(0.45f, 1.5f) : type[i] == 1 ? MathUtils.random(1.4f, 3.0f) : 0f;
				age[i] = 0;
				life[i] = MathUtils.random(0.16f, 0.34f);
				thick[i] = MathUtils.random(0.8f, 1.25f);
				seed[i] = MathUtils.random(1, 1 << 20);
			}
		}
	}

	private int build(int i, float cx, float cy, float r) {
		// the shape re-rolls every 45 ms while the endpoints stay put
		rng = seed[i] + (int) (age[i] / 0.045f) * 7919;
		int n;
		if (type[i] == 0) {
			n = 14;
			for (int k = 0; k < n; k++) {
				float t = k / (float) (n - 1);
				float ang = a0[i] + span[i] * t;
				float rad = r * (k == 0 || k == n - 1 ? 0.96f : 0.84f + 0.16f * rnd());
				px[k] = cx + MathUtils.cos(ang) * rad;
				py[k] = cy + MathUtils.sin(ang) * rad;
			}
		} else if (type[i] == 1) {
			n = 11;
			float x0 = cx + MathUtils.cos(a0[i]) * r * 0.94f, y0 = cy + MathUtils.sin(a0[i]) * r * 0.94f;
			float x1 = cx + MathUtils.cos(a0[i] + span[i]) * r * 0.94f, y1 = cy + MathUtils.sin(a0[i] + span[i]) * r * 0.94f;
			float nx = -(y1 - y0), ny = x1 - x0;
			float nl = (float) Math.sqrt(nx * nx + ny * ny);
			nx /= nl;
			ny /= nl;
			for (int k = 0; k < n; k++) {
				float t = k / (float) (n - 1);
				float off = (k == 0 || k == n - 1 ? 0f : (rnd() - 0.5f) * 0.34f * r) * (float) Math.sin(Math.PI * t) * 1.6f;
				px[k] = x0 + (x1 - x0) * t + nx * off;
				py[k] = y0 + (y1 - y0) * t + ny * off;
			}
		} else {
			n = 9;
			float ang = a0[i];
			for (int k = 0; k < n; k++) {
				float t = k / (float) (n - 1);
				ang += (k == 0 ? 0f : (rnd() - 0.5f) * 0.32f);
				float rad = r * (0.93f + 0.62f * t);
				px[k] = cx + MathUtils.cos(ang) * rad;
				py[k] = cy + MathUtils.sin(ang) * rad;
			}
		}
		for (int k = 0; k < n; k++) {
			float dx = px[k] - cx, dy = py[k] - cy;
			float d = (float) Math.sqrt(dx * dx + dy * dy);
			float max = r * 0.97f;
			if (d > max) {
				px[k] = cx + dx / d * max;
				py[k] = cy + dy / d * max;
			}
		}
		return n;
	}

	/** Call between Fx.additiveOn/Off, after the shield is drawn. (cx, cy) shield center, r its radius in px. */
	public void draw(SpriteBatch sb, float cx, float cy, float r, int shieldState) {
		float[] c = COLORS[Math.min(shieldState, COLORS.length - 1)];
		float u = r / 61f;
		for (int i = 0; i < count; i++) {
			float t = age[i] / life[i];
			float env = Math.min(1f, t * 9f) * (1f - t * t);
			float flick = 0.7f + 0.3f * (((seed[i] + (int) (age[i] / 0.045f) * 31) & 7) / 7f);
			float a = env * flick * 0.85f;
			int n = build(i, cx, cy, r);
			float w = thick[i] * u;
			for (int k = 0; k + 1 < n; k++)
				Fx.segment(sb, px[k], py[k], px[k + 1], py[k + 1], 12f * w, c[0], c[1], c[2], a * 0.24f);
			for (int k = 0; k + 1 < n; k++)
				Fx.segment(sb, px[k], py[k], px[k + 1], py[k + 1], 5f * w, c[0], c[1], c[2], a * 0.7f);
			for (int k = 0; k + 1 < n; k++)
				Fx.segment(sb, px[k], py[k], px[k + 1], py[k + 1], 1.9f * w, 1f, 1f, 1f, a);
		}
	}
}
