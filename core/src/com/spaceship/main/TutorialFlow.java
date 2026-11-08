package com.spaceship.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Align;
import com.spaceship.handlers.Fx;
import com.spaceship.objects.Ball;
import com.spaceship.objects.Spaceship;

/**
 * The guided first run: a few slow, scripted orbs and one line of text per step, played in the player's first game.
 * Nothing in it can be failed (no energy is lost, the run cannot end); when it finishes the counters are reset and the
 * real game starts. Steps: move, catch a blue orb, switch the shield and catch a red orb, wrong colour costs energy,
 * keep both colours even.
 */
class TutorialFlow {
	private static final int MOVE = 0, BLUE = 1, RED = 2, WRONG = 3, LIFE = 4, ITEM = 5, USE = 6, GAIN = 7, TIP = 8, EVEN = 9, READY = 10, GO = 11, DONE = 12;
	private static final int ORB_SPEED = 240;

	private final PlayScreen ps;
	private final Main game;
	private final Texture px;
	private final GlyphLayout layout = new GlyphLayout();

	private int step = MOVE;
	private float stepTime;
	private float travelL, travelR;
	private float lastX = -1f;
	private Ball orb;
	private int tries;
	private int evenSpawned;
	private float nextSpawn;
	private float pulse;
	private boolean skipRequested;
	private float orbX;
	private int energyBefore;

	TutorialFlow(PlayScreen ps, Main game) {
		this.ps = ps;
		this.game = game;
		Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
		pm.setColor(1f, 1f, 1f, 1f);
		pm.fill();
		px = new Texture(pm);
		pm.dispose();
	}

	boolean isFinished() {
		return step == DONE;
	}

	void dispose() {
		px.dispose();
	}

	private boolean english() {
		return game.getMMS().language == game.getMMS().ON;
	}

	private String text() {
		boolean en = english();
		switch (step) {
		case MOVE:
			return en ? "Hold the left or right side of the screen to move" : "Toca el lado izquierdo o derecho de la pantalla para moverte";
		case BLUE:
			return en ? "Blue orb, blue shield.\nCatch it!" : "Orbe azul, escudo azul.\nAtr\u00e1palo!";
		case RED:
			return en ? "Red orb? Tap the middle to switch your shield" : "Orbe rojo? Toca el centro para cambiar de escudo";
		case WRONG:
			return en ? "The wrong colour drains your energy" : "El color equivocado te quita energ\u00eda";
		case LIFE:
			return en ? "Yellow orbs refill your energy" : "Los orbes amarillos recargan tu energ\u00eda";
		case ITEM:
			return en ? "Catch power-up orbs for a boost" : "Atrapa orbes de poder para un impulso";
		case USE:
			return en ? "Tap the middle to use it" : "Toca el centro para usarlo";
		case GAIN:
			return en ? "Blue orbs count double for as long as the power-up is in effect!" : "\u00a1Los orbes azules valen doble mientras el poder est\u00e9 activo!";
		case TIP:
			return en ? "There are more power-ups that can help you..." : "Hay m\u00e1s poderes que te pueden ayudar...";
		case EVEN:
			return en ? "Your score is the colour you collect the least.\nKeep them even!" : "Tu puntaje es el color que menos recoges.\n\u00a1Mantenlos parejos!";
		case READY:
			return en ? "Ready?" : "Listo?";
		case GO:
			return "GO!";
		}
		return "";
	}

