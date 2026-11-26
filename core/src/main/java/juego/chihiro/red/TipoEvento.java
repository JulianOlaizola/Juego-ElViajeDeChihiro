package juego.chihiro.red;

public enum TipoEvento {
    AVISO,
    INICIO_PARTIDA,
    FIN_PARTIDA,
    JUGADOR_DESCONECTADO,
    JUGADOR_RECONECTADO;

    public static TipoEvento desdeTexto(String texto) {
        for (TipoEvento tipo : values()) {
            if (tipo.name().equals(texto)) {
                return tipo;
            }
        }
        return null;
    }
}
