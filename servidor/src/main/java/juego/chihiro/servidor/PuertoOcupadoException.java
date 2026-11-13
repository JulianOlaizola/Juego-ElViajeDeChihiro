package juego.chihiro.servidor;

public class PuertoOcupadoException extends Exception {

    private static final long serialVersionUID = 1L;

    private final int puerto;

    public PuertoOcupadoException(int puerto, Throwable causa) {
        super("El puerto " + puerto + " ya está en uso", causa);
        this.puerto = puerto;
    }

    public int getPuerto() {
        return puerto;
    }
}
