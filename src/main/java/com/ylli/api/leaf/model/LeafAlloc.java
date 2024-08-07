package com.ylli.api.leaf.model;

import io.mybatis.provider.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Entity.Table("leaf_alloc")
@Getter
@Setter
@NoArgsConstructor
public class LeafAlloc {

    @Entity.Column(id = true, updatable = false, useGeneratedKeys = true)
    public Long id;

    public String bizTag;

    //起始id
    public Long idx;

    public Integer step;

    public String description;

    public Timestamp updateTime;
}

