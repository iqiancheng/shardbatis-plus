package com.challions.dao.shardbatis.converter;

import java.util.List;

import net.sf.jsqlparser.schema.Table;

/**
 * @author qian.cheng
 */
public interface SqlConverter {


    public List<Table> getTables();

    public String toSQL();
}
