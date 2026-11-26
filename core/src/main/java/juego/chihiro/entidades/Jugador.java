package juego.chihiro.entidades;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

import juego.chihiro.mundo.ObjetoEnMano;
import juego.chihiro.red.InstantaneaPartida;
import juego.chihiro.utiles.Constantes;
import juego.chihiro.utiles.Recursos;

public class Jugador extends Entidad {
    private static final float VELOCIDAD_INTERPOLACION = 15f;
    private static final float DISTANCIA_PARA_SALTAR = 64f;

    private final int numero;
    private final Recursos recursos;
    private final Rectangle zonaInteraccion = new Rectangle();

    private Direccion direccion = Direccion.ABAJO;
    private ObjetoEnMano objetoEnMano = ObjetoEnMano.NADA;
    private boolean enMovimiento;
    private boolean conectado;
    private boolean ubicado;

    public Jugador(int numero, Recursos recursos) {
        super(0f, 0f, Constantes.ANCHO_HITBOX, Constantes.ALTO_HITBOX);
        this.numero = numero;
        this.recursos = recursos;
    }

    public void actualizar(float delta, InstantaneaPartida.DatosJugador datos) {
        conectado = datos.estaConectado();
        direccion = datos.getDireccion();
        objetoEnMano = datos.getObjeto();
        enMovimiento = datos.estaEnMovimiento();

        float objetivoX = datos.getX();
        float objetivoY = datos.getY();
        float distancia = Math.abs(objetivoX - getCentroX()) + Math.abs(objetivoY - getCentroY());
        if (!ubicado || distancia > DISTANCIA_PARA_SALTAR) {
            ubicarCentro(objetivoX, objetivoY);
            ubicado = true;
        } else {
            float factor = Math.min(1f, delta * VELOCIDAD_INTERPOLACION);
            ubicarCentro(MathUtils.lerp(getCentroX(), objetivoX, factor),
                MathUtils.lerp(getCentroY(), objetivoY, factor));
        }

        tiempoAnimacion = enMovimiento ? tiempoAnimacion + delta : 0f;
        actualizarZonaInteraccion();
    }

    private void ubicarCentro(float x, float y) {
        hitbox.x = x - hitbox.width / 2f;
        hitbox.y = y - hitbox.height / 2f;
    }

    private void actualizarZonaInteraccion() {
        float ancho = hitbox.width + Constantes.MARGEN_ZONA_INTERACCION;
        float alto = hitbox.height + Constantes.MARGEN_ZONA_INTERACCION;
        zonaInteraccion.set(
            getCentroX() - ancho / 2f + direccion.getX() * Constantes.ALCANCE_INTERACCION,
            getCentroY() - alto / 2f + direccion.getY() * Constantes.ALCANCE_INTERACCION,
            ancho,
            alto);
    }

    @Override
    public void dibujar(SpriteBatch batch) {
        if (!ubicado) {
            return;
        }
        TextureRegion cuadro = enMovimiento
            ? recursos.caminata(numero, direccion.ordinal()).getKeyFrame(tiempoAnimacion)
            : recursos.reposo(numero, direccion.ordinal());

        float x = getCentroX() - Constantes.TAM_TILE / 2f;
        float y = hitbox.y - Constantes.DESFASE_HITBOX_Y;
        if (!conectado) {
            batch.setColor(1f, 1f, 1f, 0.35f);
        }
        batch.draw(cuadro, x, y, Constantes.TAM_TILE, Constantes.TAM_TILE);

        if (objetoEnMano != ObjetoEnMano.NADA) {
            batch.draw(recursos.objeto(objetoEnMano.getIndiceRegion()),
                x + 10f, y + Constantes.TAM_TILE - 8f, 20f, 20f);
        }
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public int getNumero() {
        return numero;
    }

    public Rectangle getZonaInteraccion() {
        return zonaInteraccion;
    }

    public ObjetoEnMano getObjetoEnMano() {
        return objetoEnMano;
    }

    public boolean estaUbicado() {
        return ubicado;
    }
}
