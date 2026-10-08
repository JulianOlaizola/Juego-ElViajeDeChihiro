package juego.chihiro.pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import juego.chihiro.Main;
import juego.chihiro.entradas.Accion;
import juego.chihiro.entradas.ControladorEntradas;
import juego.chihiro.utiles.Constantes;
import juego.chihiro.utiles.Recursos;

public class PantallaMenu extends ScreenAdapter {
    private static final String[] OPCIONES = { "Jugar", "Instrucciones", "Salir" };

    private final Main juego;
    private final Recursos recursos;
    private final ControladorEntradas entradas;
    private final OrthographicCamera camara = new OrthographicCamera();
    private final Viewport vista;

    private int seleccion;

    public PantallaMenu(Main juego) {
        this.juego = juego;
        this.recursos = juego.getRecursos();
        this.entradas = juego.getEntradas();
        this.vista = new FitViewport(Constantes.ANCHO_VISTA, Constantes.ALTO_VISTA, camara);
    }

    @Override
    public void render(float delta) {
        if (!actualizar()) {
            return;
        }
        dibujar();
    }

    private boolean actualizar() {
        juego.atenderControlesDeAudio();

        if (entradas.consumir(Accion.SUBIR)) {
            seleccion = (seleccion + OPCIONES.length - 1) % OPCIONES.length;
        }
        if (entradas.consumir(Accion.BAJAR)) {
            seleccion = (seleccion + 1) % OPCIONES.length;
        }

        if (entradas.consumir(Accion.CONFIRMAR)) {
            if (seleccion == 0) {
                juego.cambiarPantalla(new PantallaJuego(juego));
                return false;
            }
            if (seleccion == 1) {
                juego.cambiarPantalla(new PantallaInstrucciones(juego));
                return false;
            }
            Gdx.app.exit();
            return false;
        }

        entradas.limpiarAcciones();
        return true;
    }

    private void dibujar() {
        ScreenUtils.clear(0.07f, 0.05f, 0.09f, 1f);
        vista.apply();
        camara.update();

        SpriteBatch batch = juego.getBatch();
        batch.setProjectionMatrix(camara.combined);
        batch.begin();

        float anchoLogo = 480f;
        float altoLogo = anchoLogo * recursos.getLogo().getRegionHeight()
            / recursos.getLogo().getRegionWidth();
        batch.draw(recursos.getLogo(), (Constantes.ANCHO_VISTA - anchoLogo) / 2f,
                   Constantes.ALTO_VISTA - altoLogo - 46f, anchoLogo, altoLogo);

        for (int i = 0; i < OPCIONES.length; i++) {
            boolean elegida = i == seleccion;
            recursos.getFuenteMedia().setColor(elegida ? Color.GOLD : Color.WHITE);
            recursos.getFuenteMedia().draw(batch,
                (elegida ? "> " : "   ") + OPCIONES[i],
                0f, 252f - i * 46f, Constantes.ANCHO_VISTA, Align.center, false);
        }
        recursos.getFuenteMedia().setColor(Color.WHITE);

        recursos.getFuenteChica().setColor(Color.LIGHT_GRAY);
        recursos.getFuenteChica().draw(batch,
            "W/S o flechas para elegir   -   ENTER para confirmar",
            0f, 74f, Constantes.ANCHO_VISTA, Align.center, false);
        recursos.getFuenteChica().draw(batch,
            "M silencia   -   +/- volumen   -   " + juego.getAudio().describirVolumen(),
            0f, 48f, Constantes.ANCHO_VISTA, Align.center, false);
        recursos.getFuenteChica().setColor(Color.WHITE);

        batch.end();
    }

    @Override
    public void resize(int ancho, int alto) {
        vista.update(ancho, alto, true);
    }
}
