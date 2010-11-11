package platform.interop.form.layout;

import java.awt.*;

public class FormContainerSet <C extends AbstractContainer<C, T>, T extends AbstractComponent<C, T>> {

    private C mainContainer;
    private C formButtonContainer;

    public C getMainContainer() {
        return mainContainer;
    }

    public C getFormButtonContainer() {
        return formButtonContainer;
    }

    public static <C extends AbstractContainer<C, T>, T extends AbstractComponent<C, T>, F extends AbstractFunction<C,T>> FormContainerSet<C,T> fillContainers(AbstractForm<C,T,F> form, ContainerFactory<C> contFactory, FunctionFactory<F> funcFactory) {

        FormContainerSet<C,T> set = new FormContainerSet<C,T>();

        set.mainContainer = contFactory.createContainer();
        set.mainContainer.setDescription("Главный контейнер");
        set.mainContainer.setSID("mainContainer");
        set.mainContainer.getConstraints().childConstraints = SingleSimplexConstraint.TOTHE_BOTTOM;

        set.formButtonContainer = contFactory.createContainer();
        set.formButtonContainer.setDescription("Служебные кнопки");
        set.formButtonContainer.getConstraints().childConstraints = SingleSimplexConstraint.TOTHE_RIGHT;
        set.mainContainer.add((T)set.formButtonContainer);

        F printFunction = funcFactory.createFunction();
        printFunction.setCaption("Печать");
        printFunction.getConstraints().directions = new SimplexComponentDirections(0,0.01,0.01,0);
        set.formButtonContainer.add((T)printFunction);

        F xlsFunction = funcFactory.createFunction();
        xlsFunction.setCaption("Excel");
        xlsFunction.getConstraints().directions = new SimplexComponentDirections(0,0.01,0.01,0);
        set.formButtonContainer.add((T)xlsFunction);

        F nullFunction = funcFactory.createFunction();
        nullFunction.setCaption("Сбросить");
        nullFunction.getConstraints().directions = new SimplexComponentDirections(0,0.01,0.01,0);
        set.formButtonContainer.add((T)nullFunction);

        F refreshFunction = funcFactory.createFunction();
        refreshFunction.setCaption("Обновить");
        refreshFunction.getConstraints().directions = new SimplexComponentDirections(0,0,0.01,0.01);
        set.formButtonContainer.add((T)refreshFunction);

        F applyFunction = funcFactory.createFunction();
        applyFunction.setCaption("Применить");
        applyFunction.getConstraints().directions = new SimplexComponentDirections(0,0,0.01,0.01);
        applyFunction.getConstraints().insetsSibling = new Insets(0, 8, 0, 0);
        set.formButtonContainer.add((T)applyFunction);

        F cancelFunction = funcFactory.createFunction();
        cancelFunction.setCaption("Отменить");
        cancelFunction.getConstraints().directions = new SimplexComponentDirections(0,0,0.01,0.01);
        set.formButtonContainer.add((T)cancelFunction);

        F okFunction = funcFactory.createFunction();
        okFunction.setCaption("ОК");
        okFunction.getConstraints().directions = new SimplexComponentDirections(0,0,0.01,0.01);
        okFunction.getConstraints().insetsSibling = new Insets(0, 8, 0, 0);
        set.formButtonContainer.add((T)okFunction);

        F closeFunction = funcFactory.createFunction();
        closeFunction.setCaption("Закрыть");
        closeFunction.getConstraints().directions = new SimplexComponentDirections(0,0,0.01,0.01);
        set.formButtonContainer.add((T)closeFunction);

        form.setMainContainer(set.mainContainer);
        form.setPrintFunction(printFunction);
        form.setXlsFunction(xlsFunction);
        form.setNullFunction(nullFunction);
        form.setRefreshFunction(refreshFunction);
        form.setApplyFunction(applyFunction);
        form.setCancelFunction(cancelFunction);
        form.setOkFunction(okFunction);
        form.setCloseFunction(closeFunction);

        return set;
    }
}
