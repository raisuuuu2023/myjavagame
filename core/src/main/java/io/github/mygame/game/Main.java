package io.github.mygame.game;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Main implements ApplicationListener {
    private AssetManager assetManager;
    private Texture backgroundTexture;
    private Texture startScreenBackground;
    private Texture newtonTexture;
    private Texture appleTexture;
    private Texture coconutTexture;
    private Texture particleTexture;

    private Sound appleFallSound;
    private Sound ouchSound;
    private Music backgroundMusic;
    private SpriteBatch spriteBatch;
    private FitViewport viewport;
    private Sprite newtonSprite;
    private Vector2 touchPos;
    private Array<Sprite> fallingObjects;
    private float spawnTimer;
    private Rectangle newtonRectangle;
    private Rectangle objectRectangle;
    private boolean gameOver;
    private boolean showStartButton = true;
    private BitmapFont font;
    private GlyphLayout layout;
    private int score;

    private Rectangle startButtonBounds;

    private class Particle {
        Sprite sprite;
        float velocityX, velocityY;
        float life;

        Particle(float x, float y) {
            sprite = new Sprite(particleTexture);
            sprite.setSize(0.15f, 0.15f);
            sprite.setPosition(x, y);
            velocityX = MathUtils.random(-1f, 1f);
            velocityY = MathUtils.random(1f, 3f);
            life = MathUtils.random(0.5f, 1f);
        }

        void update(float delta) {
            life -= delta;
            sprite.translate(velocityX * delta, velocityY * delta);
            sprite.setColor(1, 1, 1, Math.max(0, life));
        }

        boolean isDead() {
            return life <= 0;
        }

        void draw(SpriteBatch batch) {
            sprite.draw(batch);
        }
    }
    private Array<Particle> particles;

    @Override
    public void create() {
        assetManager = new AssetManager();

        assetManager.load("math.jpg", Texture.class);
        assetManager.load("background2.jpg", Texture.class);
        assetManager.load("Newton.png", Texture.class);
        assetManager.load("Apple.png", Texture.class);
        assetManager.load("coconut.png", Texture.class);
        assetManager.load("drop.mp3", Sound.class);
        assetManager.load("ou.mp3", Sound.class);
        assetManager.load("music.mp3", Music.class);
        assetManager.finishLoading();

        backgroundTexture = assetManager.get("math.jpg", Texture.class);
        startScreenBackground = assetManager.get("background2.jpg", Texture.class);
        newtonTexture = assetManager.get("Newton.png", Texture.class);
        appleTexture = assetManager.get("Apple.png", Texture.class);
        coconutTexture = assetManager.get("coconut.png", Texture.class);
        appleFallSound = assetManager.get("drop.mp3", Sound.class);
        ouchSound = assetManager.get("ou.mp3", Sound.class);
        backgroundMusic = assetManager.get("music.mp3", Music.class);

        particleTexture = new Texture(Gdx.files.internal("particle.png"));

        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(8, 6);

        newtonSprite = new Sprite(newtonTexture);
        newtonSprite.setSize(2, 1);
        newtonSprite.setPosition(2, 0.1f);

        touchPos = new Vector2();
        fallingObjects = new Array<>();
        particles = new Array<>();

        newtonRectangle = new Rectangle();
        objectRectangle = new Rectangle();
        gameOver = false;
        score = 0;

        font = new BitmapFont(Gdx.files.internal("font.fnt"));
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        font.getData().setScale(0.06f);
        font.setColor(Color.WHITE);
        layout = new GlyphLayout();

        startButtonBounds = new Rectangle();

        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(0.5f);
        backgroundMusic.play();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void render() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (showStartButton) {
            drawStartButton();
            handleStartButtonInput();
        } else if (gameOver) {
            drawGameScreen();
            drawGameOver();
            input();
        } else {
            drawGameScreen();
            input();
            logic();
        }
    }

    private void drawStartButton() {
        spriteBatch.begin();

        spriteBatch.setColor(Color.WHITE);
        spriteBatch.draw(startScreenBackground, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());

        String buttonText = "START";
        float buttonWidth = 3f;
        float buttonHeight = 0.7f;
        float buttonX = (viewport.getWorldWidth() - buttonWidth) / 2f - 0.5f;
        float buttonY = (viewport.getWorldHeight() - buttonHeight) / 2f + 0.5f;

        startButtonBounds.set(buttonX, buttonY, buttonWidth, buttonHeight);


        spriteBatch.setColor(0, 0, 0, 0.6f);
        spriteBatch.draw(backgroundTexture, buttonX, buttonY, buttonWidth, buttonHeight);

        spriteBatch.setColor(Color.WHITE);
        layout.setText(font, buttonText);
        float textX = buttonX + (buttonWidth - layout.width) / 2f;
        float textY = buttonY + (buttonHeight + layout.height) / 2f - 0.1f;

        font.draw(spriteBatch, buttonText, textX, textY);

        spriteBatch.end();
    }

    private void handleStartButtonInput() {
        if (Gdx.input.justTouched()) {
            Vector2 touch = new Vector2(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(touch);
            if (startButtonBounds.contains(touch.x, touch.y)) {
                showStartButton = false;
                score = 0;
                gameOver = false;
                fallingObjects.clear();
                particles.clear();
                backgroundMusic.play();
                newtonSprite.setPosition(2, 0.1f);
            }
        }
    }

    private void drawGameScreen() {
        spriteBatch.begin();

        spriteBatch.setColor(Color.WHITE);

        spriteBatch.draw(backgroundTexture, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        newtonSprite.draw(spriteBatch);

        for (Sprite obj : fallingObjects) {
            obj.draw(spriteBatch);
        }

        for (Particle p : particles) {
            p.draw(spriteBatch);
        }

        String scoreText = "SCORE " + score;
        layout.setText(font, scoreText);
        float x = 0.2f;
        float y = viewport.getWorldHeight() - 0.2f;

        font.draw(spriteBatch, scoreText, x, y);

        spriteBatch.end();
    }

    private void input() {
        float speed = 4f;
        float delta = Gdx.graphics.getDeltaTime();

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            newtonSprite.translateX(speed * delta);
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            newtonSprite.translateX(-speed * delta);
        }

        if (Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(touchPos);
            newtonSprite.setCenterX(touchPos.x);
        }

        float worldWidth = viewport.getWorldWidth();
        newtonSprite.setX(MathUtils.clamp(newtonSprite.getX(), 0, worldWidth - newtonSprite.getWidth()));
    }

    private void logic() {
        float delta = Gdx.graphics.getDeltaTime();
        newtonRectangle.set(newtonSprite.getX(), newtonSprite.getY(), newtonSprite.getWidth(), newtonSprite.getHeight());

        for (int i = particles.size - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.update(delta);
            if (p.isDead()) {
                particles.removeIndex(i);
            }
        }

        for (int i = fallingObjects.size - 1; i >= 0; i--) {
            Sprite obj = fallingObjects.get(i);
            obj.translateY(-2f * delta);
            objectRectangle.set(obj.getX(), obj.getY(), obj.getWidth(), obj.getHeight());

            if (obj.getY() < -obj.getHeight()) {
                if (obj.getTexture() == appleTexture) {
                    gameOver = true;
                    backgroundMusic.stop();
                }
                fallingObjects.removeIndex(i);
                break;
            } else if (newtonRectangle.overlaps(objectRectangle)) {
                if (obj.getTexture() == coconutTexture) {
                    ouchSound.play();
                    gameOver = true;
                    backgroundMusic.stop();
                } else {
                    appleFallSound.play();
                    score++;

                    float px = obj.getX() + obj.getWidth() / 2f;
                    float py = obj.getY() + obj.getHeight() / 2f;
                    for (int j = 0; j < 10; j++) {
                        particles.add(new Particle(px, py));
                    }
                }
                fallingObjects.removeIndex(i);
            }
        }

        if (!gameOver) {
            spawnTimer += delta;
            if (spawnTimer > 1f) {
                spawnTimer = 0;
                spawnFallingObject();
            }
        }
    }

    private void spawnFallingObject() {
        Sprite obj;
        if (MathUtils.random(1, 4) == 1) {
            obj = new Sprite(coconutTexture);
        } else {
            obj = new Sprite(appleTexture);
        }

        obj.setSize(0.8f, 0.8f);
        obj.setPosition(
            MathUtils.random(0, viewport.getWorldWidth() - obj.getWidth()),
            viewport.getWorldHeight()
        );
        fallingObjects.add(obj);
    }

    private void drawGameOver() {
        spriteBatch.begin();

        spriteBatch.setColor(0, 0, 0, 0.3f);
        spriteBatch.draw(backgroundTexture, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        spriteBatch.setColor(Color.WHITE);

        String text = "GAME OVER";
        layout.setText(font, text);
        float x = (viewport.getWorldWidth() - layout.width) / 2f;
        float y = viewport.getWorldHeight() / 2f + layout.height;
        font.draw(spriteBatch, text, x, y);

        spriteBatch.end();
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {
        assetManager.dispose();
        spriteBatch.dispose();
        font.dispose();
        particleTexture.dispose();
        startScreenBackground.dispose();
    }
}
