package juego.chihiro.red;

import java.net.InetSocketAddress;

public final class PaqueteRecibido {

    private final Mensaje mensaje;
    private final InetSocketAddress origen;
    private final long instanteMilis;

    public PaqueteRecibido(Mensaje mensaje, InetSocketAddress origen, long instanteMilis) {
        this.mensaje = mensaje;
        this.origen = origen;
        this.instanteMilis = instanteMilis;
    }

    public Mensaje getMensaje() {
        return mensaje;
    }

    public InetSocketAddress getOrigen() {
        return origen;
    }

    public long getInstanteMilis() {
        return instanteMilis;
    }
}
