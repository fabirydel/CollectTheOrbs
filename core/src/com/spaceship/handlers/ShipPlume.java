package com.spaceship.handlers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

/**
 * Engine plumes for the ships. Each ship's nozzle positions (res/ships/nozzles.txt, read from the
 * original trail art) get a flickering additive flame, tinted by the shield state and swung by
 * the ship's sideways motion.
 */
public class ShipPlume {
	// per ship: {x, y, width} triples in the 128px ship frame (y down)
	private static float[][] nozzles;
	private static final float[][] COLORS = {
			{ 0.13f, 0.83f, 0.93f }, // blue
			{ 1.00f, 0.23f, 0.36f }, // red
			{ 0.13f, 0.83f, 0.93f }, // blue-red
			{ 1.00f, 0.23f, 0.36f }, // red-blue
			{ 1.00f, 0.75f, 0.18f } // gold
	};
	private static final float BIG_L = 1.8f, BIG_W = 1.6f;		// flame size relative to the original
	private float cr = COLORS[0][0], cg = COLORS[0][1], cb = COLORS[0][2];
	private float time;
	private float lateral;

	public static void load() {
		if (nozzles != null)
			return;
		String[] lines = Gdx.files.internal("res/ships/nozzles.txt").readString().split("\\r?\\n");
		nozzles = new float[lines.length][];
		for (String line : lines) {
			String[] p = line.trim().split("\\s+");
			if (p.length < 1 || p[0].isEmpty())
				continue;
			int id = Integer.parseInt(p[0]);
			float[] n = new float[p.length - 1];
			for (int i = 1; i < p.length; i++)
				n[i - 1] = Float.parseFloat(p[i]);
			if (id < nozzles.length)
				nozzles[id] = n;
		}
	}

	/** Smoothed sideways motion in -1..1 (positive = moving right). */
	public void setLateral(float target) {
		lateral += (MathUtils.clamp(target, -1f, 1f) - lateral) * Math.min(1f, Gdx.graphics.getDeltaTime() * 12f);
	}

	// where the ship's centre was over the last second: the flame keeps the path it was left on
	private static final int HIST = 256, SEG = 20;
	private static final float LIFE = 0.45f;		// seconds a puff of flame takes to travel the glow's length
	private final float[] histT = new float[HIST], histX = new float[HIST];
	private int histN, histHead;
	private final float[] xs = new float[SEG + 1];

	private void remember(float t, float cx) {
		if (histN > 0 && t - histT[(histHead - 1 + HIST) % HIST] > 0.25f)
			histN = 0;			// the ship was away (paused, dead): start a fresh path
		histT[histHead] = t;
		histX[histHead] = cx;
		histHead = (histHead + 1) % HIST;
		if (histN < HIST)
			histN++;
	}

	/** the ship centre's x, `age` seconds ago */
	private float xAgo(float now, float age) {
		float target = now - age;
		float newerT = histT[(histHead - 1 + HIST) % HIST], newerX = histX[(histHead - 1 + HIST) % HIST];
		for (int i = 2; i <= histN; i++) {
			int idx = (histHead - i + HIST) % HIST;
			if (histT[idx] <= target) {
				float span = newerT - histT[idx];
				float f = span > 1e-5f ? (target - histT[idx]) / span : 1f;
				return histX[idx] + (newerX - histX[idx]) * f;
			}
			newerT = histT[idx];
			newerX = histX[idx];
		}
		return newerX;
	}

	private static final float KEEP = 0.3f;		// how much of the ship's sideways wandering the flame keeps (0 = straight down)

	private void path(float now, float off, float length, float flow, float maxDev) {
		float cxNow = xAgo(now, 0f);
		for (int k = 0; k <= SEG; k++)
			xs[k] = MathUtils.clamp((xAgo(now, length * k / SEG / flow) - cxNow) * KEEP, -maxDev, maxDev);
		// relax the path into a smooth drift (no zigzag), the nozzle end stays put
		for (int pass = 0; pass < 3; pass++)
			for (int k = 1; k < SEG; k++)
				xs[k] = 0.25f * xs[k - 1] + 0.5f * xs[k] + 0.25f * xs[k + 1];
		for (int k = 0; k <= SEG; k++)
			xs[k] += cxNow + off;
	}

