package juego.chihiro.red;

public enum TipoMensaje {
    PING,
    PONG,
    DESCUBRIR,
    ANUNCIO;

    public static TipoMensaje desdeTexto(String texto) {
        for (TipoMensaje tipo : values()) {
            if (tipo.name().equals(texto)) {
                return tipo;
            }
        }
        return null;
    }
}
