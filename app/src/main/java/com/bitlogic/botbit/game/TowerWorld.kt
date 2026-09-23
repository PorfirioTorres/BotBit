package com.bitlogic.botbit.game

/**
 * MODO TORRE (estilo Jump King). ESQUELETO — todavia no se usa.
 *
 * Esta clase existe para que quede claro que agregar un modo NO significa
 * tocar World. Cumple el mismo contrato (GameWorld) y GameScreen la puede
 * correr sin cambios.
 *
 * QUE SE REAPROVECHA DEL RUNNER
 *  - Gravedad e integracion de posicion (identicas)
 *  - AABB y la resolucion de colisiones contra bloques
 *  - Los niveles en JSON y el cargador
 *  - LevelTheme, RenderScratch y el bucle de paso fijo
 *
 * QUE CAMBIA
 *  1. El jugador se mueve libre en X. En el runner, PLAYER_X es constante.
 *  2. El salto se carga: Press empieza a cargar, Release lo ejecuta.
 *     La fuerza depende de cuanto se mantuvo, topada en CHARGE_MAX.
 *  3. La camara sube por salas completas en vez de hacer scroll continuo.
 *     Cuando el jugador cruza el borde superior de la sala, la camara salta
 *     a la siguiente. Eso es lo que hace que caerse duela.
 *  4. NO HAY MUERTE. status se queda en RUNNING siempre. Caerse solo te
 *     regresa a una sala anterior, y ese castigo es el juego entero.
 *  5. Al caer y golpear una pared estando en el aire, se rebota. Sin rebote
 *     no se siente a Jump King.
 *
 * OJO CON EL VERIFICADOR
 * El verificador de Python actual no sirve aqui: comprueba que exista una
 * secuencia de saltos que avance en X a velocidad fija. Para la torre hay que
 * escribir otro: un grafo de alcanzabilidad entre plataformas, donde dos
 * plataformas se conectan si alguna combinacion de carga y direccion permite
 * llegar de una a otra. Si alguna sala queda sin camino de entrada a salida,
 * la torre es imposible.
 */
class TowerWorld(
    private val level: LevelData?,
    private val characterId: String = "classic"
) : GameWorld {

    override val kind: GameKind get() = GameKind.TOWER

    override var status: GameStatus = GameStatus.RUNNING
        private set

    override var elapsed: Float = 0f
        private set

    override val score: Int get() = 0          // TODO: altura maxima alcanzada
    override val progress: Float get() = 0f    // TODO: sala actual / salas totales
    override val title: String get() = level?.name ?: "LA TORRE"
    override val coins: Int get() = 0
    override val attempts: Int get() = 1

    // --- Estado del jugador ---
    var x = 0f; private set
    var y = 0f; private set
    private var vx = 0f
    private var vy = 0f
    private var onGround = true

    // --- Carga del salto ---
    private var charging = false
    private var charge = 0f

    /** Sala visible. La camara no interpola: salta de sala en sala. */
    var room = 0; private set

    override fun update(dt: Float) {
        elapsed += dt

        if (charging) charge = (charge + dt).coerceAtMost(CHARGE_MAX)

        // TODO: gravedad + integracion (copiar de World.update, paso 2)
        // TODO: colisiones contra bloques (copiar de World.update, paso 3)
        // TODO: rebote al chocar de lado en el aire -> vx = -vx * BOUNCE
        // TODO: si y cruza el techo de la sala -> room++
        //       si y cae por debajo del piso    -> room--
    }

    override fun onInput(event: InputEvent) {
        when (event) {
            is InputEvent.Press -> if (onGround) { charging = true; charge = 0f }
            is InputEvent.Release -> {
                if (charging && onGround) {
                    val power = (charge / CHARGE_MAX).coerceIn(0.15f, 1f)
                    vy = JUMP_MIN + (JUMP_MAX - JUMP_MIN) * power
                    vx = event.dirX * SIDE_SPEED * power
                    onGround = false
                }
                charging = false
                charge = 0f
            }
            is InputEvent.Move -> if (onGround && !charging) vx = event.x * WALK_SPEED
            is InputEvent.Tap -> { /* la torre no usa el toque simple */ }
        }
    }

    override fun retry() {
        x = 0f; y = 0f; vx = 0f; vy = 0f
        onGround = true; charging = false; charge = 0f
        room = 0
        status = GameStatus.RUNNING
        elapsed = 0f
    }

    /** Fraccion de carga 0..1, para dibujar la barra sobre el robot. */
    val chargeRatio: Float get() = if (charging) charge / CHARGE_MAX else 0f

    private companion object {
        const val CHARGE_MAX = 0.55f   // segundos para carga completa
        const val JUMP_MIN = 9f
        const val JUMP_MAX = 22f
        const val SIDE_SPEED = 7f
        const val WALK_SPEED = 4.5f
        const val BOUNCE = 0.55f
    }
}
