package juego.chihiro;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

import juego.chihiro.pantallas.PantallaCarga;
import juego.chihiro.utiles.Recursos;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {
    private SpriteBatch batch;
    private AssetManager assets;
    private Recursos recursos;

    @Override
    public void create() {
        batch = new SpriteBatch();
        assets = new AssetManager();

        assets.setLoader(TiledMap.class, new TmxMapLoader(new InternalFileHandleResolver()));

        setScreen(new PantallaCarga(this));
    }

    public void prepararRecursos() {
        recursos = new Recursos(assets);
    }

    public void cambiarPantalla(Screen nueva) {
        Screen anterior = getScreen();
        setScreen(nueva);
        if (anterior != null) {
            anterior.dispose();
        }
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public AssetManager getAssets() {
        return assets;
    }

    public Recursos getRecursos() {
        return recursos;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (getScreen() != null) {
            getScreen().dispose();
        }
        if (recursos != null) {
            recursos.dispose();
        }
        assets.dispose();
        batch.dispose();
    }
}
