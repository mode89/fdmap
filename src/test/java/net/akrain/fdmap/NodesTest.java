package net.akrain.fdmap;

import static net.akrain.fdmap.Nodes.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Objects;
import org.junit.jupiter.api.Test;

public class NodesTest {

    static ArrayNode makeArrayNode2(
            final Entry e1,
            final Entry e2,
            final int shift) {
        return (ArrayNode) assoc(makeArrayNode(e1, 0), 0, e2);
    }

    static CollisionNode makeCollisionNode(final Entry e1, final Entry e2) {
        assertTrue(e1.keyHash == e2.keyHash);
        assertFalse(Objects.equals(e1.key, e2.key));
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

        final Entry e2 = new Entry(2, 2, 2);
        final ArrayNode a2 = (ArrayNode) assoc(a1, 0, e2);
        assertTrue(dissoc(a2, 0, 1, 1) == e2);

        final Entry e3 = new Entry(33, 3, 3);
        final ArrayNode a3 = (ArrayNode) assoc(a1, 0, e3);
        assertTrue(dissoc(a3, 0, 1, 1) == e3);

        final Entry e4 = new Entry(65, 4, 4);
        final ArrayNode a4 = (ArrayNode) assoc(a3, 0, e4);
        assertNull(getEntry(dissoc(a4, 0, 1, 1), 0, 1, 1));

        final ArrayNode a5 = (ArrayNode) assoc(a3, 0, e2);
        assertNull(getEntry(dissoc(a5, 0, 2, 2), 0, 2, 2));
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
    void seqEntry() {
        final Entry e = new Entry(1, 1, 1);
        final Seq s = seq(e, "root");
        assertEquals("root", s.root);
        assertTrue(s.entry == e);
        assertEquals(0, s.entryIndex);
    }

    @Test
    void seqArrayNode() {
        final Entry e1 = new Entry(1, 1, 1);
        final Entry e2 = new Entry(2, 2, 2);
        final ArrayNode a = (ArrayNode) assoc(makeArrayNode(e1, 0), 0, e2);
        final Seq s = seq(a, "root");
        assertEquals("root", s.root);
        assertTrue(s.entry == e1);
        assertEquals(0, s.entryIndex);
    }

    @Test
    void seqCollisionNode() {
        final Entry e1 = new Entry(1, 1, 1);
        final Entry e2 = new Entry(1, 2, 2);
        final CollisionNode c = makeCollisionNode(e1, e2);
        final Seq s = seq(c, "root");
        assertEquals("root", s.root);
        assertTrue(s.entry == e1);
        assertEquals(0, s.entryIndex);
    }

    @Test
    void nextEntry() {
        final Entry e = new Entry(1, 1, 1);
        final Seq s = next(e, "root", 0, 1, 0);
        assertNull(s);
    }

    @Test
    void nextArrayNode() {
        final Entry e1 = new Entry(1, 1, 1);
        final ArrayNode a1 = makeArrayNode(e1, 0);
        final Seq s1 = next(a1, "root", 0, 1, 0);
        assertNull(s1);

        final Entry e2 = new Entry(3, 3, 3);
        final ArrayNode a2 = (ArrayNode) assoc(a1, 0, e2);
        final Seq s2 = next(a2, "root", 0, 1, 0);
        assertEquals("root", s2.root);
        assertTrue(s2.entry == e2);
        assertEquals(0, s2.entryIndex);

        final Entry e3 = new Entry(33, 5, 5);
        final ArrayNode a3 = (ArrayNode) assoc(a1, 0, e3);
        final Seq s3 = next(a3, "root", 0, 1, 0);
        assertEquals("root", s3.root);
        assertTrue(s3.entry == e3);
        assertEquals(0, s3.entryIndex);
    }

    @Test
    void nextCollisionNode() {
        final Entry e1 = new Entry(1, 1, 1);
        final Entry e2 = new Entry(1, 2, 2);
        final CollisionNode c = makeCollisionNode(e1, e2);

        final Seq s1 = next(c, "root", 0, 1, 0);
        assertEquals("root", s1.root);
        assertTrue(s1.entry == e2);
        assertEquals(1, s1.entryIndex);

        final Seq s2 = next(c, "root", 0, 1, 1);
        assertNull(s2);
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
        final Entry e2 = new Entry(2, 2, 2);
        final ArrayNode a = makeArrayNode2(e1, e2, 0);
        assertTrue(difference(0, a, new Entry(3, 3, 3)) == a);
        assertTrue(difference(0, a, new Entry(1, 1, 3)) == a);
        assertTrue(difference(0, a, e1) == e2);
        assertTrue(difference(0, a, new Entry(1, 1, 1)) == e2);

        assertNull(difference(0, a,
            makeArrayNode2(new Entry(1, 1, 1), new Entry(2, 2, 2), 0)));
        assertTrue(difference(0, a,
            makeArrayNode2(new Entry(1, 1, 2), new Entry(2, 2, 3), 0)) == a);

        final Entry e3 = new Entry(3, 3, 3);
        assertTrue(difference(0, assoc(a, 0, e3), a) == e3);
        final Entry e4 = new Entry(4, 4, 4);
        final ArrayNode d = (ArrayNode) difference(
            0, assoc(assoc(a, 0, e3), 0, e4), a);
        assertNull(getEntry(d, 0, 1, 1));
        assertNull(getEntry(d, 0, 2, 2));
        assertTrue(getEntry(d, 0, 3, 3) == e3);
        assertTrue(getEntry(d, 0, 4, 4) == e4);

        assertTrue(difference(
            0, a, makeCollisionNode(
                new Entry(1, 2, 2), new Entry(1, 3, 3))) == a);
        assertTrue(difference(
            0, a, makeCollisionNode(e1, new Entry(1, 2, 2))) == e2);
        assertTrue(difference(
            0, a, makeCollisionNode(
                new Entry(1, 2, 2), new Entry(1, 1, 1))) == e2);
        assertTrue(difference(
            0, a, makeCollisionNode(
                new Entry(1, 1, 2), new Entry(1, 2, 2))) == a);
    }

    @Test
    void difference_ArrayNode_ArrayNode_singleChild_returnArrayNode() {
        final Entry e1 = new Entry(1, 1, 1);
        final Entry e2 = new Entry(2, 2, 2);
        final Entry e3 = new Entry(33, 3, 3);
        final ArrayNode a1 = (ArrayNode) assoc(
            makeArrayNode2(e1, e2, 0), 0, e3);
        final ArrayNode a2 = makeArrayNode2(e2, new Entry(33, 3, 4), 0);
        final ArrayNode d = (ArrayNode) difference(0, a1, a2);
        assertTrue(getEntry(d, 0, 1, 1) == e1);
        assertNull(getEntry(d, 0, 2, 2));
        assertTrue(getEntry(d, 0, 33, 3) == e3);
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

    @Test
    void countEntriesTest() {
        final Entry e = new Entry(1, 1, 1);
        final ArrayNode a = makeArrayNode(e, 0);
        final CollisionNode c = makeCollisionNode(e, new Entry(1, 2, 2));
        assertEquals(1, countEntries(e));
        assertEquals(1, countEntries(a));
        assertEquals(2, countEntries(c));
    }

    @Test
    void entryHash() {
        final Entry e = new Entry(1, 1, 1);
        assertTrue(e.hashCode() == e.hashCode());
        assertTrue(e.hashCode() == new Entry(1, 1, 1).hashCode());
        assertTrue(e.hashCode() != new Entry(2, 2, 2).hashCode());
    }

    @Test
    void entryEquality() {
        assertTrue(Objects.equals(new Entry(1, 1, 1), new Entry(1, 1, 1)));
        assertFalse(Objects.equals(new Entry(1, 1, 1), new Entry(1, 1, 2)));
        assertFalse(Objects.equals(new Entry(1, 1, 1), new Entry(2, 2, 1)));
        assertFalse(Objects.equals(new Entry(1, 1, 1), new Entry(2, 2, 2)));
        assertFalse(Objects.equals(
            new Entry(1, 1, 1), java.util.Map.entry(1, 2)));
        assertTrue(Objects.equals(
            new Entry(1, 1, 2), java.util.Map.entry(1, 2)));
        assertFalse(Objects.equals(new Entry(1, 1, 1), 1));
    }

    @Test
    void equivIdentical() {
        final Entry e = new Entry(1, 1, 1);
        assertTrue(equiv(0, e, e));
    }

    @Test
    void equivNull() {
        final Entry e = new Entry(1, 1, 1);
        assertTrue(equiv(0, null, null));
        assertFalse(equiv(0, e, null));
        assertFalse(equiv(0, null, e));
    }

    @Test
    void equivDifferentCount() {
        final Entry e = new Entry(1, 1, 1);
        final ArrayNode a = makeArrayNode2(e, new Entry(2, 2, 2), 0);
        assertFalse(equiv(0, a, e));
    }

    @Test
    void equivEntry() {
        final Entry e = new Entry(1, 1, 1);
        assertTrue(equiv(0, e, new Entry(1, 1, 1)));
        assertFalse(equiv(0, e, new Entry(1, 2, 1)));
        assertFalse(equiv(0, e, new Entry(1, 1, 2)));
        assertFalse(equiv(0, e, new Entry(1, 2, 2)));
    }

    @Test
    void equivArrayNode() {
        assertFalse(equiv(0,
            makeArrayNode2(new Entry(1, 1, 1), new Entry(2, 2, 2), 0),
            makeArrayNode2(new Entry(1, 1, 1), new Entry(2, 2, 3), 0)));
        assertTrue(equiv(0,
            makeArrayNode2(new Entry(1, 1, 1), new Entry(2, 2, 2), 0),
            makeArrayNode2(new Entry(1, 1, 1), new Entry(2, 2, 2), 0)));
        assertFalse(equiv(0,
            makeArrayNode2(new Entry(1, 1, 1), new Entry(2, 2, 2), 0),
            makeCollisionNode(new Entry(1, 1, 1), new Entry(1, 3, 3))));
    }

    @Test
    void equivCollisionNode() {
        assertTrue(equiv(0,
            makeCollisionNode(new Entry(1, 1, 1), new Entry(1, 2, 2)),
            makeCollisionNode(new Entry(1, 2, 2), new Entry(1, 1, 1))));
        assertFalse(equiv(0,
            makeCollisionNode(new Entry(1, 1, 1), new Entry(1, 2, 2)),
            makeCollisionNode(new Entry(1, 1, 1), new Entry(1, 2, 1))));
        assertFalse(equiv(0,
            makeCollisionNode(new Entry(1, 1, 1), new Entry(1, 2, 2)),
            makeCollisionNode(new Entry(1, 1, 1), new Entry(1, 3, 3))));
        assertFalse(equiv(0,
            makeCollisionNode(new Entry(1, 1, 1), new Entry(1, 2, 2)),
            makeArrayNode2(new Entry(1, 1, 1), new Entry(33, 3, 3), 0)));
    }
}
