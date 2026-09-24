"""
Repara la torre: mientras haya plataformas inalcanzables, recoloca la mas baja
hasta que el grafo completo quede conectado.

Hace falta porque el grafo es dinamico: una plataforma agregada arriba puede
bloquear por su cara inferior un salto que antes funcionaba. Verificar solo
contra la anterior no basta, hay que verificar el conjunto.
"""
import json, random
import verify_tower as V

random.seed(3)
WALL_L, WALL_R, H = 1.0, 11.0, 0.5


def plats_from(obs):
    return [(o["x"], o["y"], o["w"], o["h"]) for o in obs
            if o["type"] in ("PLATFORM", "BLOCK") and o["h"] < 8]


def graph_reachable(plats):
    edges = V.build_graph(plats)
    return V.reachable(edges)


def repair(path, max_rounds=400):
    d = json.load(open(path))
    obs = d["obstacles"]
    idx_of = [i for i, o in enumerate(obs) if o["type"] == "PLATFORM"]

    for rnd in range(max_rounds):
        plats = plats_from(obs)
        seen = graph_reachable(plats)
        bad = [i for i in range(len(plats)) if i not in seen]
        if not bad:
            print(f"  reparada en {rnd} rondas")
            return d, True

        k = min(bad)                                  # la mas baja rota
        oi = None
        for i, o in enumerate(obs):
            if o["type"] == "PLATFORM" and (o["x"], o["y"], o["w"], o["h"]) == plats[k]:
                oi = i
                break
        if oi is None:
            return d, False

        base_top = plats[k - 1][1] + plats[k - 1][3] if k > 0 else 0.0
        fixed = False
        for _ in range(120):
            w = round(random.uniform(2.8, 4.2), 1)
            rise = random.uniform(1.8, 3.0)
            x = round(random.uniform(WALL_L, WALL_R - w), 2)
            obs[oi].update({"x": x, "y": round(base_top + rise - H, 2), "w": w, "h": H})
            newp = plats_from(obs)
            if k in graph_reachable(newp):
                fixed = True
                break
        if not fixed:
            print(f"  no se pudo reparar la plataforma {k} (y={plats[k][1]:.1f})")
            return d, False
    return d, False


if __name__ == "__main__":
    d, ok = repair("tower_map.json")
    if ok:
        # recolocar checkpoints sobre plataformas finales, uno por sala
        RH = d.get("roomHeight", 15.0)
        plats = sorted([o for o in d["obstacles"] if o["type"] == "PLATFORM"],
                       key=lambda o: o["y"])
        d["obstacles"] = [o for o in d["obstacles"] if o["type"] != "CHECKPOINT"]
        nxt = 1
        for p in plats:
            top = p["y"] + p["h"]
            room = int(top // RH)
            if room >= nxt and room > 0:
                d["obstacles"].append({"type": "CHECKPOINT",
                                       "x": round(p["x"] + p["w"] / 2 - 0.3, 2),
                                       "y": round(top + 0.05, 2), "w": 0.6, "h": 0.6})
                nxt = room + 1
        d["towerHeight"] = round(max(p["y"] + p["h"] for p in plats), 2)
        d["obstacles"].sort(key=lambda o: o["y"])
        json.dump(d, open("tower_map.json", "w"), indent=2, ensure_ascii=False)
        print(f"  altura final: {d['towerHeight']} | checkpoints: "
              f"{sum(1 for o in d['obstacles'] if o['type']=='CHECKPOINT')}")
    print()
    V.verify("tower_map.json")
