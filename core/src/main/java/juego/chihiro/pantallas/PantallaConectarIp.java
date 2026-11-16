package juego.chihiro.pantallas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import juego.chihiro.Main;
import juego.chihiro.entradas.Accion;
import juego.chihiro.red.DireccionServidor;
import juego.chihiro.red.Protocolo;

public class PantallaConectarIp extends PantallaBase {
    private static final float PARPADEO = 0.5f;

    private final String textoInicial;
    private String error = "";
    private float tiempo;

    public PantallaConectarIp(Main juego, String textoInicial) {
        super(juego);
        this.textoInicial = textoInicial;
    }

    @Override
    public void show() {
        entradas.iniciarCapturaTexto(textoInicial);
    }

    @Override
    public void render(float delta) {
        tiempo += delta;
        if (entradas.consumir(Accion.VOLVER)) {
            juego.cambiarPantalla(new PantallaMenu(juego));
            return;
        }
        if (entradas.consumir(Accion.CONFIRMAR)) {
            try {
                DireccionServidor direccion = DireccionServidor.parsear(entradas.getTexto());
                juego.setUltimaDireccion(entradas.getTexto());
                juego.cambiarPantalla(new PantallaConexion(juego, direccion));
                return;
            } catch (IllegalArgumentException e) {
                error = e.getMessage();
            }
        }
        entradas.limpiarAcciones();
        dibujar();
    }

    private void dibujar() {
        SpriteBatch batch = comenzarDibujo();
        titulo(batch, "Conectar por IP");
        linea(batch, recursos.getFuenteChica(),
            "Escribí la IP que muestra la consola del servidor. Si usa otro puerto: 192.168.0.10:" + (Protocolo.PUERTO_POR_DEFECTO + 1),
            380f, Color.LIGHT_GRAY);
        String cursor = ((int) (tiempo / PARPADEO)) % 2 == 0 ? "_" : " ";
        linea(batch, recursos.getFuenteGrande(), entradas.getTexto() + cursor, 300f, Color.WHITE);
        if (!error.isEmpty()) {
            linea(batch, recursos.getFuenteMedia(), error, 220f, Color.RED);
        }
        pie(batch, "ENTER para conectarse   -   BORRAR para corregir   -   ESC para volver");
        batch.end();
    }

    @Override
    public void hide() {
        entradas.detenerCapturaTexto();
    }

    @Override
    public void dispose() {
        entradas.detenerCapturaTexto();
    }
}
