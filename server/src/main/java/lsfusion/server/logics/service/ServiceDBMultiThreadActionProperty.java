package lsfusion.server.logics.service;

import lsfusion.interop.action.MessageClientAction;
import lsfusion.server.data.SQLHandledException;
import lsfusion.server.data.SQLSession;
import lsfusion.server.logics.DBManager;
import lsfusion.server.logics.ServiceLogicsModule;
import lsfusion.server.logics.property.ClassPropertyInterface;
import lsfusion.server.logics.property.ExecutionContext;
import lsfusion.server.logics.scripted.ScriptingActionProperty;
import lsfusion.server.session.DataSession;
import lsfusion.server.session.SessionCreator;

import java.sql.SQLException;

import static lsfusion.server.logics.ServerResourceBundle.getString;

public class ServiceDBMultiThreadActionProperty extends ScriptingActionProperty {
    public ServiceDBMultiThreadActionProperty(ServiceLogicsModule LM) {
        super(LM);
    }

    @Override
    public void executeCustom(final ExecutionContext<ClassPropertyInterface> context) throws SQLException, SQLHandledException {

        run(context, new RunService() {
            public void run(SQLSession session, boolean isolatedTransaction) throws SQLException, SQLHandledException {
                context.getBL().serviceLM.recalculateMultiThreadAction.execute(context);
            }});

        run(context, new RunService() {
            public void run(SQLSession session, boolean isolatedTransaction) throws SQLException, SQLHandledException {
                context.getBL().serviceLM.recalculateClassesMultiThreadAction.execute(context);
            }
        });

        context.getDbManager().analyzeDB(context.getSession().sql);

        run(context, new RunService() {
            public void run(SQLSession session, boolean isolatedTransaction) throws SQLException, SQLHandledException {
                context.getBL().serviceLM.recalculateFollowsMultiThreadAction.execute(context);
            }
        });

        run(context, new RunService() {
            public void run(SQLSession session, boolean isolatedTransaction) throws SQLException, SQLHandledException {
                context.getBL().reflectionLM.recalculateStatsMultiThreadAction.execute(context);
            }
        });

        context.delayUserInterfaction(new MessageClientAction(getString("logics.service.db.completed"), getString("logics.service.db")));
    }

    @Override
    protected boolean isVolatile() {
        return true;
    }
    
    public static boolean singleTransaction(ExecutionContext context) throws SQLException, SQLHandledException {
        return context.getBL().serviceLM.singleTransaction.read(context)!=null;
    }

    public static void runData(ExecutionContext<?> context, final RunServiceData run) throws SQLException, SQLHandledException {
        final boolean singleTransaction = singleTransaction(context);
        DBManager.runData(context, singleTransaction, new DBManager.RunServiceData() {
            public void run(SessionCreator session) throws SQLException, SQLHandledException {
                run.run(session, !singleTransaction);
            }
        });
    }

    public static void run(ExecutionContext<?> context, final RunService run) throws SQLException, SQLHandledException {
        // транзакция в Service Action'ах не особо нужна, так как действия атомарные
        final boolean singleTransaction = singleTransaction(context); 
        SQLSession sql = context.getSession().sql;
        DBManager.run(sql, singleTransaction, new DBManager.RunService() {
            public void run(SQLSession sql) throws SQLException, SQLHandledException {
                run.run(sql, !singleTransaction);
            }});
    }
}