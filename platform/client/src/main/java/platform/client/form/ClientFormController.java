package platform.client.form;

import platform.base.BaseUtils;
import platform.base.OrderedMap;
import platform.base.identity.DefaultIDGenerator;
import platform.base.identity.IDGenerator;
import platform.client.*;
import platform.client.form.tree.TreeGroupController;
import platform.client.logics.*;
import platform.client.logics.classes.ClientObjectClass;
import platform.client.logics.filter.ClientPropertyFilter;
import platform.client.navigator.ClientNavigator;
import platform.client.remote.proxy.RemoteObjectProxy;
import platform.client.serialization.ClientSerializationPool;
import platform.interop.ClassViewType;
import platform.interop.KeyStrokes;
import platform.interop.Order;
import platform.interop.Scroll;
import platform.interop.action.*;
import platform.interop.form.RemoteChanges;
import platform.interop.form.RemoteFormInterface;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static platform.interop.Order.*;

public class ClientFormController {

    private final ClientForm form;

    public RemoteFormInterface remoteForm;
    public final ClientNavigator clientNavigator;
    public final ClientFormActionDispatcher actionDispatcher;

    // здесь хранится список всех GroupObjects плюс при необходимости null
//    private List<ClientGroupObject> groupObjects;

    private static IDGenerator idGenerator = new DefaultIDGenerator();
    private int ID;

    private ClientFormLayout formLayout;

    public Map<ClientGroupObject, GroupObjectController> controllers;
    private Map<ClientTreeGroup, TreeGroupController> treeControllers;

    private JButton buttonApply;
    private JButton buttonCancel;

    public boolean dataChanged;

    public final Map<ClientGroupObject, List<ClientGroupObjectValue>> currentGridObjects = new HashMap<ClientGroupObject, List<ClientGroupObjectValue>>();

    public boolean isDialog() {
        return false;
    }

    public boolean isModal() {
        return false;
    }

    public boolean isNewSession() {
        return true;
    }

    public boolean isReadOnlyMode() {
        return form.readOnly;
    }

    public int getID() {
        return ID;
    }

    public KeyStroke getKeyStroke() {
        return form.keyStroke;
    }

    public String getCaption() {
        return form.caption;
    }

    public String getFullCaption() {
        return form.getFullCaption();
    }

    public ClientFormController(RemoteFormInterface remoteForm, ClientNavigator clientNavigator) throws IOException, ClassNotFoundException {

        ID = idGenerator.idShift();

        // Форма нужна, чтобы с ней общаться по поводу данных и прочих
        this.remoteForm = remoteForm;

        // Навигатор нужен, чтобы уведомлять его об изменениях активных объектов, чтобы он мог себя переобновлять
        this.clientNavigator = clientNavigator;

        actionDispatcher = new ClientFormActionDispatcher(this);

        form = new ClientSerializationPool().deserializeObject(new DataInputStream(new ByteArrayInputStream(remoteForm.getRichDesignByteArray())));
//        form = new ClientForm(new DataInputStream(new ByteArrayInputStream(remoteForm.getRichDesignByteArray())));

        if (remoteForm instanceof RemoteObjectProxy) {
            ((RemoteObjectProxy) remoteForm).blockedScreen = form.blockedScreen;
        }

        initializeForm();
    }

    // ------------------------------------------------------------------------------------ //
    // ----------------------------------- Инициализация ---------------------------------- //
    // ------------------------------------------------------------------------------------ //

    public ClientFormLayout getComponent() {
        return formLayout;
    }

    void initializeForm() throws IOException {

        formLayout = new ClientFormLayout(form.mainContainer) {
            boolean firstGainedFocus = true;

            @Override
            public void gainedFocus() {

                if (remoteForm == null) // типа если сработал closed, то ничего вызывать не надо
                    return;

                try {
                    remoteForm.gainedFocus();
                    if (clientNavigator != null) {
                        clientNavigator.currentFormChanged();
                    }

/*                    //при старте перемещаем фокус на стандартный (только в первый раз, из-за диалогов)
                    if (firstGainedFocus) {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                getFocusTraversalPolicy().getDefaultComponent(formLayout).requestFocusInWindow();
                            }
                        });
                        firstGainedFocus = false;
                    }*/

                    // если вдруг изменились данные в сессии
                    ClientExternalScreen.invalidate(getID());
                    ClientExternalScreen.repaintAll(getID());
                } catch (IOException e) {
                    throw new RuntimeException(ClientResourceBundle.getString("form.error.form.activation"), e);
                }
            }
        };

        initializeControllers();

        initializeRegularFilters();

        initializeDefaultOrders();

        initializeButtons();

        applyRemoteChanges();
    }

