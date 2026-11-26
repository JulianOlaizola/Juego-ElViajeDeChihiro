# Desconexiones y manejo de errores

## Errores al arrancar el servidor

| Situación | Qué pasa | Código de salida |
|---|---|---|
| El puerto ya está en uso | `bind()` lanza `BindException`, se convierte en `PuertoOcupadoException` y se muestra: *"El puerto 7777 ya está en uso. Cerrá el otro servidor o elegí otro puerto"* con el comando de ejemplo | 1 |
| Puerto inválido (`abc`, `80`, `99999`) | Mensaje de uso con el rango permitido (1024 a 65535) | 2 |
| No se puede abrir el socket por otro motivo | Mensaje con la causa | 3 |
| Falta el mapa o está roto | `MapaInvalidoException`: el servidor avisa y se cierra | — |

Nada de esto muestra un *stack trace*: el usuario siempre ve qué pasó y qué hacer.

## Paquetes inválidos

Un paquete de otro programa, de otra versión del juego o con un campo mal escrito lanza `MensajeInvalidoException`. El paquete se descarta, se cuenta (comando `estado`) y el servidor sigue. Además, cada mensaje se atiende dentro de su propio `try`, así un error inesperado al procesar un paquete no tira abajo el servidor.

## Un jugador se desconecta

**Saliendo bien** (ESC o cerrando la ventana): el cliente manda `DESCONECTAR` tres veces. El servidor libera su lugar y avisa al otro.

**Se corta de golpe** (se cuelga la PC, se desenchufa el cable, se cae el WiFi):

1. El servidor no recibe nada de ese jugador durante **5 segundos**.
2. Si la partida estaba en curso, el lugar queda **reservado** y la fase pasa a `ESPERANDO_JUGADOR`. El otro jugador ve *"Esperando a tu compañero"* con una cuenta regresiva.
3. Si el jugador vuelve antes de **30 segundos**, se reconecta con su mismo token, recupera su número y su posición, y la partida queda en **pausa** hasta que alguien la reanude.
4. Si no vuelve, se libera el lugar y los dos vuelven a la sala de espera.

Si el corte pasa en la sala de espera, el lugar se libera directamente.

## Se cae el servidor

**Cerrándolo bien** (comando `salir` o Ctrl+C): el servidor manda `DESCONECTAR` con el motivo a los dos clientes, que muestran *"Se cortó la conexión: El servidor se cerró"* y vuelven al menú.

**Se corta de golpe**:

1. El cliente no recibe nada durante 5 segundos y pasa a `RECONECTANDO`. Muestra *"Reconectando..."* con una cuenta regresiva.
2. Durante **20 segundos** reintenta `CONECTAR` con su token una vez por segundo.
3. Si el servidor vuelve (por ejemplo, era un corte de red), lo recibe de nuevo.
4. Si no, muestra el motivo y vuelve al menú **sin cerrar la aplicación**.

Si en vez de volver el mismo servidor se abre uno **nuevo** en la misma PC y puerto, ese servidor no conoce el token. Si los clientes estaban reconectando, los recibe como jugadores nuevos y los lleva a la sala de espera. Si el reinicio fue tan rápido que los clientes no llegaron a notar el corte, el servidor nuevo rechaza sus entradas con `SESION_DESCONOCIDA` y cada cliente vuelve al menú explicando el motivo.

## Otros casos

| Caso | Comportamiento |
|---|---|
| Se conecta un tercer jugador | `RECHAZO` con `SALA_LLENA` |
| Alguien intenta entrar con la partida empezada | `RECHAZO` con `PARTIDA_EN_CURSO` |
| La IP escrita no existe | *"No se encontró la dirección ..."* (la resolución DNS corre en otro hilo para no congelar la pantalla) |
| La IP existe pero no hay servidor | Después de 10 intentos: *"El servidor no respondió. Revisá la IP, el puerto y el firewall."* |
| Un cliente cambia de puerto (NAT, reconexión) | El servidor lo reconoce por el token y actualiza la dirección |

## Nueva partida sin cerrar nada

Al terminar, la pantalla de resultado muestra el puntaje y la votación de revancha. Cuando **los dos** apretan ENTER, el servidor crea una `Partida` nueva (con un mundo Box2D nuevo) y empieza otro turno. Ninguna de las tres aplicaciones se cierra. Con ESC cualquiera vuelve al menú y puede buscar otra partida.

Para probar la revancha sin jugar los 3 minutos, el comando `terminar` en la consola del servidor corta la partida actual.

## Registro

Todo lo que muestra la consola del servidor también se guarda en `servidor.log`, en la carpeta desde donde se ejecutó. Sirve para revisar qué pasó después de una prueba en las tres computadoras.
