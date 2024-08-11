package com.ylli.api.leaf.service;

import com.ylli.api.common.exception.GenericException;
import com.ylli.api.leaf.mapper.LeafAllocMapper;
import com.ylli.api.leaf.model.LeafAlloc;
import com.ylli.api.leaf.model.Segment;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class LeafService {

    LeafAllocMapper leafAllocMapper;
    ConcurrentHashMap<String, Segment> cache = new ConcurrentHashMap<>();
    AtomicBoolean isFirstLoad = new AtomicBoolean(true);

    public LeafService(LeafAllocMapper leafAllocMapper) {
        this.leafAllocMapper = leafAllocMapper;
    }

    public Long generateId(@NonNull String bizTag) {
        Segment segment = cache.get(bizTag);
        if (segment == null && (segment = reload(bizTag)) == null) {
            throw new GenericException(HttpStatus.BAD_REQUEST, String.format("bizTag: %s not exist", bizTag));
        }

        Long id = segment.cursor.incrementAndGet();
        //阀值
        if (id < segment.threshold) {
            return id;
        } else {
            synchronized (LeafService.class) {
                if (id < cache.get(bizTag).threshold) {
                    return id;
                }
                // 扩容
                resize(id, bizTag);
            }
        }
        return id;
    }

    public synchronized Segment reload(String bizTag) {
        //load from db
        if (cache.get(bizTag) == null) {
            LeafAlloc leafAlloc = getLeafAllocByTag(bizTag);
            if (leafAlloc != null) {
                if (isFirstLoad.get()) {
                    //每次初始化自动扩容一次，防止上一个号段未使用完毕而程序挂了。可能导致id 重复
                    leafAlloc = updateAndGet(bizTag);
                    isFirstLoad.set(false);
                }
                cache.putIfAbsent(bizTag, new Segment(leafAlloc));
            }
        }
        return cache.get(bizTag);
    }

    public void resize(Long cursor, String bizTag) {
        if (cursor < cache.get(bizTag).threshold) {
            return;
        }
        do {
            LeafAlloc leafAlloc = updateAndGet(bizTag);
            if (leafAlloc != null) {
                long current = cache.get(bizTag).cursor.get();
                cache.put(bizTag, new Segment(current, leafAlloc));
            }
        } while (cursor >= cache.get(bizTag).threshold);
    }

    @Transactional(rollbackFor = Exception.class)
    public LeafAlloc updateAndGet(String bizTag) {
        leafAllocMapper.updateByTag(bizTag);
        return getLeafAllocByTag(bizTag);
    }

    public LeafAlloc getLeafAllocByTag(String bizTag) {
        List<LeafAlloc> list = leafAllocMapper.selectByExample(leafAllocMapper.wrapper().eq(LeafAlloc::getBizTag, bizTag).example());
        return list.isEmpty() ? null : list.get(0);
    }
}
