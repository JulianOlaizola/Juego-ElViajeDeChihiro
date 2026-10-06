package juego.chihiro.mundo;

public enum ObjetoEnMano {
    NADA(3, "Nada"),
    SAL(0, "Sales"),
    BALDE_AGUA(1, "Balde de agua"),
    SUCIEDAD(2, "Suciedad");

    private final int indiceRegion;
    private final String etiqueta;

    ObjetoEnMano(int indiceRegion, String etiqueta) {
        this.indiceRegion = indiceRegion;
        this.etiqueta = etiqueta;
    }

    public int getIndiceRegion() {
        return indiceRegion;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
