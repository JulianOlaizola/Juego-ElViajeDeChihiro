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
import juego.chihiro.entidades.Espiritu;
import juego.chihiro.entidades.Jugador;
import juego.chihiro.entradas.Accion;
import juego.chihiro.entradas.ControladorEntradas;
import juego.chihiro.hud.Hud;
import juego.chihiro.mundo.EstadoTina;
import juego.chihiro.mundo.MapaCasaBanos;
import juego.chihiro.mundo.ObjetoInteractivo;
import juego.chihiro.red.EstadoConexion;
import juego.chihiro.red.EventoRed;
import juego.chihiro.red.FasePartida;
import juego.chihiro.red.InstantaneaPartida;
import juego.chihiro.red.SesionCliente;
import juego.chihiro.utiles.Constantes;
import juego.chihiro.utiles.DepuradorColisiones;
import juego.chihiro.utiles.Recursos;

public class PantallaJuego extends ScreenAdapter {
    private final Main juego;
    private final Recursos recursos;
    private final ControladorEntradas entradas;
    private final SesionCliente sesion;

    private final OrthographicCamera camaraMundo = new OrthographicCamera();
    private final Viewport vistaMundo;
    private final OrthographicCamera camaraHud = new OrthographicCamera();
    private final Viewport vistaHud;

    private final MapaCasaBanos mapa;
    private final OrthogonalTiledMapRenderer renderizadorMapa;
    private final Jugador[] jugadores = new Jugador[Constantes.CANTIDAD_JUGADORES];
    private final Espiritu[] espiritus;
    private final Hud hud;
    private final DepuradorColisiones depurador = new DepuradorColisiones();

    private boolean mostrarDepuracion;
    private boolean camaraUbicada;
    private String mensaje = "";
    private float tiempoMensaje;

    public PantallaJuego(Main juego) {
        this.juego = juego;
        this.recursos = juego.getRecursos();
        this.entradas = juego.getEntradas();
        this.sesion = juego.getSesion();

        vistaMundo = new FitViewport(Constantes.ANCHO_VISTA, Constantes.ALTO_VISTA, camaraMundo);
        vistaHud = new FitViewport(Constantes.ANCHO_VISTA, Constantes.ALTO_VISTA, camaraHud);

        mapa = new MapaCasaBanos(juego.getAssets());
        renderizadorMapa = new OrthogonalTiledMapRenderer(mapa.getMapa(), 1f, juego.getBatch());

        for (int i = 0; i < jugadores.length; i++) {
            jugadores[i] = new Jugador(i + 1, recursos);
        }
        espiritus = new Espiritu[mapa.getTinas().size];
        for (int i = 0; i < espiritus.length; i++) {
            ObjetoInteractivo tina = mapa.getTinas().get(i);
            float x = tina.getCentroX() - Constantes.TAM_TILE / 2f;
            float y = tina.getArea().y + tina.getArea().height - Constantes.SOLAPE_ESPIRITU_EN_TINA;
            espiritus[i] = new Espiritu(x, y, recursos);
        }
        hud = new Hud(recursos, juego.getAudio(), vistaHud, camaraHud);
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

        sesion.actualizar();
        if (sesion.getEstado() != EstadoConexion.CONECTADO) {
            juego.cambiarPantalla(new PantallaMensaje(juego, "Se cortó la conexión", sesion.getMotivo()));
            return false;
        }

        atenderEventos(delta);
        InstantaneaPartida estado = sesion.getInstantanea();
        if (estado.getFase() == FasePartida.SALA) {
            juego.cambiarPantalla(new PantallaSala(juego));
            return false;
        }

        if (entradas.consumir(Accion.VOLVER)
            || (estado.getFase() == FasePartida.TERMINADA && entradas.consumir(Accion.CONFIRMAR))) {
            juego.cambiarPantalla(new PantallaMenu(juego));
            return false;
        }

        if (estado.getFase() == FasePartida.JUGANDO) {
            sesion.setEjes(entradas.ejeHorizontalCombinado(), entradas.ejeVerticalCombinado());
            if (entradas.consumirInteraccion()) {
                sesion.pulsarInteraccion();
            }
        } else {
            sesion.setEjes(0f, 0f);
        }
        entradas.limpiarAcciones();

        for (Jugador jugador : jugadores) {
            jugador.actualizar(delta, estado.getJugador(jugador.getNumero()));
        }
        for (int i = 0; i < espiritus.length && i < estado.getTinas().size(); i++) {
            espiritus[i].actualizar(delta, estado.getTinas().get(i).getEstado() == EstadoTina.OCUPADA);
        }
        seguirConLaCamara(delta);
        return true;
    }

