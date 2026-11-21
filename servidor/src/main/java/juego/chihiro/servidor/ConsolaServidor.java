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
            case "ip":
                Gdx.app.postRunnable(juego::mostrarDirecciones);
                return true;
            case "terminar":
                Gdx.app.postRunnable(juego::terminarPartida);
                return true;
            case "ayuda":
                Registro.info("estado: muestra jugadores, partida y paquetes | ip: muestra las IP de esta PC"
                        + " | terminar: corta la partida actual"
                        + " para probar la revancha | salir: cierra el servidor avisando a los jugadores");
                return true;
            default:
                Registro.aviso("Comando desconocido: " + comando + ". Escribí 'ayuda'.");
                return true;
        }
    }
}
