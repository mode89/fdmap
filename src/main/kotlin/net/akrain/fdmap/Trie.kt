package net.akrain.fdmap.kotlin

data class Entry(
    val keyHash: Int,
    val key: Any,
    val value: Any,
)

data class ArrayNode(
    val children: Array<Any?>,
    val childrenCount: Int,
    val entryCount: Int,
)

data class CollisionNode(
    val children: ArrayList<Entry>,
    val keyHash: Int,
)

fun countEntries(node: Any): Int {
    return when (node) {
        is ArrayNode -> node.entryCount
        is Entry -> 1
        is CollisionNode -> node.children.size
        else -> throw UnsupportedOperationException()
    }
}

fun assoc(node: Any, shift: Int, entry: Entry): Any {
    return when (node) {
        is ArrayNode -> assocArrayNode(node, shift, entry)
        is Entry -> assocEntry(node, shift, entry)
        is CollisionNode -> assocCollisionNode(node, shift, entry)
        else -> throw UnsupportedOperationException()
    }
}

private fun assocArrayNode(
        node: ArrayNode,
        shift: Int,
        entry: Entry): Any {
    val childIndex = arrayIndex(shift, entry.keyHash)
    val child = node.children[childIndex]
    if (child == null) {
        return ArrayNode(
            node.children.immSet(childIndex, entry),
            node.childrenCount + 1,
            node.entryCount + 1)
    } else {
        val newChild = assoc(child, shift + 5, entry)
        if (child == newChild) {
            return node
        } else {
            return ArrayNode(
                node.children.immSet(childIndex, newChild),
                node.childrenCount,
                node.entryCount
                    + countEntries(newChild)
                    - countEntries(child))
        }
    }
}

private fun assocEntry(node: Entry, shift: Int, entry: Entry): Any {
    return if (node.key == entry.key) {
        if (node.value == entry.value) node else entry
    } else {
        if (node.keyHash == entry.keyHash) {
            CollisionNode(arrayListOf(node, entry), node.keyHash)
        } else {
            assoc(makeArrayNode(node, shift), shift, entry)
        }
    }
}

private fun assocCollisionNode(
        node: CollisionNode,
        shift: Int,
        entry: Entry): Any {
    return if (node.keyHash == entry.keyHash) {
        val childIndex = node.children
            .indexOfFirst { it.key == entry.key }
        if (childIndex == -1) {
            CollisionNode(node.children.immAdd(entry), node.keyHash)
        } else {
            val child = node.children[childIndex]
            if (child.value == entry.value) {
                node
            } else {
                CollisionNode(
                    node.children.immSet(childIndex, entry),
                    node.keyHash)
            }
        }
    } else {
        assoc(makeArrayNode(node, shift), shift, entry)
    }
}

internal fun makeArrayNode(node: Any, shift: Int): ArrayNode {
    val children = arrayOfNulls<Any>(32)
    val index = arrayIndex(shift, getKeyHash(node))
    children[index] = node
    return ArrayNode(children, 1, countEntries(node))
}

private fun arrayIndex(shift: Int, keyHash: Int): Int {
    return (keyHash ushr shift) and 0x1F
}

private fun getKeyHash(node: Any): Int {
    return when (node) {
        is Entry -> node.keyHash
        is CollisionNode -> node.keyHash
        else -> throw UnsupportedOperationException()
    }
}

private fun <T> Array<T>.immSet(i: Int, x: T): Array<T> {
    val arr = this.clone()
    arr[i] = x
    return arr
}

private fun <T> ArrayList<T>.immAdd(x: T): ArrayList<T> {
    val arr = ArrayList(this)
    arr.add(x)
    return arr
}

private fun <T> ArrayList<T>.immSet(i: Int, x: T): ArrayList<T> {
    val arr = ArrayList(this)
    arr.set(i, x)
    return arr
}
