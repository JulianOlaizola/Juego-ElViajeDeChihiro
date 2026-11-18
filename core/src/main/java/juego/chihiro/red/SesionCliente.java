package juego.chihiro.red;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class SesionCliente {

    private final ClienteUdp cliente;
    private final String nombreJugador;
    private final String descripcionServidor;
    private CompletableFuture<InetSocketAddress> resolucion;
    private InetSocketAddress servidor;

    private EstadoConexion estado;
    private String motivo = "";
    private String token = Protocolo.SIN_VALOR;
    private int numeroJugador;
    private int intentosConexion;
    private long ultimoIntentoMilis;
    private long ultimoPaqueteMilis;
    private long ultimaEntradaMilis;
    private long secuenciaEntrada;
    private InstantaneaPartida instantanea = new InstantaneaPartida();
    private boolean hayInstantanea;
    private final ReceptorEventos eventos = new ReceptorEventos();
    private long instantaneasRecibidas;
    private long instantaneasDescartadas;

    private float ejeX;
    private float ejeY;
    private int contadorInteraccion;
    private int contadorPausa;
    private boolean listo;

    public SesionCliente(InetSocketAddress servidor, String descripcionServidor, String nombreJugador) throws SocketException {
        this.cliente = new ClienteUdp();
        this.servidor = servidor;
        this.descripcionServidor = descripcionServidor;
        this.nombreJugador = nombreJugador;
        this.estado = EstadoConexion.CONECTANDO;
    }

    public SesionCliente(DireccionServidor direccion, String nombreJugador) throws SocketException {
        this.cliente = new ClienteUdp();
        this.resolucion = direccion.resolver();
        this.descripcionServidor = direccion.toString();
        this.nombreJugador = nombreJugador;
        this.estado = EstadoConexion.RESOLVIENDO;
    }

    public void actualizar() {
        long ahora = System.currentTimeMillis();
        if (estado == EstadoConexion.RESOLVIENDO) {
            esperarResolucion(ahora);
            return;
        }
        if (!sigueActiva()) {
            return;
        }
        recibir(ahora);
        if (estado == EstadoConexion.CONECTANDO) {
            reintentarConexion(ahora);
        } else if (estado == EstadoConexion.CONECTADO) {
            enviarEntrada(ahora);
            if (ahora - ultimoPaqueteMilis > Protocolo.TIEMPO_ESPERA_MILIS) {
                perder("El servidor dejó de responder");
            }
        }
    }

    private void esperarResolucion(long ahora) {
        if (!resolucion.isDone()) {
            return;
        }
        try {
            servidor = resolucion.join();
            estado = EstadoConexion.CONECTANDO;
            ultimoIntentoMilis = 0;
            reintentarConexion(ahora);
        } catch (CompletionException e) {
            Throwable causa = e.getCause() != null ? e.getCause() : e;
            perder(causa.getMessage());
        }
    }

    private void reintentarConexion(long ahora) {
        if (ahora - ultimoIntentoMilis < Protocolo.INTERVALO_REINTENTO_CONEXION_MILIS) {
            return;
        }
        if (intentosConexion >= Protocolo.INTENTOS_CONEXION) {
            perder("El servidor no respondió. Revisá la IP, el puerto y el firewall.");
            return;
        }
        intentosConexion++;
        ultimoIntentoMilis = ahora;
        cliente.enviar(new Mensaje(TipoMensaje.CONECTAR, nombreJugador, token), servidor);
    }

    private void recibir(long ahora) {
        PaqueteRecibido paquete;
        while ((paquete = cliente.siguiente()) != null) {
            if (!paquete.getOrigen().equals(servidor)) {
                continue;
            }
            ultimoPaqueteMilis = ahora;
            try {
                atender(paquete.getMensaje());
            } catch (MensajeInvalidoException e) {
                motivo = "Paquete inválido del servidor: " + e.getMessage();
            }
        }
    }

    private void atender(Mensaje mensaje) throws MensajeInvalidoException {
        switch (mensaje.tipo()) {
            case BIENVENIDA:
                String tokenNuevo = mensaje.campo(1);
                if (!tokenNuevo.equals(token)) {
                    eventos.reiniciar();
                }
                numeroJugador = mensaje.campoEntero(0);
                token = tokenNuevo;
                estado = EstadoConexion.CONECTADO;
                motivo = "";
                break;
            case RECHAZO:
                MotivoRechazo rechazo = MotivoRechazo.desdeTexto(mensaje.campo(0));
                estado = EstadoConexion.RECHAZADO;
                motivo = rechazo == null ? "El servidor rechazó la conexión" : rechazo.getDescripcion();
                break;
            case ESTADO:
                recibirInstantanea(InstantaneaPartida.desdeMensaje(mensaje));
                break;
            case EVENTO:
                EventoRed evento = EventoRed.desdeMensaje(mensaje);
                cliente.enviar(new Mensaje(TipoMensaje.ACK, token, String.valueOf(evento.getId())), servidor);
                eventos.recibir(evento);
                break;
            default:
                break;
        }
    }

    private void recibirInstantanea(InstantaneaPartida nueva) {
        instantaneasRecibidas++;
        if (hayInstantanea && nueva.getSecuencia() <= instantanea.getSecuencia()) {
            instantaneasDescartadas++;
            return;
        }
        if (nueva.getFase() != instantanea.getFase()) {
            listo = false;
        }
        instantanea = nueva;
        hayInstantanea = true;
    }

    private void enviarEntrada(long ahora) {
        if (ahora - ultimaEntradaMilis < Protocolo.INTERVALO_ENTRADA_MILIS) {
            return;
        }
        ultimaEntradaMilis = ahora;
        secuenciaEntrada++;
        cliente.enviar(new Mensaje(TipoMensaje.ENTRADA,
                token,
                String.valueOf(secuenciaEntrada),
                String.valueOf(ejeX),
                String.valueOf(ejeY),
                String.valueOf(contadorInteraccion),
                String.valueOf(contadorPausa),
                InstantaneaPartida.bandera(listo)), servidor);
    }

    private void perder(String causa) {
        estado = EstadoConexion.PERDIDA;
        motivo = causa == null ? "Error de red desconocido" : causa;
    }

    private boolean sigueActiva() {
        return estado == EstadoConexion.CONECTANDO || estado == EstadoConexion.CONECTADO;
    }

    public void setEjes(float x, float y) {
        ejeX = x;
        ejeY = y;
    }

    public void pulsarInteraccion() {
        contadorInteraccion++;
    }

    public void pedirPausa() {
        contadorPausa++;
    }

    public void alternarListo() {
        listo = !listo;
    }

    public boolean estaListo() {
        return listo;
    }

    public void cerrar() {
        if (estado == EstadoConexion.CONECTADO && servidor != null) {
            for (int i = 0; i < 3; i++) {
                cliente.enviar(new Mensaje(TipoMensaje.DESCONECTAR, token), servidor);
            }
        }
        estado = EstadoConexion.CERRADA;
        cliente.cerrar();
    }

    public EstadoConexion getEstado() {
        return estado;
    }

    public String getMotivo() {
        return motivo;
    }

    public int getNumeroJugador() {
        return numeroJugador;
    }

    public String getDescripcionServidor() {
        return descripcionServidor;
    }

    public InstantaneaPartida getInstantanea() {
        return instantanea;
    }

    public boolean tieneInstantanea() {
        return hayInstantanea;
    }

    public EventoRed siguienteEvento() {
        return eventos.siguiente();
    }

    public long getEventosDuplicados() {
        return eventos.getDuplicados();
    }

    public long getInstantaneasRecibidas() {
        return instantaneasRecibidas;
    }

    public long getInstantaneasDescartadas() {
        return instantaneasDescartadas;
    }

    public int getIntentosConexion() {
        return intentosConexion;
    }
}
