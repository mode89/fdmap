package net.akrain.fdmap;

import static net.akrain.fdmap.Map.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;

public class MapTest {

    CollisionNode makeCollisionNode(final Entry e1, final Entry e2) {
        assertTrue(e1.keyHash == e2.keyHash);
        assertFalse(e1.key.equals(e2.key));
        final ArrayList<Entry> children = new ArrayList<>();
        children.add(e1);
        children.add(e2);
        return new CollisionNode(children, e1.keyHash);
    }

    @Test
    void nodeAssocEntrySame() {
        final Entry e1 = new Entry(1, 1, 1);
        final Entry e2 = new Entry(1, 1, 1);
        assertTrue(e1 == nodeAssoc(e1, 0, e2));
    }

    @Test
    void nodeAssocEntrySameKey() {
        final Entry e1 = new Entry(1, 1, 1);
        final Entry e2 = new Entry(1, 1, 2);
        assertTrue(e2 == nodeAssoc(e1, 0, e2));
    }

    @Test
    void nodeAssocEntryCollision() {
        final Entry e1 = new Entry(1, 1, 1);
        final Entry e2 = new Entry(1, 2, 2);
        final CollisionNode n = (CollisionNode) nodeAssoc(e1, 0, e2);
        assertTrue(n.children.contains(e1));
        assertTrue(n.children.contains(e2));
        assertEquals(1, n.keyHash);
    }

    @Test
    void nodeAssocEntryDifferent() {
        final Entry e1 = new Entry(1, 1, 1);
        final Entry e2 = new Entry(2, 2, 2);
        final ArrayNode n = (ArrayNode) nodeAssoc(e1, 0, e2);
        assertTrue(n.children[1] == e1);
        assertTrue(n.children[2] == e2);
        assertEquals(2, n.childrenCount);
    }

    @Test
    void nodeAssocArrayNodeSameChild() {
        final Entry e1 = new Entry(1, 1, 1);
        final Entry e2 = new Entry(1, 1, 1);
        final ArrayNode n1 = makeArrayNode(e1, 0);
        final ArrayNode n2 = (ArrayNode) nodeAssoc(n1, 0, e2);
        assertTrue(n2 == n1);
    }

    @Test
    void nodeAssocArrayNodeReplaceChild() {
        final Entry e1 = new Entry(1, 1, 1);
        final Entry e2 = new Entry(33, 2, 2);
        final ArrayNode n1 = makeArrayNode(e1, 0);
        final ArrayNode n2 = (ArrayNode) nodeAssoc(n1, 0, e2);
        assertEquals(1, n2.childrenCount);
        final ArrayNode n3 = (ArrayNode) n2.children[1];
        assertEquals(2, n3.childrenCount);
        assertTrue(n3.children[0] == e1);
        assertTrue(n3.children[1] == e2);
    }

    @Test
    void nodeAssocCollisionNode() {
        final CollisionNode cn = makeCollisionNode(
            new Entry(1, 1, 1), new Entry(1, 2, 2));
        final Entry en = new Entry(2, 3, 3);
        final ArrayNode n = (ArrayNode) nodeAssoc(cn, 0, en);
        assertTrue(n.children[1] == cn);
        assertTrue(n.children[2] == en);
    }

    @Test
    void nodeAssocCollisionNodeSameEntry() {
        final CollisionNode cn = makeCollisionNode(
            new Entry(1, 1, 1), new Entry(1, 2, 2));
        final Entry en = new Entry(1, 2, 2);
        assertTrue(nodeAssoc(cn, 0, en) == cn);
    }

    @Test
    void nodeAssocCollisionNodeNewEntry() {
        final Entry e1 = new Entry(1, 1, 1);
        final Entry e2 = new Entry(1, 2, 2);
        final Entry e3 = new Entry(1, 3, 3);
        final CollisionNode cn = makeCollisionNode(e1, e2);
        final CollisionNode n = (CollisionNode) nodeAssoc(cn, 0, e3);
        assertTrue(n.children.contains(e1));
        assertTrue(n.children.contains(e2));
        assertTrue(n.children.contains(e3));
    }

    @Test
    void nodeAssocCollisionNodeReplaceEntry() {
        final Entry e1 = new Entry(1, 1, 1);
        final Entry e2 = new Entry(1, 2, 2);
        final Entry e3 = new Entry(1, 2, 3);
        final CollisionNode cn = makeCollisionNode(e1, e2);
        final CollisionNode n = (CollisionNode) nodeAssoc(cn, 0, e3);
        assertTrue(n.children.contains(e1));
        assertFalse(n.children.contains(e2));
        assertTrue(n.children.contains(e3));
    }

