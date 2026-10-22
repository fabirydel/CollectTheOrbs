package com.spaceship.handlers;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

/** Additive-blended glow helpers for the orb tails and engine plumes (textures in res/fx). */
public class Fx {
	public static final String[] ASSETS = { "res/fx/tail.png", "res/fx/core.png", "res/fx/plume.png", "res/fx/plume_core.png",
			"res/fx/spark_blue.png", "res/fx/spark_red.png", "res/fx/spark_gold.png", "res/fx/spark_purple.png", "res/fx/spark_white.png", "res/fx/bolt.png",
			"res/fx/orb_blue.png", "res/fx/orb_red.png", "res/fx/orb_gold.png", "res/fx/orb_purple.png",
			"res/fx/ring_wave.png", "res/fx/chevron.png" };

	/** Flicker, surges, embers and sparkles on the engine plumes and orb tails. Off = steady glow. */
	public static final boolean LIVELY = true;
	/** true = the original sprite-flash electricity on the menu force field (now brighter); false = the live procedural bolts. */
	public static final boolean CLASSIC_LIGHTNING = true;
	/** Sideways wobble of the flames and the angle of the orb tails. */
	public static final boolean WOBBLE = false;

	public static final int SPARK_BLUE = 0, SPARK_RED = 1, SPARK_GOLD = 2, SPARK_PURPLE = 3;

	private static Texture tail, core, plume, plumeCore;
	private static Texture[] sparks = new Texture[4];
	private static Texture dot, bolt, ring, chevron;
	public static final int ORB_FRAMES = 16;
	/** The orb frames are 32 px canvases whose orb body is 18 px: draw them 1.6x the 20 px orb sprite. */
	public static final float ORB_DRAW_SCALE = 1.6f;
	private static com.badlogic.gdx.graphics.g2d.TextureRegion[][] orbFrames;

	// small additive embers shared by the ship engines and the orb tails
	private static final int MAX = 400;
	private static final float[] px = new float[MAX], py = new float[MAX], pvx = new float[MAX], pvy = new float[MAX];
	private static final float[] age = new float[MAX], life = new float[MAX], psize = new float[MAX];
	private static final float[] pr = new float[MAX], pg = new float[MAX], pb = new float[MAX];
	private static int count;
	private static float time;

	public static void load(AssetManager am) {
		for (String a : ASSETS)
			am.load(a, Texture.class);
	}

	// the manager the cached textures came from: after the game quits (Gdx.app.exit disposes everything) Android can keep
	// this process alive, and the next launch creates a new AssetManager, so the cached textures would be dead
	private static AssetManager owner;

	public static void init(AssetManager am) {
		if (owner == am && tail != null)
			return;
		owner = am;
		count = 0;
		tail = am.get("res/fx/tail.png", Texture.class);
		core = am.get("res/fx/core.png", Texture.class);
		plume = am.get("res/fx/plume.png", Texture.class);
		plumeCore = am.get("res/fx/plume_core.png", Texture.class);
		sparks[SPARK_BLUE] = am.get("res/fx/spark_blue.png", Texture.class);
		sparks[SPARK_RED] = am.get("res/fx/spark_red.png", Texture.class);
		sparks[SPARK_GOLD] = am.get("res/fx/spark_gold.png", Texture.class);
		sparks[SPARK_PURPLE] = am.get("res/fx/spark_purple.png", Texture.class);
		dot = am.get("res/fx/spark_white.png", Texture.class);
		bolt = am.get("res/fx/bolt.png", Texture.class);
		ring = am.get("res/fx/ring_wave.png", Texture.class);
		chevron = am.get("res/fx/chevron.png", Texture.class);
		orbFrames = new com.badlogic.gdx.graphics.g2d.TextureRegion[4][];
		String[] orbs = { "blue", "red", "gold", "purple" };
		for (int i = 0; i < 4; i++)
			orbFrames[i] = com.badlogic.gdx.graphics.g2d.TextureRegion.split(am.get("res/fx/orb_" + orbs[i] + ".png", Texture.class), 32, 32)[0];
	}

	/** Soft white glow dot (tint it with SpriteBatch.setColor). */
	public static Texture glow() {
		return dot;
	}

	public static Texture ring() {
		return ring;
	}

	public static Texture chevron() {
		return chevron;
	}

	public static Texture spark(int kind) {
		return sparks[kind];
	}

	public static void additiveOn(SpriteBatch sb) {
		sb.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
	}

	public static void additiveOff(SpriteBatch sb) {
		sb.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		sb.setColor(1f, 1f, 1f, 1f);
	}

