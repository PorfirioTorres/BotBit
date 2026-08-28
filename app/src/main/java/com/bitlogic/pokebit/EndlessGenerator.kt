package com.bitlogic.pokebit.game

import kotlin.random.Random

/**
 * Modo infinito. No genera obstaculos al azar tile por tile (eso produce
 * niveles imposibles); encadena BLOQUES PREFABRICADOS que ya se verificaron
 * como superables, separados por un tramo de descanso.
 *
 * Todos los bloques de aqui pasaron la verificacion hasta velocidad 12.5,
 * por eso ENDLESS_MAX_SPEED esta en 12.
 */
class EndlessGenerator(private val rng: Random) {

    private var cursor = 0f

    init { reset() }

    fun reset() {
        // Pista libre al inicio para que el jugador alcance a reaccionar.
        cursor = GameConfig.TILES_VISIBLE_X + 6f
    }

    /** Genera bloques hasta cubrir worldX. Mantiene la lista ordenada por X. */
    fun generateUpTo(worldX: Float, out: MutableList<Obstacle>, gaps: MutableList<Gap>) {
        while (cursor < worldX) {
            val chunk = CHUNKS[rng.nextInt(CHUNKS.size)]

            val batch = ArrayList<Obstacle>(chunk.items.size + 1)
            for (o in chunk.items) {
                batch.add(Obstacle(o.type, o.x + cursor, o.y, o.w, o.h))
            }
            // 1 de cada 3 bloques trae un coleccionable en el punto alto del salto.
            if (rng.nextInt(3) == 0) {
                batch.add(
                    Obstacle(ObstacleType.COIN, cursor + chunk.width / 2f, 2f, 0.7f, 0.7f)
                )
            }
            batch.sortBy { it.x }
            out.addAll(batch)

            for (g in chunk.gaps) gaps.add(Gap(g.x + cursor, g.w))

            cursor += chunk.width + GameConfig.ENDLESS_REST
        }
    }

    private class Chunk(
        val width: Float,
        val items: List<Obstacle>,
        val gaps: List<Gap> = emptyList()
    )

    private companion object {

        fun spike(x: Float) = Obstacle(ObstacleType.SPIKE, x, 0f, 1f, 1f)
        fun block(x: Float, h: Float = 1f) = Obstacle(ObstacleType.BLOCK, x, 0f, 1f, h)

        val CHUNKS = listOf(
            // pico simple
            Chunk(8f, listOf(spike(3f))),
            // pico doble
            Chunk(9f, listOf(spike(3f), spike(4f))),
            // pico triple
            Chunk(10f, listOf(spike(3f), spike(4f), spike(5f))),
            // hueco corto
            Chunk(9f, emptyList(), listOf(Gap(3f, 2.5f))),
            // hueco largo
            Chunk(10f, emptyList(), listOf(Gap(3f, 3.2f))),
            // cubo suelto
            Chunk(9f, listOf(block(3f))),
            // pico y cubo
            Chunk(12f, listOf(spike(3f), block(6f)))
        )
    }
}
