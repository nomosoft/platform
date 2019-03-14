package lsfusion.server.physics.admin.service;

import lsfusion.interop.action.MessageClientAction;
import lsfusion.server.data.SQLHandledException;
import lsfusion.server.data.SQLSession;
import lsfusion.server.language.ScriptingAction;
import lsfusion.server.logics.property.classes.ClassPropertyInterface;
import lsfusion.server.logics.action.ExecutionContext;

import java.sql.SQLException;

import static lsfusion.server.base.context.ThreadLocalContext.localize;

public class PackActionProperty extends ScriptingAction {
    public PackActionProperty(ServiceLogicsModule LM) {
        super(LM);
    }

    @Override
    public void executeCustom(final ExecutionContext<ClassPropertyInterface> context) throws SQLException, SQLHandledException {
        
        ServiceDBActionProperty.run(context, new RunService() {
            public void run(SQLSession session, boolean isolatedTransaction) throws SQLException, SQLHandledException {
                context.getDbManager().packTables(session, context.getBL().LM.tableFactory.getImplementTables(), isolatedTransaction);
            }
        });
        context.delayUserInterfaction(new MessageClientAction(localize("{logics.tables.packing.completed}"), localize("{logics.tables.packing}")));
    }
}