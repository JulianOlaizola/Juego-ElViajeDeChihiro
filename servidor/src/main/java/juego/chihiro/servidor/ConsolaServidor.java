package juego.chihiro.servidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.badlogic.gdx.Gdx;

public class ConsolaServidor implements Runnable {

    private final ServidorJuego juego;

    public ConsolaServidor(ServidorJuego juego) {
        this.juego = juego;
    }

    @Override
    public void run() {
        BufferedReader lector = new BufferedReader(new InputStreamReader(System.in));
        try {
            String linea;
            while ((linea = lector.readLine()) != null) {
                if (!ejecutar(linea.trim().toLowerCase())) {
                    return;
                }
            }
        } catch (IOException e) {
            Registro.error("La consola del servidor dejó de leer comandos", e);
        }
    }

    private boolean ejecutar(String comando) {
        if (comando.isEmpty()) {
            return true;
        }
        switch (comando) {
            case "salir":
                Registro.info("Cerrando el servidor...");
                Gdx.app.postRunnable(() -> Gdx.app.exit());
                return false;
            case "estado":
                Gdx.app.postRunnable(juego::imprimirEstado);
                return true;
            case "ayuda":
                Registro.info("estado: muestra los paquetes recibidos y enviados | salir: cierra el servidor");
                return true;
            default:
                Registro.aviso("Comando desconocido: " + comando + ". Escribí 'ayuda'.");
                return true;
        }
    }
}
