package lsfusion.server.logics.navigator;

import lsfusion.server.logics.action.Action;
import lsfusion.server.logics.form.struct.FormEntity;
import lsfusion.server.logics.form.interactive.action.async.AsyncExec;
import lsfusion.server.physics.dev.i18n.LocalizedString;

public class NavigatorAction extends NavigatorElement {
    private final Action<?> action;
    private final FormEntity form;

    public NavigatorAction(Action<?> action, String canonicalName, LocalizedString caption, FormEntity form) {
        super(canonicalName, caption);
        
        this.action = action;
        this.form = form;
    }

    @Override
    protected String getDefaultIcon(boolean top) {
        if(form != null)
            return top ? "formTop.png" : "form.png";
        return top ? "actionTop.png" : "action.png";
    }

    @Override
    public boolean isLeafElement() {
        return true;
    }

    @Override
    public byte getTypeID() {
        return 2;
    }

    @Override
    public AsyncExec getAsyncExec() {
        return Action.getAsyncExec(action.getAsyncEventExec(false));
    }

    public FormEntity getForm() {
        return form;
    }

    public Action getAction() {
        return action;
    }
}