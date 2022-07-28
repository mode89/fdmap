package net.akrain.fdmap.kotlin

import clojure.lang.IPersistentMap
import clojure.lang.PersistentHashMap
import io.vavr.collection.HashMap
import java.util.concurrent.TimeUnit
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State

// typealias VHashMap = HashMap<Any?,Any?>

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
open class Dissoc100000 {

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
    fun fdmap(): PHashMap {
        var result = fmap
        for (i in 0..100000)
            result = result.dissoc(i)
        return result
    }

    @Benchmark
    fun vavr(): VHashMap {
        var result = vmap
        for (i in 0..100000)
            result = result.remove(i)
        return result
    }

    @Benchmark
    fun clojure(): IPersistentMap {
        var result: IPersistentMap = cmap
        for (i in 0..100000)
            result = result.without(i)
        return result
    }
}
