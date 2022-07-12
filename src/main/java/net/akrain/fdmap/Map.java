package net.akrain.fdmap;

import java.util.function.ToIntFunction;
import java.util.ArrayList;
import java.util.Optional;

public class Map {

    public Object root;
    public ToIntFunction<Object> keyHasher;

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

    public static Object nodeAssoc(
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
                final Object newChild = nodeAssoc(child, shift + 5, entry);
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
                    return nodeAssoc(
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
                return nodeAssoc(
                    makeArrayNode(node, shift), shift, entry);
            }
        } else {
            throw new RuntimeException("Unexpected type of node");
        }
    }

    public static Entry nodeGetEntry(
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
                return nodeGetEntry(child, shift + 5, keyHash, key);
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

    public static Object nodeDissoc(
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
                    nodeDissoc(child, shift + 5, keyHash, key);
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

    public Map(Object root) {
        this(root, key -> key.hashCode());
    }

    public Map(Object root, final ToIntFunction<Object> keyHasher) {
        this.root = root;
        this.keyHasher = keyHasher;
    }

    public Map assoc(final Object key, final Object value) {
        final Entry entry = new Entry(keyHasher.applyAsInt(key), key, value);
        if (root == null) {
            return new Map(entry, keyHasher);
        } else {
            Object newRoot = nodeAssoc(root, 0, entry);
            if (newRoot == root) {
                return this;
            } else {
                return new Map(newRoot, keyHasher);
            }
        }
    }

    public Object get(final Object key) {
        if (root == null) {
            return null;
        } else {
            final Entry entry = nodeGetEntry(
                root, 0, keyHasher.applyAsInt(key), key);
            if (entry == null) {
                return null;
            } else {
                return entry.value;
            }
        }
    }

    public Map dissoc(final Object key) {
        if (root == null) {
            return this;
        } else {
            Object newRoot = nodeDissoc(
                root, 0, keyHasher.applyAsInt(key), key);
            if (newRoot == root) {
                return this;
            } else {
                return new Map(newRoot);
            }
        }
    }
}
