package com.ruin.castile.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.ruin.castile.Castile;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 1024;
		config.height = 768;
		config.vSyncEnabled = true;
		config.useGL30 = true;
		TexturePacker.Settings settings = new TexturePacker.Settings();
		TexturePacker.process(settings, "./images", "./packedimages", "game");
		new LwjglApplication(new Castile(), config);
	}
}
