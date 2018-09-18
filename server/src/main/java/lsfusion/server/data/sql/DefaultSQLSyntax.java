package lsfusion.server.data.sql;

import lsfusion.base.BaseUtils;
import lsfusion.base.col.interfaces.immutable.ImList;
import lsfusion.base.col.interfaces.immutable.ImOrderMap;
import lsfusion.server.data.SessionTable;
import lsfusion.server.data.expr.formula.SQLSyntaxType;
import lsfusion.server.data.expr.formula.SumFormulaImpl;
import lsfusion.server.data.expr.query.GroupType;
import lsfusion.server.data.query.*;
import lsfusion.server.data.type.*;

import java.sql.*;

public abstract class DefaultSQLSyntax implements SQLSyntax {

    
    private static SQLSyntax[] syntaxes = null;
    private static SQLSyntax[] getSyntaxes() {
        if(syntaxes == null) 
            syntaxes = new SQLSyntax[] {OracleSQLSyntax.instance, MSSQLSQLSyntax.instance, MySQLSQLSyntax.instance,
                    PostgreSQLSyntax.instance, InformixSQLSyntax.instance};
        return syntaxes;
    }

    public static SQLSyntax getSyntax(String url) throws SQLException {
        String driverName = DriverManager.getDriver(url).getClass().getCanonicalName();
        for(SQLSyntax syntax : getSyntaxes())
            if(syntax.getClassName().equals(driverName))
                return syntax;
        throw new RuntimeException("SQL syntax for the specified driver was not found");
    }
    
    protected static String genTypePostfix(ImList<Type> types) {
        return genTypePostfix(types, new boolean[types.size()]);
    }

    public static String genTypePostfix(ImList<Type> types, boolean[] desc) {
        String result = "";
        for(int i=0,size=types.size();i<size;i++)
            result = (result.length()==0?"":result + "_") + types.get(i).getSID() + (desc[i]?"_D":"");
        return result;
    }

    public static String genSafeCastName(Type type) {
        return "scast_" + type.getSID();
    }

    public String getBPTextType() {
        throw new UnsupportedOperationException();
    }

    public int getBPTextSQL() {
        throw new UnsupportedOperationException();
    }

    public String getStringType(int length) {
        return "char(" + length + ")";
    }

    public int getStringSQL() {
        return Types.CHAR;
    }

    @Override
    public String getVarStringType(int length) {
        return "varchar(" + length + ")";
    }

    @Override
    public String getAnalyze() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getVacuumDB() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getVarStringSQL() {
        return Types.VARCHAR;
    }

    public String getNumericType(int length, int precision) {
        return "numeric(" + length + "," + precision + ")";
    }

    public int getNumericSQL() {
        return Types.NUMERIC;
    }

    public String getIntegerType() {
        return "integer";
    }

    public int getIntegerSQL() {
        return Types.INTEGER;
    }

    public String getDateType() {
        return "date";
    }

    public int getDateSQL() {
        return Types.DATE;
    }

    public String getDateTimeType() {
        return "timestamp";
    }

    public int getDateTimeSQL() {
        return Types.TIMESTAMP;
    }

    public String getTimeType() {
        return "time";
    }

    public int getTimeSQL() {
        return Types.TIME;
    }

    public String getLongType() {
        return "long";
    }

    public int getLongSQL() {
        return Types.BIGINT;
    }

    public String getDoubleType() {
        return "double precision";
    }

    public int getDoubleSQL() {
        return Types.DOUBLE;
    }

    public String getBitType() {
        return "integer";
    }

    public int getBitSQL() {
        return Types.INTEGER;
    }

    public String getTextType() {
        return "text";
    }

    public int getTextSQL() {
        return Types.VARCHAR;
    }

    public boolean hasDriverCompositeProblem() {
        return false;
    }

    public int getCompositeSQL() {
        return Types.BINARY;
    }

    public String getByteArrayType() {
        return "longvarbinary";
    }

    public int getByteArraySQL() {
        return Types.LONGVARBINARY;
    }

    public String getColorType() {
        return "integer";
    }

    public int getColorSQL() {
        return Types.INTEGER;
    }

    public String getBitString(Boolean value) {
        return (value ? "1" : "0");
    }

    public int updateModel() {
        return 0;
    }

    // по умолчанию
    public String getClustered() {
        return "CLUSTERED ";
    }

    // у SQL сервера что-то гдючит ISNULL (а значит скорее всего и COALESCE) когда в подзапросе просто число указывается
    public boolean isNullSafe() {
        return true;
    }

    public String getCommandEnd() {
        return "";
    }