	/** advances the script; call once per frame while the game is playing */
	void update(float dt) {
		dt = Math.min(dt, 0.1f);
		stepTime += dt;
		pulse += dt;
		if (skipRequested) {
			finish();
			return;
		}
		switch (step) {
		case MOVE: {
			float x = ps.tutShipX();
			if (lastX >= 0f) {
				if (x < lastX)
					travelL += lastX - x;
				else
					travelR += x - lastX;
			}
			lastX = x;
			float need = 0.06f * Gdx.graphics.getWidth();
			if (stepTime > 1.0f && travelL > need && travelR > need)
				next(0.6f);
			break;
		}
		case BLUE:
			if (stepTime > 0.5f && orb == null) {
				ps.tutSetShield(Spaceship.STATE_BLUE);
				orb = spawn(Ball.B_BLUE);
			}
			if (orb != null && !ps.tutAlive(orb)) {
				if (ps.tutBlues() >= 1)
					next(0.9f);
				else if (++tries < 4) {
					orb = null;
					stepTime = 0.2f;
				} else
					next(0.2f);
			}
			break;
		case RED:
			if (stepTime > 0.5f && orb == null)
				orb = spawn(Ball.B_RED);
			if (orb != null && !ps.tutAlive(orb)) {
				if (ps.tutReds() >= 1)
					next(0.9f);
				else if (++tries < 4) {
					orb = null;
					stepTime = 0.2f;
				} else
					next(0.2f);
			}
			break;
		case WRONG:
			if (stepTime > 0.5f && orb == null) {
				ps.tutSetShield(Spaceship.STATE_RED);
				ps.tutFillEnergy();
				ps.tutLock(true);
				float w = Gdx.graphics.getWidth();
				float sx = ps.tutShipX();
				orbX = MathUtils.clamp(sx + (sx < 0.5f * w ? 0.25f : -0.25f) * w, 0.12f * w, 0.88f * w);
				orb = ps.tutSpawn(Ball.B_BLUE, orbX, ORB_SPEED);
			}
			if (orb != null) {
				if (ps.tutAlive(orb)) {
					if (stepTime > 1.0f)
						ps.tutDrive(orbX, dt);
				} else {
					ps.tutLock(false);
					next(1.4f);
				}
			}
			break;
		case LIFE:
			if (stepTime > 0.5f && orb == null) {
				energyBefore = ps.tutEnergy();
				orb = spawn(Ball.B_LIFE);
			}
			if (orb != null && !ps.tutAlive(orb)) {
				if (ps.tutEnergy() > energyBefore)
					next(0.9f);
				else if (++tries < 4) {
					orb = null;
					stepTime = 0.2f;
				} else
					next(0.2f);
			}
			break;
		case ITEM:
			if (stepTime > 0.5f && orb == null)
				orb = spawn(Ball.B_OBJECT);
			if (orb != null && !ps.tutAlive(orb)) {
				if (ps.tutItemHeld())
					next(0.9f);
				else {
					orb = null;
					stepTime = 0.2f;
				}
			}
			break;
		case USE:
			if (stepTime > 0f && !ps.tutItemHeld() && ps.tutBlueX2())
				next(0.1f);
			break;
		case GAIN:
			ps.tutKeepItem();
			if (stepTime > nextSpawn && evenSpawned < 3) {
				if (evenSpawned == 0)
					ps.tutSetShield(Spaceship.STATE_BLUE);
				orb = spawn(Ball.B_BLUE);
				evenSpawned++;
				nextSpawn = stepTime + 1.4f;
			}
			if (evenSpawned >= 3 && orb != null && !ps.tutAlive(orb)) {
				ps.tutEndItem();
				next(0.9f);
			}
			break;
		case TIP:
			if (stepTime > 3.6f)
				next(0.3f);
			break;
		case EVEN:
			if (stepTime > nextSpawn && evenSpawned < 5) {
				spawn(evenSpawned % 2 == 0 ? Ball.B_RED : Ball.B_BLUE);
				evenSpawned++;
				nextSpawn = stepTime + 1.9f;
			}
			if (evenSpawned >= 5 && stepTime > 11f)
				next(0f);
			break;
		case READY:
			if (stepTime > 1.1f)
				next(0f);
			break;
		case GO:
			if (stepTime > 0.9f)
				finish();
			break;
		}
	}

	private void next(float pause) {
		step++;
		stepTime = -pause;
		orb = null;
		tries = 0;
		if (step == GAIN) {
			evenSpawned = 0;
			nextSpawn = 0f;
		}
		if (step == EVEN) {
			evenSpawned = 0;
			nextSpawn = 0.6f;
		}
		if (step == GO || step == READY)
			stepTime = 0f;
	}

	private void finish() {
		ps.tutLock(false);
		step = DONE;
		Gdx.app.getPreferences("tutorial").putBoolean("done", true);
		Gdx.app.getPreferences("tutorial").flush();
		ps.tutFinished();
	}

	private Ball spawn(int type) {
		float w = Gdx.graphics.getWidth();
		float x = MathUtils.clamp(ps.tutShipX() + MathUtils.random(-0.22f, 0.22f) * w, 0.12f * w, 0.88f * w);
		return ps.tutSpawn(type, x, ORB_SPEED);
	}

	// ---- drawing (inside the play screen's open batch) ----

