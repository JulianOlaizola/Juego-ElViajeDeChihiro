package juego.chihiro.servidor.logica;

import juego.chihiro.mundo.ObjetoEnMano;
import juego.chihiro.mundo.TipoEspiritu;

public class PedidoServidor {

    private final TipoEspiritu tipo;
    private boolean faltaSal;
    private boolean faltaAgua;
    private boolean faltaHierbas;
    private float tiempoRestante;

    public PedidoServidor(TipoEspiritu tipo) {
        this.tipo = tipo;
        this.faltaSal = tipo.necesitaSal();
        this.faltaAgua = tipo.necesitaAgua();
        this.faltaHierbas = tipo.necesitaHierbas();
        this.tiempoRestante = tipo.getTiempo();
    }

    public void actualizar(float delta) {
        tiempoRestante -= delta;
    }

    public boolean estaVencido() {
        return tiempoRestante <= 0f;
    }

    public boolean estaCompleto() {
        return !faltaSal && !faltaAgua && !faltaHierbas;
    }

    public boolean necesita(ObjetoEnMano objeto) {
        switch (objeto) {
            case SAL:
                return faltaSal;
            case BALDE_AGUA:
                return faltaAgua;
            case HIERBAS:
                return faltaHierbas;
            default:
                return false;
        }
    }

    public void entregar(ObjetoEnMano objeto) {
        switch (objeto) {
            case SAL:
                faltaSal = false;
                break;
            case BALDE_AGUA:
                faltaAgua = false;
                break;
            case HIERBAS:
                faltaHierbas = false;
                break;
            default:
                break;
        }
    }

    public String describirFaltantes() {
        StringBuilder texto = new StringBuilder();
        if (faltaSal) {
            texto.append("sales");
        }
        if (faltaAgua) {
            texto.append(texto.length() > 0 ? ", " : "").append("agua caliente");
        }
        if (faltaHierbas) {
            texto.append(texto.length() > 0 ? ", " : "").append("hierbas");
        }
        return texto.toString();
    }

    public TipoEspiritu getTipo() {
        return tipo;
    }

    public boolean faltaSal() {
        return faltaSal;
    }

    public boolean faltaAgua() {
        return faltaAgua;
    }

    public boolean faltaHierbas() {
        return faltaHierbas;
    }

    public float getProporcionRestante() {
        return Math.max(0f, tiempoRestante / tipo.getTiempo());
    }
}
