package juego.chihiro.servidor;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import juego.chihiro.red.Mensaje;
import juego.chihiro.red.MensajeInvalidoException;
import juego.chihiro.red.PaqueteRecibido;
import juego.chihiro.red.Protocolo;

public class ServidorUdp {

    private static final long ESPERA_CIERRE_HILO_MILIS = 1000;

    private final int puerto;
    private final DatagramSocket socket;
    private final ConcurrentLinkedQueue<PaqueteRecibido> entrantes = new ConcurrentLinkedQueue<PaqueteRecibido>();
    private final AtomicLong paquetesRecibidos = new AtomicLong();
    private final AtomicLong paquetesDescartados = new AtomicLong();
    private final AtomicLong paquetesEnviados = new AtomicLong();
    private final Thread hiloRecepcion;
    private volatile boolean activo;

    public ServidorUdp(int puerto) throws PuertoOcupadoException, SocketException {
        this.puerto = Protocolo.validarPuerto(puerto);
        DatagramSocket nuevoSocket = new DatagramSocket(null);
        try {
            nuevoSocket.setReuseAddress(false);
            nuevoSocket.setBroadcast(true);
            nuevoSocket.bind(new InetSocketAddress(this.puerto));
        } catch (BindException e) {
            nuevoSocket.close();
            throw new PuertoOcupadoException(this.puerto, e);
        } catch (SocketException e) {
            nuevoSocket.close();
            throw e;
        }
        this.socket = nuevoSocket;
        this.hiloRecepcion = new Thread(this::escuchar, "red-servidor-recepcion");
        this.hiloRecepcion.setDaemon(true);
    }

    public synchronized void iniciar() {
        if (activo || socket.isClosed()) {
            return;
        }
        activo = true;
        hiloRecepcion.start();
    }

    private void escuchar() {
        byte[] buffer = new byte[Protocolo.TAMANIO_MAXIMO_PAQUETE];
        while (activo) {
            DatagramPacket paquete = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(paquete);
                paquetesRecibidos.incrementAndGet();
                Mensaje mensaje = Mensaje.desdeBytes(paquete.getData(), paquete.getLength());
                InetSocketAddress origen = new InetSocketAddress(paquete.getAddress(), paquete.getPort());
                entrantes.add(new PaqueteRecibido(mensaje, origen, System.currentTimeMillis()));
            } catch (MensajeInvalidoException e) {
                paquetesDescartados.incrementAndGet();
                Registro.aviso("Paquete descartado de " + paquete.getSocketAddress() + ": " + e.getMessage());
            } catch (SocketException e) {
                if (!activo || socket.isClosed()) {
                    break;
                }
                Registro.error("Error en el socket del servidor", e);
            } catch (IOException e) {
                if (activo) {
                    Registro.error("Error al recibir un paquete", e);
                }
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
            paquetesEnviados.incrementAndGet();
            return true;
        } catch (IOException e) {
            Registro.error("No se pudo enviar " + mensaje.tipo() + " a " + destino, e);
            return false;
        } catch (IllegalStateException e) {
            Registro.error("Mensaje demasiado grande para enviar a " + destino, e);
            return false;
        }
    }

    public synchronized void cerrar() {
        if (socket.isClosed()) {
            return;
        }
        activo = false;
        socket.close();
        if (hiloRecepcion.isAlive() && Thread.currentThread() != hiloRecepcion) {
            try {
                hiloRecepcion.join(ESPERA_CIERRE_HILO_MILIS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        Registro.info("Socket UDP cerrado en el puerto " + puerto);
    }

    public boolean estaActivo() {
        return activo && !socket.isClosed();
    }

    public int getPuerto() {
        return puerto;
    }

    public long getPaquetesRecibidos() {
        return paquetesRecibidos.get();
    }

    public long getPaquetesDescartados() {
        return paquetesDescartados.get();
    }

    public long getPaquetesEnviados() {
        return paquetesEnviados.get();
    }

    public int getPaquetesEnCola() {
        return entrantes.size();
    }
}
