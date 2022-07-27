package net.akrain.fdmap.kotlin

data class Entry(
    val keyHash: Int,
    val key: Any?,
    val value: Any?,
)

data class ArrayNode(
    val children: Array<Any?>,
    val childrenCount: Int,
    val entryCount: Int,
)

data class CollisionNode(
    val children: List<Entry>,
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

fun getEntry(node: Any, shift: Int, keyHash: Int, key: Any?): Entry? {
    return when (node) {
        is ArrayNode -> {
            val childIndex = arrayIndex(shift, keyHash)
            val child = node.children[childIndex]
            if (child != null)
                getEntry(child, shift + 5, keyHash, key)
            else null
        }
        is Entry ->
            if (node.keyHash == keyHash && node.key == key)
                node
            else null
        is CollisionNode ->
            if (node.keyHash == keyHash)
                node.children.firstOrNull { it.key == key }
            else null
        else -> throw UnsupportedOperationException()
    }
}

fun dissoc(node: Any, shift: Int, keyHash: Int, key: Any?): Any? {
    return when (node) {
        is ArrayNode -> dissocArrayNode(node, shift, keyHash, key)
        is Entry -> dissocEntry(node, keyHash, key)
        is CollisionNode -> dissocCollisionNode(node, keyHash, key)
        else -> throw UnsupportedOperationException()
    }
}

private fun dissocArrayNode(
        node: ArrayNode,
        shift: Int,
        keyHash: Int,
        key: Any?): Any {
    val childIndex = arrayIndex(shift, keyHash)
    val child = node.children[childIndex]
    return if (child != null) {
        val newChild = dissoc(child, shift + 5, keyHash, key)
        if (child == newChild) {
            node
        } else {
            val newChildrenCount = if (newChild != null) {
                node.childrenCount
            } else {
                node.childrenCount - 1
            }

            if (newChildrenCount == 0) {
                throw UnsupportedOperationException(
                    "If no children left, that means the ArrayNode had "
                    + "only one entry under it, which isn't allowed")
            } else {
                // If only one child left and it isn't an ArrayNode,
                // we should return this child, instead
                val returnChild = if (newChildrenCount != 1) {
                    null
                } else {
                    if (newChild != null) {
                        if (newChild is ArrayNode) null else newChild
                    } else {
                        // Should always succeed, because ArrayNode must
                        // have at least two entries under it
                        val lastChild = node.children.first(
                            { it != null && it != child })
                        if (lastChild is ArrayNode) null else lastChild
                    }
                }

                if (returnChild == null) {
                    ArrayNode(
                        node.children.immSet(childIndex, newChild),
                        newChildrenCount,
                        node.entryCount - 1)
                } else {
                    returnChild
                }
            }
        }
    } else {
        node
    }
}

private fun dissocEntry(
        node: Entry,
        keyHash: Int,
        key: Any?): Any? {
    return if (node.keyHash == keyHash && node.key == key) {
        null
    } else {
        node
    }
}

private fun dissocCollisionNode(
        node: CollisionNode,
        keyHash: Int,
        key: Any?): Any {
    return if (node.keyHash != keyHash) {
        node
    } else {
        val childIndex = node.children.indexOfFirst { it.key == key }
        if (childIndex == -1) {
            node
        } else {
            if (node.children.size > 2) {
                CollisionNode(
                    node.children.immRemoveAt(childIndex),
                    keyHash)
            } else {
                // Should always succeed, because CollisionNode must have
                // at least two children
                node.children.first { it.key != key }
            }
        }
    }
}

fun difference(lNode: Any?, rNode: Any?, shift: Int): Any? {
    return if (lNode === rNode) {
        null
    } else if (lNode != null && rNode != null) {
        when (lNode) {
            is ArrayNode -> differenceA(lNode, rNode, shift)
            is Entry -> differenceE(lNode, rNode, shift)
            is CollisionNode -> differenceC(lNode, rNode, shift)
            else -> throw UnsupportedOperationException()
        }
    } else {
        lNode
    }
}

private fun differenceA(lNode: ArrayNode, rNode: Any, shift: Int): Any? {
    return when (rNode) {
        is ArrayNode -> {
            val children = arrayOfNulls<Any>(32)
            var childrenCount = 0
            var entryCount = 0
            var returnLeftNode = true
            for (i in 0..31) {
                val lChild = lNode.children[i]
                val rChild = rNode.children[i]
                val child = difference(lChild, rChild, shift + 5)
                children[i] = child
                if (child != null) {
                    childrenCount ++
                    entryCount += countEntries(child)
                }
                if (child != lChild) {
                    returnLeftNode = false
                }
            }

            if (childrenCount == 0) {
                null
            } else if (returnLeftNode) {
                lNode
            } else {
                // If only one child left and it is not an ArrayNode,
                // we should return this child, instead
                val lastChild = if (childrenCount == 1) {
                    children.firstOrNull { it != null && it !is ArrayNode }
                } else {
                    null
                }

                if (lastChild == null) {
                    ArrayNode(children, childrenCount, entryCount)
                } else {
                    lastChild
                }
            }
        }
        is Entry -> differenceXE(lNode, rNode, shift)
        is CollisionNode -> {
            rNode.children.fold(lNode,
                fun(result: Any, rEntry: Entry): Any {
                    val lEntry = getEntry(
                        result, shift, rEntry.keyHash, rEntry.key)
                    return if (lEntry != null
                               && (lEntry === rEntry
                                   || lEntry.value == rEntry.value)) {
                        dissoc(result, shift, rEntry.keyHash, rEntry.key)!!
                    } else {
                        result
                    }
                })
        }
        else -> throw UnsupportedOperationException()
    }
}

private fun differenceE(lEntry: Entry, rNode: Any, shift: Int): Any? {
    return when (rNode) {
        is Entry -> {
            if (lEntry.key == rNode.key && lEntry.value == rNode.value) {
                null
            } else {
                lEntry
            }
        }
        else -> {
            val rEntry = getEntry(rNode, shift, lEntry.keyHash, lEntry.key)
            if (rEntry == null) {
                lEntry
            } else if (lEntry === rEntry || lEntry.value == rEntry.value) {
                null
            } else {
                lEntry
            }
        }
    }
}

private fun differenceC(lNode: CollisionNode, rNode: Any, shift: Int): Any? {
    return when (rNode) {
        is Entry -> differenceXE(lNode, rNode, shift)
        else -> {
            val children = lNode.children.filter(
                fun(lEntry): Boolean {
                    val rEntry = getEntry(
                        rNode, shift, lEntry.keyHash, lEntry.key)
                    return if (rEntry == null) {
                        true
                    } else {
                        lEntry.value != rEntry.value
                    }
                })
            if (children.size == 0) {
                null
            } else if (children.size == 1) {
                children.get(0)
            } else if (children.size == lNode.children.size) {
                lNode
            } else {
                CollisionNode(children, lNode.keyHash)
            }
        }
    }
}

private fun differenceXE(lNode: Any, rEntry: Entry, shift: Int): Any? {
    val lEntry = getEntry(lNode, shift, rEntry.keyHash, rEntry.key)
    return if (lEntry == null) {
        lNode
    } else if (lEntry === rEntry || lEntry.value == rEntry.value) {
        dissoc(lNode, shift, rEntry.keyHash, rEntry.key)
    } else {
        lNode
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

private fun <T> List<T>.immAdd(x: T): List<T> {
    val arr = ArrayList(this)
    arr.add(x)
    return arr
}

private fun <T> List<T>.immSet(i: Int, x: T): List<T> {
    val arr = ArrayList(this)
    arr.set(i, x)
    return arr
}

private fun <T> List<T>.immRemoveAt(i: Int): List<T> {
    val arr = ArrayList(this)
    arr.removeAt(i)
    return arr
}
