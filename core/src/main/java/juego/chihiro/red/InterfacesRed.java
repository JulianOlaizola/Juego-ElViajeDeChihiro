package juego.chihiro.red;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public final class InterfacesRed {

    private InterfacesRed() {
    }

    public static List<String> direccionesLocales() throws SocketException {
        List<String> direcciones = new ArrayList<String>();
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        if (interfaces == null) {
            return direcciones;
        }
        for (NetworkInterface interfaz : Collections.list(interfaces)) {
            if (!interfaz.isUp() || interfaz.isLoopback() || interfaz.isVirtual()) {
                continue;
            }
            for (InetAddress direccion : Collections.list(interfaz.getInetAddresses())) {
                if (direccion instanceof Inet4Address) {
                    direcciones.add(direccion.getHostAddress() + " (" + interfaz.getDisplayName() + ")");
                }
            }
        }
        return direcciones;
    }
}
