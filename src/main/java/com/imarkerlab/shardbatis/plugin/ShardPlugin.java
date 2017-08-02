package com.imarkerlab.shardbatis.plugin;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.imarkerlab.shardbatis.converter.SqlConverter;
import com.imarkerlab.shardbatis.converter.SqlConverterFactory;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.schema.Table;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;

/**
 * 分片拦截器
 * @author qian.cheng
 */
@Intercepts({@org.apache.ibatis.plugin.Signature(type = StatementHandler.class
        , method = "prepare"
        , args = {java.sql.Connection.class})})
@Slf4j
public class ShardPlugin implements Interceptor {

    private final static Set<Class<?>> SINGLE_PARAM_CLASSES = Sets.newHashSet();

    @Setter
    private Map<String, String> table2DB = Maps.newHashMap();

    /**
     * 分库分表规则解析策略
     */
    private final Map<String, ShardStrategy> strategies = Maps.newHashMap();

    private final Field boundSqlField;

    static {
        SINGLE_PARAM_CLASSES.add(int.class);
        SINGLE_PARAM_CLASSES.add(Integer.class);

        SINGLE_PARAM_CLASSES.add(long.class);
        SINGLE_PARAM_CLASSES.add(Long.class);

        SINGLE_PARAM_CLASSES.add(short.class);
        SINGLE_PARAM_CLASSES.add(Short.class);

        SINGLE_PARAM_CLASSES.add(byte.class);
        SINGLE_PARAM_CLASSES.add(Byte.class);

        SINGLE_PARAM_CLASSES.add(float.class);
        SINGLE_PARAM_CLASSES.add(Float.class);

        SINGLE_PARAM_CLASSES.add(double.class);
        SINGLE_PARAM_CLASSES.add(Double.class);

        SINGLE_PARAM_CLASSES.add(boolean.class);
        SINGLE_PARAM_CLASSES.add(Boolean.class);

        SINGLE_PARAM_CLASSES.add(char.class);
        SINGLE_PARAM_CLASSES.add(Character.class);

        SINGLE_PARAM_CLASSES.add(String.class);

    }

