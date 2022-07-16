package net.akrain.fdmap;

import clojure.lang.IPersistentMap;
import clojure.lang.IPersistentVector;
import java.util.function.ToIntFunction;
import java.util.HashMap;
import java.util.Iterator;

public class Map implements IPersistentMap {

    public final Object root;
    public final ToIntFunction<Object> keyHasher;

    private static final ToIntFunction<Object>
        DEFAULT_KEY_HASHER = key -> (key == null) ? 0 : key.hashCode();
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

    public Map assocEx(Object key, Object val) {
        if (containsKey(key)) {
            throw new RuntimeException("Key already present");
        }
        return assoc(key, val);
    }

    public Map without(Object key) {
        return dissoc(key);
    }

    // Implementation of Iterable

    public Iterator<Object> iterator() {
        throw new UnsupportedOperationException();
    }

    // Implementation of Associative

    public boolean containsKey(Object key) {
        return entryAt(key) != null;
    }

    public Nodes.Entry entryAt(Object key) {
        return Nodes.getEntry(root, 0, keyHasher.applyAsInt(key), key);
    }

    // Implementation of IPersistentCollection

    public int count() {
        if (root == null) {
            return 0;
        } else {
            return Nodes.countEntries(root);
        }
    }

    public Map cons(Object obj) {
        if (obj instanceof java.util.Map.Entry) {
            final java.util.Map.Entry<Object,Object> e =
                (java.util.Map.Entry) obj;
            return assoc(e.getKey(), e.getValue());
		} else if (obj instanceof IPersistentVector) {
            final IPersistentVector v = (IPersistentVector) obj;
            if (v.count() != 2)
                throw new IllegalArgumentException(
                    "Vector arg to map conj must be a pair");
            return assoc(v.nth(0), v.nth(1));
        }
        throw new UnsupportedOperationException();
    }

    public Map empty() {
        return blank(keyHasher);
    }

    public boolean equiv(Object obj) {
        throw new UnsupportedOperationException();
    }

    // Implementation of ILookup

    public Object valAt(Object key) {
        return get(key, null);
    }

    public Object valAt(Object key, Object notFound) {
        return get(key, notFound);
    }
}
