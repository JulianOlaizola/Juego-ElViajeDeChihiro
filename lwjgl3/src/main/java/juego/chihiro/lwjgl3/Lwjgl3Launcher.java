package juego.chihiro.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import juego.chihiro.Main;
import juego.chihiro.utiles.Constantes;

public class Lwjgl3Launcher {
    private static final String DIRECCION_POR_DEFECTO = "127.0.0.1";

    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) {
            return;
        }
        String direccionInicial = args.length > 0 ? args[0] : DIRECCION_POR_DEFECTO;
        createApplication(direccionInicial);
    }

    private static Lwjgl3Application createApplication(String direccionInicial) {
        return new Lwjgl3Application(new Main(direccionInicial), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle(Constantes.TITULO);
        configuration.useVsync(true);
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);
        configuration.setWindowedMode((int) Constantes.ANCHO_VISTA, (int) Constantes.ALTO_VISTA);
        configuration.setWindowSizeLimits(640, 360, -1, -1);
        configuration.setPauseWhenLostFocus(false);
        configuration.setWindowIcon("imagenes/icono128.png", "imagenes/icono64.png",
            "imagenes/icono32.png", "imagenes/icono16.png");
        return configuration;
    }
}