    public List<ClientPropertyDraw> getPropertyDraws() {
        return form.getPropertyDraws();
    }

    private void initializeControllers() throws IOException {
        treeControllers = new HashMap<ClientTreeGroup, TreeGroupController>();
        for (ClientTreeGroup treeGroup : form.treeGroups) {
            TreeGroupController controller = new TreeGroupController(treeGroup, form, this, formLayout);
            treeControllers.put(treeGroup, controller);
        }

        controllers = new HashMap<ClientGroupObject, GroupObjectController>();

        for (ClientGroupObject group : form.groupObjects) {
            if (group.parent == null) {
                GroupObjectController controller = new GroupObjectController(group, form, this, formLayout);
                controllers.put(group, controller);
            }
        }

        for (ClientPropertyDraw properties : form.getPropertyDraws()) {
            if (properties.groupObject == null) {
                GroupObjectController controller = new GroupObjectController(null, form, this, formLayout);
                controllers.put(null, controller);
                break;
            }
        }
    }

    private void initializeRegularFilters() {
        // Проинициализируем регулярные фильтры
        for (final ClientRegularFilterGroup filterGroup : form.regularFilterGroups) {
            if (filterGroup.filters.size() == 1) {
                createSingleFilterComponent(filterGroup, BaseUtils.single(filterGroup.filters));
            } else {
                createMultipleFilterComponent(filterGroup);
            }
        }
    }

