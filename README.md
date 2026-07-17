# El viaje de Chihiro: Caos en la Casa de Baños

Videojuego cooperativo en red (2 jugadores), vista top-down 2D, inspirado en Overcooked
y ambientado en la Casa de Baños (Aburaya) de la película "El viaje de Chihiro".
Trabajo final de Laboratorio y Programación (6to año).

## Integrantes
- Alejandro Almazán
- Santiago Guerra Milhem
- Julián Olaizola

## De qué trata
Dos jugadores atienden juntos a los espíritus que llegan a la casa de baños. Hay que
prepararles el baño (buscar las sales en el almacén, activar las calderas correctas y
llevar el agua caliente en cubos), cumplir los pedidos a tiempo y limpiar las tinas para
el próximo cliente. Todo cronometrado y coordinándose entre los dos. A veces aparecen
eventos especiales (como el Espíritu del Hedor, que ensucia los pasillos y ralentiza a
los jugadores cercanos).

## Tecnologías
- Lenguaje: Java 17 (LTS)
- Framework: LibGDX 1.14.2
- Build: Gradle
- Mapas: Tiled Map Editor (.tmx)
- Red: sockets de Java (TCP + UDP), modelo cliente-servidor
- IDE: IntelliJ IDEA / Eclipse
- Plataforma objetivo: Escritorio (LWJGL3). No apuntamos a Web ni Móvil porque la parte
  de red usa sockets de Java que no corren en el navegador.

## Propuesta detallada (Wiki)
La propuesta completa está en la Wiki del repo:
https://github.com/JulianOlaizola/Juego-ElViajeDeChihiro/wiki/Propuesta-del-Proyecto

## Cómo compilar y ejecutar
Requisitos: Java 17+ y Git.

1. Clonar el repositorio:

       git clone https://github.com/JulianOlaizola/Juego-ElViajeDeChihiro.git
       cd Juego-ElViajeDeChihiro

2. Correr en escritorio (Linux/macOS):

       ./gradlew lwjgl3:run

   En Windows:

       gradlew.bat lwjgl3:run

3. Generar un ejecutable (opcional):

       ./gradlew lwjgl3:jar

   El .jar queda en lwjgl3/build/libs/.

Para jugar en red: un jugador inicia el servidor (Hostear) y el otro se conecta como
cliente ingresando la IP del host. Las dos máquinas tienen que estar en la misma red local.

## Estado actual
Pre-entrega N°1: configuración inicial del proyecto y del repositorio. Base de LibGDX
generada, repo armado y documentación iniciada.