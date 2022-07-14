package net.akrain.fdmap;

import static net.akrain.fdmap.Nodes.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;

public class NodesTest {

    CollisionNode makeCollisionNode(final Entry e1, final Entry e2) {
        assertTrue(e1.keyHash == e2.keyHash);
        assertFalse(Nodes.equal(e1.key, e2.key));
        final ArrayList<Entry> children = new ArrayList<>();
        children.add(e1);
        children.add(e2);
        return new CollisionNode(children, e1.keyHash);
    }

    @Test
    void assocEntrySame() {
        final Entry e1 = new Entry(1, 1, 1);
        final Entry e2 = new Entry(1, 1, 1);
        assertTrue(e1 == assoc(e1, 0, e2));
    }

    @Test
    void assocEntrySameKey() {
        final Entry e1 = new Entry(1, 1, 1);
        final Entry e2 = new Entry(1, 1, 2);
        assertTrue(e2 == assoc(e1, 0, e2));
    }

    @Test
    void assocEntryCollision() {
        final Entry e1 = new Entry(1, 1, 1);
        final Entry e2 = new Entry(1, 2, 2);
        final CollisionNode n = (CollisionNode) assoc(e1, 0, e2);
        assertTrue(n.children.contains(e1));
        assertTrue(n.children.contains(e2));
        assertEquals(1, n.keyHash);
    }

    @Test
    void assocEntryDifferent() {
        final Entry e1 = new Entry(1, 1, 1);
        final Entry e2 = new Entry(2, 2, 2);
        final ArrayNode n = (ArrayNode) assoc(e1, 0, e2);
        assertTrue(n.children[1] == e1);
        assertTrue(n.children[2] == e2);
        assertEquals(2, n.childrenCount);
    }

    @Test
    void assocEntryNullKey() {
        final Entry e = new Entry(42, null, 1);
        assertTrue(assoc(e, 0, new Entry(42, null, 1)) == e);
    }

    @Test
    void assocArrayNodeSameChild() {
        final Entry e1 = new Entry(1, 1, 1);
        final Entry e2 = new Entry(1, 1, 1);
        final ArrayNode n1 = makeArrayNode(e1, 0);
        final ArrayNode n2 = (ArrayNode) assoc(n1, 0, e2);
        assertTrue(n2 == n1);
    }

    @Test
    void assocArrayNodeReplaceChild() {
        final Entry e1 = new Entry(1, 1, 1);
        final Entry e2 = new Entry(33, 2, 2);
        final ArrayNode n1 = makeArrayNode(e1, 0);
        final ArrayNode n2 = (ArrayNode) assoc(n1, 0, e2);
        assertEquals(1, n2.childrenCount);
        final ArrayNode n3 = (ArrayNode) n2.children[1];
        assertEquals(2, n3.childrenCount);
        assertTrue(n3.children[0] == e1);
        assertTrue(n3.children[1] == e2);
    }

    @Test
    void assocCollisionNode() {
        final CollisionNode cn = makeCollisionNode(
            new Entry(1, 1, 1), new Entry(1, 2, 2));
        final Entry en = new Entry(2, 3, 3);
        final ArrayNode n = (ArrayNode) assoc(cn, 0, en);
        assertTrue(n.children[1] == cn);
        assertTrue(n.children[2] == en);
    }

    @Test
    void assocCollisionNodeSameEntry() {
        final CollisionNode cn = makeCollisionNode(
            new Entry(1, 1, 1), new Entry(1, 2, 2));
        final Entry en = new Entry(1, 2, 2);
        assertTrue(assoc(cn, 0, en) == cn);
    }

    @Test
    void assocCollisionNodeNewEntry() {
        final Entry e1 = new Entry(1, 1, 1);
        final Entry e2 = new Entry(1, 2, 2);
        final Entry e3 = new Entry(1, 3, 3);
        final CollisionNode cn = makeCollisionNode(e1, e2);
        final CollisionNode n = (CollisionNode) assoc(cn, 0, e3);
        assertTrue(n.children.contains(e1));
        assertTrue(n.children.contains(e2));
        assertTrue(n.children.contains(e3));
    }

