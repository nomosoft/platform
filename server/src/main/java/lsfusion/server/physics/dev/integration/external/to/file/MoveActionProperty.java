package lsfusion.server.physics.dev.integration.external.to.file;

import com.google.common.base.Throwables;
import lsfusion.server.data.sql.exception.SQLHandledException;
import lsfusion.server.logics.UtilsLogicsModule;
import lsfusion.server.logics.action.controller.context.ExecutionContext;
import lsfusion.server.logics.classes.ValueClass;
import lsfusion.server.logics.property.classes.ClassPropertyInterface;
import lsfusion.server.physics.dev.integration.external.to.file.client.MoveFileClientAction;
import lsfusion.server.physics.dev.integration.internal.to.InternalAction;

import java.sql.SQLException;
import java.util.Iterator;

public class MoveActionProperty extends InternalAction {
    private final ClassPropertyInterface sourceInterface;
    private final ClassPropertyInterface destinationInterface;
    private final ClassPropertyInterface isClientInterface;

    public MoveActionProperty(UtilsLogicsModule LM, ValueClass... classes) {
        super(LM, classes);

        Iterator<ClassPropertyInterface> i = interfaces.iterator();
        sourceInterface = i.next();
        destinationInterface = i.next();
        isClientInterface = i.next();
    }

    public void executeCustom(ExecutionContext<ClassPropertyInterface> context) throws SQLException, SQLHandledException {
        try {
            String sourcePath = (String) context.getKeyValue(sourceInterface).getValue();
            String destinationPath = (String) context.getKeyValue(destinationInterface).getValue();
            boolean isClient = context.getKeyValue(isClientInterface).getValue() != null;
            if (sourcePath != null && destinationPath != null) {
                if (isClient) {
                    String result = (String) context.requestUserInteraction(new MoveFileClientAction(sourcePath, destinationPath));
                    if (result != null) {
                        throw new RuntimeException(String.format("Failed to move file from %s to %s", sourcePath, destinationPath));
                    }
                } else {
                    FileUtils.moveFile(sourcePath, destinationPath);
                }

            }
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    protected boolean allowNulls() {
        return true;
    }
}