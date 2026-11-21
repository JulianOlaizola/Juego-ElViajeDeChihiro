# Documentación de red

Esta sección explica cómo funciona la versión en red de **El viaje de Chihiro: Caos en la Casa de Baños**: quién calcula cada cosa, qué viaja por la red y qué pasa cuando algo falla.

## Páginas

1. [Arquitectura cliente-servidor](Arquitectura-cliente-servidor)
2. [Protocolo UDP](Protocolo-UDP)
3. [Hilos y sincronización](Hilos-y-sincronizacion)
4. [Box2D en el servidor](Box2D-en-el-servidor)
5. [Descubrimiento por broadcast](Descubrimiento-por-broadcast)
6. [Confiabilidad sobre UDP](Confiabilidad-sobre-UDP)
7. [Desconexiones y manejo de errores](Desconexiones-y-errores)
8. [Compilar, ejecutar y probar en 3 computadoras](Compilar-y-ejecutar)

## Resumen en cinco líneas

- Hay **tres programas** en juego: un servidor y dos clientes, cada uno en su computadora.
- Todo viaja por **UDP**. No usamos TCP en ninguna parte.
- El **servidor es la autoridad**: corre Box2D, las reglas, los pedidos, el tiempo y el puntaje.
- Los **clientes solo mandan teclas y dibujan** lo que el servidor les dice.
- Lo que no se puede perder (avisos, inicio y fin de partida) va con **número de evento, confirmación (ACK) y reenvío**.

## Números del protocolo

| Dato | Valor |
|---|---|
| Puerto por defecto | 7777 (se puede cambiar) |
| Tick del servidor | 60 por segundo |
| Estado enviado a cada cliente | 20 por segundo (cada 3 ticks) |
| Entradas enviadas por cada cliente | unas 30 por segundo (cada 33 ms) |
| Tamaño de un paquete de estado | unos 150 bytes |
| Tráfico total del servidor | alrededor de 6 KB/s con dos jugadores |
| Ping de medición | 1 por segundo |
| Reenvío de eventos sin confirmar | cada 200 ms |
| Tiempo para dar por caída una conexión | 5 segundos sin paquetes |
| Espera del servidor a un jugador caído | 30 segundos |
| Reintentos del cliente si se cae el servidor | 20 segundos |

