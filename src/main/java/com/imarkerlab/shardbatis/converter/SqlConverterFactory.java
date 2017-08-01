package com.imarkerlab.shardbatis.converter;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

import java.io.StringReader;
import java.sql.SQLException;


/**
 * 管理各种CRUD语句的Converter
 * @author qian.cheng
 * 
 */
public class SqlConverterFactory {
    private static final Log log = LogFactory.getLog(SqlConverterFactory.class);

	private static SqlConverterFactory instance = new SqlConverterFactory();

	public static SqlConverterFactory getInstance() {
		return instance;
	}

	private final CCJSqlParserManager manager;

	public SqlConverterFactory() {
		manager = new CCJSqlParserManager();
	}

	public SqlConverter createParser(String originalSql)
			throws SQLException {
		try {
			Statement statement = manager.parse(new StringReader(originalSql));
			if (statement instanceof Select) {
				SelectSqlConverter select = new SelectSqlConverter((Select)statement);
				select.init();
				return select;
			}
			else if (statement instanceof Update) {
				UpdateSqlConverter update = new UpdateSqlConverter((Update)statement);
				update.init();
				return update;
			}
			else if (statement instanceof Insert) {
				InsertSqlConverter insert = new InsertSqlConverter((Insert)statement);
				insert.init();
				return insert;
			}
			else if (statement instanceof Delete) {
				DeleteSqlConverter delete = new DeleteSqlConverter((Delete)statement);
				delete.init();
				return delete;
			}
			else {
				throw new SQLException(
						"Unsupported Parser[" + statement.getClass().getName() + "]");
			}
		}
		catch (JSQLParserException e) {
			throw new SQLException("SQL Parse Failed", e);
		}

	}

}
