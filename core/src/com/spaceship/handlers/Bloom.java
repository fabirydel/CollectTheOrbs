package com.spaceship.handlers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/**
 * Full-screen bloom: the screen is rendered into an off-screen buffer, its bright parts are blurred at quarter and
 * eighth resolution (a tight glow plus a wide halo), and the result is added back over the scene, so the orbs, shields,
 * engine plumes and neon signs bleed light.
 *
 * Use: {@link #begin()} before the screen renders, {@link #end()} after. If the device cannot keep up, it turns itself
 * off for the rest of the session (see {@link #watch(float)}).
 */
public class Bloom {
	/** master switch (e.g. for comparing with and without) */
	public static boolean ENABLED = true;
	/**
	 * 0..1: colour fringing toward the screen edges (red and blue pulled apart, like a lens under strain). The play screen
	 * raises it for a moment on a speed-up, a hard hit and the final hit; 0 = a clean picture.
	 */
	public static float fringe;
	/** how far apart the colours go at the screen edge at full strength, as a fraction of the screen */
	private static final float FRINGE_REACH = 0.022f;

	/** brightness (0..1) where the glow starts, and how gradually it comes in */
	private static final float THRESHOLD = 0.66f, KNEE = 0.30f;
	/** strength of the tight glow and of the wide halo */
	private static final float TIGHT = 0.50f, WIDE = 0.40f;

	private static final String VERT =
			"attribute vec4 a_position;\nattribute vec4 a_color;\nattribute vec2 a_texCoord0;\n"
			+ "uniform mat4 u_projTrans;\nvarying vec2 v_uv;\n"
			+ "void main(){ v_uv = a_texCoord0; gl_Position = u_projTrans * a_position; }";
	private static final String HEAD = "#ifdef GL_ES\nprecision mediump float;\n#endif\nvarying vec2 v_uv;\nuniform sampler2D u_texture;\n";
	// keeps only the bright parts, averaging four taps so small bright details do not shimmer
	private static final String BRIGHT = HEAD + "uniform vec2 u_px;\nuniform float u_thr;\nuniform float u_knee;\n"
			+ "void main(){\n"
			+ " vec3 c = texture2D(u_texture, v_uv + u_px*vec2(-1.0,-1.0)).rgb + texture2D(u_texture, v_uv + u_px*vec2(1.0,-1.0)).rgb\n"
			+ "  + texture2D(u_texture, v_uv + u_px*vec2(-1.0,1.0)).rgb + texture2D(u_texture, v_uv + u_px*vec2(1.0,1.0)).rgb;\n"
			+ " c *= 0.25;\n"
			+ " float b = max(c.r, max(c.g, c.b));\n"
			+ " float k = clamp((b - u_thr)/u_knee, 0.0, 1.0);\n"
			+ " gl_FragColor = vec4(c*k*k, 1.0);\n}";
	// 9-tap gaussian done with 5 bilinear samples
	private static final String BLUR = HEAD + "uniform vec2 u_dir;\n"
			+ "void main(){\n"
			+ " vec3 c = texture2D(u_texture, v_uv).rgb*0.2270270270;\n"
			+ " c += (texture2D(u_texture, v_uv + u_dir*1.3846153846).rgb + texture2D(u_texture, v_uv - u_dir*1.3846153846).rgb)*0.3162162162;\n"
			+ " c += (texture2D(u_texture, v_uv + u_dir*3.2307692308).rgb + texture2D(u_texture, v_uv - u_dir*3.2307692308).rgb)*0.0702702703;\n"
			+ " gl_FragColor = vec4(c, 1.0);\n}";
	private static final String COPY = HEAD + "void main(){ gl_FragColor = vec4(texture2D(u_texture, v_uv).rgb, 1.0); }";
	// scene + glow, combined like light on a screen so bright areas do not clip harshly
	private static final String COMPOSITE = HEAD + "uniform sampler2D u_tight;\nuniform sampler2D u_wide;\nuniform float u_a;\nuniform float u_b;\nuniform float u_fringe;\n"
			+ "void main(){\n"
			+ " vec2 d = (v_uv - 0.5)*u_fringe;\n"
			+ " vec3 s = vec3(texture2D(u_texture, v_uv + d).r, texture2D(u_texture, v_uv).g, texture2D(u_texture, v_uv - d).b);\n"
			+ " vec3 g = texture2D(u_tight, v_uv).rgb*u_a + texture2D(u_wide, v_uv).rgb*u_b;\n"
			+ " gl_FragColor = vec4(s + g*(1.0 - s*0.55), 1.0);\n}";

	private ShaderProgram bright, blur, copy, composite;
	private SpriteBatch quad;
	private FrameBuffer scene, a4, b4, a8, b8;
	private int w, h;
	private boolean failed, capturing;

	// frame-rate watchdog
	private float watched, slow;
	private int frames;
	private boolean tooSlow;

	public boolean active() {
		return ENABLED && !failed && !tooSlow;
	}

	private ShaderProgram compile(String frag) {
		ShaderProgram p = new ShaderProgram(VERT, frag);
		if (!p.isCompiled()) {
			Gdx.app.error("Bloom", p.getLog());
			failed = true;
		}
		return p;
	}

