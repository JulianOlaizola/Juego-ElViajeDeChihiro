package juego.chihiro.entidades;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import juego.chihiro.utiles.Constantes;
import juego.chihiro.utiles.Recursos;

public class Espiritu extends Entidad {
    private final Recursos recursos;

    public Espiritu(float x, float y, Recursos recursos) {
        super(x, y, Constantes.TAM_TILE, Constantes.TAM_TILE);
        this.recursos = recursos;
    }

    @Override
    public void actualizar(float delta) {
        tiempoAnimacion += delta;
    }

    @Override
    public void dibujar(SpriteBatch batch) {
        TextureRegion cuadro = recursos.getFlotarEspiritu().getKeyFrame(tiempoAnimacion);
        batch.draw(cuadro, hitbox.x, hitbox.y, hitbox.width, hitbox.height);
    }
}
