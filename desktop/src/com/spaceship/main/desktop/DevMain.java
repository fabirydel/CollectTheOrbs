package com.spaceship.main.desktop;

import com.spaceship.main.Main;

public class DevMain extends Main {
	private final DevHarness harness;

	public DevMain(DevHarness harness) {
		super(0, 0);
		this.harness = harness;
		// the test driver has no ad/billing SDK: a do-nothing resolver so the Settings "Remove ads" rows show up
		Main.setActionResolver((com.spaceship.handlers.ActionResolver) java.lang.reflect.Proxy.newProxyInstance(
				getClass().getClassLoader(), new Class<?>[] { com.spaceship.handlers.ActionResolver.class },
				(proxy, method, args) -> method.getReturnType() == boolean.class ? Boolean.FALSE : method.getReturnType() == int.class ? Integer.valueOf(0) : null));
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