	private void ensure() {
		int bw = Gdx.graphics.getBackBufferWidth(), bh = Gdx.graphics.getBackBufferHeight();
		if (quad == null) {
			ShaderProgram.pedantic = false;
			bright = compile(BRIGHT);
			blur = compile(BLUR);
			copy = compile(COPY);
			composite = compile(COMPOSITE);
			quad = new SpriteBatch(2);
			quad.setProjectionMatrix(new com.badlogic.gdx.math.Matrix4().setToOrtho2D(0, 0, 1, 1));
			quad.disableBlending();
		}
		if (failed || (scene != null && bw == w && bh == h) || bw <= 0 || bh <= 0)
			return;
		disposeBuffers();
		w = bw;
		h = bh;
		try {
			scene = buffer(w, h);
			a4 = buffer(Math.max(1, w/4), Math.max(1, h/4));
			b4 = buffer(Math.max(1, w/4), Math.max(1, h/4));
			a8 = buffer(Math.max(1, w/8), Math.max(1, h/8));
			b8 = buffer(Math.max(1, w/8), Math.max(1, h/8));
		} catch (RuntimeException e) {
			Gdx.app.error("Bloom", "no off-screen buffers: " + e);
			failed = true;
		}
	}

	private static FrameBuffer buffer(int w, int h) {
		FrameBuffer f = new FrameBuffer(Pixmap.Format.RGB888, w, h, false);
		f.getColorBufferTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
		f.getColorBufferTexture().setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
		return f;
	}

	/** Start capturing the screen. Returns false (and captures nothing) when bloom is off. */
	public boolean begin() {
		capturing = false;
		if (!active())
			return false;
		ensure();
		if (failed)
			return false;
		scene.begin();
		capturing = true;
		return true;
	}

	/** Stop capturing and draw the scene with its glow to the screen. */
	public void end() {
		if (!capturing)
			return;
		capturing = false;
		scene.end();

		// bright parts at quarter resolution
		pass(scene.getColorBufferTexture(), a4, bright);
		// tight glow
		blur(a4, b4, 1f, 0f);
		blur(b4, a4, 0f, 1f);
		// wide halo at eighth resolution, blurred twice
		pass(a4.getColorBufferTexture(), a8, copy);
		for (int i = 0; i < 2; i++) {
			blur(a8, b8, 1f + i, 0f);
			blur(b8, a8, 0f, 1f + i);
		}

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		quad.setShader(composite);
		quad.begin();
		a4.getColorBufferTexture().bind(1);
		a8.getColorBufferTexture().bind(2);
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
		composite.setUniformi("u_tight", 1);
		composite.setUniformi("u_wide", 2);
		composite.setUniformf("u_a", TIGHT);
		composite.setUniformf("u_b", WIDE);
		composite.setUniformf("u_fringe", Math.max(0f, Math.min(1f, fringe))*FRINGE_REACH);
		quad.draw(scene.getColorBufferTexture(), 0, 0, 1, 1, 0, 0, 1, 1);
		quad.end();
	}

	private void pass(Texture src, FrameBuffer dst, ShaderProgram shader) {
		dst.begin();
		quad.setShader(shader);
		quad.begin();
		if (shader == bright) {
			shader.setUniformf("u_px", 1f/src.getWidth(), 1f/src.getHeight());
			shader.setUniformf("u_thr", THRESHOLD);
			shader.setUniformf("u_knee", KNEE);
		}
		quad.draw(src, 0, 0, 1, 1, 0, 0, 1, 1);
		quad.end();
		dst.end();
	}

	private void blur(FrameBuffer src, FrameBuffer dst, float dx, float dy) {
		dst.begin();
		quad.setShader(blur);
		quad.begin();
		blur.setUniformf("u_dir", dx/src.getWidth(), dy/src.getHeight());
		quad.draw(src.getColorBufferTexture(), 0, 0, 1, 1, 0, 0, 1, 1);
		quad.end();
		dst.end();
	}

	/**
	 * Call once per frame with the frame time while bloom is on. If the game runs below ~40 fps for most of a
	 * four-second window, bloom switches off for the rest of the session.
	 */
	public void watch(float dt) {
		if (tooSlow || dt > 0.1f)		// loading hitches and pauses do not count
			return;
		watched += dt;
		frames++;
		if (dt > 1f/40f)
			slow += dt;
		if (watched >= 4f) {
			if (frames > 30 && slow > watched*0.6f) {
				tooSlow = true;
				Gdx.app.log("Bloom", "turned off: device too slow");
			}
			watched = 0;
			slow = 0;
			frames = 0;
		}
	}

	private void disposeBuffers() {
		FrameBuffer[] all = { scene, a4, b4, a8, b8 };
		for (FrameBuffer f : all)
			if (f != null)
				f.dispose();
		scene = a4 = b4 = a8 = b8 = null;
	}

	public void dispose() {
		disposeBuffers();
		ShaderProgram[] all = { bright, blur, copy, composite };
		for (ShaderProgram p : all)
			if (p != null)
				p.dispose();
		if (quad != null)
			quad.dispose();
		quad = null;
	}
}
