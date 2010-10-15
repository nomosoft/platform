/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package platform.client.form;

import lpsolve.LpSolve;
import lpsolve.LpSolveException;
import platform.base.OSUtils;
import platform.client.logics.ClientComponent;
import platform.client.logics.ClientContainer;
import platform.interop.form.layout.*;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.*;
import java.util.List;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * @author NewUser
 */
public class SimplexLayout implements LayoutManager2, ComponentListener {
    private final static Logger logger = Logger.getLogger(SimplexLayout.class.getName());
    public boolean disableLayout = false;

    Dimension oldDimension;
    Map<List<Component>, Map<Component, Rectangle>> cache = new HashMap<List<Component>, Map<Component, Rectangle>>();

    List<Component> allComponents = new ArrayList<Component>();
    Map<Component, ClientComponent> constraints = new HashMap<Component, ClientComponent>();

    private Container mainContainer;

    public SimplexLayout(Container imainContainer) {
        mainContainer = imainContainer;
    }

    public SimplexLayout(Container imainContainer, ClientContainer clientComponent) {
        mainContainer = imainContainer;
        constraints.put(mainContainer, clientComponent);
    }

    public void addLayoutComponent(String name, Component comp) {
        addLayoutComponent(comp);
    }

    public void addLayoutComponent(Component comp, Object constraint) {
        addLayoutComponent(comp);
        if (constraint != null) {
            ClientComponent clientComponent = (ClientComponent) constraint;
            constraints.put(comp, clientComponent);
        }
    }

    private void addLayoutComponent(Component comp) {

        if (allComponents.indexOf(comp) == -1) {

            allComponents.add(comp);
            componentsChanged = true;
            comp.addComponentListener(this);
        }
    }

    public void removeLayoutComponent(Component comp) {

        allComponents.remove(comp);
        constraints.remove(comp);

        componentsChanged = true;
        comp.removeComponentListener(this);
    }

    public void componentResized(ComponentEvent e) {
    }

    public void componentMoved(ComponentEvent e) {
    }

    // приходится вызывать invalidate, поскольку это событие обрабатывается в EventDispatchingThread и приходит позже нормального validate
    public void componentShown(ComponentEvent e) {
        if (!componentsChanged) {
            componentsChanged = true;
            mainContainer.invalidate();
        }
    }

    public void componentHidden(ComponentEvent e) {
        if (!componentsChanged) {
            componentsChanged = true;
            mainContainer.invalidate();
        }
   }

    public Dimension preferredLayoutSize(Container parent) {
        return new Dimension(500, 500);
    }

    public Dimension minimumLayoutSize(Container parent) {
        return new Dimension(1, 1);
    }

    public Dimension maximumLayoutSize(Container target) {
        return new Dimension(10000, 10000);
    }

    private boolean componentsChanged = true;
    private List<Component> components;

    boolean fillVisibleComponents() {

        components = new ArrayList<Component>();
        for (Component component : allComponents) {

            boolean hasChild = hasNotContainerChild(component);

            // таким образом прячем и показываем контейнеры в зависимости от необходимости
            // если этого не делать, почему-то ничего не показывает - лень разбираться почему
            if (component instanceof ClientFormContainer) component.setVisible(hasChild);

            if (hasChild)
                components.add(component);
        }

        componentsChanged = false;
        return components.isEmpty();
    }

    private boolean hasNotContainerChild(Component component) {

        if (!(component instanceof ClientFormContainer))
            return component.isVisible();

        ClientFormContainer container = (ClientFormContainer) component;
        for (Component child : container.getComponents())
            if (hasNotContainerChild(child))
                return true;

        return false;
    }

