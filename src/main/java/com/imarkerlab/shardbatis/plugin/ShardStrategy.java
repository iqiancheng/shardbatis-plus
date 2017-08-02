package com.imarkerlab.shardbatis.plugin;

import java.util.Map;

/**
 * @author qian.cheng
 */
public interface ShardStrategy {

    /**
     * 解析参数生成分库分表策略
     *
     * @param params
     *            sql参数
     * @see #shard(Map)
     */
    ShardCondition shard(Map<String, Object> params);
}
