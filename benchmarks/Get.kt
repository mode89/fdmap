package net.akrain.fdmap.benchmarks

import clojure.lang.IPersistentMap
import clojure.lang.PersistentHashMap
import io.vavr.collection.HashMap
import java.util.concurrent.TimeUnit
import net.akrain.fdmap.kotlin.PHashMap
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
open class Get100000 {

    private var fmap = PHashMap.blank()
    private var cmap: IPersistentMap = PersistentHashMap.EMPTY
    private var vmap = VHashMap.empty<Any?,Any?>()

    @Setup(Level.Trial)
    fun setup() {
        fmap = PHashMap.blank()
        cmap = PersistentHashMap.EMPTY
        vmap = VHashMap.empty<Any?,Any?>()
        for (i in 0..100000) {
            fmap = fmap.assoc(i, i)
            cmap = cmap.assoc(i, i)
            vmap = vmap.put(i, i)
        }
    }

    @Benchmark
    fun fdmap(): Int {
        var result = 0
        for (i in 0..100000)
            result += fmap.get(i) as Int
        return result
    }

    @Benchmark
    fun vavr(): Int {
        var result = 0
        for (i in 0..100000)
            result += vmap.get(i).get() as Int
        return result
    }

    @Benchmark
    fun clojure(): Int {
        var result = 0
        for (i in 0..100000)
            result += cmap.valAt(i) as Int
        return result
    }
}
