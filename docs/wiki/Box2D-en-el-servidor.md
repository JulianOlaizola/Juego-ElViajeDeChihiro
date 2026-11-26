# Box2D en el servidor

## Por qué solo en el servidor

Si cada cliente simulara su propia física, por diferencias de tiempo y de redondeo los dos terminarían viendo a los personajes en lugares distintos. Con Box2D solo en el servidor hay **una única verdad**: el servidor mueve los cuerpos, y los clientes dibujan lo que reciben.

El módulo `core` y el cliente **no dependen de Box2D**. La dependencia `gdx-box2d` está solo en `servidor/build.gradle`.

## Leer el mapa sin gráficos

El servidor no tiene ventana ni OpenGL, así que no puede usar `TmxMapLoader` (que carga texturas). `MapaServidor` lee el `.tmx` como XML con `XmlReader` y saca solo lo que necesita:

- capa `colisiones`: rectángulos de paredes y muebles;
- capa `interactivos`: tinas, almacén, caldera, depósito y puntos de aparición.

Tiled mide la Y desde arriba y libGDX desde abajo, por eso cada rectángulo se convierte con `y = alto_del_mapa - y_tiled - alto_del_objeto`.

## El mundo físico

`MundoFisico` arma un `World` sin gravedad (vista desde arriba) con:

| Cuerpo | Tipo | Fixture |
|---|---|---|
| Cada pared o mueble | `StaticBody` | sólido |
| Cada objeto interactivo | `StaticBody` | **sensor**: no frena, pero se puede detectar |
| Cada jugador | `DynamicBody`, `fixedRotation` | sólido, fricción 0 |

Box2D trabaja en metros, así que usamos **32 píxeles = 1 metro** (`PIXELES_POR_METRO`). `aMetros()` y `aPixeles()` hacen la conversión.

## Paso fijo

```java
acumulador += Math.min(delta, 0.25f);
while (acumulador >= 1/60f && pasos < 5) {
    mundo.step(1/60f, 6, 2);
    acumulador -= 1/60f;
}
```

Box2D es estable con un paso constante. Si un tick tarda más, se hacen varios pasos para alcanzar el tiempo real (máximo 5, para que una pausa larga del sistema no provoque cientos de pasos de golpe).

## Movimiento

El cliente manda ejes entre -1 y 1. El servidor normaliza el vector (para que en diagonal no vaya más rápido) y le pone esa velocidad al cuerpo con `setLinearVelocity`. Box2D resuelve los choques: el personaje se desliza contra las paredes en lugar de trabarse.

## Interacciones con sensores

Cuando un jugador aprieta E, `Partida` calcula la zona adelante de él y le pregunta a Box2D qué fixtures hay ahí con `QueryAABB`. Cada fixture guarda en su `userData` a quién pertenece:

- un `JugadorServidor` → se pasan o se sacan el objeto;
- un `ObjetoServidor` → se usa la tina, la caldera, el almacén o el depósito.

## Un mundo nuevo por partida

Cada vez que empieza una partida (o una revancha) se crea una `Partida` nueva con su propio `MundoFisico`, y la anterior se libera con `dispose()`. Así no quedan cuerpos ni velocidades de la partida anterior.
