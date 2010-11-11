package platform.server.data.expr.cases;

import platform.server.data.where.Where;

public abstract class CaseWhereInterface<C> {
    public abstract Where getWhere(C cCase);

    public Where getElse() {
        return Where.FALSE;
    }
}
