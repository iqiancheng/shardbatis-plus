package com.challions.dao.shardbatis.converter;

import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.util.deparser.StatementDeParser;

/**
 * @author qian.cheng
 */
public class InsertSqlConverter implements SqlConverter {

    private boolean inited = false;

    private Insert statement;

    private List<Table> tables = new ArrayList<Table>();

    public InsertSqlConverter(Insert statement) {
        this.statement = statement;
    }

    @Override
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

    @Override
    public String toSQL() {
        StatementDeParser parser = new StatementDeParser(new StringBuilder());
        statement.accept(parser);
        return parser.getBuffer().toString();
    }

}
