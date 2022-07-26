package net.akrain.fdmap.kotlin

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNull

internal class PHashMapTest {

    @Test
    fun blankMap() {
        val m = PHashMap.blank()
        assertTrue(m === PHashMap.blank())
        assertEquals(0, m.count())
    }

    @Test
    fun blankMapWithHasher() {
        val hasher = fun(x: Any?): Int { return x as Int }
        val m = PHashMap.blank(hasher)
        assertTrue(m === PHashMap.blank(hasher))
        assertTrue(m != PHashMap.blank())
        assertEquals(0, m.count())
    }

    @Test
    fun blankGet() {
        val m = PHashMap.blank()
        assertEquals(42, m.get(1, 42))
    }

    @Test
    fun assocEntry() {
        val m0 = PHashMap.blank()
        val m1 = m0.assoc(1, 42)
        assertTrue(m0 === PHashMap.blank())
        assertTrue(m0 != m1)
        assertNull(m0.get(1))
        assertEquals(42, m1.get(1))
    }

    @Test
    fun assocNotEmpty() {
        val m0 = PHashMap.blank()
        val m1 = m0.assoc(1, 2)
        val m2 = m1.assoc(3, 4)

        assertTrue(m0 != m1)
        assertTrue(m0 != m2)
        assertTrue(m1 != m2)

        assertNull(m0.get(1))
        assertNull(m0.get(2))
        assertNull(m0.get(3))
        assertNull(m0.get(4))

        assertEquals(2, m1.get(1))
        assertNull(m1.get(2))
        assertNull(m1.get(3))
        assertNull(m1.get(4))

        assertEquals(2, m2.get(1))
        assertNull(m2.get(2))
        assertEquals(4, m2.get(3))
        assertNull(m2.get(4))
    }

    @Test
    fun assocSameEntry() {
        val m0 = PHashMap.blank()
        val m1 = m0.assoc(1, 42)
        val m2 = m1.assoc(1, 42)
        assertTrue(m0 != m1)
        assertTrue(m1 === m2)
    }

    @Test
    fun assocKeepHasher() {
        val m0 = PHashMap.blank{x -> x as Int}
        assertTrue(m0.assoc(1, 42).keyHasher === m0.keyHasher)
    }

    @Test
    fun dissocBlank() {
        val m = PHashMap.blank().dissoc(1)
        assertTrue(m === PHashMap.blank())
    }

    @Test
    fun dissocWrongKey() {
        val m = PHashMap.blank().assoc(1, 42)
        assertTrue(m.dissoc(2) === m)
    }

    @Test
    fun dissocReturnBlank() {
        val m = PHashMap.blank().assoc(1, 42)
        assertTrue(m.dissoc(1) === PHashMap.blank())
    }

    @Test
    fun dissocKeepHasher() {
        val hasher = fun(x: Any?): Int { return x as Int }
        val m = PHashMap.blank(hasher).assoc(1, 42)
        assertTrue(m.dissoc(1).keyHasher === hasher)
    }

    @Test
    fun dissocReturnNewMap() {
        val m = PHashMap.blank().assoc(1, 2).assoc(3, 4).dissoc(1)
        assertEquals(42, m.get(1, 42))
        assertEquals(4, m.get(3))
    }

    @Test
    fun countEmpty() {
        assertEquals(0, PHashMap.blank().count())
    }

    @Test
    fun countNonEmpty() {
        assertEquals(1, PHashMap.blank().assoc(5, 42).count())
    }
}
