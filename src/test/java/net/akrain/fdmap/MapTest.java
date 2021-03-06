package net.akrain.fdmap;

import static net.akrain.fdmap.Map.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.function.ToIntFunction;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

public class MapTest {

    @Test
    void map() {
        final Map m1 = blank();
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
        assertTrue(m7 == blank());
        assertTrue(m8 == m3);
        assertNull(m9.get(1));
        assertEquals("2", m9.get(2));
    }

    @Test
    void getNotFound() {
        final Map m1 = blank();
        final Map m2 = m1.assoc(1, 1);
        assertEquals(42, m1.get(1, 42));
        assertEquals(42, m2.get(2, 42));
    }

    @Test
    void mapSeq() {
        final Map m1 = blank();
        final Seq s1 = m1.seq();
        assertNull(s1);

        final Map m2 = m1.assoc(1, 2);
        final Seq s2 = m2.seq();
        assertTrue(s2.root == m2.root);
        assertEquals(2, s2.first().value);
        assertNull(s2.next());
    }

    @Test
    void keepKeyHasher() {
        final ToIntFunction<Object> hasher = (x) -> (Integer) x;
        final Map m1 = blank(hasher);
        final Map m2 = m1.assoc(1, 2);
        final Map m3 = m2.assoc(3, 4);
        final Map m4 = m3.dissoc(1);
        final Map m5 = m4.dissoc(3);
        assertTrue(m2.keyHasher == hasher);
        assertTrue(m3.keyHasher == hasher);
        assertTrue(m4.keyHasher == hasher);
        assertTrue(m3.difference(m2).keyHasher == hasher);
        assertTrue(m5 == blank(hasher));
        assertThrows(UnsupportedOperationException.class,
            () -> m1.difference(blank()));
    }

    @Test
    void nullKey() {
        assertEquals(42, blank().assoc(null, 42).get(null));
    }

    @Test
    void nullHasher() {
        assertThrows(IllegalArgumentException.class, () -> blank(null));
    }

    @Test
    void nullDifference() {
        final Map m1 = blank().assoc(1, 1);
        final Map m2 = blank().assoc(1, 1);
        assertTrue(m1.difference(m2) == blank());
    }

    @Test
    void blankIdentity() {
        final ToIntFunction<Object> hasher = (x) -> (Integer) x;
        assertTrue(blank() == blank());
        assertTrue(blank(hasher) == blank(hasher));
        assertTrue(blank() != blank(hasher));
    }

    @Test
    void mapValAt() {
        assertEquals(42, blank().valAt(1, 42));
        assertEquals(42, blank().assoc(1, 42).valAt(1));
    }

    @Test
    void implAssociative() {
        final Map m = blank().assoc(1, 42);
        assertNull(m.entryAt(42));
        assertEquals(1, m.entryAt(1).getKey());
        assertEquals(42, m.entryAt(1).getValue());
        assertTrue(m.containsKey(1));
        assertFalse(m.containsKey(42));
    }

    @Test
    void implIPersistentMap() {
        final Map m = blank().assoc(1, 2).assoc(3, 4);
        assertEquals(4, m.without(1).get(3));
        assertEquals(6, m.assocEx(5, 6).get(5));
        assertThrows(RuntimeException.class, () -> m.assocEx(3, 5));
    }

    @Test
    void getEmptyMap() {
        final ToIntFunction<Object> hasher = (x) -> (Integer) x;
        final Map m = blank(hasher).assoc(1, 2);
        assertTrue(m.empty() == blank(hasher));
    }

    @Test
    void countEntries() {
        assertEquals(0, blank().count());
        assertEquals(1, blank().assoc(null, null).count());
    }

    @Test
    void iterable() {
        final Iterator<Nodes.Entry> it = blank().assoc(1, 2).iterator();
        assertTrue(it.hasNext());
        assertEquals(new Nodes.Entry(1, 1, 2), it.next());
        assertFalse(it.hasNext());
        assertThrows(NoSuchElementException.class, () -> it.next());
    }

    @Test
    void equivalence() {
        assertTrue(blank().assoc(1, 2).equiv(blank().assoc(1, 2)));
        assertTrue(blank().assoc(1, 2).equiv(java.util.Map.of(1, 2)));
    }

    @Test
    void intersectionWrongHasher() {
        assertThrows(UnsupportedOperationException.class,
            () -> blank().intersection(blank(x -> (Integer) x)));
    }

    @Test
    void intersectionSameAsLeft() {
        final Map m1 = blank().assoc(1, 1);
        final Map m2 = m1.assoc(2, 2);
        assertTrue(m1.intersection(m2) == m1);
    }

    @Test
    void intersectionSameAsRight() {
        final Map m1 = blank().assoc(1, 1);
        final Map m2 = m1.assoc(2, 2);
        assertTrue(m2.intersection(m1) == m1);
    }

    @Test
    void intersectionEmpty() {
        final ToIntFunction<Object> hasher = (x) -> (Integer) x;
        assertTrue(
            blank(hasher).assoc(1, 1)
                .intersection(blank(hasher).assoc(2, 2))
                == blank(hasher));
    }

    @Test
    void intsersectionNewMap() {
        final ToIntFunction<Object> hasher = (x) -> (Integer) x;
        final Map m0 = blank(hasher).assoc(1, 1);
        final Map mi = m0.assoc(2, 2).intersection(m0.assoc(3, 3));
        assertEquals(m0, mi);
        assertTrue(mi.keyHasher == hasher);
    }
}