	void draw(SpriteBatch sb, BitmapFont font) {
		if (step == DONE)
			return;
		float w = Gdx.graphics.getWidth(), h = Gdx.graphics.getHeight();
		float u = Math.min(w / 720f, h / 1280f);
		float alpha = stepTime < 0f ? 0f : Math.min(1f, stepTime / 0.35f);
		if (step == GO)
			alpha = Math.max(0f, 1f - stepTime / 0.9f);

		// highlights under the text
		Fx.additiveOn(sb);
		float a = (0.45f + 0.35f * MathUtils.sin(pulse * 5f)) * alpha;
		switch (step) {
		case MOVE:
			pad(sb, ps.tutPadLeft(), a);
			pad(sb, ps.tutPadRight(), a);
			break;
		case RED:
			if (ps.tutShieldBlue())
				pad(sb, ps.tutPadShield(), 1f, 0.23f, 0.36f, a);
			else
				pad(sb, ps.tutPadShield(), 0.13f, 0.83f, 0.93f, a);
			break;
		case WRONG:
		case LIFE:
			box(sb, ps.tutLifeBar(), 1f, 0.75f, 0.18f, a, u);
			break;
		case USE:
			box(sb, ps.tutItemRing(), 0.75f, 0.35f, 1f, a, u);
			pad(sb, ps.tutPadItem(), 0.75f, 0.35f, 1f, a);
			break;
		case GAIN:
			counterBox(sb, true, 0.13f, 0.83f, 0.93f, a, u);
			break;
		case EVEN:
			// only the colour collected the least is the score
			if (ps.tutBlues() <= ps.tutReds())
				counterBox(sb, true, 0.13f, 0.83f, 0.93f, a, u);
			if (ps.tutReds() <= ps.tutBlues())
				counterBox(sb, false, 1f, 0.23f, 0.36f, a, u);
			break;
		}
		Fx.additiveOff(sb);

		// the line of text on a dark band
		float oldScaleX = font.getData().scaleX, oldScaleY = font.getData().scaleY;
		float s = (step == GO || step == READY) ? 1.5f * u : 0.52f * u;
		font.getData().setScale(s);
		font.setColor(1f, 1f, 1f, alpha);
		String t = text();
		layout.setText(font, t, font.getColor(), 0.84f * w, Align.center, true);
		float cy = 0.70f * h;
		float bandH = layout.height + 56f * u;
		sb.setColor(0f, 0f, 0f, 0.55f * alpha);
		float bandW = Math.min(w, layout.width + 56f * u);
		sb.setColor(1f, 1f, 1f, 1f);
		font.draw(sb, layout, 0.08f * w, cy + layout.height / 2);

		font.getData().setScale(oldScaleX, oldScaleY);
		font.setColor(1f, 1f, 1f, 1f);
		sb.setColor(1f, 1f, 1f, 1f);
	}

	/** pulses one of the on-screen control pads (the same boxes the Controls setting shows), drawn even when that setting is off */
	private void pad(SpriteBatch sb, Sprite pad, float a) {
		pad(sb, pad, 1f, 1f, 1f, a);
	}

	private void pad(SpriteBatch sb, Sprite pad, float r, float g, float b, float a) {
		if (pad == null)
			return;
		float oldR = pad.getColor().r, oldG = pad.getColor().g, oldB = pad.getColor().b, oldA = pad.getColor().a;
		pad.setColor(r, g, b, Math.min(1f, (ps.tutControlsOn() ? 0.6f : 0.85f) * a));
		pad.draw(sb);
		pad.setColor(oldR, oldG, oldB, oldA);
	}

	private void counterBox(SpriteBatch sb, boolean blue, float r, float g, float b, float a, float u) {
		float[] c = ps.tutCounterRect(blue);
		float pad = 18f * u;
		rect(sb, c[0] - pad, c[1] - pad, c[2] + 2 * pad, c[3] + 2 * pad, r, g, b, a, u);
	}

	/** a pulsing frame around a screen rectangle (a pad, the energy bar, a counter) */
	private void box(SpriteBatch sb, Sprite target, float r, float g, float b, float a, float u) {
		if (target == null)
			return;
		rect(sb, target.getX() - 6f * u, target.getY() - 6f * u, target.getWidth() + 12f * u, target.getHeight() + 12f * u, r, g, b, a, u);
	}

	private void rect(SpriteBatch sb, float x, float y, float bw, float bh, float r, float g, float b, float a, float u) {
		float t = 5f * u;
		sb.setColor(r, g, b, a);
		sb.draw(px, x, y, bw, t);
		sb.draw(px, x, y + bh - t, bw, t);
		sb.draw(px, x, y, t, bh);
		sb.draw(px, x + bw - t, y, t, bh);
		sb.setColor(r, g, b, a * 0.07f);
		sb.draw(px, x, y, bw, bh);
	}
}
