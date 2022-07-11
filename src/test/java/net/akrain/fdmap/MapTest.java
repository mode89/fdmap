package net.akrain.fdmap;

import static net.akrain.fdmap.Map.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;

public class MapTest {

    CollisionNode makeCollisionNode(final Entry e1, final Entry e2) {
        assertTrue(e1.keyHash == e2.keyHash);
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
}
