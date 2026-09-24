"""
Verificador de LA TORRE.

El verificador del Runner no sirve aqui: comprueba que exista una secuencia de
saltos que avance en X a velocidad constante. En la Torre no hay avance
automatico, hay que llegar de una plataforma a otra.

Este construye un GRAFO DE ALCANZABILIDAD: cada plataforma es un nodo, y hay
una arista A -> B si existe alguna combinacion de carga y direccion que permita
saltar de A y aterrizar en B. Si el piso no conecta con la cima, la torre es
imposible.

Constantes copiadas de TowerWorld.kt y GameConfig.kt.
"""
import json
from collections import deque

GRAVITY = 70.0
JUMP_MIN = 12.0
JUMP_MAX = 24.0
SIDE_SPEED = 9.0
PLAYER_SIZE = 1.0
LANDING_TOLERANCE = 0.05
BOUNCE = 0.5
WALL_LEFT = 1.0      # cara interior del bloque x=0..1
WALL_RIGHT = 11.0    # cara interior del bloque x=11..12
STEP = 1.0 / 120.0

# Muestreo: cuantas cargas y cuantas posiciones de salida se prueban
POWERS = [0.2 + i * (0.8 / 19) for i in range(20)]   # 0.20 .. 1.00
DIRS = (-1.0, 0.0, 1.0)


def apex(power):
    v = JUMP_MIN + (JUMP_MAX - JUMP_MIN) * power
    return v * v / (2 * GRAVITY)


def load(path):
    d = json.load(open(path))
    plats, walls, checkpoints = [], [], []
    for o in d["obstacles"]:
        t = o["type"]
        rect = (o["x"], o["y"], o["w"], o["h"])
        if t == "CHECKPOINT":
            checkpoints.append(rect)
        elif t in ("PLATFORM", "BLOCK"):
            # Un bloque muy alto es pared, no plataforma
            if o["h"] >= 8:
                walls.append(rect)
            else:
                plats.append(rect)
    plats.sort(key=lambda p: (p[1], p[0]))
    return d, plats, walls, checkpoints


def simulate(px, py, power, dirx, solids, near=None):
    """Lanza desde (px, py) y devuelve el indice del solido donde aterriza, o None."""
    if near is None:
        near = [(i, s) for i, s in enumerate(solids)
                if py - 8 <= s[1] + s[3] <= py + 6]
    vy = JUMP_MIN + (JUMP_MAX - JUMP_MIN) * power
    vx = dirx * SIDE_SPEED * power
    x, y = px, py
    for _ in range(1400):                     # ~11 s de vuelo, de sobra
        prev_y = y
        vy -= GRAVITY * STEP
        y += vy * STEP
        x += vx * STEP

        # Paredes laterales
        if x < WALL_LEFT:
            x = WALL_LEFT
            vx = -vx * BOUNCE
        elif x + PLAYER_SIZE > WALL_RIGHT:
            x = WALL_RIGHT - PLAYER_SIZE
            vx = -vx * BOUNCE

        for i, (ox, oy, ow, oh) in near:
            top = oy + oh
            if not (x < ox + ow and x + PLAYER_SIZE > ox):
                continue
            if not (y < top and y + PLAYER_SIZE > oy):
                continue
            if vy <= 0 and prev_y >= top - LANDING_TOLERANCE:
                return i, x
            # Golpe lateral o de cabeza: rebota, sigue volando
            if vy > 0:
                vy = -vy * BOUNCE
            else:
                vx = -vx * BOUNCE
            break

        if y <= 0 and vy <= 0:
            return -1, x                      # piso
        if y < -5:
            return None, x
    return None, x


def build_graph(plats, verbose=False):
    """nodo -1 = piso; nodo i = plats[i]."""
    nodes = [(-1, WALL_LEFT, WALL_RIGHT, 0.0)]
    for i, (ox, oy, ow, oh) in enumerate(plats):
        nodes.append((i, ox, ox + ow, oy + oh))

    edges = {n[0]: set() for n in nodes}
    for nid, left, right, top in nodes:
        # posiciones de salida a lo largo de la plataforma
        near = [(i, s) for i, s in enumerate(plats)
                if top - 8 <= s[1] + s[3] <= top + 6]
        span = max(right - PLAYER_SIZE - left, 0.0)
        starts = [left + span * k / 4 for k in range(5)] if span > 0 else [left]
        for sx in starts:
            for p in POWERS:
                for d in DIRS:
                    land, _ = simulate(sx, top, p, d, plats, near)
                    if land is None:
                        continue
                    if land != nid:
                        edges[nid].add(land)
    return edges


def reachable(edges, start=-1):
    seen, q = {start}, deque([start])
    while q:
        n = q.popleft()
        for m in edges.get(n, ()):
            if m not in seen:
                seen.add(m)
                q.append(m)
    return seen


def verify(path, verbose=True):
    d, plats, walls, checkpoints = load(path)
    edges = build_graph(plats)
    seen = reachable(edges)

    top_index = max(range(len(plats)), key=lambda i: plats[i][1]) if plats else None
    altura = max((p[1] + p[3] for p in plats), default=0)

    if verbose:
        print(f"torre: {path.split('/')[-1]}")
        print(f"  plataformas: {len(plats)} | paredes: {len(walls)} | checkpoints: {len(checkpoints)}")
        print(f"  altura maxima: {altura:.1f} tiles")
        print(f"  salto maximo: {apex(1.0):.2f} tiles | salto minimo: {apex(0.2):.2f} tiles")

    huerfanas = [i for i in range(len(plats)) if i not in seen]
    cima_ok = top_index in seen if top_index is not None else False

    if verbose:
        if huerfanas:
            print(f"  INALCANZABLES: {len(huerfanas)} plataformas")
            for i in huerfanas[:8]:
                ox, oy, ow, oh = plats[i]
                print(f"     y={oy:.1f} x={ox:.1f}..{ox+ow:.1f}")
            if len(huerfanas) > 8:
                print(f"     ... y {len(huerfanas)-8} mas")
        else:
            print("  todas las plataformas son alcanzables")
        print(f"  CIMA ALCANZABLE: {'SI' if cima_ok else 'NO'}")

    # checkpoints alcanzables: deben caer sobre una plataforma alcanzable
    cp_ok = 0
    for (cx, cy, cw, ch) in checkpoints:
        for i, (ox, oy, ow, oh) in enumerate(plats):
            if ox - 0.5 <= cx <= ox + ow + 0.5 and abs(cy - (oy + oh)) < 1.5:
                if i in seen:
                    cp_ok += 1
                break
    if verbose and checkpoints:
        print(f"  checkpoints sobre plataforma alcanzable: {cp_ok}/{len(checkpoints)}")

    return cima_ok and not huerfanas


if __name__ == "__main__":
    import sys
    p = sys.argv[1] if len(sys.argv) > 1 else "tower_map.json"
    ok = verify(p)
    print("\nRESULTADO:", "TORRE SUPERABLE" if ok else "TORRE CON PROBLEMAS")
