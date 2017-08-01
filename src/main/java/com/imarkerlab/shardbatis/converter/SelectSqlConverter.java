package com.imarkerlab.shardbatis.converter;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.util.deparser.StatementDeParser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SelectSqlConverter implements SqlConverter, SelectVisitor, FromItemVisitor, ExpressionVisitor, ItemsListVisitor {

	private boolean inited = false;

	private Select statement;

	private List<Table> tables = new ArrayList<Table>();

	public SelectSqlConverter(Select statement) {
		this.statement = statement;
	}

	public List<Table> getTables() {
		return tables;
	}

	public String toSQL() {
		StatementDeParser deParser = new StatementDeParser(new StringBuilder());
		statement.accept(deParser);
		return deParser.getBuffer().toString();
	}

	public void init() {
		if (inited) {
			return;
		}
		inited = true;
		statement.getSelectBody().accept(this);
	}

	public void visit(PlainSelect plainSelect) {
		plainSelect.getFromItem().accept(this);

		if (plainSelect.getJoins() != null) {
			Iterator<Join> joinsIt = plainSelect.getJoins().iterator();
			while (joinsIt.hasNext()) {
				Join join = (Join) joinsIt.next();
				join.getRightItem().accept(this);
			}
		}
		if (plainSelect.getWhere() != null)
			plainSelect.getWhere().accept(this);
	}

	public void visit(Table table) {
		tables.add(table);
	}

	public void visit(SubSelect subSelect) {
		subSelect.getSelectBody().accept(this);
	}

	public void visit(Addition addition) {
		visitBinaryExpression(addition);
	}

	public void visit(AndExpression andExpression) {
		visitBinaryExpression(andExpression);
	}

	public void visit(Between between) {
		between.getLeftExpression().accept(this);
		between.getBetweenExpressionStart().accept(this);
		between.getBetweenExpressionEnd().accept(this);
	}

	public void visit(Column tableColumn) {
	}

	public void visit(Division division) {
		visitBinaryExpression(division);
	}

	
	public void visit(HexValue hexValue)
	{

	}

	
	public void visit(MySQLGroupConcat groupConcat)
	{

	}

	
	public void visit(RowConstructor rowConstructor)
	{

	}

	public void visit(DoubleValue doubleValue) {
	}

	public void visit(EqualsTo equalsTo) {
		visitBinaryExpression(equalsTo);
	}

	public void visit(Function function) {
	}

	public void visit(GreaterThan greaterThan) {
		visitBinaryExpression(greaterThan);
	}

	public void visit(GreaterThanEquals greaterThanEquals) {
		visitBinaryExpression(greaterThanEquals);
	}

	public void visit(InExpression inExpression) {
		inExpression.getLeftExpression().accept(this);
		inExpression.getLeftItemsList().accept(this);
		inExpression.getRightItemsList().accept(this);
	}

	public void visit(IsNullExpression isNullExpression) {
	}

	public void visit(JdbcParameter jdbcParameter) {
	}

	public void visit(LikeExpression likeExpression) {
		visitBinaryExpression(likeExpression);
	}

	public void visit(ExistsExpression existsExpression) {
		existsExpression.getRightExpression().accept(this);
	}

	public void visit(LongValue longValue) {
	}

	public void visit(MinorThan minorThan) {
		visitBinaryExpression(minorThan);
	}

	public void visit(MinorThanEquals minorThanEquals) {
		visitBinaryExpression(minorThanEquals);
	}

	public void visit(Multiplication multiplication) {
		visitBinaryExpression(multiplication);
	}

	public void visit(NotEqualsTo notEqualsTo) {
		visitBinaryExpression(notEqualsTo);
	}

	public void visit(NullValue nullValue) {
	}

	public void visit(OrExpression orExpression) {
		visitBinaryExpression(orExpression);
	}

	public void visit(Parenthesis parenthesis) {
		parenthesis.getExpression().accept(this);
	}

	public void visit(Subtraction subtraction) {
		visitBinaryExpression(subtraction);
	}

	public void visitBinaryExpression(BinaryExpression binaryExpression) {
		binaryExpression.getLeftExpression().accept(this);
		binaryExpression.getRightExpression().accept(this);
	}

	public void visit(ExpressionList expressionList) {
		Iterator<Expression> iter = expressionList.getExpressions().iterator();
		while (iter.hasNext()) {
			Expression expression = iter.next();
			expression.accept(this);
		}
	}

	public void visit(DateValue dateValue) {
	}

	public void visit(TimestampValue timestampValue) {
	}

	public void visit(TimeValue timeValue) {
	}

	public void visit(CaseExpression caseExpression) {
	}

	public void visit(WhenClause whenClause) {
	}

	public void visit(AllComparisonExpression allComparisonExpression) {
		allComparisonExpression.getSubSelect().getSelectBody().accept(this);
	}

	public void visit(AnyComparisonExpression anyComparisonExpression) {
		anyComparisonExpression.getSubSelect().getSelectBody().accept(this);
	}

	public void visit(SubJoin subjoin) {
		subjoin.getLeft().accept(this);
		subjoin.getJoin().getRightItem().accept(this);
	}

	public void visit(Concat concat) {
		visitBinaryExpression(concat);
	}

	public void visit(Matches matches) {
		visitBinaryExpression(matches);
	}

	public void visit(BitwiseAnd bitwiseAnd) {
		visitBinaryExpression(bitwiseAnd);
	}

	public void visit(BitwiseOr bitwiseOr) {
		visitBinaryExpression(bitwiseOr);
	}

	public void visit(BitwiseXor bitwiseXor) {
		visitBinaryExpression(bitwiseXor);
	}

	public void visit(StringValue stringValue) {
	}

	
	public void visit(MultiExpressionList expressionList) {
	}

	
	public void visit(SignedExpression arg0) {

	}

	
	public void visit(JdbcNamedParameter arg0) {

	}

	
	public void visit(CastExpression arg0) {

	}

	
	public void visit(Modulo arg0) {

	}

	
	public void visit(AnalyticExpression arg0) {

	}

	
	public void visit(WithinGroupExpression arg0) {

	}

	
	public void visit(ExtractExpression arg0) {

	}

	
	public void visit(IntervalExpression arg0) {

	}

	
	public void visit(OracleHierarchicalExpression arg0) {

	}

	
	public void visit(RegExpMatchOperator arg0) {

	}

	
	public void visit(JsonExpression arg0) {

	}

	
	public void visit(RegExpMySQLOperator arg0) {

	}

	
	public void visit(UserVariable arg0) {

	}

	
	public void visit(NumericBind arg0) {

	}

	
	public void visit(KeepExpression arg0) {

	}

	
	public void visit(LateralSubSelect arg0) {

	}

	
	public void visit(ValuesList arg0) {

	}

	
	public void visit(SetOperationList arg0) {

	}

	
	public void visit(WithItem arg0) {

	}

}