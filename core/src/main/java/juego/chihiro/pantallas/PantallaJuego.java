package juego.chihiro.pantallas;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import juego.chihiro.Main;
import juego.chihiro.entradas.ControladorEntradas;
import juego.chihiro.mundo.MapaCasaBanos;
import juego.chihiro.utiles.Constantes;

public class PantallaJuego extends ScreenAdapter {
    private final Main juego;
    private final ControladorEntradas entradas;

    private final OrthographicCamera camaraMundo = new OrthographicCamera();
    private final Viewport vistaMundo;

    private final MapaCasaBanos mapa;
    private final OrthogonalTiledMapRenderer renderizadorMapa;

    public PantallaJuego(Main juego) {
        this.juego = juego;
        this.entradas = juego.getEntradas();

        vistaMundo = new FitViewport(Constantes.ANCHO_VISTA, Constantes.ALTO_VISTA, camaraMundo);

        mapa = new MapaCasaBanos(juego.getAssets());

        renderizadorMapa = new OrthogonalTiledMapRenderer(mapa.getMapa(), 1f, juego.getBatch());

        camaraMundo.position.set(mapa.getAncho() / 2f, mapa.getAlto() / 2f, 0f);
    }

    @Override
    public void render(float delta) {
        juego.atenderControlesDeAudio();
        entradas.limpiarAcciones();
        dibujar();
    }

    private void dibujar() {
        ScreenUtils.clear(0.05f, 0.04f, 0.07f, 1f);

        vistaMundo.apply();
        camaraMundo.update();

        renderizadorMapa.setView(camaraMundo);
        renderizadorMapa.render(mapa.getCapasVisibles());
    }

    @Override
    public void resize(int ancho, int alto) {
        vistaMundo.update(ancho, alto, false);
    }

    @Override
    public void dispose() {
        renderizadorMapa.dispose();
    }
}