    private void atenderEventos(float delta) {
        if (tiempoMensaje > 0f) {
            tiempoMensaje -= delta;
        }
        EventoRed evento;
        while ((evento = sesion.siguienteEvento()) != null) {
            switch (evento.getTipo()) {
                case AVISO:
                    mostrarMensaje(evento.dato(0));
                    break;
                case JUGADOR_DESCONECTADO:
                    mostrarMensaje("El jugador " + evento.dato(0) + " se desconectó");
                    break;
                case JUGADOR_RECONECTADO:
                    mostrarMensaje("El jugador " + evento.dato(0) + " volvió a la partida");
                    break;
                default:
                    break;
            }
        }
    }

    private void mostrarMensaje(String texto) {
        mensaje = texto;
        tiempoMensaje = Constantes.DURACION_MENSAJE;
    }

    private void seguirConLaCamara(float delta) {
        float mitadAncho = Constantes.ANCHO_VISTA / 2f;
        float mitadAlto = Constantes.ALTO_VISTA / 2f;

        float objetivoX = (jugadores[0].getCentroX() + jugadores[1].getCentroX()) / 2f;
        float objetivoY = (jugadores[0].getCentroY() + jugadores[1].getCentroY()) / 2f;
        objetivoX = MathUtils.clamp(objetivoX, mitadAncho, Math.max(mitadAncho, mapa.getAncho() - mitadAncho));
        objetivoY = MathUtils.clamp(objetivoY, mitadAlto, Math.max(mitadAlto, mapa.getAlto() - mitadAlto));

        if (!camaraUbicada) {
            camaraMundo.position.set(objetivoX, objetivoY, 0f);
            camaraUbicada = jugadores[0].estaUbicado();
            return;
        }
        float suavizado = Math.min(1f, delta * Constantes.SUAVIZADO_CAMARA);
        camaraMundo.position.x = MathUtils.lerp(camaraMundo.position.x, objetivoX, suavizado);
        camaraMundo.position.y = MathUtils.lerp(camaraMundo.position.y, objetivoY, suavizado);
    }

    private void dibujar() {
        ScreenUtils.clear(0.05f, 0.04f, 0.07f, 1f);
        InstantaneaPartida estado = sesion.getInstantanea();

        vistaMundo.apply();
        camaraMundo.update();
        renderizadorMapa.setView(camaraMundo);
        renderizadorMapa.render(mapa.getCapasVisibles());

        SpriteBatch batch = juego.getBatch();
        batch.setProjectionMatrix(camaraMundo.combined);
        batch.begin();
        for (Espiritu espiritu : espiritus) {
            espiritu.dibujar(batch);
        }
        for (Jugador jugador : jugadores) {
            jugador.dibujar(batch);
        }
        batch.end();

        if (mostrarDepuracion) {
            depurador.dibujar(camaraMundo, mapa, jugadores);
        }

        hud.dibujar(batch, estado, sesion.getNumeroJugador(), tiempoMensaje > 0f ? mensaje : "");

        if (estado.getFase() == FasePartida.TERMINADA) {
            dibujarVelo(estado.gano() ? "¡Turno completado!" : "Se cerró la casa de baños",
                "Puntaje " + estado.getPuntaje() + "   -   ENTER para volver al menú");
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
    public void dispose() {
        renderizadorMapa.dispose();
        depurador.dispose();
    }
}
