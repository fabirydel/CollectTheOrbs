package com.spaceship.handlers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

/**
 * Purchase celebration: a mechanical shutter slams shut over the card that was just bought, its lock light turns from
 * red to green, and the two plates grind open again with sparks flying from the seam, revealing the unlocked card.
 *
 * The plates slide in from the card's left and right edges and retract back into them (they never cover the neighbours).
 * Everything is in screen pixels; draw it after the menus, inside a batch.
 */
public class MechDoor {
	// timeline, seconds
	private static final float SLAM = 0.13f, UNLOCK = 0.36f, JOLT = 0.46f, JOLT_END = 0.52f, OPEN = 0.62f, END = 1.12f;
	private static final int MAX = 160;
	/** how far inside the card's edge the shutter sits, as a fraction of the card's shorter side */
	private static final float INSET = 0.035f;

	private Texture metal, cap, grip, screw;
	private float x, y, w, h, age = 99f, unit = 1f;
	private boolean slammed, unlocked, jolted;

	// sparks: hot bits of metal that fly off and fall
	private final float[] px = new float[MAX], py = new float[MAX], vx = new float[MAX], vy = new float[MAX], pa = new float[MAX], pl = new float[MAX], ps = new float[MAX];
	private int count;

	private void load() {
		if (metal != null)
			return;
		metal = new Texture(Gdx.files.internal("res/fx/door_metal.png"));
		cap = new Texture(Gdx.files.internal("res/fx/door_cap.png"));
		cap.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		grip = new Texture(Gdx.files.internal("res/fx/door_grip.png"));
		screw = new Texture(Gdx.files.internal("res/fx/door_screw.png"));
		metal.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		grip.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		screw.setFilter(TextureFilter.Linear, TextureFilter.Linear);
	}

	/** lock light colours: the orbs that paid for the purchase */
	public static final float[] BLUE = { 0.25f, 0.85f, 1f }, RED = { 1f, 0.25f, 0.4f }, GOLD = { 1f, 0.8f, 0.25f };
	private float[] leftLight = GOLD, rightLight = GOLD;

	/**
	 * Play the shutter over the card at (x, y) with size (w, h), in screen pixels (y up).
	 * @param left colour of the lock light on the left plate (BLUE, RED or GOLD)
	 * @param right colour of the lock light on the right plate
	 */
	public void play(float x, float y, float w, float h, float[] left, float[] right) {
		load();
		leftLight = left;
		rightLight = right;
		// the shutter sits inside the card's neon border, so the border stays visible around it
		float inset = INSET*Math.min(w, h);
		this.x = x + inset;
		this.y = y + inset;
		this.w = w - 2f*inset;
		this.h = h - 2f*inset;
		unit = Gdx.graphics.getWidth()/720f;
		age = 0f;
		count = 0;
		slammed = unlocked = jolted = false;
	}

	public boolean playing() {
		return age < END + 0.8f && (age < END || count > 0);
	}

	/** how far each plate is retracted: 1 = fully open (hidden in the card's edge), 0 = shut */
	private float openness() {
		if (age < SLAM) {
			float t = age/SLAM;
			return 1f - t*t;				// accelerates shut
		}
		if (age < JOLT)
			return 0f;
		if (age < JOLT_END)
			return 0.07f*(age - JOLT)/(JOLT_END - JOLT);		// the lock lets go: a short jerk
		if (age < OPEN)
			return 0.07f;
		if (age < END) {
			float t = (age - OPEN)/(END - OPEN);
			return 0.07f + 0.93f*t*t*(3f - 2f*t);
		}
		return 1f;
	}

	private void spark(float sx, float sy, float dirX, float speed, float spread) {
		if (count >= MAX)
			return;
		int i = count++;
		float ang = MathUtils.random(-spread, spread);
		float sp = speed*MathUtils.random(0.35f, 1f)*unit;
		px[i] = sx;
		py[i] = sy;
		vx[i] = dirX*MathUtils.cos(ang)*sp;
		vy[i] = MathUtils.sin(ang)*sp + 120f*unit;
		pa[i] = 0f;
		pl[i] = MathUtils.random(0.3f, 0.75f);
		ps[i] = MathUtils.random(5f, 11f)*unit;
	}

	private void burst(int n, float speed) {
		float seam = x + w/2;
		for (int i = 0; i < n; i++)
			spark(seam, y + MathUtils.random(0.05f, 0.95f)*h, MathUtils.randomBoolean() ? 1f : -1f, speed, 1.1f);
	}

	private void update(float dt) {
		age += dt;
		if (!slammed && age >= SLAM) {
			slammed = true;
			burst(26, 620f);
		}
		if (!unlocked && age >= UNLOCK)
			unlocked = true;
		if (!jolted && age >= JOLT) {
			jolted = true;
			burst(34, 760f);
		}
		if (age >= OPEN && age < OPEN + 0.22f) {
			// grinding open: sparks keep spitting from the top and bottom of the widening gap
			float gap = openness()*w/2;
			for (int k = 0; k < 3; k++) {
				boolean left = MathUtils.randomBoolean();
				spark(x + w/2 + (left ? -gap : gap), MathUtils.randomBoolean() ? y + h*0.04f : y + h*0.96f, left ? 1f : -1f, 420f, 1.3f);
			}
		}
		float g = 1500f*unit;
		for (int i = count - 1; i >= 0; i--) {
			pa[i] += dt;
			if (pa[i] >= pl[i]) {
				count--;
				px[i] = px[count]; py[i] = py[count]; vx[i] = vx[count]; vy[i] = vy[count];
				pa[i] = pa[count]; pl[i] = pl[count]; ps[i] = ps[count];
				continue;
			}
			vy[i] -= g*dt;
			px[i] += vx[i]*dt;
			py[i] += vy[i]*dt;
		}
	}

