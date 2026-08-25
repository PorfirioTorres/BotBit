package com.bitlogic.pokebit.game

import android.content.Context
import org.json.JSONObject



class LevelData(
    val id: String,
    val name: String,
    val lengthTiles: Float,
    val scrollSpeed: Float,
    val obstacles: List<Obstacle>,
    val gaps: List<Gap>
)

object LevelLoader {

    fun fromAssets(context: Context, path: String): LevelData {
        val raw = context.assets.open(path).bufferedReader().use { it.readText() }
        return parse(raw)
    }

    fun parse(raw: String): LevelData {
        val root = JSONObject(raw)

        val obstacles = ArrayList<Obstacle>()
        root.optJSONArray("obstacles")?.let { arr ->
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val type = runCatching { ObstacleType.valueOf(o.getString("type")) }.getOrNull()
                    ?: continue
                obstacles.add(
                    Obstacle(
                        type = type,
                        x = o.getDouble("x").toFloat(),
                        y = o.optDouble("y", 0.0).toFloat(),
                        w = o.optDouble("w", 1.0).toFloat(),
                        h = o.optDouble("h", 1.0).toFloat()
                    )
                )
            }
        }

        val gaps = ArrayList<Gap>()
        root.optJSONArray("gaps")?.let { arr ->
            for (i in 0 until arr.length()) {
                val g = arr.getJSONObject(i)
                gaps.add(Gap(g.getDouble("x").toFloat(), g.getDouble("w").toFloat()))
            }
        }

        return LevelData(
            id = root.getString("id"),
            name = root.optString("name", root.getString("id")),
            lengthTiles = root.optDouble("length", 200.0).toFloat(),
            scrollSpeed = root.optDouble("scrollSpeed", GameConfig.BASE_SCROLL_SPEED.toDouble()).toFloat(),
            // El orden por X es obligatorio: World recorre la lista y corta apenas
            // pasa la pantalla. Si no esta ordenada, se saltan colisiones.
            obstacles = obstacles.sortedBy { it.x },
            gaps = gaps.sortedBy { it.x }
        )
    }
}
