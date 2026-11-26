package juego.chihiro.red;

import java.util.ArrayList;
import java.util.List;

import juego.chihiro.entidades.Direccion;
import juego.chihiro.mundo.EstadoTina;
import juego.chihiro.mundo.Nivel;
import juego.chihiro.mundo.ObjetoEnMano;
import juego.chihiro.mundo.TipoEspiritu;
import juego.chihiro.utiles.Constantes;

public class InstantaneaPartida {

    private static final int MAXIMO_TINAS = 16;

    private long secuencia;
    private FasePartida fase = FasePartida.SALA;
    private Nivel nivel = Nivel.SALON;
    private int puntajeAcumulado;
    private float tiempoRestante = Nivel.SALON.getDuracion();
    private int pedidosCompletados;
    private int pedidosFallidos;
    private int puntaje;
    private boolean calderaEncendida;
    private boolean aguaCaliente;
    private float proporcionCaldera;
    private boolean gano;
    private int segundosParaReconexion;
    private final DatosJugador[] jugadores = new DatosJugador[Constantes.CANTIDAD_JUGADORES];
    private final List<DatosTina> tinas = new ArrayList<DatosTina>();

    public InstantaneaPartida() {
        for (int i = 0; i < jugadores.length; i++) {
            jugadores[i] = new DatosJugador();
        }
    }

    public Mensaje aMensaje() {
        List<String> campos = new ArrayList<String>();
        campos.add(String.valueOf(secuencia));
        campos.add(fase.name());
        campos.add(String.valueOf(nivel.ordinal()));
        campos.add(String.valueOf(puntajeAcumulado));
        campos.add(decimal(tiempoRestante));
        campos.add(String.valueOf(pedidosCompletados));
        campos.add(String.valueOf(pedidosFallidos));
        campos.add(String.valueOf(puntaje));
        campos.add(bandera(calderaEncendida));
        campos.add(bandera(aguaCaliente));
        campos.add(decimal(proporcionCaldera));
        campos.add(bandera(gano));
        campos.add(String.valueOf(segundosParaReconexion));
        for (DatosJugador jugador : jugadores) {
            campos.add(bandera(jugador.conectado));
            campos.add(bandera(jugador.listo));
            campos.add(decimal(jugador.x));
            campos.add(decimal(jugador.y));
            campos.add(String.valueOf(jugador.direccion.ordinal()));
            campos.add(bandera(jugador.enMovimiento));
            campos.add(String.valueOf(jugador.objeto.ordinal()));
        }
        campos.add(String.valueOf(tinas.size()));
        for (DatosTina tina : tinas) {
            campos.add(String.valueOf(tina.estado.ordinal()));
            campos.add(String.valueOf(tina.tipo.ordinal()));
            campos.add(bandera(tina.faltaSal));
            campos.add(bandera(tina.faltaAgua));
            campos.add(bandera(tina.faltaHierbas));
            campos.add(decimal(tina.proporcion));
        }
        return new Mensaje(TipoMensaje.ESTADO, campos.toArray(new String[0]));
    }

    public static InstantaneaPartida desdeMensaje(Mensaje mensaje) throws MensajeInvalidoException {
        InstantaneaPartida instantanea = new InstantaneaPartida();
        LectorCampos lector = new LectorCampos(mensaje);
        instantanea.secuencia = lector.largo();
        FasePartida fase = FasePartida.desdeTexto(lector.texto());
        if (fase == null) {
            throw new MensajeInvalidoException("Fase de partida desconocida");
        }
        instantanea.fase = fase;
        instantanea.nivel = elemento(Nivel.values(), lector.entero());
        instantanea.puntajeAcumulado = lector.entero();
        instantanea.tiempoRestante = lector.decimal();
        instantanea.pedidosCompletados = lector.entero();
        instantanea.pedidosFallidos = lector.entero();
        instantanea.puntaje = lector.entero();
        instantanea.calderaEncendida = lector.bandera();
        instantanea.aguaCaliente = lector.bandera();
        instantanea.proporcionCaldera = lector.decimal();
        instantanea.gano = lector.bandera();
        instantanea.segundosParaReconexion = lector.entero();
        for (DatosJugador jugador : instantanea.jugadores) {
            jugador.conectado = lector.bandera();
            jugador.listo = lector.bandera();
            jugador.x = lector.decimal();
            jugador.y = lector.decimal();
            jugador.direccion = elemento(Direccion.values(), lector.entero());
            jugador.enMovimiento = lector.bandera();
            jugador.objeto = elemento(ObjetoEnMano.values(), lector.entero());
        }
        int cantidadTinas = lector.entero();
        if (cantidadTinas < 0 || cantidadTinas > MAXIMO_TINAS) {
            throw new MensajeInvalidoException("Cantidad de tinas inválida: " + cantidadTinas);
        }
        for (int i = 0; i < cantidadTinas; i++) {
            DatosTina tina = new DatosTina();
            tina.estado = elemento(EstadoTina.values(), lector.entero());
            tina.tipo = elemento(TipoEspiritu.values(), lector.entero());
            tina.faltaSal = lector.bandera();
            tina.faltaAgua = lector.bandera();
            tina.faltaHierbas = lector.bandera();
            tina.proporcion = lector.decimal();
            instantanea.tinas.add(tina);
        }
        return instantanea;
    }

