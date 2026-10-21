package com.spaceship.main.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.spaceship.main.Main;

public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setTitle("Collect The Orbs");
		config.setWindowedMode(420, 720);
		config.useVsync(true);
		new Lwjgl3Application(new Main(0, 0), config);
	}
}
