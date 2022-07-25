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

    @Test
    fun dissocEntry_SameKeyAndHash() {
        assertNull(dissoc(Entry(1, 1, 1), 0, 1, 1))
    }

    @Test
    fun dissocEntry_WrongHash() {
        assertEquals(Entry(1, 1, 1), dissoc(Entry(1, 1, 1), 0, 2, 1))
    }

    @Test
    fun dissocCollisionNode_WrongHash() {
        val c = makeCollisionNode(Entry(1, 1, 1), Entry(1, 2, 2))
        assertTrue(dissoc(c, 0, 2, 1) == c)
    }

    @Test
    fun dissocCollisionNode_WrongKey() {
        val c = makeCollisionNode(Entry(1, 1, 1), Entry(1, 2, 2))
        assertTrue(dissoc(c, 0, 1, 3) == c)
    }

    @Test
    fun dissocCollisionNode_ReturnEntry() {
        val c = makeCollisionNode(Entry(1, 1, 1), Entry(1, 2, 2))
        assertEquals(Entry(1, 1, 1), dissoc(c, 0, 1, 2))
    }

    @Test
    fun dissocCollisionNode_NewCollisionNode() {
        val c = makeCollisionNode(
            Entry(1, 1, 1),
            Entry(1, 2, 2),
            Entry(1, 3, 3))
        assertEquals(
            makeCollisionNode(Entry(1, 2, 2), Entry(1, 3, 3)),
            dissoc(c, 0, 1, 1))
    }

    @Test
    fun dissocArrayNode_Unchanged() {
        val a = makeArrayNodeOf(Entry(1, 1, 1), Entry(2, 2, 2))
        assertTrue(dissoc(a, 0, 3, 3) == a)
    }

    @Test
    fun dissocArrayNode_UnchangedChild() {
        val a = makeArrayNodeOf(Entry(1, 1, 1), Entry(2, 2, 2))
        assertTrue(dissoc(a, 0, 33, 3) == a)
    }

    @Test
    fun dissocArrayNode_NewArrayNode() {
        val a = dissoc(
            makeArrayNodeOf(
                Entry(1, 1, 1),
                Entry(2, 2, 2),
                Entry(3, 3, 3)),
            0, 2, 2) as ArrayNode
        assertEquals(2, a.childrenCount)
        assertEquals(2, a.entryCount)
        assertEquals(Entry(1, 1, 1), getEntry(a, 0, 1, 1))
        assertNull(getEntry(a, 0, 2, 2))
        assertEquals(Entry(3, 3, 3), getEntry(a, 0, 3, 3))
    }

    @Test
    fun dissocArrayNode_ReturnLastChild() {
        val e = Entry(1, 1, 1)
        val a = makeArrayNodeOf(e, Entry(2, 2, 2))
        assertTrue(dissoc(a, 0, 2, 2) == e)
    }

    @Test
    fun dissocArrayNode_LastChildIsArrayNode() {
        val a = dissoc(
            makeArrayNodeOf(
                Entry(1, 1, 1),
                Entry(2, 2, 2),
                Entry(33, 3, 3)),
            0, 2, 2) as ArrayNode
        assertEquals(1, a.childrenCount)
        assertEquals(2, a.entryCount)
        assertEquals(Entry(1, 1, 1), getEntry(a, 0, 1, 1))
        assertNull(getEntry(a, 0, 2, 2))
        assertEquals(Entry(33, 3, 3), getEntry(a, 0, 33, 3))
    }

    @Test
    fun dissocArrayNode_NewChildIsArrayNode() {
        val a = dissoc(
            makeArrayNodeOf(
                Entry(1, 1, 1),
                Entry(33, 2, 2),
                Entry(65, 3, 3)),
            0, 33, 2) as ArrayNode
        assertEquals(1, a.childrenCount)
        assertEquals(2, a.entryCount)
        assertEquals(Entry(1, 1, 1), getEntry(a, 0, 1, 1))
        assertNull(getEntry(a, 0, 33, 2))
        assertEquals(Entry(65, 3, 3), getEntry(a, 0, 65, 3))
    }

    @Test
    fun dissocArrayNode_ReturnNewChild() {
        val e = Entry(1, 1, 1)
        val a = makeArrayNodeOf(e, Entry(33, 3, 3))
        assertTrue(dissoc(a, 0, 33, 3) == e)
    }
}

internal fun makeCollisionNode(vararg entries: Entry): CollisionNode {
    assertTrue(entries.size > 1)
    val keyHash = entries[0].keyHash
    for (e in entries) {
        assertTrue(e.keyHash == keyHash)
        assertEquals(1, entries.count { it.key == e.key })
    }
    return CollisionNode(entries.toCollection(ArrayList()), keyHash)
}

internal fun makeArrayNodeOf(vararg entries: Entry): ArrayNode {
    assertTrue(entries.size > 1);
    for (e in entries) {
        assertEquals(1, entries.count { it.key == e.key })
    }
    return entries.drop(1).fold(
        makeArrayNode(entries[0], 0),
        { result, entry -> assoc(result, 0, entry) as ArrayNode })
}
