# Confiabilidad sobre UDP

La consigna exige usar **solo UDP**. UDP no garantiza que un paquete llegue, ni que llegue una sola vez, ni en orden. Separamos los datos en dos grupos y tratamos cada uno distinto.

## Datos que se pueden perder: el estado

Posiciones, tiempo, puntaje, tinas: todo eso viaja en `ESTADO` **20 veces por segundo**. Si se pierde uno, 50 ms después llega otro más nuevo. No tiene sentido reenviar un estado viejo.

Lo que sí hacemos es descartar los que llegan **desordenados**:

```java
if (hayInstantanea && nueva.getSecuencia() <= instantanea.getSecuencia()) {
    instantaneasDescartadas++;
    return;
}
```

Lo mismo hace el servidor con las `ENTRADA` de cada jugador (`SesionJugador.registrarEntrada`).

## Datos que no se pueden perder: los eventos

Un aviso ("¡Espíritu satisfecho!"), el inicio o el fin de la partida, o que el compañero se desconectó: si se pierden, el jugador no se entera. Para esos usamos un esquema propio de confirmación:

```
 Servidor                                         Cliente
 --------                                         -------
 encolar evento #8 en CanalConfiable
 EVENTO(8, AVISO, ...) ──────────── se pierde ──X
 (pasan 200 ms sin ACK)
 EVENTO(8, AVISO, ...) ─────────────────────────► lo procesa
                                                  ACK(8)
 ◄─────────────────────────────────────────────── 
 lo borra de pendientes
```

| Pieza | Clase | Qué hace |
|---|---|---|
| Numerar | `CanalConfiable.encolar()` | Cada evento de cada jugador recibe un id creciente |
| Guardar hasta confirmar | `CanalConfiable` | Un mapa de id → mensaje pendiente |
| Reenviar | `CanalConfiable.paraReenviar()` | Cada 200 ms reenvía lo que sigue sin confirmar |
| Confirmar | `SesionCliente` | Contesta `ACK` con el id **cada vez** que recibe un evento, aunque sea repetido |
| Evitar duplicados | `ReceptorEventos` | Recuerda los ids ya procesados (ventana de 512) y descarta los repetidos |

¿Por qué el cliente confirma también los repetidos? Porque si el `ACK` anterior se perdió, el servidor va a seguir reenviando. Volver a confirmar corta el ciclo.

## Interacciones: contador acumulado

Apretar E no es un evento confiable, es un **contador** que viaja en todas las `ENTRADA`. Si el servidor vio "7" y llega "9", sabe que hubo dos pulsaciones nuevas, aunque se hayan perdido los paquetes intermedios. La pausa funciona igual. Es más simple que confirmar cada pulsación y no agrega paquetes.

## Comparación con lo que pedía la propuesta original

La propuesta dividía los datos entre TCP (confiables) y UDP (tiempo real). Mantuvimos esa misma división, pero la parte confiable la implementamos nosotros sobre UDP:

| Propuesta (TCP) | Versión final (UDP) |
|---|---|
| Conexión inicial | `CONECTAR` con reintentos cada 500 ms hasta `BIENVENIDA` o `RECHAZO` |
| Inicio y fin de partida | `EVENTO` `INICIO_PARTIDA` y `FIN_PARTIDA` con `ACK` |
| Entrega correcta o incorrecta | `EVENTO` `AVISO` con texto y efecto de sonido |
| Desconexión de un jugador | `EVENTO` `JUGADOR_DESCONECTADO` y `JUGADOR_RECONECTADO` |
| Resultado final y puntaje | Viajan en cada `ESTADO` y en el `FIN_PARTIDA` |
