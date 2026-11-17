package juego.chihiro.pantallas;

import java.net.InetSocketAddress;
import java.net.SocketException;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import juego.chihiro.Main;
import juego.chihiro.entradas.Accion;
import juego.chihiro.red.DireccionServidor;
import juego.chihiro.red.EstadoConexion;
import juego.chihiro.red.Protocolo;
import juego.chihiro.red.SesionCliente;

public class PantallaConexion extends PantallaBase {
    private final String descripcion;
    private SesionCliente sesion;
    private String error = "";

    public PantallaConexion(Main juego, InetSocketAddress destino, String nombre) {
        super(juego);
        this.descripcion = nombre + " (" + destino.getAddress().getHostAddress() + ":" + destino.getPort() + ")";
        try {
            sesion = new SesionCliente(destino, descripcion, nombreDelJugador());
            juego.iniciarSesion(sesion);
        } catch (SocketException e) {
            error = "No se pudo abrir la red: " + e.getMessage();
        }
    }

    public PantallaConexion(Main juego, DireccionServidor direccion) {
        super(juego);
        this.descripcion = direccion.toString();
        try {
            sesion = new SesionCliente(direccion, nombreDelJugador());
            juego.iniciarSesion(sesion);
        } catch (SocketException e) {
            error = "No se pudo abrir la red: " + e.getMessage();
        }
    }

    static String nombreDelJugador() {
        String usuario = System.getProperty("user.name", "");
        return usuario.isBlank() ? "Jugador" : usuario;
    }

    @Override
    public void render(float delta) {
        juego.atenderControlesDeAudio();
        boolean fallo = sesion == null
            || sesion.getEstado() == EstadoConexion.PERDIDA
            || sesion.getEstado() == EstadoConexion.RECHAZADO;
        if (entradas.consumir(Accion.VOLVER) || (fallo && entradas.consumir(Accion.CONFIRMAR))) {
            juego.cambiarPantalla(new PantallaMenu(juego));
            return;
        }
        entradas.limpiarAcciones();
        if (sesion != null) {
            sesion.actualizar();
            if (sesion.getEstado() == EstadoConexion.CONECTADO) {
                juego.cambiarPantalla(new PantallaSala(juego));
                return;
            }
            if (fallo) {
                error = sesion.getMotivo();
            }
        }
        dibujar();
    }

    private void dibujar() {
        SpriteBatch batch = comenzarDibujo();
        titulo(batch, "Conectando");
        linea(batch, recursos.getFuenteMedia(), descripcion, 340f, Color.WHITE);
        if (!error.isEmpty()) {
            linea(batch, recursos.getFuenteMedia(), error, 280f, Color.RED);
            pie(batch, "ENTER o ESC para volver al menú");
        } else {
            String detalle = sesion.getEstado() == EstadoConexion.RESOLVIENDO
                ? "Buscando la dirección..."
                : "Esperando respuesta (intento " + sesion.getIntentosConexion() + " de " + Protocolo.INTENTOS_CONEXION + ")";
            linea(batch, recursos.getFuenteMedia(), detalle, 280f, Color.LIGHT_GRAY);
            pie(batch, "ESC para cancelar");
        }
        batch.end();
    }
}
