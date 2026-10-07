package juego.chihiro.pantallas;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import juego.chihiro.Main;
import juego.chihiro.entidades.Jugador;
import juego.chihiro.entradas.Accion;
import juego.chihiro.entradas.ControladorEntradas;
import juego.chihiro.mundo.GestorPedidos;
import juego.chihiro.mundo.MapaCasaBanos;
import juego.chihiro.utiles.Constantes;
import juego.chihiro.utiles.DepuradorColisiones;
import juego.chihiro.utiles.Recursos;

public class PantallaJuego extends ScreenAdapter {
    private final Main juego;
    private final Recursos recursos;
    private final ControladorEntradas entradas;

    private final OrthographicCamera camaraMundo = new OrthographicCamera();
    private final Viewport vistaMundo;

    private final MapaCasaBanos mapa;
    private final OrthogonalTiledMapRenderer renderizadorMapa;
    private final Jugador jugador1;
    private final Jugador jugador2;
    private final GestorPedidos gestorPedidos;
    private final DepuradorColisiones depurador = new DepuradorColisiones();

    private boolean mostrarDepuracion;

    public PantallaJuego(Main juego) {
        this.juego = juego;
        this.recursos = juego.getRecursos();
        this.entradas = juego.getEntradas();

        vistaMundo = new FitViewport(Constantes.ANCHO_VISTA, Constantes.ALTO_VISTA, camaraMundo);

        mapa = new MapaCasaBanos(juego.getAssets());

        renderizadorMapa = new OrthogonalTiledMapRenderer(mapa.getMapa(), 1f, juego.getBatch());

        jugador1 = new Jugador(1, mapa.getAparicionJugador1().x, mapa.getAparicionJugador1().y,
            recursos, entradas, mapa);
        jugador2 = new Jugador(2, mapa.getAparicionJugador2().x, mapa.getAparicionJugador2().y,
            recursos, entradas, mapa);

        gestorPedidos = new GestorPedidos(mapa, recursos);

        camaraMundo.position.set(jugador1.getCentroX(), jugador1.getCentroY(), 0f);
    }

    @Override
    public void render(float delta) {
        actualizar(delta);
        dibujar();
    }

    private void actualizar(float delta) {
        juego.atenderControlesDeAudio();
        if (entradas.consumir(Accion.DEPURAR)) {
            mostrarDepuracion = !mostrarDepuracion;
        }

        jugador1.actualizar(delta);
        jugador2.actualizar(delta);
        gestorPedidos.actualizar(delta);

        seguirConLaCamara(delta);

        entradas.limpiarAcciones();
    }

    private void seguirConLaCamara(float delta) {
        float mitadAncho = Constantes.ANCHO_VISTA / 2f;
        float mitadAlto = Constantes.ALTO_VISTA / 2f;

        float objetivoX = (jugador1.getCentroX() + jugador2.getCentroX()) / 2f;
        float objetivoY = (jugador1.getCentroY() + jugador2.getCentroY()) / 2f;
        objetivoX = MathUtils.clamp(objetivoX, mitadAncho,
            Math.max(mitadAncho, mapa.getAncho() - mitadAncho));
        objetivoY = MathUtils.clamp(objetivoY, mitadAlto,
            Math.max(mitadAlto, mapa.getAlto() - mitadAlto));

        float suavizado = Math.min(1f, delta * Constantes.SUAVIZADO_CAMARA);
        camaraMundo.position.x = MathUtils.lerp(camaraMundo.position.x, objetivoX, suavizado);
        camaraMundo.position.y = MathUtils.lerp(camaraMundo.position.y, objetivoY, suavizado);
    }

    private void dibujar() {
        ScreenUtils.clear(0.05f, 0.04f, 0.07f, 1f);

        vistaMundo.apply();
        camaraMundo.update();

        renderizadorMapa.setView(camaraMundo);
        renderizadorMapa.render(mapa.getCapasVisibles());

        SpriteBatch batch = juego.getBatch();
        batch.setProjectionMatrix(camaraMundo.combined);
        batch.begin();
        gestorPedidos.dibujar(batch);
        jugador1.dibujar(batch);
        jugador2.dibujar(batch);
        batch.end();

        if (mostrarDepuracion) {
            depurador.dibujar(camaraMundo, mapa, jugador1, jugador2);
        }
    }

    @Override
    public void resize(int ancho, int alto) {
        vistaMundo.update(ancho, alto, false);
    }

    @Override
    public void dispose() {
        renderizadorMapa.dispose();
        depurador.dispose();
    }
}
