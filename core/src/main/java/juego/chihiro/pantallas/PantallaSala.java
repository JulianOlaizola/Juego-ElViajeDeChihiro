package juego.chihiro.pantallas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import juego.chihiro.Main;
import juego.chihiro.entradas.Accion;
import juego.chihiro.red.EstadoConexion;
import juego.chihiro.red.FasePartida;
import juego.chihiro.red.InstantaneaPartida;
import juego.chihiro.red.SesionCliente;
import juego.chihiro.utiles.Constantes;

public class PantallaSala extends PantallaBase {
    private final SesionCliente sesion;

    public PantallaSala(Main juego) {
        super(juego);
        this.sesion = juego.getSesion();
    }

    @Override
    public void render(float delta) {
        juego.atenderControlesDeAudio();
        if (entradas.consumir(Accion.VOLVER)) {
            juego.cambiarPantalla(new PantallaMenu(juego));
            return;
        }
        sesion.actualizar();
        if (!sesion.estaEnPartida()) {
            juego.cambiarPantalla(new PantallaMensaje(juego, "Se cortó la conexión", sesion.getMotivo()));
            return;
        }
        if (sesion.getEstado() == EstadoConexion.CONECTADO && sesion.getInstantanea().getFase() != FasePartida.SALA) {
            juego.cambiarPantalla(new PantallaJuego(juego));
            return;
        }
        sesion.setEjes(0f, 0f);
        if (entradas.consumir(Accion.CONFIRMAR)) {
            sesion.alternarListo();
        }
        entradas.limpiarAcciones();
        dibujar();
    }

    private void dibujar() {
        InstantaneaPartida instantanea = sesion.getInstantanea();
        SpriteBatch batch = comenzarDibujo();
        titulo(batch, "Sala de espera");
        String ping = sesion.getLatenciaMilis() < 0 ? "" : "   -   ping " + sesion.getLatenciaMilis() + " ms";
        linea(batch, recursos.getFuenteChica(), sesion.getDescripcionServidor() + ping, 440f, Color.LIGHT_GRAY);
        linea(batch, recursos.getFuenteMedia(), "Sos el jugador " + sesion.getNumeroJugador(), 400f, Color.GOLD);

        float y = 330f;
        for (int numero = 1; numero <= Constantes.CANTIDAD_JUGADORES; numero++) {
            String detalle;
            Color color;
            if (!instantanea.estaConectado(numero)) {
                detalle = "esperando que se conecte...";
                color = Color.GRAY;
            } else if (instantanea.estaListo(numero)) {
                detalle = "¡listo!";
                color = Color.LIME;
            } else {
                detalle = "conectado, todavía no está listo";
                color = Color.WHITE;
            }
            String quien = numero == sesion.getNumeroJugador() ? " (vos)" : "";
            linea(batch, recursos.getFuenteMedia(), "Jugador " + numero + quien + ": " + detalle, y, color);
            y -= 44f;
        }

        String accion = sesion.estaListo() ? "ENTER para cancelar \"listo\"" : "ENTER cuando estés listo";
        linea(batch, recursos.getFuenteMedia(), accion, 170f, Color.WHITE);
        linea(batch, recursos.getFuenteChica(), "La partida empieza cuando los dos jugadores están listos.", 130f, Color.LIGHT_GRAY);
        if (sesion.getEstado() == EstadoConexion.RECONECTANDO) {
            linea(batch, recursos.getFuenteMedia(), "Se perdió la señal del servidor. Reconectando... "
                + sesion.segundosReconectando() + " s", 90f, Color.ORANGE);
        }
        pie(batch, "ESC para salir de la sala");
        batch.end();
    }
}
