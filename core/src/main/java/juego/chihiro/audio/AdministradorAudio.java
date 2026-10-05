package juego.chihiro.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;

import juego.chihiro.utiles.Constantes;

public class AdministradorAudio implements Disposable {
    public enum Efecto {
        TOMAR,
        ENTREGAR,
        PEDIDO_COMPLETO,
        ERROR,
        LIMPIAR
    }

    private static final String CLAVE_MUSICA = "volumenMusica";
    private static final String CLAVE_EFECTOS = "volumenEfectos";
    private static final String CLAVE_SILENCIO = "silenciado";

    private final Music musica;
    private final ObjectMap<Efecto, Sound> efectos = new ObjectMap<>();
    private final Preferences preferencias;

    private float volumenMusica;
    private float volumenEfectos;
    private boolean silenciado;

    public AdministradorAudio(AssetManager assets) {
        musica = assets.get(Constantes.RUTA_MUSICA, Music.class);
        efectos.put(Efecto.TOMAR, assets.get(Constantes.RUTA_TOMAR, Sound.class));
        efectos.put(Efecto.ENTREGAR, assets.get(Constantes.RUTA_ENTREGAR, Sound.class));
        efectos.put(Efecto.PEDIDO_COMPLETO, assets.get(Constantes.RUTA_PEDIDO_COMPLETO, Sound.class));
        efectos.put(Efecto.ERROR, assets.get(Constantes.RUTA_ERROR, Sound.class));
        efectos.put(Efecto.LIMPIAR, assets.get(Constantes.RUTA_LIMPIAR, Sound.class));

        preferencias = Gdx.app.getPreferences(Constantes.ARCHIVO_PREFERENCIAS);
        volumenMusica = preferencias.getFloat(CLAVE_MUSICA, Constantes.VOLUMEN_MUSICA_INICIAL);
        volumenEfectos = preferencias.getFloat(CLAVE_EFECTOS, Constantes.VOLUMEN_EFECTOS_INICIAL);
        silenciado = preferencias.getBoolean(CLAVE_SILENCIO, false);

        musica.setLooping(true);
        aplicarVolumen();
        musica.play();
    }

    public void reproducir(Efecto efecto) {
        if (silenciado) {
            return;
        }
        Sound sonido = efectos.get(efecto);
        if (sonido != null) {
            sonido.play(volumenEfectos);
        }
    }

    public void alternarSilencio() {
        silenciado = !silenciado;
        aplicarVolumen();
        guardar();
    }

    public void subirVolumen() {
        cambiarVolumen(Constantes.PASO_VOLUMEN);
    }

    public void bajarVolumen() {
        cambiarVolumen(-Constantes.PASO_VOLUMEN);
    }

    private void cambiarVolumen(float paso) {
        volumenMusica = MathUtils.clamp(volumenMusica + paso, 0f, 1f);
        volumenEfectos = MathUtils.clamp(volumenEfectos + paso, 0f, 1f);
        aplicarVolumen();
        guardar();
    }

    private void aplicarVolumen() {
        musica.setVolume(silenciado ? 0f : volumenMusica);
    }

    private void guardar() {
        preferencias.putFloat(CLAVE_MUSICA, volumenMusica);
        preferencias.putFloat(CLAVE_EFECTOS, volumenEfectos);
        preferencias.putBoolean(CLAVE_SILENCIO, silenciado);
        preferencias.flush();
    }

    public boolean estaSilenciado() {
        return silenciado;
    }

    public String describirVolumen() {
        if (silenciado) {
            return "SILENCIO";
        }
        return "VOL " + Math.round(volumenMusica * 100f) + "%";
    }

    @Override
    public void dispose() {
        if (musica.isPlaying()) {
            musica.stop();
        }
        guardar();
    }
}
