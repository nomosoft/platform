package platform.gwt.form.client.form.ui;

import platform.gwt.cellview.client.Column;
import platform.gwt.cellview.client.KeyboardRowChangedEvent;
import platform.gwt.cellview.client.cell.Cell;
import platform.gwt.form.shared.view.GForm;
import platform.gwt.form.shared.view.GGroupObject;
import platform.gwt.form.shared.view.GOrder;
import platform.gwt.form.shared.view.GPropertyDraw;
import platform.gwt.form.shared.view.changes.GGroupObjectValue;
import platform.gwt.form.shared.view.grid.GridEditableCell;

import java.util.*;

import static java.util.Collections.singleton;

public class GTreeTable extends GGridPropertyTable<GTreeGridRecord> {
    private boolean dataUpdated;

    private ArrayList<GTreeGridRecord> currentRecords;

    private GTreeTableTree tree;

    private List<String> createdFields = new ArrayList<String>();

    private GTreeGridRecord selectedRecord;

    private Set<GTreeTableNode> expandedNodes;

    public GTreeTable(GFormController iformController, GForm iform) {
        super(iformController);

        tree = new GTreeTableTree(iform);
        Column<GTreeGridRecord, Object> column = new Column<GTreeGridRecord, Object>(new GTreeGridControlCell(this)) {
            @Override
            public Object getValue(GTreeGridRecord object) {
                return object.getAttribute("treeColumn");
            }
        };
        GGridPropertyTableHeader header = new GGridPropertyTableHeader(this, "Дерево");
        createdFields.add("treeColumn");
        headers.add(header);
        addColumn(column, header);
        setColumnWidth(column, "80px");

        addKeyboardRowChangedHandler(new KeyboardRowChangedEvent.Handler() {
            @Override
            public void onKeyboardRowChanged(KeyboardRowChangedEvent event) {
                final GTreeGridRecord kbSelectedRecord = getKeyboardSelectedRowValue();
                if (kbSelectedRecord != null && !kbSelectedRecord.equals(selectedRecord)) {
                    setCurrentRecord(kbSelectedRecord);
                    form.changeGroupObjectLater(kbSelectedRecord.getGroup(), kbSelectedRecord.getKey());
                }
            }
        });

        sortableHeaderManager = new GGridSortableHeaderManager<GPropertyDraw>(this, true) {
            @Override
            protected void orderChanged(GPropertyDraw columnKey, GOrder modiType) {
                form.changePropertyOrder(columnKey, GGroupObjectValue.EMPTY, modiType);
            }

            @Override
            protected GPropertyDraw getColumnKey(int column) {
                return tree.getColumnProperty(column);
            }
        };
    }

    public void removeProperty(GGroupObject group, GPropertyDraw property) {
        dataUpdated = true;
        int index = tree.removeProperty(group, property);
        if (index > 0) {
            removeColumn(index);
        }
    }

    public void addProperty(GGroupObject group, GPropertyDraw property) {
        dataUpdated = true;

        int index = tree.addProperty(group, property);

        if (index > -1) {
            if (!createdFields.contains(property.sID)) {
                Column<GTreeGridRecord, Object> gridColumn = createGridColumn(property);
                GGridPropertyTableHeader header = new GGridPropertyTableHeader(this, property.getCaptionOrEmpty());

                headers.add(index, header);
                insertColumn(index, gridColumn, header);
                createdFields.add(index, property.sID);

                setColumnWidth(gridColumn, property.getMinimumWidth());
            }
        }
    }

    private Column<GTreeGridRecord, Object> createGridColumn(final GPropertyDraw property) {
        return new Column<GTreeGridRecord, Object>(new GridEditableCell(this, true)) {
            @Override
            public Object getValue(GTreeGridRecord record) {
                int column = tree.columnProperties.indexOf(property);
                return tree.getValue(record.getGroup(), column, record.getKey());
            }
        };
    }

    public void setKeys(GGroupObject group, ArrayList<GGroupObjectValue> keys, ArrayList<GGroupObjectValue> parents) {
        tree.setKeys(group, keys, parents);
        dataUpdated = true;
        needToRestoreScrollPosition = true;
    }

