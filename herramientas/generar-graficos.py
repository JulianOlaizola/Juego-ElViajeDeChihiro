import sys, os, math, random
from PIL import Image, ImageDraw, ImageFont, ImageFilter

DEST = sys.argv[1]
T = 32
random.seed(7)

def hexa(c, a=255):
    c = c.lstrip('#')
    return (int(c[0:2], 16), int(c[2:4], 16), int(c[4:6], 16), a)

def mezclar(c1, c2, t):
    return tuple(int(c1[i] + (c2[i] - c1[i]) * t) for i in range(4))

def contorno(img, color=hexa('1b1420')):
    a = img.split()[3]
    dil = a.filter(ImageFilter.MaxFilter(3))
    capa = Image.new('RGBA', img.size, color)
    mask = Image.eval(dil, lambda v: 255 if v > 0 else 0)
    base = Image.new('RGBA', img.size, (0, 0, 0, 0))
    base.paste(capa, (0, 0), mask)
    base.alpha_composite(img)
    return base

def px(d, x, y, c):
    d.point((x, y), fill=c)

def rect(d, x0, y0, x1, y1, c):
    d.rectangle([x0, y0, x1, y1], fill=c)

def elipse(d, x0, y0, x1, y1, c):
    d.ellipse([x0, y0, x1, y1], fill=c)

def tile():
    return Image.new('RGBA', (T, T), (0, 0, 0, 0))

def ruido(d, x0, y0, x1, y1, colores, cantidad):
    for _ in range(cantidad):
        px(d, random.randint(x0, x1), random.randint(y0, y1), random.choice(colores))

MADERA = [hexa('c99a5e'), hexa('b8864d'), hexa('d9ab6c'), hexa('a8773f')]

def piso_madera(variante):
    im = tile(); d = ImageDraw.Draw(im)
    rect(d, 0, 0, 31, 31, MADERA[0])
    for fila in range(4):
        y = fila * 8
        tono = MADERA[(fila + variante) % 3]
        rect(d, 0, y, 31, y + 7, tono)
        rect(d, 0, y + 7, 31, y + 7, MADERA[3])
        rect(d, 0, y, 31, y, mezclar(tono, hexa('ffffff'), 0.18))
        corte = (fila * 11 + variante * 7) % 28 + 2
        rect(d, corte, y, corte, y + 7, MADERA[3])
        for _ in range(3):
            gx = random.randint(1, 29); gy = y + random.randint(2, 5)
            rect(d, gx, gy, gx + random.randint(2, 5), gy, mezclar(tono, MADERA[3], 0.5))
    if variante == 1:
        elipse(d, 18, 10, 22, 13, hexa('8a5f31')); px(d, 20, 11, hexa('6e4824'))
    return im

def azulejo():
    im = tile(); d = ImageDraw.Draw(im)
    rect(d, 0, 0, 31, 31, hexa('9fd3e0'))
    for i in range(2):
        for j in range(2):
            x, y = i * 16, j * 16
            rect(d, x, y, x + 15, y + 15, hexa('8cc6d6') if (i + j) % 2 else hexa('a8dbe7'))
            rect(d, x, y, x + 15, y, hexa('7ab3c4')); rect(d, x, y, x, y + 15, hexa('7ab3c4'))
            rect(d, x + 2, y + 2, x + 5, y + 2, hexa('e4f6fa')); px(d, x + 2, y + 3, hexa('e4f6fa'))
    return im

