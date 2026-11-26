package juego.chihiro.servidor.logica;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

import juego.chihiro.mundo.EstadoTina;
import juego.chihiro.mundo.ObjetoEnMano;
import juego.chihiro.mundo.TipoInteractivo;
import juego.chihiro.red.Efecto;
import juego.chihiro.servidor.fisica.MapaServidor;
import juego.chihiro.servidor.fisica.MundoFisico;
import juego.chihiro.servidor.fisica.ObjetoServidor;
import juego.chihiro.utiles.Constantes;

public class Partida implements Disposable {

    private static final int MAXIMO_INTERACCIONES_POR_TICK = 3;

    private final MundoFisico mundo;
    private final JugadorServidor[] jugadores = new JugadorServidor[Constantes.CANTIDAD_JUGADORES];
    private final Array<TinaServidor> tinas = new Array<>();
    private final List<Aviso> avisos = new ArrayList<Aviso>();

    private float tiempoRestante = Constantes.DURACION_NIVEL;
    private float esperaProximoPedido = Constantes.ESPERA_PRIMER_PEDIDO;
    private float esperaAvisoTinasSucias = Constantes.INTERVALO_AVISO_TINAS_SUCIAS;
    private boolean calderaEncendida;
    private float temperaturaCaldera;
    private int pedidosCompletados;
    private int pedidosFallidos;
    private int puntaje;
    private boolean terminada;
    private boolean gano;

    public Partida(MapaServidor mapa, int[] contadoresIniciales) {
        mundo = new MundoFisico(mapa);
        for (int i = 0; i < jugadores.length; i++) {
            jugadores[i] = new JugadorServidor(i + 1);
            jugadores[i].ubicar(mundo, mapa.getAparicion(i + 1));
            jugadores[i].sincronizarContador(contadoresIniciales[i]);
        }
        for (ObjetoServidor objeto : mapa.getInteractivos()) {
            if (objeto.getTipo() == TipoInteractivo.TINA) {
                tinas.add(new TinaServidor(tinas.size + 1, objeto));
            }
        }
    }

    public void aplicarEntrada(int numeroJugador, float ejeX, float ejeY, int contadorInteraccion) {
        JugadorServidor jugador = jugadores[numeroJugador - 1];
        jugador.setEjes(ejeX, ejeY);
        int pendientes = jugador.interaccionesPendientes(contadorInteraccion, MAXIMO_INTERACCIONES_POR_TICK);
        if (terminada) {
            return;
        }
        for (int i = 0; i < pendientes; i++) {
            interactuar(jugador);
        }
    }

    public void actualizar(float delta) {
        if (terminada) {
            for (JugadorServidor jugador : jugadores) {
                jugador.detener();
            }
            return;
        }
        for (JugadorServidor jugador : jugadores) {
            jugador.aplicarMovimiento();
        }
        mundo.avanzar(delta);
        actualizarCaldera(delta);
        actualizarPedidos(delta);
        tiempoRestante -= delta;
        revisarFin();
    }

    private void actualizarCaldera(float delta) {
        if (calderaEncendida && temperaturaCaldera < Constantes.TIEMPO_CALENTAR_CALDERA) {
            temperaturaCaldera = Math.min(Constantes.TIEMPO_CALENTAR_CALDERA, temperaturaCaldera + delta);
        }
    }

    private void actualizarPedidos(float delta) {
        for (TinaServidor tina : tinas) {
            tina.actualizar(delta);
            if (tina.getEstado() == EstadoTina.OCUPADA && tina.getPedido().estaVencido()) {
                tina.ensuciar();
                pedidosFallidos++;
                avisarATodos("Un espíritu se fue enojado y dejó sucia la tina " + tina.getNumero(), Efecto.ERROR);
            }
        }
        esperaProximoPedido -= delta;
        if (esperaProximoPedido <= 0f && pedidosActivos() < Constantes.MAX_PEDIDOS_ACTIVOS) {
            crearPedido();
            esperaProximoPedido = Constantes.ESPERA_ENTRE_PEDIDOS;
        }
        revisarTinasSucias(delta);
    }

    private void revisarTinasSucias(float delta) {
        if (pedidosActivos() > 0 || tinasEn(EstadoTina.LIBRE) > 0) {
            esperaAvisoTinasSucias = 0f;
            return;
        }
        esperaAvisoTinasSucias -= delta;
        if (esperaAvisoTinasSucias <= 0f) {
            avisarATodos("¡Todas las tinas están sucias! Límpienlas para que entren más espíritus", Efecto.ERROR);
            esperaAvisoTinasSucias = Constantes.INTERVALO_AVISO_TINAS_SUCIAS;
        }
    }

    private int tinasEn(EstadoTina estado) {
        int cantidad = 0;
        for (TinaServidor tina : tinas) {
            if (tina.getEstado() == estado) {
                cantidad++;
            }
        }
        return cantidad;
    }

