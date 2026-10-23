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
			// the liquid in horizontal bands whose ends sway, so the surface ripples (harder while the level changes)
			float full = fullW*sx;
			float amp = level > 0.995f ? 0f : (0.3f + 2.2f*surge)*sx;
			int bands = 8;
			float bh = lh/bands;
			sb.setColor(tint.r, tint.g, tint.b, a);
			for (int i = 0; i < bands; i++) {
				float bw = MathUtils.clamp(w*sx + amp*MathUtils.sin(t*2.6f + i*0.85f), 1f, full);
				sb.draw(liquid, lx, ly + (bands - 1 - i)*bh, bw, bh + 1f, u0, (i + 0.5f)/bands - 0.5f/64f*0f + 0.008f, scroll + bw/sx/64f, (i + 0.5f)/bands + 0.5f/bands - 0.008f);
			}
			Fx.additiveOn(sb);
			// bloom: the liquid added on top so it glows like the shield, plus a slower counter-flowing shimmer
			sb.setColor(tint.r, tint.g, tint.b, a*0.14f);
			sb.draw(liquid, lx, ly, w*sx, lh, u0, 0f, u1, 1f);
			sb.setColor(tint.r, tint.g, tint.b, a*0.09f);
			float v0 = -scroll*0.6f + 0.35f;
			sb.draw(liquid, lx, ly, w*sx, lh, v0, 0f, v0 + w/64f, 1f);
			// glossy highlight along the top of the liquid
			sb.setColor(1f, 0.97f, 0.8f, a*(0.11f + 0.05f*MathUtils.sin(t*1.7f)));
			sb.draw(Fx.glow(), lx, ly + lh*0.66f, w*sx, lh*0.3f);
			// bubbles drifting up through the liquid
			for (int i = 0; i < 9; i++) {
				float rise = (t*(0.16f + 0.05f*(i % 3)) + i*0.37f) % 1f;
				float bx = (i*0.618034f % 1f)*w + 1.2f*MathUtils.sin(t*1.8f + i*2.1f);
				if (bx < 1f || bx > w - 1f)
					continue;
				float bs = (1.5f + 0.5f*(i % 3))*sx;
				float fade = Math.min(1f, rise*6f)*Math.min(1f, (1f - rise)*5f);
				sb.setColor(1f, 0.96f, 0.75f, a*0.55f*fade);
				sb.draw(Fx.glow(), lx + bx*sx - bs, ly + lh*0.1f + rise*lh*0.8f - bs, bs*2f, bs*2f);
			}
			// glowing tip where the liquid ends, brighter while the level is changing
			float tw = 12f*sx*(1f + 0.5f*surge);
			sb.setColor(tint.r, tint.g, tint.b, a*(0.45f + 0.5f*surge));
			sb.draw(tip, lx + w*sx - tw, ly, tw, lh);
			Fx.additiveOff(sb);
		}
		sb.setColor(1f, 1f, 1f, 1f);
	}

	/**
	 * Call right after the frame is drawn: light from the liquid shining through the glass window only (the metal rails and
	 * end caps stay unlit): scatter into the empty part of the tube, light catching the glass edges, and a slow glint.
	 */
	public void drawGlass(SpriteBatch sb, Sprite frame, Color tint) {
		float level = MathUtils.clamp(shown, 0f, 1f);
		if (level <= 0.002f)
			return;
		float t = Fx.time();
		float sx = frame.getWidth()/(cells == 3 ? 102f : cells == 4 ? 122f : 143f);
		float sy = frame.getHeight()/BAR_H;
		float a = frame.getColor().a;
		float fullW = frame.getWidth()/sx - 2f*IN_X0;
		float w = fullW*level;
		float lx = frame.getX() + IN_X0*sx, ly = frame.getY() + IN_Y0*sy, lh = (IN_Y1 - IN_Y0)*sy;
		float tipX = lx + w*sx;
		float pulse = 0.5f + 0.5f*MathUtils.sin(t*1.3f);
		Fx.additiveOn(sb);
		// light scattering into the empty glass beyond the liquid
		float room = lx + fullW*sx - tipX;
		float reach = Math.min(44f*sx, room);
		if (reach > 1f) {
			sb.setColor(tint.r, tint.g, tint.b, a*(0.50f + 0.12f*pulse));
			sb.draw(tip, tipX, ly, reach, lh, 1f, 0f, 0f, 1f);
			float near = Math.min(16f*sx, room);
			sb.setColor(1f, 0.9f, 0.55f, a*(0.45f + 0.1f*pulse));
			sb.draw(tip, tipX, ly, near, lh, 1f, 0f, 0f, 1f);
		}
		// the whole window glows a little, strongest toward the middle of the liquid
		sb.setColor(tint.r, tint.g, tint.b, a*(0.10f + 0.04f*pulse));
		sb.draw(Fx.glow(), lx, ly, w*sx, lh);
		// light catching the glass edges, top and bottom
		sb.setColor(1f, 0.93f, 0.62f, a*(0.34f + 0.1f*pulse));
		sb.draw(Fx.glow(), lx, ly + lh - 2.4f*sy, w*sx, 3.6f*sy);
		sb.setColor(tint.r, tint.g, tint.b, a*0.30f);
		sb.draw(Fx.glow(), lx, ly - 1.2f*sy, w*sx, 3.4f*sy);
		// a soft glint sliding along the glass now and then
		float gp = (t*0.2f) % 1.6f;
		if (gp < 1f) {
			float gw = 16f*sx, gx = lx + gp*(w*sx + gw) - gw;
			float x0 = Math.max(gx, lx), x1 = Math.min(gx + gw, lx + w*sx);
			if (x1 > x0) {
				sb.setColor(1f, 0.98f, 0.85f, a*0.16f*(float) Math.sin(gp*Math.PI));
				sb.draw(Fx.glow(), x0, ly, x1 - x0, lh);
			}
		}
		Fx.additiveOff(sb);
	}
}
