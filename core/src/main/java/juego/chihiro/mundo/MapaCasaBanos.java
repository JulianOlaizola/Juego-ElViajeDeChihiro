package juego.chihiro.mundo;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import juego.chihiro.utiles.Constantes;

public class MapaCasaBanos {
    private final TiledMap mapa;
    private final int[] capasVisibles;
    private final Array<Rectangle> colisiones = new Array<>();
    private final Array<ObjetoInteractivo> interactivos = new Array<>();
    private final Vector2 aparicionJugador1 = new Vector2();
    private final Vector2 aparicionJugador2 = new Vector2();
    private final float ancho;
    private final float alto;

    public MapaCasaBanos(AssetManager assets) {
        mapa = assets.get(Constantes.RUTA_MAPA, TiledMap.class);

        TiledMapTileLayer capaPiso =
            (TiledMapTileLayer) mapa.getLayers().get(Constantes.CAPA_PISO);
        ancho = capaPiso.getWidth() * capaPiso.getTileWidth();
        alto = capaPiso.getHeight() * capaPiso.getTileHeight();

        capasVisibles = new int[] {
            mapa.getLayers().getIndex(Constantes.CAPA_PISO),
            mapa.getLayers().getIndex(Constantes.CAPA_DECORACION)
        };

        leerColisiones();
        leerInteractivos();
    }

    private void leerColisiones() {
        MapLayer capa = mapa.getLayers().get(Constantes.CAPA_COLISIONES);
        if (capa == null) {
            return;
        }
        for (MapObject objeto : capa.getObjects()) {
            if (objeto instanceof RectangleMapObject) {
                colisiones.add(((RectangleMapObject) objeto).getRectangle());
            }
        }
    }

    private void leerInteractivos() {
        MapLayer capa = mapa.getLayers().get(Constantes.CAPA_INTERACTIVOS);
        if (capa == null) {
            return;
        }
        for (MapObject objeto : capa.getObjects()) {
            if (!(objeto instanceof RectangleMapObject)) {
                continue;
            }
            Rectangle area = ((RectangleMapObject) objeto).getRectangle();
            String clave = objeto.getProperties().get(Constantes.PROPIEDAD_TIPO, "", String.class);
            TipoInteractivo tipo = TipoInteractivo.desdeClave(clave);
            if (tipo == null) {
                continue;
            }

            if (tipo == TipoInteractivo.SPAWN_JUGADOR_1) {
                aparicionJugador1.set(area.x, area.y);
            } else if (tipo == TipoInteractivo.SPAWN_JUGADOR_2) {
                aparicionJugador2.set(area.x, area.y);
            } else {
                interactivos.add(new ObjetoInteractivo(objeto.getName(), tipo, area));
            }
        }
    }

    public boolean choca(Rectangle rectangulo) {
        for (Rectangle solido : colisiones) {
            if (solido.overlaps(rectangulo)) {
                return true;
            }
        }
        return false;
    }

    public ObjetoInteractivo objetoEn(Rectangle zona) {
        for (ObjetoInteractivo objeto : interactivos) {
            if (objeto.getArea().overlaps(zona)) {
                return objeto;
            }
        }
        return null;
    }

    public TiledMap getMapa() {
        return mapa;
    }

    public int[] getCapasVisibles() {
        return capasVisibles;
    }

    public Array<Rectangle> getColisiones() {
        return colisiones;
    }

    public Array<ObjetoInteractivo> getInteractivos() {
        return interactivos;
    }

    public Vector2 getAparicionJugador1() {
        return aparicionJugador1;
    }

    public Vector2 getAparicionJugador2() {
        return aparicionJugador2;
    }

    public float getAncho() {
        return ancho;
    }

    public float getAlto() {
        return alto;
    }
}
