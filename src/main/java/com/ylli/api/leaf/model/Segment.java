package com.ylli.api.leaf.model;

import java.util.concurrent.atomic.AtomicLong;

public class Segment {
    public float loadFactor = 0.7f;
    public AtomicLong cursor;

    public volatile Long threshold;

    public Segment(LeafAlloc leafAlloc) {
        this.cursor = new AtomicLong(leafAlloc.idx);
        this.threshold = (long) (leafAlloc.step * loadFactor) + leafAlloc.idx;
    }

    public Segment(Long cursor, LeafAlloc leafAlloc) {
        this.threshold = (long) (leafAlloc.step * loadFactor) + leafAlloc.idx;
        this.cursor = new AtomicLong(cursor);
    }
}
