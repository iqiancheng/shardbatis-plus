package com.imarkerlab.shardbatis.plugin;

import com.imarkerlab.shardbatis.strategy.ShardStrategy;

import java.util.Map;

/**
 * 分库分表接口
 * @author qian.cheng
 */
public interface ShardPlugin {

    /**
     * 分库分表逻辑
     * @param params
     * @return
     * @see #shard(Map)
     */
    ShardStrategy shard(Map<String, Object> params);
}
