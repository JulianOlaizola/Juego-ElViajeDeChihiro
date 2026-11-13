package juego.chihiro.red;

import java.util.Arrays;

public final class Mensaje {

    private static final int CAMPOS_DE_CABECERA = 3;

    private final TipoMensaje tipo;
    private final String[] campos;

    public Mensaje(TipoMensaje tipo, String... campos) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de mensaje no puede ser nulo");
        }
        this.tipo = tipo;
        this.campos = new String[campos == null ? 0 : campos.length];
        for (int i = 0; i < this.campos.length; i++) {
            this.campos[i] = limpiar(campos[i]);
        }
    }

    public TipoMensaje tipo() {
        return tipo;
    }

    public int cantidadCampos() {
        return campos.length;
    }

    public String campo(int indice) throws MensajeInvalidoException {
        if (indice < 0 || indice >= campos.length) {
            throw new MensajeInvalidoException("Al mensaje " + tipo + " le falta el campo " + indice);
        }
        return campos[indice];
    }

    public int campoEntero(int indice) throws MensajeInvalidoException {
        String valor = campo(indice);
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            throw new MensajeInvalidoException("El campo " + indice + " de " + tipo + " no es un entero: " + valor, e);
        }
    }

    public long campoLargo(int indice) throws MensajeInvalidoException {
        String valor = campo(indice);
        try {
            return Long.parseLong(valor);
        } catch (NumberFormatException e) {
            throw new MensajeInvalidoException("El campo " + indice + " de " + tipo + " no es un número: " + valor, e);
        }
    }

    public byte[] aBytes() {
        StringBuilder texto = new StringBuilder();
        texto.append(Protocolo.IDENTIFICADOR).append(Protocolo.SEPARADOR);
        texto.append(Protocolo.VERSION).append(Protocolo.SEPARADOR);
        texto.append(tipo.name());
        for (String campo : campos) {
            texto.append(Protocolo.SEPARADOR).append(campo);
        }
        byte[] datos = texto.toString().getBytes(Protocolo.CODIFICACION);
        if (datos.length > Protocolo.TAMANIO_MAXIMO_PAQUETE) {
            throw new IllegalStateException("El mensaje " + tipo + " ocupa " + datos.length + " bytes y supera el máximo de " + Protocolo.TAMANIO_MAXIMO_PAQUETE);
        }
        return datos;
    }

    public static Mensaje desdeBytes(byte[] datos, int longitud) throws MensajeInvalidoException {
        if (datos == null || longitud <= 0) {
            throw new MensajeInvalidoException("Paquete vacío");
        }
        if (longitud > datos.length) {
            throw new MensajeInvalidoException("Longitud de paquete inválida: " + longitud);
        }
        String texto = new String(datos, 0, longitud, Protocolo.CODIFICACION);
        String[] partes = texto.split(Protocolo.SEPARADOR, -1);
        if (partes.length < CAMPOS_DE_CABECERA) {
            throw new MensajeInvalidoException("Paquete incompleto");
        }
        if (!Protocolo.IDENTIFICADOR.equals(partes[0])) {
            throw new MensajeInvalidoException("El paquete no pertenece al juego");
        }
        int version;
        try {
            version = Integer.parseInt(partes[1]);
        } catch (NumberFormatException e) {
            throw new MensajeInvalidoException("Versión de protocolo ilegible: " + partes[1], e);
        }
        if (version != Protocolo.VERSION) {
            throw new MensajeInvalidoException("Versión de protocolo incompatible: " + version + " (se esperaba " + Protocolo.VERSION + ")");
        }
        TipoMensaje tipo = TipoMensaje.desdeTexto(partes[2]);
        if (tipo == null) {
            throw new MensajeInvalidoException("Tipo de mensaje desconocido: " + partes[2]);
        }
        return new Mensaje(tipo, Arrays.copyOfRange(partes, CAMPOS_DE_CABECERA, partes.length));
    }

    private static String limpiar(String campo) {
        if (campo == null) {
            return "";
        }
        return campo.replace(Protocolo.SEPARADOR, ",").replace('\n', ' ').replace('\r', ' ');
    }

    @Override
    public String toString() {
        return tipo + Arrays.toString(campos);
    }
}
