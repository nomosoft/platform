package lsfusion.server.logics.tasks;

import java.util.HashSet;
import java.util.Set;

public abstract class SingleProgramTask extends ProgramTask {

    protected SingleProgramTask() {
        dependsToProceed = 0;
    }
    
    public long runTime = 0;

    public Set<Task> getAllDependencies() {
        throw new UnsupportedOperationException();
    }
}
