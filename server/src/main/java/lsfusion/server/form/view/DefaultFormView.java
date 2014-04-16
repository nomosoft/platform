package lsfusion.server.form.view;

import com.google.common.base.Optional;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import lsfusion.interop.KeyStrokes;
import lsfusion.interop.PropertyEditType;
import lsfusion.interop.form.layout.*;
import lsfusion.server.form.entity.FormEntity;
import lsfusion.server.form.entity.GroupObjectEntity;
import lsfusion.server.form.entity.PropertyDrawEntity;
import lsfusion.server.form.entity.TreeGroupEntity;
import lsfusion.server.form.entity.filter.RegularFilterGroupEntity;
import lsfusion.server.logics.mutables.Version;
import lsfusion.server.logics.property.group.AbstractGroup;
import lsfusion.server.serialization.ServerIdentitySerializable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultFormView extends FormView {
    protected transient Map<GroupObjectView, ContainerView> groupContainers = new HashMap<GroupObjectView, ContainerView>();
    public ContainerView getGroupObjectContainer(GroupObjectView groupObject) { return groupContainers.get(groupObject); }
    public ContainerView getGroupObjectContainer(GroupObjectEntity groupObject) { return getGroupObjectContainer(get(groupObject)); }

    protected transient Map<GroupObjectView, ContainerView> gridContainers = new HashMap<GroupObjectView, ContainerView>();
    public ContainerView getGridContainer(GroupObjectView treeGroup) { return gridContainers.get(treeGroup); }
    public ContainerView getGridContainer(GroupObjectEntity groupObject) { return getGridContainer(get(groupObject)); }

    protected transient Map<GroupObjectView, ContainerView> panelContainers = new HashMap<GroupObjectView, ContainerView>();
    public ContainerView getPanelContainer(GroupObjectView groupObject) { return panelContainers.get(groupObject); }
    public ContainerView getPanelContainer(GroupObjectEntity groupObject) { return getPanelContainer(get(groupObject)); }

    protected final Map<ServerIdentitySerializable, ContainerView> panelPropsContainers = new HashMap<ServerIdentitySerializable, ContainerView>();
    public ContainerView getPanelPropsContainer(GroupObjectView groupObject) { return panelPropsContainers.get(groupObject); }

    protected transient Map<TreeGroupView, ContainerView> treeContainers = new HashMap<TreeGroupView, ContainerView>();
    public ContainerView getTreeContainer(TreeGroupView treeGroup) { return treeContainers.get(treeGroup); }
    public ContainerView getTreeContainer(TreeGroupEntity treeGroup) { return getTreeContainer(get(treeGroup)); }

    protected transient Map<ServerIdentitySerializable, ContainerView> controlsContainers = new HashMap<ServerIdentitySerializable, ContainerView>();
    public ContainerView getControlsContainer(GroupObjectView groupObject) { return controlsContainers.get(groupObject); }
    public ContainerView getControlsContainer(TreeGroupView treeGroup) { return controlsContainers.get(treeGroup); }

    protected final Map<ServerIdentitySerializable, ContainerView> toolbarPropsContainers = new HashMap<ServerIdentitySerializable, ContainerView>();
    public ContainerView getToolbarPropsContainer(GroupObjectView groupObject) { return toolbarPropsContainers.get(groupObject); }
    public ContainerView getToolbarPropsContainer(TreeGroupView treeGroup) { return toolbarPropsContainers.get(treeGroup); }

    protected transient Map<ServerIdentitySerializable, ContainerView> rightControlsContainers = new HashMap<ServerIdentitySerializable, ContainerView>();
    public ContainerView getRightControlsContainer(GroupObjectView groupObject) { return rightControlsContainers.get(groupObject); }
    public ContainerView getRightControlsContainer(TreeGroupView treeGroup) { return rightControlsContainers.get(treeGroup); }

    protected final Map<ServerIdentitySerializable,ContainerView> filtersContainers = new HashMap<ServerIdentitySerializable, ContainerView>();
    public ContainerView getFilterContainer(GroupObjectView groupObject) { return filtersContainers.get(groupObject); }
    public ContainerView getFilterContainer(TreeGroupView treeGroup) { return filtersContainers.get(treeGroup); }

    protected transient Table<Optional<GroupObjectView>, AbstractGroup, ContainerView> groupPropertyContainers = HashBasedTable.create();
    public ContainerView getGroupPropertyContainer(GroupObjectView groupObject, AbstractGroup group) { return groupPropertyContainers.get(Optional.fromNullable(groupObject), group); }
    public ContainerView getGroupPropertyContainer(GroupObjectEntity groupObject, AbstractGroup group) { return getGroupPropertyContainer(get(groupObject), group); }

    public ContainerView formButtonContainer;
    public ContainerView noGroupPanelContainer;
    public ContainerView noGroupPanelPropsContainer;

    private ContainerFactory<ContainerView> containerFactory = new ContainerFactory<ContainerView>() {
        public ContainerView createContainer() {
            return new ContainerView(idGenerator.idShift());
        }
    };

    public DefaultFormView() {
    }

    public DefaultFormView(FormEntity<?> formEntity, Version version) {
        super(formEntity, version);

        caption = entity.getTitle();
        autoRefresh = entity.autoRefresh;

        FormContainerSet<ContainerView, ComponentView> formSet = FormContainerSet.fillContainers(this, containerFactory, new ContainerView.VersionContainerAdder(version));
        setComponentSID(formSet.getFormButtonContainer(), formSet.getFormButtonContainer().getSID());
        setComponentSID(formSet.getNoGroupPanelContainer(), formSet.getNoGroupPanelContainer().getSID());
        setComponentSID(formSet.getNoGroupPanelPropsContainer(), formSet.getNoGroupPanelPropsContainer().getSID());

        formButtonContainer = formSet.getFormButtonContainer();
        noGroupPanelContainer = formSet.getNoGroupPanelContainer();
        noGroupPanelPropsContainer = formSet.getNoGroupPanelPropsContainer();

        panelContainers.put(null, noGroupPanelContainer);
        panelPropsContainers.put(null, noGroupPanelPropsContainer);

        for (GroupObjectView groupObject : getNFGroupObjectsListIt(version)) {
            addGroupObjectView(groupObject, version);
        }

        for (TreeGroupView treeGroup : getNFTreeGroupsListIt(version)) {
            addTreeGroupView(treeGroup, version);
        }

        for (PropertyDrawView propertyDraw : getNFPropertiesListIt(version)) {
            addPropertyDrawView(propertyDraw, version);
        }

        for (RegularFilterGroupView filterGroup : getNFRegularFiltersListIt(version)) {
            addRegularFilterGroupView(filterGroup, version);
        }

        mainContainer.add(noGroupPanelContainer, version);
        mainContainer.add(formButtonContainer, version);

        initFormButtons(version);
    }

    private void initFormButtons(Version version) {
        PropertyDrawView printFunction = get(entity.printActionPropertyDraw);
        setupFormButton(printFunction, KeyStrokes.getPrintKeyStroke(), "print.png");

        PropertyDrawView xlsFunction = get(entity.xlsActionPropertyDraw);
        setupFormButton(xlsFunction, KeyStrokes.getXlsKeyStroke(), "xls.png");

        PropertyDrawView editFunction = get(entity.editActionPropertyDraw);
        setupFormButton(editFunction, KeyStrokes.getEditKeyStroke(), "editReport.png");

        PropertyDrawView dropFunction = get(entity.dropActionPropertyDraw);
        setupFormButton(dropFunction, KeyStrokes.getNullKeyStroke(), null);

        PropertyDrawView refreshFunction = get(entity.refreshActionPropertyDraw);
        setupFormButton(refreshFunction, KeyStrokes.getRefreshKeyStroke(), "refresh.png");
        refreshFunction.drawAsync = true;

        PropertyDrawView applyFunction = get(entity.applyActionPropertyDraw);
        setupFormButton(applyFunction, KeyStrokes.getApplyKeyStroke(), null);

        PropertyDrawView cancelFunction = get(entity.cancelActionPropertyDraw);
        setupFormButton(cancelFunction, KeyStrokes.getCancelKeyStroke(), null);

        PropertyDrawView okFunction = get(entity.okActionPropertyDraw);
        setupFormButton(okFunction, KeyStrokes.getOkKeyStroke(), null);

        PropertyDrawView closeFunction = get(entity.closeActionPropertyDraw);
        setupFormButton(closeFunction, KeyStrokes.getCloseKeyStroke(), null);

        ContainerView leftControlsContainer = createContainer(null, null, "leftControls");
        leftControlsContainer.setType(ContainerType.CONTAINERH);
        leftControlsContainer.childrenAlignment = Alignment.LEADING;
        leftControlsContainer.flex = 0;

        ContainerView rightControlsContainer = createContainer(null, null, "rightControls");
        rightControlsContainer.setType(ContainerType.CONTAINERH);
        rightControlsContainer.childrenAlignment = Alignment.TRAILING;
        rightControlsContainer.flex = 1;

        leftControlsContainer.add(printFunction, version);
        leftControlsContainer.add(xlsFunction, version);
        leftControlsContainer.add(editFunction, version);
        leftControlsContainer.add(dropFunction, version);

        rightControlsContainer.add(refreshFunction, version);
        rightControlsContainer.add(applyFunction, version);
        rightControlsContainer.add(cancelFunction, version);
        rightControlsContainer.add(okFunction, version);
        rightControlsContainer.add(closeFunction, version);

        formButtonContainer.add(leftControlsContainer, version);
        formButtonContainer.add(rightControlsContainer, version);
    }

    private void setupFormButton(PropertyDrawView action, KeyStroke editKey, String iconPath) {
        action.editKey = editKey;
        action.focusable = false;
        action.entity.setEditType(PropertyEditType.EDITABLE);
        action.alignment = FlexAlignment.STRETCH;

        if (iconPath != null) {
            action.showEditKey = false;
            action.design.setIconPath(iconPath);
        }
    }

    private void addGroupObjectView(GroupObjectView groupObject, Version version) {
        GroupObjectContainerSet<ContainerView, ComponentView> groupSet = GroupObjectContainerSet.create(groupObject, containerFactory, new ContainerView.VersionContainerAdder(version));

        mainContainer.add(groupSet.getGroupContainer(), version);

        groupContainers.put(groupObject, groupSet.getGroupContainer());
        setComponentSID(groupSet.getGroupContainer(), groupSet.getGroupContainer().getSID());
        gridContainers.put(groupObject, groupSet.getGridContainer());
        setComponentSID(groupSet.getGridContainer(), groupSet.getGridContainer().getSID());
        panelContainers.put(groupObject, groupSet.getPanelContainer());
        setComponentSID(groupSet.getPanelContainer(), groupSet.getPanelContainer().getSID());
        panelPropsContainers.put(groupObject, groupSet.getPanelPropsContainer());
        setComponentSID(groupSet.getPanelPropsContainer(), groupSet.getPanelPropsContainer().getSID());
        controlsContainers.put(groupObject, groupSet.getControlsContainer());
        setComponentSID(groupSet.getControlsContainer(), groupSet.getControlsContainer().getSID());
        rightControlsContainers.put(groupObject, groupSet.getRightControlsContainer());
        setComponentSID(groupSet.getRightControlsContainer(), groupSet.getRightControlsContainer().getSID());
        filtersContainers.put(groupObject, groupSet.getFiltersContainer());
        setComponentSID(groupSet.getFiltersContainer(), groupSet.getFiltersContainer().getSID());
        toolbarPropsContainers.put(groupObject, groupSet.getToolbarPropsContainer());
        setComponentSID(groupSet.getToolbarPropsContainer(), groupSet.getToolbarPropsContainer().getSID());

        if (groupObject.size() == 1) {
            groupSet.getGridContainer().addFirst(groupObject.get(0).classChooser, version);
        } else if (groupObject.size() > 1) {
            List<ContainerView> containers = new ArrayList<ContainerView>();
            for (int i = 0; i < groupObject.size() - 1; i++) {
                ContainerView container = createContainer();
                container.setType(ContainerType.HORIZONTAL_SPLIT_PANE);
                container.add(groupObject.get(i).classChooser, version);
                containers.add(container);
            }
            containers.get(containers.size() - 1).add(groupObject.get(groupObject.size() - 1).classChooser, version);
            for (int i = containers.size() - 1; i > 0; i--) {
                containers.get(i - 1).add(containers.get(i), version);
            }
            groupSet.getGridContainer().addFirst(containers.get(0), version);
        }

        if (groupObject.entity.isForcedPanel()) {
            groupSet.getGroupContainer().flex = 0;
        }
    }

    private void addTreeGroupView(TreeGroupView treeGroup, Version version) {
        TreeGroupContainerSet<ContainerView, ComponentView> treeSet = TreeGroupContainerSet.create(treeGroup, containerFactory, new ContainerView.VersionContainerAdder(version));

        treeContainers.put(treeGroup, treeSet.getTreeContainer());
        setComponentSID(treeSet.getTreeContainer(), treeSet.getTreeContainer().getSID());
        controlsContainers.put(treeGroup, treeSet.getControlsContainer());
        setComponentSID(treeSet.getControlsContainer(), treeSet.getControlsContainer().getSID());
        rightControlsContainers.put(treeGroup, treeSet.getRightControlsContainer());
        setComponentSID(treeSet.getRightControlsContainer(), treeSet.getRightControlsContainer().getSID());
        filtersContainers.put(treeGroup, treeSet.getFiltersContainer());
        setComponentSID(treeSet.getFiltersContainer(), treeSet.getFiltersContainer().getSID());
        toolbarPropsContainers.put(treeGroup, treeSet.getToolbarPropsContainer());
        setComponentSID(treeSet.getToolbarPropsContainer(), treeSet.getToolbarPropsContainer().getSID());

        //вставляем перед первым groupObject в данной treeGroup
        mainContainer.addBefore(treeSet.getTreeContainer(), groupContainers.get(mgroupObjects.get(treeGroup.entity.getGroups().get(0))), version);
    }

    private void addPropertyDrawView(PropertyDrawView propertyDraw, Version version) {
        PropertyDrawEntity drawEntity = propertyDraw.entity;
        drawEntity.proceedDefaultDesign(propertyDraw, this);

        GroupObjectEntity groupObject = drawEntity.getNFToDraw(entity, version);
        GroupObjectView groupObjectView = mgroupObjects.get(groupObject);

        if (groupObjectView != null && propertyDraw.entity.isDrawToToolbar()) {
            ContainerView propertyContainer = null;
            if (groupObject.treeGroup != null) {
                propertyContainer = getToolbarPropsContainer(mtreeGroups.get(groupObject.treeGroup));
            } else {
                propertyContainer = getToolbarPropsContainer(mgroupObjects.get(groupObject));
            }

            propertyDraw.alignment = FlexAlignment.CENTER;

            propertyContainer.add(propertyDraw, version);
        } else {
            addPropertyDrawToLayout(groupObjectView, propertyDraw, version);
        }
    }

    private void addRegularFilterGroupView(RegularFilterGroupView filterGroup, Version version) {
        ContainerView filterContainer = null;
        GroupObjectEntity groupObject = filterGroup.entity.getNFToDraw(entity, version);
        if (groupObject.treeGroup != null) {
            filterContainer = getFilterContainer(mtreeGroups.get(groupObject.treeGroup));
        } else {
            filterContainer = getFilterContainer(mgroupObjects.get(groupObject));
        }
        filterContainer.add(filterGroup, version);
    }

    @Override
    public GroupObjectView addGroupObject(GroupObjectEntity groupObject, Version version) {
        GroupObjectView view = super.addGroupObject(groupObject, version);
        addGroupObjectView(view, version);
        return view;
    }

    @Override
    public TreeGroupView addTreeGroup(TreeGroupEntity treeGroup, Version version) {
        TreeGroupView view = super.addTreeGroup(treeGroup, version);
        addTreeGroupView(view, version);
        return view;
    }

    @Override
    public PropertyDrawView addPropertyDraw(PropertyDrawEntity propertyDraw, Version version) {
        PropertyDrawView view = super.addPropertyDraw(propertyDraw, version);
        addPropertyDrawView(view, version);
        return view;
    }

    @Override
    public RegularFilterGroupView addRegularFilterGroup(RegularFilterGroupEntity filterGroup, Version version) {
        RegularFilterGroupView view = super.addRegularFilterGroup(filterGroup, version);
        addRegularFilterGroupView(view, version);
        return view;
    }

//    private void addPropertyDrawToLayout(GroupObjectView groupObject, PropertyDrawView propertyDraw) {
//        AbstractGroup propertyParentGroup = propertyDraw.entity.propertyObject.property.getParent();
//
//        Pair<ContainerView, ContainerView> groupContainers = getPropGroupContainer(groupObject, propertyParentGroup);
//        groupContainers.second.add(propertyDraw);
//    }
//
//    //возвращает контейнер группы и контейнер свойств этой группы
//    private Pair<ContainerView, ContainerView> getPropGroupContainer(GroupObjectView groupObject, AbstractGroup currentGroup) {
//        if (currentGroup == null) {
//            return new Pair<ContainerView, ContainerView>(panelContainers.get(groupObject), panelPropsContainers.get(groupObject));
//        }
//
//        if (!currentGroup.createContainer) {
//            return getPropGroupContainer(groupObject, currentGroup.getParent());
//        }
//
//        //ищем в созданных
//        ContainerView currentGroupContainer = groupPropertyContainers.get(Optional.fromNullable(groupObject), currentGroup);
//        ContainerView currentGroupPropsContainer = groupPropertyPropsContainer.get(Optional.fromNullable(groupObject), currentGroup);
//        if (currentGroupContainer == null) {
//            String currentGroupContainerSID = getPropertyGroupContainerSID(groupObject, currentGroup);
//            String currentGroupPropsContainerSID = currentGroupContainerSID + ".props";
//
//            //ищем по имени
//            currentGroupContainer = getContainerBySID(currentGroupContainerSID);
//            if (currentGroupContainer == null) {
//                //не нашли - создаём
//                currentGroupContainer = createContainer(currentGroup.caption, null, currentGroupContainerSID);
//                currentGroupContainer.setType(ContainerType.CONTAINERV);
//
//                currentGroupPropsContainer = createGroupPropsContainer(currentGroupContainer, currentGroupPropsContainerSID);
//
//                groupPropertyPropsContainer.put(Optional.fromNullable(groupObject), currentGroup, currentGroupPropsContainer);
//
//                Pair<ContainerView, ContainerView> parentGroupContainers = getPropGroupContainer(groupObject, currentGroup.getParent());
//                parentGroupContainers.first.add(currentGroupContainer);
//            } else {
//                //нашли контейнер группы по имени
//                currentGroupPropsContainer = getContainerBySID(currentGroupPropsContainerSID);
//                if (currentGroupPropsContainer == null) {
//                    //...но не нашли контейнер свойств по имени
//                    currentGroupPropsContainer = createGroupPropsContainer(currentGroupContainer, currentGroupPropsContainerSID);
//                }
//            }
//
//            groupPropertyContainers.put(Optional.fromNullable(groupObject), currentGroup, currentGroupContainer);
//        }
//
//        return new Pair<ContainerView, ContainerView>(currentGroupContainer, currentGroupPropsContainer);
//    }
//
//    private ContainerView createGroupPropsContainer(ContainerView groupContainer, String currentGroupPropsContainerSID) {
//        ContainerView groupPropsContainer = createContainer(null, null, currentGroupPropsContainerSID);
//        groupPropsContainer.setType(ContainerType.COLUMNS);
//        groupPropsContainer.columns = 6;
//        groupContainer.add(groupPropsContainer);
//        return groupPropsContainer;
//    }

    private void addPropertyDrawToLayout(GroupObjectView groupObject, PropertyDrawView propertyDraw, Version version) {
        // иерархическая структура контейнеров групп: каждый контейнер группы - это CONTAINERH,
        // в который сначала добавляется COLUMNS для свойств этой группы, а затем - контейнеры подгрупп
        AbstractGroup propertyParentGroup = propertyDraw.entity.propertyObject.property.getNFParent(version);

        ContainerView propGroupContainer = getPropGroupContainer(groupObject, propertyParentGroup, version);
        propGroupContainer.add(propertyDraw, version);
    }

    //возвращает контейнер группы и контейнер свойств этой группы
    private ContainerView getPropGroupContainer(GroupObjectView groupObject, AbstractGroup currentGroup, Version version) {
        if (currentGroup == null) {
            return panelPropsContainers.get(groupObject);
        }

        if (!currentGroup.createContainer) {
            return getPropGroupContainer(groupObject, currentGroup.getNFParent(version), version);
        }

        //ищем в созданных
        ContainerView currentGroupContainer = groupPropertyContainers.get(Optional.fromNullable(groupObject), currentGroup);
        if (currentGroupContainer == null) {
            String currentGroupContainerSID = getPropertyGroupContainerSID(groupObject, currentGroup);

            //ищем по имени
            currentGroupContainer = getContainerBySID(currentGroupContainerSID);
            if (currentGroupContainer == null) {
                //сначала создаём контейнеры для верхних групп, чтобы соблюдался порядок
                getPropGroupContainer(groupObject, currentGroup.getNFParent(version), version);

                //затем создаём контейнер для текущей группы
                currentGroupContainer = createContainer(currentGroup.caption, null, currentGroupContainerSID);
                currentGroupContainer.setType(ContainerType.COLUMNS);
                currentGroupContainer.columns = 4;

                panelContainers.get(groupObject).add(currentGroupContainer, version);
            }

            groupPropertyContainers.put(Optional.fromNullable(groupObject), currentGroup, currentGroupContainer);
        }

        return currentGroupContainer;
    }

    private static String getPropertyGroupContainerSID(GroupObjectView group, AbstractGroup propertyGroup) {
        String propertyGroupSID = propertyGroup.getSID();
        if (propertyGroupSID.contains("_")) {
            String[] sids = propertyGroupSID.split("_", 2);
            propertyGroupSID = sids[1];
        }
        // todo : здесь конечно совсем хак - нужно более четкую схему сделать
//        if (lm.getGroupBySID(propertyGroupSID) != null) {
//            используем простое имя для групп данного модуля
//            propertyGroupSID = lm.transformSIDToName(propertyGroupSID);
//        }
        return (group == null ? "NOGROUP" : group.entity.getSID()) + "." + propertyGroupSID; // todo [dale]: разобраться с NOGROUP
    }
}
