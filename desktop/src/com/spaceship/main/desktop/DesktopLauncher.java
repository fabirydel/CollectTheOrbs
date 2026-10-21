package com.spaceship.main.desktop;

import java.io.File;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.spaceship.main.Main;

public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setTitle("Collect The Orbs");
		// Pixel 8 Pro shape (20:9). Normal play uses a window that fits a laptop screen; the dev driver keeps 448x997
		// because its scripted tap coordinates are written for that size.
		boolean dev = System.getenv("CTO_DEV_DIR") != null;
		config.setWindowedMode(dev ? 448 : 360, dev ? 997 : 800);
		config.setResizable(false);
		config.useVsync(true);
		String devDir = System.getenv("CTO_DEV_DIR");
		ApplicationListener game = devDir != null ? new DevMain(new DevHarness(new File(devDir))) : new Main(0, 0);
		new Lwjgl3Application(game, config);
	}
}
