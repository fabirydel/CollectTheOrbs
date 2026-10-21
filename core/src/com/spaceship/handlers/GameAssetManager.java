package com.spaceship.handlers;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;

/**
 * AssetManager that loads textures listed in res/logical-sizes.txt as high-resolution textures
 * that report their original size (see LogicalTextureData), with mipmaps and linear filtering.
 * Lines in the file are: path width height
 */
public class GameAssetManager extends AssetManager {
	private final Map<String, int[]> logicalSizes = new HashMap<String, int[]>();

	public GameAssetManager() {
		FileHandle list = Gdx.files.internal("res/logical-sizes.txt");
		if (!list.exists())
			return;
		for (String line : list.readString().split("\\r?\\n")) {
			String[] p = line.trim().split("\\s+");
			if (p.length == 3)
				logicalSizes.put(p[0], new int[] { Integer.parseInt(p[1]), Integer.parseInt(p[2]) });
		}
	}

	@Override
	public synchronized <T> void load(String fileName, Class<T> type) {
		int[] size = logicalSizes.get(fileName);
		if (type == Texture.class && size != null) {
			TextureParameter parameter = new TextureParameter();
			parameter.textureData = new LogicalTextureData(getFileHandleResolver().resolve(fileName), size[0], size[1]);
			parameter.minFilter = TextureFilter.MipMapLinearLinear;
			parameter.magFilter = TextureFilter.Linear;
			super.load(fileName, Texture.class, parameter);
		} else {
			super.load(fileName, type);
		}
	}
}
