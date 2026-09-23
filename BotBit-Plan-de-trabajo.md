# BotBit — Plan de trabajo

Documento de alcance para el cierre del proyecto. Reemplaza al borrador previo.

**Estado actual:** modo Carrera completo (3 niveles verificados), selector de
modos, inventario, misiones, temas visuales con parallax, animaciones
procedurales y login con Google funcionando. Torre y Arena están declaradas
pero no implementadas.

---

## Prioridades

El backlog original tiene aproximadamente el triple de trabajo del que cabe en
el plazo. Esta es la clasificación honesta:

| Prioridad | Qué incluye | Por qué |
|---|---|---|
| **P0 — Imprescindible** | Modo Torre, monedero global, tienda real, menú nuevo, HUD del infinito, audio placeholder | Sin esto el proyecto se ve incompleto |
| **P1 — Importante** | Reclamar recompensa en misiones, pantalla de Ajustes, logo e ícono, Arena recortada | Suma mucho y cuesta poco, salvo Arena |
| **P2 — Si sobra tiempo** | Sincronización con Firestore, checkpoints persistentes, misiones nuevas, power-ups | Alto riesgo o bajo impacto por ahora |

Si hay que sacrificar algo, que sea P2 completo antes que entregar tres modos a
medias.

---

## 1. Economía y personajes

### 1.1 Monedero global

Hoy las monedas se guardan por nivel (`coins_<levelId>`) y no se acumulan.

**Qué se necesita:**
- Un saldo único `monedas_totales` en `ProgressStore`
- Al terminar cualquier partida, sumar las monedas de esa ronda al saldo
- Mantener el registro por nivel, que sirve para el "3/3" de cada nivel

**Criterio de aceptación:** jugar el nivel 1 dos veces suma monedas dos veces
al saldo global, pero el contador "3/3" del nivel no se duplica.

### 1.2 Tienda funcional

Los precios (50, 75, 100, 150, 200) ya existen en `CharacterData` pero no se
cobran.

**Qué se necesita:**
- Restar del saldo global al comprar
- Bloquear el botón si el saldo es insuficiente y mostrar cuánto falta
- Persistir el desbloqueo (ya existe `unlockCharacter`)

### 1.3 Stats que afectan la física — LEER ANTES DE IMPLEMENTAR

Esta es la parte más delicada del backlog, y no por lo difícil sino por lo
silencioso que es el fallo.

Si `SALTO` modifica `JUMP_VELOCITY`, **un nivel deja de ser "superable" y pasa
a ser "superable con ese robot"**. Con 3 niveles y 6 robots son 18
combinaciones distintas, y un robot con salto bajo puede volver imposible un
nivel sin que nadie lo note hasta que un jugador se atore.

Medí el umbral exacto con el verificador:

| Nivel | `JUMP_VELOCITY` mínimo | Altura de salto |
|---|---|---|
| Pradera Verde | 16.82 | 2.02 tiles |
| Cueva de Cristal | 16.82 | 2.02 tiles |
| **Distrito Neón** | **17.76** | **2.25 tiles** |

Por debajo de **17.76** el nivel 3 se vuelve imposible: la plataforma de la
azotea queda fuera de alcance.

**Mapeo recomendado:**

```kotlin
// SALTO (1-5) -> JUMP_VELOCITY. El piso duro es 17.76; se deja margen.
val jumpVelocity = 17.9f + salto * 0.3f      // 18.2 .. 19.4

// VELOCIDAD (1-5) -> multiplicador del scroll
val speedMultiplier = 0.95f + velocidad * 0.03f   // 0.98x .. 1.10x
```

Verifiqué las 18 combinaciones con este mapeo: **todas pasan.**

Sobre la velocidad hay una trampa aparte. Geométricamente los niveles aguantan
hasta 2.19x, pero eso es engañoso: **el verificador juega con timing perfecto.**
Subir la velocidad no hace los obstáculos geométricamente imposibles, reduce el
tiempo de reacción humano. Un nivel puede pasar la verificación y ser
injugable. Por eso el multiplicador se queda en ±10%, no en lo que la
geometría permitiría.

**Regla permanente:** cualquier cambio a `GameConfig` o al mapeo de stats
obliga a volver a correr `stats_matrix.py`. Son segundos y evita entregar un
nivel imposible.

### 1.4 Estadística PESO (propuesta)

Afectaría `GRAVITY` por robot. **Recomendación: no en esta entrega.** La
gravedad cambia la altura *y* el tiempo de vuelo a la vez, así que el espacio a
verificar se vuelve tridimensional. Si se hace, verificar antes.

