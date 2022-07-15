package net.akrain.fdmap;

import clojure.lang.IPersistentCollection;
import clojure.lang.ISeq;

public class Seq implements ISeq {

    public final Object root;
    public final Nodes.Entry entry;
    public final int entryIndex;

    public Seq(
            final Object root,
            final Nodes.Entry entry,
            int entryIndex) {
        this.root = root;
        this.entry = entry;
        this.entryIndex = entryIndex;
    }

    // Implementation of ISeq

    public Nodes.Entry first() {
        return entry;
    }

    public Seq next() {
        return Nodes.next(root, root, 0, entry.keyHash, entryIndex);
    }

    public Seq more() {
        throw new UnsupportedOperationException();
    }

    public ISeq cons(Object obj) {
        throw new UnsupportedOperationException();
    }

    // Implementation of Sequable

    public Seq seq() {
        throw new UnsupportedOperationException();
    }

    // Implementation of IPersistentCollection

    public int count() {
        throw new UnsupportedOperationException();
    }

    public IPersistentCollection empty() {
        throw new UnsupportedOperationException();
    }

    public boolean equiv(Object obj) {
        throw new UnsupportedOperationException();
    }
}
