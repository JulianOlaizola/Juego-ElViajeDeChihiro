# El viaje de Chihiro: Caos en la Casa de Baños

Videojuego cooperativo **en red** para 2 jugadores, vista top-down 2D, inspirado en Overcooked y ambientado en la Casa de Baños (Aburaya) de la película "El viaje de Chihiro". Trabajo final de Laboratorio y Programación (6to año), E.T. N.° 35 "Ing. E. Latzina".

* * *

## 👥 Integrantes

- Alejandro Almazán
- Santiago Guerra Milhem
- Julián Olaizola

* * *

## 🎮 De qué trata

Dos jugadores, cada uno en su computadora, atienden juntos a los espíritus que llegan a la casa de baños. Hay que prepararles el baño (buscar las sales en el almacén, encender la caldera y llevar el agua caliente en baldes), cumplir los pedidos a tiempo y limpiar las tinas para el próximo cliente. Los jugadores se pueden pasar lo que llevan en la mano para repartirse el trabajo.

**El turno se gana** atendiendo 5 pedidos antes de que se terminen los 3 minutos. **El turno se pierde** si se van 3 espíritus sin ser atendidos o si se acaba el tiempo. Al terminar, los dos pueden votar la revancha sin cerrar el juego.

* * *

## 🌐 Cómo funciona la red

- **Tres programas:** un servidor y dos clientes, en tres computadoras de la misma red local.
- **Solo UDP**, con sockets de Java (`DatagramSocket`). No se usa TCP.
- **Servidor autoritativo:** el servidor corre Box2D, las reglas, los pedidos, el tiempo y el puntaje. Los clientes solo mandan sus teclas y dibujan lo que reciben.
- **Hilos:** cada lado tiene un hilo que solo recibe paquetes y los deja en una cola segura; el bucle del juego la vacía sin bloquearse nunca.
- **Descubrimiento por broadcast:** el cliente encuentra solo los servidores de la red local, o se conecta escribiendo la IP.
- **Confiabilidad propia:** el estado viaja 20 veces por segundo con número de secuencia; los eventos importantes viajan con id, confirmación (ACK) y reenvío.
- **Desconexiones:** si se cae un jugador, el servidor lo espera 30 segundos; si se cae el servidor, el cliente reintenta 20 segundos y si no vuelve al menú sin cerrarse.