    public void layoutContainer(final Container parent) {

        if (disableLayout) return;

        if (parent != mainContainer) return;


        Dimension dimension = parent.getSize();
        boolean dimensionChanged = true;
        if (oldDimension != null) {
            dimensionChanged = (Math.abs(dimension.getHeight() - oldDimension.getHeight()) +
                                Math.abs(dimension.getWidth() - oldDimension.getWidth()) > 0);
        }

        if (!dimensionChanged && !componentsChanged) return;

        if (fillVisibleComponents()) return;

        if (!dimensionChanged) {

            Map<Component, Rectangle> cachedCoords = cache.get(components);
            if (cachedCoords != null) {
                for (Component comp : components)
                    comp.setBounds(cachedCoords.get(comp));
                return;
            }
        } else {
            //System.out.println("Old " + oldDimension);
            //System.out.println("New " + dimension);
            //System.out.println("Parent " + parent);
            oldDimension = dimension;
            cache.clear();
        }

        logger.info("Begin layoutContainer " + dimension);

        LpSolve solver = null;

        try {

            solver = LpSolve.makeLp(0, 0);

            fillComponentVariables(solver);
            fillComponentConstraints(solver, parent);
            fillConstraints(solver);

            fillObjFunction(solver);

            solver.setTimeout(5);

            solver.setVerbose(LpSolve.IMPORTANT);
            solver.setOutputfile("");

            int res = solver.solve();
            if (res < 2) {

                setComponentsBounds(solver.getPtrVariables());

                Map<Component, Rectangle> cachedCoords = new HashMap<Component, Rectangle>();
                for (Component comp : components)
                    cachedCoords.put(comp, comp.getBounds());

                cache.put(components, cachedCoords);
            }

        } catch (LpSolveException ex) {
//            Logger.getLogger(SimplexLayout.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            // delete the problem and free memory
            if (solver != null)
                solver.deleteLp();
        }
        logger.info("End layoutContainer");
//        System.out.println("Layout complete : " + (System.currentTimeMillis()-stl));
    }

    private Map<Component, SimplexComponentInfo> infos;
    private SimplexComponentInfo targetInfo;

    private void fillComponentVariables(LpSolve solver) throws LpSolveException {

        infos = new HashMap<Component, SimplexComponentInfo>();

        int columnNumber = 0;

        targetInfo = new SimplexComponentInfo();
        targetInfo.L = ++columnNumber;
        targetInfo.T = ++columnNumber;
        targetInfo.R = ++columnNumber;
        targetInfo.B = ++columnNumber;

        for (Component component : components) {

            SimplexComponentInfo info = new SimplexComponentInfo();
            info.L = ++columnNumber;
            info.T = ++columnNumber;
            info.R = ++columnNumber;
            info.B = ++columnNumber;
            infos.put(component, info);
        }

        for (int i = 0; i < columnNumber; i++) {
            solver.addColumn(new double[0]);
        }

    }

    private void fillComponentConstraints(LpSolve solver, Container parent) throws LpSolveException {

        solver.addConstraintex(1, new double[]{1}, new int[]{targetInfo.L}, LpSolve.EQ, 0);
        solver.addConstraintex(1, new double[]{1}, new int[]{targetInfo.T}, LpSolve.EQ, 0);

        solver.addConstraintex(1, new double[]{1}, new int[]{targetInfo.R}, LpSolve.EQ, parent.getWidth());
        solver.addConstraintex(1, new double[]{1}, new int[]{targetInfo.B}, LpSolve.EQ, parent.getHeight());

        for (Component component : components) {

            Dimension min = component.getMinimumSize();
            Dimension max = component.getMaximumSize();

            SimplexComponentInfo info = infos.get(component);

            //добавляем везде 1, иначе на округлении она теряется
            solver.addConstraintex(2, new double[]{1, -1}, new int[]{info.R, info.L}, LpSolve.GE, min.width);
            solver.addConstraintex(2, new double[]{1, -1}, new int[]{info.B, info.T}, LpSolve.GE, min.height);

            //приходится убирать ограничение на макс. размер, если растягивается объект, иначе ни один растягиваться не будет
            // upd : вилимо можно и не убирать
//            if (constraints.get(component).fillHorizontal == 0)
            solver.addConstraintex(2, new double[]{1, -1}, new int[]{info.R, info.L}, LpSolve.LE, max.width + 1.0);

//            if (constraints.get(component).fillVertical == 0)
            solver.addConstraintex(2, new double[]{1, -1}, new int[]{info.B, info.T}, LpSolve.LE, max.height);
        }

    }

    private void fillConstraints(LpSolve solver) throws LpSolveException {

        for (Component comp : components)
            fillInsideConstraint(solver, comp);

        for (Component parent : components)
            fillSiblingsConstraint(solver, parent);

        fillSiblingsConstraint(solver, mainContainer);

    }

