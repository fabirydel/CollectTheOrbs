package com.spaceship.handlers;

/**
 * The run's speed, in one place. Time-based only (never score).
 *
 * The run is a series of tiers. Within a tier the orbs creep up a little; at the end of a tier there is a few seconds' CHARGE
 * phase, during which the ship plays its charge-up animation (orbs keep spawning), then the speed jumps to the next tier's
 * base speed (the BURST).
 *
 * Only advance it with update() while the game is actually playing (not paused, not dead).
 */
public class SpeedDirector {
	public static final int TIERS = 8;                 // tier 0 .. TIERS - 1
	public static final float TIER_SECONDS = 30f;      // play time spent in a tier before the charge-up starts
	public static final float CHARGE_SECONDS = 4f;     // length of the charge-up animation before each tier change
	public static final float BURST_AT = 3.2f;         // when, within the charge-up, the speed jumps
	public static final float START_SPEED = 350f;      // orb speed at the start of tier 0 (unchanged from the original game)
	public static final float TOP_SPEED = 1250f;       // orb speed in the last tier (unchanged from the original game)
	public static final float CREEP = 0.05f;           // extra speed (fraction) gained while inside a tier
	public static final float DENSITY_START = 13f;     // orbs on screen at the start (what the original game had)
	public static final float DENSITY_END = 9f;        // and at the top speed (about what the original ended with)
	public static final float START_SPAWN_GAP = 0.350f; // seconds between orbs at the start (unchanged)

	public enum Phase { RUN, CHARGE }

	private int tier;
	private float tierTime;
	private Phase phase = Phase.RUN;
	private float chargeTime;
	private boolean burstFired;
	private boolean burstPending;
	private boolean resumePending;

	public void reset() {
		tier = 0;
		tierTime = 0f;
		phase = Phase.RUN;
		chargeTime = 0f;
		burstFired = false;
		burstPending = false;
		resumePending = false;
	}

	public void update(float dt) {
		dt = Math.min(dt, 0.1f); // a long frame (app resumed, etc.) must not skip ahead
		if (phase == Phase.RUN) {
			tierTime += dt;
			if (tier < TIERS - 1 && tierTime >= TIER_SECONDS)
				startCharge();
		} else {
			chargeTime += dt;
			if (!burstFired && chargeTime >= BURST_AT) {
				burstFired = true;
				tier++;
				tierTime = 0f;
				burstPending = true;
			}
			if (chargeTime >= CHARGE_SECONDS) {
				phase = Phase.RUN;
				resumePending = true;
			}
		}
	}

	private void startCharge() {
		phase = Phase.CHARGE;
		chargeTime = 0f;
		burstFired = false;
	}

	/** Base speed of a tier: geometric from START_SPEED to TOP_SPEED, so every tier is the same relative step. */
	public static float baseSpeed(int tier) {
		return START_SPEED * (float) Math.pow(TOP_SPEED / START_SPEED, tier / (float) (TIERS - 1));
	}

	/** Current orb speed (design units per second). */
	public float speed() {
		float creep = tier < TIERS - 1 ? CREEP * Math.min(1f, tierTime / TIER_SECONDS) : 0f;
		if (phase == Phase.CHARGE && !burstFired)
			creep = CREEP; // hold the end-of-tier speed until the burst
		return baseSpeed(tier) * (1f + creep);
	}

	/** Seconds between orb spawns. Orb density (orbs on screen) glides from DENSITY_START to DENSITY_END over the tiers. */
	public float spawnGap() {
		float progress = Math.min(1f, (tier + Math.min(1f, tierTime / TIER_SECONDS)) / (float) (TIERS - 1));
		float density = DENSITY_START + (DENSITY_END - DENSITY_START) * progress;
		return START_SPAWN_GAP * (START_SPEED / speed()) * (DENSITY_START / density);
	}

	public Phase getPhase() {
		return phase;
	}

	/** Seconds into the charge-up (0 when not charging). */
	public float getChargeTime() {
		return phase == Phase.CHARGE ? chargeTime : 0f;
	}

	/** 0..1 through the tiers: how far along the run is (drives how fast things feel). */
	public int getTier() {
		return tier;
	}

	/** True once, on the frame the speed jumps. */
	public boolean consumeBurst() {
		boolean b = burstPending;
		burstPending = false;
		return b;
	}

	/** True once, on the frame orbs start spawning again after a charge-up. */
	public boolean consumeResume() {
		boolean b = resumePending;
		resumePending = false;
		return b;
	}

	/** Test hook: start the charge-up now. */
	public void debugCharge() {
		if (phase == Phase.RUN && tier < TIERS - 1)
			startCharge();
	}

	/** Test hook: jump to a tier immediately. */
	public void debugSetTier(int t) {
		tier = Math.max(0, Math.min(TIERS - 1, t));
		tierTime = 0f;
		phase = Phase.RUN;
	}
}
