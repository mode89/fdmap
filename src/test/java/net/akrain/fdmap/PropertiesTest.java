package net.akrain.fdmap;

import static net.akrain.fdmap.Map.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.jqwik.api.*;

public class PropertiesTest {

    @Property
    boolean build(@ForAll("genOpsAndKeys") Tuple opsAndKeys) {
        final List<Tuple> ops = (List<Tuple>) opsAndKeys.items().get(0);
        final Set<Object> keys = (Set<Object>) opsAndKeys.items().get(1);
        final HashMap<Object,Object> hmap = applyOps(ops, new HashMap<>());
        final Map fdmap = applyOps(ops, blank());
        assertSimilar(fdmap, hmap, keys);
        return true;
    }

    @Property
    boolean mapSeq(@ForAll("genOpsAndKeys") Tuple opsAndKeys) {
        final List<Tuple> ops = (List<Tuple>) opsAndKeys.items().get(0);
        final Set<Object> keys = (Set<Object>) opsAndKeys.items().get(1);
        final HashMap<Object,Object> hmap = applyOps(ops, new HashMap<>());
        final Map fdmap = applyOps(ops, blank());

        final Set<java.util.Map.Entry<Object,Object>> hmapEntries =
            hmap.entrySet();
        final ArrayList<Nodes.Entry> fdmapEntries = new ArrayList<>();
        for (Seq s = fdmap.seq(); s != null; s = s.next()) {
            fdmapEntries.add(s.first());
        }

        final boolean equalEntryCounts =
            hmapEntries.size() == fdmapEntries.size();

        final boolean equalEntrySets = hmapEntries.stream()
            .filter(he -> fdmapEntries.stream()
                .filter(fde -> Objects.equals(he.getKey(), fde.key)
                    && Objects.equals(he.getValue(), fde.value))
                .count() == 1)
            .count() == hmapEntries.size();

        return equalEntryCounts && equalEntrySets;
    }

    @Property(tries = 10000)
    boolean difference(@ForAll("genDifferenceSamples") Tuple sample) {

        final Set<Object> keys = (Set<Object>) sample.items().get(0);
        final List<Tuple> buildOps = (List<Tuple>) sample.items().get(1);
        final List<Tuple> ops = (List<Tuple>) sample.items().get(2);

        final HashMap<Object,Object> hmap1 =
            applyOps(buildOps, new HashMap<>());
        final HashMap<Object,Object> hmap2 = applyOps(ops, hmap1);

        final Map fdmap1 = applyOps(buildOps, blank());
        final Map fdmap2 = applyOps(ops, fdmap1);

        assertSimilar(
            fdmap1.difference(fdmap2),
            hashMapDifference(hmap1, hmap2),
            keys);
        assertSimilar(
            fdmap2.difference(fdmap1),
            hashMapDifference(hmap2, hmap1),
            keys);

        return true;
    }

    private static HashMap<Object,Object> applyOps(
            List<Tuple> ops, HashMap<Object,Object> map) {
        map = new HashMap<>(map);
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

    private static Map applyOps(List<Tuple> ops, Map map) {
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

    private static void assertSimilar(
            final Map fdmap,
            final HashMap<Object,Object> hmap,
            final Set<Object> knownKeys) {
        final Set<Object> usedKeys = hmap.keySet();
        final Set<Object> unusedKeys = new HashSet<>(knownKeys);
        unusedKeys.removeAll(usedKeys);
        assertArrayEquals(
            usedKeys.stream().map(k -> hmap.get(k)).toArray(),
            usedKeys.stream().map(k -> fdmap.get(k)).toArray());
        assertTrue(unusedKeys.stream()
            .map(k -> fdmap.get(k))
            .allMatch(x -> x == null));
        assertEquals(hmap.size(), fdmap.count());
    }

    private static HashMap<Object,Object> hashMapDifference(
            HashMap<Object,Object> leftMap,
            HashMap<Object,Object> rightMap) {
        final HashMap<Object,Object> result = new HashMap<>();
        for (java.util.Map.Entry<Object,Object> entry: leftMap.entrySet()) {
            final Object key = entry.getKey();
            final Object value = entry.getValue();
            final Object rightValue = rightMap.get(key);
            if (!rightMap.containsKey(key)
                || !Objects.equals(rightMap.get(key), value)) {
                result.put(key, value);
            }
        }
        return result;
    }

    @Provide
    Arbitrary<Tuple> genDifferenceSamples() {
        return genKeys()
            .flatMap(keys ->
                Combinators.combine(genBuildOps(keys), genOps(keys)).as(
                    (buildOps, ops) -> Tuple.of(keys, buildOps, ops)));
    }

    @Provide
    Arbitrary<List<Tuple>> genBuildOps(Set<Object> allKeys) {

        // Arbitraries.subsetOf() can't handle nulls
        allKeys = new HashSet<>(allKeys);
        allKeys.remove(null);

        Arbitrary<Object> obj = genObject();
        return Arbitraries.subsetOf(allKeys)
            .map(keys -> keys.stream()
                .map(k -> Tuple.of("assoc", k, obj.sample()))
                .collect(Collectors.toList()));
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
        return Combinators.combine(values, hashes)
            .as((vs, hs) -> vs.stream()
                .map(v -> (Object) new Key(v, Arbitraries.of(hs).sample()))
                .collect(Collectors.toSet()))
            .flatMap(keys -> Arbitraries.of(true, false, false, false, false)
                .flatMap(new Function<Boolean,Arbitrary<Set<Object>>>() {
                    @Override
                    public Arbitrary<Set<Object>> apply(Boolean addNull) {
                        if (addNull) {
                            keys.add(null);
                        }
                        return Arbitraries.just(keys);
                    }
                }));
    }

    @Provide
    Arbitrary<Object> genObject() {
        return Arbitraries.oneOf(
            Arbitraries.just(null),
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
