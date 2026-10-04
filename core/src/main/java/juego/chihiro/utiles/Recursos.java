package juego.chihiro.utiles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class Recursos implements Disposable {
    private final Array<Array<Animation<TextureRegion>>> caminatas = new Array<>();
    private final Array<Array<TextureRegion>> reposos = new Array<>();
    private final Array<TextureRegion> objetos = new Array<>();

    private final Animation<TextureRegion> flotarEspiritu;
    private final TextureRegion pixel;
    private final TextureRegion logo;

    private final BitmapFont fuenteChica;
    private final BitmapFont fuenteMedia;
    private final BitmapFont fuenteGrande;

    public Recursos(AssetManager assets) {
        Texture hojaJugadores = texturaNitida(assets, Constantes.RUTA_JUGADORES);
        TextureRegion[][] cuadros =
            TextureRegion.split(hojaJugadores, Constantes.TAM_TILE, Constantes.TAM_TILE);

        for (int jugador = 0; jugador < Constantes.CANTIDAD_JUGADORES; jugador++) {
            Array<Animation<TextureRegion>> caminataJugador = new Array<>();
            Array<TextureRegion> reposoJugador = new Array<>();
            for (int direccion = 0; direccion < Constantes.DIRECCIONES; direccion++) {
                int filaBase = jugador * Constantes.DIRECCIONES + direccion;
                Array<TextureRegion> secuencia = new Array<>();
                for (int columna = 0; columna < Constantes.CUADROS_POR_FILA; columna++) {
                    secuencia.add(cuadros[filaBase][columna]);
                }
                caminataJugador.add(new Animation<>(
                    Constantes.DURACION_CUADRO, secuencia, Animation.PlayMode.LOOP));
                reposoJugador.add(cuadros[filaBase][0]);
            }
            caminatas.add(caminataJugador);
            reposos.add(reposoJugador);
        }

        Texture hojaEspiritus = texturaNitida(assets, Constantes.RUTA_ESPIRITUS);
        TextureRegion[][] cuadrosEspiritu =
            TextureRegion.split(hojaEspiritus, Constantes.TAM_TILE, Constantes.TAM_TILE);
        Array<TextureRegion> secuenciaEspiritu = new Array<>();
        for (int columna = 0; columna < Constantes.CUADROS_POR_FILA; columna++) {
            secuenciaEspiritu.add(cuadrosEspiritu[0][columna]);
        }
        flotarEspiritu = new Animation<>(
            Constantes.DURACION_CUADRO * 2f, secuenciaEspiritu, Animation.PlayMode.LOOP);

        Texture hojaObjetos = texturaNitida(assets, Constantes.RUTA_OBJETOS);
        TextureRegion[][] cuadrosObjeto =
            TextureRegion.split(hojaObjetos, Constantes.TAM_TILE, Constantes.TAM_TILE);
        for (int columna = 0; columna < Constantes.CUADROS_POR_FILA; columna++) {
            objetos.add(cuadrosObjeto[0][columna]);
        }

        pixel = new TextureRegion(texturaNitida(assets, Constantes.RUTA_PIXEL));

        Texture texturaLogo = assets.get(Constantes.RUTA_LOGO, Texture.class);
        texturaLogo.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        logo = new TextureRegion(texturaLogo);

        FreeTypeFontGenerator generador =
            new FreeTypeFontGenerator(Gdx.files.internal(Constantes.RUTA_FUENTE));
        fuenteChica = generarFuente(generador, Constantes.TAM_FUENTE_CHICA);
        fuenteMedia = generarFuente(generador, Constantes.TAM_FUENTE_MEDIA);
        fuenteGrande = generarFuente(generador, Constantes.TAM_FUENTE_GRANDE);
        generador.dispose();
    }

    private static Texture texturaNitida(AssetManager assets, String ruta) {
        Texture textura = assets.get(ruta, Texture.class);

        textura.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        return textura;
    }

    private static BitmapFont generarFuente(FreeTypeFontGenerator generador, int tamano) {
        FreeTypeFontGenerator.FreeTypeFontParameter parametro =
            new FreeTypeFontGenerator.FreeTypeFontParameter();
        parametro.size = tamano;
        parametro.color = Color.WHITE;
        parametro.borderWidth = 1.4f;
        parametro.borderColor = new Color(0f, 0f, 0f, 0.85f);
        parametro.minFilter = Texture.TextureFilter.Linear;
        parametro.magFilter = Texture.TextureFilter.Linear;
        parametro.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "áéíóúÁÉÍÓÚñÑüÜ¡¿°";
        return generador.generateFont(parametro);
    }

    public Animation<TextureRegion> caminata(int jugador, int direccion) {
        return caminatas.get(jugador - 1).get(direccion);
    }

    public TextureRegion reposo(int jugador, int direccion) {
        return reposos.get(jugador - 1).get(direccion);
    }

    public Animation<TextureRegion> getFlotarEspiritu() {
        return flotarEspiritu;
    }

    public TextureRegion objeto(int indiceRegion) {
        return objetos.get(indiceRegion);
    }

    public TextureRegion getPixel() {
        return pixel;
    }

    public TextureRegion getLogo() {
        return logo;
    }

    public BitmapFont getFuenteChica() {
        return fuenteChica;
    }

    public BitmapFont getFuenteMedia() {
        return fuenteMedia;
    }

    public BitmapFont getFuenteGrande() {
        return fuenteGrande;
    }

    @Override
    public void dispose() {
        fuenteChica.dispose();
        fuenteMedia.dispose();
        fuenteGrande.dispose();
    }
}
