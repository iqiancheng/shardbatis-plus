package com.imarkerlab.shardbatis.converter;

import net.sf.jsqlparser.schema.Table;

import java.util.List;

/**
 * @author qian.cheng
 */
public interface SqlConverter {


    public List<Table> getTables();

    public String toSQL();
}
