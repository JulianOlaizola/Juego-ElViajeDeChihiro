package juego.chihiro.red;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public final class InterfacesRed {

    private static final byte[] BROADCAST_GENERAL = { (byte) 255, (byte) 255, (byte) 255, (byte) 255 };

    private InterfacesRed() {
    }

    public static List<String> direccionesLocales() throws SocketException {
        List<String> direcciones = new ArrayList<String>();
        for (NetworkInterface interfaz : interfacesActivas()) {
            for (InetAddress direccion : Collections.list(interfaz.getInetAddresses())) {
                if (direccion instanceof Inet4Address) {
                    direcciones.add(direccion.getHostAddress() + " (" + interfaz.getDisplayName() + ")");
                }
            }
        }
        return direcciones;
    }

    public static List<InetAddress> direccionesBroadcast() {
        List<InetAddress> destinos = new ArrayList<InetAddress>();
        try {
            destinos.add(InetAddress.getByAddress(BROADCAST_GENERAL));
        } catch (UnknownHostException e) {
            throw new IllegalStateException("Dirección de broadcast inválida", e);
        }
        try {
            for (NetworkInterface interfaz : interfacesActivas()) {
                for (InterfaceAddress direccion : interfaz.getInterfaceAddresses()) {
                    InetAddress broadcast = direccion.getBroadcast();
                    if (broadcast != null && !destinos.contains(broadcast)) {
                        destinos.add(broadcast);
                    }
                }
            }
        } catch (SocketException e) {
            return destinos;
        }
        return destinos;
    }

    private static List<NetworkInterface> interfacesActivas() throws SocketException {
        List<NetworkInterface> activas = new ArrayList<NetworkInterface>();
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        if (interfaces == null) {
            return activas;
        }
        for (NetworkInterface interfaz : Collections.list(interfaces)) {
            if (interfaz.isUp() && !interfaz.isLoopback() && !interfaz.isVirtual()) {
                activas.add(interfaz);
            }
        }
        return activas;
    }
}
