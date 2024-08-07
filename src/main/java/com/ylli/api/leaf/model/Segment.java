package com.ylli.api.leaf.model;

import java.util.concurrent.atomic.AtomicLong;

public class Segment {
    public float loadFactor = 0.7f;
    public AtomicLong cursor;
    public LeafAlloc leafAlloc;

    public volatile Long threshold;

    public Segment(LeafAlloc leafAlloc) {
        this.cursor = new AtomicLong(leafAlloc.idx);
        this.leafAlloc = leafAlloc;
        this.threshold = (long) (leafAlloc.step * loadFactor) + leafAlloc.idx;
    }
}
