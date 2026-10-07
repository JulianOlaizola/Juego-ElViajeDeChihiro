package juego.chihiro.mundo;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import juego.chihiro.entidades.Espiritu;

public class Tina {
    private final ObjetoInteractivo objeto;
    private EstadoTina estado = EstadoTina.LIBRE;
    private Pedido pedido;
    private Espiritu espiritu;

    public Tina(ObjetoInteractivo objeto) {
        this.objeto = objeto;
    }

    public void actualizar(float delta) {
        if (estado == EstadoTina.OCUPADA) {
            pedido.actualizar(delta);
            espiritu.actualizar(delta);
        }
    }

    public void dibujar(SpriteBatch batch) {
        if (espiritu != null) {
            espiritu.dibujar(batch);
        }
    }

    public void recibirEspiritu(Pedido pedido, Espiritu espiritu) {
        this.pedido = pedido;
        this.espiritu = espiritu;
        this.estado = EstadoTina.OCUPADA;
    }

    public void ensuciar() {
        this.pedido = null;
        this.espiritu = null;
        this.estado = EstadoTina.SUCIA;
    }

    public void limpiar() {
        this.estado = EstadoTina.LIBRE;
    }

    public ObjetoInteractivo getObjeto() {
        return objeto;
    }

    public EstadoTina getEstado() {
        return estado;
    }

    public Pedido getPedido() {
        return pedido;
    }
}