    public void updatePropertyValues(GPropertyDraw property, Map<GGroupObjectValue, Object> propValues, boolean updateKeys) {
        if (propValues != null) {
            dataUpdated = true;
            tree.setPropertyValues(property, propValues, updateKeys);
        }
    }

    @Override
    public void updateCellBackgroundValues(GPropertyDraw propertyDraw, Map<GGroupObjectValue, Object> values) {
        super.updateCellBackgroundValues(propertyDraw, values);
        dataUpdated = true;
    }

    @Override
    public void updateCellForegroundValues(GPropertyDraw propertyDraw, Map<GGroupObjectValue, Object> values) {
        super.updateCellForegroundValues(propertyDraw, values);
        dataUpdated = true;
    }

    @Override
    public void updateRowBackgroundValues(Map<GGroupObjectValue, Object> values) {
        super.updateRowBackgroundValues(values);
        dataUpdated = true;
    }

    @Override
    public void updateRowForegroundValues(Map<GGroupObjectValue, Object> values) {
        super.updateRowForegroundValues(values);
        dataUpdated = true;
    }

    public void update() {
        storeScrollPosition();

        if (dataUpdated) {
            restoreVisualState();

            currentRecords = tree.getUpdatedRecords();
            updatePropertyReaders();
            setRowData(currentRecords);
            redraw();

            dataUpdated = false;
        }

        updateHeader();
    }

    protected void updatePropertyReaders() {
        if (currentRecords != null &&
                //раскраска в дереве - редкое явление, поэтому сразу проверяем есть ли она
                (rowBackgroundValues.size() != 0 || rowForegroundValues.size() != 0 || cellBackgroundValues.size() != 0 || cellForegroundValues.size() != 0)) {
            for (GridDataRecord record : currentRecords) {
                GGroupObjectValue key = record.getKey();

                Object rBackground = rowBackgroundValues.get(key);
                Object rForeground = rowForegroundValues.get(key);

                List<GPropertyDraw> columnProperties = getColumnProperties();
                for (int j = 0; j < columnProperties.size(); j++) {
                    GPropertyDraw property = columnProperties.get(j);

                    Object background = rBackground;
                    if (background == null) {
                        Map<GGroupObjectValue, Object> propBackgrounds = cellBackgroundValues.get(property);
                        if (propBackgrounds != null) {
                            background = propBackgrounds.get(key);
                        }
                    }

                    Object foreground = rForeground;
                    if (foreground == null) {
                        Map<GGroupObjectValue, Object> propForegrounds = cellForegroundValues.get(property);
                        if (propForegrounds != null) {
                            foreground = propForegrounds.get(key);
                        }
                    }

                    record.setBackground(j + 1, background == null ? property.background : background);
                    record.setForeground(j + 1, foreground == null ? property.foreground : foreground);
                }
            }
        }
    }

    protected void updateHeader() {
        boolean needsHeaderRefresh = false;
        rowHeight = 0.0;
        for (GPropertyDraw property : getColumnProperties()) {
            Map<GGroupObjectValue, Object> captions = propertyCaptions.get(property);
            if (captions != null) {
                Object value = captions.values().iterator().next();
                headers.get(getColumnIndex(property)).setCaption(value == null ? "" : value.toString().trim());
                needsHeaderRefresh = true;
            }
            rowHeight = Math.max(rowHeight, property.getMinimumPixelHeight());
        }
        if (needsHeaderRefresh) {
            refreshHeaders();
        }
    }

    public void fireExpandNode(GTreeGridRecord record) {
        saveVisualState();
        GTreeTableNode node = tree.getNodeByRecord(record);
        if (node != null) {
            expandedNodes.add(node);
            form.expandGroupObject(node.getGroup(), node.getKey());
        }
    }

    public void fireCollapseNode(GTreeGridRecord record) {
        saveVisualState();
        GTreeTableNode node = tree.getNodeByRecord(record);
        if (node != null) {
            expandedNodes.remove(node);
            form.collapseGroupObject(node.getGroup(), node.getKey());
        }
    }