    @Test
    void nodeGetEntry_Entry() {
        final Entry e = new Entry(1, 1, 1);
        assertTrue(nodeGetEntry(e, 0, 1, 1) == e);
        assertNull(nodeGetEntry(e, 0, 1, 2));
        assertNull(nodeGetEntry(e, 0, 2, 2));
    }

    @Test
    void nodeGetEntry_ArrayNode() {
        final Entry e = new Entry(1, 1, 1);
        final ArrayNode a = makeArrayNode(e, 0);
        assertTrue(nodeGetEntry(a, 0, 1, 1) == e);
        assertNull(nodeGetEntry(a, 0, 2, 2));
    }

    @Test
    void nodeGetEntry_CollisionNode() {
        final Entry e1 = new Entry(1, 1, 1);
        final Entry e2 = new Entry(1, 2, 2);
        final CollisionNode cn = makeCollisionNode(e1, e2);
        assertTrue(nodeGetEntry(cn, 0, 1, 1) == e1);
        assertNull(nodeGetEntry(cn, 0, 3, 3));
    }

    @Test
    void nodeDissoc_Entry() {
        final Entry e = new Entry(1, 1, 1);
        assertTrue(nodeDissoc(e, 0, 1, 2) == e);
        assertNull(nodeDissoc(e, 0, 1, 1));
    }

    @Test
    void nodeDissoc_ArrayNode() {
        final Entry e1 = new Entry(1, 1, 1);
        final ArrayNode a1 = makeArrayNode(e1, 0);
        assertTrue(nodeDissoc(a1, 0, 2, 2) == a1);
        assertTrue(nodeDissoc(a1, 0, 1, 2) == a1);
        assertNull(nodeDissoc(a1, 0, 1, 1));

        final Entry e2 = new Entry(2, 2, 2);
        final ArrayNode a2 = (ArrayNode) nodeAssoc(a1, 0, e2);
        final ArrayNode n2 = (ArrayNode) nodeDissoc(a2, 0, 1, 1);
        assertNull(n2.children[1]);
        assertTrue(n2.children[2] == e2);
        assertEquals(1, n2.childrenCount);

        final Entry e3 = new Entry(33, 3, 3);
        final ArrayNode a3 = (ArrayNode) nodeAssoc(a1, 0, e3);
        final ArrayNode n3 = (ArrayNode) nodeDissoc(a3, 0, 1, 1);
        assertNull(nodeGetEntry(n3, 0, 1, 1));
        assertTrue(nodeGetEntry(n3, 0, 33, 3) == e3);
    }

    @Test
    void nodeDissoc_CollisionNode() {
        final Entry e1 = new Entry(1, 1, 1);
        final Entry e2 = new Entry(1, 2, 2);
        final CollisionNode c1 = makeCollisionNode(e1, e2);
        assertTrue(nodeDissoc(c1, 0, 3, 3) == c1);
        assertTrue(nodeDissoc(c1, 0, 1, 3) == c1);
        assertTrue(nodeDissoc(c1, 0, 1, 1) == e2);

        final Entry e3 = new Entry(1, 3, 3);
        final CollisionNode c2 = (CollisionNode) nodeAssoc(c1, 0, e3);
        final CollisionNode c3 = (CollisionNode) nodeDissoc(c2, 0, 1, 1);
        assertEquals(2, c3.children.size());
        assertNull(nodeGetEntry(c3, 0, 1, 1));
        assertTrue(nodeGetEntry(c3, 0, 1, 2) == e2);
        assertTrue(nodeGetEntry(c3, 0, 1, 3) == e3);
    }

    @Test
    void nodeDifferenceIdentical() {
        final Entry e = new Entry(1, 1, 1);
        assertNull(nodeDifference(0, e, e));
    }

    @Test
    void nodeDifferenceNull() {
        final Entry e = new Entry(1, 1, 1);
        assertTrue(nodeDifference(0, e, null) == e);
        assertNull(nodeDifference(0, null, e));
    }

