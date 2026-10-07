package juego.chihiro.mundo;

import juego.chihiro.audio.AdministradorAudio;
import juego.chihiro.entidades.Jugador;
import juego.chihiro.utiles.Constantes;

public class GestorInteracciones {
    private final MapaCasaBanos mapa;
    private final GestorPedidos gestorPedidos;
    private final AdministradorAudio audio;

    private boolean calderaEncendida;
    private float temperaturaCaldera;

    private String mensaje = "";
    private float tiempoMensaje;

    public GestorInteracciones(MapaCasaBanos mapa, GestorPedidos gestorPedidos,
                               AdministradorAudio audio) {
        this.mapa = mapa;
        this.gestorPedidos = gestorPedidos;
        this.audio = audio;
    }

    public void actualizar(float delta) {
        if (calderaEncendida && temperaturaCaldera < Constantes.TIEMPO_CALENTAR_CALDERA) {
            temperaturaCaldera = Math.min(Constantes.TIEMPO_CALENTAR_CALDERA,
                                          temperaturaCaldera + delta);
        }
        if (tiempoMensaje > 0f) {
            tiempoMensaje -= delta;
        }
    }

    public void interactuar(Jugador jugador) {
        ObjetoInteractivo objeto = mapa.objetoEn(jugador.getZonaInteraccion());
        if (objeto == null) {
            rechazar("No hay nada para usar acá");
            return;
        }

        switch (objeto.getTipo()) {
            case ALMACEN_SALES:
                tomarSales(jugador);
                break;
            case CALDERA:
                usarCaldera(jugador);
                break;
            case TINA:
                usarTina(jugador, objeto);
                break;
            case DEPOSITO_SUCIEDAD:
                usarDeposito(jugador);
                break;
            default:
                rechazar("Eso no se puede usar");
                break;
        }
    }

    private void tomarSales(Jugador jugador) {
        if (!jugador.tieneLasManosLibres()) {
            rechazar("Tenés las manos ocupadas");
            return;
        }
        jugador.setObjetoEnMano(ObjetoEnMano.SAL);
        aceptar("Tomaste sales aromáticas", AdministradorAudio.Efecto.TOMAR);
    }

    private void usarCaldera(Jugador jugador) {
        if (!calderaEncendida) {
            calderaEncendida = true;
            temperaturaCaldera = 0f;
            aceptar("Encendiste la caldera", AdministradorAudio.Efecto.TOMAR);
            return;
        }
        if (!estaElAguaCaliente()) {
            rechazar("El agua todavía está fría");
            return;
        }
        if (!jugador.tieneLasManosLibres()) {
            rechazar("Tenés las manos ocupadas");
            return;
        }
        jugador.setObjetoEnMano(ObjetoEnMano.BALDE_AGUA);
        aceptar("Llenaste un balde de agua caliente", AdministradorAudio.Efecto.TOMAR);
    }

    private void usarTina(Jugador jugador, ObjetoInteractivo objeto) {
        Tina tina = gestorPedidos.buscarTina(objeto);
        if (tina == null) {
            rechazar("Esa tina no está registrada");
            return;
        }

        if (tina.getEstado() == EstadoTina.SUCIA) {
            if (!jugador.tieneLasManosLibres()) {
                rechazar("Necesitás las manos libres para limpiar");
                return;
            }
            jugador.setObjetoEnMano(ObjetoEnMano.SUCIEDAD);
            tina.limpiar();
            aceptar("Limpiaste la tina", AdministradorAudio.Efecto.LIMPIAR);
            return;
        }

        if (tina.getEstado() != EstadoTina.OCUPADA) {
            rechazar("Esta tina no tiene ningún pedido");
            return;
        }

        Pedido pedido = tina.getPedido();
        if (!pedido.necesita(jugador.getObjetoEnMano())) {
            rechazar("Este pedido no necesita eso");
            return;
        }

        pedido.entregar(jugador.getObjetoEnMano());
        jugador.setObjetoEnMano(ObjetoEnMano.NADA);

        if (pedido.estaCompleto()) {
            gestorPedidos.completar(tina);
            aceptar("¡Espíritu satisfecho!", AdministradorAudio.Efecto.PEDIDO_COMPLETO);
        } else {
            aceptar("Entregado, falta lo otro", AdministradorAudio.Efecto.ENTREGAR);
        }
    }

    private void usarDeposito(Jugador jugador) {
        if (jugador.tieneLasManosLibres()) {
            rechazar("No tenés nada para tirar");
            return;
        }
        jugador.setObjetoEnMano(ObjetoEnMano.NADA);
        aceptar("Tiraste lo que llevabas", AdministradorAudio.Efecto.LIMPIAR);
    }

    private void aceptar(String texto, AdministradorAudio.Efecto efecto) {
        mensaje = texto;
        tiempoMensaje = Constantes.DURACION_MENSAJE;
        audio.reproducir(efecto);
    }

    private void rechazar(String texto) {
        mensaje = texto;
        tiempoMensaje = Constantes.DURACION_MENSAJE;
        audio.reproducir(AdministradorAudio.Efecto.ERROR);
    }

    public boolean estaElAguaCaliente() {
        return calderaEncendida && temperaturaCaldera >= Constantes.TIEMPO_CALENTAR_CALDERA;
    }

    public boolean estaLaCalderaEncendida() {
        return calderaEncendida;
    }

    public float getProporcionCaldera() {
        return Math.min(1f, temperaturaCaldera / Constantes.TIEMPO_CALENTAR_CALDERA);
    }

    public String getMensaje() {
        return tiempoMensaje > 0f ? mensaje : "";
    }
}