    public void saveVisualState() {
        expandedNodes = new HashSet<GTreeTableNode>();
        expandedNodes.addAll(getExpandedChildren(tree.root));
    }

    private List<GTreeTableNode> getExpandedChildren(GTreeTableNode node) {
        List<GTreeTableNode> exChildren = new ArrayList<GTreeTableNode>();
        for (GTreeTableNode child : node.getChildren()) {
            if (child.isOpen()) {
                exChildren.add(child);
                exChildren.addAll(getExpandedChildren(child));
            }
        }
        return exChildren;
    }

    public void restoreVisualState() {
        for (GTreeTableNode node : tree.root.getChildren()) {
            expandNode(node);
        }
    }

    private void setCurrentRecord(GTreeGridRecord record) {
        this.selectedRecord = record;
    }

    public GTreeGridRecord getSelectedRecord() {
        return selectedRecord;
    }

    public GGroupObjectValue getCurrentKey() {
        return selectedRecord == null ? GGroupObjectValue.EMPTY : selectedRecord.getKey();
    }

    public GPropertyDraw getCurrentProperty() {
        GPropertyDraw property = getSelectedProperty();
        if (property == null && getColumnCount() > 1) {
            property = tree.getColumnProperty(1);
        }
        return property;
    }

    public Object getSelectedValue(GPropertyDraw property) {
        GTreeGridRecord record = getSelectedRecord();
        return record == null ? null : tree.getValue(record.getGroup(), getColumnIndex(property) - 1, record.getKey());
    }

    private void expandNode(GTreeTableNode node) {
        if (expandedNodes != null && expandedNodes.contains(node) && !tree.hasOnlyExpandningNodeAsChild(node)) {
            node.setOpen(true);
            for (GTreeTableNode child : node.getChildren()) {
                expandNode(child);
            }
        } else {
            node.setOpen(false);
        }
    }

    public List<GPropertyDraw> getColumnProperties() {
        return tree.columnProperties;
    }

    public int getColumnIndex(GPropertyDraw property) {
        return getColumnProperties().indexOf(property) + 1;
    }

    private GGroupObject getRowGroup(int row) {
        return currentRecords.get(row).getGroup();
    }

    @Override
    public GPropertyDraw getProperty(Cell.Context context) {
        return tree.getProperty(getRowGroup(context.getIndex()), context.getColumn() - 1);
    }

    @Override
    public GGroupObjectValue getColumnKey(Cell.Context context) {
        return currentRecords.get(context.getIndex()).getKey();
    }

    @Override
    public boolean isEditable(Cell.Context context) {
        if (context.getColumn() != 0) {
            GPropertyDraw property = getProperty(context);
            return property != null && !property.isReadOnly();
        }
        return false;
    }

    @Override
    public Object getValueAt(Cell.Context context) {
        GTreeGridRecord record = (GTreeGridRecord) context.getRowValue();
        return tree.getValue(record.getGroup(), context.getColumn() - 1, record.getKey());
    }

    @Override
    public void setValueAt(Cell.Context context, Object value) {
        int row = context.getIndex();
        int column = context.getColumn();

        GTreeGridRecord rowRecord = (GTreeGridRecord) context.getRowValue();

        GPropertyDraw property = getProperty(context);
        rowRecord.setAttribute(property.sID, value);

        tree.putValue(property, rowRecord.getKey(), value);

        setRowData(row, Arrays.asList(rowRecord));
        redrawColumns(singleton(getColumn(column)), false);
    }

    public void changeOrder(GPropertyDraw property, GOrder modiType) {
        int propertyIndex = tree.getPropertyColumnIndex(property);
        if (propertyIndex > 0) {
            sortableHeaderManager.changeOrder(property, modiType);
        } else {
            //меняем напрямую для верхних groupObjects
            form.changePropertyOrder(property, GGroupObjectValue.EMPTY, modiType);
        }
    }
}
