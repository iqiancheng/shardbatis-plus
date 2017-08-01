package com.imarkerlab.shardbatis.converter;


import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.util.deparser.StatementDeParser;

import java.util.ArrayList;
import java.util.List;


/**
 * @author qiancheng
 */
public class DeleteSqlConverter implements SqlConverter {

    private boolean inited = false;

    private Delete statement;

    private List<Table> tables = new ArrayList<Table>();

    public DeleteSqlConverter(Delete statement) {
        this.statement = statement;
    }

    public List<Table> getTables() {
        return tables;
    }

    public void init() {
        if (inited) {
            return;
        }
        inited = true;
        tables.add(statement.getTable());
    }

    public String toSQL() {
        StatementDeParser deParser = new StatementDeParser(new StringBuilder());
        statement.accept(deParser);
        return deParser.getBuffer().toString();
    }

}