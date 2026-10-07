package juego.chihiro.utiles;

public final class Constantes {
    private Constantes() {
    }

    public static final float ANCHO_VISTA = 960f;
    public static final float ALTO_VISTA = 540f;

    public static final String RUTA_MAPA = "mapas/salon.tmx";
    public static final int TAM_TILE = 32;
    public static final String CAPA_PISO = "piso";
    public static final String CAPA_DECORACION = "decoracion";
    public static final String CAPA_COLISIONES = "colisiones";
    public static final String CAPA_INTERACTIVOS = "interactivos";
    public static final String PROPIEDAD_TIPO = "tipo";

    public static final String RUTA_JUGADORES = "imagenes/jugadores.png";
    public static final String RUTA_ESPIRITUS = "imagenes/espiritus.png";
    public static final String RUTA_OBJETOS = "imagenes/objetos.png";
    public static final String RUTA_LOGO = "imagenes/logo.png";
    public static final String RUTA_PIXEL = "imagenes/pixel.png";

    public static final String RUTA_MUSICA = "audio/musica_salon.ogg";
    public static final String RUTA_TOMAR = "audio/tomar.wav";
    public static final String RUTA_ENTREGAR = "audio/entregar.wav";
    public static final String RUTA_PEDIDO_COMPLETO = "audio/pedido_completo.wav";
    public static final String RUTA_ERROR = "audio/error.wav";
    public static final String RUTA_LIMPIAR = "audio/limpiar.wav";

    public static final String RUTA_FUENTE = "fuentes/fuente.ttf";
    public static final int TAM_FUENTE_CHICA = 15;
    public static final int TAM_FUENTE_MEDIA = 22;
    public static final int TAM_FUENTE_GRANDE = 40;

    public static final int CANTIDAD_JUGADORES = 2;
    public static final int CUADROS_POR_FILA = 4;
    public static final int DIRECCIONES = 4;
    public static final float DURACION_CUADRO = 0.12f;

    public static final float VELOCIDAD_JUGADOR = 150f;
    public static final float ANCHO_HITBOX = 18f;
    public static final float ALTO_HITBOX = 12f;
    public static final float ALCANCE_INTERACCION = 20f;
    public static final float MARGEN_ZONA_INTERACCION = 8f;
    public static final float DESFASE_HITBOX_Y = 3f;

    public static final float SUAVIZADO_CAMARA = 5f;

    public static final float DURACION_NIVEL = 180f;
    public static final int PEDIDOS_PARA_GANAR = 5;
    public static final int FALLOS_PARA_PERDER = 3;
    public static final float TIEMPO_PEDIDO = 45f;
    public static final int MAX_PEDIDOS_ACTIVOS = 2;
    public static final float ESPERA_PRIMER_PEDIDO = 2f;
    public static final float ESPERA_ENTRE_PEDIDOS = 6f;
    public static final float SOLAPE_ESPIRITU_EN_TINA = 8f;
    public static final float TIEMPO_CALENTAR_CALDERA = 3f;
    public static final int PUNTOS_POR_PEDIDO = 100;
    public static final float DURACION_MENSAJE = 2.5f;

    public static final String ARCHIVO_PREFERENCIAS = "chihiro-preferencias";
    public static final float VOLUMEN_MUSICA_INICIAL = 0.55f;
    public static final float VOLUMEN_EFECTOS_INICIAL = 0.8f;
    public static final float PASO_VOLUMEN = 0.1f;
}
