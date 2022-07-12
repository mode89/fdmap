package net.akrain.fdmap;

import static net.akrain.fdmap.Map.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.jqwik.api.*;

public class PropertiesTest {

    @Property
    boolean sameAsBuiltinMap(@ForAll("genOpsAndKeys") Tuple opsAndKeys) {
        final List<Tuple> ops = (List<Tuple>) opsAndKeys.items().get(0);
        final Set<Object> keys = (Set<Object>) opsAndKeys.items().get(1);
        final HashMap<Object,Object> bmap = makeBuiltinMap(ops);
        final Map fdmap = makeFDMap(ops);
        final Set<Object> usedKeys = bmap.keySet();
        final Set<Object> unusedKeys = new HashSet<>(keys);
        unusedKeys.removeAll(usedKeys);
        assertArrayEquals(
            usedKeys.stream().map(k -> bmap.get(k)).toArray(),
            usedKeys.stream().map(k -> fdmap.get(k)).toArray());
        assertTrue(unusedKeys.stream()
            .map(k -> fdmap.get(k))
            .allMatch(x -> x == null));
        return true;
    }

    private static HashMap<Object,Object> makeBuiltinMap(List<Tuple> ops) {
        HashMap<Object,Object> map = new HashMap<>();
        for (Tuple op: ops) {
            final String name = (String) op.items().get(0);
            final Object key = op.items().get(1);
            final Object value = op.items().get(2);
            if (name == "assoc") {
                map.put(key, value);
            } else if (name == "dissoc") {
                map.remove(key);
            }
        }
        return map;
    }

    private static Map makeFDMap(List<Tuple> ops) {
        Map map = new Map(null);
        for (Tuple op: ops) {
            final String name = (String) op.items().get(0);
            final Object key = op.items().get(1);
            final Object value = op.items().get(2);
            if (name == "assoc") {
                map = map.assoc(key, value);
            } else if (name == "dissoc") {
                map = map.dissoc(key);
            }
        }
        return map;
    }

    @Provide
    Arbitrary<Tuple> genOpsAndKeys() {
        return genKeys()
            .flatMap(keys -> genOps(keys)
                .flatMap(ops -> Arbitraries.just(Tuple.of(ops, keys))));
    }

    @Provide
    Arbitrary<List<Tuple>> genOps(Set<Object> ks) {
        return genOp(ks).list();
    }

    @Provide
    Arbitrary<Tuple> genOp(Set<Object> ks) {
        return Arbitraries.oneOf(genAssocOp(ks), genDissocOp(ks));
    }

    @Provide
    Arbitrary<Tuple> genAssocOp(Set<Object> ks) {
        return Combinators.combine(
                Arbitraries.just("assoc"),
                Arbitraries.of(ks),
                genValue())
            .as((op, k, v) -> Tuple.of(op, k, v));
    }

    @Provide
    Arbitrary<Tuple> genDissocOp(Set<Object> ks) {
        return Combinators.combine(
                Arbitraries.just("dissoc"),
                Arbitraries.of(ks),
                Arbitraries.just(null))
            .as((op, k, v) -> Tuple.of(op, k, v));
    }

    @Provide
    Arbitrary<Object> genValue() {
        return genObject();
    }

    @Provide
    Arbitrary<Set<Object>> genKeys() {
        Arbitrary<Set<Object>> values = genObject().set().ofMinSize(1);
        Arbitrary<Set<Integer>> hashes =
            Arbitraries.integers().set().ofMinSize(2);
        return Combinators.combine(values, hashes).as(
            (vs, hs) -> vs.stream()
                .map(v -> new Key(v, Arbitraries.of(hs).sample()))
                .collect(Collectors.toSet()));
    }

    @Provide
    Arbitrary<Object> genObject() {
        return Arbitraries.oneOf(
            Arbitraries.integers(),
            Arbitraries.strings(),
            Arbitraries.doubles());
    }

    private static class Key {
        public Object value;
        public int hash;

        public Key(Object value, int hash) {
            this.value = value;
            this.hash = hash;
        }

        @Override
        public int hashCode() {
            return this.hash;
        }

        @Override
        public String toString() {
            return String.format("Key@{v:'%s',h:%d}", value, hash);
        }
    }
}