    private void fillInsideConstraint(LpSolve solver, Component comp) throws LpSolveException {

        if (components.indexOf(comp.getParent()) != -1)
            SingleSimplexConstraint.IS_INSIDE.fillConstraint(solver, infos.get(comp), infos.get(comp.getParent()), getConstraint(comp), getConstraint(comp.getParent()), comp, comp.getParent());
        else
            SingleSimplexConstraint.IS_INSIDE.fillConstraint(solver, infos.get(comp), targetInfo, getConstraint(comp), getConstraint(comp.getParent()), comp, comp.getParent());
    }

    private SimplexConstraints<ClientComponent> getConstraint(Component comp) {
        ClientComponent component = constraints.get(comp);
        return component != null
               ? component.constraints
               : SimplexConstraints.DEFAULT_CONSTRAINT;
    }

    private void fillSiblingsConstraint(LpSolve solver, Component parent) throws LpSolveException {

        List<Component> contComponents = new ArrayList<Component>();

        // если для этого объекта есть свой ClientContainer
        if (constraints.containsKey(parent) && constraints.get(parent) instanceof ClientContainer) {
        }

        for (Component comp : components)
            if (comp.getParent() == parent) {
                contComponents.add(comp);
            }

        SimplexConstraints parentConstraints = getConstraint(parent);

        int maxVar = parentConstraints.maxVariables;

        int compCount = 0;
        //упорядочиваем компоненты по их order'у и запихиваем в comporder
        TreeMap<Integer, ArrayList<Component>> comporder = new TreeMap<Integer, ArrayList<Component>>();
        for (Component comp : contComponents) {
            Integer order = getConstraint(comp).order;
            ArrayList<Component> alc = comporder.get(order);
            if (alc == null) alc = new ArrayList<Component>();
            alc.add(comp);
            comporder.put(order, alc);
            compCount++;
        }

        if (compCount < 2) return;

        List<Component> order = new ArrayList<Component>();

        Map<Component, SimplexSolverDirections> vars = new HashMap<Component, SimplexSolverDirections>();

        //бъем все компоненты на группы (maxVar - 1)
        //для каждой группы создаем одну переменную направлений curDir, и одна переменная направлений globalDir для всех групп
        int maxCol = (maxVar < 3) ? 1 : ((compCount - 1) / (maxVar - 1) + 1);

        SimplexSolverDirections curDir = null;
        int curCol = 0;

        Iterator<Integer> it = comporder.navigableKeySet().iterator();
        int curCount = 0;
        while (it.hasNext()) {

            ArrayList<Component> complist = comporder.get(it.next());

            for (Component comp : complist) {

                if (curCol == 0 && maxCol > 1 && compCount - curCount > 1)
                    curDir = new SimplexSolverDirections(solver, parentConstraints.childConstraints.forbDir);

                order.add(comp);
                vars.put(comp, curDir);

                curCount++;
                curCol++;
                if (curCol == maxCol) {
                    curCol = 0;
                }
            }
        }

        SimplexSolverDirections globalDir = new SimplexSolverDirections(solver, parentConstraints.childConstraints.forbDir);

        // замыкаем пересечения, чтобы посчитать заведомо верные условия
        Map<Component, Map<Component, DoNotIntersectSimplexConstraint>> intersects = new HashMap<Component, Map<Component, DoNotIntersectSimplexConstraint>>();
        for (Component comp1 : contComponents) {
            intersects.put(comp1, new HashMap<Component, DoNotIntersectSimplexConstraint>());
            for (Component comp2 : contComponents) {
                DoNotIntersectSimplexConstraint cons = getConstraint(comp1).intersects.get(constraints.get(comp2));
                if (comp1 != comp2 && cons != null && cons.isStraight()) {
                    intersects.get(comp1).put(comp2, cons);
                }
            }
        }

        for (Component comp3 : contComponents)
            for (Component comp1 : contComponents) {
                DoNotIntersectSimplexConstraint inter13 = intersects.get(comp1).get(comp3);
                if (comp1 != comp3 && inter13 != null)
                    for (Component comp2 : contComponents) {
                        if (comp2 != comp1 && comp2 != comp3 && inter13.equals(intersects.get(comp3).get(comp2)))
                            intersects.get(comp1).put(comp2, inter13);
                }
            }



        for (Component comp1 : contComponents)
            for (Component comp2 : contComponents)
                if (comp1 != comp2 && !getConstraint(comp2).intersects.containsKey(constraints.get(comp1))) {

                    if (getConstraint(comp1).intersects.containsKey(constraints.get(comp2))) {
                        getConstraint(comp1).intersects.get(constraints.get(comp2)).fillConstraint(solver, infos.get(comp1), infos.get(comp2), getConstraint(comp1), getConstraint(comp2), null);
                    } else {

                        // проверка на избыточные условия пересечения
                        if (intersects.get(comp1).get(comp2) != null || intersects.get(comp2).get(comp1) != null)
                            continue;

                        int order1 = order.indexOf(comp1);
                        int order2 = order.indexOf(comp2);

                        if (order1 > order2)
                            continue;

                        SimplexComponentInfo info1 = infos.get(comp1); //(order1 < order2) ? infos.get(comp1) : infos.get(comp2);
                        SimplexComponentInfo info2 = infos.get(comp2); //(order1 < order2) ? infos.get(comp2) : infos.get(comp1);

                        SimplexSolverDirections dir = globalDir;
                        // для компонент из одной "группы" используем одни и те же переменные, из разных - globalDir
                        if (vars.get(comp1) == vars.get(comp2) && vars.get(comp1) != null) dir = vars.get(comp1);

                        parentConstraints.childConstraints.fillConstraint(solver, info1, info2, getConstraint(comp1), getConstraint(comp2), (maxVar == 0 ? null : dir));
                    }

                }

    }

