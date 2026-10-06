package juego.chihiro.entidades;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

import juego.chihiro.entradas.ControladorEntradas;
import juego.chihiro.mundo.MapaCasaBanos;
import juego.chihiro.mundo.ObjetoEnMano;
import juego.chihiro.utiles.Constantes;
import juego.chihiro.utiles.Recursos;

public class Jugador extends Entidad {
    private final int numero;
    private final Recursos recursos;
    private final ControladorEntradas entradas;
    private final MapaCasaBanos mapa;

    private final Rectangle zonaInteraccion = new Rectangle();

    private Direccion direccion = Direccion.ABAJO;
    private ObjetoEnMano objetoEnMano = ObjetoEnMano.NADA;
    private boolean enMovimiento;

    public Jugador(int numero, float x, float y, Recursos recursos,
                   ControladorEntradas entradas, MapaCasaBanos mapa) {
        super(x + (Constantes.TAM_TILE - Constantes.ANCHO_HITBOX) / 2f,
            y + Constantes.DESFASE_HITBOX_Y,
            Constantes.ANCHO_HITBOX,
            Constantes.ALTO_HITBOX);
        this.numero = numero;
        this.recursos = recursos;
        this.entradas = entradas;
        this.mapa = mapa;
        actualizarZonaInteraccion();
    }

    @Override
    public void actualizar(float delta) {
        float ejeX = entradas.ejeHorizontal(numero);
        float ejeY = entradas.ejeVertical(numero);
        enMovimiento = ejeX != 0f || ejeY != 0f;

        if (enMovimiento) {
            direccion = Direccion.desdeVector(ejeX, ejeY);

            float largo = (float) Math.sqrt(ejeX * ejeX + ejeY * ejeY);
            ejeX /= largo;
            ejeY /= largo;
            mover(ejeX * Constantes.VELOCIDAD_JUGADOR * delta,
                ejeY * Constantes.VELOCIDAD_JUGADOR * delta);

            tiempoAnimacion += delta;
        } else {
            tiempoAnimacion = 0f;
        }

        actualizarZonaInteraccion();
    }

    private void mover(float dx, float dy) {
        hitbox.x += dx;
        if (mapa.choca(hitbox)) {
            hitbox.x -= dx;
        }
        hitbox.y += dy;
        if (mapa.choca(hitbox)) {
            hitbox.y -= dy;
        }
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
        TextureRegion cuadro = enMovimiento
            ? recursos.caminata(numero, direccion.ordinal()).getKeyFrame(tiempoAnimacion)
            : recursos.reposo(numero, direccion.ordinal());

        float x = getCentroX() - Constantes.TAM_TILE / 2f;
        float y = hitbox.y - Constantes.DESFASE_HITBOX_Y;
        batch.draw(cuadro, x, y, Constantes.TAM_TILE, Constantes.TAM_TILE);

        if (objetoEnMano != ObjetoEnMano.NADA) {
            batch.draw(recursos.objeto(objetoEnMano.getIndiceRegion()),
                x + 10f, y + Constantes.TAM_TILE - 8f, 20f, 20f);
        }
    }

    public Rectangle getZonaInteraccion() {
        return zonaInteraccion;
    }

    public ObjetoEnMano getObjetoEnMano() {
        return objetoEnMano;
    }

    public void setObjetoEnMano(ObjetoEnMano objetoEnMano) {
        this.objetoEnMano = objetoEnMano;
    }

    public boolean tieneLasManosLibres() {
        return objetoEnMano == ObjetoEnMano.NADA;
    }
}
