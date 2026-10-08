package juego.chihiro.pantallas;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import juego.chihiro.Main;
import juego.chihiro.utiles.Constantes;

public class PantallaCarga extends ScreenAdapter {
    private static final float ANCHO_BARRA = 520f;
    private static final float ALTO_BARRA = 22f;

    private final Main juego;
    private final OrthographicCamera camara = new OrthographicCamera();
    private final Viewport vista;
    private final ShapeRenderer formas = new ShapeRenderer();
    private final BitmapFont fuente = new BitmapFont();

    private float progresoMostrado;

    public PantallaCarga(Main juego) {
        this.juego = juego;
        this.vista = new FitViewport(Constantes.ANCHO_VISTA, Constantes.ALTO_VISTA, camara);
        encolarAssets();
    }

    private void encolarAssets() {
        AssetManager assets = juego.getAssets();
        assets.load(Constantes.RUTA_JUGADORES, Texture.class);
        assets.load(Constantes.RUTA_ESPIRITUS, Texture.class);
        assets.load(Constantes.RUTA_OBJETOS, Texture.class);
        assets.load(Constantes.RUTA_LOGO, Texture.class);
        assets.load(Constantes.RUTA_PIXEL, Texture.class);
        assets.load(Constantes.RUTA_MAPA, TiledMap.class);
        assets.load(Constantes.RUTA_MUSICA, Music.class);
        assets.load(Constantes.RUTA_TOMAR, Sound.class);
        assets.load(Constantes.RUTA_ENTREGAR, Sound.class);
        assets.load(Constantes.RUTA_PEDIDO_COMPLETO, Sound.class);
        assets.load(Constantes.RUTA_ERROR, Sound.class);
        assets.load(Constantes.RUTA_LIMPIAR, Sound.class);
    }

    @Override
    public void render(float delta) {
        boolean termino = juego.getAssets().update();

        progresoMostrado = MathUtils.lerp(progresoMostrado,
                                          juego.getAssets().getProgress(),
                                          Math.min(1f, delta * 8f));

        ScreenUtils.clear(0.07f, 0.05f, 0.09f, 1f);
        vista.apply();
        camara.update();

        dibujarBarra();
        dibujarTexto();

        if (termino && progresoMostrado > 0.99f) {
            juego.prepararRecursos();
            juego.cambiarPantalla(new PantallaMenu(juego));
        }
    }

    private void dibujarBarra() {
        float x = (Constantes.ANCHO_VISTA - ANCHO_BARRA) / 2f;
        float y = (Constantes.ALTO_VISTA - ALTO_BARRA) / 2f;

        formas.setProjectionMatrix(camara.combined);
        formas.begin(ShapeRenderer.ShapeType.Filled);
        formas.setColor(0.18f, 0.15f, 0.20f, 1f);
        formas.rect(x, y, ANCHO_BARRA, ALTO_BARRA);
        formas.setColor(0.89f, 0.69f, 0.38f, 1f);
        formas.rect(x, y, ANCHO_BARRA * progresoMostrado, ALTO_BARRA);
        formas.end();

        formas.begin(ShapeRenderer.ShapeType.Line);
        formas.setColor(Color.WHITE);
        formas.rect(x, y, ANCHO_BARRA, ALTO_BARRA);
        formas.end();
    }

    private void dibujarTexto() {
        float x = (Constantes.ANCHO_VISTA - ANCHO_BARRA) / 2f;
        float y = (Constantes.ALTO_VISTA - ALTO_BARRA) / 2f;

        juego.getBatch().setProjectionMatrix(camara.combined);
        juego.getBatch().begin();
        fuente.setColor(Color.WHITE);
        fuente.draw(juego.getBatch(), "Cargando la casa de baños...", x, y + 60f);
        fuente.draw(juego.getBatch(), Math.round(progresoMostrado * 100f) + " %", x, y - 16f);
        juego.getBatch().end();
    }

    @Override
    public void resize(int ancho, int alto) {
        vista.update(ancho, alto, true);
    }

    @Override
    public void dispose() {
        formas.dispose();
        fuente.dispose();
    }
}
