package net.akrain.fdmap.kotlin

import clojure.lang.IPersistentMap
import clojure.lang.PersistentHashMap
import io.vavr.collection.HashMap
import java.util.concurrent.TimeUnit
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State

typealias VHashMap = HashMap<Any?,Any?>

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
open class Assoc100000 {

    @Benchmark
    fun fdmap(): PHashMap {
        var result = PHashMap.blank()
        for (i in 0..100000)
            result = result.assoc(i, i)
        return result
    }

    @Benchmark
    fun vavr(): VHashMap {
        var result = VHashMap.empty<Any?,Any?>()
        for (i in 0..100000)
            result = result.put(i, i)
        return result
    }

    @Benchmark
    fun clojure(): IPersistentMap {
        var result: IPersistentMap = PersistentHashMap.EMPTY
        for (i in 0..100000)
            result = result.assoc(i, i)
        return result
    }
}
