package com.spaceship.handlers;

import java.util.IdentityHashMap;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FileTextureData;
import com.badlogic.gdx.utils.NumberUtils;

/**
 * A SpriteBatch that makes the neon art (the grey fonts, the round icon buttons, START and the question prompt) glitch
 * like the splash sign: now and then a letter or an icon stutters, and every so often a line dips and recovers letter
 * by letter. Every letter, line and icon has its own schedule, derived from where it sits in the text, so nothing flickers
 * in unison. Only active while {@link #glitch} is set (the main menu and its sub-menus).
 */
public class NeonBatch extends SpriteBatch {
	public boolean glitch;

	private static final long START = System.nanoTime();
	private static final int NONE = 0, FONT = 1, ICON = 2;
	/** icons (round buttons, START) stay steady; only text glitches */
	private static final boolean GLITCH_ICONS = false;
	private final IdentityHashMap<Texture, Integer> kinds = new IdentityHashMap<Texture, Integer>();
	private final IdentityHashMap<Texture, Integer> names = new IdentityHashMap<Texture, Integer>();
	private float[] scratch = new float[400];

	private int kind(Texture t){
		Integer k = kinds.get(t);
		if(k == null){
			int v = NONE;
			if(t.getTextureData() instanceof FileTextureData){
				FileHandle fh = ((FileTextureData)t.getTextureData()).getFileHandle();
				String name = fh.name();
				if(name.startsWith("neon_prototype") && name.contains("grey")){
					v = FONT;
				}else if(GLITCH_ICONS && fh.path().contains("res/gui/") && name.startsWith("b")){
					v = ICON;		// the round icon buttons, START and the question prompt
					names.put(t, name.hashCode());
				}
			}
			kinds.put(t, v);
			k = v;
		}
		return k;
	}

	private static int hash(int a, int b){
		int h = a*0x9E3779B1 ^ (b + 0x7F4A7C15)*0x85EBCA6B;
		h ^= h >>> 15;
		h *= 0x2C1B3C6D;
		h ^= h >>> 13;
		return h;
	}

	/**
	 * brightness (0..1) of one letter or icon (seed) at time t seconds; line identifies the string it belongs to, so
	 * lines stutter and dip on their own schedules
	 */
	private static float level(int seed, int line, float t, boolean glitcher){
		float lv = 0.95f + 0.05f*(float)Math.sin(t*1.6f + (seed & 255)*0.05f);
		if(glitcher){
			float slotLen = 3.2f + ((seed >>> 8) & 255)/255f*3.6f;
			int slot = (int)Math.floor(t/slotLen);
			int h = hash(seed, slot);
			if((h & 3) != 0 && (h & 12) != 0){
				float local = t - slot*slotLen - ((h >>> 4) & 255)/255f*(slotLen - 0.5f);
				if(local >= 0 && local < 0.45f){
					int i = (int)(local/0.07f);
					if((i & 1) == 1) lv *= 0.12f + ((h >>> (8 + i)) & 7)/7f*0.28f;
				}
			}
		}
		// a line's power dip: its own period, offset and recovery, and not every line takes part every time
		float dipLen = 6f + ((line >>> 5) & 255)/255f*6f;
		int ds = (int)Math.floor(t/dipLen);
		int hd = hash(ds, line);
		if((hd & 7) == 0){
			float dt = t - ds*dipLen - ((hd >>> 4) & 1023)/1023f*(dipLen - 1f);
			float rec = 0.1f + ((seed >>> 3) & 127)/127f*0.35f;
			if(dt >= 0 && dt < rec + 0.2f)
				lv *= dt < rec ? 0.3f : 0.3f + 0.7f*(dt - rec)/0.2f;
		}
		return lv;
	}

	private static float fade(float packed, float lv){
		int c = Float.floatToRawIntBits(packed);
		int a = (int)(((c >>> 24) & 0xff)*lv);
		return NumberUtils.intToFloatColor((c & 0x00ffffff) | (a << 24));
	}

	/** an icon's brightness: its schedule comes from which icon it is and roughly where it sits on screen */
	private float iconLevel(Texture t, float x, float y){
		Integer n = names.get(t);
		int seed = hash(n == null ? 0 : n, hash((int)Math.floor(x/96f), (int)Math.floor(y/96f)));
		return level(seed, seed, (System.nanoTime() - START)/1e9f, true);
	}

	@Override
	public void draw(Texture t, float x, float y, float w, float h){
		if(glitch && kind(t) == ICON){
			float old = getPackedColor();
			setPackedColor(fade(old, iconLevel(t, x, y)));
			super.draw(t, x, y, w, h);
			setPackedColor(old);
		}else
			super.draw(t, x, y, w, h);
	}

	@Override
	public void draw(TextureRegion r, float x, float y, float w, float h){
		if(glitch && kind(r.getTexture()) == ICON){
			float old = getPackedColor();
			setPackedColor(fade(old, iconLevel(r.getTexture(), x, y)));
			super.draw(r, x, y, w, h);
			setPackedColor(old);
		}else
			super.draw(r, x, y, w, h);
	}

	@Override
	public void draw(TextureRegion r, float x, float y, float ox, float oy, float w, float h, float sx, float sy, float rot){
		if(glitch && kind(r.getTexture()) == ICON){
			float old = getPackedColor();
			setPackedColor(fade(old, iconLevel(r.getTexture(), x, y)));
			super.draw(r, x, y, ox, oy, w, h, sx, sy, rot);
			setPackedColor(old);
		}else
			super.draw(r, x, y, ox, oy, w, h, sx, sy, rot);
	}

	@Override
	public void draw(Texture texture, float[] v, int offset, int count){
		int kind = glitch ? kind(texture) : NONE;
		if(kind == NONE){
			super.draw(texture, v, offset, count);
			return;
		}
		if(scratch.length < count) scratch = new float[count];
		System.arraycopy(v, offset, scratch, 0, count);
		float t = (System.nanoTime() - START)/1e9f;
		// the string this call draws: its first glyph and length tell its lines apart
		int line = hash(Float.floatToRawIntBits(scratch[3]), count);
		for(int g = 0; g + 20 <= count; g += 20){
			float lv;
			if(kind == ICON){
				lv = iconLevel(texture, scratch[g], scratch[g + 1]);
			}else{
				// the letter (its atlas position) at this place in this line: no two letters share a schedule
				int seed = hash(hash(Float.floatToRawIntBits(scratch[g + 3]), Float.floatToRawIntBits(scratch[g + 4])), line + g);
				lv = level(seed, line, t, (seed & 7) == 0);
			}
			if(lv >= 0.999f) continue;
			for(int k = 0; k < 4; k++)
				scratch[g + 5*k + 2] = fade(scratch[g + 5*k + 2], lv);
		}
		super.draw(texture, scratch, 0, count);
	}
}
