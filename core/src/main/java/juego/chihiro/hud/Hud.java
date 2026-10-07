package juego.chihiro.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;

import juego.chihiro.audio.AdministradorAudio;
import juego.chihiro.entidades.Jugador;
import juego.chihiro.mundo.EstadoTina;
import juego.chihiro.mundo.GestorInteracciones;
import juego.chihiro.mundo.GestorPedidos;
import juego.chihiro.mundo.Tina;
import juego.chihiro.utiles.Constantes;
import juego.chihiro.utiles.Recursos;

public class Hud {
    private static final float MARGEN = 14f;
    private static final float ALTO_BARRA_SUPERIOR = 42f;
    private static final float ANCHO_TARJETA = 210f;

    private static final Color FONDO = new Color(0f, 0f, 0f, 0.55f);
    private static final Color RIEL = new Color(1f, 1f, 1f, 0.18f);
    private static final Color VERDE = new Color(0.45f, 0.82f, 0.55f, 0.95f);
    private static final Color ROJO = new Color(0.92f, 0.42f, 0.38f, 0.95f);
    private static final Color NARANJA = new Color(0.95f, 0.62f, 0.25f, 0.95f);

    private final Recursos recursos;
    private final AdministradorAudio audio;
    private final Viewport vista;
    private final OrthographicCamera camara;

    public Hud(Recursos recursos, AdministradorAudio audio, Viewport vista,
               OrthographicCamera camara) {
        this.recursos = recursos;
        this.audio = audio;
        this.vista = vista;
        this.camara = camara;
    }

    public void dibujar(SpriteBatch batch, float tiempoRestante, GestorPedidos pedidos,
                        GestorInteracciones interacciones, Jugador jugador1, Jugador jugador2) {
        vista.apply();
        batch.setProjectionMatrix(camara.combined);
        batch.begin();

        dibujarBarraSuperior(batch, tiempoRestante, pedidos);
        dibujarPedidos(batch, pedidos);
        dibujarCaldera(batch, interacciones);
        dibujarManos(batch, jugador1, MARGEN, "JUGADOR 1");
        dibujarManos(batch, jugador2, Constantes.ANCHO_VISTA - MARGEN - ANCHO_TARJETA, "JUGADOR 2");
        dibujarMensaje(batch, interacciones);

        batch.end();
    }

    private void dibujarBarraSuperior(SpriteBatch batch, float tiempoRestante, GestorPedidos pedidos) {
        float y = Constantes.ALTO_VISTA - ALTO_BARRA_SUPERIOR;
        panel(batch, 0f, y, Constantes.ANCHO_VISTA, ALTO_BARRA_SUPERIOR, FONDO);

        BitmapFont fuente = recursos.getFuenteMedia();
        float linea = y + ALTO_BARRA_SUPERIOR / 2f + fuente.getCapHeight() / 2f;

        fuente.setColor(tiempoRestante <= 30f ? ROJO : Color.WHITE);
        fuente.draw(batch, "TIEMPO " + formatearTiempo(tiempoRestante), MARGEN, linea);

        fuente.setColor(Color.WHITE);
        fuente.draw(batch, "PEDIDOS " + pedidos.getPedidosCompletados()
            + "/" + Constantes.PEDIDOS_PARA_GANAR, 196f, linea);

        fuente.setColor(pedidos.getPedidosFallidos() > 0 ? ROJO : Color.WHITE);
        fuente.draw(batch, "FALLOS " + pedidos.getPedidosFallidos()
            + "/" + Constantes.FALLOS_PARA_PERDER, 400f, linea);

        fuente.setColor(Color.WHITE);
        fuente.draw(batch, "PUNTAJE " + pedidos.getPuntaje(), 570f, linea);
        fuente.setColor(audio.estaSilenciado() ? Color.LIGHT_GRAY : Color.WHITE);
        fuente.draw(batch, audio.describirVolumen(), 780f, linea);
        fuente.setColor(Color.WHITE);
    }

