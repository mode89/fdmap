package net.akrain.fdmap;

import java.util.function.ToIntFunction;
import java.util.ArrayList;
import java.util.Optional;

public class Map {

    public final Object root;
    public final ToIntFunction<Object> keyHasher;

    public Map(Object root) {
        this(root, key -> key.hashCode());
    }

    public Map(Object root, final ToIntFunction<Object> keyHasher) {
        this.root = root;
        this.keyHasher = keyHasher;
    }

    public Map assoc(final Object key, final Object value) {
        final Nodes.Entry entry = new Nodes.Entry(
            keyHasher.applyAsInt(key), key, value);
        if (root == null) {
            return new Map(entry, keyHasher);
        } else {
            Object newRoot = Nodes.assoc(root, 0, entry);
            if (newRoot == root) {
                return this;
            } else {
                return new Map(newRoot, keyHasher);
            }
        }
    }

    public Object get(final Object key) {
        return get(key, null);
    }

    public Object get(final Object key, final Object notFound) {
        if (root == null) {
            return notFound;
        } else {
            final Nodes.Entry entry = Nodes.getEntry(
                root, 0, keyHasher.applyAsInt(key), key);
            if (entry == null) {
                return notFound;
            } else {
                return entry.value;
            }
        }
    }

    public Map dissoc(final Object key) {
        if (root == null) {
            return this;
        } else {
            Object newRoot = Nodes.dissoc(
                root, 0, keyHasher.applyAsInt(key), key);
            if (newRoot == root) {
                return this;
            } else {
                return new Map(newRoot);
            }
        }
    }

    public Seq seq() {
        if (root == null) {
            return null;
        } else {
            return Nodes.seq(root, root);
        }
    }

    public Map difference(final Map other) {
        Object rootDiff = Nodes.difference(0, this.root, other.root);
        if (rootDiff == this.root) {
            return this;
        } else {
            return new Map(rootDiff);
        }
    }
}
