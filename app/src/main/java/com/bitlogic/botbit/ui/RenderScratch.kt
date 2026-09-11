package com.bitlogic.botbit.ui

import androidx.compose.ui.graphics.Path

/**
 * Objetos reutilizables del bucle de dibujo.
 *
 * Se crea UNA vez en GameScreen con remember y se reutiliza en cada frame.
 * Crear un Path dentro del Canvas parece inofensivo, pero a 60 fps con varios
 * picos en pantalla son cientos de objetos por segundo. Cuando el recolector
 * de basura corre, se pierde un frame entero, y en un juego de precision eso
 * es una muerte que el jugador no se merecia.
 *
 * Un Path se puede reutilizar siempre que se llame reset() antes de volver a
 * construirlo: drawPath lo consume en el momento.
 */
class RenderScratch {
    /** Path de un solo uso para picos y marcas. Llamar reset() antes de usarlo. */
    val path = Path()

    /** Path secundario, para cuando se necesitan dos formas a la vez. */
    val path2 = Path()

    /** El fondo cachea sus propias rutas de siluetas. */
    val background = BackgroundRenderer()
}
