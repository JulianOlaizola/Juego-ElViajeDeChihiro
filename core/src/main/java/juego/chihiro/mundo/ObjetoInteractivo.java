package juego.chihiro.mundo;

import com.badlogic.gdx.math.Rectangle;

public class ObjetoInteractivo {
    private final String nombre;
    private final TipoInteractivo tipo;
    private final Rectangle area;

    public ObjetoInteractivo(String nombre, TipoInteractivo tipo, Rectangle area) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.area = area;
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

    public float getCentroX() {
        return area.x + area.width / 2f;
    }
}
