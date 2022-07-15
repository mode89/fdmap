package net.akrain.fdmap;

import clojure.lang.IMapEntry;
import clojure.lang.IPersistentMap;
import java.util.function.ToIntFunction;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;

public class Map implements IPersistentMap {

    public final Object root;
    public final ToIntFunction<Object> keyHasher;

    public Map() {
        this(null, key -> key.hashCode());
    }

    public Map(final ToIntFunction<Object> keyHasher) {
        this(null, keyHasher);
    }

    private Map(final Object root, final ToIntFunction<Object> hasher) {
        if (hasher == null) {
            throw new IllegalArgumentException(
                "Key-hasher isn't allowed to be null");
        }
        this.root = root;
        this.keyHasher = hasher;
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
                return new Map(newRoot, keyHasher);
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
        if (this.keyHasher != other.keyHasher) {
            throw new UnsupportedOperationException(
                "Can't calculated difference of maps that were built " +
                "with different key-hashers");
        }
        Object rootDiff = Nodes.difference(0, this.root, other.root);
        if (rootDiff == this.root) {
            return this;
        } else {
            return new Map(rootDiff, keyHasher);
        }
    }

    // Implementation of IPersistentMap

    public Map assocEx(Object key, Object val) {
        throw new UnsupportedOperationException();
    }

    public Map without(Object key) {
        throw new UnsupportedOperationException();
    }

    // Implementation of Iterable

    public Iterator<Object> iterator() {
        throw new UnsupportedOperationException();
    }

    // Implementation of Associative

    public boolean containsKey(Object key) {
        throw new UnsupportedOperationException();
    }

    public IMapEntry entryAt(Object key) {
        throw new UnsupportedOperationException();
    }

    // Implementation of IPersistentCollection

    public int count() {
        throw new UnsupportedOperationException();
    }

    public Map cons(Object obj) {
        throw new UnsupportedOperationException();
    }

    public Map empty() {
        throw new UnsupportedOperationException();
    }

    public boolean equiv(Object obj) {
        throw new UnsupportedOperationException();
    }

    // Implementation of ILookup

    public Object valAt(Object key) {
        throw new UnsupportedOperationException();
    }

    public Object valAt(Object key, Object notFound) {
        throw new UnsupportedOperationException();
    }
}
