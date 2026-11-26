package juego.chihiro.mundo;

public enum TipoEspiritu {
    COMUN("Espíritu común", 0, 100, 45f, true, true, false),
    BOSQUE("Espíritu del bosque", 1, 130, 50f, false, true, true),
    LLUVIA("Espíritu de la lluvia", 2, 180, 60f, true, true, true);

    private final String nombre;
    private final int fila;
    private final int puntos;
    private final float tiempo;
    private final boolean necesitaSal;
    private final boolean necesitaAgua;
    private final boolean necesitaHierbas;

    TipoEspiritu(String nombre, int fila, int puntos, float tiempo,
                 boolean necesitaSal, boolean necesitaAgua, boolean necesitaHierbas) {
        this.nombre = nombre;
        this.fila = fila;
        this.puntos = puntos;
        this.tiempo = tiempo;
        this.necesitaSal = necesitaSal;
        this.necesitaAgua = necesitaAgua;
        this.necesitaHierbas = necesitaHierbas;
    }

    public String getNombre() {
        return nombre;
    }

    public int getFila() {
        return fila;
    }

    public int getPuntos() {
        return puntos;
    }

    public float getTiempo() {
        return tiempo;
    }

    public boolean necesitaSal() {
        return necesitaSal;
    }

    public boolean necesitaAgua() {
        return necesitaAgua;
    }

    public boolean necesitaHierbas() {
        return necesitaHierbas;
    }
}
