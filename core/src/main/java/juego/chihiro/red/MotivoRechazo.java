package juego.chihiro.red;

public enum MotivoRechazo {
    SALA_LLENA("La partida ya tiene dos jugadores"),
    PARTIDA_EN_CURSO("La partida ya empezó y no hay lugar libre"),
    SESION_DESCONOCIDA("El servidor ya no reconoce esta sesión (puede que se haya reiniciado)");

    private final String descripcion;

    MotivoRechazo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public static MotivoRechazo desdeTexto(String texto) {
        for (MotivoRechazo motivo : values()) {
            if (motivo.name().equals(texto)) {
                return motivo;
            }
        }
        return null;
    }
}