    private static <T> T elemento(T[] valores, int indice) throws MensajeInvalidoException {
        if (indice < 0 || indice >= valores.length) {
            throw new MensajeInvalidoException("Índice fuera de rango: " + indice);
        }
        return valores[indice];
    }

    static String bandera(boolean valor) {
        return valor ? "1" : "0";
    }

    private static String decimal(float valor) {
        return String.valueOf(Math.round(valor * 100f) / 100f);
    }

    public long getSecuencia() {
        return secuencia;
    }

    public void setSecuencia(long secuencia) {
        this.secuencia = secuencia;
    }

    public FasePartida getFase() {
        return fase;
    }

    public void setFase(FasePartida fase) {
        this.fase = fase;
    }

    public Nivel getNivel() {
        return nivel;
    }

    public int getPuntajeAcumulado() {
        return puntajeAcumulado;
    }

    public int getPuntajeTotal() {
        return puntajeAcumulado + puntaje;
    }

    public void setNivel(Nivel nivel, int puntajeAcumulado) {
        this.nivel = nivel;
        this.puntajeAcumulado = puntajeAcumulado;
    }

    public float getTiempoRestante() {
        return tiempoRestante;
    }

    public int getPedidosCompletados() {
        return pedidosCompletados;
    }

    public int getPedidosFallidos() {
        return pedidosFallidos;
    }

    public int getPuntaje() {
        return puntaje;
    }

    public boolean estaLaCalderaEncendida() {
        return calderaEncendida;
    }

    public boolean estaElAguaCaliente() {
        return aguaCaliente;
    }

    public float getProporcionCaldera() {
        return proporcionCaldera;
    }

    public boolean gano() {
        return gano;
    }

    public void setPartida(float tiempoRestante, int pedidosCompletados, int pedidosFallidos, int puntaje, boolean gano) {
        this.tiempoRestante = tiempoRestante;
        this.pedidosCompletados = pedidosCompletados;
        this.pedidosFallidos = pedidosFallidos;
        this.puntaje = puntaje;
        this.gano = gano;
    }

    public int getSegundosParaReconexion() {
        return segundosParaReconexion;
    }

    public void setSegundosParaReconexion(int segundosParaReconexion) {
        this.segundosParaReconexion = segundosParaReconexion;
    }

    public void setCaldera(boolean encendida, boolean caliente, float proporcion) {
        this.calderaEncendida = encendida;
        this.aguaCaliente = caliente;
        this.proporcionCaldera = proporcion;
    }

    public boolean estaConectado(int numeroJugador) {
        return jugadores[numeroJugador - 1].conectado;
    }

    public boolean estaListo(int numeroJugador) {
        return jugadores[numeroJugador - 1].listo;
    }

    public void setJugador(int numeroJugador, boolean conectado, boolean listo) {
        jugadores[numeroJugador - 1].conectado = conectado;
        jugadores[numeroJugador - 1].listo = listo;
    }

    public void setCuerpo(int numeroJugador, float x, float y, Direccion direccion, boolean enMovimiento, ObjetoEnMano objeto) {
        DatosJugador jugador = jugadores[numeroJugador - 1];
        jugador.x = x;
        jugador.y = y;
        jugador.direccion = direccion;
        jugador.enMovimiento = enMovimiento;
        jugador.objeto = objeto;
    }

    public DatosJugador getJugador(int numeroJugador) {
        return jugadores[numeroJugador - 1];
    }

    public void agregarTina(EstadoTina estado, TipoEspiritu tipo, boolean faltaSal, boolean faltaAgua,
                            boolean faltaHierbas, float proporcion) {
        DatosTina tina = new DatosTina();
        tina.estado = estado;
        tina.tipo = tipo;
        tina.faltaSal = faltaSal;
        tina.faltaAgua = faltaAgua;
        tina.faltaHierbas = faltaHierbas;
        tina.proporcion = proporcion;
        tinas.add(tina);
    }

    public List<DatosTina> getTinas() {
        return tinas;
    }

    public static class DatosJugador {
        private boolean conectado;
        private boolean listo;
        private float x;
        private float y;
        private Direccion direccion = Direccion.ABAJO;
        private boolean enMovimiento;
        private ObjetoEnMano objeto = ObjetoEnMano.NADA;

        public boolean estaConectado() {
            return conectado;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public Direccion getDireccion() {
            return direccion;
        }

        public boolean estaEnMovimiento() {
            return enMovimiento;
        }

        public ObjetoEnMano getObjeto() {
            return objeto;
        }
    }

    public static class DatosTina {
        private EstadoTina estado = EstadoTina.LIBRE;
        private TipoEspiritu tipo = TipoEspiritu.COMUN;
        private boolean faltaSal;
        private boolean faltaAgua;
        private boolean faltaHierbas;
        private float proporcion;

        public EstadoTina getEstado() {
            return estado;
        }

        public TipoEspiritu getTipo() {
            return tipo;
        }

        public boolean faltaHierbas() {
            return faltaHierbas;
        }

        public boolean faltaSal() {
            return faltaSal;
        }

        public boolean faltaAgua() {
            return faltaAgua;
        }

        public float getProporcion() {
            return proporcion;
        }
    }
}
