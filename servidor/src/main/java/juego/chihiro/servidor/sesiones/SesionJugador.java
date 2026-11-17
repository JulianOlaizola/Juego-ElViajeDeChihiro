package juego.chihiro.servidor.sesiones;

import java.net.InetSocketAddress;

public class SesionJugador {

    private final int numero;
    private final CanalConfiable canal = new CanalConfiable();
    private String token;
    private String nombre = "";
    private InetSocketAddress direccion;
    private boolean conectado;
    private boolean listo;
    private long ultimaActividadMilis;
    private long ultimaSecuenciaEntrada;
    private float ejeX;
    private float ejeY;
    private int contadorInteraccion;
    private int contadorPausa;

    public SesionJugador(int numero) {
        this.numero = numero;
    }

    public void ocupar(String token, String nombre, InetSocketAddress direccion, long ahora) {
        this.token = token;
        this.nombre = nombre;
        this.direccion = direccion;
        this.conectado = true;
        this.listo = false;
        this.ultimaActividadMilis = ahora;
        this.ultimaSecuenciaEntrada = 0;
        this.ejeX = 0f;
        this.ejeY = 0f;
        this.contadorInteraccion = 0;
        this.contadorPausa = 0;
        this.canal.reiniciar();
    }

    public void liberar() {
        token = null;
        nombre = "";
        direccion = null;
        conectado = false;
        listo = false;
        ejeX = 0f;
        ejeY = 0f;
        canal.reiniciar();
    }

    public boolean registrarEntrada(long secuencia, float x, float y, int interacciones, int pausas,
                                    boolean estaListo, InetSocketAddress origen, long ahora) {
        if (secuencia <= ultimaSecuenciaEntrada) {
            return false;
        }
        ultimaSecuenciaEntrada = secuencia;
        ejeX = x;
        ejeY = y;
        contadorInteraccion = interacciones;
        contadorPausa = pausas;
        listo = estaListo;
        direccion = origen;
        ultimaActividadMilis = ahora;
        return true;
    }

    public void tocar(InetSocketAddress origen, long ahora) {
        direccion = origen;
        ultimaActividadMilis = ahora;
    }

    public boolean estaLibre() {
        return token == null;
    }

    public CanalConfiable getCanal() {
        return canal;
    }

    public int getNumero() {
        return numero;
    }

    public String getToken() {
        return token;
    }

    public String getNombre() {
        return nombre;
    }

    public InetSocketAddress getDireccion() {
        return direccion;
    }

    public boolean estaConectado() {
        return conectado;
    }

    public void setConectado(boolean conectado) {
        this.conectado = conectado;
    }

    public boolean estaListo() {
        return listo;
    }

    public void setListo(boolean listo) {
        this.listo = listo;
    }

    public long getUltimaActividadMilis() {
        return ultimaActividadMilis;
    }

    public float getEjeX() {
        return ejeX;
    }

    public float getEjeY() {
        return ejeY;
    }

    public int getContadorInteraccion() {
        return contadorInteraccion;
    }

    public int getContadorPausa() {
        return contadorPausa;
    }
}