    // На текущей момент основными целевыми функциями являются :
    // Растянуть как можно больше в высоту пропорционально fillVertical с коэффициентом 1000
    // Растянуть как можно больше в высоту пропорционально fillHorizontal с коэффициентом 1000
    // Сделать как можно ближе к Preferred размерам с коэффициентом -100 и 100
    // Сделать как можно больше те, у кого установлен fillVertical или fillHorizontal с коэффициентом 1
    // Сделать как можно выше, левее с коэффициентом 0.01

    private void fillObjFunction(LpSolve solver) throws LpSolveException {

        solver.addColumn(new double[0]);

        int colmaxw = solver.getNcolumns();
        boolean fillmaxw = false;

        solver.addColumn(new double[0]);
        int colmaxh = solver.getNcolumns();
        boolean fillmaxh = false;

        List<Double> objFnc = new ArrayList<Double>();
        for (int i = 0; i < solver.getNcolumns() + 1; i++)
            objFnc.add(0.0);

//        double[] objFnc = new double[solver.getNcolumns()+1];

//        List<Integer> extraObjFnc;

        for (Component component : components) {

            Dimension max = component.getMaximumSize();
            Dimension pref = component.getPreferredSize();
            double prefCoeff = 50.0 / pref.width;

            SimplexComponentInfo info = infos.get(component);

            SimplexConstraints constraint = getConstraint(component);

            // нужно проверять на максимальный размер, иначе кнопка раскрытия дерева сильно ограничит сверху colmaxw
            if (constraint.fillHorizontal > 0 && max.getWidth() >= mainContainer.getWidth()) {
                solver.addConstraintex(3, new double[]{1, -1, -1 * constraint.fillHorizontal}, new int[]{info.R, info.L, colmaxw}, LpSolve.GE, 0);
                fillmaxw = true;
            } else {

                // Preferred size
                if (constraint.fillHorizontal >= 0) {

                    solver.addColumn(new double[0]);
                    int var = solver.getNcolumns();
                    solver.addConstraintex(1, new double[]{1}, new int[]{var}, LpSolve.GE, pref.width + 1.0);
                    solver.addConstraintex(3, new double[]{1, -1, 1}, new int[]{var, info.R, info.L}, LpSolve.GE, 0);
                    objFnc.add(-100.0 - prefCoeff);

                    solver.addColumn(new double[0]);
                    var = solver.getNcolumns();
                    solver.addConstraintex(1, new double[]{1}, new int[]{var}, LpSolve.LE, pref.width + 1.0);
                    solver.addConstraintex(3, new double[]{1, -1, 1}, new int[]{var, info.R, info.L}, LpSolve.LE, 0);
                    objFnc.add(100.0 + prefCoeff);
                }
            }

            if (constraint.fillVertical > 0 && max.getHeight() >= mainContainer.getHeight()) {
                solver.addConstraintex(3, new double[]{1, -1, -1 * constraint.fillVertical}, new int[]{info.B, info.T, colmaxh}, LpSolve.GE, 0);
                fillmaxh = true;
            } else {

                // Preferred size
                if (constraint.fillVertical >= 0) {

                    solver.addColumn(new double[0]);
                    int var = solver.getNcolumns();
                    solver.addConstraintex(1, new double[]{1}, new int[]{var}, LpSolve.GE, pref.height);
                    solver.addConstraintex(3, new double[]{1, -1, 1}, new int[]{var, info.B, info.T}, LpSolve.GE, 0);
                    objFnc.add(-100.0 - prefCoeff);

                    solver.addColumn(new double[0]);
                    var = solver.getNcolumns();
                    solver.addConstraintex(1, new double[]{1}, new int[]{var}, LpSolve.LE, pref.height);
                    solver.addConstraintex(3, new double[]{1, -1, 1}, new int[]{var, info.B, info.T}, LpSolve.LE, 0);
                    objFnc.add(100.0 + prefCoeff);
                }
            }

            // направления и расширения до максимума
            objFnc.set(info.T, -constraint.directions.T + ((constraint.fillVertical > 0) ? -1 : 0.0));
            objFnc.set(info.L, -constraint.directions.L + ((constraint.fillHorizontal > 0) ? -1 : 0.0));
            objFnc.set(info.B, constraint.directions.B + ((constraint.fillVertical > 0) ? 1 : 0.0));
            objFnc.set(info.R, constraint.directions.R + ((constraint.fillHorizontal > 0) ? 1 : 0.0));

        }

        // самое важное условие - ему выдается самый большой коэффициент
        objFnc.set(colmaxw, (fillmaxw) ? 1000.0 : 0.0);
        objFnc.set(colmaxh, (fillmaxh) ? 1000.0 : 0.0);

        double[] objArr = new double[objFnc.size()];
        for (int i = 0; i < objFnc.size(); i++)
            objArr[i] = objFnc.get(i);
        solver.setObjFn(objArr);
//        solver.setObjFnex(2, new double[] {1, 1}, new int[] {maxcolw, maxcolh});
        solver.setMaxim();
    }


