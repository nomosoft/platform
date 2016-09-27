package lsfusion.interop.form.layout;

import static lsfusion.interop.form.layout.GroupObjectContainerSet.*;

public class TreeGroupContainerSet <C extends AbstractContainer<C, T>, T extends AbstractComponent<C, T>> {

    private C treeContainer;
    private C controlsContainer;
    private C rightControlsContainer;
    private C filtersContainer;
    private C toolbarPropsContainer;

    public C getTreeContainer() {
        return treeContainer;
    }

    public C getControlsContainer() {
        return controlsContainer;
    }

    public C getRightControlsContainer() {
        return rightControlsContainer;
    }

    public C getFiltersContainer() {
        return filtersContainer;
    }

    public C getToolbarPropsContainer() {
        return toolbarPropsContainer;
    }

    public static <C extends AbstractContainer<C, T>, T extends AbstractComponent<C, T>> TreeGroupContainerSet<C, T> create(AbstractTreeGroup<C,T> treeGroup, ContainerFactory<C> factory) {
        return create(treeGroup, factory, ContainerAdder.<C, T>DEFAULT());
    }
    public static <C extends AbstractContainer<C, T>, T extends AbstractComponent<C, T>> TreeGroupContainerSet<C, T> create(AbstractTreeGroup<C,T> treeGroup, ContainerFactory<C> factory, ContainerAdder<C, T> adder) {
        TreeGroupContainerSet<C,T> set = new TreeGroupContainerSet<>();

        set.treeContainer = factory.createContainer();
        set.treeContainer.setCaption("{form.layout.tree}");
        set.treeContainer.setDescription("{form.layout.tree}");
        set.treeContainer.setSID(treeGroup.getSID() + GroupObjectContainerSet.TREE_GROUP_CONTAINER);

        set.controlsContainer = factory.createContainer(); // контейнер всех управляющих объектов
        set.controlsContainer.setDescription("{form.layout.conrol.objects}");
        set.controlsContainer.setSID(treeGroup.getSID() + CONTROLS_CONTAINER);

        set.toolbarPropsContainer = factory.createContainer(); // контейнер тулбара
        set.toolbarPropsContainer.setDescription("{form.layout.toolbar.props.container}");
        set.toolbarPropsContainer.setSID(treeGroup.getSID() + TOOLBAR_PROPS_CONTAINER);

        set.filtersContainer = factory.createContainer(); // контейнер фильтров
        set.filtersContainer.setDescription("{form.layout.filters.container}");
        set.filtersContainer.setSID(treeGroup.getSID() + FILTERS_CONTAINER);

        set.rightControlsContainer = factory.createContainer();
        set.rightControlsContainer.setSID(treeGroup.getSID() + CONTROLS_RIGHT_CONTAINER);

        set.treeContainer.setType(ContainerType.CONTAINERV);
        set.treeContainer.setFlex(1);
        set.treeContainer.setAlignment(FlexAlignment.STRETCH);
        adder.add(set.treeContainer, (T) treeGroup);
        adder.add(set.treeContainer, (T) set.controlsContainer);
        adder.add(set.treeContainer, (T) treeGroup.getFilter());

        set.controlsContainer.setType(ContainerType.CONTAINERH);
        set.controlsContainer.setAlignment(FlexAlignment.STRETCH);
        set.controlsContainer.setChildrenAlignment(Alignment.LEADING);
        adder.add(set.controlsContainer, (T) treeGroup.getToolbar());
        adder.add(set.controlsContainer, (T) set.rightControlsContainer);
        
        set.rightControlsContainer.setType(ContainerType.CONTAINERH);
        set.rightControlsContainer.setAlignment(FlexAlignment.CENTER);
        set.rightControlsContainer.setChildrenAlignment(Alignment.TRAILING);
        adder.add(set.rightControlsContainer, (T) set.filtersContainer);
        adder.add(set.rightControlsContainer, (T) set.toolbarPropsContainer);

        set.filtersContainer.setType(ContainerType.CONTAINERH);
        set.filtersContainer.setAlignment(FlexAlignment.CENTER);
        set.filtersContainer.setChildrenAlignment(Alignment.TRAILING);

        set.toolbarPropsContainer.setType(ContainerType.CONTAINERH);
        set.toolbarPropsContainer.setAlignment(FlexAlignment.CENTER);

        treeGroup.setFlex(1);
        treeGroup.setAlignment(FlexAlignment.STRETCH);

        treeGroup.getFilter().setAlignment(FlexAlignment.STRETCH);
        treeGroup.getToolbar().setMargin(2);

        return set;
    }
}
