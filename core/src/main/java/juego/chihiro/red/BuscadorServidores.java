package juego.chihiro.red;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BuscadorServidores {

    private static final long INTERVALO_BUSQUEDA_MILIS = 2000;
    private static final long VENCIMIENTO_MILIS = 6000;

    private final ClienteUdp cliente;
    private final int puerto;
    private final Map<String, ServidorEncontrado> encontrados = new LinkedHashMap<String, ServidorEncontrado>();
    private long ultimaBusquedaMilis;
    private int busquedasRealizadas;
    private int anunciosInvalidos;

    public BuscadorServidores(int puerto) throws SocketException {
        this.cliente = new ClienteUdp();
        this.puerto = puerto;
    }

    public void actualizar() {
        long ahora = System.currentTimeMillis();
        if (ahora - ultimaBusquedaMilis >= INTERVALO_BUSQUEDA_MILIS) {
            cliente.enviarBroadcast(new Mensaje(TipoMensaje.DESCUBRIR), puerto);
            ultimaBusquedaMilis = ahora;
            busquedasRealizadas++;
        }
        PaqueteRecibido paquete;
        while ((paquete = cliente.siguiente()) != null) {
            if (paquete.getMensaje().tipo() == TipoMensaje.ANUNCIO) {
                registrar(paquete);
            }
        }
        Iterator<ServidorEncontrado> iterador = encontrados.values().iterator();
        while (iterador.hasNext()) {
            if (ahora - iterador.next().getUltimaRespuestaMilis() > VENCIMIENTO_MILIS) {
                iterador.remove();
            }
        }
    }

    private void registrar(PaqueteRecibido paquete) {
        Mensaje anuncio = paquete.getMensaje();
        try {
            String nombre = anuncio.campo(0);
            int puertoJuego = anuncio.campoEntero(1);
            int conectados = anuncio.campoEntero(2);
            int maximo = anuncio.campoEntero(3);
            InetSocketAddress direccion =
                    new InetSocketAddress(paquete.getOrigen().getAddress(), puertoJuego);
            String clave = direccion.getAddress().getHostAddress() + ":" + puertoJuego;
            ServidorEncontrado servidor = encontrados.get(clave);
            if (servidor == null) {
                servidor = new ServidorEncontrado(direccion);
                encontrados.put(clave, servidor);
            }
            servidor.actualizar(nombre, conectados, maximo, paquete.getInstanteMilis());
        } catch (MensajeInvalidoException e) {
            anunciosInvalidos++;
        }
    }

    public List<ServidorEncontrado> getEncontrados() {
        return new ArrayList<ServidorEncontrado>(encontrados.values());
    }

    public int getBusquedasRealizadas() {
        return busquedasRealizadas;
    }

    public int getAnunciosInvalidos() {
        return anunciosInvalidos;
    }

    public void cerrar() {
        cliente.cerrar();
    }
}