	/** a stretch of flame that came loose at a turn and keeps falling on its own */
	private static class Piece {
		float[][] xs;			// [nozzle * 3 + layer][SEG + 1]
		float[] y, len, wid, flow, r, g, b, a;	// per [nozzle * 3 + layer]
		float age;
	}
	private static final float PIECE_LIFE = 0.8f;
	private Piece snap;
	private final java.util.ArrayList<Piece> pieces = new java.util.ArrayList<Piece>();

	/** Cut the flame here (a left/right press): what is already out keeps going south, a fresh flame starts at the nozzle. */
	public void split() {
		if (snap != null) {
			snap.age = 0;
			pieces.add(snap);
			snap = null;
			if (pieces.size() > 12)
				pieces.remove(0);
		}
		histN = 0;
	}

	private void keep(int slot, int slots, float[] path, float y, float len, float wid, float flow, float r, float g, float b, float a) {
		if (snap == null || snap.xs.length != slots) {
			snap = new Piece();
			snap.xs = new float[slots][SEG + 1];
			snap.y = new float[slots]; snap.len = new float[slots]; snap.wid = new float[slots]; snap.flow = new float[slots];
			snap.r = new float[slots]; snap.g = new float[slots]; snap.b = new float[slots]; snap.a = new float[slots];
		}
		System.arraycopy(path, 0, snap.xs[slot], 0, SEG + 1);
		snap.y[slot] = y; snap.len[slot] = len; snap.wid[slot] = wid; snap.flow[slot] = flow;
		snap.r[slot] = r; snap.g[slot] = g; snap.b[slot] = b; snap.a[slot] = a;
	}

	private void drawPieces(SpriteBatch sb, float dt) {
		for (int i = pieces.size() - 1; i >= 0; i--) {
			Piece p = pieces.get(i);
			p.age += dt;
			if (p.age >= PIECE_LIFE) {
				pieces.remove(i);
				continue;
			}
			float fade = 1f - p.age / PIECE_LIFE;
			fade *= fade;
			for (int s = 0; s < p.xs.length; s++)
				Fx.plumeRibbon(sb, s % 3 == 2, p.xs[s], SEG, p.y[s] - p.flow[s] * p.age, p.len[s], p.wid[s], p.r[s], p.g[s], p.b[s], p.a[s] * fade, 0.35f);
		}
	}

	private float surge;
	private float speed;
	private float chargeLen = 1f, chargeAlpha = 1f, chargeWhite = 0f;

	/** The speed-up sequence's control of the flames (see SpeedUpFx): length and brightness multipliers, extra core whiteness. */
	public void setCharge(float len, float alpha, float white) {
		chargeLen = len;
		chargeAlpha = alpha;
		chargeWhite = white;
	}

	/** How fast the run feels, 0..1 (PlayScreen.getSpeedFactor): longer, narrower, hotter flames and busier embers. */
	public void setSpeed(float s) {
		speed += (MathUtils.clamp(s, 0f, 1f) - speed) * Math.min(1f, Gdx.graphics.getDeltaTime() * 4f);
	}