    private void crearPedido() {
        Array<TinaServidor> libres = new Array<>();
        for (TinaServidor tina : tinas) {
            if (tina.getEstado() == EstadoTina.LIBRE) {
                libres.add(tina);
            }
        }
        if (libres.isEmpty()) {
            return;
        }
        TinaServidor elegida = libres.random();
        elegida.recibirEspiritu(new PedidoServidor());
        avisarATodos("Llegó un espíritu a la tina " + elegida.getNumero(), Efecto.NINGUNO);
    }

    private int pedidosActivos() {
        return tinasEn(EstadoTina.OCUPADA);
    }

    private void revisarFin() {
        if (pedidosCompletados >= Constantes.PEDIDOS_PARA_GANAR) {
            terminar(true);
        } else if (pedidosFallidos >= Constantes.FALLOS_PARA_PERDER || tiempoRestante <= 0f) {
            terminar(false);
        }
    }

    private void terminar(boolean ganoLaPartida) {
        terminada = true;
        gano = ganoLaPartida;
        tiempoRestante = Math.max(0f, tiempoRestante);
        for (JugadorServidor jugador : jugadores) {
            jugador.detener();
        }
        avisarATodos(ganoLaPartida ? "¡Turno completado!" : "Se cerró la casa de baños",
            ganoLaPartida ? Efecto.PEDIDO_COMPLETO : Efecto.ERROR);
    }

    public void forzarFin() {
        if (!terminada) {
            tiempoRestante = 0f;
            terminar(false);
        }
    }

    private void interactuar(JugadorServidor jugador) {
        ObjetivoInteraccion objetivo = buscarObjetivo(jugador);
        if (objetivo.companero != null) {
            pasarObjeto(jugador, objetivo.companero);
            return;
        }
        if (objetivo.objeto == null) {
            rechazar(jugador, "No hay nada para usar acá");
            return;
        }
        switch (objetivo.objeto.getTipo()) {
            case ALMACEN_SALES:
                tomarSales(jugador);
                break;
            case CALDERA:
                usarCaldera(jugador);
                break;
            case TINA:
                usarTina(jugador, buscarTina(objetivo.objeto));
                break;
            case DEPOSITO_SUCIEDAD:
                usarDeposito(jugador);
                break;
            default:
                rechazar(jugador, "Eso no se puede usar");
                break;
        }
    }

    private ObjetivoInteraccion buscarObjetivo(JugadorServidor jugador) {
        ObjetivoInteraccion objetivo = new ObjetivoInteraccion();
        mundo.buscarEn(jugador.getZonaInteraccion(), (Fixture fixture) -> {
            Object datos = fixture.getUserData();
            if (datos instanceof JugadorServidor && datos != jugador && objetivo.companero == null) {
                objetivo.companero = (JugadorServidor) datos;
            } else if (datos instanceof ObjetoServidor && objetivo.objeto == null) {
                objetivo.objeto = (ObjetoServidor) datos;
            }
            return true;
        });
        return objetivo;
    }

    private void pasarObjeto(JugadorServidor jugador, JugadorServidor companero) {
        if (!jugador.tieneLasManosLibres() && companero.tieneLasManosLibres()) {
            companero.setObjetoEnMano(jugador.getObjetoEnMano());
            jugador.setObjetoEnMano(ObjetoEnMano.NADA);
            aceptar(jugador, "Le pasaste " + companero.getObjetoEnMano().getEtiqueta().toLowerCase()
                + " al jugador " + companero.getNumero(), Efecto.TOMAR);
            aceptar(companero, "El jugador " + jugador.getNumero() + " te pasó "
                + companero.getObjetoEnMano().getEtiqueta().toLowerCase(), Efecto.TOMAR);
            return;
        }
        if (jugador.tieneLasManosLibres() && !companero.tieneLasManosLibres()) {
            jugador.setObjetoEnMano(companero.getObjetoEnMano());
            companero.setObjetoEnMano(ObjetoEnMano.NADA);
            aceptar(jugador, "Le sacaste " + jugador.getObjetoEnMano().getEtiqueta().toLowerCase()
                + " al jugador " + companero.getNumero(), Efecto.TOMAR);
            aceptar(companero, "El jugador " + jugador.getNumero() + " te sacó lo que llevabas", Efecto.TOMAR);
            return;
        }
        rechazar(jugador, jugador.tieneLasManosLibres()
            ? "Ninguno de los dos tiene algo para pasar"
            : "Los dos tienen las manos ocupadas");
    }

    private void tomarSales(JugadorServidor jugador) {
        if (!jugador.tieneLasManosLibres()) {
            rechazar(jugador, "Tenés las manos ocupadas");
            return;
        }
        jugador.setObjetoEnMano(ObjetoEnMano.SAL);
        aceptar(jugador, "Tomaste sales aromáticas", Efecto.TOMAR);
    }

