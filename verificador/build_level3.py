"""
Construye el NIVEL 3 (tema ciudad) y comprueba que sea superable.

Es el ultimo nivel, asi que va mas rapido y mas denso que los anteriores:
  Nivel 1: 16 obstaculos, velocidad 8.5
  Nivel 2: 26 obstaculos, velocidad 8.5
  Nivel 3: objetivo ~32 obstaculos, velocidad 9.2

Mas velocidad significa MENOS tiempo de reaccion, no obstaculos mas dificiles.
Por eso los patrones se verifican a la velocidad real del nivel.
"""
import json
from verify import solvable

SPEED = 9.2

# (nombre, ancho, obstaculos, huecos) en coordenadas relativas
PATTERNS = [
    ("pico_simple",   7,  [("SPIKE", 3, 0, 1, 1)], []),
    ("pico_doble",    8,  [("SPIKE", 3, 0, 1, 1), ("SPIKE", 4, 0, 1, 1)], []),
    ("pico_triple",   9,  [("SPIKE", 3, 0, 1, 1), ("SPIKE", 4, 0, 1, 1),
                           ("SPIKE", 5, 0, 1, 1)], []),
    ("pico_cuadruple", 10, [("SPIKE", 3, 0, 1, 1), ("SPIKE", 4, 0, 1, 1),
                            ("SPIKE", 5, 0, 1, 1), ("SPIKE", 6, 0, 1, 1)], []),
    ("hueco_corto",   8,  [], [(3, 2.6)]),
    ("hueco_largo",   9,  [], [(3, 3.4)]),
    ("hueco_ancho",   10, [], [(3, 4.0)]),
    ("bloque",        8,  [("BLOCK", 3, 0, 1, 1)], []),
    ("bloque_alto",   9,  [("BLOCK", 3, 0, 1, 2)], []),
    ("escalon",       11, [("BLOCK", 3, 0, 1, 1), ("BLOCK", 6, 0, 1, 2)], []),
    ("azotea",        12, [("PLATFORM", 3.5, 1.5, 3.5, 0.5),
                           ("SPIKE", 4, 0, 1, 1), ("SPIKE", 5, 0, 1, 1)], []),
    ("pico_bloque",   11, [("SPIKE", 3, 0, 1, 1), ("BLOCK", 6, 0, 1, 1)], []),
    ("doble_hueco",   13, [], [(3, 2.5), (8, 2.5)]),
    ("hueco_pico",    12, [("SPIKE", 7, 0, 1, 1)], [(3, 2.6)]),
    ("pasillo",       12, [("BLOCK", 3, 0, 1, 2), ("SPIKE", 6, 0, 1, 1),
                           ("SPIKE", 7, 0, 1, 1)], []),
    ("torre_doble",   13, [("BLOCK", 3, 0, 1, 1), ("SPIKE", 6, 0, 1, 1),
                           ("BLOCK", 9, 0, 1, 2)], []),
]

print(f"=== patrones a velocidad {SPEED} ===")
ok_patterns = {}
for name, w, obs, gaps in PATTERNS:
    sh = [(t, x + 9, y, ow, oh) for (t, x, y, ow, oh) in obs]
    sg = [(x + 9, gw) for (x, gw) in gaps]
    ok, at = solvable(sh, sg, w + 18, SPEED)
    print(f"{'OK ' if ok else 'X  '} {name:16s} ancho={w:>2} {'' if ok else f'muere en x={at:.1f}'}")
    if ok:
        ok_patterns[name] = (w, obs, gaps)

# Curva de dificultad: empieza suave, aprieta al final
ORDER = [
    "pico_simple", "bloque", "hueco_corto", "pico_doble",
    "pico_bloque", "hueco_largo", "escalon", "pico_triple",
    "bloque_alto", "doble_hueco", "azotea", "hueco_pico",
    "pico_cuadruple", "pasillo", "hueco_ancho", "torre_doble",
    "pico_triple", "doble_hueco", "pico_cuadruple", "hueco_largo",
    "pasillo", "pico_doble",
]

REST = 3.0   # descanso entre patrones; mas corto = mas denso que niveles 1 y 2

obstacles, gaps = [], []
cursor = 11.0
for name in ORDER:
    if name not in ok_patterns:
        continue
    w, obs, gp = ok_patterns[name]
    for (t, x, y, ow, oh) in obs:
        obstacles.append({"type": t, "x": round(x + cursor, 2), "y": y, "w": ow, "h": oh})
    for (x, gw) in gp:
        gaps.append({"x": round(x + cursor, 2), "w": gw})
    cursor += w + REST

length = round(cursor + 9, 1)

# 3 monedas en el apice de tres saltos, repartidas a lo largo
spikes = sorted([o for o in obstacles if o["type"] == "SPIKE"], key=lambda o: o["x"])
for idx in (1, len(spikes) // 2, len(spikes) - 2):
    s = spikes[idx]
    obstacles.append({"type": "COIN", "x": round(s["x"] + 0.5, 2), "y": 2.0, "w": 0.7, "h": 0.7})

obstacles.sort(key=lambda o: o["x"])

sim_obs = [(o["type"], o["x"], o["y"], o["w"], o["h"]) for o in obstacles]
sim_gaps = [(g["x"], g["w"]) for g in gaps]
ok, at = solvable(sim_obs, sim_gaps, length, SPEED)

print(f"\n=== nivel 3 completo ===")
print(f"largo {length} tiles | {len(obstacles)} obstaculos | {len(gaps)} huecos "
      f"| {length / SPEED:.1f} s de juego")
print("RESULTADO:", "SUPERABLE" if ok else f"IMPOSIBLE, muere en x={at:.1f}")

if ok:
    level = {
        "id": "distrito_neon",
        "name": "NIVEL 3: DISTRITO NEON",
        "theme": "ciudad",
        "length": length,
        "scrollSpeed": SPEED,
        "gaps": gaps,
        "obstacles": obstacles,
    }
    with open("level_03.json", "w") as f:
        json.dump(level, f, indent=2, ensure_ascii=False)
    print("escrito level_03.json")
