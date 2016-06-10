package lsfusion.server.data;

import lsfusion.base.MutableObject;
import lsfusion.server.ServerLoggers;

import java.sql.SQLException;

// local (not remote) object with SQL resources 
public abstract class MutableClosedObject<O> extends MutableObject implements AutoCloseable {

    private boolean closed;
    @AssertSynchronized
    public void close(O owner) throws SQLException {
        ServerLoggers.assertLog(!closed, "ALREADY CLOSED " + this);
        shutdown(owner, true);
    }
    
    protected boolean isClosed() {
        return closed;
    }

    @Override
    public void close() throws SQLException {
        close(null);
    }

    private void shutdown(O owner, boolean explicit) throws SQLException {
        if(closed)
            return;
        if(explicit)
            onExplicitClose(owner);
        onFinalClose(owner);
        closed = true;
    }
    
    public O getFinalizeOwner() {
        return null;
    }
    
    // явная очистка ресурсов, которые поддерживаются через weak ref'ы
    protected void onExplicitClose(O owner) throws SQLException {
    }

    // все кроме weakRef (onExplicitClose)
    protected void onFinalClose(O owner) throws SQLException {
    }

    protected void finalize() throws Throwable {
        try
        {
            shutdown(getFinalizeOwner(), false);
        }
        catch (SQLException e)
        {
        }
        finally
        {
            super.finalize();
        }
    }
}
