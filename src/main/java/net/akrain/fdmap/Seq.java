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

    @Override
    public Nodes.Entry first() {
        return entry;
    }

    @Override
    public Seq next() {
        return Nodes.next(root, root, 0, entry.keyHash, entryIndex);
    }

    @Override
    public Seq more() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ISeq cons(Object obj) {
        throw new UnsupportedOperationException();
    }

    // Implementation of Sequable

    @Override
    public Seq seq() {
        return this;
    }

    // Implementation of IPersistentCollection

    @Override
    public int count() {
        throw new UnsupportedOperationException();
    }

    @Override
    public IPersistentCollection empty() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equiv(Object obj) {
        throw new UnsupportedOperationException();
    }
}
