# El viaje de Chihiro: Caos en la Casa de Baños

Videojuego cooperativo para 2 jugadores, vista top-down 2D, inspirado en Overcooked y
ambientado en la Casa de Baños (Aburaya) de la película "El viaje de Chihiro".
Trabajo final de Laboratorio y Programación (6to año).

---

## 🎥 Video del prototipo jugable

**Segunda pre-entrega — el prototipo en funcionamiento:**

### ▶️ https://www.youtube.com/watch?v=utll0ygc8ck

> El video muestra el prototipo jugable andando: el menú, los dos jugadores moviéndose por
> el salón, el ciclo completo de un pedido, el HUD, la pausa y el fin de partida.
> El enlace está publicado como **"Cualquiera con el enlace puede ver"**.

---

## 👥 Integrantes

- Alejandro Almazán
- Santiago Guerra Milhem
- Julián Olaizola

---

## 🎮 De qué trata

Dos jugadores atienden juntos a los espíritus que llegan a la casa de baños. Hay que
prepararles el baño (buscar las sales en el almacén, encender la caldera y llevar el agua
caliente en baldes), cumplir los pedidos a tiempo y limpiar las tinas para el próximo
cliente. Todo cronometrado y coordinándose entre los dos.

**El turno se gana** atendiendo 5 pedidos antes de que se terminen los 3 minutos.
**El turno se pierde** si se van 3 espíritus sin ser atendidos o si se acaba el tiempo.

---

## ✅ Qué se implementó en esta pre-entrega

| Punto de la consigna | Cómo quedó resuelto | Dónde mirarlo |
| --- | --- | --- |
| **1. Entradas del jugador** | Clase dedicada `ControladorEntradas` que extiende `InputAdapter`. Separa el estado continuo (teclas mantenidas para el movimiento) de las acciones de un solo disparo (interactuar, pausa, volumen), que se consumen una única vez. Las teclas `+` y `-` se leen como caracteres tipeados, así que funcionan con cualquier distribución de teclado. | `core/.../entradas/` |
| **2. Animaciones con spritesheet** | Caminata en 4 direcciones para los dos jugadores y flotado de los espíritus, recortadas con `TextureRegion.split` y reproducidas con `Animation` en modo `LOOP`. | `core/.../utiles/Recursos.java` |
| **3. Mapa o entorno con elementos interactivos** | Salón hecho en Tiled (`.tmx`) con capas de piso, decoración, colisiones e interactivos. El almacén de sales, la caldera, las 4 tinas y el depósito de suciedad salen del mapa, no están hardcodeados. | `assets/mapas/`, `core/.../mundo/MapaCasaBanos.java` |
| **4. Cámara y Viewport** | `OrthographicCamera` + `FitViewport` de 960x540. La cámara sigue el punto medio entre los dos jugadores con suavizado y se recorta contra los bordes del mapa. La imagen nunca se deforma al redimensionar. | `core/.../pantallas/PantallaJuego.java` |
| **5. Pantallas y estados** | `Main` extiende `Game` y hay 4 pantallas: carga, menú, instrucciones y juego. Dentro de la partida hay tres estados: jugando, pausa (también automática al perder el foco la ventana) y terminada. | `core/.../pantallas/` |
| **6. Interacción y colisiones** | Colisiones eje por eje contra los rectángulos del mapa (el personaje se desliza en vez de trabarse) y una zona de interacción proyectada adelante del jugador. Cada interacción cambia un estado visible: la tina pasa de libre a ocupada, a sucia y de vuelta a libre. | `core/.../entidades/Jugador.java`, `core/.../mundo/GestorInteracciones.java` |
| **7. HUD fijo** | Cámara y `Viewport` propios, separados de los del mundo. Muestra tiempo, pedidos, fallos, puntaje, estado de la caldera, pedidos activos y el objeto en mano de cada jugador. | `core/.../hud/Hud.java` |
| **8. Sonido y música** | Música de fondo en bucle y 5 efectos distintos según el resultado de cada interacción. Volumen y silencio se controlan con el teclado y quedan guardados en las preferencias. | `core/.../audio/AdministradorAudio.java` |

---

## 🕹️ Controles

```text
Jugador 1     W A S D            moverse
              E                  interactuar

Jugador 2     ↑ ← ↓ →            moverse
              SHIFT derecho      interactuar

Generales     P o ESC            pausar y reanudar
              ENTER o ESPACIO    confirmar (en la pausa, vuelve al menú)
              M                  silenciar
              + / -              subir y bajar el volumen
              F1                 ver colisiones (modo depuración)
```

**Cómo se atiende un pedido:**

