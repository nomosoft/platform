package lsfusion.server.logics.navigator;

import lsfusion.server.logics.form.interactive.action.async.AsyncExec;
import lsfusion.server.physics.dev.i18n.LocalizedString;

public class NavigatorFolder extends NavigatorElement {
    public NavigatorFolder(String canonicalName, LocalizedString caption) {
        super(canonicalName, caption);
    }

    @Override
    protected String getDefaultIcon(boolean top) {
        return top ? "openTop.png" : "open.png";
    }

    @Override
    public boolean isLeafElement() {
        return false;
    }

    @Override
    public byte getTypeID() {
        return 1;
    }

    @Override
    public AsyncExec getAsyncExec() {
        return null;
    }
}
