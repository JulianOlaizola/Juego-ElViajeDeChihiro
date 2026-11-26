package juego.chihiro.mundo;

public enum Nivel {
    SALON(1, "El salón principal", "mapas/salon.tmx", 180f, 5, 3, 2, 6f,
        new TipoEspiritu[] {TipoEspiritu.COMUN, TipoEspiritu.COMUN, TipoEspiritu.BOSQUE}),
    ALA_ESTE(2, "Los baños del ala este", "mapas/nivel2.tmx", 210f, 7, 3, 3, 5f,
        new TipoEspiritu[] {TipoEspiritu.COMUN, TipoEspiritu.BOSQUE, TipoEspiritu.LLUVIA}),
    GRAN_BANO(3, "La noche del gran baño", "mapas/nivel3.tmx", 240f, 9, 4, 3, 4.5f,
        new TipoEspiritu[] {TipoEspiritu.COMUN, TipoEspiritu.BOSQUE, TipoEspiritu.LLUVIA, TipoEspiritu.LLUVIA});

    private final int numero;
    private final String nombre;
    private final String rutaMapa;
    private final float duracion;
    private final int pedidosParaGanar;
    private final int fallosParaPerder;
    private final int maximoPedidosActivos;
    private final float esperaEntrePedidos;
    private final TipoEspiritu[] tiposDePedido;

    Nivel(int numero, String nombre, String rutaMapa, float duracion, int pedidosParaGanar,
          int fallosParaPerder, int maximoPedidosActivos, float esperaEntrePedidos, TipoEspiritu[] tiposDePedido) {
        this.numero = numero;
        this.nombre = nombre;
        this.rutaMapa = rutaMapa;
        this.duracion = duracion;
        this.pedidosParaGanar = pedidosParaGanar;
        this.fallosParaPerder = fallosParaPerder;
        this.maximoPedidosActivos = maximoPedidosActivos;
        this.esperaEntrePedidos = esperaEntrePedidos;
        this.tiposDePedido = tiposDePedido;
    }

    public Nivel siguiente() {
        return esElUltimo() ? null : values()[ordinal() + 1];
    }

    public boolean esElUltimo() {
        return ordinal() == values().length - 1;
    }

    public int getNumero() {
        return numero;
    }

    public String getNombre() {
        return nombre;
    }

    public String getRutaMapa() {
        return rutaMapa;
    }

    public float getDuracion() {
        return duracion;
    }

    public int getPedidosParaGanar() {
        return pedidosParaGanar;
    }

    public int getFallosParaPerder() {
        return fallosParaPerder;
    }

    public int getMaximoPedidosActivos() {
        return maximoPedidosActivos;
    }

    public float getEsperaEntrePedidos() {
        return esperaEntrePedidos;
    }

    public TipoEspiritu tipoAlAzar(float azar) {
        int indice = Math.min(tiposDePedido.length - 1, (int) (azar * tiposDePedido.length));
        return tiposDePedido[indice];
    }
}
