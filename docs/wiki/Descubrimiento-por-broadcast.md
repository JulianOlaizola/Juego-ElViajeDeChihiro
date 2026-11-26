# Descubrimiento por broadcast

## Qué problema resuelve

Para conectarse hace falta la IP del servidor. En vez de obligar a escribirla, el cliente puede **preguntarle a toda la red local** si hay un servidor del juego.

## Cómo funciona

```
 Cliente                                   Red local                       Servidor
 -------                                   ---------                       --------
 DESCUBRIR → 255.255.255.255:7777  ───────► todas las PCs de la red ──────► lo recibe
           → 192.168.0.255:7777                                            (las demás lo ignoran)
 ◄──────────────────────────────────────────────────────────────────────── ANUNCIO(nombre, 7777, 1, 2)
 lo agrega a la lista
```

1. `BuscadorServidores` manda `DESCUBRIR` cada 2 segundos al puerto 7777.
2. Lo manda a `255.255.255.255` y además a la dirección de broadcast de cada placa de red activa (por ejemplo `192.168.0.255`). Algunas redes y sistemas operativos solo dejan pasar una de las dos, por eso mandamos a ambas.
3. El servidor contesta por **unicast** (directo a quien preguntó) con `ANUNCIO`: nombre, puerto, jugadores conectados y máximo.
4. El cliente arma la dirección del servidor con **la IP de origen del paquete** que recibió y el puerto que dice el anuncio.
5. Si un servidor deja de contestar durante 6 segundos, se borra de la lista.

## Detalles de implementación

- Para mandar a una dirección de broadcast, el socket necesita `setBroadcast(true)`.
- Las direcciones de broadcast de cada placa se obtienen con `NetworkInterface.getInterfaceAddresses()` → `getBroadcast()`. Se ignoran las placas apagadas, de loopback y virtuales (`InterfacesRed`).
- Los dos programas fuerzan IPv4 con `java.net.preferIPv4Stack=true` antes de abrir cualquier socket, porque el broadcast es una característica de IPv4.
- Un servidor lleno se muestra en gris.

## Si no aparece el servidor

| Causa | Solución |
|---|---|
| Firewall de Windows | La primera vez que se abre Java, permitir el acceso en **redes privadas** (en el servidor y en los clientes) |
| La red está marcada como pública | Cambiarla a privada en la configuración de Windows |
| Red de la escuela con aislamiento de clientes o broadcast bloqueado | Usar "Conectar por IP" con la IP que muestra la consola del servidor |
| El servidor usa otro puerto | La búsqueda solo pregunta en el 7777: usar "Conectar por IP" con `IP:puerto` |
