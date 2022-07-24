package net.akrain.fdmap.kotlin

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNull

internal class TrieTest {

    @Test
    fun assocEntry_SameKeyAndValue() {
        val e = Entry(1, 1, 1)
        assertEquals(e, assoc(e, 0, Entry(1, 1, 1)))
    }

    @Test
    fun assocEntry_SameKey() {
        val e = Entry(1, 1, 1)
        assertTrue(assoc(Entry(1, 1, 2), 0, e) === e)
    }

    @Test
    fun assocEntry_ReturnCollisionNode() {
        val c = assoc(Entry(1, 1, 1), 0, Entry(1, 2, 2)) as CollisionNode
        assertEquals(1, c.keyHash)
        assertEquals(2, c.children.size)
        assertEquals(Entry(1, 1, 1), c.children.get(0))
        assertEquals(Entry(1, 2, 2), c.children.get(1))
    }

    @Test
    fun assocEntry_ReturnArrayNode() {
        val a = assoc(Entry(1, 1, 1), 0, Entry(2, 2, 2)) as ArrayNode
        assertEquals(2, a.childrenCount)
        assertEquals(2, a.entryCount)
        assertEquals(Entry(1, 1, 1), a.children[1])
        assertEquals(Entry(2, 2, 2), a.children[2])
    }

    @Test
    fun assocCollisionNode_AddChild() {
        val c = assoc(
            makeCollisionNode(Entry(1, 1, 1), Entry(1, 2, 2)),
            0,
            Entry(1, 3, 3)) as CollisionNode
        assertEquals(1, c.keyHash)
        assertEquals(3, c.children.size)
        assertEquals(Entry(1, 1, 1), c.children.get(0))
        assertEquals(Entry(1, 2, 2), c.children.get(1))
        assertEquals(Entry(1, 3, 3), c.children.get(2))
    }

    @Test
    fun assocCollisionNode_SameEntry() {
        val c = makeCollisionNode(Entry(1, 1, 1), Entry(1, 2, 2));
        assertTrue(assoc(c, 0, Entry(1, 1, 1)) === c)
    }

    @Test
    fun assocCollisionNode_ReplaceEntry() {
        val c = assoc(
            makeCollisionNode(Entry(1, 1, 1), Entry(1, 2, 2)),
            0,
            Entry(1, 2, 3)) as CollisionNode
        assertEquals(1, c.keyHash)
        assertEquals(2, c.children.size)
        assertEquals(Entry(1, 1, 1), c.children.get(0))
        assertEquals(Entry(1, 2, 3), c.children.get(1))
    }

    @Test
    fun assocCollisionNode_ReturnArrayNode() {
        val c = makeCollisionNode(Entry(1, 1, 1), Entry(1, 2, 2))
        val a = assoc(c, 0, Entry(2, 2, 2)) as ArrayNode
        assertEquals(3, a.entryCount)
        assertEquals(2, a.childrenCount)
        assertEquals(c, a.children[1])
        assertEquals(Entry(2, 2, 2), a.children[2])
    }

    @Test
    fun assocArrayNode_SameChild() {
        val a = makeArrayNodeOf(Entry(1, 1, 1), Entry(2, 2, 2))
        assertTrue(assoc(a, 0, Entry(1, 1, 1)) == a)
    }

    @Test
    fun assocArrayNode_ReplaceChild() {
        val a = assoc(
            makeArrayNodeOf(Entry(1, 1, 1), Entry(2, 2, 2)),
            0,
            Entry(1, 1, 2)) as ArrayNode
        assertEquals(2, a.childrenCount)
        assertEquals(2, a.entryCount)
        assertEquals(Entry(1, 1, 2), a.children[1])
        assertEquals(Entry(2, 2, 2), a.children[2])
    }

    @Test
    fun getEntry_Entry_Same() {
        assertEquals(Entry(1, 1, 1), getEntry(Entry(1, 1, 1), 0, 1, 1))
    }

    @Test
    fun getEntry_Entry_WrongKey() {
        assertNull(getEntry(Entry(1, 1, 1), 0, 1, 2))
    }

    @Test
    fun getEntry_Entry_WrongKeyAndHash() {
        assertNull(getEntry(Entry(1, 1, 1), 0, 2, 2))
        assertNull(getEntry(Entry(1, 1, 1), 0, 2, 1))
    }

    @Test
    fun getEntry_CollisionNode() {
        assertEquals(
            Entry(1, 2, 2),
            getEntry(
                makeCollisionNode(Entry(1, 1, 1), Entry(1, 2, 2)),
                0, 1, 2))
    }

    @Test
    fun getEntry_CollisionNode_WrongKeyHash() {
        assertNull(
            getEntry(
                makeCollisionNode(Entry(1, 1, 1), Entry(1, 2, 2)),
                0, 2, 3))
    }

    @Test
    fun getEntry_CollisionNode_WrongKey() {
        assertNull(
            getEntry(
                makeCollisionNode(Entry(1, 1, 1), Entry(1, 2, 2)),
                0, 1, 3))
    }

    @Test
    fun getEntry_ArrayNode() {
        assertEquals(
            Entry(2, 2, 2),
            getEntry(
                makeArrayNodeOf(Entry(1, 1, 1), Entry(2, 2, 2)),
                0, 2, 2))
    }

    @Test
    fun getEntry_ArrayNode_NotFound() {
        assertNull(
            getEntry(
                makeArrayNodeOf(Entry(1, 1, 1), Entry(2, 2, 2)),
                0, 3, 3))
    }
}

internal fun makeCollisionNode(e1: Entry, e2: Entry): CollisionNode {
    assertTrue(e1.keyHash == e2.keyHash)
    assertTrue(e1.key != e2.key)
    return CollisionNode(arrayListOf(e1, e2), e1.keyHash)
}

internal fun makeArrayNodeOf(vararg entries: Entry): ArrayNode {
    assertTrue(entries.size >= 2);
    return entries.drop(1).fold(
        makeArrayNode(entries[0], 0),
        { result, entry -> assoc(result, 0, entry) as ArrayNode })
}
