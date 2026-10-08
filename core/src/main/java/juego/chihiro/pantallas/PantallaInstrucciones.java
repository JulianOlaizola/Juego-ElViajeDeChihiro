package juego.chihiro.pantallas;

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

public class PantallaInstrucciones extends ScreenAdapter {
    private static final String[] LINEAS = {
        "Objetivo: atender " + Constantes.PEDIDOS_PARA_GANAR + " pedidos antes de que se acabe el turno.",
        "Con " + Constantes.FALLOS_PARA_PERDER + " espíritus impacientes, la casa de baños cierra.",
        "",
        "Cada espíritu pide sales y un balde de agua caliente.",
        "1. Encendé la caldera y esperá a que el agua se caliente.",
        "2. Juntá sales en el almacén y llená el balde en la caldera.",
        "3. Entregá las dos cosas en la tina que tiene el pedido.",
        "4. La tina queda sucia: limpiala y tirá la suciedad en el depósito.",
        "El depósito sirve para tirar cualquier cosa que lleves en la mano.",
        "",
        "Jugador 1:  W A S D para moverse  -  E para interactuar",
        "Jugador 2:  flechas para moverse  -  SHIFT derecho para interactuar",
        "",
        "P o ESC: pausa   -   M: silencio   -   + / -: volumen   -   F1: ver colisiones"
    };

    private final Main juego;
    private final Recursos recursos;
    private final ControladorEntradas entradas;
    private final OrthographicCamera camara = new OrthographicCamera();
    private final Viewport vista;

    public PantallaInstrucciones(Main juego) {
        this.juego = juego;
        this.recursos = juego.getRecursos();
        this.entradas = juego.getEntradas();
        this.vista = new FitViewport(Constantes.ANCHO_VISTA, Constantes.ALTO_VISTA, camara);
    }

    @Override
    public void render(float delta) {
        juego.atenderControlesDeAudio();
        if (entradas.consumir(Accion.VOLVER) || entradas.consumir(Accion.CONFIRMAR)) {
            juego.cambiarPantalla(new PantallaMenu(juego));
            return;
        }
        entradas.limpiarAcciones();

        ScreenUtils.clear(0.07f, 0.05f, 0.09f, 1f);
        vista.apply();
        camara.update();

        SpriteBatch batch = juego.getBatch();
        batch.setProjectionMatrix(camara.combined);
        batch.begin();

        recursos.getFuenteGrande().setColor(Color.GOLD);
        recursos.getFuenteGrande().draw(batch, "Instrucciones", 0f, Constantes.ALTO_VISTA - 40f,
                                        Constantes.ANCHO_VISTA, Align.center, false);
        recursos.getFuenteGrande().setColor(Color.WHITE);

        recursos.getFuenteChica().setColor(Color.WHITE);
        float y = Constantes.ALTO_VISTA - 110f;
        for (String linea : LINEAS) {
            recursos.getFuenteChica().draw(batch, linea, 0f, y,
                                           Constantes.ANCHO_VISTA, Align.center, false);
            y -= 26f;
        }

        recursos.getFuenteChica().setColor(Color.LIGHT_GRAY);
        recursos.getFuenteChica().draw(batch, "ESC o ENTER para volver al menú",
                                       0f, 48f, Constantes.ANCHO_VISTA, Align.center, false);
        recursos.getFuenteChica().setColor(Color.WHITE);

        batch.end();
    }

    @Override
    public void resize(int ancho, int alto) {
        vista.update(ancho, alto, true);
    }
}
