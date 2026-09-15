"""
Verificador de niveles de BotBit.

Replica EXACTAMENTE la fisica de World.update() para comprobar, con busqueda
en anchura sobre todas las secuencias de saltos posibles, si un nivel se puede
terminar.

Constantes leidas de GameConfig.kt. Si cambias GRAVITY, JUMP_VELOCITY o
PLAYER_SIZE en Kotlin, cambialas aqui tambien o la verificacion miente.
"""

GRAVITY = 70.0
JUMP_VELOCITY = 18.5
PLAYER_X = 1.5          # ojo: es 1.5, no 2.5
PLAYER_SIZE = 1.0       # ojo: es 1.0, no 0.9
LETHAL_SHRINK = 0.55
SPIKE_SHRINK = 0.5
LANDING_TOLERANCE = 0.05
DEATH_Y = -4.0
STEP = 1.0 / 120.0


def shrink(l, b, w, h, f):
    nw, nh = w * f, h * f
    return (l + (w - nw) / 2, b + (h - nh) / 2, nw, nh)


def overlaps(a, b):
    return (a[0] < b[0] + b[2] and a[0] + a[2] > b[0] and
            a[1] < b[1] + b[3] and a[1] + a[3] > b[1])


def step_sim(scroll_x, y, vy, on_ground, jump, obstacles, gaps):
    prev_y = y
    if jump and on_ground:
        vy = JUMP_VELOCITY
        on_ground = False
    vy -= GRAVITY * STEP
    y += vy * STEP

    grounded = False
    cx = scroll_x + PLAYER_X + PLAYER_SIZE / 2
    over_gap = any(g[0] < cx < g[0] + g[1] for g in gaps)

    if not over_gap and y <= 0.0 and vy <= 0.0:
        y, vy, grounded = 0.0, 0.0, True

    body = (scroll_x + PLAYER_X, y, PLAYER_SIZE, PLAYER_SIZE)
    for (t, ox, oy, ow, oh) in obstacles:
        if ox > body[0] + body[2] + 2:
            break
        if ox + ow < body[0] - 2:
            continue
        if t == "COIN":
            continue
        if t == "SPIKE":
            if overlaps(shrink(*body, LETHAL_SHRINK), shrink(ox, oy, ow, oh, SPIKE_SHRINK)):
                return y, vy, grounded, True
        elif t in ("BLOCK", "PLATFORM"):
            if overlaps(body, (ox, oy, ow, oh)):
                if vy <= 0 and prev_y >= oy + oh - LANDING_TOLERANCE:
                    y = oy + oh
                    vy = 0.0
                    grounded = True
                    body = (body[0], y, PLAYER_SIZE, PLAYER_SIZE)
                else:
                    return y, vy, grounded, True

    if y < DEATH_Y:
        return y, vy, grounded, True
    return y, vy, grounded, False


def solvable(obstacles, gaps, length, speed):
    """BFS sobre (paso, y, vy, en_suelo). Solo se ramifica cuando se puede saltar."""
    obstacles = sorted(obstacles, key=lambda o: o[1])
    total_steps = int(length / (speed * STEP)) + 1
    frontier = {(0.0, 0.0, True)}
    seen = set()
    for i in range(total_steps):
        scroll_x = speed * STEP * i
        nxt = set()
        for (y, vy, og) in frontier:
            for jump in ((False, True) if og else (False,)):
                ny, nvy, nog, dead = step_sim(scroll_x, y, vy, og, jump, obstacles, gaps)
                if dead:
                    continue
                key = (i + 1, round(ny, 2), round(nvy, 1), nog)
                if key in seen:
                    continue
                seen.add(key)
                nxt.add((ny, nvy, nog))
        frontier = nxt
        if not frontier:
            return False, scroll_x
    return True, length


def geometry(speed):
    air = 2 * JUMP_VELOCITY / GRAVITY
    return dict(airtime=air, apex=JUMP_VELOCITY ** 2 / (2 * GRAVITY), reach=air * speed)


if __name__ == "__main__":
    for s in (8.5, 9.2, 10.0):
        g = geometry(s)
        print(f"velocidad {s}: aire {g['airtime']:.2f}s  altura {g['apex']:.2f}  alcance {g['reach']:.2f} tiles")
