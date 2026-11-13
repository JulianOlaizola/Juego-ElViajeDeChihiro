package juego.chihiro.servidor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import juego.chihiro.red.Mensaje;
import juego.chihiro.red.MensajeInvalidoException;
import juego.chihiro.red.Protocolo;
import juego.chihiro.red.TipoMensaje;

public final class ProbadorConexion {

    private static final int INTENTOS = 4;
    private static final int ESPERA_RESPUESTA_MILIS = 2000;

    private ProbadorConexion() {
    }

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "127.0.0.1";
        int puerto;
        try {
            puerto = args.length > 1 ? Protocolo.leerPuerto(args[1]) : Protocolo.PUERTO_POR_DEFECTO;
        } catch (IllegalArgumentException e) {
            Registro.error(e.getMessage(), null);
            return;
        }

        InetSocketAddress destino;
        try {
            destino = new InetSocketAddress(InetAddress.getByName(host), puerto);
        } catch (UnknownHostException e) {
            Registro.error("No se encontró la dirección " + host, null);
            return;
        }

        int respondidos = 0;
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(ESPERA_RESPUESTA_MILIS);
            byte[] buffer = new byte[Protocolo.TAMANIO_MAXIMO_PAQUETE];
            for (int i = 1; i <= INTENTOS; i++) {
                long enviado = System.nanoTime();
                byte[] datos = new Mensaje(TipoMensaje.PING, String.valueOf(enviado)).aBytes();
                socket.send(new DatagramPacket(datos, datos.length, destino));
                try {
                    DatagramPacket respuesta = new DatagramPacket(buffer, buffer.length);
                    socket.receive(respuesta);
                    Mensaje pong = Mensaje.desdeBytes(respuesta.getData(), respuesta.getLength());
                    if (pong.tipo() != TipoMensaje.PONG) {
                        Registro.aviso("Intento " + i + ": respuesta inesperada " + pong);
                        continue;
                    }
                    double milis = (System.nanoTime() - pong.campoLargo(0)) / 1_000_000.0;
                    respondidos++;
                    Registro.info(String.format("Intento %d: PONG de %s en %.2f ms (tick del servidor %s)", i, respuesta.getSocketAddress(), milis, pong.campo(1)));
                } catch (SocketTimeoutException e) {
                    Registro.aviso("Intento " + i + ": sin respuesta en " + ESPERA_RESPUESTA_MILIS + " ms");
                } catch (MensajeInvalidoException e) {
                    Registro.aviso("Intento " + i + ": respuesta inválida (" + e.getMessage() + ")");
                }
            }
        } catch (IOException e) {
            Registro.error("Error de red", e);
        }
        Registro.info("Respuestas: " + respondidos + " de " + INTENTOS);
    }
}
