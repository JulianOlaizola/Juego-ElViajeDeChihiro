package juego.chihiro.entidades;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public abstract class Entidad {
    protected final Rectangle hitbox;
    protected float tiempoAnimacion;

    protected Entidad(float x, float y, float ancho, float alto) {
        this.hitbox = new Rectangle(x, y, ancho, alto);
    }

    public abstract void actualizar(float delta);

    public abstract void dibujar(SpriteBatch batch);

    public Rectangle getHitbox() {
        return hitbox;
    }

    public float getCentroX() {
        return hitbox.x + hitbox.width / 2f;
    }

    public float getCentroY() {
        return hitbox.y + hitbox.height / 2f;
    }
}