    private void setComponentsBounds(double[] coords) {

        for (Component comp : components) {

            SimplexComponentInfo info = infos.get(comp);

            int LP = 0, TP = 0;
            if (components.indexOf(comp.getParent()) != -1) {
                SimplexComponentInfo infoP = infos.get(comp.getParent());
                LP = (int) coords[infoP.L - 1];
                TP = (int) coords[infoP.T - 1];
            }


            comp.setBounds((int) Math.round(coords[info.L - 1] - LP), (int) Math.round(coords[info.T - 1] - TP),
                    (int) Math.round(coords[info.R - 1] - coords[info.L - 1]), (int) Math.round(coords[info.B - 1] - coords[info.T - 1]));
        }

    }

    public float getLayoutAlignmentX(Container target) {
        return (float) 0.0;
    }

    public float getLayoutAlignmentY(Container target) {
        return (float) 0.0;
    }

    public void invalidateLayout(Container target) {
    }

    // приходится делать не через dropLayoutCaches, поскольку механизм invalidate() считает, что отрисовка проходит всегда очень быстро
    // в итоге invalidate срабатывает даже при добавлении / удалении объектов

    public void dropCaches() {

        oldDimension = null;
        cache.clear();
    }

    public static void loadLibraries() throws IOException {
        OSUtils.loadLibrary("lpsolve55", "/platform/client/form/", SimplexLayout.class);
        OSUtils.loadLibrary("lpsolve55j", "/platform/client/form/", SimplexLayout.class);
    }
}
