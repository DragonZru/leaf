package com.ylli.api.leaf.service;

import com.ylli.api.common.exception.GenericException;
import com.ylli.api.leaf.mapper.LeafAllocMapper;
import com.ylli.api.leaf.model.LeafAlloc;
import com.ylli.api.leaf.model.Segment;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class LeafService {

    LeafAllocMapper leafAllocMapper;

    HashMap<String, Segment> cache = new HashMap();
    AtomicBoolean isFirstLoad = new AtomicBoolean(true);

    public LeafService(LeafAllocMapper leafAllocMapper) {
        this.leafAllocMapper = leafAllocMapper;
    }

    public long generateId(@NonNull String bizTag) {
        Segment segment = cache.get(bizTag) == null ? reload(bizTag) : cache.get(bizTag);
        if (segment == null) {
            throw new GenericException(HttpStatus.BAD_REQUEST, String.format("bizTag: %s not exist", bizTag));
        }

        long id = segment.cursor.incrementAndGet();
        //阀值
        if (id < segment.threshold) {
            return id;
        } else {
            synchronized (segment) {
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
                //每次初始化自动扩容一次，防止上一个号段未使用完毕而程序挂了。可能导致id 重复
                if (isFirstLoad.get()) {
                    leafAlloc = updateAndGet(bizTag);
                    isFirstLoad.set(false);
                }
                return cache.putIfAbsent(bizTag, new Segment(leafAlloc));
            }
        }
        // 不能直接返回null
        // 多线程环境下可能多个线程同时进入reload() -> cache.get(bizTag) == null
        // 1.T1更新缓存成功,返回segment
        // 2.T2 if(cache.get(bizTag) == null) 此时=false,直接执行return
        return cache.get(bizTag);
    }

    public void resize(Long id, String bizTag) {
        if (id < cache.get(bizTag).threshold) {
            return;
        }
        LeafAlloc leafAlloc = updateAndGet(bizTag);
        if (leafAlloc != null) {
            cache.put(bizTag, new Segment(leafAlloc));
        }
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