    private void usarCaldera(JugadorServidor jugador) {
        if (!calderaEncendida) {
            calderaEncendida = true;
            temperaturaCaldera = 0f;
            aceptar(jugador, "Encendiste la caldera", Efecto.TOMAR);
            return;
        }
        if (!estaElAguaCaliente()) {
            rechazar(jugador, "El agua todavía está fría");
            return;
        }
        if (!jugador.tieneLasManosLibres()) {
            rechazar(jugador, "Tenés las manos ocupadas");
            return;
        }
        jugador.setObjetoEnMano(ObjetoEnMano.BALDE_AGUA);
        aceptar(jugador, "Llenaste un balde de agua caliente", Efecto.TOMAR);
    }

    private void usarTina(JugadorServidor jugador, TinaServidor tina) {
        if (tina == null) {
            rechazar(jugador, "Esa tina no está registrada");
            return;
        }
        if (tina.getEstado() == EstadoTina.SUCIA) {
            if (!jugador.tieneLasManosLibres()) {
                rechazar(jugador, "Necesitás las manos libres para limpiar");
                return;
            }
            jugador.setObjetoEnMano(ObjetoEnMano.SUCIEDAD);
            tina.limpiar();
            aceptar(jugador, "Limpiaste la tina", Efecto.LIMPIAR);
            return;
        }
        if (tina.getEstado() != EstadoTina.OCUPADA) {
            rechazar(jugador, "Esta tina no tiene ningún pedido");
            return;
        }
        PedidoServidor pedido = tina.getPedido();
        if (!pedido.necesita(jugador.getObjetoEnMano())) {
            rechazar(jugador, "Este pedido no necesita eso");
            return;
        }
        pedido.entregar(jugador.getObjetoEnMano());
        jugador.setObjetoEnMano(ObjetoEnMano.NADA);
        if (pedido.estaCompleto()) {
            completar(tina);
            aceptar(jugador, "¡Espíritu satisfecho! La tina " + tina.getNumero() + " quedó sucia: limpiala",
                Efecto.PEDIDO_COMPLETO);
        } else {
            aceptar(jugador, "Entregado, falta lo otro", Efecto.ENTREGAR);
        }
    }

    private void completar(TinaServidor tina) {
        float bonus = tina.getPedido().getProporcionRestante();
        puntaje += Constantes.PUNTOS_POR_PEDIDO + Math.round(bonus * Constantes.PUNTOS_POR_PEDIDO);
        pedidosCompletados++;
        tina.ensuciar();
    }

    private void usarDeposito(JugadorServidor jugador) {
        if (jugador.tieneLasManosLibres()) {
            rechazar(jugador, "No tenés nada para tirar");
            return;
        }
        jugador.setObjetoEnMano(ObjetoEnMano.NADA);
        aceptar(jugador, "Tiraste lo que llevabas", Efecto.LIMPIAR);
    }

    private TinaServidor buscarTina(ObjetoServidor objeto) {
        for (TinaServidor tina : tinas) {
            if (tina.getObjeto() == objeto) {
                return tina;
            }
        }
        return null;
    }

    private void aceptar(JugadorServidor jugador, String texto, Efecto efecto) {
        avisos.add(new Aviso(jugador.getNumero(), texto, efecto));
    }

    private void rechazar(JugadorServidor jugador, String texto) {
        avisos.add(new Aviso(jugador.getNumero(), texto, Efecto.ERROR));
    }

    private void avisarATodos(String texto, Efecto efecto) {
        avisos.add(new Aviso(0, texto, efecto));
    }

    public List<Aviso> retirarAvisos() {
        List<Aviso> retirados = new ArrayList<Aviso>(avisos);
        avisos.clear();
        return retirados;
    }

    public boolean estaElAguaCaliente() {
        return calderaEncendida && temperaturaCaldera >= Constantes.TIEMPO_CALENTAR_CALDERA;
    }

    public boolean estaLaCalderaEncendida() {
        return calderaEncendida;
    }

    public float getProporcionCaldera() {
        return MathUtils.clamp(temperaturaCaldera / Constantes.TIEMPO_CALENTAR_CALDERA, 0f, 1f);
    }

    public JugadorServidor getJugador(int numero) {
        return jugadores[numero - 1];
    }

    public Array<TinaServidor> getTinas() {
        return tinas;
    }

    public float getTiempoRestante() {
        return tiempoRestante;
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

    public boolean estaTerminada() {
        return terminada;
    }

    public boolean gano() {
        return gano;
    }

    public MundoFisico getMundo() {
        return mundo;
    }

    @Override
    public void dispose() {
        mundo.dispose();
    }

    private static final class ObjetivoInteraccion {
        private JugadorServidor companero;
        private ObjetoServidor objeto;
    }
}
