# Changelog

Todos los cambios importantes del proyecto se anotan acá.
Seguimos el formato de Keep a Changelog (https://keepachangelog.com/es-ES/1.0.0/)
y el versionado semántico (https://semver.org/lang/es/).

## [Unreleased]

## [1.0.0] - 2026-11-19
### Agregado
- Módulo `servidor`: aplicación sin ventana (backend headless de libGDX) con su propio
  bucle de 60 ticks por segundo y consola de comandos (`estado`, `ip`, `terminar`, `ayuda`,
  `salir`).
- Comunicación exclusivamente por UDP con un protocolo de texto propio
  (`CHIHIRO;versión;TIPO;campos`) compartido entre cliente y servidor en `core`.
- Hilos de recepción en el cliente y en el servidor que dejan los paquetes en una cola
  segura, para que el render y el bucle del servidor nunca se bloqueen.
- Box2D en el servidor: paredes estáticas, objetos interactivos como sensores, jugadores
  dinámicos y paso fijo de 1/60 s. El mapa se lee del `.tmx` sin cargar gráficos.
- Lógica de la partida en el servidor: pedidos, tinas, caldera, tiempo, puntaje,
  victoria y derrota.
- Pasar y sacar objetos entre jugadores mirándose e interactuando.
- Búsqueda de servidores en la red local por broadcast y conexión escribiendo la IP.
- Sala de espera con asignación de jugador 1 y 2, rechazo de un tercer jugador y
  confirmación de "listo".
- Estado completo de la partida enviado 20 veces por segundo con número de secuencia;
  el cliente descarta los paquetes viejos e interpola las posiciones.
- Eventos confiables sobre UDP (avisos, inicio y fin de partida, desconexiones) con id,
  confirmación (ACK), reenvío cada 200 ms y descarte de duplicados.
- Pausa compartida entre los dos jugadores.
- Pantalla de resultado con puntaje, pedidos, fallos y tiempo sobrante.
- Revancha: los dos jugadores votan y empieza una partida nueva sin cerrar ninguna
  aplicación.
- Manejo de desconexiones: el servidor espera 30 segundos a un jugador caído y el
  cliente reintenta 20 segundos si se cae el servidor; al cerrar el servidor se avisa
  a los clientes.
- Manejo de errores: puerto ocupado, puerto inválido, paquetes inválidos, IP
  inexistente y servidor que no responde, siempre con un mensaje claro.
- Medición del ping en el HUD y registro del servidor en `servidor.log`.
- Tarea `gradlew jars`, que genera `ElViajeDeChihiro-Cliente.jar` y
  `ElViajeDeChihiro-Servidor.jar` en `entrega/` junto con scripts `.bat` y `.sh`.
- Documentación de red en `docs/wiki`, publicada también en la Wiki.

### Cambiado
- El cliente ya no simula el juego: manda sus teclas y dibuja lo que recibe. El
  cooperativo local en un solo teclado se reemplazó por el juego en red.
- Menú principal: "Buscar partida en la red", "Conectar por IP", "Instrucciones" y "Salir".
- HUD alimentado por el estado que llega del servidor.
- Sonidos disparados por los eventos que manda el servidor.
- La ventana del cliente ya no se pausa sola al perder el foco: la pausa la decide el
  servidor para los dos jugadores.
- Los `.jar` ya no llevan la versión en el nombre.

### Eliminado
- `GestorInteracciones`, `GestorPedidos`, `Tina`, `Pedido` y `EstadoPartida` del
  cliente: esa lógica ahora vive en el servidor.
- Comentarios de la plantilla en los archivos de Gradle, en `gradle.properties` y en el
  launcher de escritorio (se conserva el aviso de licencia de `StartupHelper.java`).

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