    public ShardPlugin() {
        try {
            boundSqlField = BoundSql.class.getDeclaredField("sql");
            boundSqlField.setAccessible(true);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Object intercept(Invocation invocation)
            throws Throwable {

        if (CollectionUtils.isEmpty(table2DB)) {
            throw new IllegalArgumentException("请初始化逻辑表 与数据库映射");
        }

        StatementHandler statementHandler = (StatementHandler)invocation.getTarget();
        BoundSql boundSql = statementHandler.getBoundSql();
        String originalSql = boundSql.getSql();

        log.debug("Shard Original SQL:{}", originalSql);

        SqlConverter sqlParser = SqlConverterFactory.getInstance().createParser(originalSql);
        List<Table> tables = sqlParser.getTables();

        if (tables.isEmpty()) {
            return invocation.proceed();
        }

        for (Table table : tables) {
            boolean strict = false;
            String rowTableName = table.getName();
            String logicTableName = null;
            if (rowTableName.startsWith("`") && rowTableName.endsWith("`")) {
                strict = true;
                logicTableName = rowTableName.substring(1, rowTableName.length() - 1);
            }
            else {
                logicTableName = rowTableName;
            }

            ShardStrategy strategy = strategies.get(logicTableName);

            String physicalDBName = table2DB.get(logicTableName);

            if (StringUtils.isEmpty(physicalDBName)) {
                throw new IllegalArgumentException(logicTableName + "找不到对应的db");
            }

            if (strategy == null) { // 找不到strategy,直接根据表名找找到对应的schema

                if (strict) {
                    String targetTableName = "`" + logicTableName + "`";
                    table.setName(targetTableName);
                    table.setSchemaName("`" + physicalDBName + "`");
                }
                else {
                    table.setName(logicTableName);
                    table.setSchemaName(physicalDBName);
                }
            }
            else {
                Map<String, Object> params = paresParams(boundSql);
                ShardCondition condition = strategy.shard(params);
                String logicDb = table2DB.get(logicTableName);

                String dbPrix = logicDb.substring(0, logicDb.length() - 2);
                String dbSuffix = logicDb.substring(logicDb.length() - 3, logicDb.length());
                physicalDBName = dbPrix + condition.getDatabaseSuffix() + dbSuffix;

                if (strict) {
                    String targetTableName = "`" + logicTableName + condition.getTableSuffix()
                            + "`";
                    table.setName(targetTableName);
                    table.setSchemaName("`" + physicalDBName + "`");
                }
                else {
                    table.setName(logicTableName + "_" + condition.getTableSuffix());
                    table.setSchemaName(physicalDBName);
                }
            }
        }

        String targetSQL = sqlParser.toSQL();

        log.debug("Shard Convert SQL:{}", targetSQL);

        boundSqlField.set(boundSql, targetSQL);

        return invocation.proceed();

    }

    /**
     * 解析参数
     *
     * @param boundSql 原sql
     * @return sql key = value 参数
     * @throws Throwable 解析失败快速抛出异常
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> paresParams(BoundSql boundSql)
            throws Throwable {
        Object parameterObject = boundSql.getParameterObject();
        Map<String, Object> params = null;
        if (SINGLE_PARAM_CLASSES.contains(parameterObject.getClass())) {
            // 单一参数
            List<ParameterMapping> mapping = boundSql.getParameterMappings();
            if (mapping != null && !mapping.isEmpty()) {
                ParameterMapping m = mapping.get(0);
                params = new HashMap<String, Object>();
                params.put(m.getProperty(), parameterObject);
            }
            else {
                params = Collections.emptyMap();
            }
        }
        else {
            // 对象参数
            if (parameterObject instanceof Map) {
                params = (Map<String, Object>)parameterObject;
            }
            else {
                params = new HashMap<String, Object>();
                BeanInfo beanInfo = Introspector.getBeanInfo(parameterObject.getClass());
                PropertyDescriptor[] proDescrtptors = beanInfo.getPropertyDescriptors();
                if (proDescrtptors != null && proDescrtptors.length > 0) {
                    for (PropertyDescriptor propDesc : proDescrtptors) {
                        params.put(propDesc.getName(),
                                propDesc.getReadMethod().invoke(parameterObject));
                    }
                }
            }
        }
        return params;
    }

    /**
     * @param target
     *            target
     * @return plugin
     */
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    /**
     * 初始化配置
     *
     * @param properties
     *            配置文件
     */
    public void setProperties(Properties properties) {
        String configsLocation = properties.getProperty("configsLocation");
        if (configsLocation == null) {
            throw new IllegalArgumentException("ShardPlugin[" + getClass().getName()
                    + "] Property[configsLocation] Cannot Empty");
        }
        ClassLoader classLoader = this.getClass().getClassLoader();

        InputStream configInputStream = null;
        InputStream validateInputStream = null;
        InputStream xsdInputStream = null;
        try {

            String clazzName = this.getClass().getName();
            String xsdPath = clazzName.substring(0, clazzName.lastIndexOf('.') + 1).replace('.',
                    '/') + "mybatis-sharding-config.xsd";

            xsdInputStream = classLoader.getResourceAsStream(xsdPath);
            configInputStream = classLoader.getResourceAsStream(configsLocation);
            validateInputStream = classLoader.getResourceAsStream(configsLocation);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            SAXReader reader = new SAXReader(parser.getXMLReader());
            Document document = reader.read(configInputStream);
            Element root = document.getRootElement();
            parseStrategies(root);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            try {
                if (validateInputStream != null) {
                    validateInputStream.close();
                }
            }
            catch (IOException e) {
                // ignore
            }

            try {
                if (xsdInputStream != null) {
                    xsdInputStream.close();
                }
            }
            catch (IOException e) {
                // ignore
            }

            try {
                if (configInputStream != null) {
                    configInputStream.close();
                }
            }
            catch (IOException e) {
                // ignore
            }
        }

    }

    /**
     * 解析策略文件
     *
     * @param root
     *
     * @throws ClassNotFoundException
     *             exception
     * @throws InstantiationException
     *             exception
     * @throws IllegalAccessException
     *             exception
     */
    private void parseStrategies(Element root)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        List<?> strategies = root.elements("strategy");
        if (strategies != null) {
            for (Object o : strategies) {
                Element strategy = (Element)o;
                String logicTable = strategy.attribute("logicTable").getStringValue();
                String strategyClass = strategy.attribute("class").getStringValue();
                Class<?> clazz = Class.forName(strategyClass);
                ShardStrategy shardStrategy = (ShardStrategy)clazz.newInstance();
                if (this.strategies.containsKey(logicTable)) {
                    throw new IllegalArgumentException("LogicTable[" + logicTable + "] Duplicate");
                }
                this.strategies.put(logicTable, shardStrategy);
            }
        }
    }


}