    private void dibujarPedidos(SpriteBatch batch, GestorPedidos pedidos) {
        float alto = 56f;
        float y = Constantes.ALTO_VISTA - ALTO_BARRA_SUPERIOR - MARGEN - alto;
        BitmapFont fuente = recursos.getFuenteChica();
        int indice = 0;

        for (Tina tina : pedidos.getTinas()) {
            indice++;
            if (tina.getEstado() != EstadoTina.OCUPADA) {
                continue;
            }

            panel(batch, MARGEN, y, ANCHO_TARJETA, alto, FONDO);

            String nombre = tina.getObjeto().getNombre();
            if (nombre == null || nombre.isEmpty()) {
                nombre = "Tina " + indice;
            }

            StringBuilder falta = new StringBuilder("falta: ");
            if (tina.getPedido().faltaSal()) {
                falta.append("sales ");
            }
            if (tina.getPedido().faltaAgua()) {
                falta.append("agua");
            }

            fuente.setColor(Color.WHITE);
            fuente.draw(batch, nombre, MARGEN + 8f, y + alto - 10f);
            fuente.setColor(Color.GOLD);
            fuente.draw(batch, falta.toString().trim(), MARGEN + 8f, y + alto - 28f);
            fuente.setColor(Color.WHITE);

            float proporcion = tina.getPedido().getProporcionRestante();
            barra(batch, MARGEN + 8f, y + 10f, ANCHO_TARJETA - 16f, 7f, proporcion,
                  proporcion < 0.3f ? ROJO : VERDE);

            y -= alto + 8f;
        }
    }

    private void dibujarCaldera(SpriteBatch batch, GestorInteracciones interacciones) {
        float alto = 46f;
        float x = Constantes.ANCHO_VISTA - MARGEN - ANCHO_TARJETA;
        float y = Constantes.ALTO_VISTA - ALTO_BARRA_SUPERIOR - MARGEN - alto;

        panel(batch, x, y, ANCHO_TARJETA, alto, FONDO);

        String texto;
        Color color;
        if (!interacciones.estaLaCalderaEncendida()) {
            texto = "apagada";
            color = ROJO;
        } else if (!interacciones.estaElAguaCaliente()) {
            texto = "calentando";
            color = NARANJA;
        } else {
            texto = "agua lista";
            color = VERDE;
        }

        BitmapFont fuente = recursos.getFuenteChica();
        fuente.setColor(Color.WHITE);
        fuente.draw(batch, "CALDERA", x + 8f, y + alto - 10f);
        fuente.setColor(color);
        fuente.draw(batch, texto, x + 96f, y + alto - 10f);
        fuente.setColor(Color.WHITE);

        barra(batch, x + 8f, y + 11f, ANCHO_TARJETA - 16f, 7f,
              interacciones.getProporcionCaldera(), color);
    }

    private void dibujarManos(SpriteBatch batch, Jugador jugador, float x, String etiqueta) {
        float alto = 46f;
        float y = MARGEN;

        panel(batch, x, y, ANCHO_TARJETA, alto, FONDO);
        batch.draw(recursos.objeto(jugador.getObjetoEnMano().getIndiceRegion()),
                   x + 8f, y + 7f, 32f, 32f);

        BitmapFont fuente = recursos.getFuenteChica();
        fuente.setColor(Color.WHITE);
        fuente.draw(batch, etiqueta, x + 50f, y + alto - 10f);
        fuente.setColor(Color.GOLD);
        fuente.draw(batch, jugador.getObjetoEnMano().getEtiqueta(), x + 50f, y + alto - 28f);
        fuente.setColor(Color.WHITE);
    }

    private void dibujarMensaje(SpriteBatch batch, GestorInteracciones interacciones) {
        String mensaje = interacciones.getMensaje();
        if (mensaje.isEmpty()) {
            return;
        }

        float ancho = 520f;
        float alto = 32f;
        float x = (Constantes.ANCHO_VISTA - ancho) / 2f;
        float y = MARGEN + 46f + 12f;

        panel(batch, x, y, ancho, alto, FONDO);
        BitmapFont fuente = recursos.getFuenteMedia();
        fuente.setColor(Color.WHITE);
        fuente.draw(batch, mensaje, x, y + alto - 8f, ancho, Align.center, false);
    }

    private void panel(SpriteBatch batch, float x, float y, float ancho, float alto, Color color) {
        batch.setColor(color);
        batch.draw(recursos.getPixel(), x, y, ancho, alto);
        batch.setColor(Color.WHITE);
    }

    private void barra(SpriteBatch batch, float x, float y, float ancho, float alto,
                       float proporcion, Color color) {
        panel(batch, x, y, ancho, alto, RIEL);
        panel(batch, x, y, ancho * MathUtils.clamp(proporcion, 0f, 1f), alto, color);
    }

    private static String formatearTiempo(float segundos) {
        int total = Math.max(0, MathUtils.ceil(segundos));
        return String.format("%02d:%02d", total / 60, total % 60);
    }
}
