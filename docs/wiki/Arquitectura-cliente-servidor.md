# Arquitectura cliente-servidor

## Los tres módulos del proyecto

| Módulo | Qué es | Qué tiene adentro |
|---|---|---|
| `core` | Código compartido | El protocolo de red (`juego.chihiro.red`), las pantallas, el HUD, el dibujo de los personajes y las constantes del juego. Lo usan el cliente y el servidor. |
| `servidor` | Aplicación sin ventana | Socket UDP, sesiones de jugadores, Box2D y todas las reglas de la partida. Se ejecuta con `HeadlessApplication` de libGDX. |
| `lwjgl3` | Aplicación de escritorio | El launcher del cliente: abre la ventana y arranca `Main`. |

El servidor depende de `core` para hablar exactamente el mismo protocolo que el cliente. Si `Mensaje` o `TipoMensaje` estuvieran duplicados, cualquier cambio en un lado rompería el otro.

## Quién decide cada cosa

| Cosa | La calcula | El cliente... |
|---|---|---|
| Posición de los jugadores | Servidor (Box2D) | la recibe y la suaviza al dibujar |
| Colisiones con paredes y muebles | Servidor (Box2D) | no las calcula |
| Qué objeto tiene cada uno en la mano | Servidor | lo dibuja |
| Pedidos, tinas, caldera | Servidor | los muestra en el HUD |
| Tiempo, puntaje, victoria y derrota | Servidor | los muestra |
| Pausa | Servidor (la pide cualquier cliente) | muestra el velo de pausa |
| Música y efectos | Cada cliente, localmente | suena cuando llega un evento |

Esto se llama **servidor autoritativo**. Si un cliente manda algo raro, el servidor lo valida y no se rompe la partida del otro.

## Recorrido de una tecla

```
 Cliente 1                     Servidor                          Cliente 2
 ---------                     --------                          ---------
 aprieta D
 ENTRADA(ejeX=1) ───────────►  hilo de red la mete en la cola
                               tick: saca de la cola
                               aplica velocidad al cuerpo
                               Box2D avanza 1/60 s
                               cada 3 ticks arma ESTADO
 ◄──────────────────────────── ESTADO(x, y, ...) ──────────────►
 interpola y dibuja                                              interpola y dibuja
```

## Recorrido de una interacción (tecla E)

1. El cliente suma 1 a su **contador de interacciones** y lo manda dentro de cada `ENTRADA`.
2. El servidor compara ese contador con el último que vio. Si subió, ejecuta una interacción por cada unidad nueva (máximo 3 por tick).
3. Busca con Box2D qué hay en la zona adelante del jugador: un compañero, la caldera, una tina, el almacén o el depósito.
4. Aplica la regla y manda un `EVENTO` de tipo `AVISO` con el texto y el sonido que corresponde.

Usamos un contador en vez de mandar "apretó E" una sola vez porque UDP puede perder ese paquete. Con el contador, el siguiente `ENTRADA` que llega trae el número nuevo y la interacción no se pierde.

## Fases de la partida

```
SALA ──(los dos listos)──► JUGANDO ◄──(P o ESC)──► PAUSA
                             │  ▲
          (se cae alguien)   │  │ (vuelve: queda en PAUSA)
                             ▼  │
                       ESPERANDO_JUGADOR ──(no vuelve en 30 s)──► SALA
                             
JUGANDO ──(ganan o pierden)──► TERMINADA ──(los dos votan revancha)──► JUGANDO
```

La fase vive en el servidor (`FasePartida`) y viaja en cada `ESTADO`.

## Clases principales

| Clase | Módulo | Responsabilidad |
|---|---|---|
| `ServidorLauncher` | servidor | `main`: lee el puerto, abre el socket, arranca el bucle |
| `ServidorUdp` | servidor | Socket, hilo de recepción y cola de entrada |
| `ServidorJuego` | servidor | Bucle de 60 ticks: atiende mensajes, simula, difunde el estado |
| `GestorSesiones` / `SesionJugador` | servidor | Quién es el jugador 1 y el 2, su token, su dirección y su última entrada |
| `CanalConfiable` | servidor | Eventos pendientes de confirmación y reenvíos |
| `Partida` | servidor | Reglas del juego y mundo Box2D de esa partida |
| `MundoFisico` / `MapaServidor` | servidor | Box2D y lectura del mapa `.tmx` sin gráficos |
| `ClienteUdp` | core | Socket del cliente y su hilo de recepción |
| `SesionCliente` | core | Conexión, envío de entradas, recepción de estado y eventos, reconexión |
| `InstantaneaPartida` | core | El estado completo que manda el servidor, en un solo paquete |
| `PantallaJuego` | core | Dibuja el mapa, los personajes y el HUD con la última instantánea |
