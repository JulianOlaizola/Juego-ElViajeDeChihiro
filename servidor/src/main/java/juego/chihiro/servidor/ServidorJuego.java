package juego.chihiro.servidor;

import java.net.SocketException;
import java.util.List;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;

import juego.chihiro.red.InterfacesRed;
import juego.chihiro.red.Mensaje;
import juego.chihiro.red.MensajeInvalidoException;
import juego.chihiro.red.PaqueteRecibido;
import juego.chihiro.red.TipoMensaje;
import juego.chihiro.servidor.fisica.MapaInvalidoException;
import juego.chihiro.servidor.fisica.MapaServidor;
import juego.chihiro.servidor.fisica.MundoFisico;
import juego.chihiro.servidor.logica.JugadorServidor;
import juego.chihiro.utiles.Constantes;

public class ServidorJuego extends ApplicationAdapter {

    public static final int TICKS_POR_SEGUNDO = 60;
    private static final int MAXIMO_PAQUETES_POR_TICK = 256;

    private final ServidorUdp red;
    private final JugadorServidor[] jugadores = new JugadorServidor[Constantes.CANTIDAD_JUGADORES];
    private MapaServidor mapa;
    private MundoFisico mundo;
    private long ticks;
    private long instanteAnterior;

    public ServidorJuego(ServidorUdp red) {
        this.red = red;
    }

    @Override
    public void create() {
        try {
            mapa = new MapaServidor(Constantes.RUTA_MAPA);
        } catch (MapaInvalidoException e) {
            Registro.error("No se pudo cargar el mapa del servidor", e);
            Gdx.app.exit();
            return;
        }
        mundo = new MundoFisico(mapa);
        for (int i = 0; i < jugadores.length; i++) {
            jugadores[i] = new JugadorServidor(i + 1);
            jugadores[i].ubicar(mundo, mapa.getAparicion(i + 1));
        }
        instanteAnterior = System.nanoTime();

        Registro.info("Servidor de El Viaje de Chihiro escuchando por UDP en el puerto " + red.getPuerto());
        Registro.info("Mundo físico Box2D listo: " + mundo.getCantidadParedes() + " paredes, "
            + mundo.getCantidadSensores() + " objetos interactivos y " + jugadores.length + " jugadores");
        mostrarDirecciones();
        Registro.info("Comandos disponibles: estado, ayuda, salir");
    }

    private void mostrarDirecciones() {
        try {
            List<String> direcciones = InterfacesRed.direccionesLocales();
            if (direcciones.isEmpty()) {
                Registro.aviso("No se encontró ninguna red local activa. Solo se podrá jugar en esta computadora (127.0.0.1).");
                return;
            }
            for (String direccion : direcciones) {
                Registro.info("IP para conectarse: " + direccion);
            }
        } catch (SocketException e) {
            Registro.error("No se pudieron listar las interfaces de red", e);
        }
    }

    @Override
    public void render() {
        if (mundo == null) {
            return;
        }
        long ahora = System.nanoTime();
        float delta = (ahora - instanteAnterior) / 1_000_000_000f;
        instanteAnterior = ahora;
        ticks++;

        procesarEntrantes();
        for (JugadorServidor jugador : jugadores) {
            jugador.aplicarMovimiento();
        }
        mundo.avanzar(delta);
    }

    private void procesarEntrantes() {
        int procesados = 0;
        PaqueteRecibido paquete;
        while (procesados < MAXIMO_PAQUETES_POR_TICK && (paquete = red.siguiente()) != null) {
            procesados++;
            try {
                atender(paquete);
            } catch (MensajeInvalidoException e) {
                Registro.aviso("Mensaje inválido de " + paquete.getOrigen() + ": " + e.getMessage());
            } catch (RuntimeException e) {
                Registro.error("Error inesperado al atender " + paquete.getMensaje() + " de " + paquete.getOrigen(), e);
            }
        }
    }

    private void atender(PaqueteRecibido paquete) throws MensajeInvalidoException {
        Mensaje mensaje = paquete.getMensaje();
        switch (mensaje.tipo()) {
            case PING:
                responderPing(paquete);
                break;
            default:
                Registro.aviso("Mensaje no esperado por el servidor: " + mensaje + " de " + paquete.getOrigen());
                break;
        }
    }

    private void responderPing(PaqueteRecibido paquete) throws MensajeInvalidoException {
        String marcaDeTiempo = paquete.getMensaje().campo(0);
        red.enviar(new Mensaje(TipoMensaje.PONG, marcaDeTiempo, String.valueOf(ticks)), paquete.getOrigen());
        Registro.info("PING recibido de " + paquete.getOrigen() + ", respondido con PONG");
    }

    public void imprimirEstado() {
        Registro.info("Ticks: " + ticks
            + " | Recibidos: " + red.getPaquetesRecibidos()
            + " | Enviados: " + red.getPaquetesEnviados()
            + " | Descartados: " + red.getPaquetesDescartados()
            + " | En cola: " + red.getPaquetesEnCola());
        for (JugadorServidor jugador : jugadores) {
            if (jugador != null) {
                Registro.info("Jugador " + jugador.getNumero() + " en ("
                    + Math.round(jugador.getCentroX()) + ", " + Math.round(jugador.getCentroY()) + ")");
            }
        }
    }

    @Override
    public void dispose() {
        if (mundo != null) {
            mundo.dispose();
        }
        red.cerrar();
        Registro.info("Servidor cerrado.");
    }
}
