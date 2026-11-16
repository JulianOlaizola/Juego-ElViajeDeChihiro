package juego.chihiro.pantallas;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import juego.chihiro.Main;
import juego.chihiro.entradas.ControladorEntradas;
import juego.chihiro.utiles.Constantes;
import juego.chihiro.utiles.Recursos;

public abstract class PantallaBase extends ScreenAdapter {
    protected final Main juego;
    protected final Recursos recursos;
    protected final ControladorEntradas entradas;
    private final OrthographicCamera camara = new OrthographicCamera();
    private final Viewport vista;

    protected PantallaBase(Main juego) {
        this.juego = juego;
        this.recursos = juego.getRecursos();
        this.entradas = juego.getEntradas();
        this.vista = new FitViewport(Constantes.ANCHO_VISTA, Constantes.ALTO_VISTA, camara);
    }

    protected SpriteBatch comenzarDibujo() {
        ScreenUtils.clear(0.07f, 0.05f, 0.09f, 1f);
        vista.apply();
        camara.update();
        SpriteBatch batch = juego.getBatch();
        batch.setProjectionMatrix(camara.combined);
        batch.begin();
        return batch;
    }

    protected void titulo(SpriteBatch batch, String texto) {
        BitmapFont fuente = recursos.getFuenteGrande();
        fuente.setColor(Color.GOLD);
        fuente.draw(batch, texto, 0f, Constantes.ALTO_VISTA - 40f, Constantes.ANCHO_VISTA, Align.center, false);
        fuente.setColor(Color.WHITE);
    }

    protected void linea(SpriteBatch batch, BitmapFont fuente, String texto, float y, Color color) {
        fuente.setColor(color);
        fuente.draw(batch, texto, 0f, y, Constantes.ANCHO_VISTA, Align.center, false);
        fuente.setColor(Color.WHITE);
    }

    protected void pie(SpriteBatch batch, String texto) {
        linea(batch, recursos.getFuenteChica(), texto, 48f, Color.LIGHT_GRAY);
    }

    @Override
    public void resize(int ancho, int alto) {
        vista.update(ancho, alto, true);
    }
}
