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

	/** Call between Fx.additiveOn/Off, before the ship sprite is drawn. */
	public void draw(SpriteBatch sb, Sprite ship, int shipId, int shieldState, float alpha) {
		if (nozzles == null || shipId >= nozzles.length || nozzles[shipId] == null)
			return;
		float dt = Gdx.graphics.getDeltaTime();
		time += dt;
		float[] target = COLORS[Math.min(shieldState, COLORS.length - 1)];
		float k = Math.min(1f, dt * 10f);
		cr += (target[0] - cr) * k;
		cg += (target[1] - cg) * k;
		cb += (target[2] - cb) * k;

		float wEff = ship.getWidth() * ship.getScaleX();
		float hEff = ship.getHeight() * ship.getScaleY();
		float cx = ship.getX() + ship.getWidth() / 2;
		float cy = ship.getY() + ship.getHeight() / 2;
		float u = wEff / 128f;
		float sway = -lateral * 16f;
		float boost = 1f + 0.35f * Math.abs(lateral);

		float[] n = nozzles[shipId];
		for (int i = 0; i + 2 < n.length; i += 3) {
			float x = cx + (n[i] - 64f) * wEff / 128f;
			float y = cy + (64f - n[i + 1]) * hEff / 128f + 2f * u;
			float w = n[i + 2];
			float flicker = 0.92f + 0.06f * MathUtils.sin(time * 37f + i * 1.7f) + 0.03f * MathUtils.sin(time * 91f + i);
			float length = (50f + w * 1.4f) * u * flicker * boost;
			float width = (7f + w * 1.25f) * u;
			float a = alpha * (0.8f + 0.1f * flicker);
			Fx.plume(sb, false, x, y, length, width * 1.5f, sway, cr, cg, cb, a);
			Fx.plume(sb, true, x, y, length * 0.62f, width * 0.7f, sway, 1f - (1f - cr) * 0.35f, 1f - (1f - cg) * 0.35f, 1f - (1f - cb) * 0.35f, a);
		}
	}
}
