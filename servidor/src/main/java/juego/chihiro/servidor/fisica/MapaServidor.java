package juego.chihiro.servidor.fisica;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.SerializationException;
import com.badlogic.gdx.utils.XmlReader;

import juego.chihiro.mundo.TipoInteractivo;
import juego.chihiro.utiles.Constantes;

public class MapaServidor {

    private final Array<Rectangle> colisiones = new Array<>();
    private final Array<ObjetoServidor> interactivos = new Array<>();
    private final Vector2 aparicionJugador1 = new Vector2();
    private final Vector2 aparicionJugador2 = new Vector2();
    private float ancho;
    private float alto;

    public MapaServidor(String ruta) throws MapaInvalidoException {
        FileHandle archivo = Gdx.files.internal(ruta);
        if (!archivo.exists()) {
            throw new MapaInvalidoException("No se encontró el mapa " + ruta);
        }
        XmlReader.Element raiz;
        try {
            raiz = new XmlReader().parse(archivo);
        } catch (SerializationException | GdxRuntimeException e) {
            throw new MapaInvalidoException("No se pudo leer el mapa " + ruta, e);
        }
        int columnas = raiz.getIntAttribute("width", 0);
        int filas = raiz.getIntAttribute("height", 0);
        int anchoTile = raiz.getIntAttribute("tilewidth", Constantes.TAM_TILE);
        int altoTile = raiz.getIntAttribute("tileheight", Constantes.TAM_TILE);
        if (columnas <= 0 || filas <= 0) {
            throw new MapaInvalidoException("El mapa no tiene tamaño válido");
        }
        ancho = columnas * anchoTile;
        alto = filas * altoTile;

        for (XmlReader.Element grupo : raiz.getChildrenByName("objectgroup")) {
            String nombreCapa = grupo.getAttribute("name", "");
            if (Constantes.CAPA_COLISIONES.equals(nombreCapa)) {
                leerColisiones(grupo);
            } else if (Constantes.CAPA_INTERACTIVOS.equals(nombreCapa)) {
                leerInteractivos(grupo);
            }
        }
        if (colisiones.isEmpty()) {
            throw new MapaInvalidoException("El mapa no tiene la capa de colisiones");
        }
    }

    private void leerColisiones(XmlReader.Element grupo) {
        for (XmlReader.Element objeto : grupo.getChildrenByName("object")) {
            colisiones.add(rectangulo(objeto));
        }
    }

    private void leerInteractivos(XmlReader.Element grupo) {
        for (XmlReader.Element objeto : grupo.getChildrenByName("object")) {
            TipoInteractivo tipo = TipoInteractivo.desdeClave(propiedad(objeto, Constantes.PROPIEDAD_TIPO));
            if (tipo == null) {
                continue;
            }
            Rectangle area = rectangulo(objeto);
            if (tipo == TipoInteractivo.SPAWN_JUGADOR_1) {
                aparicionJugador1.set(area.x, area.y);
            } else if (tipo == TipoInteractivo.SPAWN_JUGADOR_2) {
                aparicionJugador2.set(area.x, area.y);
            } else {
                interactivos.add(new ObjetoServidor(interactivos.size, objeto.getAttribute("name", ""), tipo, area));
            }
        }
    }

    private Rectangle rectangulo(XmlReader.Element objeto) {
        float x = objeto.getFloatAttribute("x", 0f);
        float yTiled = objeto.getFloatAttribute("y", 0f);
        float anchoObjeto = objeto.getFloatAttribute("width", 0f);
        float altoObjeto = objeto.getFloatAttribute("height", 0f);
        return new Rectangle(x, alto - yTiled - altoObjeto, anchoObjeto, altoObjeto);
    }

    private static String propiedad(XmlReader.Element objeto, String nombre) {
        XmlReader.Element propiedades = objeto.getChildByName("properties");
        if (propiedades == null) {
            return "";
        }
        for (XmlReader.Element propiedad : propiedades.getChildrenByName("property")) {
            if (nombre.equals(propiedad.getAttribute("name", ""))) {
                return propiedad.getAttribute("value", "");
            }
        }
        return "";
    }

    public Array<Rectangle> getColisiones() {
        return colisiones;
    }

    public Array<ObjetoServidor> getInteractivos() {
        return interactivos;
    }

    public Vector2 getAparicion(int numeroJugador) {
        return numeroJugador == 1 ? aparicionJugador1 : aparicionJugador2;
    }

    public float getAncho() {
        return ancho;
    }

    public float getAlto() {
        return alto;
    }
}