    private void createMultipleFilterComponent(final ClientRegularFilterGroup filterGroup) {
        final JComboBox comboBox = new JComboBox();
        comboBox.addItem(new ClientRegularFilterWrapped(ClientResourceBundle.getString("form.all")));
        for (ClientRegularFilter filter : filterGroup.filters) {
            comboBox.addItem(new ClientRegularFilterWrapped(filter));
        }

        if (filterGroup.drawToToolbar && filterGroup.keyBindingGroup != null) {
            GroupObjectController controller = controllers.get(filterGroup.keyBindingGroup);
            controller.addFilterToToolbar(filterGroup, comboBox);
        }

        if (filterGroup.defaultFilter >= 0) {
            ClientRegularFilter defaultFilter = filterGroup.filters.get(filterGroup.defaultFilter);
            comboBox.setSelectedItem(new ClientRegularFilterWrapped(defaultFilter));
        }
        comboBox.addItemListener(new ItemAdapter() {
            @Override
            public void itemSelected(ItemEvent e) {
                try {
                    setRegularFilter(filterGroup, ((ClientRegularFilterWrapped) e.getItem()).filter);
                } catch (IOException ioe) {
                    throw new RuntimeException(ClientResourceBundle.getString("form.error.changing.regular.filter"), ioe);
                }
            }
        });

        for (final ClientRegularFilter filter : filterGroup.filters) {
            formLayout.addBinding(filter.key, "regularFilter" + filterGroup.getID() + filter.getID(), new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    comboBox.setSelectedItem(new ClientRegularFilterWrapped(filter));
                }
            });
        }

        formLayout.add(filterGroup, comboBox);
    }

    private void createSingleFilterComponent(final ClientRegularFilterGroup filterGroup, final ClientRegularFilter singleFilter) {
        final JCheckBox checkBox = new JCheckBox(singleFilter.getFullCaption());

        if (filterGroup.defaultFilter >= 0) {
            checkBox.setSelected(true);
        }

        if (filterGroup.drawToToolbar && filterGroup.keyBindingGroup != null) {
            GroupObjectController controller = controllers.get(filterGroup.keyBindingGroup);
            controller.addFilterToToolbar(filterGroup, checkBox);
        }

        checkBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent ie) {
                try {
                    if (ie.getStateChange() == ItemEvent.SELECTED)
                        setRegularFilter(filterGroup, singleFilter);
                    if (ie.getStateChange() == ItemEvent.DESELECTED)
                        setRegularFilter(filterGroup, null);
                } catch (IOException e) {
                    throw new RuntimeException(ClientResourceBundle.getString("form.error.changing.regular.filter"), e);
                }
            }
        });
        formLayout.add(filterGroup, checkBox);
        formLayout.addBinding(singleFilter.key, "regularFilter" + filterGroup.getID() + singleFilter.getID(), new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                checkBox.setSelected(!checkBox.isSelected());
            }
        });
    }

    public void quickEditFilter(int initialFilterPropertyDrawID) {
        ClientPropertyDraw propertyDraw = form.getProperty(initialFilterPropertyDrawID);
        if (propertyDraw != null && controllers.containsKey(propertyDraw.groupObject)) {
            controllers.get(propertyDraw.groupObject).quickEditFilter(propertyDraw);
        }
    }

    public void selectProperty(int propertyDrawId) {
        ClientPropertyDraw propertyDraw = form.getProperty(propertyDrawId);
        if (propertyDraw != null && controllers.containsKey(propertyDraw.groupObject)) {
            controllers.get(propertyDraw.groupObject).selectProperty(propertyDraw);
        }
    }

    public GroupObjectLogicsSupplier getGroupObjectLogicsSupplier(ClientGroupObject group) {
        GroupObjectController groupObjectController = controllers.get(group);
        if (groupObjectController != null) {
            return groupObjectController;
        }

        return group.parent != null
                ? treeControllers.get(group.parent)
                : null;
    }

    public class ClientRegularFilterWrapped {

        public ClientRegularFilter filter;
        private String caption;

        public ClientRegularFilterWrapped(String caption) {
            this(caption, null);
        }

        public ClientRegularFilterWrapped(ClientRegularFilter filter) {
            this(null, filter);
        }

        public ClientRegularFilterWrapped(String caption, ClientRegularFilter filter) {
            this.filter = filter;
            this.caption = caption;
        }

        @Override
        public boolean equals(Object wrapped) {
            return wrapped instanceof ClientRegularFilterWrapped
                    && (filter != null ? filter.equals(((ClientRegularFilterWrapped) wrapped).filter) : this == wrapped);
        }

        @Override
        public String toString() {
            return caption == null ? filter.getFullCaption() : caption;
        }
    }

    private void initializeButtons() {

        KeyStroke printKeyStroke = KeyStrokes.getPrintKeyStroke();
        KeyStroke xlsKeyStroke = KeyStrokes.getXlsKeyStroke();
        KeyStroke nullKeyStroke = KeyStrokes.getNullKeyStroke();
        KeyStroke refreshKeyStroke = KeyStrokes.getRefreshKeyStroke();
        KeyStroke okKeyStroke = KeyStrokes.getApplyKeyStroke(isModal() && isReadOnlyMode());
        KeyStroke closeKeyStroke = KeyStrokes.getCancelKeyStroke(true);
        KeyStroke applyKeyStroke = KeyStrokes.getApplyKeyStroke(false);
        KeyStroke cancelKeyStroke = KeyStrokes.getCancelKeyStroke(!isModal());

        // Добавляем стандартные кнопки

        if (Main.module.isFull() && !isDialog()) {
            addClientFunction(form.getPrintFunction(), printKeyStroke, new AbstractAction() {
                public void actionPerformed(ActionEvent ae) {
                    print();
                }
            });

            addClientFunction(form.getXlsFunction(), xlsKeyStroke, new AbstractAction() {
                public void actionPerformed(ActionEvent ae) {
                    Main.module.runExcel(remoteForm);
                }
            });
        }

        addClientFunction(form.getRefreshFunction(), refreshKeyStroke, new AbstractAction() {
            public void actionPerformed(ActionEvent ae) {
                refreshData();
            }
        });

        if (isDialog())
            addClientFunction(form.getNullFunction(), nullKeyStroke, new AbstractAction() {
                public void actionPerformed(ActionEvent ae) {
                    nullPressed();
                }
            });

        if (isNewSession()) {
            buttonApply = addClientFunction(form.getApplyFunction(), applyKeyStroke, new AbstractAction() {
                public void actionPerformed(ActionEvent ae) {
                    applyChanges(false);
                }
            });
            buttonApply.setEnabled(false);

            buttonCancel = addClientFunction(form.getCancelFunction(), cancelKeyStroke, new AbstractAction() {
                public void actionPerformed(ActionEvent ae) {
                    cancelChanges();
                }
            });
            buttonCancel.setEnabled(false);
        }

        if (isModal()) {
            addClientFunction(form.getOkFunction(), okKeyStroke, new AbstractAction() {
                public void actionPerformed(ActionEvent ae) {
                    okPressed();
                }
            });

            addClientFunction(form.getCloseFunction(), closeKeyStroke, new AbstractAction() {
                public void actionPerformed(ActionEvent ae) {
                    closePressed();
                }
            });
        }
    }

    private JButton addClientFunction(ClientFunction function, final KeyStroke keyStroke, final AbstractAction functionAction) {
        if (function.visible) {
            functionAction.putValue(Action.NAME, function.caption + " (" + SwingUtils.getKeyStrokeCaption(keyStroke) + ")");

            JButton actionButton = new ClientButton(functionAction);
            actionButton.setFocusable(false);

            formLayout.add(function, actionButton);
            formLayout.addBinding(keyStroke, function.type + "FunctionAction", functionAction);
            return actionButton;
        }
        return null;
    }

    private boolean defaultOrdersInitialized = false;

    private void initializeDefaultOrders() throws IOException {
        //сначала получаем изменения, чтобы был первоначальный список свойств в таблице
        applyRemoteChanges();
        try {
            // Применяем порядки по умолчанию
            applyOrders(form.defaultOrders);
            defaultOrdersInitialized = true;
        } catch (IOException e) {
            throw new RuntimeException(ClientResourceBundle.getString("form.error.cant.initialize.default.orders"));
        }
    }

    private void applyOrders(OrderedMap<ClientPropertyDraw, Boolean> orders) throws IOException {
        Set<ClientGroupObject> wasOrder = new HashSet<ClientGroupObject>();
        for (Map.Entry<ClientPropertyDraw, Boolean> entry : orders.entrySet()) {
            ClientPropertyDraw property = entry.getKey();
            ClientGroupObject groupObject = property.getGroupObject();
            GroupObjectLogicsSupplier groupObjectLogicsSupplier = getGroupObjectLogicsSupplier(groupObject);
            if (groupObjectLogicsSupplier != null) {
                groupObjectLogicsSupplier.changeOrder(property, !wasOrder.contains(groupObject) ? REPLACE : ADD);
                wasOrder.add(groupObject);
                if (!entry.getValue()) {
                    groupObjectLogicsSupplier.changeOrder(property, DIR);
                }
            }
        }
    }

    private void applyActions(List<ClientAction> actions, boolean beforeApply) throws IOException {
        for (ClientAction action : actions) {
            if (action.isBeforeApply() == beforeApply) {
                action.dispatch(actionDispatcher);
            }
        }
    }

    private boolean applyRemoteChanges() throws IOException {
        RemoteChanges remoteChanges = remoteForm.getRemoteChanges();
        List<ClientAction> remoteActions = remoteChanges.actions;

        applyActions(remoteActions, true);

        applyFormChanges(new ClientFormChanges(new DataInputStream(new ByteArrayInputStream(remoteChanges.form)), form, controllers));

        applyActions(remoteActions, false);

        if (clientNavigator != null) {
            clientNavigator.changeCurrentClass(remoteChanges.classID);
        }

        if (BaseUtils.last(remoteActions) instanceof ContinueAutoActionsClientAction) {
            remoteForm.continueAutoActions();
            refreshData();
        }

        for (ClientAction action : remoteActions)
            if (action instanceof StopAutoActionsClientAction)
                return false;

        return true;
    }

    private void applyFormChanges(ClientFormChanges formChanges) {
        if (formChanges.dataChanged != null) {
            dataChanged = formChanges.dataChanged;
            if (buttonApply != null) {
                Color defaultBackGround = getDefaultApplyBackground();
                buttonApply.setBackground(dataChanged ? Color.green : defaultBackGround);
                buttonApply.setEnabled(dataChanged);
                if (buttonCancel != null) {
                    buttonCancel.setEnabled(dataChanged);
                }
            }
        }

        for (Map.Entry<ClientGroupObject, ClassViewType> entry : formChanges.classViews.entrySet()) {
            ClassViewType classView = entry.getValue();
            if (classView != ClassViewType.GRID) {
                currentGridObjects.remove(entry.getKey());
            }
        }
        currentGridObjects.putAll(formChanges.gridObjects);

        for (GroupObjectController controller : controllers.values()) {
            controller.processFormChanges(formChanges, currentGridObjects);
        }

        for (TreeGroupController treeController : treeControllers.values()) {
            treeController.processFormChanges(formChanges, currentGridObjects);
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                formLayout.getComponent().revalidate();
                ClientExternalScreen.repaintAll(getID());
            }
        });

        // выдадим сообщение если было от сервера
        if (formChanges.message.length() > 0) {
            Log.error(formChanges.message);
        }

    }

    private Color defaultApplyBackground;

    private Color getDefaultApplyBackground() {
        if (defaultApplyBackground == null) {
            defaultApplyBackground = buttonApply.getBackground();
        }
        return defaultApplyBackground;
    }

    public void changeGroupObject(ClientGroupObject group, ClientGroupObjectValue objectValue) throws IOException {
        if (objectValue == null || remoteForm == null)// remoteForm может равняться null, если к моменту вызова форму уже закрыли
            return;

        if (group.parent != null || !objectValue.equals(controllers.get(group).getCurrentObject())) {
            // если ClientGroupObject в дереве, то вызывать не надо изменение объекта
            if (group.parent == null) {
                controllers.get(group).setCurrentGroupObject(objectValue);
            }
            remoteForm.changeGroupObject(group.getID(), objectValue.serialize());

            applyRemoteChanges();
        }
    }

    public void expandGroupObject(ClientGroupObject group, ClientGroupObjectValue objectValue) throws IOException {
        remoteForm.expandGroupObject(group.getID(), objectValue.serialize());

        applyRemoteChanges();
    }

    public void changeGroupObject(ClientGroupObject groupObject, Scroll changeType) throws IOException {
        SwingUtils.stopSingleAction(groupObject.getActionID(), false);

        remoteForm.changeGroupObject(groupObject.getID(), changeType.serialize());
        applyRemoteChanges();
    }


    public void changePropertyDraw(ClientPropertyDraw property, ClientGroupObjectValue columnKey, Object value, boolean all, boolean aggValue) throws IOException {
        // для глобальных свойств пока не может быть отложенных действий
        if (property.getGroupObject() != null) {
            SwingUtils.stopSingleAction(property.getGroupObject().getActionID(), true);
        }

        remoteForm.changePropertyDraw(property.getID(), columnKey.serialize(), BaseUtils.serializeObject(value), all, aggValue);
        applyRemoteChanges();
    }

    public void pasteExternalTable(List<ClientPropertyDraw> propertyList, List<List<Object>> table) throws IOException {
        List<Integer> propertyIdList = new ArrayList<Integer>();
        for (ClientPropertyDraw propertyDraw : propertyList) {
            propertyIdList.add(propertyDraw.getID());
        }
        remoteForm.pasteExternalTable(propertyIdList, table);
        applyRemoteChanges();
    }

    public void pasteMulticellValue(Map<ClientPropertyDraw, List<ClientGroupObjectValue>> cells, Object value) throws IOException {
        Map<Integer, List<Map<Integer, Object>>> reCells = new HashMap<Integer, List<Map<Integer, Object>>>();
        for (ClientPropertyDraw property : cells.keySet()) {
            List<Map<Integer, Object>> keys = new ArrayList<Map<Integer, Object>>();
            for (ClientGroupObjectValue groupObjectValue : cells.get(property)) {
                Map<Integer, Object> key = new HashMap<Integer, Object>();
                for (ClientObject object : groupObjectValue.keySet()) {
                    key.put(object.getID(), groupObjectValue.get(object));
                }
                keys.add(key);
            }
            reCells.put(property.getID(), keys);
        }
        remoteForm.pasteMulticellValue(reCells, value);
        applyRemoteChanges();
    }

    public void groupChangePropertyDraw(ClientPropertyDraw mainProperty, ClientGroupObjectValue mainColumnKey,
                                        ClientPropertyDraw getterProperty, ClientGroupObjectValue getterColumnKey) throws IOException {
        // для глобальных свойств пока не может быть отложенных действий
        if (mainProperty.getGroupObject() != null) {
            SwingUtils.stopSingleAction(mainProperty.getGroupObject().getActionID(), true);
        }

        remoteForm.groupChangePropertyDraw(mainProperty.getID(), mainColumnKey.serialize(), getterProperty.getID(), getterColumnKey.serialize());
        refreshData();
    }

    public void changeGridClass(ClientObject object, ClientObjectClass cls) throws IOException {

        remoteForm.changeGridClass(object.getID(), cls.ID);
        applyRemoteChanges();
    }

    public void switchClassView(ClientGroupObject groupObject) throws IOException {

        SwingUtils.stopSingleAction(groupObject.getActionID(), true);

        remoteForm.switchClassView(groupObject.getID());

        applyRemoteChanges();
    }

    public void changeClassView(ClientGroupObject groupObject, ClassViewType show) throws IOException {

        SwingUtils.stopSingleAction(groupObject.getActionID(), true);

        remoteForm.changeClassView(groupObject.getID(), show);

        applyRemoteChanges();
    }

    public void changeOrder(ClientPropertyDraw property, Order modiType, ClientGroupObjectValue columnKey) throws IOException {
        if (defaultOrdersInitialized) {
            remoteForm.changePropertyOrder(property.getID(), modiType.serialize(), columnKey.serialize());
            applyRemoteChanges();
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void changeFind(List<ClientPropertyFilter> conditions) {
    }

    private final Map<ClientGroupObject, List<ClientPropertyFilter>> currentFilters = new HashMap<ClientGroupObject, List<ClientPropertyFilter>>();

    public void changeFilter(ClientGroupObject groupObject, List<ClientPropertyFilter> conditions) throws IOException {
        currentFilters.put(groupObject, conditions);
        applyCurrentFilters();
    }

    public void changeFilter(ClientTreeGroup treeGroup, List<ClientPropertyFilter> conditions) throws IOException {
        Map<ClientGroupObject, List<ClientPropertyFilter>> filters = BaseUtils.groupList(new BaseUtils.Group<ClientGroupObject, ClientPropertyFilter>() {
            public ClientGroupObject group(ClientPropertyFilter key) {
                return key.groupObject;
            }
        }, conditions);

        for (ClientGroupObject group : treeGroup.groups) {
            List<ClientPropertyFilter> groupFilters = filters.get(group);
            if (groupFilters == null) {
                groupFilters = new ArrayList<ClientPropertyFilter>();
            }

            currentFilters.put(group, groupFilters);
        }

        applyCurrentFilters();
    }

    private void applyCurrentFilters() throws IOException {
        remoteForm.clearUserFilters();

        for (List<ClientPropertyFilter> listFilter : currentFilters.values()) {
            for (ClientPropertyFilter filter : listFilter) {
                remoteForm.addFilter(Serializer.serializeClientFilter(filter));
            }
        }

        applyRemoteChanges();
    }

    private void setRegularFilter(ClientRegularFilterGroup filterGroup, ClientRegularFilter filter) throws IOException {
        remoteForm.setRegularFilter(filterGroup.getID(), (filter == null) ? -1 : filter.getID());

        applyOrders(filter != null ? filter.orders : filterGroup.nullOrders);

        applyRemoteChanges();
    }

    public int countRecords(int groupObjectID) throws IOException {
        return remoteForm.countRecords(groupObjectID);
    }

    public Object calculateSum(int propertyID, byte[] columnKeys) throws IOException {
        return remoteForm.calculateSum(propertyID, columnKeys);
    }

    public Map<List<Object>, List<Object>> groupData(Map<Integer, List<byte[]>> groupMap, Map<Integer, List<byte[]>> sumMap, Map<Integer,
            List<byte[]>> maxMap, boolean onlyNotNull) throws IOException {
        return remoteForm.groupData(groupMap, sumMap, maxMap, onlyNotNull);
    }

    public void changePageSize(ClientGroupObject groupObject, Integer pageSize) throws IOException {
        remoteForm.changePageSize(groupObject.getID(), pageSize);
    }


    void print() {

        try {
            Main.frame.runReport(remoteForm, false);
        } catch (Exception e) {
            throw new RuntimeException(ClientResourceBundle.getString("form.error.printing.form"), e);
        }
    }

    void refreshData() {

        try {

            remoteForm.refreshData();

            applyRemoteChanges();

        } catch (IOException e) {
            throw new RuntimeException(ClientResourceBundle.getString("form.error.refreshing.form"), e);
        }
    }

    boolean applyChanges(boolean sureApply) {

        try {

            if (dataChanged) {

                if (!sureApply) {
                    String okMessage = "";
                    for (ClientGroupObject group : form.groupObjects) {
                        if (controllers.containsKey(group)) {
                            okMessage += controllers.get(group).getSaveMessage();
                        }
                    }

                    if (!okMessage.isEmpty()) {
                        if (!(SwingUtils.showConfirmDialog(getComponent(), okMessage, null, JOptionPane.QUESTION_MESSAGE, SwingUtils.YES_BUTTON) == JOptionPane.YES_OPTION)) {
                            return false;
                        }
                    }
                }

                if (remoteForm.hasClientApply()) {
                    ClientApply clientApply = remoteForm.checkClientChanges();
                    if (clientApply instanceof CheckFailed) { // чтобы не делать лишний RMI вызов
                        Log.error(((CheckFailed) clientApply).message);
                        return false;
                    } else {
                        Object clientResult;
                        try {
                            clientResult = ((ClientResultAction) clientApply).dispatchResult(actionDispatcher);
                        } catch (Exception e) {
                            throw new RuntimeException(ClientResourceBundle.getString("form.error.applying.changes"), e);
                        }
                        remoteForm.applyClientChanges(clientResult);

                        return applyRemoteChanges();
                    }
                } else {
                    remoteForm.applyChanges();

                    return applyRemoteChanges();
                }
            } else
                return true;

        } catch (IOException e) {
            throw new RuntimeException(ClientResourceBundle.getString("form.error.applying.changes"), e);
        }
    }

    boolean cancelChanges() {
        try {
            if (dataChanged) {
                if (SwingUtils.showConfirmDialog(getComponent(), ClientResourceBundle.getString("form.do.you.really.want.to.undo.changes"), null, JOptionPane.WARNING_MESSAGE, SwingUtils.NO_BUTTON) == JOptionPane.YES_OPTION) {
                    remoteForm.cancelChanges();

                    applyRemoteChanges();
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(ClientResourceBundle.getString("form.error.undoing.changes"), e);
        }

        return true;
    }

    public boolean okPressed() {
        try {
            remoteForm.dialogClosed();

            if (isNewSession()) {
                return applyChanges(false);
            }
        } catch (IOException e) {
            throw new RuntimeException(ClientResourceBundle.getString("form.error.closing.dialog"), e);
        }

        return true;
    }

    boolean closePressed() {
        return cancelChanges();
    }

    boolean nullPressed() {
        return true;
    }

    public void dropLayoutCaches() {
        formLayout.dropCaches();
    }

    public void closed() {
        // здесь мы сбрасываем ссылку на remoteForm для того, чтобы сборщик мусора быстрее собрал удаленные объекты
        // это нужно, чтобы connection'ы на сервере закрывались как можно быстрее
        if (remoteForm != null) {
            remoteForm = null;
        }
    }

    public Dimension calculatePreferredSize() {
        return formLayout.calculatePreferredSize();
    }

    public void moveGroupObject(ClientGroupObject parentGroup, ClientGroupObjectValue parentKey, ClientGroupObject childGroup, ClientGroupObjectValue childKey, int index) throws IOException {
        remoteForm.moveGroupObject(parentGroup.getID(), parentKey.serialize(), childGroup.getID(), childKey.serialize(), index);

        applyRemoteChanges();
    }
}