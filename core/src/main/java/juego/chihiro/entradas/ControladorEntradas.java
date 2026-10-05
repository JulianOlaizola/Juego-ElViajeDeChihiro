package juego.chihiro.entradas;

import java.util.Arrays;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;

public class ControladorEntradas extends InputAdapter {
    private final boolean[] acciones = new boolean[Accion.values().length];

    private boolean arriba1;
    private boolean abajo1;
    private boolean izquierda1;
    private boolean derecha1;

    private boolean arriba2;
    private boolean abajo2;
    private boolean izquierda2;
    private boolean derecha2;

    @Override
    public boolean keyDown(int tecla) {
        switch (tecla) {
            case Input.Keys.W:
                arriba1 = true;
                marcar(Accion.SUBIR);
                return true;
            case Input.Keys.S:
                abajo1 = true;
                marcar(Accion.BAJAR);
                return true;
            case Input.Keys.A:
                izquierda1 = true;
                return true;
            case Input.Keys.D:
                derecha1 = true;
                return true;
            case Input.Keys.E:
                marcar(Accion.INTERACTUAR_JUGADOR_1);
                return true;

            case Input.Keys.UP:
                arriba2 = true;
                marcar(Accion.SUBIR);
                return true;
            case Input.Keys.DOWN:
                abajo2 = true;
                marcar(Accion.BAJAR);
                return true;
            case Input.Keys.LEFT:
                izquierda2 = true;
                return true;
            case Input.Keys.RIGHT:
                derecha2 = true;
                return true;
            case Input.Keys.SHIFT_RIGHT:
                marcar(Accion.INTERACTUAR_JUGADOR_2);
                return true;

            case Input.Keys.P:
            case Input.Keys.ESCAPE:

                marcar(Accion.PAUSA);
                marcar(Accion.VOLVER);
                return true;
            case Input.Keys.ENTER:
            case Input.Keys.SPACE:
                marcar(Accion.CONFIRMAR);
                return true;
            case Input.Keys.M:
                marcar(Accion.SILENCIAR);
                return true;
            case Input.Keys.F1:
                marcar(Accion.DEPURAR);
                return true;

            default:
                return false;
        }
    }

    @Override
    public boolean keyUp(int tecla) {
        switch (tecla) {
            case Input.Keys.W:
                arriba1 = false;
                return true;
            case Input.Keys.S:
                abajo1 = false;
                return true;
            case Input.Keys.A:
                izquierda1 = false;
                return true;
            case Input.Keys.D:
                derecha1 = false;
                return true;
            case Input.Keys.UP:
                arriba2 = false;
                return true;
            case Input.Keys.DOWN:
                abajo2 = false;
                return true;
            case Input.Keys.LEFT:
                izquierda2 = false;
                return true;
            case Input.Keys.RIGHT:
                derecha2 = false;
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean keyTyped(char caracter) {
        if (caracter == '+' || caracter == '=') {
            marcar(Accion.SUBIR_VOLUMEN);
            return true;
        }
        if (caracter == '-') {
            marcar(Accion.BAJAR_VOLUMEN);
            return true;
        }
        return false;
    }

    public float ejeHorizontal(int jugador) {
        boolean izquierda = (jugador == 1) ? izquierda1 : izquierda2;
        boolean derecha = (jugador == 1) ? derecha1 : derecha2;
        return (derecha ? 1f : 0f) - (izquierda ? 1f : 0f);
    }

    public float ejeVertical(int jugador) {
        boolean arriba = (jugador == 1) ? arriba1 : arriba2;
        boolean abajo = (jugador == 1) ? abajo1 : abajo2;
        return (arriba ? 1f : 0f) - (abajo ? 1f : 0f);
    }

    public boolean consumir(Accion accion) {
        if (acciones[accion.ordinal()]) {
            acciones[accion.ordinal()] = false;
            return true;
        }
        return false;
    }

    public void limpiarAcciones() {
        Arrays.fill(acciones, false);
    }

    public void soltarDirecciones() {
        arriba1 = false;
        abajo1 = false;
        izquierda1 = false;
        derecha1 = false;
        arriba2 = false;
        abajo2 = false;
        izquierda2 = false;
        derecha2 = false;
    }

    private void marcar(Accion accion) {
        acciones[accion.ordinal()] = true;
    }
}
