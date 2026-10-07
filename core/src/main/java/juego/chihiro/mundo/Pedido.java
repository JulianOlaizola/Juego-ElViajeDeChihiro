package juego.chihiro.mundo;

import juego.chihiro.utiles.Constantes;

public class Pedido {
    private boolean salEntregada;
    private boolean aguaEntregada;
    private float tiempoRestante = Constantes.TIEMPO_PEDIDO;

    public void actualizar(float delta) {
        tiempoRestante -= delta;
    }

    public boolean estaVencido() {
        return tiempoRestante <= 0f;
    }

    public boolean estaCompleto() {
        return salEntregada && aguaEntregada;
    }

    public boolean necesita(ObjetoEnMano objeto) {
        if (objeto == ObjetoEnMano.SAL) {
            return !salEntregada;
        }
        if (objeto == ObjetoEnMano.BALDE_AGUA) {
            return !aguaEntregada;
        }
        return false;
    }

    public void entregar(ObjetoEnMano objeto) {
        if (objeto == ObjetoEnMano.SAL) {
            salEntregada = true;
        } else if (objeto == ObjetoEnMano.BALDE_AGUA) {
            aguaEntregada = true;
        }
    }

    public boolean faltaSal() {
        return !salEntregada;
    }

    public boolean faltaAgua() {
        return !aguaEntregada;
    }

    public float getProporcionRestante() {
        return Math.max(0f, tiempoRestante / Constantes.TIEMPO_PEDIDO);
    }
}
