package juego.chihiro.servidor.logica;

import juego.chihiro.red.Efecto;

public class Aviso {

    private final int jugador;
    private final String texto;
    private final Efecto efecto;

    public Aviso(int jugador, String texto, Efecto efecto) {
        this.jugador = jugador;
        this.texto = texto;
        this.efecto = efecto;
    }

    public int getJugador() {
        return jugador;
    }

    public String getTexto() {
        return texto;
    }

    public Efecto getEfecto() {
        return efecto;
    }
}
