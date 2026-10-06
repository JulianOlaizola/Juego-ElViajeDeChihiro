package juego.chihiro.mundo;

public enum TipoInteractivo {
    ALMACEN_SALES("almacen_sales"),
    CALDERA("caldera"),
    TINA("tina"),
    DEPOSITO_SUCIEDAD("deposito_suciedad"),
    SPAWN_JUGADOR_1("spawn_jugador_1"),
    SPAWN_JUGADOR_2("spawn_jugador_2");

    private final String clave;

    TipoInteractivo(String clave) {
        this.clave = clave;
    }

    public static TipoInteractivo desdeClave(String clave) {
        for (TipoInteractivo tipo : values()) {
            if (tipo.clave.equals(clave)) {
                return tipo;
            }
        }
        return null;
    }
}
