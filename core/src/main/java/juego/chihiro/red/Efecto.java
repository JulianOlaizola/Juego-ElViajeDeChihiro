package juego.chihiro.red;

public enum Efecto {
    NINGUNO,
    TOMAR,
    ENTREGAR,
    PEDIDO_COMPLETO,
    ERROR,
    LIMPIAR;

    public static Efecto desdeTexto(String texto) {
        for (Efecto efecto : values()) {
            if (efecto.name().equals(texto)) {
                return efecto;
            }
        }
        return NINGUNO;
    }
}
