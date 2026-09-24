"""
Genera LA GRAN TORRE aceptando SOLO plataformas que el verificador confirma
alcanzables desde la anterior.

Descubrimiento clave: las plataformas son solidas por abajo (TowerWorld rebota
al golpear la cara inferior). Por eso NO se puede poner una plataforma
directamente encima de otra: el jugador choca de cabeza y cae.

La torre correcta alterna lados, como un Jump King de verdad: se sube en
zigzag, no en linea recta.
"""
import json, random
import verify_tower as V

random.seed(11)

ROOM_HEIGHT, TOTAL_ROOMS = 15.0, 20
TOP_Y = ROOM_HEIGHT * TOTAL_ROOMS
WALL_L, WALL_R = 1.0, 11.0
H = 0.5

def reachable_from(src_top, src_x, src_w, cand, solids):
    """Devuelve True si existe algun salto de la plataforma origen al candidato."""
    near = [(i, s) for i, s in enumerate(solids) if src_top - 8 <= s[1] + s[3] <= src_top + 8]
    span = max(src_w - V.PLAYER_SIZE, 0.0)
    starts = [src_x + span * k / 4 for k in range(5)]
    target = len(solids) - 1          # el candidato es el ultimo agregado
    hits = 0
    for sx in starts:
        for p in V.POWERS:
            for dx in V.DIRS:
                land, _ = V.simulate(sx, src_top, p, dx, solids, near)
                if land == target:
                    hits += 1
                    if hits >= 3:     # al menos 3 formas de lograrlo = margen humano
                        return True
    return False

obstacles = [
    {"type": "BLOCK", "x": 0,  "y": -1, "w": 12, "h": 1},
    {"type": "BLOCK", "x": 0,  "y": 0,  "w": 1,  "h": TOP_Y + 20},
    {"type": "BLOCK", "x": 11, "y": 0,  "w": 1,  "h": TOP_Y + 20},
]
solids = [(0, -1, 12, 1)]             # el piso cuenta como solido

cur_top, cur_x, cur_w = 0.0, 5.0, 6.0  # el piso: se puede salir de casi cualquier x
side = 1                               # 1 = derecha, -1 = izquierda
plats = 0
rejected = 0
next_cp_room = 1

while cur_top < TOP_Y:
    placed = False
    for _ in range(60):                # intentos por plataforma
        rise = random.uniform(2.2, 3.2)
        w = round(random.uniform(2.6, 4.0), 1)
        # Zigzag: el lado alterna, con jitter para que no quede mecanico
        if side > 0:
            x = random.uniform(max(WALL_L, cur_x + cur_w - 0.5), WALL_R - w)
        else:
            x = random.uniform(WALL_L, max(WALL_L, cur_x - w + 0.5))
        x = round(min(max(x, WALL_L), WALL_R - w), 2)
        top = cur_top + rise
        cand = (x, round(top - H, 2), w, H)
        solids.append(cand)
        if reachable_from(cur_top, cur_x, cur_w, cand, solids):
            obstacles.append({"type": "PLATFORM", "x": cand[0], "y": cand[1],
                              "w": cand[2], "h": cand[3]})
            plats += 1
            room = int(top // ROOM_HEIGHT)
            if room >= next_cp_room:
                obstacles.append({"type": "CHECKPOINT",
                                  "x": round(x + w / 2 - 0.3, 2), "y": round(top + 0.05, 2),
                                  "w": 0.6, "h": 0.6})
                next_cp_room = room + 1
            cur_top, cur_x, cur_w = top, x, w
            side = -side
            placed = True
            break
        solids.pop()
        rejected += 1
    if not placed:
        side = -side
        if not any(True for _ in range(1)):
            break
        # si un lado se atora, intenta el otro una vez mas
        continue

# Meta
for _ in range(80):
    rise = random.uniform(2.2, 3.0)
    w, x = 5.0, 3.5
    top = cur_top + rise
    cand = (x, round(top - H, 2), w, H)
    solids.append(cand)
    if reachable_from(cur_top, cur_x, cur_w, cand, solids):
        obstacles.append({"type": "PLATFORM", "x": x, "y": cand[1], "w": w, "h": H})
        obstacles.append({"type": "CHECKPOINT", "x": round(x + w/2 - 0.3, 2),
                          "y": round(top + 0.05, 2), "w": 0.6, "h": 0.6})
        cur_top = top
        plats += 1
        break
    solids.pop()

tower = {
    "id": "la_gran_torre", "name": "LA GRAN TORRE", "theme": "cueva",
    "length": 0, "scrollSpeed": 0,
    "towerHeight": round(cur_top, 2), "roomHeight": ROOM_HEIGHT,
    "gaps": [], "obstacles": obstacles,
}
json.dump(tower, open("tower_map.json", "w"), indent=2, ensure_ascii=False)
print(f"plataformas aceptadas: {plats} | intentos descartados: {rejected}")
print(f"altura final: {cur_top:.1f} tiles")
print(f"checkpoints: {sum(1 for o in obstacles if o['type']=='CHECKPOINT')}\n")
ok = V.verify("tower_map.json")
print("\nRESULTADO:", "TORRE SUPERABLE" if ok else "TORRE CON PROBLEMAS")
