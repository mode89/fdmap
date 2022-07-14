package net.akrain.fdmap;

import java.util.ArrayList;
import java.util.Optional;

public class Nodes {

    public static class Entry {
        public final int keyHash;
        public final Object key;
        public final Object value;

        public Entry(
                final int keyHash,
                final Object key,
                final Object value) {
            this.keyHash = keyHash;
            this.key = key;
            this.value = value;
        }
    }

    public static class ArrayNode {
        public final Object[] children;
        public final int childrenCount;

        public ArrayNode(final Object[] children, final int childrenCount) {
            this.children = children;
            this.childrenCount = childrenCount;
        }
    }

    public static class CollisionNode {
        public final ArrayList<Entry> children;
        public final int keyHash;

        public CollisionNode(
                final ArrayList<Entry> children,
                final int keyHash) {
            this.keyHash = keyHash;
            this.children = children;
        }
    }

    private static int arrayIndex(final int shift, final int keyHash) {
        return (keyHash >>> shift) & 0x1F;
    }

    private static int getKeyHash(final Object node) {
        final Class<?> nodeClass = node.getClass();
        if (nodeClass == Entry.class) {
            return ((Entry) node).keyHash;
        } else if (nodeClass == CollisionNode.class) {
            return ((CollisionNode) node).keyHash;
        } else {
            throw new RuntimeException("Unsupported operation");
        }
    }

    public static ArrayNode makeArrayNode(
            final Object node,
            final int shift) {
        final Object[] children = new Object[32];
        final int index = arrayIndex(shift, getKeyHash(node));
        children[index] = node;
        return new ArrayNode(children, 1);
    }

    public static Object assoc(
            final Object nodeObj,
            final int shift,
            final Entry entry) {
        final Class<?> nodeClass = nodeObj.getClass();
        if (nodeClass == ArrayNode.class) {
            final ArrayNode node = (ArrayNode) nodeObj;
            final int childIndex = arrayIndex(shift, entry.keyHash);
            final Object child = node.children[childIndex];
            if (child == null) {
                final Object[] newChildren = node.children.clone();
                newChildren[childIndex] = entry;
                return new ArrayNode(newChildren, node.childrenCount + 1);
            } else {
                final Object newChild = assoc(child, shift + 5, entry);
                if (child == newChild) {
                    return node;
                } else {
                    final Object[] newChildren = node.children.clone();
                    newChildren[childIndex] = newChild;
                    return new ArrayNode(newChildren, node.childrenCount);
                }
            }
        } else if (nodeClass == Entry.class) {
            final Entry node = (Entry) nodeObj;
            if (node.key.equals(entry.key)) {
                if (node.value.equals(entry.value)) {
                    return node;
                } else {
                    return entry;
                }
            } else {
                if (node.keyHash == entry.keyHash) {
                    ArrayList<Entry> children = new ArrayList<Entry>();
                    children.add(node);
                    children.add(entry);
                    return new CollisionNode(children, node.keyHash);
                } else {
                    return assoc(
                        makeArrayNode(node, shift), shift, entry);
                }
            }
        } else if (nodeClass == CollisionNode.class) {
            final CollisionNode node = (CollisionNode) nodeObj;
            if (node.keyHash == entry.keyHash) {
                int childIndex = -1;
                for (int i = 0; i < node.children.size(); ++ i) {
                    if (node.children.get(i).key.equals(entry.key)) {
                        childIndex = i;
                        break;
                    }
                }
                if (childIndex != -1) {
                    final Entry child = node.children.get(childIndex);
                    if (child.value.equals(entry.value)) {
                        return node;
                    } else {
                        final ArrayList<Entry> newChildren =
                            new ArrayList<>(node.children);
                        newChildren.set(childIndex, entry);
                        return new CollisionNode(newChildren, node.keyHash);
                    }
                } else {
                    final ArrayList<Entry> newChildren =
                        new ArrayList<>(node.children);
                    newChildren.add(entry);
                    return new CollisionNode(newChildren, node.keyHash);
                }
            } else {
                return assoc(
                    makeArrayNode(node, shift), shift, entry);
            }
        } else {
            throw new RuntimeException("Unexpected type of node");
        }
    }

