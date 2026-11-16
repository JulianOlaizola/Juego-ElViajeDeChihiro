package juego.chihiro.servidor;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;

import juego.chihiro.red.FasePartida;
import juego.chihiro.red.InstantaneaPartida;
import juego.chihiro.red.InterfacesRed;
import juego.chihiro.red.LectorCampos;
import juego.chihiro.red.Mensaje;
import juego.chihiro.red.MensajeInvalidoException;
import juego.chihiro.red.MotivoRechazo;
import juego.chihiro.red.PaqueteRecibido;
import juego.chihiro.red.Protocolo;
import juego.chihiro.red.TipoMensaje;
import juego.chihiro.servidor.fisica.MapaInvalidoException;
import juego.chihiro.servidor.fisica.MapaServidor;
import juego.chihiro.servidor.fisica.MundoFisico;
import juego.chihiro.servidor.logica.JugadorServidor;
import juego.chihiro.servidor.sesiones.GestorSesiones;
import juego.chihiro.servidor.sesiones.SesionJugador;
import juego.chihiro.utiles.Constantes;

public class ServidorJuego extends ApplicationAdapter {

    public static final int TICKS_POR_SEGUNDO = 60;
    private static final int MAXIMO_PAQUETES_POR_TICK = 256;

    private final ServidorUdp red;
    private final GestorSesiones sesiones = new GestorSesiones();
    private final JugadorServidor[] jugadores = new JugadorServidor[Constantes.CANTIDAD_JUGADORES];
    private MapaServidor mapa;
    private MundoFisico mundo;
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
        mundo = new MundoFisico(mapa);
        for (int i = 0; i < jugadores.length; i++) {
            jugadores[i] = new JugadorServidor(i + 1);
            jugadores[i].ubicar(mundo, mapa.getAparicion(i + 1));
        }
        instanteAnterior = System.nanoTime();
        nombreServidor = buscarNombreDelEquipo();

        Registro.info("Servidor de El Viaje de Chihiro escuchando por UDP en el puerto " + red.getPuerto());
        Registro.info("Mundo físico Box2D listo: " + mundo.getCantidadParedes() + " paredes, "
                + mundo.getCantidadSensores() + " objetos interactivos y " + jugadores.length + " jugadores");
        mostrarDirecciones();
        Registro.info("Esperando jugadores. Comandos disponibles: estado, ayuda, salir");
    }

    private String buscarNombreDelEquipo() {
        try {
            return "Casa de Baños de " + InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return nombreServidor;
        }
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
        long ahoraMilis = System.currentTimeMillis();

        procesarEntrantes();
        revisarDesconexiones(ahoraMilis);
        actualizarFase();
        if (fase == FasePartida.JUGANDO) {
            for (JugadorServidor jugador : jugadores) {
                jugador.aplicarMovimiento();
            }
            mundo.avanzar(delta);
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
        boolean eraConocido = sesiones.buscarPorToken(tokenPrevio) != null;
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
        Registro.info("Jugador " + sesion.getNumero() + " (" + sesion.getNombre() + ") conectado desde " + paquete.getOrigen());
    }

    private void recibirEntrada(PaqueteRecibido paquete) throws MensajeInvalidoException {
        LectorCampos lector = new LectorCampos(paquete.getMensaje());
        SesionJugador sesion = sesiones.buscarPorToken(lector.texto());
        if (sesion == null || !sesion.estaConectado()) {
            red.enviar(new Mensaje(TipoMensaje.RECHAZO, MotivoRechazo.SESION_DESCONOCIDA.name()), paquete.getOrigen());
            return;
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

    private void desconectar(PaqueteRecibido paquete) throws MensajeInvalidoException {
        SesionJugador sesion = sesiones.buscarPorToken(paquete.getMensaje().campo(0));
        if (sesion != null) {
            Registro.info("Jugador " + sesion.getNumero() + " salió de la partida");
            quitar(sesion);
        }
    }

    private void revisarDesconexiones(long ahora) {
        for (SesionJugador sesion : sesiones.vencidas(ahora)) {
            Registro.aviso("Jugador " + sesion.getNumero() + " no responde hace "
                    + (Protocolo.TIEMPO_ESPERA_MILIS / 1000) + " segundos: se lo desconecta");
            quitar(sesion);
        }
    }

    private void quitar(SesionJugador sesion) {
        sesion.liberar();
        if (fase != FasePartida.SALA) {
            volverALaSala();
        }
    }

    private void actualizarFase() {
        if (fase == FasePartida.SALA && sesiones.todosListos()) {
            comenzarPartida();
        }
        for (JugadorServidor jugador : jugadores) {
            SesionJugador sesion = sesiones.get(jugador.getNumero());
            jugador.setEjes(sesion.getEjeX(), sesion.getEjeY());
        }
    }

    private void comenzarPartida() {
        fase = FasePartida.JUGANDO;
        sesiones.reiniciarListos();
        for (JugadorServidor jugador : jugadores) {
            jugador.ubicar(mundo, mapa.getAparicion(jugador.getNumero()));
        }
        Registro.info("Los dos jugadores están listos: comienza la partida");
    }

    private void volverALaSala() {
        fase = FasePartida.SALA;
        sesiones.reiniciarListos();
        for (JugadorServidor jugador : jugadores) {
            jugador.detener();
        }
        Registro.info("La partida se canceló: se vuelve a la sala de espera");
    }

    private void difundirEstado() {
        InstantaneaPartida instantanea = new InstantaneaPartida();
        instantanea.setSecuencia(++secuenciaEstado);
        instantanea.setFase(fase);
        for (SesionJugador sesion : sesiones.todas()) {
            instantanea.setJugador(sesion.getNumero(), sesion.estaConectado(), sesion.estaListo());
        }
        Mensaje mensaje = instantanea.aMensaje();
        for (SesionJugador sesion : sesiones.conectadas()) {
            red.enviar(mensaje, sesion.getDireccion());
        }
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
                    : "libre";
            Registro.info("Jugador " + sesion.getNumero() + ": " + detalle);
        }
        for (JugadorServidor jugador : jugadores) {
            if (jugador != null) {
                Registro.info("Cuerpo del jugador " + jugador.getNumero() + " en ("
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