    @Test
    void assocCollisionNodeReplaceEntry() {
        final Entry e1 = new Entry(1, 1, 1);
        final Entry e2 = new Entry(1, 2, 2);
        final Entry e3 = new Entry(1, 2, 3);
        final CollisionNode cn = makeCollisionNode(e1, e2);
        final CollisionNode n = (CollisionNode) assoc(cn, 0, e3);
        assertTrue(n.children.contains(e1));
        assertFalse(n.children.contains(e2));
        assertTrue(n.children.contains(e3));
    }

    @Test
    void assocCollisionNodeNullKeyValue() {
        final CollisionNode cn = makeCollisionNode(
            new Entry(42, null, null),
            new Entry(42, 1, 1));
        assertTrue(assoc(cn, 0, new Entry(42, null, null)) == cn);
    }

    @Test
    void getEntry_Entry() {
        final Entry e = new Entry(1, 1, 1);
        assertTrue(getEntry(e, 0, 1, 1) == e);
        assertNull(getEntry(e, 0, 1, 2));
        assertNull(getEntry(e, 0, 2, 2));
        assertNull(getEntry(new Entry(42, null, 1), 0, 42, 1));
    }

    @Test
    void getEntry_ArrayNode() {
        final Entry e = new Entry(1, 1, 1);
        final ArrayNode a = makeArrayNode(e, 0);
        assertTrue(getEntry(a, 0, 1, 1) == e);
        assertNull(getEntry(a, 0, 2, 2));
    }

    @Test
    void getEntry_CollisionNode() {
        final Entry e1 = new Entry(1, 1, 1);
        final Entry e2 = new Entry(1, 2, 2);
        final CollisionNode cn = makeCollisionNode(e1, e2);
        assertTrue(getEntry(cn, 0, 1, 1) == e1);
        assertNull(getEntry(cn, 0, 3, 3));
    }

    @Test
    void getEntry_CollisionNode_NullKey() {
        final Entry e = new Entry(42, null, null);
        final CollisionNode cn = makeCollisionNode(new Entry(42, 1, 1), e);
        assertTrue(getEntry(cn, 0, 42, null) == e);
    }

    @Test
    void dissoc_Entry() {
        final Entry e = new Entry(1, 1, 1);
        assertTrue(dissoc(e, 0, 1, 2) == e);
        assertNull(dissoc(e, 0, 1, 1));

        final Entry e2 = new Entry(42, null, null);
        assertTrue(dissoc(e2, 0, 42, 1) == e2);
    }

    @Test
    void dissoc_ArrayNode() {
        final Entry e1 = new Entry(1, 1, 1);
        final ArrayNode a1 = makeArrayNode(e1, 0);
        assertTrue(dissoc(a1, 0, 2, 2) == a1);
        assertTrue(dissoc(a1, 0, 1, 2) == a1);
        assertNull(dissoc(a1, 0, 1, 1));

        final Entry e2 = new Entry(2, 2, 2);
        final ArrayNode a2 = (ArrayNode) assoc(a1, 0, e2);
        final ArrayNode n2 = (ArrayNode) dissoc(a2, 0, 1, 1);
        assertNull(n2.children[1]);
        assertTrue(n2.children[2] == e2);
        assertEquals(1, n2.childrenCount);

        final Entry e3 = new Entry(33, 3, 3);
        final ArrayNode a3 = (ArrayNode) assoc(a1, 0, e3);
        final ArrayNode n3 = (ArrayNode) dissoc(a3, 0, 1, 1);
        assertNull(getEntry(n3, 0, 1, 1));
        assertTrue(getEntry(n3, 0, 33, 3) == e3);
    }

    @Test
    void dissoc_CollisionNode() {
        final Entry e1 = new Entry(1, 1, 1);
        final Entry e2 = new Entry(1, 2, 2);
        final CollisionNode c1 = makeCollisionNode(e1, e2);
        assertTrue(dissoc(c1, 0, 3, 3) == c1);
        assertTrue(dissoc(c1, 0, 1, 3) == c1);
        assertTrue(dissoc(c1, 0, 1, 1) == e2);

        final Entry e3 = new Entry(1, 3, 3);
        final CollisionNode c2 = (CollisionNode) assoc(c1, 0, e3);
        final CollisionNode c3 = (CollisionNode) dissoc(c2, 0, 1, 1);
        assertEquals(2, c3.children.size());
        assertNull(getEntry(c3, 0, 1, 1));
        assertTrue(getEntry(c3, 0, 1, 2) == e2);
        assertTrue(getEntry(c3, 0, 1, 3) == e3);

        final Entry eNull = new Entry(42, null, null);
        final CollisionNode cNull = makeCollisionNode(
            eNull, new Entry(42, 1, 1));
        assertTrue(dissoc(cNull, 0, 42, 1) == eNull);
    }

