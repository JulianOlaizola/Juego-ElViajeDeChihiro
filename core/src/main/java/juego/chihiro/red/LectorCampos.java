package juego.chihiro.red;

public class LectorCampos {

    private final Mensaje mensaje;
    private int indice;

    public LectorCampos(Mensaje mensaje) {
        this.mensaje = mensaje;
    }

    public String texto() throws MensajeInvalidoException {
        return mensaje.campo(indice++);
    }

    public int entero() throws MensajeInvalidoException {
        return mensaje.campoEntero(indice++);
    }

    public long largo() throws MensajeInvalidoException {
        return mensaje.campoLargo(indice++);
    }

    public float decimal() throws MensajeInvalidoException {
        String valor = texto();
        try {
            return Float.parseFloat(valor);
        } catch (NumberFormatException e) {
            throw new MensajeInvalidoException("Número decimal inválido: " + valor, e);
        }
    }

    public boolean bandera() throws MensajeInvalidoException {
        String valor = texto();
        if ("1".equals(valor)) {
            return true;
        }
        if ("0".equals(valor)) {
            return false;
        }
        throw new MensajeInvalidoException("Se esperaba 0 o 1 y llegó: " + valor);
    }
}
