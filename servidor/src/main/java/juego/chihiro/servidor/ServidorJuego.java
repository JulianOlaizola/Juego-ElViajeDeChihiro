package juego.chihiro.servidor;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;

import juego.chihiro.mundo.TipoEspiritu;
import juego.chihiro.red.FasePartida;
import juego.chihiro.red.InstantaneaPartida;
import juego.chihiro.red.InterfacesRed;
import juego.chihiro.red.LectorCampos;
import juego.chihiro.red.Mensaje;
import juego.chihiro.red.MensajeInvalidoException;
import juego.chihiro.red.MotivoRechazo;
import juego.chihiro.red.PaqueteRecibido;
import juego.chihiro.red.Protocolo;
import juego.chihiro.red.TipoEvento;
import juego.chihiro.red.TipoMensaje;
import juego.chihiro.servidor.fisica.MapaInvalidoException;
import juego.chihiro.servidor.fisica.MapaServidor;
import juego.chihiro.servidor.logica.Aviso;
import juego.chihiro.servidor.logica.JugadorServidor;
import juego.chihiro.servidor.logica.Partida;
import juego.chihiro.servidor.logica.PedidoServidor;
import juego.chihiro.servidor.logica.TinaServidor;
import juego.chihiro.servidor.sesiones.GestorSesiones;
import juego.chihiro.servidor.sesiones.SesionJugador;
import juego.chihiro.utiles.Constantes;

public class ServidorJuego extends ApplicationAdapter {

    public static final int TICKS_POR_SEGUNDO = 60;
    private static final int MAXIMO_PAQUETES_POR_TICK = 256;

    private final ServidorUdp red;
    private final GestorSesiones sesiones = new GestorSesiones();
    private MapaServidor mapa;
    private Partida partida;
    private FasePartida fase = FasePartida.SALA;
    private String nombreServidor = "Casa de Baños";
    private long ticks;
    private long secuenciaEstado;
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
        instanteAnterior = System.nanoTime();
        nombreServidor = buscarNombreDelEquipo();

