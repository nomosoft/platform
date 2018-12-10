package lsfusion.gwt.server.form.handlers;

import lsfusion.gwt.server.form.FormServerResponseActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;
import lsfusion.gwt.server.LSFusionDispatchServlet;
import lsfusion.gwt.server.form.provider.FormSessionObject;
import lsfusion.gwt.server.convert.GwtToClientConverter;
import lsfusion.gwt.shared.actions.form.CollapseGroupObject;
import lsfusion.gwt.shared.actions.form.ServerResponseResult;

import java.io.IOException;

public class CollapseGroupObjectHandler extends FormServerResponseActionHandler<CollapseGroupObject> {
    private static GwtToClientConverter gwtConverter = GwtToClientConverter.getInstance();

    public CollapseGroupObjectHandler(LSFusionDispatchServlet servlet) {
        super(servlet);
    }

    @Override
    public ServerResponseResult executeEx(CollapseGroupObject action, ExecutionContext context) throws DispatchException, IOException {
        FormSessionObject form = getFormSessionObject(action.formSessionID);

        byte[] keyValues = gwtConverter.convertOrCast(action.value);

        return getServerResponseResult(form, form.remoteForm.collapseGroupObject(action.requestIndex, defaultLastReceivedRequestIndex, action.groupObjectId, keyValues));
    }
}