	/** Call between Fx.additiveOn/Off, before the ship sprite is drawn. */
	public void draw(SpriteBatch sb, Sprite ship, int shipId, int shieldState, float alpha, boolean emit) {
		if (nozzles == null || shipId >= nozzles.length || nozzles[shipId] == null)
			return;
		float dt = Gdx.graphics.getDeltaTime();
		float t = Fx.time();
		float[] target = COLORS[Math.min(shieldState, COLORS.length - 1)];
		float k = Math.min(1f, dt * 10f);
		cr += (target[0] - cr) * k;
		cg += (target[1] - cg) * k;
		cb += (target[2] - cb) * k;

		// occasional surge, like the engine catching
		if (Fx.LIVELY && emit && MathUtils.random() < dt * 1.0f)
			surge = 1f;
		surge *= 1f - Math.min(1f, dt * 3f);

		float wEff = ship.getWidth() * ship.getScaleX();
		float hEff = ship.getHeight() * ship.getScaleY();
		float cx = ship.getX() + ship.getWidth() / 2;
		float cy = ship.getY() + ship.getHeight() / 2;
		float u = wEff / 128f;
		float boost = 1f;
		remember(t, cx);

		drawPieces(sb, dt);
		float[] n = nozzles[shipId];
		int slots = n.length / 3 * 3;
		for (int i = 0; i + 2 < n.length; i += 3) {
			float ph = i * 1.7f;
			float x = cx + (n[i] - 64f) * wEff / 128f;
			float y = cy + (64f - n[i + 1]) * hEff / 128f + 2f * u;
			float w = n[i + 2];
			float flicker = !Fx.LIVELY ? 0.92f : 0.88f + 0.04f * MathUtils.sin(t * 16f + ph) + 0.03f * MathUtils.sin(t * 34f + 2f * ph)
					+ MathUtils.random(-0.015f, 0.015f) + 0.15f * surge;
			float pulse = !Fx.LIVELY ? 1f : 1f + 0.05f * MathUtils.sin(t * 24f + ph) + MathUtils.random(-0.015f, 0.015f);
						float length = BIG_L * (82f + w * 2.2f) * u * flicker * boost * (1f + 1.15f * speed) * chargeLen;
			float width = BIG_W * (7f + w * 1.25f) * u * pulse * (1f - 0.14f * speed);
			float a = alpha * (0.78f + 0.12f * flicker) * chargeAlpha;
			// long faint wake, then the glow, then the hot core
			if (chargeWhite > 0.02f) {
				// glowing hot nozzles while the engines charge / ignite
				float gs = (16f + w * 1.2f) * u * (1f + 0.6f * chargeWhite);
				sb.setColor(1f, 1f, 1f, Math.min(1f, chargeWhite) * 0.9f * (chargeAlpha > 0.5f ? 1f : 0.3f));
				sb.draw(Fx.glow(), x - gs / 2, y - gs / 2, gs, gs);
			}
			float flow = length / LIFE;
			float off = x - cx;
			float hot = Math.min(1f, 0.35f + 0.5f * speed + chargeWhite); // the core goes whiter as the run speeds up and as the engines ignite
			float wakeL = length * (1.5f + 0.5f * speed), coreL = length * (0.62f + 0.1f * speed);
			path(t, off, wakeL, flow, 0.6f * wEff);
			float wa = a * (0.38f + 0.12f * speed);
			keep(i + 0, slots, xs, y, wakeL, width * 1.1f, flow, cr, cg, cb, wa);
			Fx.plumeRibbon(sb, false, xs, SEG, y, wakeL, width * 1.1f, cr, cg, cb, wa);
			path(t, off, length, flow, 0.6f * wEff);
			keep(i + 1, slots, xs, y, length, width * 1.5f, flow, cr, cg, cb, a);
			Fx.plumeRibbon(sb, false, xs, SEG, y, length, width * 1.5f, cr, cg, cb, a);
			path(t, off, coreL, flow, 0.6f * wEff);
			float hr = 1f - (1f - cr) * (1f - hot), hg = 1f - (1f - cg) * (1f - hot), hb = 1f - (1f - cb) * (1f - hot), ha = Math.min(1f, a * (1f + 0.25f * speed));
			keep(i + 2, slots, xs, y, coreL, width * 0.7f, flow, hr, hg, hb, ha);
			Fx.plumeRibbon(sb, true, xs, SEG, y, coreL, width * 0.7f, hr, hg, hb, ha);

			if (Fx.LIVELY && emit && MathUtils.random() < dt * 44f * (1f + 2.5f * speed)) {
				float white = MathUtils.random(0f, 0.55f);
				Fx.emit(x + MathUtils.random(-w * 0.5f, w * 0.5f) * u, y - MathUtils.random(0f, 12f) * u,
						MathUtils.random(-36f, 36f) * u, -MathUtils.random(70f, 230f) * u * (1f + 1.6f * speed),
						MathUtils.random(0.35f, 0.85f), MathUtils.random(5f, 12f) * u,
						cr + (1f - cr) * white, cg + (1f - cg) * white, cb + (1f - cb) * white);
			}
		}
	}
}
