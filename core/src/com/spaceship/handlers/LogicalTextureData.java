package com.spaceship.handlers;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.glutils.FileTextureData;

/**
 * Texture data for art that is drawn at a higher resolution than the size the game code expects.
 * The GL texture keeps the full pixel size, but getWidth()/getHeight() report the original
 * ("logical") size, so sprite sizes and texture region coordinates in the game code stay valid.
 */
public class LogicalTextureData extends FileTextureData {
	private final int logicalWidth;
	private final int logicalHeight;

	public LogicalTextureData(FileHandle file, int logicalWidth, int logicalHeight) {
		super(file, null, null, true);
		this.logicalWidth = logicalWidth;
		this.logicalHeight = logicalHeight;
	}

	@Override
	public int getWidth() {
		return logicalWidth;
	}

	@Override
	public int getHeight() {
		return logicalHeight;
	}
}