	/** Comet tail with its bright head at (x, y), extending toward direction (dx, dy). */
	public static void tail(SpriteBatch sb, boolean hot, float x, float y, float dx, float dy, float length, float width,
			float r, float g, float b, float a) {
		Texture t = hot ? core : tail;
		float rot = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees - 90f;
		sb.setColor(r, g, b, a);
		sb.draw(t, x - width / 2, y, width / 2, 0, width, length, 1f, 1f, rot, 0, 0, t.getWidth(), t.getHeight(), false, false);
	}

	/** Engine flame with its top at the nozzle (x, y) hanging downward, swung by swayDeg around the nozzle. */
	public static void plume(SpriteBatch sb, boolean hot, float x, float y, float length, float width, float swayDeg,
			float r, float g, float b, float a) {
		Texture t = hot ? plumeCore : plume;
		sb.setColor(r, g, b, a);
		sb.draw(t, x - width / 2, y - length, width / 2, length, width, length, 1f, 1f, swayDeg, 0, 0, t.getWidth(), t.getHeight(), false, false);
	}

	/** Seconds since start, advanced by update(); drives the flicker of tails and flames. */
	public static float time() {
		return time;
	}

	public static void emit(float x, float y, float vx, float vy, float lifeSec, float size, float r, float g, float b) {
		if (count >= MAX)
			return;
		int i = count++;
		px[i] = x; py[i] = y; pvx[i] = vx; pvy[i] = vy;
		age[i] = 0; life[i] = lifeSec; psize[i] = size;
		pr[i] = r; pg[i] = g; pb[i] = b;
	}

	public static void update(float dt) {
		time += dt;
		for (int i = count - 1; i >= 0; i--) {
			age[i] += dt;
			if (age[i] >= life[i]) {
				count--;
				px[i] = px[count]; py[i] = py[count]; pvx[i] = pvx[count]; pvy[i] = pvy[count];
				age[i] = age[count]; life[i] = life[count]; psize[i] = psize[count];
				pr[i] = pr[count]; pg[i] = pg[count]; pb[i] = pb[count];
				continue;
			}
			px[i] += pvx[i] * dt;
			py[i] += pvy[i] * dt;
			float drag = 1f - Math.min(1f, dt * 1.8f);
			pvx[i] *= drag;
			pvy[i] *= drag;
		}
	}

	public static void clear() {
		count = 0;
	}

	/** Call between additiveOn/Off. */
	public static void draw(SpriteBatch sb) {
		for (int i = 0; i < count; i++) {
			float t = age[i] / life[i];
			float a = (1f - t) * (1f - t);
			float s = psize[i] * (1f - 0.55f * t);
			sb.setColor(pr[i], pg[i], pb[i], a);
			sb.draw(dot, px[i] - s / 2, py[i] - s / 2, s, s);
		}
	}

	/** One straight piece of a bolt from (x0, y0) to (x1, y1), thickness th, between additiveOn/Off. */
	public static void segment(SpriteBatch sb, float x0, float y0, float x1, float y1, float th, float r, float g, float b, float a) {
		float dx = x1 - x0, dy = y1 - y0;
		float len = (float) Math.sqrt(dx * dx + dy * dy);
		if (len < 0.01f)
			return;
		float deg = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees;
		sb.setColor(r, g, b, a);
		sb.draw(bolt, x0, y0 - th / 2, 0, th / 2, len + th * 0.35f, th, 1f, 1f, deg, 0, 0, bolt.getWidth(), bolt.getHeight(), false, false);
	}

	/** Looping energy animation frames for an orb: 0 = blue, 1 = red, 2 = gold (energy), 3 = purple (item). */
	public static com.badlogic.gdx.graphics.g2d.TextureRegion[] orbFrames(int kind) {
		return orbFrames[kind];
	}

	/** Draws the orb's animated energy frame (soft edge + corona) centered on a 20 px-style orb sprite. */
	public static void drawOrb(SpriteBatch sb, com.badlogic.gdx.graphics.g2d.Sprite sprite, int kind, float phase) {
		com.badlogic.gdx.graphics.g2d.TextureRegion r = orbFrames[kind][(int) ((time*1.3f + phase)*ORB_FRAMES) % ORB_FRAMES];
		float w = sprite.getWidth()*sprite.getScaleX()*ORB_DRAW_SCALE;
		float h = sprite.getHeight()*sprite.getScaleY()*ORB_DRAW_SCALE;
		float cx = sprite.getX() + sprite.getWidth()/2;
		float cy = sprite.getY() + sprite.getHeight()/2;
		sb.setColor(sprite.getColor());
		sb.draw(r, cx - w/2, cy - h/2, w, h);
		sb.setColor(1f, 1f, 1f, 1f);
	}
}
