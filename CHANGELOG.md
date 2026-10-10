# Changelog

Todos los cambios importantes del proyecto se anotan acá.
Seguimos el formato de Keep a Changelog (https://keepachangelog.com/es-ES/1.0.0/)
y el versionado semántico (https://semver.org/lang/es/).

## [Unreleased]
### Agregado
- (Acá van las próximas funcionalidades del juego.)

## [0.2.0] - 2026-10-09
### Agregado
- Prototipo jugable de la casa de baños: dos jugadores atendiendo espíritus en simultáneo.
- Clase principal convertida en Game, con pantallas de carga, menú, instrucciones y juego.
- Carga de recursos con AssetManager y pantalla de progreso mientras se cargan.
- Controlador de entradas propio basado en InputAdapter, que separa el estado continuo de
  las teclas de movimiento de las acciones de un solo disparo.
- Animaciones de caminata en cuatro direcciones para los dos jugadores y animación de
  flotado de los espíritus, recortadas de spritesheets.
- Salón de la casa de baños hecho en Tiled, con capas de piso, decoración, colisiones y
  objetos interactivos leídos desde el mapa.
- Cámara que sigue a los dos jugadores con FitViewport, recortada contra los bordes del
  mapa para no mostrar el vacío de afuera.
- Movimiento en cuatro direcciones con colisiones resueltas eje por eje, así el personaje
  se desliza a lo largo de las paredes en vez de trabarse.
- Ciclo de pedidos: los espíritus llegan a las tinas, piden sales y un balde de agua
  caliente, tienen paciencia limitada y dejan la tina sucia cuando se van.
- Interacción con el almacén de sales, la caldera, las tinas y el depósito de suciedad,
  con mensajes en pantalla y un efecto de sonido distinto según el resultado. El depósito
  también sirve para descartar sales o un balde que ya no se necesitan.
- HUD fijo con cámara y viewport propios: tiempo, pedidos, fallos, puntaje, estado de la
  caldera, pedidos activos y objeto en mano de cada jugador.
- Estados de partida: jugando, pausa y finalización, con pantallas de victoria y derrota.
  Desde la pausa se puede volver al menú sin cerrar la aplicación.
- Pausa automática cuando la ventana pierde el foco.
- Música de fondo en bucle, cinco efectos de sonido, y controles de volumen y silencio con
  la configuración guardada en las preferencias del usuario.
- Depurador visual de colisiones y zonas de interacción, que se enciende con F1.
- Recursos placeholder del prototipo: mapa de Tiled, spritesheets, sonidos, música, íconos
  de la ventana y tipografía.
- Enlace al video del prototipo jugable en el README.
- Sección del README que relaciona cada punto de la consigna con dónde está resuelto.

### Cambiado
- Launcher de escritorio: título del juego, resolución 960x540, límite mínimo de ventana e
  íconos propios en lugar de los de la plantilla.
- Estado actual del README: pasa de configuración inicial a prototipo jugable, y aclara que
  en esta etapa el cooperativo es local y la red viene después.

### Corregido
- Versión del proyecto en gradle.properties: seguía en 0.1.0 aunque este CHANGELOG ya
  registraba la 0.1.1. Ahora coincide con la versión publicada, 0.2.0.

### Eliminado
- Imágenes de ejemplo de la plantilla de LibGDX (assets/libgdx.png y los íconos
  libgdx16/32/64/128.png), que ya no se usan.

## [0.1.1] - 2026-08-27
### Agregado
- Figuras de la propuesta en docs/propuesta/imagenes, referenciadas desde la Wiki.
- Sección "Historial de cambios" en el README con el enlace a este archivo.
- Versión de Gradle (9.6.1, incluida en el wrapper) en las tecnologías del README.

### Cambiado
- La propuesta de la Wiki pasó de ser un resumen general al documento formal completo
  presentado y aprobado, publicado como página principal.
- Encabezados de este CHANGELOG traducidos al español según Keep a Changelog en español.
- Enlace a la Wiki en el README apuntado a la página principal de la propuesta.
- Versión de LibGDX indicada en la propuesta de la Wiki, actualizada de 1.12.1 a 1.14.2
  para que coincida con la que usa el proyecto.

### Corregido
- Fecha de la versión 0.1.0: pasa a 2026-07-17, la del primer commit del repositorio.

## [0.1.0] - 2026-07-17
### Agregado
- Estructura inicial del proyecto LibGDX con gdx-liftoff (módulos core y lwjgl3).
- Repositorio en GitHub con control de versiones.
- README con la descripción del juego, tecnologías e instrucciones.
- .gitignore para proyectos LibGDX / Gradle.
- Wiki del proyecto con la propuesta de "El viaje de Chihiro: Caos en la Casa de Baños".
