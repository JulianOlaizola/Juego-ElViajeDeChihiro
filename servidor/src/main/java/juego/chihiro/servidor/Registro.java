package juego.chihiro.servidor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class Registro {

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static PrintWriter archivo;

    private Registro() {
    }

    public static synchronized void abrirArchivo(String ruta) {
        try {
            archivo = new PrintWriter(new OutputStreamWriter(new FileOutputStream(ruta, true), StandardCharsets.UTF_8), true);
            archivo.println();
            info("Registro guardándose también en " + ruta);
        } catch (IOException e) {
            archivo = null;
            aviso("No se pudo abrir el archivo de registro " + ruta + ": " + e.getMessage());
        }
    }

    public static synchronized void cerrarArchivo() {
        if (archivo != null) {
            archivo.close();
            archivo = null;
        }
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
        String linea = "[" + LocalTime.now().format(FORMATO_HORA) + "] [" + nivel + "] " + mensaje;
        salida.println(sinTildes(linea));
        salida.flush();
        if (archivo != null) {
            archivo.println(linea);
        }
    }

    private static String sinTildes(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }
}
