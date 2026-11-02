package com.spaceship.handlers;

import java.util.IdentityHashMap;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FileTextureData;
import com.badlogic.gdx.utils.NumberUtils;

/**
 * A SpriteBatch that makes the neon-lettering textures (the grey fonts and the START / question buttons) glitch like the
 * splash sign: now and then a letter stutters, and every few seconds the whole sign dips and recovers letter by letter.
 * Glyphs are told apart by their atlas position, so every "e" shares one pattern and a few letters do the glitching.
 * Only active while {@link #glitch} is set (the main menu and its sub-menus).
 */
public class NeonBatch extends SpriteBatch {
	public boolean glitch;

	private static final long START = System.nanoTime();
	private final IdentityHashMap<Texture, Integer> kinds = new IdentityHashMap<Texture, Integer>();
	private float[] scratch = new float[400];

	private static final int NONE = 0, FONT = 1, SPRITE = 2;

	private int kind(Texture t){
		Integer k = kinds.get(t);
		if(k == null){
			int v = NONE;
			if(t.getTextureData() instanceof FileTextureData){
				String name = ((FileTextureData)t.getTextureData()).getFileHandle().name();
				if(name.startsWith("neon_prototype") && name.contains("grey"))
					v = FONT;
				else if(name.equals("bGo.png") || name.startsWith("bQuestion"))
					v = SPRITE;
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

	/** brightness (0..1) of the letter with this seed at time t seconds */
	private static float level(int seed, float t, boolean glitcher){
		float lv = 0.95f + 0.05f*(float)Math.sin(t*1.6f + (seed & 255)*0.05f);
		if(glitcher){
			float slotLen = 2.2f;
			int slot = (int)Math.floor(t/slotLen);
			int h = hash(seed, slot);
			if((h & 3) != 0){
				float local = t - slot*slotLen - ((h >>> 4) & 255)/255f*1.4f;
				if(local >= 0 && local < 0.45f){
					int i = (int)(local/0.07f);
					if((i & 1) == 1) lv *= 0.12f + ((h >>> (8 + i)) & 7)/7f*0.28f;
				}
			}
		}
		// whole sign power dip, every so often, recovering letter by letter
		float dipLen = 5.5f;
		int ds = (int)Math.floor(t/dipLen);
		int hd = hash(ds, 77);
		if((hd & 3) != 0){
			float dt = t - ds*dipLen - 0.5f - ((hd >>> 4) & 1023)/1023f*3f;
			float rec = 0.12f + ((seed >>> 3) & 127)/127f*0.3f;
			if(dt >= 0 && dt < rec + 0.2f){
				lv *= dt < rec ? 0.3f : 0.3f + 0.7f*(dt - rec)/0.2f;
			}
		}
		return lv;
	}

	@Override
	public void draw(Texture texture, float[] v, int offset, int count){
		if(!glitch || kind(texture) == NONE){
			super.draw(texture, v, offset, count);
			return;
		}
		boolean sprite = kind(texture) == SPRITE;
		if(scratch.length < count) scratch = new float[count];
		System.arraycopy(v, offset, scratch, 0, count);
		float t = (System.nanoTime() - START)/1e9f;
		for(int g = 0; g + 20 <= count; g += 20){
			// atlas position (u, v) of the glyph's first corner identifies the letter
			int seed = hash(Float.floatToRawIntBits(scratch[g + 3]), Float.floatToRawIntBits(scratch[g + 4]));
			float lv = level(seed, t, sprite || (seed & 7) == 0);
			if(lv >= 0.999f) continue;
			for(int k = 0; k < 4; k++){
				int c = Float.floatToRawIntBits(scratch[g + 5*k + 2]);
				int a = (int)(((c >>> 24) & 0xff)*lv);
				scratch[g + 5*k + 2] = NumberUtils.intToFloatColor((c & 0x00ffffff) | (a << 24));
			}
		}
		super.draw(texture, scratch, 0, count);
	}
}