        Registro.info("Servidor de El Viaje de Chihiro escuchando por UDP en el puerto " + red.getPuerto());
        Registro.info("Mapa cargado: " + mapa.getColisiones().size + " paredes y "
            + mapa.getInteractivos().size + " objetos interactivos. Box2D se crea al empezar cada partida.");
        mostrarDirecciones();
        Registro.info("Esperando jugadores. Comandos disponibles: estado, ip, terminar, ayuda, salir");
    }

    private String buscarNombreDelEquipo() {
        try {
            return "Casa de Baños de " + InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return nombreServidor;
        }
    }

    public void mostrarDirecciones() {
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
        if (mapa == null) {
            return;
        }
        long ahora = System.nanoTime();
        float delta = (ahora - instanteAnterior) / 1_000_000_000f;
        instanteAnterior = ahora;
        ticks++;
        long ahoraMilis = System.currentTimeMillis();

        procesarEntrantes();
        reenviarEventos(ahoraMilis);
        revisarDesconexiones(ahoraMilis);
        actualizarFase();
        atenderPausas();
        if (partida != null && fase == FasePartida.JUGANDO) {
            simular(delta);
        }
        if (ticks % Protocolo.TICKS_ENTRE_ESTADOS == 0) {
            difundirEstado();
        }
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
            case DESCUBRIR:
                responderDescubrimiento(paquete);
                break;
            case CONECTAR:
                conectar(paquete);
                break;
            case ENTRADA:
                recibirEntrada(paquete);
                break;
            case ACK:
                confirmarEvento(paquete);
                break;
            case DESCONECTAR:
                desconectar(paquete);
                break;
            default:
                Registro.aviso("Mensaje no esperado por el servidor: " + mensaje + " de " + paquete.getOrigen());
                break;
        }
    }

    private void responderPing(PaqueteRecibido paquete) throws MensajeInvalidoException {
        String marcaDeTiempo = paquete.getMensaje().campo(0);
        red.enviar(new Mensaje(TipoMensaje.PONG, marcaDeTiempo, String.valueOf(ticks)), paquete.getOrigen());
    }

    private void responderDescubrimiento(PaqueteRecibido paquete) {
        red.enviar(new Mensaje(TipoMensaje.ANUNCIO,
            nombreServidor,
            String.valueOf(red.getPuerto()),
            String.valueOf(sesiones.cantidadConectados()),
            String.valueOf(Constantes.CANTIDAD_JUGADORES)), paquete.getOrigen());
    }

    private void conectar(PaqueteRecibido paquete) throws MensajeInvalidoException {
        String nombre = paquete.getMensaje().campo(0);
        String tokenPrevio = paquete.getMensaje().campo(1);
        SesionJugador conocida = sesiones.buscarPorToken(tokenPrevio);
        boolean eraConocido = conocida != null;
        boolean vuelve = eraConocido && !conocida.estaConectado();
        if (fase != FasePartida.SALA && !eraConocido) {
            red.enviar(new Mensaje(TipoMensaje.RECHAZO, MotivoRechazo.PARTIDA_EN_CURSO.name()), paquete.getOrigen());
            Registro.aviso("Rechazado " + paquete.getOrigen() + ": la partida ya empezó");
            return;
        }
        SesionJugador sesion = sesiones.conectar(nombre, tokenPrevio, paquete.getOrigen(), System.currentTimeMillis());
        if (sesion == null) {
            red.enviar(new Mensaje(TipoMensaje.RECHAZO, MotivoRechazo.SALA_LLENA.name()), paquete.getOrigen());
            Registro.aviso("Rechazado " + paquete.getOrigen() + ": la sala está llena");
            return;
        }
        red.enviar(new Mensaje(TipoMensaje.BIENVENIDA, String.valueOf(sesion.getNumero()), sesion.getToken()),
            paquete.getOrigen());
        if (vuelve) {
            anunciarRegreso(sesion);
        } else if (!eraConocido) {
            Registro.info("Jugador " + sesion.getNumero() + " (" + sesion.getNombre() + ") conectado desde " + paquete.getOrigen());
        }
    }

    private void anunciarRegreso(SesionJugador sesion) {
        Registro.info("Jugador " + sesion.getNumero() + " se reconectó desde " + sesion.getDireccion());
        difundirEvento(TipoEvento.JUGADOR_RECONECTADO, String.valueOf(sesion.getNumero()));
        if (fase == FasePartida.ESPERANDO_JUGADOR && !sesiones.hayReservas()) {
            fase = FasePartida.PAUSA;
            Registro.info("Están los dos jugadores otra vez: la partida queda en pausa hasta que alguien la reanude");
        }
    }

    private void recibirEntrada(PaqueteRecibido paquete) throws MensajeInvalidoException {
        LectorCampos lector = new LectorCampos(paquete.getMensaje());
        SesionJugador sesion = sesiones.buscarPorToken(lector.texto());
        if (sesion == null) {
            red.enviar(new Mensaje(TipoMensaje.RECHAZO, MotivoRechazo.SESION_DESCONOCIDA.name()), paquete.getOrigen());
            return;
        }
        if (!sesion.estaConectado()) {
            sesion.tocar(paquete.getOrigen(), System.currentTimeMillis());
            sesion.setConectado(true);
            anunciarRegreso(sesion);
        }
        long secuencia = lector.largo();
        float ejeX = lector.decimal();
        float ejeY = lector.decimal();
        int interacciones = lector.entero();
        int pausas = lector.entero();
        boolean listo = lector.bandera();
        sesion.registrarEntrada(secuencia, ejeX, ejeY, interacciones, pausas, listo,
            paquete.getOrigen(), System.currentTimeMillis());
    }

    private void confirmarEvento(PaqueteRecibido paquete) throws MensajeInvalidoException {
        SesionJugador sesion = sesiones.buscarPorToken(paquete.getMensaje().campo(0));
        if (sesion != null) {
            sesion.getCanal().confirmar(paquete.getMensaje().campoLargo(1));
        }
    }

    private void enviarEvento(SesionJugador sesion, TipoEvento tipo, String... datos) {
        if (sesion.estaConectado()) {
            red.enviar(sesion.getCanal().encolar(tipo, datos), sesion.getDireccion());
        }
    }

    private void difundirEvento(TipoEvento tipo, String... datos) {
        for (SesionJugador sesion : sesiones.conectadas()) {
            enviarEvento(sesion, tipo, datos);
        }
    }

    private void reenviarEventos(long ahora) {
        for (SesionJugador sesion : sesiones.conectadas()) {
            for (Mensaje mensaje : sesion.getCanal().paraReenviar(ahora)) {
                red.enviar(mensaje, sesion.getDireccion());
            }
        }
    }

    private void desconectar(PaqueteRecibido paquete) throws MensajeInvalidoException {
        SesionJugador sesion = sesiones.buscarPorToken(paquete.getMensaje().campo(0));
        if (sesion != null) {
            Registro.info("Jugador " + sesion.getNumero() + " salió de la partida");
            liberar(sesion);
        }
    }

    private void revisarDesconexiones(long ahora) {
        for (SesionJugador sesion : sesiones.vencidas(ahora)) {
            Registro.aviso("Jugador " + sesion.getNumero() + " no responde hace "
                + (Protocolo.TIEMPO_ESPERA_MILIS / 1000) + " segundos");
            perderConexion(sesion);
        }
        for (SesionJugador sesion : sesiones.reservasVencidas(ahora)) {
            Registro.aviso("Jugador " + sesion.getNumero() + " no volvió a tiempo: se libera su lugar");
            liberar(sesion);
        }
    }

    private void perderConexion(SesionJugador sesion) {
        boolean partidaEnCurso = fase == FasePartida.JUGANDO
            || fase == FasePartida.PAUSA
            || fase == FasePartida.ESPERANDO_JUGADOR;
        if (!partidaEnCurso) {
            liberar(sesion);
            return;
        }
        sesion.setConectado(false);
        fase = FasePartida.ESPERANDO_JUGADOR;
        difundirEvento(TipoEvento.JUGADOR_DESCONECTADO, String.valueOf(sesion.getNumero()));
        Registro.aviso("La partida queda en espera: el jugador " + sesion.getNumero() + " puede volver hasta "
            + (Protocolo.TIEMPO_RECONEXION_SERVIDOR_MILIS / 1000) + " segundos después de su último paquete");
    }

    private void liberar(SesionJugador sesion) {
        int numero = sesion.getNumero();
        sesion.liberar();
        difundirEvento(TipoEvento.JUGADOR_DESCONECTADO, String.valueOf(numero));
        if (fase != FasePartida.SALA) {
            volverALaSala();
        }
    }

    private void actualizarFase() {
        if (fase == FasePartida.SALA && sesiones.todosListos()) {
            comenzarPartida();
        } else if (fase == FasePartida.TERMINADA && sesiones.todosListos()) {
            Registro.info("Los dos jugadores votaron revancha");
            comenzarPartida();
            difundirEvento(TipoEvento.AVISO, "¡Revancha! Empieza un turno nuevo", "PEDIDO_COMPLETO");
        }
    }

    private void atenderPausas() {
        for (SesionJugador sesion : sesiones.conectadas()) {
            if (!sesion.consumirPedidoDePausa()) {
                continue;
            }
            if (fase == FasePartida.JUGANDO) {
                fase = FasePartida.PAUSA;
                difundirEvento(TipoEvento.AVISO, "El jugador " + sesion.getNumero() + " pausó la partida", "NINGUNO");
                Registro.info("Jugador " + sesion.getNumero() + " pausó la partida");
            } else if (fase == FasePartida.PAUSA) {
                fase = FasePartida.JUGANDO;
                difundirEvento(TipoEvento.AVISO, "El jugador " + sesion.getNumero() + " reanudó la partida", "NINGUNO");
                Registro.info("Jugador " + sesion.getNumero() + " reanudó la partida");
            }
        }
    }

    private void simular(float delta) {
        for (SesionJugador sesion : sesiones.todas()) {
            partida.aplicarEntrada(sesion.getNumero(), sesion.getEjeX(), sesion.getEjeY(),
                sesion.getContadorInteraccion());
        }
        partida.actualizar(delta);
        for (Aviso aviso : partida.retirarAvisos()) {
            if (aviso.getJugador() == 0) {
                difundirEvento(TipoEvento.AVISO, aviso.getTexto(), aviso.getEfecto().name());
            } else {
                enviarEvento(sesiones.get(aviso.getJugador()), TipoEvento.AVISO, aviso.getTexto(), aviso.getEfecto().name());
            }
        }
        if (partida.estaTerminada()) {
            fase = FasePartida.TERMINADA;
            difundirEvento(TipoEvento.FIN_PARTIDA, partida.gano() ? "1" : "0", String.valueOf(partida.getPuntaje()));
            sesiones.reiniciarListos();
            Registro.info("Partida terminada. " + (partida.gano() ? "Ganaron" : "Perdieron")
                + " con " + partida.getPuntaje() + " puntos. Pueden votar revancha desde el juego.");
        }
    }

    private void comenzarPartida() {
        sesiones.reiniciarListos();
        int[] contadores = new int[Constantes.CANTIDAD_JUGADORES];
        for (SesionJugador sesion : sesiones.todas()) {
            contadores[sesion.getNumero() - 1] = sesion.getContadorInteraccion();
            sesion.descartarPedidosDePausa();
        }
        descartarPartida();
        partida = new Partida(mapa, contadores);
        fase = FasePartida.JUGANDO;
        difundirEvento(TipoEvento.INICIO_PARTIDA);
        Registro.info("Los dos jugadores están listos: comienza la partida con un mundo Box2D nuevo");
    }

    private void descartarPartida() {
        if (partida != null) {
            partida.dispose();
            partida = null;
        }
    }

    private void volverALaSala() {
        fase = FasePartida.SALA;
        sesiones.reiniciarListos();
        descartarPartida();
        Registro.info("La partida se canceló: se vuelve a la sala de espera");
    }

    private void difundirEstado() {
        InstantaneaPartida instantanea = new InstantaneaPartida();
        instantanea.setSecuencia(++secuenciaEstado);
        instantanea.setFase(fase);
        instantanea.setSegundosParaReconexion((int) (sesiones.milisParaReconexion(System.currentTimeMillis()) / 1000));
        for (SesionJugador sesion : sesiones.todas()) {
            instantanea.setJugador(sesion.getNumero(), sesion.estaConectado(), sesion.estaListo());
        }
        if (partida != null) {
            completarConLaPartida(instantanea);
        }
        Mensaje mensaje = instantanea.aMensaje();
        for (SesionJugador sesion : sesiones.conectadas()) {
            red.enviar(mensaje, sesion.getDireccion());
        }
    }

    private void completarConLaPartida(InstantaneaPartida instantanea) {
        instantanea.setPartida(partida.getTiempoRestante(), partida.getPedidosCompletados(),
            partida.getPedidosFallidos(), partida.getPuntaje(), partida.gano());
        instantanea.setCaldera(partida.estaLaCalderaEncendida(), partida.estaElAguaCaliente(),
            partida.getProporcionCaldera());
        for (int numero = 1; numero <= Constantes.CANTIDAD_JUGADORES; numero++) {
            JugadorServidor jugador = partida.getJugador(numero);
            instantanea.setCuerpo(numero, jugador.getCentroX(), jugador.getCentroY(), jugador.getDireccion(),
                jugador.estaEnMovimiento(), jugador.getObjetoEnMano());
        }
        for (TinaServidor tina : partida.getTinas()) {
            PedidoServidor pedido = tina.getPedido();
            instantanea.agregarTina(tina.getEstado(),
                pedido == null ? TipoEspiritu.COMUN : pedido.getTipo(),
                pedido != null && pedido.faltaSal(),
                pedido != null && pedido.faltaAgua(),
                pedido != null && pedido.faltaHierbas(),
                pedido == null ? 0f : pedido.getProporcionRestante());
        }
    }

    public void terminarPartida() {
        if (partida == null || (fase != FasePartida.JUGANDO && fase != FasePartida.PAUSA)) {
            Registro.aviso("No hay ninguna partida en curso para terminar");
            return;
        }
        Registro.info("Se termina la partida desde la consola");
        partida.forzarFin();
        fase = FasePartida.JUGANDO;
    }

    public void imprimirEstado() {
        Registro.info("Fase: " + fase + " | Ticks: " + ticks
            + " | Recibidos: " + red.getPaquetesRecibidos()
            + " | Enviados: " + red.getPaquetesEnviados()
            + " | Descartados: " + red.getPaquetesDescartados()
            + " | En cola: " + red.getPaquetesEnCola());
        for (SesionJugador sesion : sesiones.todas()) {
            String detalle = sesion.estaConectado()
                ? sesion.getNombre() + " en " + sesion.getDireccion() + (sesion.estaListo() ? " (listo)" : "")
                  + " | eventos sin confirmar: " + sesion.getCanal().getPendientes()
                  + " | reenvíos: " + sesion.getCanal().getReenvios()
                : "libre";
            Registro.info("Jugador " + sesion.getNumero() + ": " + detalle);
        }
        if (partida == null) {
            return;
        }
        Registro.info("Tiempo: " + Math.round(partida.getTiempoRestante()) + " s | Pedidos: "
            + partida.getPedidosCompletados() + " | Fallos: " + partida.getPedidosFallidos()
            + " | Puntaje: " + partida.getPuntaje());
        for (int numero = 1; numero <= Constantes.CANTIDAD_JUGADORES; numero++) {
            JugadorServidor jugador = partida.getJugador(numero);
            Registro.info("Cuerpo del jugador " + numero + " en (" + Math.round(jugador.getCentroX()) + ", "
                + Math.round(jugador.getCentroY()) + ") con " + jugador.getObjetoEnMano().getEtiqueta());
        }
    }

    private void avisarCierre() {
        Mensaje cierre = new Mensaje(TipoMensaje.DESCONECTAR, "El servidor se cerró");
        for (SesionJugador sesion : sesiones.conectadas()) {
            for (int i = 0; i < 3; i++) {
                red.enviar(cierre, sesion.getDireccion());
            }
        }
    }

    @Override
    public void dispose() {
        avisarCierre();
        descartarPartida();
        red.cerrar();
        Registro.info("Servidor cerrado.");
        Registro.cerrarArchivo();
    }
}
