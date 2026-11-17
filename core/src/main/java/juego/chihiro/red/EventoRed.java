package juego.chihiro.red;

import java.util.Arrays;

public final class EventoRed {

    private final long id;
    private final TipoEvento tipo;
    private final String[] datos;

    public EventoRed(long id, TipoEvento tipo, String... datos) {
        this.id = id;
        this.tipo = tipo;
        this.datos = datos == null ? new String[0] : datos.clone();
    }

    public Mensaje aMensaje() {
        String[] campos = new String[datos.length + 2];
        campos[0] = String.valueOf(id);
        campos[1] = tipo.name();
        System.arraycopy(datos, 0, campos, 2, datos.length);
        return new Mensaje(TipoMensaje.EVENTO, campos);
    }

    public static EventoRed desdeMensaje(Mensaje mensaje) throws MensajeInvalidoException {
        long id = mensaje.campoLargo(0);
        TipoEvento tipo = TipoEvento.desdeTexto(mensaje.campo(1));
        if (tipo == null) {
            throw new MensajeInvalidoException("Tipo de evento desconocido: " + mensaje.campo(1));
        }
        String[] datos = new String[mensaje.cantidadCampos() - 2];
        for (int i = 0; i < datos.length; i++) {
            datos[i] = mensaje.campo(i + 2);
        }
        return new EventoRed(id, tipo, datos);
    }

    public long getId() {
        return id;
    }

    public TipoEvento getTipo() {
        return tipo;
    }

    public String dato(int indice) {
        return indice < datos.length ? datos[indice] : "";
    }

    @Override
    public String toString() {
        return "#" + id + " " + tipo + Arrays.toString(datos);
    }
}
