package juego.chihiro.red;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class Protocolo {

    public static final String IDENTIFICADOR = "CHIHIRO";
    public static final int VERSION = 1;
    public static final int PUERTO_POR_DEFECTO = 7777;
    public static final int PUERTO_MINIMO = 1024;
    public static final int PUERTO_MAXIMO = 65535;
    public static final int TAMANIO_MAXIMO_PAQUETE = 4096;
    public static final String SEPARADOR = ";";
    public static final Charset CODIFICACION = StandardCharsets.UTF_8;

    private Protocolo() {
    }

    public static int validarPuerto(int puerto) {
        if (puerto < PUERTO_MINIMO || puerto > PUERTO_MAXIMO) {
            throw new IllegalArgumentException("El puerto debe estar entre " + PUERTO_MINIMO + " y " + PUERTO_MAXIMO + ": " + puerto);
        }
        return puerto;
    }

    public static int leerPuerto(String texto) {
        try {
            return validarPuerto(Integer.parseInt(texto.trim()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El puerto tiene que ser un número: " + texto);
        }
    }
}
