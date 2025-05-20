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


abstract class GameEntity {
    protected float x, y;
    protected Texture texture;

    public GameEntity(Texture texture, float x, float y) {
        this.texture = texture;
        this.x = x;
        this.y = y;
    }

    public abstract void update(float delta);

    public void draw(SpriteBatch batch) {
        batch.draw(texture, x, y);
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }
}

interface Drawable {
    void draw(SpriteBatch batch);
}


class Apple extends GameEntity implements Drawable {
    public Apple(Texture texture, float x, float y) {
        super(texture, x, y);
    }

    @Override
    public void update(float delta) {

        y -= 2 * delta;
    }
}


class Coconut extends GameEntity implements Drawable {
    public Coconut(Texture texture, float x, float y) {
        super(texture, x, y);
    }

    @Override
    public void update(float delta) {
        y -= 2 * delta;
    }
}


public class game implements ApplicationListener {
    private static final float GAME_WIDTH = 8f;
    private static final float GAME_HEIGHT = 6f;
    private static int totalPlayers = 0;

    private AssetManager assetManager;
    private Texture backgroundTexture;
    private Texture startScreenBackground;
    private Texture newtonTexture;
    private Texture appleTexture;
    private Texture coconutTexture;
    private Sound appleFallSound;
    private Sound ouchSound;
    private Music backgroundMusic;
    private SpriteBatch spriteBatch;
    private FitViewport viewport;
    private Sprite newtonSprite;
    private Vector2 touchPos;
    private Array<GameEntity> fallingObjects;
    private float spawnTimer;
    private Rectangle newtonRectangle;
    private Rectangle objectRectangle;
    private boolean gameOver;
    private boolean showStartButton = true;
    private BitmapFont font;
    private GlyphLayout layout;
    private int score;
    private Rectangle startButtonBounds;

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

        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(GAME_WIDTH, GAME_HEIGHT);

        newtonSprite = new Sprite(newtonTexture);
        newtonSprite.setSize(2, 1);
        newtonSprite.setPosition(2, 0.1f);

        touchPos = new Vector2();
        fallingObjects = new Array<>();
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

        totalPlayers++;
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

        for (GameEntity obj : fallingObjects) {
            obj.draw(spriteBatch);
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

        for (int i = fallingObjects.size - 1; i >= 0; i--) {
            GameEntity obj = fallingObjects.get(i);
            obj.update(delta);
            objectRectangle.set(obj.getX(), obj.getY(), obj.texture.getWidth(), obj.texture.getHeight());

            if (obj.getY() < -obj.texture.getHeight()) {
                if (obj instanceof Apple) {
                    gameOver = true;
                    backgroundMusic.stop();
                }
                fallingObjects.removeIndex(i);
                break;
            } else if (newtonRectangle.overlaps(objectRectangle)) {
                if (obj instanceof Coconut) {
                    ouchSound.play();
                    gameOver = true;
                    backgroundMusic.stop();
                } else {
                    appleFallSound.play();
                    score++;
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
        GameEntity obj;
        if (MathUtils.random(1, 4) == 1) {
            obj = new Coconut(coconutTexture, MathUtils.random(0, viewport.getWorldWidth() - 0.8f), viewport.getWorldHeight());
        } else {
            obj = new Apple(appleTexture, MathUtils.random(0, viewport.getWorldWidth() - 0.8f), viewport.getWorldHeight());
        }
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
