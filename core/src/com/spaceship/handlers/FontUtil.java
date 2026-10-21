package com.spaceship.handlers;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;

/** Replacement for the com.spaceship.handlers.FontUtil.bounds(BitmapFont, String) removed in libGDX 1.5. */
public class FontUtil {
	private static final GlyphLayout layout = new GlyphLayout();

	public static GlyphLayout bounds(BitmapFont font, CharSequence text) {
		layout.setText(font, text);
		return layout;
	}
}
