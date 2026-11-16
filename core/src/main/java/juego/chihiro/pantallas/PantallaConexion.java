package juego.chihiro.pantallas;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import juego.chihiro.Main;
import juego.chihiro.entradas.Accion;
import juego.chihiro.red.ClienteUdp;
import juego.chihiro.red.DireccionServidor;
import juego.chihiro.red.Mensaje;
import juego.chihiro.red.MensajeInvalidoException;
import juego.chihiro.red.PaqueteRecibido;
import juego.chihiro.red.TipoMensaje;

public class PantallaConexion extends PantallaBase {
    private static final float INTERVALO_PING = 0.5f;
    private static final int INTENTOS_MAXIMOS = 6;

    private final String descripcion;
    private CompletableFuture<InetSocketAddress> resolucion;
    private InetSocketAddress destino;
    private ClienteUdp cliente;
    private float esperaPing;
    private int intentos;
    private String estado = "Buscando el servidor...";
    private Color colorEstado = Color.WHITE;
    private boolean terminado;

    public PantallaConexion(Main juego, InetSocketAddress destino, String nombre) {
        super(juego);
        this.descripcion = nombre + " (" + destino.getAddress().getHostAddress() + ":" + destino.getPort() + ")";
        this.destino = destino;
        abrirSocket();
    }

    public PantallaConexion(Main juego, DireccionServidor direccion) {
        super(juego);
        this.descripcion = direccion.toString();
        this.resolucion = direccion.resolver();
        abrirSocket();
    }

    private void abrirSocket() {
        try {
            cliente = new ClienteUdp();
        } catch (SocketException e) {
            fallar("No se pudo abrir la red: " + e.getMessage());
        }
    }

    @Override
    public void render(float delta) {
        juego.atenderControlesDeAudio();
        if (entradas.consumir(Accion.VOLVER) || (terminado && entradas.consumir(Accion.CONFIRMAR))) {
            juego.cambiarPantalla(new PantallaMenu(juego));
            return;
        }
        entradas.limpiarAcciones();
        if (!terminado) {
            esperarResolucion();
            probarConexion(delta);
        }
        dibujar();
    }

    private void esperarResolucion() {
        if (destino != null || resolucion == null || !resolucion.isDone()) {
            return;
        }
        try {
            destino = resolucion.join();
        } catch (CompletionException e) {
            Throwable causa = e.getCause() != null ? e.getCause() : e;
            fallar(causa.getMessage());
        }
    }

    private void probarConexion(float delta) {
        if (destino == null || cliente == null || terminado) {
            return;
        }
        PaqueteRecibido paquete;
        while ((paquete = cliente.siguiente()) != null) {
            if (paquete.getMensaje().tipo() == TipoMensaje.PONG) {
                registrarPong(paquete.getMensaje());
                return;
            }
        }
        esperaPing -= delta;
        if (esperaPing > 0f) {
            return;
        }
        if (intentos >= INTENTOS_MAXIMOS) {
            fallar("El servidor no respondió. Revisá la IP, el puerto y el firewall.");
            return;
        }
        intentos++;
        esperaPing = INTERVALO_PING;
        estado = "Probando conexión (intento " + intentos + " de " + INTENTOS_MAXIMOS + ")...";
        cliente.enviar(new Mensaje(TipoMensaje.PING, String.valueOf(System.nanoTime())), destino);
    }

    private void registrarPong(Mensaje pong) {
        try {
            double milis = (System.nanoTime() - pong.campoLargo(0)) / 1_000_000.0;
            estado = String.format("El servidor respondió en %.1f ms", milis);
            colorEstado = Color.LIME;
        } catch (MensajeInvalidoException e) {
            estado = "El servidor respondió con datos inválidos";
            colorEstado = Color.RED;
        }
        terminado = true;
    }

    private void fallar(String motivo) {
        estado = motivo;
        colorEstado = Color.RED;
        terminado = true;
    }

    private void dibujar() {
        SpriteBatch batch = comenzarDibujo();
        titulo(batch, "Conexión");
        linea(batch, recursos.getFuenteMedia(), descripcion, 340f, Color.WHITE);
        linea(batch, recursos.getFuenteMedia(), estado, 280f, colorEstado);
        pie(batch, terminado ? "ENTER o ESC para volver al menú" : "ESC para cancelar");
        batch.end();
    }

    @Override
    public void dispose() {
        if (cliente != null) {
            cliente.cerrar();
        }
    }
}
