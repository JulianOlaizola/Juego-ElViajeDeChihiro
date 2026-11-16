package juego.chihiro.servidor.fisica;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;

import juego.chihiro.utiles.Constantes;

public class MundoFisico implements Disposable {

    public static final float PIXELES_POR_METRO = 32f;
    public static final float PASO = 1f / 60f;
    private static final int ITERACIONES_VELOCIDAD = 6;
    private static final int ITERACIONES_POSICION = 2;
    private static final int MAXIMO_PASOS_POR_TICK = 5;
    private static final float MAXIMO_DELTA = 0.25f;

    private static boolean box2dIniciado;

    private final World mundo;
    private final int cantidadParedes;
    private final int cantidadSensores;
    private float acumulador;

    public MundoFisico(MapaServidor mapa) {
        iniciarBox2D();
        mundo = new World(new Vector2(0f, 0f), true);
        for (Rectangle pared : mapa.getColisiones()) {
            crearRectangulo(BodyDef.BodyType.StaticBody, pared, false, null);
        }
        for (ObjetoServidor objeto : mapa.getInteractivos()) {
            crearRectangulo(BodyDef.BodyType.StaticBody, objeto.getArea(), true, objeto);
        }
        cantidadParedes = mapa.getColisiones().size;
        cantidadSensores = mapa.getInteractivos().size;
    }

    private static synchronized void iniciarBox2D() {
        if (!box2dIniciado) {
            Box2D.init();
            box2dIniciado = true;
        }
    }

    private Body crearRectangulo(BodyDef.BodyType tipo, Rectangle area, boolean sensor, Object datos) {
        BodyDef definicion = new BodyDef();
        definicion.type = tipo;
        definicion.position.set(aMetros(area.x + area.width / 2f), aMetros(area.y + area.height / 2f));
        Body cuerpo = mundo.createBody(definicion);

        PolygonShape forma = new PolygonShape();
        forma.setAsBox(aMetros(area.width / 2f), aMetros(area.height / 2f));
        FixtureDef accesorio = new FixtureDef();
        accesorio.shape = forma;
        accesorio.isSensor = sensor;
        Fixture fixture = cuerpo.createFixture(accesorio);
        fixture.setUserData(datos);
        forma.dispose();
        return cuerpo;
    }

    public Body crearCuerpoJugador(float centroX, float centroY, Object duenio) {
        BodyDef definicion = new BodyDef();
        definicion.type = BodyDef.BodyType.DynamicBody;
        definicion.fixedRotation = true;
        definicion.position.set(aMetros(centroX), aMetros(centroY));
        Body cuerpo = mundo.createBody(definicion);

        PolygonShape forma = new PolygonShape();
        forma.setAsBox(aMetros(Constantes.ANCHO_HITBOX / 2f), aMetros(Constantes.ALTO_HITBOX / 2f));
        FixtureDef accesorio = new FixtureDef();
        accesorio.shape = forma;
        accesorio.density = 1f;
        accesorio.friction = 0f;
        Fixture fixture = cuerpo.createFixture(accesorio);
        fixture.setUserData(duenio);
        cuerpo.setUserData(duenio);
        forma.dispose();
        return cuerpo;
    }

    public void destruir(Body cuerpo) {
        if (cuerpo != null) {
            mundo.destroyBody(cuerpo);
        }
    }

    public int avanzar(float delta) {
        acumulador += Math.min(delta, MAXIMO_DELTA);
        int pasos = 0;
        while (acumulador >= PASO && pasos < MAXIMO_PASOS_POR_TICK) {
            mundo.step(PASO, ITERACIONES_VELOCIDAD, ITERACIONES_POSICION);
            acumulador -= PASO;
            pasos++;
        }
        if (pasos == MAXIMO_PASOS_POR_TICK) {
            acumulador = 0f;
        }
        return pasos;
    }

    public void buscarEn(Rectangle zona, QueryCallback consulta) {
        mundo.QueryAABB(consulta,
            aMetros(zona.x), aMetros(zona.y),
            aMetros(zona.x + zona.width), aMetros(zona.y + zona.height));
    }

    public int getCantidadParedes() {
        return cantidadParedes;
    }

    public int getCantidadSensores() {
        return cantidadSensores;
    }

    public static float aMetros(float pixeles) {
        return pixeles / PIXELES_POR_METRO;
    }

    public static float aPixeles(float metros) {
        return metros * PIXELES_POR_METRO;
    }

    @Override
    public void dispose() {
        mundo.dispose();
    }
}