---

## 2. Modo Torre

El más importante de lo que falta. Reaprovecha ~65% del motor actual.

### 2.1 Alcance

- **Un solo archivo JSON** con toda la ascensión, no niveles separados
- Cámara que sube por salas completas, sin scroll continuo
- Salto cargado: mantener presionado carga, soltar ejecuta, la dirección importa
- Rebote al chocar de lado estando en el aire
- **Sin muerte.** Caerse cuesta altura, no una vida

Ese último punto define el género. Si se le pone muerte y reinicio, deja de ser
Jump King.

### 2.2 Checkpoints

Al tocar ciertas plataformas marcadas, guardar sala y posición. Al reabrir la
app, reaparecer ahí.

**Recomendación:** implementar el guardado en `ProgressStore` (local) y dejar la
sincronización en la nube para después. El checkpoint tiene que escribirse al
instante; si depende de la red, se pierde progreso cuando falla.

### 2.3 Verificación

**El verificador actual no sirve para la Torre.** Comprueba que exista una
secuencia de saltos que avance en X a velocidad constante, y en la Torre no hay
avance automático.

Hace falta otro: un grafo de alcanzabilidad entre plataformas, donde dos
plataformas se conectan si alguna combinación de carga y dirección permite
llegar. Si una sala queda sin camino de entrada a salida, la torre es
imposible.

Mismo principio, otro algoritmo. Es material directo para la exposición.

---

## 3. Modo Arena

### 3.1 Alcance recortado a propósito

Vampire Survivors completo es un proyecto de meses. Lo que sí cabe:

- 1 tipo de enemigo que persigue al jugador
- 1 arma que dispara sola al enemigo más cercano
- Oleadas por temporizador que aumentan la cantidad
- XP que sube una sola estadística al subir de nivel

Se lee como el género, se puede demostrar, y se amplía después.

La división en "niveles (sobrevivir X tiempo)" e "infinito" queda para después
del recorte. Primero que funcione uno.

### 3.2 Dos requisitos técnicos obligatorios

**Pooling.** Arreglo fijo de ~250 enemigos creado al inicio, reciclados con una
bandera `activo`. Crear objetos por oleada alimenta al recolector de basura, y
cuando corre se pierde un frame completo.

**Rejilla espacial.** Con 200 enemigos, comparar todos contra todos son 19,900
parejas por frame. Con celdas de 2 tiles baja a unas 600.

Sin estas dos cosas no se llega a 60 fps. No son optimizaciones opcionales.

---

## 4. Misiones

### 4.1 Reclamar recompensa

Cambio de flujo: al cumplir el objetivo la misión pasa a estado
**"Por reclamar"** y aparece un botón. La recompensa se suma al monedero global
solo al presionarlo.

Tres estados: `ACTIVA` → `POR_RECLAMAR` → `RECLAMADA`.

### 4.2 Limpieza de interfaz

Quitar los botones de reinicio y borrado. La pantalla queda como una lista de
retos activos y otra de completados por reclamar.

### 4.3 Misiones nuevas

| Misión | Objetivo | Costo de implementar |
|---|---|---|
| Ahorrador | X monedas en una partida sin morir | Bajo — ya se cuentan |
| Maratón | 5000 tiles acumulados | Bajo — sumar `scrollX` al terminar |
| Casi me mato | Pasar a menos de 0.5 tiles de un pico, X veces | Medio |
| Socio de la Torre | Llegar al primer checkpoint con el robot Aqua | Depende de la Torre |

"Casi me mato" se implementa dentro del bucle de colisiones de `World.update`:
cuando se revisa un pico, si el hitbox mortal no choca pero la distancia
vertical es menor a 0.5 tiles, se cuenta un roce. No cuesta rendimiento porque
ese recorrido ya existe.

**Sugerencia:** empezar con Ahorrador y Maratón, que son casi gratis.

---

## 5. Navegación y menú

### 5.1 Estructura nueva

```
JUGAR        -> Selección de modo
PERSONAJES   -> Inventario y tienda
MISIONES
AJUSTES      -> nuevo
SALIR
```

Quitar "Modo Infinito" del menú principal: ya está dentro del selector.

### 5.2 Récords en contexto

Quitar la mejor puntuación de la pantalla de inicio. Mostrar el récord donde
corresponde: el de cada nivel dentro del selector de niveles, el del infinito
en su tarjeta, y así con cada modo.

