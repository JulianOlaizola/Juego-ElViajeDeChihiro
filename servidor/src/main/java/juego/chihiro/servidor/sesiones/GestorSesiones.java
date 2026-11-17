package juego.chihiro.servidor.sesiones;

import java.net.InetSocketAddress;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import juego.chihiro.red.Protocolo;
import juego.chihiro.utiles.Constantes;

public class GestorSesiones {

    private static final char[] SIMBOLOS = "0123456789abcdef".toCharArray();
    private static final int LARGO_TOKEN = 12;

    private final SesionJugador[] sesiones = new SesionJugador[Constantes.CANTIDAD_JUGADORES];
    private final SecureRandom aleatorio = new SecureRandom();

    public GestorSesiones() {
        for (int i = 0; i < sesiones.length; i++) {
            sesiones[i] = new SesionJugador(i + 1);
        }
    }

    public SesionJugador conectar(String nombre, String tokenPrevio, InetSocketAddress origen, long ahora) {
        SesionJugador existente = buscarPorToken(tokenPrevio);
        if (existente != null) {
            existente.tocar(origen, ahora);
            existente.setConectado(true);
            return existente;
        }
        SesionJugador mismaDireccion = buscarPorDireccion(origen);
        if (mismaDireccion != null) {
            mismaDireccion.tocar(origen, ahora);
            return mismaDireccion;
        }
        for (SesionJugador sesion : sesiones) {
            if (sesion.estaLibre()) {
                sesion.ocupar(nuevoToken(), nombre, origen, ahora);
                return sesion;
            }
        }
        return null;
    }

    public SesionJugador buscarPorToken(String token) {
        if (token == null || Protocolo.SIN_VALOR.equals(token)) {
            return null;
        }
        for (SesionJugador sesion : sesiones) {
            if (token.equals(sesion.getToken())) {
                return sesion;
            }
        }
        return null;
    }

    private SesionJugador buscarPorDireccion(InetSocketAddress origen) {
        for (SesionJugador sesion : sesiones) {
            if (!sesion.estaLibre() && origen.equals(sesion.getDireccion())) {
                return sesion;
            }
        }
        return null;
    }

    public List<SesionJugador> vencidas(long ahora) {
        List<SesionJugador> vencidas = new ArrayList<SesionJugador>();
        for (SesionJugador sesion : sesiones) {
            if (sesion.estaConectado() && ahora - sesion.getUltimaActividadMilis() > Protocolo.TIEMPO_ESPERA_MILIS) {
                vencidas.add(sesion);
            }
        }
        return vencidas;
    }

    public List<SesionJugador> conectadas() {
        List<SesionJugador> conectadas = new ArrayList<SesionJugador>();
        for (SesionJugador sesion : sesiones) {
            if (sesion.estaConectado()) {
                conectadas.add(sesion);
            }
        }
        return conectadas;
    }

    public int cantidadConectados() {
        return conectadas().size();
    }

    public boolean todosListos() {
        for (SesionJugador sesion : sesiones) {
            if (!sesion.estaConectado() || !sesion.estaListo()) {
                return false;
            }
        }
        return true;
    }

    public void reiniciarListos() {
        for (SesionJugador sesion : sesiones) {
            sesion.setListo(false);
        }
    }

    public SesionJugador get(int numero) {
        return sesiones[numero - 1];
    }

    public SesionJugador[] todas() {
        return sesiones;
    }

    private String nuevoToken() {
        StringBuilder token = new StringBuilder();
        for (int i = 0; i < LARGO_TOKEN; i++) {
            token.append(SIMBOLOS[aleatorio.nextInt(SIMBOLOS.length)]);
        }
        return token.toString();
    }
}