	public void draw(SpriteBatch sb, float dt) {
		if (!playing() || metal == null)
			return;
		update(Math.min(dt, 0.05f));
		if (age < END) {
			float open = openness();
			float plate = (w/2)*(1f - open);
			// the clank when the plates meet and when the lock lets go
			float jx = 0f, jy = 0f;
			float since = age >= JOLT ? age - JOLT : age >= SLAM ? age - SLAM : 9f;
			if (since < 0.09f) {
				float k = (1f - since/0.09f)*1.5f*unit;
				jx = MathUtils.random(-k, k);
				jy = MathUtils.random(-k, k);
			}
			float fade = age > END - 0.08f ? (END - age)/0.08f : 1f;
			drawPlate(sb, x + jx, y + jy, plate, false, fade);
			drawPlate(sb, x + w + jx, y + jy, plate, true, fade);

			Fx.additiveOn(sb);
			float seam = x + w/2 + jx;
			// lock light on each plate beside the seam, in the colour of the orbs that paid: it flickers while locked and
			// burns steady and bright once released
			// (hidden once the plate has retracted too far to carry it, so it never sits on the card's border)
			if (open*w/2 + 46f*unit + 16f*unit < w/2) {
				float blink = unlocked ? 1f : 0.5f + 0.3f*MathUtils.sin(age*38f);
				float ls = 26f*unit;
				for (int side = -1; side <= 1; side += 2) {
					float lx = seam + side*(open*w/2 + 46f*unit), ly = y + h/2 + jy;
					float[] c = side < 0 ? leftLight : rightLight;
					sb.setColor(c[0], c[1], c[2], fade*blink);
					sb.draw(Fx.glow(), lx - ls/2, ly - ls/2, ls, ls);
					if (unlocked) {
						// a soft halo once it is lit for good
						sb.setColor(c[0], c[1], c[2], fade*0.45f);
						sb.draw(Fx.glow(), lx - ls, ly - ls, ls*2f, ls*2f);
					}
					sb.setColor(1f, 1f, 1f, fade*blink*0.8f);
					sb.draw(Fx.glow(), lx - ls*0.2f, ly - ls*0.2f, ls*0.4f, ls*0.4f);
				}
			}
			// light spilling through the gap as it opens
			if (age >= JOLT) {
				float k = Math.max(0f, 1f - (age - JOLT)/0.5f);
				float gw = Math.min(w, open*w + 34f*unit);
				sb.setColor(1f, 0.82f, 0.45f, 0.85f*k*fade);
				sb.draw(Fx.glow(), seam - gw/2, y, gw, h);
			}
			// a hot flash on the seam when the plates meet
			if (age >= SLAM && age < SLAM + 0.12f) {
				float k = 1f - (age - SLAM)/0.12f;
				float gw = 60f*unit;
				sb.setColor(1f, 0.9f, 0.7f, k);
				sb.draw(Fx.glow(), seam - gw/2, y, gw, h);
			}
			Fx.additiveOff(sb);
		}
		// sparks: bright streaks along their path, white-hot then orange
		Fx.additiveOn(sb);
		for (int i = 0; i < count; i++) {
			float t = pa[i]/pl[i];
			float a = 1f - t*t;
			float sp = (float) Math.sqrt(vx[i]*vx[i] + vy[i]*vy[i]) + 0.001f;
			float len = Math.min(46f*unit, sp*0.035f) + ps[i];
			Fx.tail(sb, true, px[i], py[i], -vx[i]/sp, -vy[i]/sp, len, ps[i]*0.7f, 1f, 0.55f + 0.45f*(1f - t), 0.2f + 0.7f*(1f - t)*(1f - t), a);
			sb.setColor(1f, 0.9f, 0.7f, a);
			sb.draw(Fx.glow(), px[i] - ps[i]/2, py[i] - ps[i]/2, ps[i], ps[i]);
		}
		Fx.additiveOff(sb);
	}

	/** one plate, growing from its outer edge toward the seam; mirrored draws the right-hand plate */
	private void drawPlate(SpriteBatch sb, float edgeX, float py0, float width, boolean mirrored, float alpha) {
		if (width < 1f)
			return;
		sb.setColor(1f, 1f, 1f, alpha);
		float gw = Math.min(width, h*grip.getWidth()/grip.getHeight()*0.55f);
		// the outer end has rounded corners, like the card it covers
		float cw = Math.min(width - gw, h*cap.getWidth()/cap.getHeight());
		float body = width - gw - cw;
		float dir = mirrored ? -1f : 1f;		// the right-hand plate is the left one flipped
		if (cw > 0f)
			sb.draw(cap, edgeX, py0, dir*cw, h);
		if (body > 0f)
			sb.draw(metal, edgeX + dir*cw, py0, dir*body, h);
		sb.draw(grip, edgeX + dir*(cw + body), py0, dir*gw, h);
		// screws near the outer corners, as long as there is room for them
		float s = h*0.11f;
		if (cw + body > s*2.4f) {
			float sx0 = mirrored ? edgeX - s*1.9f : edgeX + s*0.9f;
			sb.draw(screw, sx0, py0 + s*0.9f, s, s);
			sb.draw(screw, sx0, py0 + h - s*1.9f, s, s);
		}
		sb.setColor(1f, 1f, 1f, 1f);
	}

	public void dispose() {
		if (metal != null) {
			metal.dispose();
			cap.dispose();
			grip.dispose();
			screw.dispose();
			metal = null;
		}
	}
}
