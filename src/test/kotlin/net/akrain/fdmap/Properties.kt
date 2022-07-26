package net.akrain.fdmap.kotlin

import kotlin.test.assertEquals
import kotlin.test.assertTrue
import net.jqwik.api.*
import net.jqwik.kotlin.api.*

typealias Arb<T> = Arbitrary<T>
typealias Arbs = Arbitraries

class Properties {

    @Property
    fun build(@ForAll("genOpsAndKeys") opsAndKeys: Tuple) {
        val ops = opsAndKeys.items().get(0) as List<Tuple>
        val keys = opsAndKeys.items().get(1) as Set<Any?>
        val hm = applyOps(ops, HashMap())
        val pm = applyOps(ops, PHashMap.blank())
        assertSimilar(hm, pm, keys)
    }

    @Provide
    private fun genOpsAndKeys(): Arb<Tuple> {
        return genKeys().flatMap {
            knownKeys -> genValues().flatMap {
                knownValues -> genOps(knownKeys, knownValues).flatMap {
                    ops -> Arbs.just(Tuple.of(ops, knownKeys))
                }
            }
        }
    }
}

private fun applyOps(
        ops: List<Tuple>,
        map: HashMap<Any?,Any?>): HashMap<Any?,Any?> {
    val map = HashMap(map)
    for (op in ops) {
        val name = op.items().get(0)
        val key = op.items().get(1)
        val value = op.items().get(2)
        if (name == "assoc") {
            map.put(key, value)
        } else if (name == "dissoc") {
            map.remove(key)
        } else {
            throw UnsupportedOperationException()
        }
    }
    return map
}

private fun applyOps(ops: List<Tuple>, map: PHashMap): PHashMap {
    return ops.fold(map,
        fun(map, op): PHashMap {
            val name = op.items().get(0)
            val key = op.items().get(1)
            val value = op.items().get(2)
            return if (name == "assoc") {
                map.assoc(key, value)
            } else if (name == "dissoc") {
                map.dissoc(key)
            } else {
                throw UnsupportedOperationException()
            }
        })
}

private fun assertSimilar(
        hm: HashMap<Any?,Any?>,
        pm: PHashMap,
        knownKeys: Set<Any?>) {
    val usedKeys = hm.keys
    val unusedKeys = HashSet(knownKeys)
    unusedKeys.removeAll(usedKeys)
    assertEquals(
        usedKeys.map{ k -> hm.get(k) },
        usedKeys.map{ k -> pm.get(k) })
    assertTrue(unusedKeys
        .map{k -> pm.get(k)}
        .all{x -> x == null})
    assertEquals(hm.size, pm.count())
}

@Provide
private fun genOps(
        knownKeys: Set<Any?>,
        knownValues: Set<Any?>): Arb<List<Tuple>> {
    return genOp(knownKeys, knownValues).list()
}

@Provide
private fun genOp(knownKeys: Set<Any?>, knownValues: Set<Any?>): Arb<Tuple> {
    return Arbs.oneOf(
        genAssocOp(knownKeys, knownValues),
        genDissocOp(knownKeys))
}

@Provide
private fun genAssocOp(
        knownKeys: Set<Any?>,
        knownValues: Set<Any?>): Arb<Tuple> {
    return combine(
        Arbs.just("assoc"),
        Arbs.of(knownKeys),
        Arbs.of(knownValues))
        { op, k, v -> Tuple.of(op, k, v) }
}

@Provide
private fun genDissocOp(ks: Set<Any?>): Arb<Tuple> {
    return combine(
        Arbs.just("dissoc"),
        Arbs.of(ks),
        Arbs.just(null))
        { op, k, v -> Tuple.of(op, k, v) }
}

@Provide
private fun genKeys(): Arb<Set<Any?>> {
    val values = genObject().set().ofMinSize(1)
    val hashes = Int.any().set().ofMinSize(2)
    val addNull = frequency(Pair(4, false), Pair(1, true))
    return combine(values, hashes, addNull,
        fun(vs, hs, addNull): Set<Any?> {
            val keys: MutableSet<Any?> = vs
                .map{ v -> Key(v, Arbs.of(hs).sample()) }
                .toMutableSet()
            if (addNull) {
                keys.add(null)
            }
            return keys
        })
}

@Provide
private fun genValues(): Arb<Set<Any?>> {
    return genObject().set().ofMinSize(1)
}

@Provide
private fun genObject(): Arb<Any?> {
    return Arbs.oneOf(
        Arbs.integers(),
        Arbs.doubles(),
        Arbs.strings(),
    ).asGeneric().orNull(0.1)
}

private data class Key(val value: Any?, val hash: Int) {
    override fun hashCode(): Int {
        return hash
    }
}