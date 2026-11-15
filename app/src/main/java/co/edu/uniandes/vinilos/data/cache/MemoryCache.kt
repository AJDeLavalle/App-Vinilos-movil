package co.edu.uniandes.vinilos.data.cache

/**
 * Caché en memoria acotada para las respuestas del backend.
 *
 * - Guarda como máximo [maxEntries] elementos y descarta el usado hace más tiempo (LRU), así la
 *   memoria no crece con cada detalle que se abre.
 * - Cada elemento vence a los [ttlMillis]: los datos los editan otros usuarios del backend.
 * - Los repositorios la invalidan cuando la app escribe (crear álbum, asociar track).
 *
 * Es segura entre hilos: los repositorios la usan desde Dispatchers.IO.
 */
class MemoryCache<K : Any, V : Any>(
    private val maxEntries: Int,
    private val ttlMillis: Long,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private class Entry<V>(val value: V, val storedAt: Long)

    // accessOrder = true: el orden de iteración va del menos al más recientemente usado.
    private val entries = object : LinkedHashMap<K, Entry<V>>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, Entry<V>>) = size > maxEntries
    }

    @Synchronized
    operator fun get(key: K): V? {
        val entry = entries[key] ?: return null
        if (clock() - entry.storedAt >= ttlMillis) {
            entries.remove(key)
            return null
        }
        return entry.value
    }

    @Synchronized
    operator fun set(key: K, value: V) {
        entries[key] = Entry(value, clock())
    }

    @Synchronized
    fun remove(key: K) {
        entries.remove(key)
    }

    @Synchronized
    fun clear() = entries.clear()

    @get:Synchronized
    val size: Int get() = entries.size
}

/** Devuelve lo guardado en [key] o lo carga con [load] y lo guarda. Los errores no se guardan. */
suspend fun <K : Any, V : Any> MemoryCache<K, V>.getOrLoad(key: K, load: suspend () -> V): V =
    this[key] ?: load().also { this[key] = it }