1. Encender la caldera y esperar unos segundos a que el agua se caliente.
2. Juntar sales en el almacén y llenar un balde en la caldera.
3. Entregar las dos cosas en la tina que tiene el espíritu esperando.
4. La tina queda sucia: limpiarla y tirar la suciedad en el depósito.

El depósito sirve para tirar cualquier cosa que lleves en la mano.

> Los dos jugadores pueden hacer todo esto en paralelo, y esa es la gracia: conviene que
> uno se ocupe de las sales y el otro del agua.

---

## 🛠️ Tecnologías

- **Lenguaje:** Java 17 (LTS)
- **Framework:** LibGDX 1.14.2
- **Build:** Gradle 9.6.1 (incluido en el wrapper del repositorio, no hace falta instalarlo)
- **Mapas:** Tiled Map Editor (`.tmx`)
- **Red (próxima etapa):** sockets de Java (TCP + UDP), modelo cliente-servidor
- **IDE:** IntelliJ IDEA / Eclipse
- **Plataforma objetivo:** Escritorio (LWJGL3 3.4.1). No apuntamos a Web ni Móvil porque la
  parte de red usa sockets de Java que no corren en el navegador.

---

## ⚙️ Cómo compilar y ejecutar

Requisitos: JDK 17 o superior y Git. No hace falta instalar Gradle: el wrapper incluido en
el repositorio descarga la versión 9.6.1 la primera vez que lo ejecutás.

1. Clonar el repositorio:

   ```bash
   git clone https://github.com/JulianOlaizola/Juego-ElViajeDeChihiro.git
   cd Juego-ElViajeDeChihiro
   ```

2. Correr en escritorio (Linux/macOS):

   ```bash
   ./gradlew lwjgl3:run
   ```

   En Windows:

   ```bat
   gradlew.bat lwjgl3:run
   ```

3. Generar un ejecutable (opcional):

   ```bash
   ./gradlew lwjgl3:jar
   ```

   El `.jar` queda en `lwjgl3/build/libs/`.

---

## 📁 Estructura del proyecto

```text
Juego-ElViajeDeChihiro/
├── assets/
│   ├── audio/            música de fondo y efectos
│   ├── fuentes/          tipografía del juego
│   ├── imagenes/         spritesheets, logo e íconos de la ventana
│   └── mapas/            salón hecho en Tiled (.tmx + tileset)
├── docs/
│   └── propuesta/        figuras de la propuesta (las usa la Wiki)
├── core/src/main/java/juego/chihiro/
│   ├── Main.java         Game: dueño del batch, los assets, las entradas y el audio
│   ├── audio/            AdministradorAudio
│   ├── entidades/        Entidad, Jugador, Espiritu, Direccion
│   ├── entradas/         Accion, ControladorEntradas
│   ├── hud/              Hud
│   ├── mundo/            mapa, tinas, pedidos y reglas de interacción
│   ├── pantallas/        carga, menú, instrucciones y juego
│   └── utiles/           Constantes, Recursos, DepuradorColisiones
└── lwjgl3/               launcher de escritorio
```

---

## 📖 Propuesta detallada (Wiki)

La propuesta completa del proyecto, aprobada por la cátedra, está en la Wiki:
https://github.com/JulianOlaizola/Juego-ElViajeDeChihiro/wiki/Propuesta-del-Proyecto

---

## 📌 Estado actual

**Segunda pre-entrega: prototipo jugable.** El juego arranca en el menú, se puede jugar un
turno completo con los dos jugadores y termina en victoria o derrota.

**Lo que falta para el producto final:**

- **Multijugador en red.** En este prototipo el cooperativo es local: los dos jugadores
  comparten el teclado de la misma máquina. La capa de red con sockets (TCP + UDP) es la
  próxima etapa, y las entradas ya están aisladas en una sola clase justamente para poder
  reemplazar la fuente de los comandos sin tocar el resto del juego.
- Recursos gráficos y sonoros definitivos.
- Eventos especiales, como el Espíritu del Hedor.
- Más variedad de pedidos y más niveles.

---

## 📝 Historial de cambios

Los cambios de cada versión están registrados en [CHANGELOG.md](CHANGELOG.md), siguiendo el
formato Keep a Changelog y versionado semántico.

---

## ⚠️ Nota sobre los recursos

Los gráficos y sonidos de esta pre-entrega son **placeholders** generados por nosotros para
probar el funcionamiento del prototipo; no son los definitivos. La tipografía es Liberation
Sans, distribuida bajo licencia SIL Open Font License.

"El viaje de Chihiro" y la Casa de Baños Aburaya son propiedad de Studio Ghibli. Este es un
proyecto académico sin fines de lucro, inspirado en la película, que no utiliza ni
distribuye recursos originales de la obra.
