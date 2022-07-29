package net.akrain.fdmap.benchmarks

import clojure.lang.IPersistentMap
import clojure.lang.PersistentHashMap
import io.vavr.collection.HashMap
import java.util.concurrent.TimeUnit
import java.util.UUID
import net.akrain.fdmap.kotlin.PHashMap
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Warmup

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
open class RandomAccess {

    data class Command(
        val name: String,
        val key: Any,
        val value: Any?)

    private var commands: List<Command> = listOf()

    // @Setup(Level.Trial)
    @Setup(Level.Invocation)
    fun setup() {
        val objects = (0..10000)
        // val objects = generateSequence{ UUID.randomUUID() }
        //     .take(10000)
        //     .toList()
        val commandNames = listOf("assoc", "dissoc", "get")
        commands = generateSequence(fun(): Command {
            val name = commandNames.random()
            return when (name) {
                "assoc" -> Command(name, objects.random(), objects.random())
                "dissoc" -> Command(name, objects.random(), null)
                "get" -> Command(name, objects.random(), null)
                else -> throw RuntimeException()
            }
        }).take(100000).toList()
    }

    @Benchmark
    fun fdmap(): Pair<PHashMap,Any?> {
        return commands.fold(Pair(PHashMap.blank(), null as Any?)) {
            (m, _), cmd -> when(cmd.name) {
                "assoc" -> Pair(m.assoc(cmd.key, cmd.value), null)
                "dissoc" -> Pair(m.dissoc(cmd.key), null)
                "get" -> Pair(m, m.get(cmd.key))
                else -> throw RuntimeException()
            }
        }
    }

    @Benchmark
    fun vavr(): Pair<VHashMap,Any?> {
        return commands.fold(Pair(VHashMap.empty<Any,Any>(), null as Any?)) {
            (m, _), cmd -> when(cmd.name) {
                "assoc" -> Pair(m.put(cmd.key, cmd.value), null)
                "dissoc" -> Pair(m.remove(cmd.key), null)
                "get" -> Pair(m, m.get(cmd.key))
                else -> throw RuntimeException()
            }
        }
    }

    @Benchmark
    fun clojure(): Pair<IPersistentMap,Any?> {
        return commands.fold(
            Pair(PersistentHashMap.EMPTY as IPersistentMap, null as Any?)) {
            (m, _), cmd -> when(cmd.name) {
                "assoc" -> Pair(m.assoc(cmd.key, cmd.value), null)
                "dissoc" -> Pair(m.without(cmd.key), null)
                "get" -> Pair(m, m.valAt(cmd.key))
                else -> throw RuntimeException()
            }
        }
    }
}