    @Test
    void differenceIdentical() {
        final Entry e = new Entry(1, 1, 1);
        assertNull(difference(0, e, e));
    }

    @Test
    void differenceNull() {
        final Entry e = new Entry(1, 1, 1);
        assertTrue(difference(0, e, null) == e);
        assertNull(difference(0, null, e));
    }

    @Test
    void differenceEntry() {
        final Entry e1 = new Entry(1, 1, 1);
        assertNull(difference(0, e1, new Entry(1, 1, 1)));
        assertTrue(difference(0, e1, new Entry(1, 1, 2)) == e1);
        assertTrue(difference(0, e1, new Entry(2, 2, 1)) == e1);

        assertNull(difference(0, e1, makeArrayNode(e1, 0)));
        assertTrue(difference(
            0, e1, makeArrayNode(new Entry(2, 2, 2), 0)) == e1);
        assertNull(difference(
            0, e1, makeArrayNode(new Entry(1, 1, 1), 0)));
        assertTrue(difference(
            0, e1, makeArrayNode(new Entry(1, 1, 2), 0)) == e1);
        assertNull(difference(
            0, e1, makeCollisionNode(
                new Entry(1, 1, 1), new Entry(1, 2, 2))));

        assertNull(difference(
            0, new Entry(42, null, null), new Entry(42, null, null)));
    }

    @Test
    void differenceArrayNode() {
        final Entry e1 = new Entry(1, 1, 1);
        final ArrayNode a = makeArrayNode(e1, 0);
        assertTrue(difference(0, a, new Entry(2, 2, 2)) == a);
        assertTrue(difference(0, a, new Entry(1, 1, 2)) == a);
        assertNull(difference(0, a, e1));
        assertNull(difference(0, a, new Entry(1, 1, 1)));

        assertNull(difference(
            0, a, makeArrayNode(new Entry(1, 1, 1), 0)));
        assertTrue(difference(
            0, a, makeArrayNode(new Entry(1, 1, 2), 0)) == a);

        final Entry e2 = new Entry(2, 2, 2);
        final ArrayNode d = (ArrayNode) difference(
            0, assoc(a, 0, e2), a);
        assertNull(getEntry(d, 0, 1, 1));
        assertTrue(getEntry(d, 0, 2, 2) == e2);

        assertTrue(difference(
            0, a, makeCollisionNode(
                new Entry(1, 2, 2), new Entry(1, 3, 3))) == a);
        assertNull(difference(
            0, a, makeCollisionNode(e1, new Entry(1, 2, 2))));
        assertNull(difference(
            0, a, makeCollisionNode(
                new Entry(1, 2, 2), new Entry(1, 1, 1))));
        assertTrue(difference(
            0, a, makeCollisionNode(
                new Entry(1, 1, 2), new Entry(1, 2, 2))) == a);
    }

    @Test
    void differenceCollisionNode() {
        final Entry e1 = new Entry(1, 1, 1);
        final Entry e2 = new Entry(1, 2, 2);
        final CollisionNode c = makeCollisionNode(e1, e2);
        assertTrue(difference(0, c, new Entry(1, 2, 2)) == e1);
        assertNull(difference(0, c, makeCollisionNode(e1, e2)));
        assertTrue(difference(0, c, makeArrayNode(e1, 0)) == e2);
        assertTrue(difference(
            0, c, makeArrayNode(new Entry(1, 2, 3), 0)) == c);

        final CollisionNode d = (CollisionNode) difference(
            0, assoc(c, 0, new Entry(1, 3, 3)),
            makeArrayNode(new Entry(1, 3, 3), 0));
        assertTrue(getEntry(d, 0, 1, 1) == e1);
        assertTrue(getEntry(d, 0, 1, 2) == e2);
        assertNull(getEntry(d, 0, 1, 3));
    }
}
