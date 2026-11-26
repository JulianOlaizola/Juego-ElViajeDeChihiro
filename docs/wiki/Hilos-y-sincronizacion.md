# Hilos y sincronización

## El problema

`DatagramSocket.receive()` es **bloqueante**: el hilo que lo llama se queda quieto hasta que llega un paquete. Si lo llamáramos desde `render()`, el juego se congelaría cada vez que no llega nada.

## La solución: un hilo de red y una cola

```
 Hilo de red (daemon)                       Hilo principal (render / tick)
 --------------------                       ------------------------------
 while (activo) {                           cada cuadro:
   socket.receive(paquete);  ← se bloquea     while ((p = cola.poll()) != null) {
   mensaje = Mensaje.desdeBytes(...)            atender(p);
   cola.add(new PaqueteRecibido(...))  ───►   }
 }                                            simular / dibujar
```

- El hilo de red **solo** recibe, valida y encola. No toca el estado del juego.
- El hilo principal **solo** lee de la cola con `poll()`, que nunca bloquea: si está vacía devuelve `null`.
- La cola es una `ConcurrentLinkedQueue`, que es segura para que un hilo escriba y otro lea al mismo tiempo sin `synchronized`.

Este mismo esquema está en los dos lados:

| Lado | Clase | Nombre del hilo |
|---|---|---|
| Servidor | `ServidorUdp` | `red-servidor-recepcion` |
| Cliente | `ClienteUdp` | `red-cliente-recepcion` |

## Variables compartidas entre hilos

| Variable | Tipo | Por qué |
|---|---|---|
| `activo` | `volatile boolean` | El hilo principal la pone en `false` al cerrar y el hilo de red tiene que ver el cambio enseguida |
| Contadores de paquetes | `AtomicLong` | Los incrementa el hilo de red y los lee la consola del servidor |
| Último error del cliente | `volatile String` | Lo escribe el hilo de red y lo puede leer la pantalla |

Todo el estado del juego (sesiones, partida, Box2D) lo toca **un solo hilo**, el del bucle. Por eso no hace falta sincronizarlo.

## Cómo se cierra un hilo bloqueado

`cerrar()` pone `activo = false` y cierra el socket. Cerrar el socket hace que `receive()` lance una `SocketException`; el hilo la atrapa, ve que `activo` es `false` y sale del bucle. Después `cerrar()` espera al hilo con `join()` un tiempo máximo. Los hilos son *daemon*, así que nunca impiden que se cierre la aplicación.

## La consola del servidor

`ConsolaServidor` corre en otro hilo leyendo comandos (`estado`, `ip`, `terminar`, `salir`). No toca el estado directamente: le pasa el pedido al hilo del juego con `Gdx.app.postRunnable(...)`, que lo ejecuta en el próximo tick. Es la forma que ofrece libGDX de "mandar trabajo" al hilo principal.

## Ritmo de cada hilo

| Hilo | Ritmo |
|---|---|
| Bucle del servidor | 60 ticks por segundo (`HeadlessApplicationConfiguration.updatesPerSecond`) |
| Render del cliente | Lo que dé el monitor (vsync) |
| Hilos de red | Sin ritmo fijo: se despiertan cuando llega un paquete |

El cliente manda sus entradas cada 33 ms medidos con el reloj, sin importar los FPS, así una computadora lenta no manda menos entradas.
