package juego.chihiro.servidor.logica;

import juego.chihiro.mundo.EstadoTina;
import juego.chihiro.servidor.fisica.ObjetoServidor;

public class TinaServidor {

    private final int numero;
    private final ObjetoServidor objeto;
    private EstadoTina estado = EstadoTina.LIBRE;
    private PedidoServidor pedido;

    public TinaServidor(int numero, ObjetoServidor objeto) {
        this.numero = numero;
        this.objeto = objeto;
    }

    public void actualizar(float delta) {
        if (estado == EstadoTina.OCUPADA) {
            pedido.actualizar(delta);
        }
    }

    public void recibirEspiritu(PedidoServidor nuevoPedido) {
        pedido = nuevoPedido;
        estado = EstadoTina.OCUPADA;
    }

    public void ensuciar() {
        pedido = null;
        estado = EstadoTina.SUCIA;
    }

    public void limpiar() {
        estado = EstadoTina.LIBRE;
    }

    public int getNumero() {
        return numero;
    }

    public ObjetoServidor getObjeto() {
        return objeto;
    }

    public EstadoTina getEstado() {
        return estado;
    }

    public PedidoServidor getPedido() {
        return pedido;
    }
}
