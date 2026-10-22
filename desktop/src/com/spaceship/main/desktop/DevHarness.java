package com.spaceship.main.desktop;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.graphics.PixmapIO;

/**
 * Development-only test driver, enabled with the CTO_DEV_DIR environment variable. It reads
 * commands from CTO_DEV_DIR/cmd (tap X Y | drag X1 Y1 X2 Y2 | hold X Y FRAMES [SHOT] | speed ORB_SPEED (350..1250) | scroll X Y AMOUNT | wait MS | shot NAME | quit), replaces Gdx.input with a
 * proxy that reports scripted touches (the game polls Gdx.input), and saves screenshots of the
 * game's own framebuffer to CTO_DEV_DIR/NAME.png. It creates CTO_DEV_DIR/idle when the queue is empty.
 */
public class DevHarness {
	private final File dir;
	private final Deque<String> queue = new ArrayDeque<String>();
	private Input real;
	private boolean virtTouched, virtJust;
	private int vx, vy;
	private final Deque<int[]> frames = new ArrayDeque<int[]>();
	private long waitUntil;
	private String pendingShot;
	private String holdShot;
	public java.util.function.IntConsumer speedHook;

	public DevHarness(File dir) {
		this.dir = dir;
		dir.mkdirs();
	}

	public void install() {
		real = Gdx.input;
		Gdx.input = (Input) Proxy.newProxyInstance(Input.class.getClassLoader(), new Class[] { Input.class }, (proxy, m, args) -> {
			boolean firstPointer = args == null || args.length == 0 || (args[0] instanceof Integer && (Integer) args[0] == 0);
			if ((virtTouched || virtJust) && firstPointer) {
				String n = m.getName();
				if (n.equals("justTouched")) return virtJust;
				if (n.equals("isTouched")) return virtTouched;
				if (n.equals("getX")) return vx;
				if (n.equals("getY")) return vy;
			}
			try {
				return m.invoke(real, args);
			} catch (InvocationTargetException e) {
				throw e.getCause();
			}
		});
	}

	public void afterRender() {
		if (pendingShot != null) {
			capture(pendingShot);
			pendingShot = null;
		}
		virtJust = false;
		if (!frames.isEmpty()) {
			int[] f = frames.poll();
			vx = f[1];
			vy = f[2];
			InputProcessor p = real.getInputProcessor();
			if (f[0] == 0) {
				virtTouched = true;
				virtJust = true;
				if (p != null) log("down " + p.getClass().getSimpleName() + " " + p.touchDown(vx, vy, 0, Input.Buttons.LEFT));
			} else if (f[0] == 1) {
				if (p != null) log("drag " + vy + " " + p.touchDragged(vx, vy, 0));
			} else if (f[0] == 2) {
				if (p != null) p.touchUp(vx, vy, 0, Input.Buttons.LEFT);
				virtTouched = false;
			} else if (f[0] == 6) {
				pendingShot = holdShot;
			} else if (f[0] == 4) {
				if (p != null) p.mouseMoved(vx, vy);
			} else if (f[0] == 5) {
				if (p != null) p.scrolled(0, f[1]);
			}
			return;
		}
		if (TimeUtils.millis() < waitUntil) return;
		if (queue.isEmpty()) {
			readCommandFile();
			if (queue.isEmpty()) {
				new File(dir, "idle").getParentFile().mkdirs();
				try { new File(dir, "idle").createNewFile(); } catch (Exception e) { }
				return;
			}
		}
		String[] c = queue.poll().trim().split("\\s+");
		if (c[0].equals("tap")) {
			int x = Integer.parseInt(c[1]), y = Integer.parseInt(c[2]);
			frames.add(new int[] { 0, x, y });
			for (int i = 0; i < 3; i++) frames.add(new int[] { 3, x, y });
			frames.add(new int[] { 2, x, y });
		} else if (c[0].equals("drag")) {
			int x1 = Integer.parseInt(c[1]), y1 = Integer.parseInt(c[2]), x2 = Integer.parseInt(c[3]), y2 = Integer.parseInt(c[4]);
			int n = 20;
			frames.add(new int[] { 0, x1, y1 });
			for (int i = 1; i <= n; i++) frames.add(new int[] { 1, x1 + (x2 - x1) * i / n, y1 + (y2 - y1) * i / n });
			frames.add(new int[] { 2, x2, y2 });
		} else if (c[0].equals("hold")) {
			int x = Integer.parseInt(c[1]), y = Integer.parseInt(c[2]), n = Integer.parseInt(c[3]);
			holdShot = c.length > 4 ? c[4] : null;
			frames.add(new int[] { 0, x, y });
			for (int i = 0; i < n; i++) frames.add(new int[] { 1, x, y });
			if (holdShot != null) frames.add(new int[] { 6, x, y });
			frames.add(new int[] { 3, x, y });
			frames.add(new int[] { 2, x, y });
		} else if (c[0].equals("scroll")) {
			int x = Integer.parseInt(c[1]), y = Integer.parseInt(c[2]), amount = Integer.parseInt(c[3]);
			frames.add(new int[] { 4, x, y });
			frames.add(new int[] { 3, x, y });
			for (int i = 0; i < Math.abs(amount); i++) {
				frames.add(new int[] { 5, amount > 0 ? 1 : -1, y });
				frames.add(new int[] { 3, x, y });
			}
		} else if (c[0].equals("speed")) {
			int v = Integer.parseInt(c[1]);
			if (speedHook != null) speedHook.accept(v);
		} else if (c[0].equals("wait")) {
			waitUntil = TimeUtils.millis() + Long.parseLong(c[1]);
		} else if (c[0].equals("shot")) {
			pendingShot = c[1];
		} else if (c[0].equals("quit")) {
			Gdx.app.exit();
		}
	}

	private void log(String m) {
		try { Files.write(new File(dir, "harness.log").toPath(), (m + "\n").getBytes(), java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND); } catch (Exception e) { }
	}

	private void readCommandFile() {
		File f = new File(dir, "cmd");
		if (!f.exists()) return;
		try {
			List<String> lines = Files.readAllLines(f.toPath());
			f.delete();
			new File(dir, "idle").delete();
			for (String l : lines)
				if (!l.trim().isEmpty()) queue.add(l);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void capture(String name) {
		Pixmap pm = Pixmap.createFromFrameBuffer(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
		ByteBuffer b = pm.getPixels();
		for (int i = 3; i < b.limit(); i += 4)
			b.put(i, (byte) 255);
		PixmapIO.writePNG(Gdx.files.absolute(new File(dir, name + ".png").getAbsolutePath()), pm, -1, true);
		pm.dispose();
	}
}
