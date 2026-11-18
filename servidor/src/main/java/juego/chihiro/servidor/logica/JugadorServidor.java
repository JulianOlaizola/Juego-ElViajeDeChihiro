package juego.chihiro.servidor.logica;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

import juego.chihiro.entidades.Direccion;
import juego.chihiro.mundo.ObjetoEnMano;
import juego.chihiro.servidor.fisica.MundoFisico;
import juego.chihiro.utiles.Constantes;

public class JugadorServidor {

    private final int numero;
    private final Rectangle zonaInteraccion = new Rectangle();
    private Body cuerpo;
    private int ultimoContadorInteraccion;
    private Direccion direccion = Direccion.ABAJO;
    private ObjetoEnMano objetoEnMano = ObjetoEnMano.NADA;
    private float ejeX;
    private float ejeY;
    private boolean enMovimiento;

    public JugadorServidor(int numero) {
        this.numero = numero;
    }

    public void ubicar(MundoFisico mundo, Vector2 aparicion) {
        float centroX = aparicion.x + Constantes.TAM_TILE / 2f;
        float centroY = aparicion.y + Constantes.DESFASE_HITBOX_Y + Constantes.ALTO_HITBOX / 2f;
        cuerpo = mundo.crearCuerpoJugador(centroX, centroY, this);
        direccion = Direccion.ABAJO;
        objetoEnMano = ObjetoEnMano.NADA;
        ejeX = 0f;
        ejeY = 0f;
        enMovimiento = false;
    }

    public void setEjes(float x, float y) {
        ejeX = Math.max(-1f, Math.min(1f, x));
        ejeY = Math.max(-1f, Math.min(1f, y));
    }

    public void aplicarMovimiento() {
        if (cuerpo == null) {
            return;
        }
        enMovimiento = ejeX != 0f || ejeY != 0f;
        if (!enMovimiento) {
            cuerpo.setLinearVelocity(0f, 0f);
            return;
        }
        direccion = Direccion.desdeVector(ejeX, ejeY);
        float largo = (float) Math.sqrt(ejeX * ejeX + ejeY * ejeY);
        float velocidad = MundoFisico.aMetros(Constantes.VELOCIDAD_JUGADOR);
        cuerpo.setLinearVelocity(ejeX / largo * velocidad, ejeY / largo * velocidad);
    }

    public void detener() {
        ejeX = 0f;
        ejeY = 0f;
        enMovimiento = false;
        if (cuerpo != null) {
            cuerpo.setLinearVelocity(0f, 0f);
        }
    }

    public int interaccionesPendientes(int contadorRecibido, int maximo) {
        int nuevas = contadorRecibido - ultimoContadorInteraccion;
        ultimoContadorInteraccion = contadorRecibido;
        if (nuevas < 0) {
            return 0;
        }
        return Math.min(nuevas, maximo);
    }

    public void sincronizarContador(int contadorRecibido) {
        ultimoContadorInteraccion = contadorRecibido;
    }

    public Rectangle getZonaInteraccion() {
        float ancho = Constantes.ANCHO_HITBOX + Constantes.MARGEN_ZONA_INTERACCION;
        float alto = Constantes.ALTO_HITBOX + Constantes.MARGEN_ZONA_INTERACCION;
        return zonaInteraccion.set(
                getCentroX() - ancho / 2f + direccion.getX() * Constantes.ALCANCE_INTERACCION,
                getCentroY() - alto / 2f + direccion.getY() * Constantes.ALCANCE_INTERACCION,
                ancho,
                alto);
    }

    public float getCentroX() {
        return cuerpo == null ? 0f : MundoFisico.aPixeles(cuerpo.getPosition().x);
    }

    public float getCentroY() {
        return cuerpo == null ? 0f : MundoFisico.aPixeles(cuerpo.getPosition().y);
    }

    public int getNumero() {
        return numero;
    }

    public Direccion getDireccion() {
        return direccion;
    }

    public boolean estaEnMovimiento() {
        return enMovimiento;
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
