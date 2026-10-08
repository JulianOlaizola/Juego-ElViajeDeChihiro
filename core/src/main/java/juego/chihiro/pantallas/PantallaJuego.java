package juego.chihiro.pantallas;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import juego.chihiro.Main;
import juego.chihiro.audio.AdministradorAudio;
import juego.chihiro.entidades.Jugador;
import juego.chihiro.entradas.Accion;
import juego.chihiro.entradas.ControladorEntradas;
import juego.chihiro.hud.Hud;
import juego.chihiro.mundo.GestorInteracciones;
import juego.chihiro.mundo.GestorPedidos;
import juego.chihiro.mundo.MapaCasaBanos;
import juego.chihiro.utiles.Constantes;
import juego.chihiro.utiles.DepuradorColisiones;
import juego.chihiro.utiles.Recursos;

public class PantallaJuego extends ScreenAdapter {
    private final Main juego;
    private final Recursos recursos;
    private final ControladorEntradas entradas;
    private final AdministradorAudio audio;

    private final OrthographicCamera camaraMundo = new OrthographicCamera();
    private final Viewport vistaMundo;
    private final OrthographicCamera camaraHud = new OrthographicCamera();
    private final Viewport vistaHud;

    private final MapaCasaBanos mapa;
    private final OrthogonalTiledMapRenderer renderizadorMapa;
    private final Jugador jugador1;
    private final Jugador jugador2;
    private final GestorPedidos gestorPedidos;
    private final GestorInteracciones gestorInteracciones;
    private final Hud hud;
    private final DepuradorColisiones depurador = new DepuradorColisiones();

    private EstadoPartida estado = EstadoPartida.JUGANDO;
    private float tiempoRestante = Constantes.DURACION_NIVEL;
    private boolean gano;
    private boolean mostrarDepuracion;

    public PantallaJuego(Main juego) {
        this.juego = juego;
        this.recursos = juego.getRecursos();
        this.entradas = juego.getEntradas();
        this.audio = juego.getAudio();

        vistaMundo = new FitViewport(Constantes.ANCHO_VISTA, Constantes.ALTO_VISTA, camaraMundo);
        vistaHud = new FitViewport(Constantes.ANCHO_VISTA, Constantes.ALTO_VISTA, camaraHud);

        mapa = new MapaCasaBanos(juego.getAssets());

        renderizadorMapa = new OrthogonalTiledMapRenderer(mapa.getMapa(), 1f, juego.getBatch());

        jugador1 = new Jugador(1, mapa.getAparicionJugador1().x, mapa.getAparicionJugador1().y,
                               recursos, entradas, mapa);
        jugador2 = new Jugador(2, mapa.getAparicionJugador2().x, mapa.getAparicionJugador2().y,
                               recursos, entradas, mapa);

        gestorPedidos = new GestorPedidos(mapa, recursos);
        gestorInteracciones = new GestorInteracciones(mapa, gestorPedidos, audio);
        hud = new Hud(recursos, audio, vistaHud, camaraHud);

        camaraMundo.position.set(jugador1.getCentroX(), jugador1.getCentroY(), 0f);
    }

    @Override
    public void render(float delta) {
        if (!actualizar(delta)) {
            return;
        }
        dibujar();
    }

    private boolean actualizar(float delta) {
        juego.atenderControlesDeAudio();
        if (entradas.consumir(Accion.DEPURAR)) {
            mostrarDepuracion = !mostrarDepuracion;
        }

        if (estado == EstadoPartida.TERMINADA) {
            if (entradas.consumir(Accion.CONFIRMAR)) {
                juego.cambiarPantalla(new PantallaMenu(juego));
                return false;
            }
            entradas.limpiarAcciones();
            return true;
        }

        if (entradas.consumir(Accion.PAUSA)) {
            estado = (estado == EstadoPartida.PAUSA) ? EstadoPartida.JUGANDO : EstadoPartida.PAUSA;
        }

        if (estado == EstadoPartida.PAUSA) {
            if (entradas.consumir(Accion.CONFIRMAR)) {
                juego.cambiarPantalla(new PantallaMenu(juego));
                return false;
            }
            entradas.limpiarAcciones();
            return true;
        }

        jugador1.actualizar(delta);
        jugador2.actualizar(delta);
        gestorPedidos.actualizar(delta);
        gestorInteracciones.actualizar(delta);

        if (entradas.consumir(Accion.INTERACTUAR_JUGADOR_1)) {
            gestorInteracciones.interactuar(jugador1);
        }
        if (entradas.consumir(Accion.INTERACTUAR_JUGADOR_2)) {
            gestorInteracciones.interactuar(jugador2);
        }

        seguirConLaCamara(delta);

        tiempoRestante -= delta;
        revisarFinDePartida();

        entradas.limpiarAcciones();
        return true;
    }

    private void revisarFinDePartida() {
        if (gestorPedidos.getPedidosCompletados() >= Constantes.PEDIDOS_PARA_GANAR) {
            terminar(true);
        } else if (gestorPedidos.getPedidosFallidos() >= Constantes.FALLOS_PARA_PERDER
                   || tiempoRestante <= 0f) {
            terminar(false);
        }
    }

    private void terminar(boolean ganoLaPartida) {
        gano = ganoLaPartida;
        estado = EstadoPartida.TERMINADA;
        tiempoRestante = Math.max(0f, tiempoRestante);
        audio.reproducir(ganoLaPartida
            ? AdministradorAudio.Efecto.PEDIDO_COMPLETO
            : AdministradorAudio.Efecto.ERROR);
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

        hud.dibujar(batch, tiempoRestante, gestorPedidos, gestorInteracciones, jugador1, jugador2);

        if (estado == EstadoPartida.PAUSA) {
            dibujarVelo("PAUSA", "P o ESC para seguir   -   ENTER para volver al menú");
        } else if (estado == EstadoPartida.TERMINADA) {
            dibujarVelo(gano ? "¡Turno completado!" : "Se cerró la casa de baños",
                        "Puntaje " + gestorPedidos.getPuntaje() + "   -   ENTER para volver al menú");
        }
    }

    private void dibujarVelo(String titulo, String detalle) {
        SpriteBatch batch = juego.getBatch();
        vistaHud.apply();
        batch.setProjectionMatrix(camaraHud.combined);
        batch.begin();

        batch.setColor(0f, 0f, 0f, 0.62f);
        batch.draw(recursos.getPixel(), 0f, 0f, Constantes.ANCHO_VISTA, Constantes.ALTO_VISTA);
        batch.setColor(Color.WHITE);

        recursos.getFuenteGrande().draw(batch, titulo, 0f, Constantes.ALTO_VISTA / 2f + 40f,
                                        Constantes.ANCHO_VISTA, Align.center, false);
        recursos.getFuenteMedia().draw(batch, detalle, 0f, Constantes.ALTO_VISTA / 2f - 20f,
                                       Constantes.ANCHO_VISTA, Align.center, false);
        batch.end();
    }

    @Override
    public void resize(int ancho, int alto) {
        vistaMundo.update(ancho, alto, false);
        vistaHud.update(ancho, alto, true);
    }

    @Override
    public void pause() {
        if (estado == EstadoPartida.JUGANDO) {
            estado = EstadoPartida.PAUSA;
        }
    }

    @Override
    public void dispose() {
        renderizadorMapa.dispose();
        depurador.dispose();
    }
}
