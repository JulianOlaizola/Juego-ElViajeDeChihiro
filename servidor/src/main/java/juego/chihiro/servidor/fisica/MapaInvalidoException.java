package juego.chihiro.servidor.fisica;

public class MapaInvalidoException extends Exception {

    private static final long serialVersionUID = 1L;

    public MapaInvalidoException(String motivo) {
        super(motivo);
    }

    public MapaInvalidoException(String motivo, Throwable causa) {
        super(motivo, causa);
    }
}
