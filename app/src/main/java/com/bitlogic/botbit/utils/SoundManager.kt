package com.bitlogic.botbit.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import com.bitlogic.botbit.R

/**
 * Audio del juego. Musica de fondo por tema y efectos cortos.
 *
 * Funciona SIN archivos: si un recurso no existe, la llamada no hace nada y el
 * juego sigue igual. Cuando agreguen los .mp3 y .wav a res/raw, se encienden
 * solos sin tocar este codigo.
 *
 * REGLA IMPORTANTE sobre desde donde se llama:
 * Los efectos NUNCA se disparan desde World.update(). La simulacion corre con
 * paso fijo y puede ejecutar varios pasos dentro de un mismo frame, asi que una
 * moneda podria sonar tres veces seguidas. La UI compara el contador contra el
 * del frame anterior y llama una sola vez por cambio.
 *
 * Archivos esperados en res/raw (nombres exactos, en minusculas y sin guiones):
 *   bgm_pradera.mp3   bgm_cueva.mp3   bgm_ciudad.mp3   bgm_torre.mp3   bgm_arena.mp3
 *   sfx_jump.wav      sfx_coin.wav    sfx_death.wav    sfx_win.wav     sfx_charge.wav
 */
object SoundManager {

    private var soundPool: SoundPool? = null
    private var music: MediaPlayer? = null
    private var currentTrack: String? = null

    private val sfxIds = mutableMapOf<String, Int>()

    /** 0f..1f. Se persisten desde la pantalla de Ajustes. */
    var musicVolume: Float = 0.6f
        set(value) {
            field = value.coerceIn(0f, 1f)
            music?.setVolume(field, field)
        }

    var sfxVolume: Float = 0.8f
        set(value) { field = value.coerceIn(0f, 1f) }

    fun init(context: Context) {
        if (soundPool != null) return

        soundPool = SoundPool.Builder()
            .setMaxStreams(6)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()

        listOf("sfx_jump", "sfx_coin", "sfx_death", "sfx_win", "sfx_charge")
            .forEach { name -> loadSfx(context, name) }
    }

    private fun loadSfx(context: Context, name: String) {
        val resId = rawId(context, name)
        if (resId != 0) {
            runCatching { soundPool?.load(context, resId, 1) }
                .getOrNull()?.let { sfxIds[name] = it }
        }
    }

    /** Busca el recurso por nombre. Devuelve 0 si el archivo todavia no existe. */
    private fun rawId(context: Context, name: String): Int =
        runCatching {
            context.resources.getIdentifier(name, "raw", context.packageName)
        }.getOrDefault(0)

    // ---------------- Musica ----------------

    /**
     * Arranca la pista del tema indicado. Si ya esta sonando esa misma, no
     * reinicia: evita el corte al pasar de la pantalla de nivel al juego.
     *
     * @param theme "pradera", "cueva", "ciudad", "torre" o "arena"
     */
    fun playMusicForTheme(context: Context, theme: String) {
        val track = "bgm_${theme.lowercase()}"
        if (track == currentTrack && music?.isPlaying == true) return

        stopMusic()
        val resId = rawId(context, track)
        if (resId == 0) return                    // todavia no hay archivo

        runCatching {
            music = MediaPlayer.create(context, resId)?.apply {
                isLooping = true
                setVolume(musicVolume, musicVolume)
                start()
            }
            currentTrack = track
        }
    }

    fun pauseMusic() {
        runCatching { if (music?.isPlaying == true) music?.pause() }
    }

    fun resumeMusic() {
        runCatching { if (music?.isPlaying == false) music?.start() }
    }

    fun stopMusic() {
        runCatching {
            music?.stop()
            music?.release()
        }
        music = null
        currentTrack = null
    }

    // ---------------- Efectos ----------------

    private fun play(name: String, rate: Float = 1f) {
        val id = sfxIds[name] ?: return
        soundPool?.play(id, sfxVolume, sfxVolume, 1, 0, rate)
    }

    fun playJumpSound() = play("sfx_jump")
    fun playCoinSound() = play("sfx_coin")
    fun playDeathSound() = play("sfx_death")
    fun playWinSound() = play("sfx_win")

    /** La carga del salto en la Torre: sube de tono conforme se carga. */
    fun playChargeSound(ratio: Float) = play("sfx_charge", 0.8f + ratio * 0.6f)

    fun release() {
        stopMusic()
        runCatching { soundPool?.release() }
        soundPool = null
        sfxIds.clear()
    }
}
