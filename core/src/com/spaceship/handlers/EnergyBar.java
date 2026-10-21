package com.spaceship.handlers;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

/**
 * Play-screen energy bar: a glass tube (res/hud/lifeBar*Lifes.png is the frame) holding gold liquid that
 * drains and refills smoothly, with flowing shimmer inside and a glowing tip at the surface.
 * The tube is drawn in three layers: dark back, liquid, then the frame/glass on top (the caller draws the frame).
 */
public class EnergyBar {
	public static final String[] ASSETS = { "res/fx/liquid.png", "res/fx/liquid_tip.png", "res/fx/tubeback3.png", "res/fx/tubeback4.png", "res/fx/tubeback5.png" };

	// liquid interior inside the 36 px high bar art, logical px (see tools/art/energy.py)
	private static final float IN_X0 = 13f, IN_Y0 = 10f, IN_Y1 = 26f, BAR_H = 36f;

	private final Texture back, liquid, tip;
	private final int cells;
	private float shown = -1f;
	private float surge;

	public static void load(AssetManager am) {
		for (String a : ASSETS)
			am.load(a, Texture.class);
	}

	public EnergyBar(AssetManager am, int cells) {
		this.cells = cells;
		back = am.get("res/fx/tubeback" + cells + ".png", Texture.class);
		liquid = am.get("res/fx/liquid.png", Texture.class);
		tip = am.get("res/fx/liquid_tip.png", Texture.class);
		liquid.setWrap(TextureWrap.Repeat, TextureWrap.ClampToEdge);
		liquid.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
	}

	/** Jump the displayed level (new game). */
	public void snap(float level) {
		shown = level;
	}

	/** Eases the displayed level toward the target (0..1); fast enough to read as draining/refilling. */
	public void update(float dt, float target) {
		if (shown < 0f) {
			shown = target;
			return;
		}
		float diff = target - shown;
		if (Math.abs(diff) > 0.0005f)
			surge = Math.min(1f, surge + Math.abs(diff) * 1.6f);
		shown += diff * Math.min(1f, dt * 5.5f);
		if (Math.abs(target - shown) < 0.002f)
			shown = target;
		surge *= 1f - Math.min(1f, dt * 4f);
	}

	/**
	 * @param frame the frame sprite: its position and size place the whole bar; its alpha fades the bar in
	 * @param tint color multiplier for the liquid (white = gold; the game tweens it red when energy is low)
	 */
	public void draw(SpriteBatch sb, Sprite frame, Color tint) {
		float t = Fx.time();
		float sx = frame.getWidth()/(cells == 3 ? 102f : cells == 4 ? 122f : 143f);
		float sy = frame.getHeight()/BAR_H;
		float x = frame.getX(), y = frame.getY();
		float a = frame.getColor().a;

		sb.setColor(1f, 1f, 1f, a);
		sb.draw(back, x, y, frame.getWidth(), frame.getHeight());

		float level = MathUtils.clamp(shown, 0f, 1f);
		float fullW = frame.getWidth()/sx*1f - 2f*IN_X0; // interior width in logical px
		float w = fullW*level;
		if (w > 0.5f) {
			float lx = x + IN_X0*sx, ly = y + IN_Y0*sy, lh = (IN_Y1 - IN_Y0)*sy;
			float scroll = t*0.22f;
			float u0 = scroll, u1 = scroll + w/64f;
			sb.setColor(tint.r, tint.g, tint.b, a);
			sb.draw(liquid, lx, ly, w*sx, lh, u0, 0f, u1, 1f);
			// slower counter-flowing shimmer on top
			Fx.additiveOn(sb);
			sb.setColor(tint.r, tint.g, tint.b, a*0.07f);
			float v0 = -scroll*0.6f + 0.35f;
			sb.draw(liquid, lx, ly, w*sx, lh, v0, 0f, v0 + w/64f, 1f);
			// glowing tip where the liquid ends, brighter while the level is changing
			float tw = 12f*sx*(1f + 0.5f*surge);
			sb.setColor(tint.r, tint.g, tint.b, a*(0.45f + 0.5f*surge));
			sb.draw(tip, lx + w*sx - tw, ly, tw, lh);
			Fx.additiveOff(sb);
		}
		sb.setColor(1f, 1f, 1f, 1f);
	}
}