    @Test
    void nodeDifferenceEntry() {
        final Entry e1 = new Entry(1, 1, 1);
        assertNull(nodeDifference(0, e1, new Entry(1, 1, 1)));
        assertTrue(nodeDifference(0, e1, new Entry(1, 1, 2)) == e1);
        assertTrue(nodeDifference(0, e1, new Entry(2, 2, 1)) == e1);

        assertNull(nodeDifference(0, e1, makeArrayNode(e1, 0)));
        assertTrue(nodeDifference(
            0, e1, makeArrayNode(new Entry(2, 2, 2), 0)) == e1);
        assertNull(nodeDifference(
            0, e1, makeArrayNode(new Entry(1, 1, 1), 0)));
        assertTrue(nodeDifference(
            0, e1, makeArrayNode(new Entry(1, 1, 2), 0)) == e1);
        assertNull(nodeDifference(
            0, e1, makeCollisionNode(
                new Entry(1, 1, 1), new Entry(1, 2, 2))));
    }

    @Test
    void nodeDifferenceArrayNode() {
        final Entry e1 = new Entry(1, 1, 1);
        final ArrayNode a = makeArrayNode(e1, 0);
        assertTrue(nodeDifference(0, a, new Entry(2, 2, 2)) == a);
        assertTrue(nodeDifference(0, a, new Entry(1, 1, 2)) == a);
        assertNull(nodeDifference(0, a, e1));
        assertNull(nodeDifference(0, a, new Entry(1, 1, 1)));

        assertNull(nodeDifference(
            0, a, makeArrayNode(new Entry(1, 1, 1), 0)));
        assertTrue(nodeDifference(
            0, a, makeArrayNode(new Entry(1, 1, 2), 0)) == a);

        final Entry e2 = new Entry(2, 2, 2);
        final ArrayNode d = (ArrayNode) nodeDifference(
            0, nodeAssoc(a, 0, e2), a);
        assertNull(nodeGetEntry(d, 0, 1, 1));
        assertTrue(nodeGetEntry(d, 0, 2, 2) == e2);

        assertTrue(nodeDifference(
            0, a, makeCollisionNode(
                new Entry(1, 2, 2), new Entry(1, 3, 3))) == a);
        assertNull(nodeDifference(
            0, a, makeCollisionNode(e1, new Entry(1, 2, 2))));
        assertNull(nodeDifference(
            0, a, makeCollisionNode(
                new Entry(1, 2, 2), new Entry(1, 1, 1))));
        assertTrue(nodeDifference(
            0, a, makeCollisionNode(
                new Entry(1, 1, 2), new Entry(1, 2, 2))) == a);
    }

    @Test
    void nodeDifferenceCollisionNode() {
        final Entry e1 = new Entry(1, 1, 1);
        final Entry e2 = new Entry(1, 2, 2);
        final CollisionNode c = makeCollisionNode(e1, e2);
        assertTrue(nodeDifference(0, c, new Entry(1, 2, 2)) == e1);
        assertNull(nodeDifference(0, c, makeCollisionNode(e1, e2)));
        assertTrue(nodeDifference(0, c, makeArrayNode(e1, 0)) == e2);
        assertTrue(nodeDifference(
            0, c, makeArrayNode(new Entry(1, 2, 3), 0)) == c);

        final CollisionNode d = (CollisionNode) nodeDifference(
            0, nodeAssoc(c, 0, new Entry(1, 3, 3)),
            makeArrayNode(new Entry(1, 3, 3), 0));
        assertTrue(nodeGetEntry(d, 0, 1, 1) == e1);
        assertTrue(nodeGetEntry(d, 0, 1, 2) == e2);
        assertNull(nodeGetEntry(d, 0, 1, 3));
    }

    @Test
    void map() {
        final Map m1 = new Map(null);
        final Map m2 = m1.assoc(1, "1");
        final Map m3 = m2.assoc(1, "1");
        final Map m4 = m2.assoc(1, "2");
        final Map m5 = m1.dissoc(1);
        final Map m6 = m4.dissoc(2);
        final Map m7 = m2.dissoc(1);
        final Map m8 = m3.difference(m4);
        final Map m9 = m2.assoc(2, "2").difference(m2);
        assertNull(m1.get(1));
        assertEquals("1", m2.get(1));
        assertTrue(m2 == m3);
        assertEquals("2", m4.get(1));
        assertNull(m4.get(2));
        assertTrue(m1 == m5);
        assertTrue(m4 == m6);
        assertNull(m7.get(1));
        assertTrue(m8 == m3);
        assertNull(m9.get(1));
        assertEquals("2", m9.get(2));
    }
}
