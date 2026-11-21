# Compilar, ejecutar y probar en 3 computadoras

## Requisitos

- JDK 17 o superior en la computadora que compila.
- Java 17 o superior en las computadoras que juegan (alcanza con el JRE).
- No hace falta instalar Gradle: el repositorio trae el *wrapper*.

## Generar los dos ejecutables

Desde la carpeta del proyecto:

```
gradlew.bat jars
```

En Linux o macOS: `./gradlew jars`

Queda todo en la carpeta `entrega/`:

```
entrega/
├── ElViajeDeChihiro-Cliente.jar
├── ElViajeDeChihiro-Servidor.jar
├── cliente.bat     cliente.sh
└── servidor.bat    servidor.sh
```

Los dos `.jar` incluyen las librerías y los recursos: se pueden copiar solos a otra computadora con un pendrive.

## Ejecutar

**Servidor** (puerto 7777 por defecto):

```
java -jar ElViajeDeChihiro-Servidor.jar
java -jar ElViajeDeChihiro-Servidor.jar 7778
```

Al arrancar muestra las IP de la computadora. Comandos de la consola: `estado`, `ip`, `terminar`, `ayuda`, `salir`.

**Cliente**:

```
java -jar ElViajeDeChihiro-Cliente.jar
java -jar ElViajeDeChihiro-Cliente.jar 192.168.0.10
```

El argumento es opcional: solo completa de antemano la pantalla "Conectar por IP".

En Windows también se puede hacer doble clic en `servidor.bat` y `cliente.bat`.

## Durante el desarrollo

```
gradlew.bat servidor:run
gradlew.bat lwjgl3:run
```

## Prueba en 3 computadoras

| PC | Qué corre |
|---|---|
| PC 1 | `ElViajeDeChihiro-Servidor.jar` |
| PC 2 | `ElViajeDeChihiro-Cliente.jar` (jugador 1) |
| PC 3 | `ElViajeDeChihiro-Cliente.jar` (jugador 2) |

1. Conectar las tres a la **misma red** (mismo router o mismo switch).
2. PC 1: abrir el servidor. Si Windows pregunta por el firewall, **permitir en redes privadas**. Anotar la IP que muestra la consola.
3. PC 2 y PC 3: abrir el cliente → **Buscar partida en la red**. Tiene que aparecer el servidor. Si no aparece, usar **Conectar por IP** con la IP anotada.
4. En la sala de espera, los dos aprietan ENTER. Empieza la partida.
5. Verificar:
   - los dos ven los mismos personajes en el mismo lugar;
   - el ping que muestra el HUD está por debajo de 50 ms en una red cableada;
   - pasarse un objeto entre jugadores mirándose y apretando E;
   - pausar con P en una PC y que se pause en la otra;
   - desconectar el cable de red de la PC 3 unos segundos: la PC 2 muestra "Esperando a tu compañero"; al reconectarlo, la partida queda en pausa y se reanuda con P;
   - en el servidor, escribir `terminar` y votar la revancha en las dos PC;
   - escribir `salir` en el servidor: los dos clientes vuelven al menú con el aviso.
6. Revisar `servidor.log` en la PC 1.

## Problemas frecuentes

| Síntoma | Causa probable | Solución |
|---|---|---|
| `java` no se reconoce como comando | No está Java instalado o no está en el PATH | Instalar Temurin 17 o 21 y abrir una consola nueva |
| "El puerto 7777 ya está en uso" | Quedó otro servidor abierto | Cerrarlo, o usar `java -jar ElViajeDeChihiro-Servidor.jar 7778` y conectarse con `IP:7778` |
| La búsqueda no encuentra el servidor | Firewall, red pública o broadcast bloqueado | Ver [Descubrimiento por broadcast](Descubrimiento-por-broadcast) y usar "Conectar por IP" |
| "El servidor no respondió" con la IP correcta | Firewall en la PC del servidor | Permitir Java en redes privadas en el Firewall de Windows |
| El juego anda pero "salta" | WiFi con mucha interferencia | Probar con cable |
