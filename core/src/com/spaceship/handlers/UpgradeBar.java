package com.spaceship.handlers;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;

/**
 * One upgrade's progress as the play screen's energy tube: gold liquid filling to level/max, eased when it changes
 * (so buying a level fills the tube up). Uses the 5-cell tube art for every upgrade so all rows look the same.
 */
public class UpgradeBar extends Widget {
	private static final float ART_W = 143f, ART_H = 36f;

	private final EnergyBar bar;
	private final Sprite frame;
	private final Color tint = new Color(1f, 1f, 1f, 1f);
	private float target;
	private final float scale;

	public UpgradeBar(AssetManager am, float scale, float level) {
		this.scale = scale;
		bar = new EnergyBar(am, 5);
		frame = new Sprite(am.get("res/hud/lifeBar5Lifes.png", Texture.class));
		target = level;
		bar.snap(level);
		setSize(ART_W * scale, ART_H * scale);
	}

	/** New fill level, 0..1. */
	public void setLevel(float level) {
		target = level;
	}

	@Override
	public float getPrefWidth() {
		return ART_W * scale;
	}

	@Override
	public float getPrefHeight() {
		return ART_H * scale;
	}

	@Override
	public void act(float delta) {
		super.act(delta);
		bar.update(delta, target);
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		frame.setBounds(getX(), getY(), getWidth(), getHeight());
		frame.setColor(1f, 1f, 1f, getColor().a * parentAlpha);
		bar.draw((SpriteBatch) batch, frame, tint);
		frame.draw(batch);
		batch.setColor(Color.WHITE);
	}
}