`ProgressStore` ya tiene `bestScoreForLevel(levelId)` y `bestScoreFor(kind)`,
así que los datos ya existen.

### 5.3 Pantalla de Ajustes

Contenido mínimo: volumen de música, volumen de efectos, sesión de la cuenta y
borrar progreso local.

---

## 6. HUD del modo infinito

El infinito hoy muestra "0/3 monedas", que no tiene sentido cuando no hay un
máximo.

- Cambiar a contador acumulativo sin límite
- En el panel de resultados, mostrar "Monedas obtenidas: X" en vez de fracción
- La barra de progreso tampoco aplica: reemplazarla por distancia recorrida

---

## 7. Audio

### 7.1 SoundManager

Una clase que cargue la pista según el tema del nivel (Pradera, Cueva, Ciudad),
dejando el código listo para recibir los `.mp3`.

### 7.2 Disparadores

```kotlin
playJumpSound()
playCoinSound()
playDeathSound()
playWinSound()
```

**Detalle importante:** los efectos se disparan desde la UI, no desde `World`.
La simulación corre con paso fijo y puede ejecutar varios pasos en un frame; si
el sonido se dispara ahí, una moneda puede sonar dos veces. La UI compara el
contador de monedas contra el del frame anterior y suena una vez por cambio.

---

## 8. Identidad visual

- Logo "BotBit" estilizado en lugar del actual
- Ícono de aplicación en Android (todas las densidades y adaptativo)
- **Fondos:** ya resuelto en la entrega anterior. Los tres temas usan
  degradados y colores base, sin blanco puro. El terreno tiene césped, tierra y
  zona honda; los pozos son vacío oscuro con paredes.

---

## 9. Sincronización con Firestore

Las dependencias ya están y el login con Google funciona.

### 9.1 No migrar: reflejar

La tentación es mover el guardado de `SharedPreferences` a Firestore. **Es un
error con este plazo.** Firestore es asíncrono y puede fallar; si el guardado
del juego depende de la red, la partida se traba o se pierde progreso sin
conexión.

El patrón correcto:

- `SharedPreferences` sigue siendo la **verdad local**. El juego nunca espera a
  la red.
- Firestore recibe una copia en segundo plano cuando hay sesión y conexión.
- Al iniciar sesión en otro dispositivo, se baja la copia y se compara: gana el
  valor más alto en récords y monedas.

Así, si Firestore falla, el juego sigue funcionando exactamente igual.

### 9.2 Estructura

```
usuarios/{uid}
    monedas_totales: Int
    robots_desbloqueados: [String]
    personaje_seleccionado: String
    ajustes: { musica: Float, efectos: Float }
    misiones: { <id>: { progreso: Int, estado: String } }

usuarios/{uid}/records/{modo}
    modo: "runner_niveles" | "runner_infinito" | "torre" | "arena"
    mejor_puntaje: Int
    fecha: Timestamp
```

Separar los récords en subcolección permite consultarlos sin bajar todo el
documento del usuario, y deja la puerta abierta a una tabla global después.

### 9.3 Comentarios

Documentar el flujo de datos en español, marcando en cada función si escribe
local, remoto o ambos. El punto de confusión típico es no saber qué guardado es
la verdad.

---

## 10. Power-ups

**Estado: investigación, no implementación.** Ninguno cabe en esta entrega.

| Modo | Propuesta | Nota |
|---|---|---|
| Runner | Imán (atrae monedas 10 s) | No toca la física, seguro |
| Runner | Escudo (aguanta un choque) | **Invalida el verificador**: cambia qué es superable |
| Torre | Suela adherente (menos rebote) | Coherente con el género |
| Torre | Hélices (impulso extra en aire) | Rompe el grafo de alcanzabilidad |
| Arena | Doble disparo | Sencillo |
| Arena | Sobrecarga (mata por contacto) | Sencillo |

El patrón se repite: los power-ups que **cambian lo que el jugador puede hacer**
obligan a re-verificar los niveles. Los cosméticos o de conveniencia (imán,
doble disparo) son gratis. Si se implementan, empezar por esos.

---

## Cronograma sugerido

| Semana | Entregable |
|---|---|
| 1 | Monedero global, tienda real, menú nuevo, HUD del infinito |
| 2–3 | Modo Torre con su verificador |
| 4 | Arena recortada, misiones con reclamo, audio placeholder |
| 5 | Ajustes, logo e ícono, pulido, exposición |

Firestore entra en la semana 1 **solo si es requisito de la materia**. Si no,
va al final: es lo más riesgoso y lo menos visible en una demo.
