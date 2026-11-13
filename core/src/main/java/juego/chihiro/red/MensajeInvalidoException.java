package juego.chihiro.red;

public class MensajeInvalidoException extends Exception {

    private static final long serialVersionUID = 1L;

    public MensajeInvalidoException(String motivo) {
        super(motivo);
    }

    public MensajeInvalidoException(String motivo, Throwable causa) {
        super(motivo, causa);
    }
}