    public static Entry getEntry(
            final Object nodeObj,
            final int shift,
            final int keyHash,
            final Object key) {
        final Class<?> nodeClass = nodeObj.getClass();
        if (nodeClass == ArrayNode.class) {
            final ArrayNode node = (ArrayNode) nodeObj;
            final int childIndex = arrayIndex(shift, keyHash);
            final Object child = node.children[childIndex];
            if (child == null) {
                return null;
            } else {
                return getEntry(child, shift + 5, keyHash, key);
            }
        } else if (nodeClass == Entry.class) {
            final Entry node = (Entry) nodeObj;
            if (node.keyHash == keyHash) {
                if (node.key.equals(key)) {
                    return node;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else if (nodeClass == CollisionNode.class) {
            return ((CollisionNode) nodeObj).children
                .stream()
                .filter(e -> e.key.equals(key))
                .findFirst()
                .orElse(null);
        } else {
            throw new RuntimeException("Unexpected type of node");
        }
    }

    public static Object dissoc(
            final Object nodeObj,
            final int shift,
            final int keyHash,
            final Object key) {
        final Class<?> nodeClass = nodeObj.getClass();
        if (nodeClass == ArrayNode.class) {
            final ArrayNode node = (ArrayNode) nodeObj;
            final int childIndex = arrayIndex(shift, keyHash);
            final Object child = node.children[childIndex];
            if (child == null) {
                return node;
            } else {
                final Object newChild =
                    dissoc(child, shift + 5, keyHash, key);
                if (child == newChild) {
                    return node;
                } else {
                    if (newChild == null) {
                        if (node.childrenCount > 1) {
                            final Object[] newChildren =
                                node.children.clone();
                            newChildren[childIndex] = null;
                            return new ArrayNode(
                                newChildren, node.childrenCount - 1);
                        } else {
                            return null;
                        }
                    } else {
                        final Object[] newChildren = node.children.clone();
                        newChildren[childIndex] = newChild;
                        return new ArrayNode(newChildren, node.childrenCount);
                    }
                }
            }
        } else if (nodeClass == Entry.class) {
            final Entry node = (Entry) nodeObj;
            if (node.key.equals(key)) {
                return null;
            } else {
                return node;
            }
        } else if (nodeClass == CollisionNode.class) {
            final CollisionNode node = (CollisionNode) nodeObj;
            if (node.keyHash != keyHash) {
                return node;
            } else {
                final Optional<Entry> child = node.children.stream()
                    .filter(e -> e.key.equals(key))
                    .findFirst();
                if (child.isPresent()) {
                    if (node.children.size() > 2) {
                        final ArrayList<Entry> newChildren =
                            new ArrayList<>(node.children);
                        newChildren.remove(child.get());
                        return new CollisionNode(newChildren, keyHash);
                    } else {
                        return node.children.stream()
                            .filter(e -> !e.key.equals(key))
                            .findFirst()
                            .get();
                    }
                } else {
                    return node;
                }
            }
        } else {
            throw new RuntimeException("Unexpected type of node");
        }
    }

    private static Object differenceWithEntry(
            final int shift,
            final Object leftNode,
            final Object rightNode) {
        final Entry rightEntry = (Entry) rightNode;
        final Entry leftEntry = getEntry(
            leftNode, shift, rightEntry.keyHash, rightEntry.key);
        if (leftEntry == null) {
            return leftNode;
        } else if (leftEntry == rightEntry
                   || leftEntry.value.equals(rightEntry.value)) {
            return dissoc(
                leftNode,
                shift,
                rightEntry.keyHash,
                rightEntry.key);
        } else {
            return leftNode;
        }
    }

    public static Object difference(
            final int shift,
            final Object leftNodeObj,
            final Object rightNodeObj) {
        if (leftNodeObj == rightNodeObj) {
            return null;
        } else if (leftNodeObj != null && rightNodeObj != null) {
            final Class<?> leftNodeClass = leftNodeObj.getClass();
            final Class<?> rightNodeClass = rightNodeObj.getClass();
            if (leftNodeClass == ArrayNode.class) {
                final ArrayNode leftNode = (ArrayNode) leftNodeObj;
                if (rightNodeClass == ArrayNode.class) {
                    final ArrayNode rightNode = (ArrayNode) rightNodeObj;
                    final Object[] children = new Object[32];
                    int childrenCount = 0;
                    boolean returnLeftNode = true;
                    for (int i = 0; i < 32; ++ i) {
                        final Object leftChild = leftNode.children[i];
                        final Object rightChild = rightNode.children[i];
                        final Object child = difference(
                            shift + 5, leftChild, rightChild);
                        children[i] = child;
                        if (child != null) {
                            childrenCount += 1;
                        }
                        if (child != leftChild) {
                            returnLeftNode = false;
                        }
                    }
                    if (childrenCount == 0) {
                        return null;
                    } else if (returnLeftNode) {
                        return leftNode;
                    } else {
                        return new ArrayNode(children, childrenCount);
                    }
                } else if (rightNodeClass == Entry.class) {
                    return differenceWithEntry(
                        shift, leftNode, rightNodeObj);
                } else if (rightNodeClass == CollisionNode.class) {
                    final CollisionNode rightNode =
                        (CollisionNode) rightNodeObj;
                    Object result = leftNode;
                    for (Entry rightEntry: rightNode.children) {
                        final Entry leftEntry = getEntry(
                            result, shift, rightEntry.keyHash, rightEntry.key);
                        if (leftEntry != null
                            && (leftEntry == rightEntry
                                || leftEntry.value.equals(rightEntry.value))) {
                            result = dissoc(
                                result,
                                shift,
                                rightEntry.keyHash,
                                rightEntry.key);
                        }
                        if (result == null) {
                            break;
                        }
                    }
                    return result;
                } else {
                    throw new RuntimeException(
                        "Unexpected type of right node");
                }
            } else if (leftNodeClass == Entry.class) {
                final Entry leftNode = (Entry) leftNodeObj;
                if (rightNodeClass == ArrayNode.class
                    || rightNodeClass == CollisionNode.class) {
                    final Entry rightEntry = getEntry(
                        rightNodeObj, shift, leftNode.keyHash, leftNode.key);
                    if (rightEntry == null) {
                        return leftNode;
                    } else if (leftNode == rightEntry
                               || leftNode.value.equals(rightEntry.value)) {
                        return null;
                    } else {
                        return leftNode;
                    }
                } else if (rightNodeClass == Entry.class) {
                    final Entry rightNode = (Entry) rightNodeObj;
                    if (leftNode.key.equals(rightNode.key)
                        && leftNode.value.equals(rightNode.value)) {
                        return null;
                    } else {
                        return leftNode;
                    }
                } else {
                    throw new RuntimeException(
                        "Unexpected type of right node");
                }
            } else if (leftNodeClass == CollisionNode.class) {
                final CollisionNode leftNode = (CollisionNode) leftNodeObj;
                if (rightNodeClass == ArrayNode.class
                    || rightNodeClass == CollisionNode.class) {
                    final ArrayList<Entry> children = new ArrayList<>();
                    for (Entry leftEntry: leftNode.children) {
                        final Entry rightEntry = getEntry(
                            rightNodeObj,
                            shift,
                            leftEntry.keyHash,
                            leftEntry.key);
                        if (rightEntry == null
                            || !rightEntry.value.equals(leftEntry.value)) {
                            children.add(leftEntry);
                        }
                    }
                    final int childrenNum = children.size();
                    if (childrenNum == 0) {
                        return null;
                    } else if (childrenNum == 1) {
                        return children.get(0);
                    } else if (childrenNum == leftNode.children.size()) {
                        return leftNode;
                    } else {
                        return new CollisionNode(children, leftNode.keyHash);
                    }
                } else if (rightNodeClass == Entry.class) {
                    return differenceWithEntry(
                        shift, leftNode, rightNodeObj);
                } else {
                    throw new RuntimeException(
                        "Unexpected type of right node");
                }
            } else {
                throw new RuntimeException("Unexpected type of left node");
            }
        } else {
            return leftNodeObj;
        }
    }
}
