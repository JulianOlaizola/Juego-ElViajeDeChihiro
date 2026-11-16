package juego.chihiro.pantallas;

import java.net.SocketException;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import juego.chihiro.Main;
import juego.chihiro.entradas.Accion;
import juego.chihiro.red.BuscadorServidores;
import juego.chihiro.red.Protocolo;
import juego.chihiro.red.ServidorEncontrado;

public class PantallaBuscarServidores extends PantallaBase {
    private static final int MAXIMO_VISIBLES = 6;

    private BuscadorServidores buscador;
    private String error = "";
    private int seleccion;

    public PantallaBuscarServidores(Main juego) {
        super(juego);
        try {
            buscador = new BuscadorServidores(Protocolo.PUERTO_POR_DEFECTO);
        } catch (SocketException e) {
            error = "No se pudo abrir la red: " + e.getMessage();
        }
    }

    @Override
    public void render(float delta) {
        juego.atenderControlesDeAudio();
        List<ServidorEncontrado> servidores = buscador == null ? List.of() : actualizarBusqueda();

        if (entradas.consumir(Accion.VOLVER)) {
            juego.cambiarPantalla(new PantallaMenu(juego));
            return;
        }
        if (!servidores.isEmpty()) {
            if (entradas.consumir(Accion.SUBIR)) {
                seleccion = (seleccion + servidores.size() - 1) % servidores.size();
            }
            if (entradas.consumir(Accion.BAJAR)) {
                seleccion = (seleccion + 1) % servidores.size();
            }
            seleccion = Math.min(seleccion, servidores.size() - 1);
            if (entradas.consumir(Accion.CONFIRMAR)) {
                ServidorEncontrado elegido = servidores.get(seleccion);
                juego.cambiarPantalla(new PantallaConexion(juego, elegido.getDireccion(), elegido.getNombre()));
                return;
            }
        }
        entradas.limpiarAcciones();
        dibujar(servidores);
    }

    private List<ServidorEncontrado> actualizarBusqueda() {
        buscador.actualizar();
        return buscador.getEncontrados();
    }

    private void dibujar(List<ServidorEncontrado> servidores) {
        SpriteBatch batch = comenzarDibujo();
        titulo(batch, "Buscar partida en la red");

        if (!error.isEmpty()) {
            linea(batch, recursos.getFuenteMedia(), error, 300f, Color.RED);
        } else if (servidores.isEmpty()) {
            String puntos = ".".repeat(buscador.getBusquedasRealizadas() % 4);
            linea(batch, recursos.getFuenteMedia(), "Buscando servidores en la red local" + puntos, 300f, Color.WHITE);
            linea(batch, recursos.getFuenteChica(),
                "Si no aparece, revisá que el servidor esté abierto y que el firewall permita Java en redes privadas.",
                250f, Color.LIGHT_GRAY);
        } else {
            float y = 400f;
            for (int i = 0; i < servidores.size() && i < MAXIMO_VISIBLES; i++) {
                ServidorEncontrado servidor = servidores.get(i);
                boolean elegido = i == seleccion;
                Color color = servidor.estaLleno() ? Color.GRAY : (elegido ? Color.GOLD : Color.WHITE);
                linea(batch, recursos.getFuenteMedia(), (elegido ? "> " : "   ") + servidor.describir(), y, color);
                y -= 44f;
            }
        }
        pie(batch, "W/S o flechas para elegir   -   ENTER para conectarse   -   ESC para volver");
        batch.end();
    }

    @Override
    public void dispose() {
        if (buscador != null) {
            buscador.cerrar();
        }
    }
}
