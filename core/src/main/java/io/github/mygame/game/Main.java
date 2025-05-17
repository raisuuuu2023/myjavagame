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
    Sound appleFallSound;
    Music backgroundMusic;
    SpriteBatch spriteBatch;
    FitViewport viewport;
    Sprite newtonSprite;
    Vector2 touchPos;
    Array<Sprite> appleSprites;
    float appleTimer;
    Rectangle newtonRectangle;
    Rectangle appleRectangle;
    boolean gameOver;
    BitmapFont font;
    GlyphLayout layout;

    @Override
    public void create() {
        backgroundTexture = new Texture(Gdx.files.internal("math.jpg"));
        newtonTexture = new Texture(Gdx.files.internal("Newton.png"));
        appleTexture = new Texture(Gdx.files.internal("Apple.png"));
        appleFallSound = Gdx.audio.newSound(Gdx.files.internal("drop.mp3"));
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));

        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(8, 6);

        newtonSprite = new Sprite(newtonTexture);
        newtonSprite.setSize(2, 1);
        newtonSprite.setPosition(3, 0.5f);

        touchPos = new Vector2();
        appleSprites = new Array<>();
        newtonRectangle = new Rectangle();
        appleRectangle = new Rectangle();
        gameOver = false;

        font = new BitmapFont();
        font.getData().setScale(0.09f);
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

        for (Sprite apple : appleSprites) {
            apple.draw(spriteBatch);
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
        for (int i = appleSprites.size - 1; i >= 0; i--) {
            Sprite apple = appleSprites.get(i);
            apple.translateY(-2f * delta);
            appleRectangle.set(apple.getX(), apple.getY(), apple.getWidth(), apple.getHeight());

            if (apple.getY() < -apple.getHeight()) {
                gameOver = true;
                backgroundMusic.stop();
                break;
            } else if (newtonRectangle.overlaps(appleRectangle)) {
                appleSprites.removeIndex(i);
                appleFallSound.play();
            }
        }

        if (!gameOver) {
            appleTimer += delta;
            if (appleTimer > 1f) {
                appleTimer = 0;
                spawnApple();
            }
        }
    }

    private void spawnApple() {
        Sprite apple = new Sprite(appleTexture);
        apple.setSize(0.8f, 0.8f);
        apple.setPosition(
            MathUtils.random(0, viewport.getWorldWidth() - apple.getWidth()),
            viewport.getWorldHeight()
        );
        appleSprites.add(apple);
    }

    private void drawGameOver() {
        spriteBatch.begin();

        spriteBatch.setColor(0, 0, 0, 0.3f);
        spriteBatch.draw(backgroundTexture, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        spriteBatch.setColor(Color.WHITE);

        font.setColor(Color.WHITE);
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
        appleFallSound.dispose();
        backgroundMusic.dispose();
        spriteBatch.dispose();
        font.dispose();
    }
}