La explicación completa está en la Wiki, sección [Documentación de red](https://github.com/JulianOlaizola/Juego-ElViajeDeChihiro/wiki/Documentacion-de-red), y en la carpeta [`docs/wiki`](docs/wiki).

* * *

## ✅ Requisitos técnicos y dónde están resueltos

| Requisito | Cómo quedó resuelto | Dónde mirarlo |
|---|---|---|
| **Sockets UDP** | Socket del servidor con hilo de escucha y socket del cliente con su propio hilo. Protocolo de texto `CHIHIRO;1;TIPO;...` validado campo por campo | `servidor/.../ServidorUdp.java`, `core/.../red/ClienteUdp.java`, `core/.../red/Mensaje.java` |
| **Hilos sin bloquear el render** | `receive()` corre en un hilo daemon que deja los paquetes en una `ConcurrentLinkedQueue`; el bucle los saca con `poll()` | `ServidorUdp`, `ClienteUdp`, `ServidorJuego.procesarEntrantes()` |
| **Sincronización de posiciones** | `ESTADO` a 20 Hz con número de secuencia; el cliente descarta los viejos e interpola | `core/.../red/InstantaneaPartida.java`, `SesionCliente`, `entidades/Jugador.java` |
| **Box2D solo en el servidor** | Mundo sin gravedad, paredes estáticas, objetos como sensores y jugadores dinámicos, a paso fijo de 1/60 s. El cliente no depende de Box2D | `servidor/.../fisica/MundoFisico.java`, `servidor/.../logica/Partida.java` |
| **Clientes que solo renderizan** | La pantalla de juego manda ejes y contadores, y dibuja la última instantánea | `core/.../pantallas/PantallaJuego.java` |
| **Descubrimiento por broadcast** | `DESCUBRIR` a 255.255.255.255 y al broadcast de cada placa; el servidor responde `ANUNCIO` | `core/.../red/BuscadorServidores.java`, `InterfacesRed.java` |
| **Puerto ocupado** | `PuertoOcupadoException` con mensaje claro y sugerencia de otro puerto | `servidor/.../ServidorLauncher.java` |
| **Desconexiones y reconexión** | Tiempos de espera en los dos lados, reserva del lugar, reconexión con token | `ServidorJuego.revisarDesconexiones()`, `SesionCliente` |
| **Nueva partida sin cerrar la aplicación** | Revancha votada por los dos jugadores; el servidor crea una partida y un mundo Box2D nuevos | `ServidorJuego.actualizarFase()` |
| **Ejecutables `.jar`** | `gradlew jars` genera cliente y servidor con todo adentro | `build.gradle`, carpeta `entrega/` |

`core/...` es `core/src/main/java/juego/chihiro` y `servidor/...` es `servidor/src/main/java/juego/chihiro/servidor`.

* * *

## 🕹️ Controles

```
Moverse          W A S D  o  flechas
Interactuar      E  o  SHIFT derecho
Pausa            P o ESC (se pausa para los dos)
Confirmar        ENTER (también "listo" y "revancha")
Volver al menú   ENTER desde la pausa, ESC en el resultado o en la sala
Audio            M silencia, + / - volumen
Depuración       F1 muestra colisiones y zonas de interacción
```

**Cómo se atiende un pedido:**

1. Encender la caldera y esperar unos segundos a que el agua se caliente.
2. Juntar sales en el almacén y llenar un balde en la caldera.
3. Entregar las dos cosas en la tina que tiene el espíritu esperando.
4. La tina queda sucia: limpiarla y tirar la suciedad en el depósito.

Mirando al compañero y apretando E, le pasás lo que llevás o se lo sacás.

* * *

## ⚙️ Cómo compilar y ejecutar

Requisitos: JDK 17 o superior y Git. No hace falta instalar Gradle.

```
git clone https://github.com/JulianOlaizola/Juego-ElViajeDeChihiro.git
cd Juego-ElViajeDeChihiro
gradlew.bat jars
```

En Linux o macOS: `./gradlew jars`. Los ejecutables quedan en `entrega/`.

**Servidor** (en una computadora):

```
java -jar ElViajeDeChihiro-Servidor.jar [puerto]
```

Muestra las IP de la computadora. Comandos: `estado`, `ip`, `terminar`, `ayuda`, `salir`.

**Cliente** (en cada una de las otras dos):

```
java -jar ElViajeDeChihiro-Cliente.jar [ip-del-servidor]
```

Desde el menú: **Buscar partida en la red** o **Conectar por IP**. En la sala de espera, ENTER cuando estés listo.

La primera vez, Windows pregunta por el firewall: hay que **permitir Java en redes privadas** en las tres computadoras.

Durante el desarrollo: `gradlew.bat servidor:run` y `gradlew.bat lwjgl3:run`.

* * *

## 🛠️ Tecnologías

- **Lenguaje:** Java 17 (LTS)
- **Framework:** LibGDX 1.14.2 (backend LWJGL3 para el cliente, headless para el servidor)
- **Física:** Box2D (gdx-box2d), solo en el servidor
- **Red:** sockets UDP de Java (`DatagramSocket`), modelo cliente-servidor
- **Build:** Gradle 9.6.1 (incluido en el wrapper)
- **Mapas:** Tiled Map Editor (`.tmx`)

* * *

## 📁 Estructura del proyecto

```
Juego-ElViajeDeChihiro/
├── assets/                  mapa de Tiled, imágenes, audio y tipografía
├── core/src/main/java/juego/chihiro/
│   ├── Main.java            Game: assets, entradas, audio y sesión de red
│   ├── audio/               AdministradorAudio
│   ├── entidades/           Jugador y Espiritu (solo se dibujan)
│   ├── entradas/            Accion, ControladorEntradas
│   ├── hud/                 Hud
│   ├── mundo/               mapa para dibujar, tipos de objetos y de tinas
│   ├── pantallas/           carga, menú, búsqueda, conexión, sala, juego y mensajes
│   ├── red/                 protocolo, cliente UDP, sesión, búsqueda y eventos
│   └── utiles/              Constantes, Recursos, DepuradorColisiones
├── servidor/src/main/java/juego/chihiro/servidor/
│   ├── ServidorLauncher     main del servidor
│   ├── ServidorUdp          socket e hilo de recepción
│   ├── ServidorJuego        bucle de 60 ticks
│   ├── fisica/              mapa sin gráficos y mundo Box2D
│   ├── logica/              partida, jugadores, tinas y pedidos
│   └── sesiones/            jugadores conectados y eventos confiables
├── lwjgl3/                  launcher de escritorio del cliente
├── scripts/                 .bat y .sh para ejecutar los .jar
└── docs/
    ├── propuesta/           figuras de la propuesta
    └── wiki/                documentación de red (copia de la Wiki)
```

* * *

## 🎥 Videos

- **Segunda pre-entrega (prototipo local):** https://www.youtube.com/watch?v=utll0ygc8ck

* * *

## 📖 Wiki

- [Propuesta del proyecto](https://github.com/JulianOlaizola/Juego-ElViajeDeChihiro/wiki/Propuesta-del-Proyecto)
- [Documentación de red](https://github.com/JulianOlaizola/Juego-ElViajeDeChihiro/wiki/Documentacion-de-red)

* * *

## 📌 Estado actual

**Versión 1.0.0: entrega final.** El juego se juega en red entre tres computadoras: un servidor y dos clientes. Incluye todo el alcance mínimo de la propuesta.

Del alcance deseable quedan pendientes: más niveles, el Espíritu del Hedor, más variedad de pedidos y los recursos gráficos y sonoros definitivos.

* * *

## 📝 Historial de cambios

Los cambios de cada versión están en [CHANGELOG.md](CHANGELOG.md), con el formato Keep a Changelog y versionado semántico.

* * *

## ⚠️ Nota sobre los recursos

Los gráficos y sonidos son **placeholders** generados por nosotros. La tipografía es Liberation Sans, distribuida bajo licencia SIL Open Font License. `StartupHelper.java` (launcher de escritorio) es código de la plantilla de libGDX bajo licencia Apache 2.0 y conserva su aviso de licencia.

"El viaje de Chihiro" y la Casa de Baños Aburaya son propiedad de Studio Ghibli. Este es un proyecto académico sin fines de lucro, inspirado en la película, que no utiliza ni distribuye recursos originales de la obra.
