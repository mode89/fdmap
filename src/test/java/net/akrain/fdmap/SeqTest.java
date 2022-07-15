package net.akrain.fdmap;

import static net.akrain.fdmap.Nodes.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class SeqTest {

    @Test
    void seqFirstNext() {
        final Entry e1 = new Entry(1, 1, 1);
        final Entry e2 = new Entry(1, 2, 2);
        final CollisionNode c = NodesTest.makeCollisionNode(e1, e2);
        final Seq s = new Seq(c, e1, 0);
        assertTrue(s.first() == e1);
        final Seq sNext = s.next();
        assertTrue(sNext.root == c);
        assertTrue(sNext.entry == e2);
        assertEquals(1, sNext.entryIndex);
    }
}
