package com.bitlogic.botbit.game

import android.content.Context
import org.json.JSONObject

data class LevelData(
    val id: String,
    val name: String,
    val lengthTiles: Int,
    val scrollSpeed: Float,
    val obstacles: List<Obstacle>,
    val gaps: List<Gap>,
    /** Tema visual. Si el JSON no lo trae, se usa el de por defecto. */
    val theme: String = "pradera"
)

object LevelLoader {
    fun fromAssets(context: Context, path: String): LevelData {
        val json = context.assets.open(path).bufferedReader().use { it.readText() }
        val root = JSONObject(json)

        val obs = ArrayList<Obstacle>()
        val jsonObs = root.getJSONArray("obstacles")
        for (i in 0 until jsonObs.length()) {
            val o = jsonObs.getJSONObject(i)
            obs.add(
                Obstacle(
                    type = ObstacleType.valueOf(o.getString("type")),
                    x = o.getDouble("x").toFloat(),
                    y = o.getDouble("y").toFloat(),
                    w = o.getDouble("w").toFloat(),
                    h = o.getDouble("h").toFloat()
                )
            )
        }

        val gaps = ArrayList<Gap>()
        val jsonGaps = root.optJSONArray("gaps")
        if (jsonGaps != null) {
            for (i in 0 until jsonGaps.length()) {
                val g = jsonGaps.getJSONObject(i)
                gaps.add(Gap(g.getDouble("x").toFloat(), g.getDouble("w").toFloat()))
            }
        }

        return LevelData(
            theme = root.optString("theme", "pradera"),
            id = root.getString("id"),
            name = root.getString("name"),
            lengthTiles = root.getInt("length"),
            scrollSpeed = root.optDouble("scrollSpeed", GameConfig.BASE_SCROLL_SPEED.toDouble())
                .toFloat(),
            obstacles = obs,
            gaps = gaps
        )
    }
}
