package com.imarkerlab.shardbatis.plugin;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public final class ShardCondition {

	/**
	 * 库后缀
	 */
	private String databaseSuffix;

	/**
	 * 表后缀
	 */
	private String tableSuffix;

}