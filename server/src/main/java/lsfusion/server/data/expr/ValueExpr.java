package lsfusion.server.data.expr;

import lsfusion.base.file.FileData;
import lsfusion.base.comb.map.GlobalObject;
import lsfusion.base.file.RawFileData;
import lsfusion.base.mutability.TwinImmutableObject;
import lsfusion.base.col.SetFact;
import lsfusion.base.col.interfaces.immutable.ImSet;
import lsfusion.base.col.interfaces.mutable.MExclSet;
import lsfusion.base.col.interfaces.mutable.MMap;
import lsfusion.base.col.interfaces.mutable.add.MAddSet;
import lsfusion.server.base.caches.ManualLazy;
import lsfusion.server.base.caches.hash.HashContext;
import lsfusion.server.classes.*;
import lsfusion.server.data.QueryEnvironment;
import lsfusion.server.data.Value;
import lsfusion.server.data.query.CompileSource;
import lsfusion.server.data.query.EnsureTypeEnvironment;
import lsfusion.server.data.query.JoinData;
import lsfusion.server.data.translator.MapTranslate;
import lsfusion.server.data.type.Type;
import lsfusion.server.data.type.TypeObject;
import lsfusion.server.data.where.Where;
import lsfusion.server.data.DataObject;
import lsfusion.server.data.ObjectValue;
import lsfusion.server.logics.classes.*;

import java.math.BigInteger;


public class ValueExpr extends AbstractValueExpr<ConcreteClass> implements Value {

    public final Object object;

    public Value removeBig(MAddSet<Value> usedValues) {
        if(objectClass instanceof DynamicFormatFileClass && ((FileData)object).getLength() > 1000) {
            int i=usedValues.size();
            while(true) {
                Value removeValue = new ValueExpr(new FileData(new RawFileData(new BigInteger(""+(i++)).toByteArray()), ""), (DynamicFormatFileClass)objectClass);
                if(!usedValues.contains(removeValue))
                    return removeValue;
            }
        }
        if(objectClass instanceof StaticFormatFileClass && ((RawFileData)object).getLength() > 1000) {
            int i=usedValues.size();
            while(true) {
                Value removeValue = new ValueExpr(new RawFileData(new BigInteger(""+(i++)).toByteArray()), (StaticFormatFileClass)objectClass);
                if(!usedValues.contains(removeValue))
                    return removeValue;
            }
        }
        return null;
    }

    public <T> ValueExpr(T object, DataClass<T> dataClass) {
        this((Object)object, dataClass);
    }
    public ValueExpr(Long object, ConcreteObjectClass objectClass) {
        this((Object)object, (ConcreteClass)objectClass);
    }
    public ValueExpr(Integer object, ConcreteObjectClass objectClass) {
        this((Object)object, (ConcreteClass)objectClass);
        throw new UnsupportedOperationException();// should be long
    }

    @Override
    public String toDebugString() {
        return toString();
    }

    public ValueExpr(Object object, ConcreteClass objectClass) {
        super(objectClass);

        this.object = object;

        assert objectClass.getType().read(object).equals(object); // чтобы читалось то что писалось
    }

    public static StaticValueExpr TRUE = new StaticValueExpr(true,LogicalClass.instance);
    public static Expr get(Where where) {
        return TRUE.and(where);
    }

    public static IntegralClass COUNTCLASS = IntegerClass.instance;
    public static StaticValueExpr COUNT = new StaticValueExpr(1, COUNTCLASS);

    public String getSource(CompileSource compile, boolean needValue) {
        String result = compile.params.get(this);

        // регистрируем тип заранее в env, потому как при парсинге он только проверяется
        Type type = objectClass.getType();
        if (!type.isSafeType())
            type.getCast(result, compile.syntax, compile.env);

        return result;
    }

    public Type getType(KeyType keyType) {
        return getType();
    }

    public void fillAndJoinWheres(MMap<JoinData, Where> joins, Where andWhere) {
    }

    public boolean calcTwins(TwinImmutableObject o) {
        return object.equals(((ValueExpr)o).object) && objectClass.equals(((ValueExpr)o).objectClass);
    }

    @Override
    public int immutableHashCode() {
        return object.hashCode()*31+objectClass.hashCode();
    }

    protected int hash(HashContext hashContext) {
        return hashContext.values.hash(this);
    }

    // нельзя потому как при трансляции значения потеряются
/*    @Override
    public ValueExpr scale(int mult) {
        return new ValueExpr(((IntegralClass)objectClass).multiply((Number) object,mult),objectClass);
    }*/

    protected ValueExpr translate(MapTranslate translator) {
        return translator.translate(this);
    }

    public ImSet<Value> getValues() {
        return SetFact.<Value>singleton(this);
    }

    public static Value ZERO = new ValueExpr(0.0, DoubleClass.instance);
    public static Value TRUEVAL = new ValueExpr(true, LogicalClass.instance);

    private static ImSet<Value> staticExprs;
    private static ImSet<Value> getStaticExprs() {
        if(staticExprs == null) {
            MExclSet<Value> mStaticExprs = SetFact.mExclSet(4);
            mStaticExprs.exclAdd(ValueExpr.ZERO);
            mStaticExprs.exclAdd(ValueExpr.TRUEVAL);
            staticExprs = mStaticExprs.immutable();
        }
        return staticExprs;
    }

    public static ImSet<? extends Value> removeStatic(ImSet<? extends Value> col) {
        return SetFact.remove(col, getStaticExprs());
    }

    // пересечение с игнорированием ValueExpr.TRUE
    public static boolean noStaticContains(ImSet<? extends Value> col1, ImSet<? extends Value> col2) {
        return ((ImSet<Value>)removeStatic(col1)).containsAll(removeStatic(col2));
    }

    public TypeObject getParseInterface(QueryEnvironment env, EnsureTypeEnvironment typeEnv) {
        return new TypeObject(object, objectClass.getType());
    }

    public GlobalObject getValueClass() {
        return objectClass;
    }

    @Override
    public String toString() {
        return object + " - " + objectClass;
    }

    private DataObject dataObject;
    @ManualLazy
    public DataObject getDataObject() { // по сути множественное наследование, поэтому ManualLazy
        if(dataObject==null)
            dataObject = new DataObject(this);
        return dataObject;
    }
    public ValueExpr(DataObject dataObject) {
        this(dataObject.object, dataObject.objectClass);
        this.dataObject = dataObject;
    }

    @Override
    public ObjectValue getObjectValue(QueryEnvironment env) {
        return getDataObject();
    }

    @Override
    public int getStaticEqualClass() {
        return 0;
    }
}
