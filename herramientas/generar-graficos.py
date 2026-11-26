import sys, os
from collections import deque

DEST = sys.argv[1]
T = 32

PISO_MADERA, PISO_MADERA_B, AZULEJO, PARED, ZOCALO, ALFOMBRA, DEPOSITO, LAMPARA = 1, 2, 3, 4, 5, 6, 7, 8
SHOJI, SALES, CALDERA, TINA_CHICA, TINA_A, TINA_B, TINA_C, TINA_D = 9, 10, 11, 12, 13, 14, 15, 16
HIERBAS, PLANTA, FAROL, PIEDRA, PIEDRA_B, ALFOMBRA_AZUL, NOREN, MADERA_OSCURA = 17, 18, 19, 20, 21, 22, 23, 24


class Mapa:
    def __init__(self, ancho, alto, piso):
        self.ancho, self.alto = ancho, alto
        self.piso = [[piso[(x * 7 + y * 3) % len(piso)] for x in range(ancho)] for y in range(alto)]
        self.deco = [[0] * ancho for _ in range(alto)]
        self.colisiones = []
        self.interactivos = []
        self.ruta = []
        self.contadores = {}

    def pintar_piso(self, x0, y0, x1, y1, tiles):
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                self.piso[y][x] = tiles[(x * 7 + y * 3) % len(tiles)]

    def deco_rect(self, x0, y0, x1, y1, tile):
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                self.deco[y][x] = tile

    def pared(self, x0, y0, x1, y1):
        self.deco_rect(x0, y0, x1, y1, PARED)
        self.colision(x0, y0, x1, y1)

    def colision(self, x0, y0, x1, y1):
        self.colisiones.append((x0 * T, y0 * T, (x1 - x0 + 1) * T, (y1 - y0 + 1) * T))

    def objeto(self, tipo, x, y, w, h, nombre=None, solido=True):
        if nombre is None:
            self.contadores[tipo] = self.contadores.get(tipo, 0) + 1
            nombre = tipo if self.contadores[tipo] == 1 and tipo not in ('tina',) else '%s_%d' % (tipo, self.contadores[tipo])
        if solido:
            self.colision(x, y, x + w - 1, y + h - 1)
        self.interactivos.append((nombre, tipo, x * T, y * T, w * T, h * T))

    def tina(self, x, y):
        self.deco[y][x], self.deco[y][x + 1] = TINA_A, TINA_B
        self.deco[y + 1][x], self.deco[y + 1][x + 1] = TINA_C, TINA_D
        self.objeto('tina', x, y, 2, 2)

    def estacion(self, tipo, tile, x, y, w, h):
        self.deco_rect(x, y, x + w - 1, y + h - 1, tile)
        self.objeto(tipo, x, y, w, h)

    def adorno(self, tile, x, y):
        self.deco[y][x] = tile
        self.colision(x, y, x, y)

    def spawn(self, numero, x, y):
        self.interactivos.append(('spawn_jugador_%d' % numero, 'spawn_jugador_%d' % numero, x * T, y * T, T, T))

    def punto_ruta(self, x, y):
        self.ruta.append((x, y))

    def bordes(self):
        a, h = self.ancho, self.alto
        self.deco_rect(0, 0, a - 1, 1, PARED)
        for x in range(4, a - 2, 7):
            self.deco[1][x] = LAMPARA
        self.deco_rect(1, 2, a - 2, 2, ZOCALO)
        self.deco_rect(0, h - 1, a - 1, h - 1, PARED)
        self.deco_rect(0, 0, 0, h - 1, PARED)
        self.deco_rect(a - 1, 0, a - 1, h - 1, PARED)
        self.colisiones.append((0, 0, a * T, 3 * T))
        self.colisiones.append((0, (h - 1) * T, a * T, T))
        self.colisiones.append((0, 0, T, h * T))
        self.colisiones.append(((a - 1) * T, 0, T, h * T))

    def bloqueada(self, cx, cy):
        px, py = cx * T + T / 2, cy * T + T / 2
        for (x, y, w, h) in self.colisiones:
            if x <= px < x + w and y <= py < y + h:
                return True
        return False

    def verificar(self, nombre):
        inicio = next((x // T, y // T) for (n, t, x, y, w, h) in self.interactivos if t == 'spawn_jugador_1')
        visto = {inicio}
        cola = deque([inicio])
        while cola:
            cx, cy = cola.popleft()
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                n = (cx + dx, cy + dy)
                if 0 <= n[0] < self.ancho and 0 <= n[1] < self.alto and n not in visto and not self.bloqueada(*n):
                    visto.add(n)
                    cola.append(n)
        for (n, t, x, y, w, h) in self.interactivos:
            if t.startswith('spawn'):
                assert (x // T, y // T) in visto, nombre + ': spawn inalcanzable ' + n
                continue
            vecinos = set()
            for cx in range(x // T, (x + w) // T):
                vecinos.add((cx, y // T - 1))
                vecinos.add((cx, (y + h) // T))
            assert vecinos & visto, nombre + ': no se puede llegar a ' + n
        for (x, y) in self.ruta:
            assert (x, y) in visto, nombre + ': punto de ruta bloqueado %d,%d' % (x, y)
        for i in range(len(self.ruta)):
            (x0, y0), (x1, y1) = self.ruta[i], self.ruta[(i + 1) % len(self.ruta)]
            pasos = int(max(abs(x1 - x0), abs(y1 - y0)) * 8) + 1
            for k in range(pasos + 1):
                fx = x0 + (x1 - x0) * k / pasos
                fy = y0 + (y1 - y0) * k / pasos
                for ox in (-0.45, 0.45):
                    for oy in (-0.45, 0.45):
                        px, py = (fx + 0.5 + ox) * T, (fy + 0.5 + oy) * T
                        for (cx, cy, w, h) in self.colisiones:
                            assert not (cx <= px < cx + w and cy <= py < cy + h), \
                                nombre + ': el tramo %s -> %s atraviesa una pared' % ((x0, y0), (x1, y1))
        tipos = {t for (n, t, x, y, w, h) in self.interactivos}
        for necesario in ('tina', 'almacen_sales', 'caldera', 'deposito_suciedad', 'estante_hierbas',
                          'spawn_jugador_1', 'spawn_jugador_2'):
            assert necesario in tipos, nombre + ': falta ' + necesario
        print(nombre, 'OK:', sum(1 for i in self.interactivos if i[1] == 'tina'), 'tinas,',
              len(self.colisiones), 'colisiones,', len(self.ruta), 'puntos de ruta')

    def tmx(self):
        a, h = self.ancho, self.alto
        lineas = ['<?xml version="1.0" encoding="UTF-8"?>',
                  '<map version="1.10" tiledversion="1.10.2" orientation="orthogonal" renderorder="right-down" '
                  'width="%d" height="%d" tilewidth="32" tileheight="32" infinite="0" nextlayerid="5" nextobjectid="%d">'
                  % (a, h, len(self.colisiones) + len(self.interactivos) + len(self.ruta) + 1),
                  ' <tileset firstgid="1" name="casa_banos" tilewidth="32" tileheight="32" tilecount="24" columns="4">',
                  '  <image source="tileset_casa_banos.png" width="128" height="192"/>',
                  ' </tileset>']
        for i, (nombre, capa) in enumerate((('piso', self.piso), ('decoracion', self.deco))):
            lineas.append(' <layer id="%d" name="%s" width="%d" height="%d">' % (i + 1, nombre, a, h))
            lineas.append('  <data encoding="csv">')
            lineas.append(',\n'.join(','.join(str(v) for v in fila) for fila in capa))
            lineas.append('</data>')
            lineas.append(' </layer>')
        ident = 1
        lineas.append(' <objectgroup id="3" name="colisiones">')
        for (x, y, w, hh) in self.colisiones:
            lineas.append('  <object id="%d" x="%d" y="%d" width="%d" height="%d"/>' % (ident, x, y, w, hh))
            ident += 1
        lineas.append(' </objectgroup>')
        lineas.append(' <objectgroup id="4" name="interactivos">')
        objetos = list(self.interactivos)
        for i, (x, y) in enumerate(self.ruta):
            objetos.append(('ruta_hedor_%d' % (i + 1), 'ruta_hedor', x * T, y * T, T, T))
        for (nombre, tipo, x, y, w, hh) in objetos:
            lineas.append('  <object id="%d" name="%s" x="%d" y="%d" width="%d" height="%d">' % (ident, nombre, x, y, w, hh))
            lineas.append('   <properties>')
            lineas.append('    <property name="tipo" value="%s"/>' % tipo)
            lineas.append('   </properties>')
            lineas.append('  </object>')
            ident += 1
        lineas.append(' </objectgroup>')
        lineas.append('</map>')
        return '\n'.join(lineas) + '\n'


def nivel2():
    m = Mapa(44, 28, [PISO_MADERA, PISO_MADERA, PISO_MADERA, PISO_MADERA_B])
    m.bordes()
    m.pintar_piso(2, 3, 25, 10, [AZULEJO])
    for x in (4, 10, 16, 22):
        m.tina(x, 5)
    m.pintar_piso(2, 18, 15, 25, [AZULEJO])
    for x in (4, 10):
        m.tina(x, 21)
    m.pared(2, 13, 14, 13)
    m.pared(19, 13, 34, 13)
    m.pared(28, 3, 28, 9)
    m.pintar_piso(1, 14, 42, 15, [ALFOMBRA_AZUL])
    m.pared(18, 18, 18, 26)
    m.estacion('almacen_sales', SALES, 36, 3, 4, 2)
    m.estacion('estante_hierbas', HIERBAS, 31, 3, 4, 2)
    m.estacion('caldera', CALDERA, 36, 23, 4, 2)
    m.estacion('deposito_suciedad', DEPOSITO, 22, 24, 2, 2)
    for (x, y) in ((1, 12), (42, 12), (17, 25), (42, 25), (27, 3)):
        m.adorno(PLANTA, x, y)
    for (x, y) in ((36, 12), (24, 18)):
        m.adorno(FAROL, x, y)
    m.spawn(1, 30, 19)
    m.spawn(2, 32, 19)
    for (x, y) in ((6, 15), (16, 16), (38, 16), (38, 9), (30, 11), (10, 11), (16, 12), (16, 15)):
        m.punto_ruta(x, y)
    return m


def nivel3():
    m = Mapa(46, 30, [PIEDRA, PIEDRA, PIEDRA_B])
    m.bordes()
    m.pintar_piso(1, 3, 20, 13, [MADERA_OSCURA, PISO_MADERA])
    m.pintar_piso(25, 3, 44, 13, [MADERA_OSCURA, PISO_MADERA])
    m.pintar_piso(3, 4, 18, 9, [AZULEJO])
    m.pintar_piso(27, 4, 42, 9, [AZULEJO])
    for x in (5, 10, 15):
        m.tina(x, 5)
    for x in (29, 34, 39):
        m.tina(x, 5)
    m.pared(1, 13, 8, 13)
    m.pared(13, 13, 21, 13)
    m.pared(21, 3, 22, 10)
    m.pared(24, 13, 32, 13)
    m.pared(37, 13, 44, 13)
    m.pintar_piso(1, 15, 44, 16, [ALFOMBRA])
    m.pared(14, 20, 31, 21)
    m.pintar_piso(14, 22, 31, 22, [ZOCALO])
    m.estacion('almacen_sales', SALES, 2, 25, 4, 2)
    m.estacion('estante_hierbas', HIERBAS, 40, 25, 4, 2)
    m.estacion('caldera', CALDERA, 21, 25, 4, 2)
    m.estacion('deposito_suciedad', DEPOSITO, 2, 18, 2, 2)
    m.estacion('deposito_suciedad', DEPOSITO, 42, 18, 2, 2)
    for (x, y) in ((1, 12), (20, 12), (25, 12), (44, 12)):
        m.adorno(FAROL, x, y)
    for (x, y) in ((13, 27), (32, 27), (10, 18), (35, 18)):
        m.adorno(PLANTA, x, y)
    m.spawn(1, 21, 17)
    m.spawn(2, 24, 17)
    for (x, y) in ((10, 11), (10, 16), (5, 17), (8, 23), (22, 23), (36, 23), (39, 17), (35, 16), (35, 11),
                   (23, 11), (23, 16), (12, 16)):
        m.punto_ruta(x, y)
    return m


for archivo, generador in (('nivel2.tmx', nivel2), ('nivel3.tmx', nivel3)):
    mapa = generador()
    mapa.verificar(archivo)
    with open(os.path.join(DEST, archivo), 'w') as f:
        f.write(mapa.tmx())
