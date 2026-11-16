package juego.chihiro.red;

import java.util.ArrayList;
import java.util.List;

import juego.chihiro.utiles.Constantes;

public class InstantaneaPartida {

    private long secuencia;
    private FasePartida fase = FasePartida.SALA;
    private final boolean[] conectados = new boolean[Constantes.CANTIDAD_JUGADORES];
    private final boolean[] listos = new boolean[Constantes.CANTIDAD_JUGADORES];

    public Mensaje aMensaje() {
        List<String> campos = new ArrayList<String>();
        campos.add(String.valueOf(secuencia));
        campos.add(fase.name());
        for (int i = 0; i < Constantes.CANTIDAD_JUGADORES; i++) {
            campos.add(bandera(conectados[i]));
            campos.add(bandera(listos[i]));
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
        for (int i = 0; i < Constantes.CANTIDAD_JUGADORES; i++) {
            instantanea.conectados[i] = lector.bandera();
            instantanea.listos[i] = lector.bandera();
        }
        return instantanea;
    }

    static String bandera(boolean valor) {
        return valor ? "1" : "0";
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

    public boolean estaConectado(int numeroJugador) {
        return conectados[numeroJugador - 1];
    }

    public boolean estaListo(int numeroJugador) {
        return listos[numeroJugador - 1];
    }

    public void setJugador(int numeroJugador, boolean conectado, boolean listo) {
        conectados[numeroJugador - 1] = conectado;
        listos[numeroJugador - 1] = listo;
    }
}
