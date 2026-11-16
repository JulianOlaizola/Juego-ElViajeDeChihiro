package juego.chihiro.servidor.fisica;

import com.badlogic.gdx.math.Rectangle;

import juego.chihiro.mundo.TipoInteractivo;

public class ObjetoServidor {

    private final int indice;
    private final String nombre;
    private final TipoInteractivo tipo;
    private final Rectangle area;

    public ObjetoServidor(int indice, String nombre, TipoInteractivo tipo, Rectangle area) {
        this.indice = indice;
        this.nombre = nombre;
        this.tipo = tipo;
        this.area = area;
    }

    public int getIndice() {
        return indice;
    }

    public String getNombre() {
        return nombre;
    }

    public TipoInteractivo getTipo() {
        return tipo;
    }

    public Rectangle getArea() {
        return area;
    }
}