    public String getCreateSessionTable(String tableName, String declareString) {
        return "CREATE TEMPORARY TABLE " + tableName + " (" + declareString + ")";
    }

    public String getSessionTableName(String tableName) {
        return tableName;
    }

    public String getQueryName(String tableName, SessionTable.TypeStruct type, StringBuilder envString, boolean usedRecursion, EnsureTypeEnvironment typeEnv) {
        return getSessionTableName(tableName);
    }

    public boolean isGreatest() {
        return true;
    }

    public boolean useFJ() {
        return false;
    }

    public boolean orderUnion() {
        return false;
    }

    public String getDropSessionTable(String tableName) {
        return "DROP TABLE " + getSessionTableName(tableName);
    }

    public String getOrderDirection(boolean descending, boolean notNull) {
        return descending ? "DESC" : "ASC";
    }

    public boolean nullUnionTrouble() {
        return false;
    }

    public boolean inlineTrouble() {
        return false;
    }

    public boolean inlineSelfJoinTrouble() {
        return false;
    }

    public String getHour() {
        return "EXTRACT(HOUR FROM CURRENT_TIME)";
    }

    public String getMinute() {
        return "EXTRACT(MINUTE FROM CURRENT_TIME)";
    }

    public String getEpoch() {
        return "EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)";
    }

    public String getDateTime() {
        return "DATE_TRUNC('milliseconds', CURRENT_TIMESTAMP)";
    }

    public String getTypeChange(Type oldType, Type type, String name, MStaticExecuteEnvironment env) {
        throw new UnsupportedOperationException();
    }

    public String getInsensitiveLike() {
        return "LIKE";
    }

    public boolean supportGroupNumbers() {
        return false;
    }

    public String getCountDistinct(String field) {
        return "COUNT(DISTINCT " + field + ")";
    }

    public String getCount(String field) {
        return "COUNT(" + field + ")";
    }

    public boolean noMaxImplicitCast() {
        return false;
    }

    public boolean noDynamicSampling() {
        return false;
    }

    public void setLogLevel(int level) {
    }

    public boolean orderTopProblem() {
        throw new RuntimeException("unknown");
    }

    public String getConcTypeName(ConcatenateType type) {
        return "T" + genTypePostfix(type.getTypes(), type.getDesc());
    }

    public String getIIF(String ifWhere, String trueExpr, String falseExpr) {
        return "CASE WHEN " + ifWhere + " THEN " + trueExpr + " ELSE " + falseExpr + " END";
    }

    public String getAndExpr(String where, String expr, Type type, TypeEnvironment typeEnv) {
        return getIIF(where, expr, SQLSyntax.NULL);
    }

    public String getTableTypeName(SessionTable.TypeStruct tableType) {
        throw new UnsupportedOperationException();
    }

    public boolean noDynamicSQL() {
        throw new UnsupportedOperationException();
    }

    public boolean enabledCTE() {
        throw new UnsupportedOperationException();
    }

    public String getSafeCastNameFnc(Type type) {
        return genSafeCastName(type);
    }

    public boolean isDeadLock(SQLException e) {
        throw new UnsupportedOperationException();
    }

    public boolean isUpdateConflict(SQLException e) {
        throw new UnsupportedOperationException();
    }

    public boolean isUniqueViolation(SQLException e) {
        throw new UnsupportedOperationException();
    }

    public boolean isTableDoesNotExist(SQLException e) {
        return false;
    }

    public boolean isTimeout(SQLException e) {
        return false;
    }

    public String getRetryWithReason(SQLException e) {
        return null;
    }

    public String getRandom() {
        return "random()";
    }

    public boolean isTransactionCanceled(SQLException e) {
        throw new UnsupportedOperationException();
    }

    public boolean isConnectionClosed(SQLException e) {
        return false;
    }

    public boolean hasJDBCTimeoutMultiThreadProblem() {
        throw new UnsupportedOperationException();
    }

    public void setACID(Statement statement, boolean acid) throws SQLException {
    }

    public String getMetaName(String name) {
        return name;
    }

    public String getFieldName(String name) {
        return name;
    }

    public String getTableName(String name) {
        return name;
    }

    public String getGlobalTableName(String name) {
        return name;
    }

    public String getConstraintName(String name) {
        return name;
    }

    public String getIndexName(String name) {
        return name;
    }

    public boolean hasSelectivityProblem() {
        return false;
    }

    public String getAdjustSelectivityPredicate() {
        throw new UnsupportedOperationException();
    }

    public String getStringConcatenate() {
        return "+";
    }

