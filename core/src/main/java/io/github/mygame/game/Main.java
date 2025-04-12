package io.github.mygame.game;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

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

    @Override
    public void create() {
        backgroundTexture = new Texture("math.jpg");
        newtonTexture = new Texture("Newton.png");
        appleTexture = new Texture("Apple.png");
        appleFallSound = Gdx.audio.newSound(Gdx.files.internal("drop.mp3"));
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));
        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(8, 6);
        newtonSprite = new Sprite(newtonTexture);
        newtonSprite.setSize(2, 1);
        touchPos = new Vector2();
        appleSprites = new Array<>();
        newtonRectangle = new Rectangle();
        appleRectangle = new Rectangle();
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(.5f);
        backgroundMusic.play();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void render() {
        input();
        logic();
        draw();
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
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();
        float newtonWidth = newtonSprite.getWidth();
        float newtonHeight = newtonSprite.getHeight();

        newtonSprite.setX(MathUtils.clamp(newtonSprite.getX(), 0, worldWidth - newtonWidth));

        float delta = Gdx.graphics.getDeltaTime();
        newtonRectangle.set(newtonSprite.getX(), newtonSprite.getY(), newtonWidth, newtonHeight);

        for (int i = appleSprites.size - 1; i >= 0; i--) {
            Sprite appleSprite = appleSprites.get(i);
            float appleWidth = appleSprite.getWidth();
            float appleHeight = appleSprite.getHeight();

            appleSprite.translateY(-2f * delta);
            appleRectangle.set(appleSprite.getX(), appleSprite.getY(), appleWidth, appleHeight);

            if (appleSprite.getY() < -appleHeight) appleSprites.removeIndex(i);
            else if (newtonRectangle.overlaps(appleRectangle)) {
                appleSprites.removeIndex(i);
                appleFallSound.play();
            }
        }

        appleTimer += delta;
        if (appleTimer > 1f) {
            appleTimer = 0;
            createApple();
        }
    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();

        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        spriteBatch.draw(backgroundTexture, 0, 0, worldWidth, worldHeight);
        newtonSprite.draw(spriteBatch);

        for (Sprite appleSprite : appleSprites) {
            appleSprite.draw(spriteBatch);
        }

        spriteBatch.end();
    }

    private void createApple() {
        float appleWidth = 1;
        float appleHeight = 1;
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        Sprite appleSprite = new Sprite(appleTexture);
        appleSprite.setSize(appleWidth, appleHeight);
        appleSprite.setX(MathUtils.random(0f, worldWidth - appleWidth));
        appleSprite.setY(worldHeight);
        appleSprites.add(appleSprite);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {

    }
}
