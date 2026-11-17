package juego.chihiro.red;

import java.net.InetSocketAddress;

public class ServidorEncontrado {

    private final InetSocketAddress direccion;
    private String nombre;
    private int conectados;
    private int maximo;
    private long ultimaRespuestaMilis;

    public ServidorEncontrado(InetSocketAddress direccion) {
        this.direccion = direccion;
    }

    public void actualizar(String nombre, int conectados, int maximo, long instanteMilis) {
        this.nombre = nombre;
        this.conectados = conectados;
        this.maximo = maximo;
        this.ultimaRespuestaMilis = instanteMilis;
    }

    public InetSocketAddress getDireccion() {
        return direccion;
    }

    public String getNombre() {
        return nombre;
    }

    public int getConectados() {
        return conectados;
    }

    public int getMaximo() {
        return maximo;
    }

    public long getUltimaRespuestaMilis() {
        return ultimaRespuestaMilis;
    }

    public boolean estaLleno() {
        return conectados >= maximo;
    }

    public String describir() {
        return nombre + "  -  " + direccion.getAddress().getHostAddress() + ":" + direccion.getPort()
                + "  -  " + conectados + "/" + maximo + " jugadores";
    }
}
