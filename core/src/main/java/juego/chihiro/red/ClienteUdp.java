package juego.chihiro.red;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClienteUdp {

    private static final long ESPERA_CIERRE_HILO_MILIS = 500;

    private final DatagramSocket socket;
    private final ConcurrentLinkedQueue<PaqueteRecibido> entrantes = new ConcurrentLinkedQueue<PaqueteRecibido>();
    private final Thread hiloRecepcion;
    private volatile boolean activo;
    private volatile String ultimoError = "";

    public ClienteUdp() throws SocketException {
        socket = new DatagramSocket();
        socket.setBroadcast(true);
        activo = true;
        hiloRecepcion = new Thread(this::escuchar, "red-cliente-recepcion");
        hiloRecepcion.setDaemon(true);
        hiloRecepcion.start();
    }

    private void escuchar() {
        byte[] buffer = new byte[Protocolo.TAMANIO_MAXIMO_PAQUETE];
        while (activo) {
            DatagramPacket paquete = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(paquete);
                Mensaje mensaje = Mensaje.desdeBytes(paquete.getData(), paquete.getLength());
                InetSocketAddress origen = new InetSocketAddress(paquete.getAddress(), paquete.getPort());
                entrantes.add(new PaqueteRecibido(mensaje, origen, System.currentTimeMillis()));
            } catch (MensajeInvalidoException e) {
                ultimoError = e.getMessage();
            } catch (SocketException e) {
                if (!activo || socket.isClosed()) {
                    break;
                }
                ultimoError = e.getMessage();
            } catch (IOException e) {
                ultimoError = e.getMessage();
            }
        }
    }

    public PaqueteRecibido siguiente() {
        return entrantes.poll();
    }

    public boolean enviar(Mensaje mensaje, SocketAddress destino) {
        if (!activo || destino == null) {
            return false;
        }
        try {
            byte[] datos = mensaje.aBytes();
            socket.send(new DatagramPacket(datos, datos.length, destino));
            return true;
        } catch (IOException | IllegalStateException e) {
            ultimoError = e.getMessage();
            return false;
        }
    }

    public int enviarBroadcast(Mensaje mensaje, int puerto) {
        int enviados = 0;
        for (InetAddress destino : InterfacesRed.direccionesBroadcast()) {
            if (enviar(mensaje, new InetSocketAddress(destino, puerto))) {
                enviados++;
            }
        }
        return enviados;
    }

    public synchronized void cerrar() {
        if (socket.isClosed()) {
            return;
        }
        activo = false;
        socket.close();
        if (Thread.currentThread() != hiloRecepcion) {
            try {
                hiloRecepcion.join(ESPERA_CIERRE_HILO_MILIS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public boolean estaActivo() {
        return activo && !socket.isClosed();
    }

    public String getUltimoError() {
        return ultimoError;
    }
}
