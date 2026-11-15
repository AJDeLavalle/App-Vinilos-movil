package co.edu.uniandes.vinilos.data.cache

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MemoryCacheTest {

    private var now = 0L
    private fun cache(maxEntries: Int = 3, ttl: Long = 1_000) = MemoryCache<Int, String>(maxEntries, ttl) { now }

    @Test
    fun guardado_seDevuelveMientrasNoVenza() {
        val cache = cache()
        cache[1] = "uno"

        now = 999
        assertEquals("uno", cache[1])

        now = 1_000
        assertNull(cache[1])
    }

    @Test
    fun alSuperarElMaximo_descartaElUsadoHaceMasTiempo() {
        val cache = cache(maxEntries = 2)
        cache[1] = "uno"
        cache[2] = "dos"
        cache[1] // 1 pasa a ser el más reciente

        cache[3] = "tres"

        assertEquals("uno", cache[1])
        assertNull(cache[2])
        assertEquals("tres", cache[3])
        assertEquals(2, cache.size)
    }

    @Test
    fun getOrLoad_cargaUnaSolaVez_yNoGuardaErrores() = runTest {
        val cache = cache()
        var loads = 0

        repeat(3) { cache.getOrLoad(1) { loads++; "uno" } }
        assertEquals(1, loads)

        runCatching { cache.getOrLoad(2) { throw IllegalStateException("sin red") } }
        assertNull(cache[2])
    }

    @Test
    fun removeYClear_invalidan() {
        val cache = cache()
        cache[1] = "uno"
        cache[2] = "dos"

        cache.remove(1)
        assertNull(cache[1])

        cache.clear()
        assertEquals(0, cache.size)
    }
}
