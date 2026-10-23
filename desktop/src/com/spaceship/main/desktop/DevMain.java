package com.spaceship.main.desktop;

import com.spaceship.main.Main;

public class DevMain extends Main {
	private final DevHarness harness;

	public DevMain(DevHarness harness) {
		super(0, 0);
		this.harness = harness;
	}

	@Override
	public void create() {
		super.create();
		harness.install();
		harness.speedHook = v -> { if (getPS() == null) return; if (v == -1) getPS().debugCharge(); else if (v == -2) getPS().debugKeepAlive(true); else getPS().debugSetTier(v); };
	}

	@Override
	public void render() {
		super.render();
		harness.afterRender();
	}
}
