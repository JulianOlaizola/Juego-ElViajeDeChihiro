package juego.chihiro.servidor;

import java.net.SocketException;

import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;

import juego.chihiro.red.Protocolo;

public final class ServidorLauncher {

    private static final int SALIDA_PUERTO_OCUPADO = 1;
    private static final int SALIDA_ARGUMENTO_INVALIDO = 2;
    private static final int SALIDA_ERROR_DE_RED = 3;
    private static final String ARCHIVO_REGISTRO = "servidor.log";

    private ServidorLauncher() {
    }

    public static void main(String[] args) {
        Protocolo.preferirIPv4();
        int puerto;
        try {
            puerto = args.length > 0 ? Protocolo.leerPuerto(args[0]) : Protocolo.PUERTO_POR_DEFECTO;
        } catch (IllegalArgumentException e) {
            Registro.error(e.getMessage() + ". Uso: java -jar ElViajeDeChihiro-Servidor.jar [puerto]", null);
            System.exit(SALIDA_ARGUMENTO_INVALIDO);
            return;
        }

        Registro.abrirArchivo(ARCHIVO_REGISTRO);
        ServidorUdp red;
        try {
            red = new ServidorUdp(puerto);
        } catch (PuertoOcupadoException e) {
            Registro.error("El puerto " + e.getPuerto() + " ya está en uso. Cerrá el otro servidor o elegí otro puerto, por ejemplo: java -jar ElViajeDeChihiro-Servidor.jar " + (e.getPuerto() + 1), null);
            System.exit(SALIDA_PUERTO_OCUPADO);
            return;
        } catch (SocketException e) {
            Registro.error("No se pudo abrir el socket UDP del servidor", e);
            System.exit(SALIDA_ERROR_DE_RED);
            return;
        }

        red.iniciar();
        Runtime.getRuntime().addShutdownHook(new Thread(red::cerrar, "cierre-servidor"));

        ServidorJuego juego = new ServidorJuego(red);
        HeadlessApplicationConfiguration configuracion = new HeadlessApplicationConfiguration();
        configuracion.updatesPerSecond = ServidorJuego.TICKS_POR_SEGUNDO;
        new HeadlessApplication(juego, configuracion);

        Thread consola = new Thread(new ConsolaServidor(juego), "consola-servidor");
        consola.setDaemon(true);
        consola.start();
    }
}
