package juego.chihiro.red;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.CompletableFuture;

public final class DireccionServidor {

    private final String host;
    private final int puerto;

    public DireccionServidor(String host, int puerto) {
        this.host = host;
        this.puerto = Protocolo.validarPuerto(puerto);
    }

    public static DireccionServidor parsear(String texto) {
        String limpio = texto == null ? "" : texto.trim();
        if (limpio.isEmpty()) {
            throw new IllegalArgumentException("Escribí la IP del servidor");
        }
        int separador = limpio.lastIndexOf(':');
        if (separador < 0) {
            return new DireccionServidor(limpio, Protocolo.PUERTO_POR_DEFECTO);
        }
        String host = limpio.substring(0, separador);
        if (host.isEmpty()) {
            throw new IllegalArgumentException("Falta la IP antes de los dos puntos");
        }
        return new DireccionServidor(host, Protocolo.leerPuerto(limpio.substring(separador + 1)));
    }

    public CompletableFuture<InetSocketAddress> resolver() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return new InetSocketAddress(InetAddress.getByName(host), puerto);
            } catch (UnknownHostException e) {
                throw new IllegalArgumentException("No se encontró la dirección " + host, e);
            }
        });
    }

    public String getHost() {
        return host;
    }

    public int getPuerto() {
        return puerto;
    }

    @Override
    public String toString() {
        return host + ":" + puerto;
    }
}
