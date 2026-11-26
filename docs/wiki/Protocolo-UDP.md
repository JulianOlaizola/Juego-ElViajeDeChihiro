# Protocolo UDP

## Formato de un paquete

Cada paquete es texto UTF-8 con campos separados por punto y coma:

```
CHIHIRO;1;TIPO;campo1;campo2;...
```

| Posición | Contenido | Para qué sirve |
|---|---|---|
| 0 | `CHIHIRO` | Descartar paquetes de otros programas que usen el mismo puerto |
| 1 | `1` | Versión del protocolo. Si no coincide, el paquete se descarta |
| 2 | Tipo de mensaje | Un valor de `TipoMensaje` |
| 3 en adelante | Datos | Dependen del tipo |

Lo arma y lo lee la clase `Mensaje`. Si un dato trae un `;`, se reemplaza por `,` para que no desarme el paquete. El tamaño máximo es 4096 bytes (`Protocolo.TAMANIO_MAXIMO_PAQUETE`).

Elegimos texto en vez de binario porque se puede leer en la consola y en Wireshark, y un paquete de estado completo ocupa unos 150 bytes, muy por debajo del límite.

## Mensajes

| Tipo | Sentido | Campos | Cuándo se manda |
|---|---|---|---|
| `PING` | cliente → servidor | marca de tiempo (nanosegundos) | Al probar la conexión y una vez por segundo durante la partida |
| `PONG` | servidor → cliente | la misma marca, tick del servidor | Respuesta al `PING`; el cliente calcula la latencia |
| `DESCUBRIR` | cliente → broadcast | ninguno | Cada 2 segundos en la pantalla "Buscar partida" |
| `ANUNCIO` | servidor → cliente | nombre, puerto, conectados, máximo | Respuesta a `DESCUBRIR` |
| `CONECTAR` | cliente → servidor | nombre del jugador, token (o `-`) | Cada 500 ms hasta 10 intentos; también para reconectar |
| `BIENVENIDA` | servidor → cliente | número de jugador, token | Cuando acepta la conexión |
| `RECHAZO` | servidor → cliente | motivo | Sala llena, partida empezada o sesión desconocida |
| `ENTRADA` | cliente → servidor | token, secuencia, eje X, eje Y, contador de interacciones, contador de pausas, listo | Cada 33 ms mientras está conectado |
| `ESTADO` | servidor → clientes | secuencia, fase, tiempo, pedidos, fallos, puntaje, caldera, resultado, espera de reconexión, datos de los 2 jugadores y de cada tina | Cada 3 ticks (20 por segundo) |
| `EVENTO` | servidor → cliente | id, tipo de evento, datos | Cuando pasa algo que no se puede perder |
| `ACK` | cliente → servidor | token, id del evento | Al recibir cada `EVENTO` |
| `DESCONECTAR` | ambos sentidos | token (cliente) o motivo (servidor) | Al salir del juego o al cerrar el servidor |

## Ejemplos reales

```
CHIHIRO;1;ENTRADA;3fa9c01b77de;1532;-1.0;0.0;7;2;0
```
Jugador con token `3fa9c01b77de`, entrada número 1532, yendo a la izquierda, 7 interacciones y 2 pausas pedidas desde que se conectó, no está votando "listo".

```
CHIHIRO;1;ESTADO;123456;JUGANDO;97.53;3;1;412;1;1;1.0;0;0;1;0;612.34;401.5;1;1;2;1;0;1100.25;655.75;3;0;1;4;0;1;0;0.42;...
```
Estado número 123456, partida en curso con 97,5 segundos, 3 pedidos, 1 fallo, 412 puntos, caldera encendida con agua caliente... después vienen los dos jugadores y las cuatro tinas.

## Por qué cada campo resuelve un problema de UDP

| Problema de UDP | Solución en el protocolo |
|---|---|
| Los paquetes llegan desordenados | `ESTADO` y `ENTRADA` llevan número de secuencia; se descarta el que es más viejo que el último procesado |
| Los paquetes se pierden | El estado se manda 20 veces por segundo: si se pierde uno, llega otro 50 ms después. Las interacciones van como contador acumulado |
| Algo importante no puede perderse | `EVENTO` con id + `ACK` + reenvío cada 200 ms. Ver [Confiabilidad sobre UDP](Confiabilidad-sobre-UDP) |
| No hay "conexión" | El token de la sesión identifica al jugador aunque cambie de puerto; si no llega nada en 5 s se considera caído |
| Llegan paquetes ajenos | Cabecera `CHIHIRO;1` y validación de cada campo con `LectorCampos` |
