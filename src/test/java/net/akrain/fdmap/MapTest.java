package net.akrain.fdmap;

import static net.akrain.fdmap.Map.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.function.ToIntFunction;
import org.junit.jupiter.api.Test;

public class MapTest {

    @Test
    void map() {
        final Map m1 = new Map();
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

    @Test
    void getNotFound() {
        final Map m1 = new Map();
        final Map m2 = m1.assoc(1, 1);
        assertEquals(42, m1.get(1, 42));
        assertEquals(42, m2.get(2, 42));
    }

    @Test
    void mapSeq() {
        final Map m1 = new Map();
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
        final Map m1 = new Map(hasher);
        final Map m2 = m1.assoc(1, 2);
        final Map m3 = m2.assoc(3, 4);
        final Map m4 = m3.dissoc(1);
        assertTrue(m2.keyHasher == hasher);
        assertTrue(m3.keyHasher == hasher);
        assertTrue(m4.keyHasher == hasher);
        assertTrue(m3.difference(m2).keyHasher == hasher);
        assertThrows(UnsupportedOperationException.class,
            () -> m1.difference(new Map()));
    }

    @Test
    void nullHasher() {
        assertThrows(IllegalArgumentException.class, () -> new Map(null));
    }
}
