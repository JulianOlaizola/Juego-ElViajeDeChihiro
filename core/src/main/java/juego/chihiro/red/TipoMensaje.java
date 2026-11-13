package juego.chihiro.red;

public enum TipoMensaje {
    PING,
    PONG;

    public static TipoMensaje desdeTexto(String texto) {
        for (TipoMensaje tipo : values()) {
            if (tipo.name().equals(texto)) {
                return tipo;
            }
        }
        return null;
    }
}
