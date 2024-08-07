package com.ylli.api.leaf.mapper;

import com.ylli.api.leaf.model.LeafAlloc;
import io.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface LeafAllocMapper extends BaseMapper<LeafAlloc, Long> {
    @Update("UPDATE leaf_alloc SET idx = idx + step WHERE biz_tag = #{biz_tag}")
    int updateByTag(@Param("biz_tag") String biz_tag);
}
