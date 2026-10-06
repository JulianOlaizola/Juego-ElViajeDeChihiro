package juego.chihiro.entidades;

public enum Direccion {
    ABAJO(0f, -1f),
    IZQUIERDA(-1f, 0f),
    DERECHA(1f, 0f),
    ARRIBA(0f, 1f);

    private final float x;
    private final float y;

    Direccion(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public static Direccion desdeVector(float x, float y) {
        if (Math.abs(x) > Math.abs(y)) {
            return x > 0f ? DERECHA : IZQUIERDA;
        }
        return y > 0f ? ARRIBA : ABAJO;
    }
}
