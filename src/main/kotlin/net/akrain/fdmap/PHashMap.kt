package net.akrain.fdmap.kotlin

typealias KeyHasher = (Any?) -> Int

private val DEFAULT_KEY_HASHER: KeyHasher = { key -> key.hashCode() }
private val BLANK_MAPS = HashMap<KeyHasher,PHashMap>()

class PHashMap private constructor(
    val root: Any?,
    val keyHasher: KeyHasher,
) {

    fun assoc(key: Any?, value: Any?): PHashMap {
        val entry = Entry(keyHasher(key), key, value)
        return if (root != null) {
            val newRoot = assoc(root, 0, entry)
            if (newRoot != root) PHashMap(newRoot, keyHasher) else this
        } else {
            PHashMap(entry, keyHasher)
        }
    }

    fun dissoc(key: Any?): PHashMap {
        return if (root != null) {
            val newRoot = dissoc(root, 0, keyHasher(key), key)
            if (newRoot != root) {
                if (newRoot != null) {
                    PHashMap(newRoot, keyHasher)
                } else {
                    blank(keyHasher)
                }
            } else {
                this
            }
        } else {
            this
        }
    }

    fun get(key: Any?): Any? {
        return get(key, null)
    }

    fun get(key: Any?, notFound: Any?): Any? {
        val entry = entryAt(key)
        return if (entry != null) entry.value else notFound
    }

    fun entryAt(key: Any?): Entry? {
        return if (root != null) {
            getEntry(root, 0, keyHasher(key), key)
        } else {
            null
        }
    }

    fun count(): Int {
        return if (root != null) countEntries(root) else 0
    }

    companion object {
        @JvmStatic
        fun blank(): PHashMap {
            return blank(DEFAULT_KEY_HASHER)
        }

        @JvmStatic
        fun blank(hasher: KeyHasher): PHashMap {
            val cachedMap = BLANK_MAPS.get(hasher)
            return if (cachedMap != null) {
                cachedMap
            } else {
                val newMap = PHashMap(null, hasher)
                BLANK_MAPS.put(hasher, newMap)
                return newMap
            }
        }
    }
}
