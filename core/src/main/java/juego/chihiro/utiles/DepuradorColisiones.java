package juego.chihiro.utiles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;

import juego.chihiro.entidades.Jugador;
import juego.chihiro.mundo.MapaCasaBanos;
import juego.chihiro.mundo.ObjetoInteractivo;

public class DepuradorColisiones implements Disposable {
    private final ShapeRenderer formas = new ShapeRenderer();

    public void dibujar(OrthographicCamera camara, MapaCasaBanos mapa, Jugador... jugadores) {
        formas.setProjectionMatrix(camara.combined);
        formas.begin(ShapeRenderer.ShapeType.Line);

        formas.setColor(Color.RED);
        for (Rectangle solido : mapa.getColisiones()) {
            formas.rect(solido.x, solido.y, solido.width, solido.height);
        }

        formas.setColor(Color.YELLOW);
        for (ObjetoInteractivo objeto : mapa.getInteractivos()) {
            Rectangle area = objeto.getArea();
            formas.rect(area.x, area.y, area.width, area.height);
        }

        for (Jugador jugador : jugadores) {
            Rectangle hitbox = jugador.getHitbox();
            formas.setColor(Color.LIME);
            formas.rect(hitbox.x, hitbox.y, hitbox.width, hitbox.height);

            Rectangle zona = jugador.getZonaInteraccion();
            formas.setColor(Color.CYAN);
            formas.rect(zona.x, zona.y, zona.width, zona.height);
        }

        formas.end();
    }

    @Override
    public void dispose() {
        formas.dispose();
    }
}
