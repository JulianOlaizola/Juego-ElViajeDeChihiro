package juego.chihiro.pantallas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import juego.chihiro.Main;
import juego.chihiro.entradas.Accion;

public class PantallaMensaje extends PantallaBase {
    private final String encabezado;
    private final String detalle;

    public PantallaMensaje(Main juego, String encabezado, String detalle) {
        super(juego);
        this.encabezado = encabezado;
        this.detalle = detalle == null ? "" : detalle;
    }

    @Override
    public void show() {
        juego.cerrarSesion();
    }

    @Override
    public void render(float delta) {
        juego.atenderControlesDeAudio();
        if (entradas.consumir(Accion.CONFIRMAR) || entradas.consumir(Accion.VOLVER)) {
            juego.cambiarPantalla(new PantallaMenu(juego));
            return;
        }
        entradas.limpiarAcciones();
        SpriteBatch batch = comenzarDibujo();
        titulo(batch, encabezado);
        linea(batch, recursos.getFuenteMedia(), detalle, 300f, Color.WHITE);
        pie(batch, "ENTER para volver al menú");
        batch.end();
    }
}
