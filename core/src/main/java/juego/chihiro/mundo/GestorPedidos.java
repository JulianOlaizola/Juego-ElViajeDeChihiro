package juego.chihiro.mundo;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

import juego.chihiro.entidades.Espiritu;
import juego.chihiro.utiles.Constantes;
import juego.chihiro.utiles.Recursos;

public class GestorPedidos {
    private final Recursos recursos;
    private final Array<Tina> tinas = new Array<>();

    private float esperaProximoPedido = Constantes.ESPERA_PRIMER_PEDIDO;
    private int pedidosCompletados;
    private int pedidosFallidos;
    private int puntaje;

    public GestorPedidos(MapaCasaBanos mapa, Recursos recursos) {
        this.recursos = recursos;

        for (ObjetoInteractivo objeto : mapa.getInteractivos()) {
            if (objeto.getTipo() == TipoInteractivo.TINA) {
                tinas.add(new Tina(objeto));
            }
        }
    }

    public void actualizar(float delta) {
        for (Tina tina : tinas) {
            tina.actualizar(delta);
            if (tina.getEstado() == EstadoTina.OCUPADA && tina.getPedido().estaVencido()) {
                tina.ensuciar();
                pedidosFallidos++;
            }
        }

        esperaProximoPedido -= delta;
        if (esperaProximoPedido <= 0f && pedidosActivos() < Constantes.MAX_PEDIDOS_ACTIVOS) {
            crearPedido();
            esperaProximoPedido = Constantes.ESPERA_ENTRE_PEDIDOS;
        }
    }

    private void crearPedido() {
        Array<Tina> libres = new Array<>();
        for (Tina tina : tinas) {
            if (tina.getEstado() == EstadoTina.LIBRE) {
                libres.add(tina);
            }
        }
        if (libres.isEmpty()) {
            return;
        }

        Tina elegida = libres.random();

        float x = elegida.getObjeto().getCentroX() - Constantes.TAM_TILE / 2f;
        float y = elegida.getObjeto().getArea().y + elegida.getObjeto().getArea().height
            - Constantes.SOLAPE_ESPIRITU_EN_TINA;
        elegida.recibirEspiritu(new Pedido(), new Espiritu(x, y, recursos));
    }

    public void completar(Tina tina) {
        float bonus = tina.getPedido().getProporcionRestante();
        puntaje += Constantes.PUNTOS_POR_PEDIDO + Math.round(bonus * Constantes.PUNTOS_POR_PEDIDO);
        pedidosCompletados++;
        tina.ensuciar();
    }

    public Tina buscarTina(ObjetoInteractivo objeto) {
        for (Tina tina : tinas) {
            if (tina.getObjeto() == objeto) {
                return tina;
            }
        }
        return null;
    }

    public int pedidosActivos() {
        int activos = 0;
        for (Tina tina : tinas) {
            if (tina.getEstado() == EstadoTina.OCUPADA) {
                activos++;
            }
        }
        return activos;
    }

    public void dibujar(SpriteBatch batch) {
        for (Tina tina : tinas) {
            tina.dibujar(batch);
        }
    }

    public Array<Tina> getTinas() {
        return tinas;
    }

    public int getPedidosCompletados() {
        return pedidosCompletados;
    }

    public int getPedidosFallidos() {
        return pedidosFallidos;
    }

    public int getPuntaje() {
        return puntaje;
    }
}
