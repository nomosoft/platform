package lsfusion.server.logics.action.init;

import lsfusion.server.logics.action.Action;
import lsfusion.server.logics.action.flow.ListCaseAction;
import lsfusion.server.logics.property.oraction.ActionOrProperty;
import lsfusion.server.logics.property.init.GroupPropertiesTask;

public class FinalizeActionAbstractTask extends GroupPropertiesTask {
    public String getCaption() {
        return "Finalizing abstract actions";
    }

    protected void runTask(ActionOrProperty property) {
        if (property instanceof Action) {
            if (property instanceof ListCaseAction && ((ListCaseAction) property).isAbstract()) {
                property.finalizeInit();
            }
        }
    }
}
