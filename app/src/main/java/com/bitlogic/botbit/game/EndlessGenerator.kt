package com.bitlogic.botbit.game

import kotlin.random.Random

class EndlessGenerator(private val random: Random) {

    private var lastX = 10f

    fun reset() {
        lastX = 10f
    }

    fun generateUpTo(targetX: Float, obstacles: ArrayList<Obstacle>, gaps: ArrayList<Gap>) {
        while (lastX < targetX) {
            val type = random.nextInt(5)
            when (type) {
                0 -> {
                    val count = random.nextInt(1, 3)
                    for (i in 0 until count) {
                        obstacles.add(Obstacle(ObstacleType.SPIKE, lastX + i.toFloat(), 0f, 1f, 1f))
                    }
                    lastX += count + GameConfig.ENDLESS_REST
                }

                1 -> {
                    val h = random.nextInt(1, 3).toFloat()
                    obstacles.add(Obstacle(ObstacleType.BLOCK, lastX, 0f, 1f, h))
                    lastX += 2f + GameConfig.ENDLESS_REST
                }

                2 -> {
                    val width = random.nextInt(2, 4).toFloat()
                    gaps.add(Gap(lastX, width))
                    // Moneda sobre el hueco
                    obstacles.add(Obstacle(ObstacleType.COIN, lastX + width / 2f - 0.5f, 3f, 1f, 1f))
                    lastX += width + GameConfig.ENDLESS_REST
                }

                3 -> {
                    obstacles.add(Obstacle(ObstacleType.PLATFORM, lastX, 2f, 3f, 0.5f))
                    lastX += 4f + GameConfig.ENDLESS_REST
                }

                4 -> {
                    obstacles.add(Obstacle(ObstacleType.SPIKE, lastX, 0f, 1f, 1f))
                    obstacles.add(Obstacle(ObstacleType.SPIKE, lastX + 1.5f, 0f, 1f, 1f))
                    obstacles.add(Obstacle(ObstacleType.COIN, lastX + 0.75f, 3f, 0.7f, 0.7f))
                    lastX += 3f + GameConfig.ENDLESS_REST
                }
            }
        }
    }
}
