package platform.client.remote.proxy;

import platform.interop.ClassViewType;
import platform.interop.action.ClientApply;
import platform.interop.form.RemoteChanges;
import platform.interop.form.RemoteDialogInterface;
import platform.interop.form.RemoteFormInterface;
import platform.interop.remote.MethodInvocation;

import java.rmi.RemoteException;
import java.util.*;

public class RemoteFormProxy<T extends RemoteFormInterface>
        extends RemoteObjectProxy<T>
        implements RemoteFormInterface {

    public static final Map<String, byte[]> cachedRichDesign = new HashMap<String, byte[]>();
    public static void dropCaches() {
        cachedRichDesign.clear();
    }

    public RemoteFormProxy(T target) {
        super(target);
    }

    public byte[] getReportDesignsByteArray(boolean toExcel) throws RemoteException {
        logRemoteMethodStartCall("getReportDesignsByteArray");
        byte[] result = target.getReportDesignsByteArray(toExcel);
        logRemoteMethodEndCall("getReportDesignsByteArray", result);
        return result;
    }

    public byte[] getSingleGroupReportDesignByteArray(boolean toExcel, int groupId) throws RemoteException {
        logRemoteMethodStartCall("getSingleGroupReportDesignByteArray");
        byte[] result = target.getSingleGroupReportDesignByteArray(toExcel, groupId);
        logRemoteMethodEndCall("getSingleGroupReportDesignByteArray", result);
        return result;
    }

    public byte[] getReportSourcesByteArray() throws RemoteException {
        logRemoteMethodStartCall("getReportSourcesByteArray");
        byte[] result = target.getReportSourcesByteArray();
        logRemoteMethodEndCall("getReportSourcesByteArray", result);
        return result;
    }

    public byte[] getSingleGroupReportSourcesByteArray(int groupId) throws RemoteException {
        logRemoteMethodStartCall("getSingleGroupReportSourcesByteArray");
        byte[] result = target.getSingleGroupReportSourcesByteArray(groupId);
        logRemoteMethodEndCall("getSingleGroupReportSourcesByteArray", result);
        return result;
    }

    @Override
    public Map<String, String> getReportPath(boolean toExcel, Integer groupId) throws RemoteException {
        Map<String, String> result = target.getReportPath(toExcel, groupId);
        return result;
    }

    public byte[] getReportHierarchyByteArray() throws RemoteException {
        logRemoteMethodStartCall("getReportHierarchyByteArray");
        byte[] result = target.getReportHierarchyByteArray();
        logRemoteMethodEndCall("getReportHierarchyByteArray", result);
        return result;
    }

    public byte[] getSingleGroupReportHierarchyByteArray(int groupId) throws RemoteException {
        logRemoteMethodStartCall("getSingleGroupReportHierarchyByteArray");
        byte[] result = target.getSingleGroupReportHierarchyByteArray(groupId);
        logRemoteMethodEndCall("getSingleGroupReportHierarchyByteArray", result);
        return result;
    }

    public RemoteChanges getRemoteChanges() throws RemoteException {
        logRemoteMethodStartCall("getRemoteChanges");
        RemoteChanges result = target.getRemoteChanges();
        logRemoteMethodEndCall("getRemoteChanges", result);
        return result;
    }

    @ImmutableMethod
    public byte[] getRichDesignByteArray() throws RemoteException {
        logRemoteMethodStartCall("getRichDesignByteArray");
        byte[] result = target.getRichDesignByteArray();
        logRemoteMethodEndCall("getRemoteChanges", result);
        return result;
    }

    @PendingRemoteMethod
    public void changePageSize(int groupID, Integer pageSize) throws RemoteException {
        logRemoteMethodStartVoidCall("changePageSize");
        target.changePageSize(groupID, pageSize);
        logRemoteMethodEndVoidCall("changePageSize");
    }

    @PendingRemoteMethod
    public void gainedFocus() throws RemoteException {
        logRemoteMethodStartVoidCall("gainedFocus");
        target.gainedFocus();
        logRemoteMethodEndVoidCall("gainedFocus");
    }

    @PendingRemoteMethod
    public void changeGroupObject(int groupID, byte[] value) throws RemoteException {
        logRemoteMethodStartCall("changeGroupObject");
        target.changeGroupObject(groupID, value);
        logRemoteMethodEndVoidCall("changeGroupObject");
    }

    @PendingRemoteMethod
    public void changeGroupObject(int groupID, byte changeType) throws RemoteException {
        logRemoteMethodStartVoidCall("changeGroupObject");
        target.changeGroupObject(groupID, changeType);
        logRemoteMethodEndVoidCall("changeGroupObject");
    }

    @PendingRemoteMethod
    public void changePropertyDraw(int propertyID, byte[] columnKey, byte[] object, boolean all, boolean aggValue) throws RemoteException {
        logRemoteMethodStartCall("changePropertyDraw");
        target.changePropertyDraw(propertyID, columnKey, object, all, aggValue);
        logRemoteMethodEndVoidCall("changePropertyDraw");
    }

    @PendingRemoteMethod
    public void groupChangePropertyDraw(int mainID, byte[] mainColumnKey, int getterID, byte[] getterColumnKey) throws RemoteException {
        logRemoteMethodStartCall("groupChangePropertyDraw");
        target.groupChangePropertyDraw(mainID, mainColumnKey, getterID, getterColumnKey);
        logRemoteMethodEndVoidCall("groupChangePropertyDraw");
    }

    @PendingRemoteMethod
    public void pasteExternalTable(List<Integer> propertyIDs, List<List<Object>> table) throws RemoteException {
        logRemoteMethodStartCall("pasteExternalTable");
        target.pasteExternalTable(propertyIDs, table);
        logRemoteMethodEndVoidCall("pasteExternalTable");
    }

    @PendingRemoteMethod
    public void pasteMulticellValue(Map<Integer, List<Map<Integer, Object>>> cells, Object value) throws RemoteException {
        logRemoteMethodStartCall("pasteMulticellValue");
        target.pasteMulticellValue(cells, value);
        logRemoteMethodEndVoidCall("pasteMulticellValue");
    }

    public boolean[] getCompatibleProperties(int mainPropertyID, int[] propertiesIDs) throws RemoteException {
        logRemoteMethodStartCall("getCompatibleProperties");
        boolean[] result = target.getCompatibleProperties(mainPropertyID, propertiesIDs);
        logRemoteMethodEndCall("getCompatibleProperties", result);
        return result;
    }

    public Object getPropertyChangeValue(int propertyID) throws RemoteException {
        logRemoteMethodStartCall("getPropertyChangeValue");
        Object result = target.getPropertyChangeValue(propertyID);
        logRemoteMethodEndCall("getPropertyChangeValue", result);
        return result;
    }

    public boolean canChangeClass(int objectID) throws RemoteException {
        logRemoteMethodStartCall("canChangeClass");
        boolean result = target.canChangeClass(objectID);
        logRemoteMethodEndCall("canChangeClass", result);
        return result;
    }

    @PendingRemoteMethod
    public void changeGridClass(int objectID, int idClass) throws RemoteException {
        logRemoteMethodStartVoidCall("changeGridClass");
        target.changeGridClass(objectID, idClass);
        logRemoteMethodEndVoidCall("changeGridClass");
    }

    @PendingRemoteMethod
    public void switchClassView(int groupID) throws RemoteException {
        logRemoteMethodStartVoidCall("switchClassView");
        target.switchClassView(groupID);
        logRemoteMethodEndVoidCall("switchClassView");
    }

    @PendingRemoteMethod
    public void changeClassView(int groupID, ClassViewType classView) throws RemoteException {
        logRemoteMethodStartVoidCall("changeClassView");
        target.changeClassView(groupID, classView);
        logRemoteMethodEndVoidCall("changeClassView");
    }

    @PendingRemoteMethod
    public void changeClassView(int groupID, String classViewName) throws RemoteException {
        logRemoteMethodStartVoidCall("changeClassView");
        target.changeClassView(groupID, classViewName);
        logRemoteMethodEndVoidCall("changeClassView");
    }

    @PendingRemoteMethod
    public void changePropertyOrder(int propertyID, byte modiType, byte[] columnKeys) throws RemoteException {
        logRemoteMethodStartVoidCall("changePropertyOrder");
        target.changePropertyOrder(propertyID, modiType, columnKeys);
        logRemoteMethodEndVoidCall("changePropertyOrder");
    }

    @PendingRemoteMethod
    public void clearUserFilters() throws RemoteException {
        logRemoteMethodStartVoidCall("clearUserFilters");
        target.clearUserFilters();
        logRemoteMethodEndVoidCall("clearUserFilters");
    }

    @PendingRemoteMethod
    public void addFilter(byte[] state) throws RemoteException {
        logRemoteMethodStartVoidCall("addFilter");
        target.addFilter(state);
        logRemoteMethodEndVoidCall("addFilter");
    }

    @PendingRemoteMethod
    public void setRegularFilter(int groupID, int filterID) throws RemoteException {
        logRemoteMethodStartVoidCall("setRegularFilter");
        target.setRegularFilter(groupID, filterID);
        logRemoteMethodEndVoidCall("setRegularFilter");
    }

    public int countRecords(int groupObjectID) throws RemoteException {
        logRemoteMethodStartCall("countRecords");
        int result = target.countRecords(groupObjectID);
        logRemoteMethodEndCall("countRecords", result);
        return result;
    }

    public Object calculateSum(int propertyID, byte[] columnKeys) throws RemoteException {
        logRemoteMethodStartCall("calculateSum");
        Object result = target.calculateSum(propertyID, columnKeys);
        logRemoteMethodEndCall("calculateSum", result);
        return result;
    }

    public Map<List<Object>, List<Object>> groupData(Map<Integer, List<byte[]>> groupMap, Map<Integer, List<byte[]>> sumMap,
                                                     Map<Integer, List<byte[]>> maxMap, boolean onlyNotNull) throws RemoteException {
        logRemoteMethodStartCall("groupData");
        Map<List<Object>, List<Object>> result = target.groupData(groupMap, sumMap, maxMap, onlyNotNull);
        logRemoteMethodEndCall("groupData", result);
        return result;
    }

    @ImmutableMethod
    public String getSID() throws RemoteException {
        logRemoteMethodStartCall("getSID");
        String result = target.getSID();
        logRemoteMethodEndCall("getSID", result);
        return result;
    }

    @PendingRemoteMethod
    public void refreshData() throws RemoteException {
        logRemoteMethodStartCall("refreshData");
        target.refreshData();
    }

    @ImmutableMethod
    public boolean hasClientApply() throws RemoteException {
        logRemoteMethodStartCall("hasClientApply");
        return target.hasClientApply();
    }

    public ClientApply checkClientChanges() throws RemoteException {
        logRemoteMethodStartCall("applyClientChanges");
        return target.checkClientChanges();
    }

    @PendingRemoteMethod
    public void applyChanges(Object clientResult) throws RemoteException {
        logRemoteMethodStartVoidCall("applyChanges");
        target.applyChanges(clientResult);
        logRemoteMethodEndVoidCall("applyChanges");
    }

    @PendingRemoteMethod
    public void okPressed() throws RemoteException {
        logRemoteMethodStartVoidCall("okPressed");
        target.okPressed();
        logRemoteMethodEndVoidCall("okPressed");
    }

    @PendingRemoteMethod
    public void closedPressed() throws RemoteException {
        logRemoteMethodStartVoidCall("closedPressed");
        target.closedPressed();
        logRemoteMethodEndVoidCall("closedPressed");
    }

    @PendingRemoteMethod
    public void continueAutoActions() throws RemoteException {
        logRemoteMethodStartVoidCall("continueAutoActions");
        target.continueAutoActions();
        logRemoteMethodEndVoidCall("continueAutoActions");
    }

    @PendingRemoteMethod
    public void cancelChanges() throws RemoteException {
        logRemoteMethodStartVoidCall("cancelChanges");
        target.cancelChanges();
        logRemoteMethodEndVoidCall("cancelChanges");
    }

    @PendingRemoteMethod
    public void expandGroupObject(int groupId, byte[] treePath) throws RemoteException {
        logRemoteMethodStartVoidCall("expandTreeNode");
        target.expandGroupObject(groupId, treePath);
        logRemoteMethodEndVoidCall("expandTreeNode");
    }

    @PendingRemoteMethod
    public void moveGroupObject(int parentGroupId, byte[] parentKey, int childGroupId, byte[] childKey, int index) throws RemoteException {
        logRemoteMethodStartVoidCall("moveGroupObject");
        target.moveGroupObject(parentGroupId, parentKey, childGroupId, childKey, index);
        logRemoteMethodEndVoidCall("moveGroupObject");
    }

    public byte[] getPropertyChangeType(int propertyID, byte[] columnKey, boolean aggValue) throws RemoteException {
        logRemoteMethodStartCall("getPropertyChangeType");
        byte[] result = target.getPropertyChangeType(propertyID, columnKey, aggValue);
        logRemoteMethodEndCall("getPropertyChangeType", result);
        return result;
    }

    @NonFlushRemoteMethod
    private RemoteDialogInterface createDialog(String methodName, Object... args) throws RemoteException {
        List<MethodInvocation> invocations = getImmutableMethodInvocations(RemoteDialogProxy.class);

        MethodInvocation creator = MethodInvocation.create(this.getClass(), methodName, args);

        Object[] result = createAndExecute(creator, invocations.toArray(new MethodInvocation[invocations.size()]));

        RemoteDialogInterface remoteDialog = (RemoteDialogInterface) result[0];
        if (remoteDialog == null) {
            return null;
        }

        RemoteDialogProxy proxy = new RemoteDialogProxy(remoteDialog);
        for (int i = 0; i < invocations.size(); ++i) {
            proxy.setProperty(invocations.get(i).name, result[i + 1]);
        }

        return proxy;
    }

    @NonPendingRemoteMethod
    public RemoteDialogInterface createClassPropertyDialog(int viewID, int value) throws RemoteException {
        return createDialog("createClassPropertyDialog", viewID, value);
    }

    @NonPendingRemoteMethod
    public RemoteDialogInterface createEditorPropertyDialog(int viewID) throws RemoteException {
        return createDialog("createEditorPropertyDialog", viewID);
    }

    @NonPendingRemoteMethod
    public RemoteDialogInterface createObjectEditorDialog(int viewID) throws RemoteException {
        return createDialog("createObjectEditorDialog", viewID);
    }

    public String getRemoteActionMessage() throws RemoteException {
        return target.getRemoteActionMessage();
    }

    @Override
    public void adjustGroupObject(int groupID, byte[] value) throws RemoteException {
        logRemoteMethodStartVoidCall("seekObject");
        target.adjustGroupObject(groupID, value);
        logRemoteMethodEndVoidCall("seekObject");
    }
}
