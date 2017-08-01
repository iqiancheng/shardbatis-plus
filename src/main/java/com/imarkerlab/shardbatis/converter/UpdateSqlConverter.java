package com.imarkerlab.shardbatis.converter;


import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.deparser.StatementDeParser;

import java.util.ArrayList;
import java.util.List;


/**
 * @author qiancheng
 */
public class UpdateSqlConverter implements SqlConverter {

    private boolean inited = false;

    private Update statement;

    private List<Table> tables = new ArrayList<Table>();

    public UpdateSqlConverter(Update statement) {
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
        tables.addAll(statement.getTables());
    }

    public String toSQL() {
        StatementDeParser deParser = new StatementDeParser(new StringBuilder());
        statement.accept(deParser);
        return deParser.getBuffer().toString();
    }

}