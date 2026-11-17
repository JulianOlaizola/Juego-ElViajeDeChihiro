package juego.chihiro.servidor.sesiones;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import juego.chihiro.red.EventoRed;
import juego.chihiro.red.Mensaje;
import juego.chihiro.red.Protocolo;
import juego.chihiro.red.TipoEvento;

public class CanalConfiable {

    private final Map<Long, Pendiente> pendientes = new LinkedHashMap<Long, Pendiente>();
    private long proximoId = 1;
    private long reenvios;

    public Mensaje encolar(TipoEvento tipo, String... datos) {
        EventoRed evento = new EventoRed(proximoId++, tipo, datos);
        Mensaje mensaje = evento.aMensaje();
        pendientes.put(evento.getId(), new Pendiente(mensaje));
        return mensaje;
    }

    public void confirmar(long id) {
        pendientes.remove(id);
    }

    public List<Mensaje> paraReenviar(long ahora) {
        List<Mensaje> vencidos = new ArrayList<Mensaje>();
        for (Pendiente pendiente : pendientes.values()) {
            if (ahora - pendiente.ultimoEnvioMilis >= Protocolo.INTERVALO_REENVIO_MILIS) {
                pendiente.ultimoEnvioMilis = ahora;
                vencidos.add(pendiente.mensaje);
                reenvios++;
            }
        }
        return vencidos;
    }

    public void reiniciar() {
        pendientes.clear();
        proximoId = 1;
    }

    public int getPendientes() {
        return pendientes.size();
    }

    public long getReenvios() {
        return reenvios;
    }

    private static final class Pendiente {
        private final Mensaje mensaje;
        private long ultimoEnvioMilis;

        private Pendiente(Mensaje mensaje) {
            this.mensaje = mensaje;
            this.ultimoEnvioMilis = System.currentTimeMillis();
        }
    }
}
