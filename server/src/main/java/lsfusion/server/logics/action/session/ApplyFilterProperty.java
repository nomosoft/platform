package lsfusion.server.logics.action.session;

import lsfusion.server.data.SQLHandledException;
import lsfusion.server.logics.BaseLogicsModule;
import lsfusion.server.logics.property.classes.ClassPropertyInterface;
import lsfusion.server.logics.action.ExecutionContext;
import lsfusion.server.language.ScriptingAction;

import java.sql.SQLException;

public class ApplyFilter extends ScriptingAction {

    private final ApplyFilter type;

    public ApplyFilterProperty(BaseLogicsModule lm, ApplyFilter type) {
        super(lm);
        this.type = type;
    }

    @Override
    protected void executeCustom(ExecutionContext<ClassPropertyInterface> context) throws SQLException, SQLHandledException {
        context.getSession().setApplyFilter(type);
    }
}
