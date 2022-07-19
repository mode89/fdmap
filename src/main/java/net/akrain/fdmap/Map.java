package net.akrain.fdmap;

import clojure.lang.APersistentMap;
import java.util.function.ToIntFunction;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public class Map extends APersistentMap {

    public final Object root;
    public final ToIntFunction<Object> keyHasher;

    private static final ToIntFunction<Object>
        DEFAULT_KEY_HASHER = key -> Objects.hashCode(key);
    private static final HashMap<ToIntFunction<Object>,Map>
        BLANK_MAPS = new HashMap<>();

    public static Map blank() {
        return blank(DEFAULT_KEY_HASHER);
    }

    public static Map blank(final ToIntFunction<Object> hasher) {
        final Map cachedMap = BLANK_MAPS.get(hasher);
        if (cachedMap != null) {
            return cachedMap;
        } else {
            final Map newMap = new Map(null, hasher);
            BLANK_MAPS.put(hasher, newMap);
            return newMap;
        }
    }

    private Map(final Object root, final ToIntFunction<Object> hasher) {
        if (hasher == null) {
            throw new IllegalArgumentException(
                "Key-hasher isn't allowed to be null");
        }
        this.root = root;
        this.keyHasher = hasher;
    }

    @Override
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

    @Override
    public Object get(final Object key) {
        return get(key, null);
    }

    public Object get(final Object key, final Object notFound) {
        if (root == null) {
            return notFound;
        } else {
            final Nodes.Entry entry = entryAt(key);
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
                if (newRoot == null) {
                    return blank(keyHasher);
                } else {
                    return new Map(newRoot, keyHasher);
                }
            }
        }
    }

    @Override
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
            if (rootDiff == null) {
                return blank(keyHasher);
            } else {
                return new Map(rootDiff, keyHasher);
            }
        }
    }

    // Implementation of IPersistentMap

    @Override
    public Map assocEx(Object key, Object val) {
        if (containsKey(key)) {
            throw new RuntimeException("Key already present");
        }
        return assoc(key, val);
    }

    @Override
    public Map without(Object key) {
        return dissoc(key);
    }

    // Implementation of Iterable

    @Override
    public Iterator<Nodes.Entry> iterator() {
        final Seq thisSeq = this.seq();
        return new Iterator<Nodes.Entry>() {
            private Seq seq = thisSeq;
            public boolean hasNext() {
                return seq != null;
            }
            public Nodes.Entry next() {
                if (seq != null) {
                    final Nodes.Entry next = seq.first();
                    seq = seq.next();
                    return next;
                } else {
                    throw new NoSuchElementException();
                }
            }
        };
    }

    // Implementation of Associative

    @Override
    public boolean containsKey(Object key) {
        return entryAt(key) != null;
    }

    @Override
    public Nodes.Entry entryAt(Object key) {
        return Nodes.getEntry(root, 0, keyHasher.applyAsInt(key), key);
    }

    // Implementation of IPersistentCollection

    @Override
    public int count() {
        if (root == null) {
            return 0;
        } else {
            return Nodes.countEntries(root);
        }
    }

    @Override
    public Map empty() {
        return blank(keyHasher);
    }

    @Override
    public boolean equiv(Object otherObj) {
        if (otherObj instanceof Map) {
            final Map other = (Map) otherObj;
            return Nodes.equiv(0, root, other.root);
        } else {
            return super.equiv(otherObj);
        }
    }

    // Implementation of ILookup

    @Override
    public Object valAt(Object key) {
        return get(key, null);
    }

    @Override
    public Object valAt(Object key, Object notFound) {
        return get(key, notFound);
    }
}
