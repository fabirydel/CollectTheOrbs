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

	// shooting stars: up to SHOTS at once (now and then two or three come as a small shower), each with its own size and
	// speed, shedding glints along its path and ending in a small flare
	private static final int SHOTS = 4, GLINTS = 48;
	private float nextShot;
	private int showerLeft;
	private float showerGap, showerDirX, showerAngle;
	private final float[] shT = new float[SHOTS], shX = new float[SHOTS], shY = new float[SHOTS], shDx = new float[SHOTS], shDy = new float[SHOTS];
	private final float[] shLife = new float[SHOTS], shSize = new float[SHOTS], shReach = new float[SHOTS], shShed = new float[SHOTS];
	private final float[] glX = new float[GLINTS], glY = new float[GLINTS], glAge = new float[GLINTS], glLife = new float[GLINTS], glSize = new float[GLINTS];
	private int glNext;

	/**
	 * Shooting-star colour for each galaxy (in the order of the Galaxies menu), picked from the galaxy picture's own
	 * palette and lightened enough to stand out against it: white-blue on the black sky, gold on the amber nebula, pink on
	 * the magenta one, and so on.
	 */
	private static final float[][] SHOT_TINT = {
			{ 0.80f, 0.90f, 1.00f },	// 0 black: cool white
			{ 1.00f, 0.76f, 0.34f },	// 1 amber nebula on black: gold
			{ 1.00f, 0.45f, 0.85f },	// 2 dark magenta with pink stars: hot pink
			{ 1.00f, 0.46f, 0.30f },	// 3 black with red embers: ember orange
			{ 0.40f, 1.00f, 0.78f },	// 4 dark teal with a green aurora: aqua green
			{ 1.00f, 0.62f, 0.96f },	// 5 bright violet: pink lilac
			{ 0.55f, 0.95f, 1.00f },	// 6 bright blue: icy cyan
			{ 1.00f, 0.90f, 0.58f },	// 7 orange fire nebula: pale gold
			{ 0.70f, 0.88f, 1.00f },	// 8 navy with blue clouds: pale sky blue
			{ 0.72f, 1.00f, 0.70f },	// 9 green nebula: mint
	};
	private float[] shotTint = SHOT_TINT[0];
	/** dev driver only: a constant stream of shooting stars, for checking their colour against each galaxy */
	public static boolean DEV_OFTEN;

	/** Which galaxy is showing (its index in the Galaxies menu): sets the shooting stars' colour. */
	public void setGalaxy(int galaxy) {
		shotTint = SHOT_TINT[galaxy >= 0 && galaxy < SHOT_TINT.length ? galaxy : 0];
	}

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
		nextShot = MathUtils.random(3f, 6f);
		for (int i = 0; i < SHOTS; i++)
			shT[i] = -1f;
		for (int i = 0; i < GLINTS; i++)
			glAge[i] = 99f;
	}

	private void launch(float dirX, float angleDeg) {
		for (int i = 0; i < SHOTS; i++) {
			if (shT[i] >= 0f)
				continue;
			boolean right = dirX > 0f;
			shX[i] = right ? MathUtils.random(-0.1f, 0.5f) : MathUtils.random(0.5f, 1.1f);
			shY[i] = MathUtils.random(0.5f, 1.05f);
			float ang = angleDeg*MathUtils.degreesToRadians;
			shDx[i] = dirX*MathUtils.cos(ang);
			shDy[i] = -MathUtils.sin(ang);
			// most are small and quick; one in five is a big slow one that crosses more of the sky
			boolean big = MathUtils.random(4) == 0;
			shSize[i] = big ? MathUtils.random(1.5f, 2.1f) : MathUtils.random(0.6f, 1.15f);
			shLife[i] = big ? MathUtils.random(0.95f, 1.35f) : MathUtils.random(0.4f, 0.75f);
			shReach[i] = big ? MathUtils.random(0.8f, 1.05f) : MathUtils.random(0.45f, 0.8f);
			shShed[i] = 0f;
			shT[i] = 0f;
			return;
		}
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
		float w = Gdx.graphics.getWidth();
		for (int i = 0; i < SHOTS; i++) {
			if (shT[i] < 0f)
				continue;
			shT[i] += dt;
			float k = shT[i]/shLife[i];
			if (k >= 1f) {
				shT[i] = -1f;
				continue;
			}
			// shed a glint on the path every so often
			shShed[i] -= dt;
			if (shShed[i] <= 0f) {
				shShed[i] = MathUtils.random(0.025f, 0.06f);
				float travel = k*shReach[i]*h;
				int g = glNext;
				glNext = (glNext + 1) % GLINTS;
				glX[g] = shX[i]*w + shDx[i]*travel + MathUtils.random(-3f, 3f)*w/360f;
				glY[g] = shY[i]*h + shDy[i]*travel + MathUtils.random(-3f, 3f)*w/360f;
				glAge[g] = 0f;
				glLife[g] = MathUtils.random(0.35f, 0.8f);
				glSize[g] = MathUtils.random(2.2f, 4.2f)*shSize[i];
			}
		}
		for (int i = 0; i < GLINTS; i++)
			glAge[i] += dt;
		if (showerLeft > 0) {
			showerGap -= dt;
			if (showerGap <= 0f) {
				showerLeft--;
				showerGap = MathUtils.random(0.12f, 0.3f);
				launch(showerDirX, showerAngle + MathUtils.random(-5f, 5f));
			}
		}
		nextShot -= dt;
		if (nextShot <= 0f) {
			nextShot = DEV_OFTEN ? 0.35f : MathUtils.random(4f, 10f);
			showerDirX = MathUtils.randomBoolean() ? 1f : -1f;
			showerAngle = MathUtils.random(16f, 44f);
			launch(showerDirX, showerAngle);
			// one time in four, one or two more follow the same way: a small shower
			if (MathUtils.random(3) == 0) {
				showerLeft = MathUtils.random(1, 2);
				showerGap = MathUtils.random(0.12f, 0.3f);
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
		// glints left along the shooting stars' paths: they twinkle out
		float[] t = shotTint;
		for (int i = 0; i < GLINTS; i++) {
			if (glAge[i] >= glLife[i])
				continue;
			float k = glAge[i]/glLife[i];
			float gs = glSize[i]*unit*(1f - 0.5f*k);
			float tw = 0.7f + 0.3f*MathUtils.sin(time*30f + i*1.7f);
			sb.setColor(t[0] + (1f - t[0])*0.4f, t[1] + (1f - t[1])*0.4f, t[2] + (1f - t[2])*0.4f, alpha*(1f - k)*(1f - k)*0.9f*tw);
			sb.draw(Fx.glow(), glX[i] - gs/2, glY[i] - gs/2, gs, gs);
		}
		// shooting stars, in the galaxy's colour: a coloured tail, a lighter hot core, a bright head, and a flare as it burns out
		for (int i = 0; i < SHOTS; i++) {
			if (shT[i] < 0f)
				continue;
			float k = shT[i]/shLife[i];
			float size = shSize[i];
			float travel = k*shReach[i]*h;
			float x = shX[i]*w + shDx[i]*travel, y = shY[i]*h + shDy[i]*travel;
			float a = alpha*(float) Math.sin(Math.min(1f, k*1.15f)*Math.PI)*0.95f;
			float len = 80f*unit*size*(0.4f + 0.6f*(float) Math.sin(k*Math.PI));
			Fx.tail(sb, false, x, y, -shDx[i], -shDy[i], len, 5.5f*unit*size, t[0], t[1], t[2], a);
			Fx.tail(sb, true, x, y, -shDx[i], -shDy[i], len*0.6f, 2.6f*unit*size,
					t[0] + (1f - t[0])*0.55f, t[1] + (1f - t[1])*0.55f, t[2] + (1f - t[2])*0.55f, a);
			float hs = 8f*unit*size;
			sb.setColor(t[0] + (1f - t[0])*0.7f, t[1] + (1f - t[1])*0.7f, t[2] + (1f - t[2])*0.7f, a);
			sb.draw(Fx.glow(), x - hs/2, y - hs/2, hs, hs);
			// a soft halo of the colour around the head
			sb.setColor(t[0], t[1], t[2], a*0.35f);
			sb.draw(Fx.glow(), x - hs*1.6f, y - hs*1.6f, hs*3.2f, hs*3.2f);
			if (k > 0.82f) {
				// burning out: a quick flare
				float f = (k - 0.82f)/0.18f;
				float fs = hs*(1.5f + 3.5f*f);
				sb.setColor(t[0] + (1f - t[0])*0.5f, t[1] + (1f - t[1])*0.5f, t[2] + (1f - t[2])*0.5f, alpha*(1f - f)*0.8f);
				sb.draw(Fx.glow(), x - fs/2, y - fs/2, fs, fs);
			}
		}
		Fx.additiveOff(sb);
	}
}
