package com.xoppa.gdx.shadertoy;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.StringBuilder;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;

public class GdxShaderToy extends ApplicationAdapter {
    Stage stage;
    Texture img;
    FullQuadToy toy;
    LogWindow logWindow;
    CollapsableTextWindow vertexWindow;
    CollapsableTextWindow fragmentWindow;
    float codeChangedTimer = -1f;
    long startTimeMillis;
    long fpsStartTimer;
    Logger logger;

    @Override
    public void create () {
        ShaderProgram.pedantic = false;
        startTimeMillis = TimeUtils.millis();
        fpsStartTimer = TimeUtils.nanoTime();
        Pixmap pixmap = new Pixmap(0, 0, Pixmap.Format.RGB565);

        img = new Texture(pixmap);
        pixmap.dispose();

        VisUI.load();
        VisUI.setDefaultTitleAlign(Align.center);
        //enable markup for colored text
        VisUI.getSkin().get("default-font", BitmapFont.class).getData().markupEnabled = true;
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        logger = new Logger("TEST", Logger.INFO) {
            StringBuilder sb = new StringBuilder();
            private void add(String message) {
                long time = TimeUtils.timeSinceMillis(startTimeMillis);
                long s = time / 1000;
                long m = s / 60;
                long h = m / 60;
                sb.setLength(0);
                //sb.append(h, 2).append(':').append(m % 60, 2).append(':').append(s % 60, 2).append('.').append(time % 1000, 3);
                sb.append(message).append("\n");
                logWindow.addText(sb.toString());
            }

            @Override
            public void info(String message) {
                super.info(message);
                //add("[#ffff00]" + message + "[]");
                add(message);
            }

            @Override
            public void error(String message) {
                super.error(message);
                //todo: fix markup color red
                //add("[red]" + message + "[]");
                add(message);
            }
        };



        toy = new FullQuadToy();
        toy.create(logger);
        toy.setTexture(img);

        ChangeListener codeChangeListener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                codeChangedTimer = 3f;
            }
        };

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        float hw = (w * 0.5f) - 100;
        float logHeight = 200f;
        logWindow = new LogWindow("Log", 0, 0, w, logHeight);
        stage.addActor(logWindow);

        //log basic debug info
        logger.info("libGDX: v" + com.badlogic.gdx.Version.VERSION);
        logger.info(Gdx.graphics.getGLVersion().getDebugVersionString());


        String vertexPath = "shaders/water.vertex.glsl";
        String fragmentPath = "shaders/water.fragment.glsl";
        //vertexPath ="shaders/stars.vertex.glsl";
        //fragmentPath = "shaders/stars.fragment.glsl";
        logger.info("loading: " + vertexPath + ", " + fragmentPath);
        final String defaultVS = Gdx.files.internal(vertexPath).readString();
        final String defaultFS = Gdx.files.internal(fragmentPath).readString();
        vertexWindow = new CollapsableTextWindow("Vertex Shader: " + vertexPath, 0, logHeight, hw, h - logHeight);
        vertexWindow.setText(defaultVS);
        vertexWindow.addTextAreaListener(codeChangeListener);
        stage.addActor(vertexWindow);
        fragmentWindow = new CollapsableTextWindow("Fragment Shader: " + fragmentPath, hw, logHeight, hw, h - logHeight);
        fragmentWindow.setText(defaultFS);
        fragmentWindow.addTextAreaListener(codeChangeListener);
        fragmentWindow.right();
        stage.addActor(fragmentWindow);

        // todo: Menubar
        //  [ ] File
        //      [ ] open -> FileExplorer -> load user fragment / vertex shader
        //      [ ] save
        //      [ ] load built in shaders -> assets.txt?
        //  [ ] Window
        //      [ ] Vertex Editor
        //      [ ] Fragment Editor
        //      [ ] Log

        toy.setShader(defaultVS, defaultFS);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        stage.getViewport().update(width, height);
        toy.resize(width, height);
        Gdx.graphics.setTitle("shadertoy - " + width + "x" + height);
    }

    @Override
    public void render() {
        update();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        toy.render();
        stage.act();
        stage.draw();
    }

    private void update() {
        if (codeChangedTimer > 0f) {
            codeChangedTimer -= Gdx.graphics.getDeltaTime();
            if (codeChangedTimer <= 0) {
                toy.setShader(vertexWindow.getText(), fragmentWindow.getText());
            }
        }
        if (TimeUtils.nanoTime() - fpsStartTimer > 1000000000) /* 1,000,000,000ns == one second */{
            //logger.info("fps: " + Gdx.graphics.getFramesPerSecond());
            fpsStartTimer = TimeUtils.nanoTime();
        }
    }

    @Override
    public void dispose() {
        VisUI.dispose();
        stage.dispose();
        img.dispose();
    }

}
