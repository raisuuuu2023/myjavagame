package io.github.mygame.game;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class Main implements ApplicationListener {
    Texture backgroundTexture;
    Texture newtonTexture;
    Texture appleTexture;
    Texture coconutTexture;
    Sound appleFallSound;
    Music backgroundMusic;
    SpriteBatch spriteBatch;
    FitViewport viewport;
    Sprite newtonSprite;
    Vector2 touchPos;
    Array<Sprite> fallingObjects;
    float spawnTimer;
    Rectangle newtonRectangle;
    Rectangle objectRectangle;
    boolean gameOver;
    BitmapFont font;
    GlyphLayout layout;

    @Override
    public void create() {
        backgroundTexture = new Texture(Gdx.files.internal("math.jpg"));
        newtonTexture = new Texture(Gdx.files.internal("Newton.png"));
        appleTexture = new Texture(Gdx.files.internal("Apple.png"));
        coconutTexture = new Texture(Gdx.files.internal("coconut.png"));
        appleFallSound = Gdx.audio.newSound(Gdx.files.internal("drop.mp3"));
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));

        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(8, 6);

        newtonSprite = new Sprite(newtonTexture);
        newtonSprite.setSize(2, 1);
        newtonSprite.setPosition(3, 0.5f);

        touchPos = new Vector2();
        fallingObjects = new Array<>();
        newtonRectangle = new Rectangle();
        objectRectangle = new Rectangle();
        gameOver = false;

        font = new BitmapFont();
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        font.getData().setScale(0.08f);
        layout = new GlyphLayout();

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

        if (gameOver) {
            drawGameScreen();
            drawGameOver();
        } else {
            drawGameScreen();
            input();
            logic();
        }
    }

    private void drawGameScreen() {
        spriteBatch.begin();
        spriteBatch.draw(backgroundTexture, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        newtonSprite.draw(spriteBatch);

        for (Sprite obj : fallingObjects) {
            obj.draw(spriteBatch);
        }
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
    }

    private void logic() {
        newtonSprite.setX(MathUtils.clamp(newtonSprite.getX(), 0, viewport.getWorldWidth() - newtonSprite.getWidth()));

        float delta = Gdx.graphics.getDeltaTime();
        newtonRectangle.set(newtonSprite.getX(), newtonSprite.getY(), newtonSprite.getWidth(), newtonSprite.getHeight());

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
                    gameOver = true;
                    backgroundMusic.stop();
                } else {
                    appleFallSound.play();
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
        float x = (viewport.getWorldWidth() - layout.width) / 2;
        float y = viewport.getWorldHeight() / 2 + layout.height;
        font.draw(spriteBatch, text, x, y);

        spriteBatch.end();
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {
        backgroundTexture.dispose();
        newtonTexture.dispose();
        appleTexture.dispose();
        coconutTexture.dispose();
        appleFallSound.dispose();
        backgroundMusic.dispose();
        spriteBatch.dispose();
        font.dispose();
    }
}