def pared():
    im = tile(); d = ImageDraw.Draw(im)
    rect(d, 0, 0, 31, 31, hexa('5a3a2c'))
    for x in range(0, 32, 8):
        rect(d, x, 0, x + 7, 31, hexa('624032') if (x // 8) % 2 else hexa('553628'))
        rect(d, x, 0, x, 31, hexa('3e271d'))
        rect(d, x + 1, 0, x + 1, 31, hexa('6f4a3a'))
    rect(d, 0, 14, 31, 17, hexa('3e271d')); rect(d, 0, 14, 31, 14, hexa('7a5240'))
    ruido(d, 0, 0, 31, 31, [hexa('4b2f23'), hexa('6a4636')], 18)
    return im

def zocalo():
    im = tile(); d = ImageDraw.Draw(im)
    rect(d, 0, 0, 31, 31, hexa('8c6a56'))
    rect(d, 0, 0, 31, 3, hexa('3e271d'))
    rect(d, 0, 4, 31, 5, hexa('b08a70'))
    rect(d, 0, 26, 31, 31, hexa('6b4c3c'))
    for x in range(2, 32, 10):
        rect(d, x, 9, x + 6, 21, hexa('9b7862')); rect(d, x, 9, x + 6, 9, hexa('b89680'))
    return im

def alfombra():
    im = tile(); d = ImageDraw.Draw(im)
    rect(d, 0, 0, 31, 31, hexa('a8322f'))
    rect(d, 0, 0, 31, 1, hexa('e0b24a')); rect(d, 0, 30, 31, 31, hexa('e0b24a'))
    rect(d, 0, 2, 31, 2, hexa('7a2020')); rect(d, 0, 29, 31, 29, hexa('7a2020'))
    for x in range(0, 32, 16):
        d.polygon([(x + 8, 8), (x + 14, 15), (x + 8, 22), (x + 2, 15)], fill=hexa('c4463c'))
        d.polygon([(x + 8, 11), (x + 11, 15), (x + 8, 19), (x + 5, 15)], fill=hexa('e0b24a'))
    return im

def deposito():
    im = tile(); d = ImageDraw.Draw(im)
    rect(d, 0, 0, 31, 31, hexa('6b4426'))
    rect(d, 2, 2, 29, 29, hexa('3a2a1c'))
    ruido(d, 4, 4, 27, 27, [hexa('4a3a22'), hexa('2a1e14'), hexa('5a4a2a')], 60)
    rect(d, 0, 0, 31, 1, hexa('8b5a34')); rect(d, 0, 30, 31, 31, hexa('4a2e1a'))
    rect(d, 0, 0, 1, 31, hexa('8b5a34')); rect(d, 30, 0, 31, 31, hexa('4a2e1a'))
    rect(d, 15, 0, 16, 31, hexa('7a4e2c'))
    return im

def lampara():
    im = pared(); d = ImageDraw.Draw(im)
    rect(d, 15, 2, 16, 7, hexa('2a1a14'))
    elipse(d, 8, 7, 23, 28, hexa('d94a3a'))
    elipse(d, 10, 9, 21, 26, hexa('f07a4a'))
    elipse(d, 12, 12, 19, 23, hexa('ffd27a'))
    for y in (11, 17, 23):
        rect(d, 9, y, 22, y, hexa('a8322f'))
    rect(d, 12, 6, 19, 7, hexa('2a1a14')); rect(d, 12, 28, 19, 29, hexa('2a1a14'))
    return im

def shoji():
    im = tile(); d = ImageDraw.Draw(im)
    rect(d, 0, 0, 31, 31, hexa('5a3a2c'))
    rect(d, 2, 2, 29, 31, hexa('f2ead6'))
    for x in (2, 11, 20, 29):
        rect(d, x, 2, x, 31, hexa('7a5240'))
    for y in (2, 10, 18, 26):
        rect(d, 2, y, 29, y, hexa('7a5240'))
    rect(d, 3, 3, 10, 9, hexa('fff8e6'))
    return im

def estante(colores):
    im = tile(); d = ImageDraw.Draw(im)
    rect(d, 0, 0, 31, 31, hexa('5e3b22'))
    for y in (0, 15):
        rect(d, 1, y + 1, 30, y + 13, hexa('3c2516'))
        rect(d, 0, y + 14, 31, y + 15, hexa('8b5a34'))
        for i, x in enumerate((3, 11, 19, 25)):
            c = colores[(i + y // 15) % len(colores)]
            ancho = 5 if i < 3 else 4
            rect(d, x, y + 5, x + ancho, y + 13, hexa(c))
            rect(d, x + 1, y + 3, x + ancho - 1, y + 4, hexa('d8c8a8'))
            px(d, x + 1, y + 7, hexa('ffffff', 180))
    return im

def hierbas_estante():
    im = tile(); d = ImageDraw.Draw(im)
    rect(d, 0, 0, 31, 31, hexa('5e3b22'))
    for y in (0, 15):
        rect(d, 1, y + 1, 30, y + 13, hexa('3c2516'))
        rect(d, 0, y + 14, 31, y + 15, hexa('8b5a34'))
        for x in (2, 12, 22):
            rect(d, x + 3, y + 2, x + 4, y + 8, hexa('7a5a2a'))
            for k in range(6):
                hx = x + random.randint(0, 7); hy = y + random.randint(6, 12)
                rect(d, hx, hy, hx + 1, hy + 1, random.choice([hexa('4f9a3f'), hexa('6fbf4f'), hexa('3a7a2f')]))
            rect(d, x + 2, y + 5, x + 5, y + 5, hexa('c9a24a'))
    return im

def caldera():
    im = tile(); d = ImageDraw.Draw(im)
    rect(d, 0, 0, 31, 31, hexa('4a4650'))
    rect(d, 2, 4, 29, 29, hexa('6a6672'))
    rect(d, 2, 4, 29, 5, hexa('8a8694'))
    rect(d, 13, 0, 18, 4, hexa('34303a'))
    rect(d, 8, 13, 23, 25, hexa('2a2228'))
    elipse(d, 10, 16, 21, 26, hexa('e8642a'))
    elipse(d, 12, 18, 19, 25, hexa('ffb03a'))
    elipse(d, 14, 21, 17, 25, hexa('fff07a'))
    for x in (4, 27):
        px(d, x, 8, hexa('a8a4b0')); px(d, x, 27, hexa('a8a4b0'))
    return im

def tina_simple():
    im = tile(); d = ImageDraw.Draw(im)
    rect(d, 1, 1, 30, 30, hexa('8b5a34'))
    rect(d, 4, 4, 27, 27, hexa('5ab4d0'))
    rect(d, 4, 4, 27, 6, hexa('8fd6ea'))
    for x in (1, 15, 30):
        rect(d, x, 1, x, 30, hexa('6b4426'))
    return im

def tina_cuarto(cuarto):
    grande = Image.new('RGBA', (64, 64), (0, 0, 0, 0)); d = ImageDraw.Draw(grande)
    d.rounded_rectangle([1, 1, 62, 62], 10, fill=hexa('8b5a34'))
    for x in range(4, 62, 7):
        rect(d, x, 1, x, 62, hexa('7a4e2c'))
    rect(d, 1, 18, 62, 20, hexa('c9a24a')); rect(d, 1, 46, 62, 48, hexa('c9a24a'))
    d.rounded_rectangle([7, 7, 56, 56], 8, fill=hexa('3e8fb0'))
    d.rounded_rectangle([9, 9, 54, 54], 7, fill=hexa('5ab4d0'))
    for (x, y) in ((14, 14), (30, 24), (20, 38), (40, 42), (44, 16)):
        rect(d, x, y, x + 5, y, hexa('a8e6f4'))
    d.arc([12, 10, 30, 22], 200, 320, fill=hexa('ffffff', 200))
    grande = contorno(grande)
    x = (cuarto % 2) * 32; y = (cuarto // 2) * 32
    return grande.crop((x, y, x + 32, y + 32))

def planta():
    im = tile(); d = ImageDraw.Draw(im)
    rect(d, 10, 21, 21, 30, hexa('9a5a34')); rect(d, 9, 20, 22, 22, hexa('b86e42'))
    for (x, y, c) in ((15, 4, '3a7a2f'), (8, 9, '4f9a3f'), (20, 8, '4f9a3f'), (12, 13, '6fbf4f'), (18, 14, '3a7a2f'), (6, 15, '6fbf4f'), (23, 15, '6fbf4f')):
        elipse(d, x - 3, y - 2, x + 4, y + 5, hexa(c))
    return contorno(im)

def farol():
    im = tile(); d = ImageDraw.Draw(im)
    rect(d, 14, 26, 17, 31, hexa('5a5660')); rect(d, 10, 28, 21, 31, hexa('4a4650'))
    rect(d, 9, 9, 22, 25, hexa('6a6672'))
    rect(d, 11, 12, 20, 22, hexa('ffd27a')); rect(d, 13, 14, 18, 20, hexa('fff0b0'))
    d.polygon([(6, 9), (16, 2), (25, 9)], fill=hexa('4a4650'))
    return contorno(im)

def piedra(variante):
    im = tile(); d = ImageDraw.Draw(im)
    rect(d, 0, 0, 31, 31, hexa('5c5a66'))
    bloques = [(0, 0, 14, 10), (15, 0, 31, 10), (0, 11, 9, 21), (10, 11, 24, 21), (25, 11, 31, 21), (0, 22, 17, 31), (18, 22, 31, 31)]
    for i, (x0, y0, x1, y1) in enumerate(bloques):
        tono = [hexa('7a7884'), hexa('6e6c78'), hexa('85838f')][(i + variante) % 3]
        rect(d, x0 + 1, y0 + 1, x1 - 1, y1 - 1, tono)
        rect(d, x0 + 1, y0 + 1, x1 - 1, y0 + 1, mezclar(tono, hexa('ffffff'), 0.2))
    if variante:
        ruido(d, 0, 0, 31, 31, [hexa('4f8a4f'), hexa('5f9a5a')], 14)
    return im

def alfombra_azul():
    im = tile(); d = ImageDraw.Draw(im)
    rect(d, 0, 0, 31, 31, hexa('2f4f8a'))
    rect(d, 0, 0, 31, 1, hexa('e0d6b0')); rect(d, 0, 30, 31, 31, hexa('e0d6b0'))
    for x in range(0, 32, 8):
        elipse(d, x + 1, 12, x + 6, 19, hexa('3f66a8')); px(d, x + 3, 15, hexa('e0d6b0'))
    return im

def noren():
    im = pared(); d = ImageDraw.Draw(im)
    rect(d, 1, 3, 30, 4, hexa('2a1a14'))
    for i, x in enumerate((2, 12, 22)):
        rect(d, x, 5, x + 8, 27, hexa('2f4f8a') if i != 1 else hexa('3a5c9a'))
        rect(d, x, 27, x + 8, 28, hexa('24406e'))
    elipse(d, 10, 10, 21, 21, hexa('f2ead6')); elipse(d, 13, 13, 18, 18, hexa('d94a3a'))
    return im

def tileset():
    tiles = [piso_madera(0), piso_madera(1), azulejo(), pared(), zocalo(), alfombra(), deposito(), lampara(),
             shoji(), estante(['9a6ad0', '6ac0d8', 'e8a84a', 'd86a8a']), caldera(), tina_simple(),
             tina_cuarto(0), tina_cuarto(1), tina_cuarto(2), tina_cuarto(3),
             hierbas_estante(), planta(), farol(), piedra(0), piedra(1), alfombra_azul(), noren(), piso_madera(2)]
    hoja = Image.new('RGBA', (128, 192), (0, 0, 0, 0))
    for i, t in enumerate(tiles):
        hoja.alpha_composite(t, ((i % 4) * 32, (i // 4) * 32))
    return hoja

def personaje(cabello, chaqueta, banda, pantalon, piel, direccion, cuadro, peinado):
    im = tile(); d = ImageDraw.Draw(im)
    pierna = [0, 1, 0, -1][cuadro]
    salto = [0, -1, 0, -1][cuadro]
    elipse(d, 9, 27, 22, 31, (0, 0, 0, 70))
    sombra = Image.new('RGBA', (T, T), (0, 0, 0, 0)); sombra.alpha_composite(im)
    cuerpo = Image.new('RGBA', (T, T), (0, 0, 0, 0)); c = ImageDraw.Draw(cuerpo)
    oy = salto
    if direccion in (0, 3):
        rect(c, 12, 23 + oy + max(0, pierna), 14, 28 + oy, pantalon)
        rect(c, 17, 23 + oy + max(0, -pierna), 19, 28 + oy, pantalon)
        rect(c, 12, 28 + oy, 14, 29 + oy, hexa('f2ead6')); rect(c, 17, 28 + oy, 19, 29 + oy, hexa('f2ead6'))
    else:
        rect(c, 14 - pierna, 23 + oy, 16 - pierna, 28 + oy, pantalon)
        rect(c, 15 + pierna, 23 + oy, 17 + pierna, 28 + oy, mezclar(pantalon, hexa('000000'), 0.25))
        rect(c, 14 - pierna, 28 + oy, 16 - pierna, 29 + oy, hexa('f2ead6'))
    rect(c, 10, 14 + oy, 21, 24 + oy, chaqueta)
    rect(c, 10, 14 + oy, 21, 15 + oy, mezclar(chaqueta, hexa('ffffff'), 0.2))
    rect(c, 10, 20 + oy, 21, 21 + oy, banda)
    if direccion == 0:
        d2 = 15
        rect(c, d2, 14 + oy, d2 + 1, 20 + oy, banda)
    brazo = [0, 1, 0, -1][cuadro]
    if direccion in (0, 3):
        rect(c, 8, 15 + oy + brazo, 9, 21 + oy + brazo, chaqueta); px(c, 8, 22 + oy + brazo, piel); px(c, 9, 22 + oy + brazo, piel)
        rect(c, 22, 15 + oy - brazo, 23, 21 + oy - brazo, chaqueta); px(c, 22, 22 + oy - brazo, piel); px(c, 23, 22 + oy - brazo, piel)
    else:
        lado = 1 if direccion == 2 else -1
        bx = 15 + brazo * lado
        rect(c, bx, 16 + oy, bx + 2, 22 + oy, mezclar(chaqueta, hexa('000000'), 0.15)); rect(c, bx, 23 + oy, bx + 2, 23 + oy, piel)
    elipse(c, 9, 2 + oy, 22, 15 + oy, piel)
    if direccion == 3:
        elipse(c, 8, 1 + oy, 23, 15 + oy, cabello)
    elif direccion == 0:
        elipse(c, 8, 1 + oy, 23, 9 + oy, cabello)
        rect(c, 8, 5 + oy, 9, 12 + oy, cabello); rect(c, 22, 5 + oy, 23, 12 + oy, cabello)
        px(c, 12, 10 + oy, hexa('1b1420')); px(c, 12, 11 + oy, hexa('1b1420'))
        px(c, 19, 10 + oy, hexa('1b1420')); px(c, 19, 11 + oy, hexa('1b1420'))
        px(c, 11, 12 + oy, hexa('f0a0a0')); px(c, 20, 12 + oy, hexa('f0a0a0'))
        rect(c, 15, 13 + oy, 16, 13 + oy, hexa('b0605a'))
    else:
        elipse(c, 8, 1 + oy, 23, 9 + oy, cabello)
        if direccion == 2:
            rect(c, 8, 5 + oy, 14, 13 + oy, cabello)
            px(c, 19, 10 + oy, hexa('1b1420')); px(c, 19, 11 + oy, hexa('1b1420')); px(c, 21, 12 + oy, hexa('f0a0a0'))
        else:
            rect(c, 17, 5 + oy, 23, 13 + oy, cabello)
            px(c, 12, 10 + oy, hexa('1b1420')); px(c, 12, 11 + oy, hexa('1b1420')); px(c, 10, 12 + oy, hexa('f0a0a0'))
    if peinado == 'rodete':
        elipse(c, 12, -1 + oy, 19, 5 + oy, cabello) if direccion != 0 else elipse(c, 12, 0 + oy, 19, 4 + oy, cabello)
        rect(c, 10, 4 + oy, 21, 4 + oy, hexa('e8c24a'))
    else:
        for x in (9, 13, 17, 21):
            px(c, x, 1 + oy, cabello); px(c, x + 1, 0 + oy, cabello)
        rect(c, 9, 5 + oy, 22, 5 + oy, hexa('f2ead6')) if direccion != 3 else None
    cuerpo = contorno(cuerpo)
    sombra.alpha_composite(cuerpo)
    return sombra

def jugadores():
    hoja = Image.new('RGBA', (128, 256), (0, 0, 0, 0))
    estilos = [
        (hexa('6b3a22'), hexa('e8843a'), hexa('f2ead6'), hexa('5a3a5a'), hexa('f7d2b0'), 'rodete'),
        (hexa('1e2230'), hexa('34508c'), hexa('f2ead6'), hexa('2a2a3a'), hexa('eac29c'), 'vincha'),
    ]
    for j, estilo in enumerate(estilos):
        for direccion in range(4):
            for cuadro in range(4):
                hoja.alpha_composite(personaje(*estilo[:5], direccion, cuadro, estilo[5]),
                                     (cuadro * 32, (j * 4 + direccion) * 32))
    return hoja

def espiritu(tipo, cuadro):
    im = tile(); c = ImageDraw.Draw(im)
    oy = [0, -1, -2, -1][cuadro]
    if tipo == 0:
        elipse(c, 7, 5 + oy, 24, 24 + oy, hexa('ece6f6'))
        for i, x in enumerate((8, 12, 16, 20)):
            elipse(c, x, 20 + oy + (1 if (i + cuadro) % 2 else 0), x + 4, 27 + oy + (1 if (i + cuadro) % 2 else 0), hexa('ece6f6'))
        elipse(c, 9, 6 + oy, 16, 12 + oy, hexa('ffffff'))
        rect(c, 12, 13 + oy, 13, 15 + oy, hexa('2a2440')); rect(c, 18, 13 + oy, 19, 15 + oy, hexa('2a2440'))
        rect(c, 10, 16 + oy, 11, 16 + oy, hexa('f0a8c0')); rect(c, 20, 16 + oy, 21, 16 + oy, hexa('f0a8c0'))
        rect(c, 15, 17 + oy, 16, 17 + oy, hexa('8a6aa0'))
    elif tipo == 1:
        elipse(c, 6, 8 + oy, 25, 27 + oy, hexa('5faa4a'))
        elipse(c, 8, 10 + oy, 15, 16 + oy, hexa('7fca5a'))
        c.polygon([(16, 9 + oy), (12, 1 + oy), (20, 3 + oy)], fill=hexa('3a8a2f'))
        rect(c, 16, 3 + oy, 16, 9 + oy, hexa('2a6a22'))
        for (x, y) in ((9, 22), (21, 20), (19, 25)):
            elipse(c, x, y + oy, x + 2, y + 2 + oy, hexa('3f7a34'))
        rect(c, 12, 15 + oy, 13, 17 + oy, hexa('2a2016')); rect(c, 18, 15 + oy, 19, 17 + oy, hexa('2a2016'))
        rect(c, 14, 20 + oy, 17, 20 + oy, hexa('2a5a22'))
    elif tipo == 2:
        c.polygon([(16, 2 + oy), (24, 16 + oy), (16, 16 + oy), (8, 16 + oy)], fill=hexa('6ab8ee'))
        elipse(c, 7, 9 + oy, 25, 27 + oy, hexa('6ab8ee'))
        elipse(c, 10, 12 + oy, 15, 18 + oy, hexa('b0e0ff'))
        rect(c, 12, 17 + oy, 13, 19 + oy, hexa('1a2a4a')); rect(c, 18, 17 + oy, 19, 19 + oy, hexa('1a2a4a'))
        rect(c, 15, 22 + oy, 16, 22 + oy, hexa('3a6aa0'))
        for k in range(2):
            x = (5 + cuadro * 5 + k * 13) % 26 + 2
            px(c, x, 4 + k * 6, hexa('ffffff')); px(c, x, 5 + k * 6, hexa('b0e0ff'))
    else:
        elipse(c, 3, 9 + oy, 28, 29 + oy, hexa('6a5a2a'))
        elipse(c, 5, 6 + oy, 21, 21 + oy, hexa('7a6a34'))
        elipse(c, 13, 11 + oy, 27, 24 + oy, hexa('5a4a22'))
        for (x, y) in ((6, 26), (13, 27), (22, 26)):
            rect(c, x, y + oy, x + 1, y + 3 + oy + (cuadro % 2), hexa('5a4a22'))
        for (x, y) in ((8, 12), (20, 18), (12, 22)):
            elipse(c, x, y + oy, x + 3, y + 2 + oy, hexa('8a8040'))
        rect(c, 10, 14 + oy, 12, 16 + oy, hexa('e8d84a')); rect(c, 18, 14 + oy, 20, 16 + oy, hexa('e8d84a'))
        px(c, 11, 15 + oy, hexa('1b1420')); px(c, 19, 15 + oy, hexa('1b1420'))
        rect(c, 12, 20 + oy, 18, 21 + oy, hexa('3a2e14'))
    im = contorno(im, hexa('1b1420') if tipo != 2 else hexa('1a3a5a'))
    if tipo == 3:
        d = ImageDraw.Draw(im)
        for k in range(3):
            x = 6 + k * 9 + (cuadro + k) % 2 * 2
            y = 1 + (cuadro * 3 + k * 2) % 5
            px(d, x, y, hexa('1b1420')); px(d, x + 1, y, hexa('9a9aa0', 200))
        for k in range(2):
            x = 4 + k * 22
            for s in range(4):
                px(d, x + (1 if (s + cuadro) % 2 else 0), 10 + s * 2, hexa('8ab04a', 200))
    sombra = Image.new('RGBA', (T, T), (0, 0, 0, 0))
    ImageDraw.Draw(sombra).ellipse([8, 28, 23, 31], fill=(0, 0, 0, 60))
    sombra.alpha_composite(im)
    return sombra

def espiritus():
    hoja = Image.new('RGBA', (128, 128), (0, 0, 0, 0))
    for tipo in range(4):
        for cuadro in range(4):
            hoja.alpha_composite(espiritu(tipo, cuadro), (cuadro * 32, tipo * 32))
    return hoja

def objeto_sal():
    im = tile(); d = ImageDraw.Draw(im)
    rect(d, 9, 9, 22, 28, hexa('d8eef4', 220))
    rect(d, 10, 14, 21, 27, hexa('9a6ad0'))
    ruido(d, 10, 14, 21, 27, [hexa('c8a8f0'), hexa('7a4ab0'), hexa('ffffff')], 22)
    rect(d, 10, 4, 21, 8, hexa('b8864d')); rect(d, 10, 4, 21, 4, hexa('d9ab6c'))
    rect(d, 11, 10, 12, 25, hexa('ffffff', 160))
    return contorno(im)

def objeto_balde():
    im = tile(); d = ImageDraw.Draw(im)
    d.polygon([(6, 12), (25, 12), (23, 28), (8, 28)], fill=hexa('a8773f'))
    for x in (9, 13, 17, 21):
        rect(d, x, 13, x, 27, hexa('8b5a34'))
    rect(d, 6, 15, 25, 16, hexa('5a5660')); rect(d, 7, 24, 24, 25, hexa('5a5660'))
    elipse(d, 6, 9, 25, 15, hexa('5ab4d0')); elipse(d, 9, 10, 18, 13, hexa('8fd6ea'))
    d.arc([7, 1, 24, 18], 180, 360, fill=hexa('5a5660'), width=2)
    im = contorno(im)
    d = ImageDraw.Draw(im)
    for (x, y) in ((11, 5), (17, 3), (21, 6)):
        px(d, x, y, hexa('ffffff', 170)); px(d, x + 1, y - 1, hexa('ffffff', 120))
    return im

def objeto_suciedad():
    im = tile(); d = ImageDraw.Draw(im)
    elipse(d, 5, 12, 26, 28, hexa('6a5a2a'))
    elipse(d, 9, 8, 22, 22, hexa('7a6a34'))
    elipse(d, 11, 11, 15, 14, hexa('9a8a4a'))
    for (x, y) in ((8, 20), (19, 16), (14, 24)):
        elipse(d, x, y, x + 3, y + 2, hexa('4a3e1a'))
    im = contorno(im)
    d = ImageDraw.Draw(im)
    for x in (9, 16, 23):
        for s in range(3):
            px(d, x + (s % 2), 2 + s * 2, hexa('8ab04a', 210))
    return im

def objeto_hierbas():
    im = tile(); d = ImageDraw.Draw(im)
    for (x0, y0, x1, y1) in ((15, 6, 12, 27), (16, 5, 17, 27), (18, 7, 21, 27)):
        d.line([(x0, y0), (x1, y1)], fill=hexa('3a7a2f'), width=1)
    for (x, y, c) in ((11, 6, '6fbf4f'), (16, 3, '4f9a3f'), (21, 6, '6fbf4f'), (9, 11, '4f9a3f'), (23, 11, '4f9a3f'), (14, 9, '8fdf6f'), (19, 10, '8fdf6f')):
        elipse(d, x - 3, y - 2, x + 3, y + 3, hexa(c))
    rect(d, 12, 19, 20, 21, hexa('c9a24a')); rect(d, 12, 19, 20, 19, hexa('e8c86a'))
    return contorno(im)

def objeto_mancha():
    im = tile(); d = ImageDraw.Draw(im)
    elipse(d, 1, 10, 30, 27, hexa('5a4a22', 230))
    elipse(d, 5, 7, 18, 18, hexa('6a5a2a', 230))
    elipse(d, 16, 16, 28, 25, hexa('6a5a2a', 230))
    elipse(d, 7, 12, 11, 14, hexa('9a8a4a', 230)); elipse(d, 19, 19, 22, 21, hexa('9a8a4a', 230))
    return contorno(im, hexa('2a2010', 200))

def objeto_tina_sucia():
    im = tile(); d = ImageDraw.Draw(im)
    for (x, y, r) in ((8, 9, 5), (21, 12, 4), (13, 21, 6), (24, 23, 3), (6, 24, 2)):
        elipse(d, x - r, y - r, x + r, y + r, hexa('6a5a2a', 235))
        elipse(d, x - r + 1, y - r + 1, x, y, hexa('8a7a3a', 235))
    d.arc([3, 3, 28, 28], 0, 360, fill=hexa('5a4a22', 200), width=2)
    im = contorno(im, hexa('2a2010', 160))
    d = ImageDraw.Draw(im)
    for x in (10, 20):
        for s in range(3):
            px(d, x + (s % 2), 1 + s * 2, hexa('8ab04a', 220))
    return im

def objeto_vapor():
    im = tile(); d = ImageDraw.Draw(im)
    for (x, y, r, a) in ((10, 22, 5, 150), (18, 16, 6, 130), (12, 9, 5, 110), (21, 5, 4, 90)):
        elipse(d, x - r, y - r, x + r, y + r, hexa('ffffff', a))
    return im

def objetos():
    hoja = Image.new('RGBA', (128, 64), (0, 0, 0, 0))
    lista = [objeto_sal(), objeto_balde(), objeto_suciedad(), tile(),
             objeto_hierbas(), objeto_mancha(), objeto_tina_sucia(), objeto_vapor()]
    for i, o in enumerate(lista):
        hoja.alpha_composite(o, ((i % 4) * 32, (i // 4) * 32))
    return hoja

def logo(ruta_fuente):
    ancho, alto = 640, 160
    im = Image.new('RGBA', (ancho, alto), (0, 0, 0, 0)); d = ImageDraw.Draw(im)
    for y in range(alto):
        t = y / alto
        d.line([(0, y), (ancho, y)], fill=mezclar(hexa('3a1e2a'), hexa('1e1424'), t))
    mascara = Image.new('L', (ancho, alto), 0)
    ImageDraw.Draw(mascara).rounded_rectangle([0, 0, ancho - 1, alto - 1], 18, fill=255)
    fondo = Image.new('RGBA', (ancho, alto), (0, 0, 0, 0)); fondo.paste(im, (0, 0), mascara)
    d = ImageDraw.Draw(fondo)
    d.rounded_rectangle([4, 4, ancho - 5, alto - 5], 15, outline=hexa('e0b24a'), width=3)
    d.rounded_rectangle([10, 10, ancho - 11, alto - 11], 11, outline=hexa('a8322f'), width=2)
    for x in (40, ancho - 40):
        d.line([(x, 10), (x, 34)], fill=hexa('2a1a14'), width=2)
        d.ellipse([x - 14, 32, x + 14, 72], fill=hexa('d94a3a'))
        d.ellipse([x - 9, 37, x + 9, 67], fill=hexa('ffb04a'))
        for y in (42, 52, 62):
            d.line([(x - 13, y), (x + 13, y)], fill=hexa('a8322f'), width=1)
    titulo = ImageFont.truetype(ruta_fuente, 44)
    sub = ImageFont.truetype(ruta_fuente, 28)
    def centrado(texto, fuente, y, color):
        w = d.textlength(texto, font=fuente)
        d.text(((ancho - w) / 2 + 3, y + 3), texto, font=fuente, fill=(0, 0, 0, 160))
        d.text(((ancho - w) / 2, y), texto, font=fuente, fill=color)
    centrado('EL VIAJE DE CHIHIRO', titulo, 34, hexa('f6e2b0'))
    centrado('Caos en la Casa de Baños', sub, 98, hexa('f0a24a'))
    return fondo

def icono(tamanio):
    base = Image.new('RGBA', (32, 32), (0, 0, 0, 0)); d = ImageDraw.Draw(base)
    d.rounded_rectangle([0, 0, 31, 31], 6, fill=hexa('3a1e2a'))
    d.rounded_rectangle([4, 12, 27, 28], 5, fill=hexa('8b5a34'))
    d.rounded_rectangle([7, 14, 24, 24], 4, fill=hexa('5ab4d0'))
    rect(d, 9, 16, 14, 16, hexa('a8e6f4'))
    for x in (10, 16, 22):
        for k, a in enumerate((210, 170, 130, 90)):
            px(d, x + (k % 2), 10 - k * 2, hexa('ffffff', a)); px(d, x + (k % 2), 9 - k * 2, hexa('ffffff', a))
    return base.resize((tamanio, tamanio), Image.NEAREST)

def guardar(img, ruta):
    completa = os.path.join(DEST, ruta)
    os.makedirs(os.path.dirname(completa), exist_ok=True)
    img.save(completa, optimize=True)

guardar(tileset(), 'mapas/tileset_casa_banos.png')
guardar(jugadores(), 'imagenes/jugadores.png')
guardar(espiritus(), 'imagenes/espiritus.png')
guardar(objetos(), 'imagenes/objetos.png')
guardar(logo(os.path.join(DEST, 'fuentes/fuente.ttf')), 'imagenes/logo.png')
for t in (16, 32, 64, 128):
    guardar(icono(t), 'imagenes/icono%d.png' % t)
