package platform.server;

public class Settings {

    public static Settings instance;

    // обозначает что компилятор будет проталкивать внутрь группирующих подзапросов, общее условие (если оно маленькое и сам группирующий подзапрос в нем не учавствует)
    private boolean pushGroupWhere = false;

    // обозначает что компилятор будет проталкивать внутрь order подзапросов, общее условие
    private boolean pushOrderWhere = false;

    // обозначает что при проверке условия на TRUE не будет преобразовывать A cmp B в 3 противоположных NOT'а как правильно, а будет использовать эвристику
    private boolean simpleCheckCompare = true;

    // обозначает что на следствия (и отрицания) условия будет проверять когда остались только термы, не делая этого на промежуточных уровнях
    private boolean checkFollowsWhenObjects = false;

    // будет ли оптимизатор пытаться перестраивать условия по правилу X OR (Y AND Z) и X=>Y, то Y AND (X OR Z)
    private boolean restructWhereOnMeans = false;

    // будет ли оптимизатор разбивать группирующие выражения, чтобы не было FULL JOIN и UNION ALL 
    public static boolean SPLIT_GROUP_INNER_JOINS(boolean max) {
        return max;
    }

    // будет ли оптимизатор разбивать группирующие выражения на максимум, так чтобы в группируемом выражении не было бы Case'ов 
    private boolean splitGroupMaxExprcases = true;

    // будет ли высчитываться что именно изменилось в группирующих свойствах или же будет считаться что изменилось все
    private boolean calculateGroupDataChanged = false;

    // не использовать инкрементную логику в группирующем свойстве на максимум
    private boolean noIncrementMaxGroupProperty = true;

    public boolean isPushGroupWhere() {
        return pushGroupWhere;
    }

    public void setPushGroupWhere(boolean pushGroupWhere) {
        this.pushGroupWhere = pushGroupWhere;
    }

    public boolean isPushOrderWhere() {
        return pushOrderWhere;
    }

    public void setPushOrderWhere(boolean pushOrderWhere) {
        this.pushOrderWhere = pushOrderWhere;
    }

    public boolean isSimpleCheckCompare() {
        return simpleCheckCompare;
    }

    public void setSimpleCheckCompare(boolean simpleCheckCompare) {
        this.simpleCheckCompare = simpleCheckCompare;
    }

    public boolean isCheckFollowsWhenObjects() {
        return checkFollowsWhenObjects;
    }

    public void setCheckFollowsWhenObjects(boolean checkFollowsWhenObjects) {
        this.checkFollowsWhenObjects = checkFollowsWhenObjects;
    }

    public boolean isRestructWhereOnMeans() {
        return restructWhereOnMeans;
    }

    public void setRestructWhereOnMeans(boolean restructWhereOnMeans) {
        this.restructWhereOnMeans = restructWhereOnMeans;
    }

    public boolean isSplitGroupMaxExprcases() {
        return splitGroupMaxExprcases;
    }

    public void setSplitGroupMaxExprcases(boolean splitGroupMaxExprcases) {
        this.splitGroupMaxExprcases = splitGroupMaxExprcases;
    }

    public boolean isCalculateGroupDataChanged() {
        return calculateGroupDataChanged;
    }

    public void setCalculateGroupDataChanged(boolean calculateGroupDataChanged) {
        this.calculateGroupDataChanged = calculateGroupDataChanged;
    }

    public boolean isNoIncrementMaxGroupProperty() {
        return noIncrementMaxGroupProperty;
    }

    public void setNoIncrementMaxGroupProperty(boolean noIncrementMaxGroupProperty) {
        this.noIncrementMaxGroupProperty = noIncrementMaxGroupProperty;
    }
}
