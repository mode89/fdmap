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
import org.openjdk.jmh.annotations.Warmup

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
open class Equiv10 {

    val range = (0..100000)
    private var fmapL = PHashMap.blank()
    private var cmapL: IPersistentMap = PersistentHashMap.EMPTY
    private var vmapL = VHashMap.empty<Any?,Any?>()
    private var fmapR = PHashMap.blank()
    private var cmapR: IPersistentMap = PersistentHashMap.EMPTY
    private var vmapR = VHashMap.empty<Any?,Any?>()

    @Setup(Level.Trial)
    fun setup() {
        fmapL = PHashMap.blank()
        cmapL = PersistentHashMap.EMPTY
        vmapL = VHashMap.empty<Any?,Any?>()
        for (i in range) {
            fmapL = fmapL.assoc(i, i)
            cmapL = cmapL.assoc(i, i)
            vmapL = vmapL.put(i, i)
        }
    }

    @Setup(Level.Invocation)
    fun setupRightMap() {
        fmapR = fmapL
        cmapR = cmapL
        vmapR = vmapL
        for (i in 1..10) {
            val n = range.random()
            fmapR = fmapR.assoc(n, n + 1)
            cmapR = cmapR.assoc(n, n + 1)
            vmapR = vmapR.put(n, n + 1)
        }
    }

    @Benchmark
    fun fdmap(): Boolean {
        return fmapL.equiv(fmapR)
    }

    @Benchmark
    fun vavr(): Boolean {
        return vmapL.equals(vmapR)
    }

    @Benchmark
    fun clojure(): Boolean {
        return cmapL.equals(cmapR)
    }
}

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 10)
open class Equiv10000 {

    val range = (0..100000)
    private var fmapL = PHashMap.blank()
    private var cmapL: IPersistentMap = PersistentHashMap.EMPTY
    private var vmapL = VHashMap.empty<Any?,Any?>()
    private var fmapR = PHashMap.blank()
    private var cmapR: IPersistentMap = PersistentHashMap.EMPTY
    private var vmapR = VHashMap.empty<Any?,Any?>()

    @Setup(Level.Trial)
    fun setup() {
        fmapL = PHashMap.blank()
        cmapL = PersistentHashMap.EMPTY
        vmapL = VHashMap.empty<Any?,Any?>()
        for (i in range) {
            fmapL = fmapL.assoc(i, i)
            cmapL = cmapL.assoc(i, i)
            vmapL = vmapL.put(i, i)
        }
    }

    @Setup(Level.Invocation)
    fun setupRightMap() {
        fmapR = fmapL
        cmapR = cmapL
        vmapR = vmapL
        for (i in 1..10000) {
            val n = range.random()
            fmapR = fmapR.assoc(n, n + 1)
            cmapR = cmapR.assoc(n, n + 1)
            vmapR = vmapR.put(n, n + 1)
        }
    }

    @Benchmark
    fun fdmap(): Boolean {
        return fmapL.equiv(fmapR)
    }

    @Benchmark
    fun vavr(): Boolean {
        return vmapL.equals(vmapR)
    }

    @Benchmark
    fun clojure(): Boolean {
        return cmapL.equals(cmapR)
    }
}
