package net.akrain.fdmap;

public class Seq {

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

    public Nodes.Entry first() {
        return entry;
    }

    public Seq next() {
        return Nodes.next(root, root, 0, entry.keyHash, entryIndex);
    }
}
