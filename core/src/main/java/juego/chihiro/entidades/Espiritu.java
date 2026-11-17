package juego.chihiro.entidades;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import juego.chihiro.utiles.Constantes;
import juego.chihiro.utiles.Recursos;

public class Espiritu extends Entidad {
    private final Recursos recursos;
    private boolean visible;

    public Espiritu(float x, float y, Recursos recursos) {
        super(x, y, Constantes.TAM_TILE, Constantes.TAM_TILE);
        this.recursos = recursos;
    }

    public void actualizar(float delta, boolean presente) {
        if (presente && !visible) {
            tiempoAnimacion = 0f;
        }
        visible = presente;
        if (visible) {
            tiempoAnimacion += delta;
        }
    }

    @Override
    public void dibujar(SpriteBatch batch) {
        if (!visible) {
            return;
        }
        TextureRegion cuadro = recursos.getFlotarEspiritu().getKeyFrame(tiempoAnimacion);
        batch.draw(cuadro, hitbox.x, hitbox.y, hitbox.width, hitbox.height);
    }
}
