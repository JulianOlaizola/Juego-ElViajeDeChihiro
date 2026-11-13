package juego.chihiro.servidor;

import java.io.PrintStream;
import java.text.Normalizer;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class Registro {

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private Registro() {
    }

    public static void info(String mensaje) {
        imprimir(System.out, "INFO", mensaje);
    }

    public static void aviso(String mensaje) {
        imprimir(System.out, "AVISO", mensaje);
    }

    public static void error(String mensaje, Throwable causa) {
        if (causa == null) {
            imprimir(System.err, "ERROR", mensaje);
        } else {
            imprimir(System.err, "ERROR", mensaje + " -> " + causa.getClass().getSimpleName() + ": " + causa.getMessage());
        }
    }

    private static synchronized void imprimir(PrintStream salida, String nivel, String mensaje) {
        salida.println("[" + LocalTime.now().format(FORMATO_HORA) + "] [" + nivel + "] " + sinTildes(mensaje));
        salida.flush();
    }

    private static String sinTildes(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }
}
