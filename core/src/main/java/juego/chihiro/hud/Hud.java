package juego.chihiro.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;

import juego.chihiro.audio.AdministradorAudio;
import juego.chihiro.mundo.EstadoTina;
import juego.chihiro.mundo.ObjetoEnMano;
import juego.chihiro.red.InstantaneaPartida;
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
    private static final Color MARRON = new Color(0.82f, 0.62f, 0.38f, 1f);

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

    public void dibujar(SpriteBatch batch, InstantaneaPartida estado, int jugadorLocal, String mensaje,
                        long latenciaMilis) {
        vista.apply();
        batch.setProjectionMatrix(camara.combined);
        batch.begin();

        dibujarBarraSuperior(batch, estado);
        dibujarPedidos(batch, estado);
        dibujarCaldera(batch, estado);
        dibujarManos(batch, estado, 1, jugadorLocal, MARGEN);
        dibujarManos(batch, estado, 2, jugadorLocal, Constantes.ANCHO_VISTA - MARGEN - ANCHO_TARJETA);
        dibujarMensaje(batch, mensaje);
        dibujarLatencia(batch, latenciaMilis);

        batch.end();
    }

    private void dibujarBarraSuperior(SpriteBatch batch, InstantaneaPartida estado) {
        float y = Constantes.ALTO_VISTA - ALTO_BARRA_SUPERIOR;
        panel(batch, 0f, y, Constantes.ANCHO_VISTA, ALTO_BARRA_SUPERIOR, FONDO);

        BitmapFont fuente = recursos.getFuenteMedia();
        float linea = y + ALTO_BARRA_SUPERIOR / 2f + fuente.getCapHeight() / 2f;

        fuente.setColor(estado.getTiempoRestante() <= 30f ? ROJO : Color.WHITE);
        fuente.draw(batch, "TIEMPO " + formatearTiempo(estado.getTiempoRestante()), MARGEN, linea);

        fuente.setColor(Color.WHITE);
        fuente.draw(batch, "PEDIDOS " + estado.getPedidosCompletados()
            + "/" + Constantes.PEDIDOS_PARA_GANAR, 196f, linea);

        fuente.setColor(estado.getPedidosFallidos() > 0 ? ROJO : Color.WHITE);
        fuente.draw(batch, "FALLOS " + estado.getPedidosFallidos()
            + "/" + Constantes.FALLOS_PARA_PERDER, 400f, linea);

        fuente.setColor(Color.WHITE);
        fuente.draw(batch, "PUNTAJE " + estado.getPuntaje(), 570f, linea);
        fuente.setColor(audio.estaSilenciado() ? Color.LIGHT_GRAY : Color.WHITE);
        fuente.draw(batch, audio.describirVolumen(), 780f, linea);
        fuente.setColor(Color.WHITE);
    }

    private void dibujarPedidos(SpriteBatch batch, InstantaneaPartida estado) {
        float alto = 56f;
        float y = Constantes.ALTO_VISTA - ALTO_BARRA_SUPERIOR - MARGEN - alto;
        BitmapFont fuente = recursos.getFuenteChica();
        int indice = 0;

        for (InstantaneaPartida.DatosTina tina : estado.getTinas()) {
            indice++;
            if (tina.getEstado() == EstadoTina.SUCIA) {
                y = dibujarTinaSucia(batch, indice, y);
                continue;
            }
            if (tina.getEstado() != EstadoTina.OCUPADA) {
                continue;
            }

            panel(batch, MARGEN, y, ANCHO_TARJETA, alto, FONDO);

            StringBuilder falta = new StringBuilder("falta: ");
            if (tina.faltaSal()) {
                falta.append("sales ");
            }
            if (tina.faltaAgua()) {
                falta.append("agua");
            }

            fuente.setColor(Color.WHITE);
            fuente.draw(batch, "Tina " + indice, MARGEN + 8f, y + alto - 10f);
            fuente.setColor(Color.GOLD);
            fuente.draw(batch, falta.toString().trim(), MARGEN + 8f, y + alto - 28f);
            fuente.setColor(Color.WHITE);

            float proporcion = tina.getProporcion();
            barra(batch, MARGEN + 8f, y + 10f, ANCHO_TARJETA - 16f, 7f, proporcion,
                proporcion < 0.3f ? ROJO : VERDE);

            y -= alto + 8f;
        }
    }

    private float dibujarTinaSucia(SpriteBatch batch, int indice, float yPedido) {
        float alto = 30f;
        float y = yPedido + 56f - alto;
        panel(batch, MARGEN, y, ANCHO_TARJETA, alto, FONDO);
        panel(batch, MARGEN, y, 4f, alto, MARRON);
        BitmapFont fuente = recursos.getFuenteChica();
        fuente.setColor(MARRON);
        fuente.draw(batch, "Tina " + indice + " sucia: limpiala", MARGEN + 10f, y + alto - 9f);
        fuente.setColor(Color.WHITE);
        return y - 8f - 56f;
    }

    private void dibujarCaldera(SpriteBatch batch, InstantaneaPartida estado) {
        float alto = 46f;
        float x = Constantes.ANCHO_VISTA - MARGEN - ANCHO_TARJETA;
        float y = Constantes.ALTO_VISTA - ALTO_BARRA_SUPERIOR - MARGEN - alto;

        panel(batch, x, y, ANCHO_TARJETA, alto, FONDO);

        String texto;
        Color color;
        if (!estado.estaLaCalderaEncendida()) {
            texto = "apagada";
            color = ROJO;
        } else if (!estado.estaElAguaCaliente()) {
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

        barra(batch, x + 8f, y + 11f, ANCHO_TARJETA - 16f, 7f, estado.getProporcionCaldera(), color);
    }

    private void dibujarManos(SpriteBatch batch, InstantaneaPartida estado, int numero, int jugadorLocal, float x) {
        float alto = 46f;
        float y = MARGEN;
        InstantaneaPartida.DatosJugador datos = estado.getJugador(numero);
        ObjetoEnMano objeto = datos.getObjeto();

        panel(batch, x, y, ANCHO_TARJETA, alto, FONDO);
        batch.draw(recursos.objeto(objeto.getIndiceRegion()), x + 8f, y + 7f, 32f, 32f);

        String etiqueta = "JUGADOR " + numero + (numero == jugadorLocal ? " (VOS)" : "");
        BitmapFont fuente = recursos.getFuenteChica();
        fuente.setColor(datos.estaConectado() ? Color.WHITE : Color.GRAY);
        fuente.draw(batch, etiqueta, x + 50f, y + alto - 10f);
        fuente.setColor(Color.GOLD);
        fuente.draw(batch, datos.estaConectado() ? objeto.getEtiqueta() : "desconectado", x + 50f, y + alto - 28f);
        fuente.setColor(Color.WHITE);
    }

    private void dibujarMensaje(SpriteBatch batch, String mensaje) {
        if (mensaje == null || mensaje.isEmpty()) {
            return;
        }

        float ancho = 600f;
        float alto = 32f;
        float x = (Constantes.ANCHO_VISTA - ancho) / 2f;
        float y = MARGEN + 46f + 12f;

        panel(batch, x, y, ancho, alto, FONDO);
        BitmapFont fuente = recursos.getFuenteMedia();
        fuente.setColor(Color.WHITE);
        fuente.draw(batch, mensaje, x, y + alto - 8f, ancho, Align.center, false);
    }

    private void dibujarLatencia(SpriteBatch batch, long latenciaMilis) {
        String texto = latenciaMilis < 0 ? "PING midiendo..." : "PING " + latenciaMilis + " ms";
        BitmapFont fuente = recursos.getFuenteChica();
        fuente.setColor(latenciaMilis > 150 ? NARANJA : Color.LIGHT_GRAY);
        fuente.draw(batch, texto, 0f, MARGEN + 28f, Constantes.ANCHO_VISTA, Align.center, false);
        fuente.setColor(Color.WHITE);
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
