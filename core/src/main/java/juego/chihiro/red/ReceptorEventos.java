package juego.chihiro.red;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;

public class ReceptorEventos {

    private static final long VENTANA_DE_IDS = 512;

    private final Set<Long> procesados = new HashSet<Long>();
    private final Queue<EventoRed> pendientes = new ArrayDeque<EventoRed>();
    private long mayorId;
    private long duplicados;

    public boolean recibir(EventoRed evento) {
        if (procesados.contains(evento.getId()) || evento.getId() <= mayorId - VENTANA_DE_IDS) {
            duplicados++;
            return false;
        }
        procesados.add(evento.getId());
        if (evento.getId() > mayorId) {
            mayorId = evento.getId();
            podar();
        }
        pendientes.add(evento);
        return true;
    }

    private void podar() {
        Iterator<Long> iterador = procesados.iterator();
        while (iterador.hasNext()) {
            if (iterador.next() <= mayorId - VENTANA_DE_IDS) {
                iterador.remove();
            }
        }
    }

    public EventoRed siguiente() {
        return pendientes.poll();
    }

    public void reiniciar() {
        procesados.clear();
        pendientes.clear();
        mayorId = 0;
    }

    public long getDuplicados() {
        return duplicados;
    }
}
