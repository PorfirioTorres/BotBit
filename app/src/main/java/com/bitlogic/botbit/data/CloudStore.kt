package com.bitlogic.botbit.data

import android.util.Log
import com.bitlogic.botbit.utils.AnalyticsHelper
import com.bitlogic.botbit.game.GameKind
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Sincronizacion del progreso con Firestore.
 *
 * PATRON DE REFLEJO, NO DE MIGRACION
 * ----------------------------------
 * SharedPreferences sigue siendo la VERDAD del juego. Firestore recibe una
 * copia en segundo plano. El juego nunca espera a la red.
 *
 * Por que importa: Firestore es asincrono y puede fallar. Si el guardado de la
 * partida dependiera de la nube, una mala conexion trabaria el juego o perderia
 * progreso. Asi, si Firestore falla, BotBit funciona exactamente igual.
 *
 * Estructura en la nube:
 *
 *   usuarios/{uid}
 *       monedas_totales: Long
 *       robots_desbloqueados: [String]
 *       personaje_seleccionado: String
 *       niveles_completados: [String]
 *       modo_joystick: String
 *       actualizado: Long          (epoch ms, para resolver conflictos)
 *
 *   usuarios/{uid}/records/{modo}
 *       mejor_puntaje: Long
 *       actualizado: Long
 *
 * Los records van en subcoleccion para poder consultarlos sin bajar el
 * documento completo, y deja la puerta abierta a una tabla global despues.
 */
class CloudStore(private val local: ProgressStore) {

    private val auth get() = FirebaseAuth.getInstance()
    private val db get() = FirebaseFirestore.getInstance()

    // Scope propio: si la Activity muere, la subida en curso no se cancela a
    // medias dejando la nube inconsistente.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val uid: String? get() = auth.currentUser?.uid

    val isSignedIn: Boolean get() = uid != null

    // ---------------------------------------------------------------
    // SUBIR
    // ---------------------------------------------------------------

    /**
     * Copia el progreso local a la nube. Se llama despues de terminar una
     * partida o de comprar un personaje. No devuelve nada a proposito: quien
     * la llama no debe esperar el resultado.
     */
    fun push() {
        val id = uid ?: return                 // sin sesion, no hay nada que hacer
        scope.launch {
            runCatching {
                val datos = mapOf(
                    "monedas_totales" to local.totalCoins.toLong(),
                    "robots_desbloqueados" to local.unlockedCharacterIds(),
                    "personaje_seleccionado" to local.getSelectedCharacter(),
                    "niveles_completados" to local.completedLevelIds(),
                    "modo_joystick" to local.joystickMode,
                    "actualizado" to System.currentTimeMillis()
                )
                db.collection(USUARIOS).document(id)
                    .set(datos, SetOptions.merge())
                    .await()
                Log.d(TAG, "Progreso subido a usuarios/$id")

                // Records por modo, cada uno en su documento
                for (kind in GameKind.entries) {
                    val puntaje = local.bestScoreFor(kind)
                    if (puntaje <= 0) continue
                    db.collection(USUARIOS).document(id)
                        .collection(RECORDS).document(kind.storageKey)
                        .set(
                            mapOf(
                                "mejor_puntaje" to puntaje.toLong(),
                                "actualizado" to System.currentTimeMillis()
                            ),
                            SetOptions.merge()
                        ).await()
                }
                lastError = null
            }.onFailure { reportError("No se pudo subir el progreso", it) }
        }
    }

    // ---------------------------------------------------------------
    // BAJAR
    // ---------------------------------------------------------------

    /**
     * Trae el progreso de la nube y lo fusiona con el local.
     *
     * REGLA DE FUSION: gana el valor mas alto.
     * No se sobrescribe lo local sin mas. Si alguien jugo sin conexion y junto
     * 500 monedas, bajar un documento viejo de 200 le borraria el avance.
     *
     * @param onDone se llama en el hilo de IO al terminar, con true si hubo cambios
     */
    fun pullAndMerge(onDone: (Boolean) -> Unit = {}) {
        val id = uid ?: return onDone(false)
        scope.launch {
            val cambio = runCatching {
                val doc = db.collection(USUARIOS).document(id).get().await()
                if (!doc.exists()) {
                    push()                      // primera sesion: sube lo que haya
                    return@runCatching false
                }

                var hubo = false

                (doc.getLong("monedas_totales"))?.let { nube ->
                    val falta = nube.toInt() - local.totalCoins
                    if (falta > 0) { local.addCoins(falta); hubo = true }
                }

                @Suppress("UNCHECKED_CAST")
                (doc.get("robots_desbloqueados") as? List<String>)?.forEach { rid ->
                    if (!local.isCharacterUnlocked(rid)) {
                        local.unlockCharacter(rid); hubo = true
                    }
                }

                @Suppress("UNCHECKED_CAST")
                (doc.get("niveles_completados") as? List<String>)?.forEach { lid ->
                    if (!local.isLevelCompleted(lid)) {
                        local.markLevelCompleted(lid); hubo = true
                    }
                }

                doc.getString("modo_joystick")?.let { local.joystickMode = it }

                // Records: tambien gana el mas alto
                val records = db.collection(USUARIOS).document(id)
                    .collection(RECORDS).get().await()
                for (r in records.documents) {
                    val kind = GameKind.entries.firstOrNull { it.storageKey == r.id } ?: continue
                    val nube = (r.getLong("mejor_puntaje") ?: 0L).toInt()
                    if (nube > local.bestScoreFor(kind)) {
                        local.saveBestScore(kind, nube); hubo = true
                    }
                }
                lastError = null
                hubo
            }.onFailure { reportError("No se pudo bajar el progreso", it) }
                .getOrDefault(false)

            onDone(cambio)
        }
    }

    /**
     * Al iniciar sesion: primero fusiona lo que hay en la nube con lo local,
     * y luego sube el resultado. Asi ambos lados quedan con el valor mas alto.
     */
    fun syncOnLogin(onDone: (Boolean) -> Unit = {}) {
        pullAndMerge { cambio ->
            push()
            onDone(cambio)
        }
    }

    /**
     * Ultimo error de la nube, o null si la ultima operacion salio bien.
     * Antes los errores solo iban a Log.w y parecia que "no pasaba nada".
     */
    @Volatile
    var lastError: String? = null
        private set

    private fun reportError(msg: String, e: Throwable) {
        // Log.e para que salga en rojo en Logcat (filtro: tag:CloudStore)
        Log.e(TAG, "$msg: ${e.message}", e)
        lastError = "$msg: ${e.message}"
        // Tambien a Crashlytics como error no fatal, para verlo en la consola
        AnalyticsHelper.recordNonFatalError(e, "CloudStore - $msg")
    }

    private companion object {
        const val TAG = "CloudStore"
        const val USUARIOS = "usuarios"
        const val RECORDS = "records"
    }
}
