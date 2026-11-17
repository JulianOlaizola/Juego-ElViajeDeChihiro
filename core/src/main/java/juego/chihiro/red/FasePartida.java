package juego.chihiro.red;

public enum FasePartida {
    SALA,
    JUGANDO,
    PAUSA,
    ESPERANDO_JUGADOR,
    TERMINADA;

    public static FasePartida desdeTexto(String texto) {
        for (FasePartida fase : values()) {
            if (fase.name().equals(texto)) {
                return fase;
            }
        }
        return null;
    }
}