    public String getArrayConcatenate(ArrayClass arrayClass, String prm1, String prm2, TypeEnvironment env) {
        throw new UnsupportedOperationException();
    }

    public String getArrayAgg(String s, ClassReader classReader, TypeEnvironment typeEnv) {
        throw new UnsupportedOperationException();
    }

    public boolean supportGroupSingleValue() {
        return true;
    }

    public String getAnyValueFunc() {
        throw new UnsupportedOperationException();
    }

    public String getStringCFunc() {
        throw new UnsupportedOperationException();
    }

    public String getLastFunc() {
        throw new UnsupportedOperationException();
    }

    public String getOrderGroupAgg(GroupType groupType, Type resultType, ImList<String> exprs, ImList<ClassReader> readers, ImOrderMap<String, CompileOrder> orders, TypeEnvironment typeEnv) {
        String orderClause = BaseUtils.clause("ORDER BY", Query.stringOrder(orders, this));

        String fnc;
        switch (groupType) {
            case STRING_AGG:
                fnc = "STRING_AGG";
                exprs = SumFormulaImpl.castToVarStrings(exprs, readers, resultType, this, typeEnv);
                break;
            case LAST:
                fnc = getLastFunc();
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return fnc + "(" + exprs.toString(",") + orderClause + ")";
    }

    public String getNotSafeConcatenateSource(ConcatenateType type, ImList<String> exprs, TypeEnvironment typeEnv) {
        throw new UnsupportedOperationException();
    }

    public boolean isIndexNameLocal() {
        throw new UnsupportedOperationException();
    }

    public String getParamUsage(int num) {
        throw new UnsupportedOperationException();
    }

    public String getRecursion(ImList<FunctionType> types, String recName, String initialSelect, String stepSelect, String stepSmallSelect, int smallLimit, String fieldDeclare, String outerParams, TypeEnvironment typeEnv) {
        throw new UnsupportedOperationException();
    }

    public String wrapSubQueryRecursion(String string) {
        return string;
    }

    public String getArrayConstructor(String source, ArrayClass rowType, TypeEnvironment env) {
        throw new UnsupportedOperationException();
    }

    public String getArrayType(ArrayClass arrayClass, TypeEnvironment typeEnv) {
        throw new UnsupportedOperationException();
    }

    public String getInArray(String element, String array) {
        throw new UnsupportedOperationException();
    }

    public boolean doesNotTrimWhenCastToVarChar() {
        throw new UnsupportedOperationException();
    }

    public boolean doesNotTrimWhenSumStrings() {
        throw new UnsupportedOperationException();
    }

    public boolean hasGroupByConstantProblem() {
        return false;
    }

    public String getRenameColumn(String table, String columnName, String newColumnName) {
        return "ALTER TABLE " + table + " RENAME " + columnName + " TO " + newColumnName;
    }

    public String getMaxMin(boolean max, String expr1, String expr2, Type type, TypeEnvironment typeEnv) {
        throw new UnsupportedOperationException();
    }

    // должно быть синхронизировано с StaticClass.isZero
    public String getNotZero(String expr, Type type, TypeEnvironment typeEnv) {
        throw new UnsupportedOperationException();
    }

    public boolean supportsAnalyzeSessionTable() {
        return false;
    }

    public String getAnalyzeSessionTable(String tableName) {
        throw new UnsupportedOperationException();
    }

    public boolean supportsDisableNestedLoop() {
        return false;
    }

    public boolean supportsNoCount() {
        return false;
    }

    public String getVolatileStats(boolean on) {
        throw new UnsupportedOperationException();
    }

    public String getChangeColumnType() {
        return "";
    }

    public SQLSyntaxType getSyntaxType() {
        throw new UnsupportedOperationException();
    }

    public Date fixDate(Date value) {
        return value;
    }

    public Timestamp fixDateTime(Timestamp value) {
        return value;
    }

    public boolean hasAggConcProblem() {
        return false;
    }

    public boolean hasNotNullIndexProblem() {
        return false;
    }

    public boolean hasNullWhereEstimateProblem() {
        return false;
    }

    public boolean hasTransactionSavepointProblem() { // если при ошибке откатить можно только всю транзакцию
        return false;
    }

    public String getAnalyze(String table) {
        return "ANALYZE " + table;
    }

    @Override
    public String getDeadlockPriority(Long priority) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean useFailedTimeInDeadlockPriority() {
        return false;
    }

    @Override
    public int getFloatingDivisionProblem() {
        return -1;
    }

    @Override
    public String getCancelActiveTaskQuery(Integer pid) {
        throw new UnsupportedOperationException();
    }
}
