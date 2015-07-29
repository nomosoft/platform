package lsfusion.server.logics.tasks.impl;

import lsfusion.base.col.SetFact;
import lsfusion.base.col.interfaces.immutable.ImSet;
import lsfusion.server.data.SQLHandledException;
import lsfusion.server.data.SQLSession;
import lsfusion.server.logics.property.AggregateProperty;
import lsfusion.server.logics.property.ExecutionContext;
import lsfusion.server.logics.property.Property;
import lsfusion.server.logics.tasks.GroupPropertiesSingleTask;
import lsfusion.server.logics.tasks.PublicTask;
import org.antlr.runtime.RecognitionException;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;

import static lsfusion.base.BaseUtils.serviceLogger;

public class CheckAggregationsTask extends GroupPropertiesSingleTask{

    public void init(ExecutionContext context) throws SQLException, SQLHandledException {
        setBL(context.getBL());
        initTasks();
        setDependencies(new HashSet<PublicTask>());
    }

    @Override
    protected void runTask(final Property property) throws RecognitionException {
        try {
            if (property instanceof AggregateProperty) {
                final SQLSession sqlSession = getBL().getDbManager().getThreadLocalSql();
                long start = System.currentTimeMillis();
                ((AggregateProperty) property).checkAggregation(sqlSession, getBL().LM.baseClass);
                long time = System.currentTimeMillis() - start;
                serviceLogger.info(String.format("Check Aggregations: %s, %sms", property.getSID(), time));

            }
        } catch (SQLException | SQLHandledException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected List getElements() {
        return getBL().getAggregateStoredProperties();
    }

    @Override
    protected String getErrorsDescription(Property element) {
        return "";
    }

    @Override
    protected ImSet<Property> getDependElements(Property key) {
        return SetFact.EMPTY();
    }
}
