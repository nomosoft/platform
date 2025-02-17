package lsfusion.gwt.client.form.object.table.grid.view;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import lsfusion.gwt.client.base.GAsync;
import lsfusion.gwt.client.base.GwtClientUtils;
import lsfusion.gwt.client.base.Pair;
import lsfusion.gwt.client.base.jsni.NativeHashMap;
import lsfusion.gwt.client.base.view.ColorUtils;
import lsfusion.gwt.client.base.view.EventHandler;
import lsfusion.gwt.client.base.view.grid.DataGrid;
import lsfusion.gwt.client.classes.GType;
import lsfusion.gwt.client.classes.data.GImageType;
import lsfusion.gwt.client.classes.data.GIntegralType;
import lsfusion.gwt.client.classes.data.GJSONType;
import lsfusion.gwt.client.classes.data.GLogicalType;
import lsfusion.gwt.client.form.controller.GFormController;
import lsfusion.gwt.client.form.filter.user.GCompare;
import lsfusion.gwt.client.form.filter.user.GFilter;
import lsfusion.gwt.client.form.filter.user.GPropertyFilter;
import lsfusion.gwt.client.form.object.GGroupObjectValue;
import lsfusion.gwt.client.form.object.table.grid.controller.GGridController;
import lsfusion.gwt.client.form.property.GPropertyDraw;
import lsfusion.gwt.client.form.property.async.GPushAsyncInput;
import lsfusion.gwt.client.form.property.async.GPushAsyncResult;
import lsfusion.gwt.client.form.property.cell.GEditBindingMap;
import lsfusion.gwt.client.form.property.cell.classes.GDateDTO;
import lsfusion.gwt.client.form.property.cell.classes.GDateTimeDTO;
import lsfusion.gwt.client.form.property.cell.view.GUserInputResult;
import lsfusion.gwt.client.form.view.Column;
import lsfusion.gwt.client.view.StyleDefaults;
import lsfusion.interop.action.ServerResponse;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static lsfusion.gwt.client.base.view.grid.DataGrid.initSinkEvents;

public abstract class GSimpleStateTableView<P> extends GStateTableView {

    protected final JavaScriptObject controller;

    public GSimpleStateTableView(GFormController form, GGridController grid) {
        super(form, grid);

        this.controller = getController();
        GwtClientUtils.setZeroZIndex(getDrawElement());

        getElement().setTabIndex(0);
        initSinkEvents(this);

        getElement().setPropertyObject("groupObject", grid.groupObject);
    }

    @Override
    public void onBrowserEvent(Event event) {
        Element target = DataGrid.getTargetAndCheck(getElement(), event);
        if(target == null || (popupObject != null && getPopupElement().isOrHasChild(target))) // if there is a popupElement we'll consider it not to be part of this view (otherwise on mouse change event focusElement.focus works, and popup panel elements looses focus)
            return;
        if(!form.previewEvent(target, event))
            return;

        super.onBrowserEvent(event);

        if(!DataGrid.checkSinkEvents(event))
            return;

        form.onPropertyBrowserEvent(new EventHandler(event), getCellParent(target), getElement(),
                handler -> {}, // no outer context
                handler -> {}, // no edit
                handler -> {}, // no outer context
                handler -> {}, handler -> {}, // no copy / paste for now
                false, true
        );
    }

    protected abstract Element getCellParent(Element target);

    private NativeHashMap<String, Column> columnMap;

    @Override
    protected void updateView() {
        columnMap = new NativeHashMap<>();
        JsArray<JavaScriptObject> list = convertToObjectsMixed(getData(columnMap));

        if(popupObject != null && !isCurrentKey(popupKey)) // if another current key set hiding popup
            hidePopup();

        onUpdate(getDrawElement(), list);
    }

    protected abstract void onUpdate(Element element, JsArray<JavaScriptObject> list);

    @Override
    protected Element getRendererAreaElement() {
        return getElement();
    }

    // we need key / value view since pivot
    private JsArray<JsArray<JavaScriptObject>> getData(NativeHashMap<String, Column> columnMap) {
        JsArray<JsArray<JavaScriptObject>> array = JavaScriptObject.createArray().cast();

        array.push(getCaptions(columnMap, null));

        // getting values
        if(keys != null)
            for (GGroupObjectValue key : keys) { // can be null if manual update
                array.push(getValues(key));
            }
        return array;
    }

    private JsArray<JavaScriptObject> getValues(GGroupObjectValue key) {
        JsArray<JavaScriptObject> rowValues = JavaScriptObject.createArray().cast();
        for (int i = 0; i < properties.size(); i++) {
            GPropertyDraw property = properties.get(i);
            List<GGroupObjectValue> propColumnKeys = columnKeys.get(i);
            NativeHashMap<GGroupObjectValue, Object> propValues = values.get(i);

            for (int j = 0; j < propColumnKeys.size(); j++) {
                GGroupObjectValue columnKey = propColumnKeys.get(j);
                if(checkShowIf(i, columnKey))
                    continue;

                GGroupObjectValue fullKey = key != null ? GGroupObjectValue.getFullKey(key, columnKey) : GGroupObjectValue.EMPTY;

                rowValues.push(convertToJSValue(property, propValues.get(fullKey)));
            }
        }
        rowValues.push(fromObject(key));
        return rowValues;
    }
    public static Object convertFromJSValue(GType type, JavaScriptObject value) {
        // have to reverse convertToJSValue as well as convertFileValue (???)
        if (type instanceof GLogicalType) {
            if(!((GLogicalType) type).threeState)
                return toBoolean(value) ? true : null;

            if(value != null)
                return toBoolean(value);
        }
        if(value == null)
            return null;
        if(type instanceof GIntegralType)
            return ((GIntegralType) type).convertDouble(toDouble(value));
        if(type instanceof GJSONType)
            return GwtClientUtils.jsonStringify(value);

        return toObject(value);
    }
    public static JavaScriptObject convertToJSValue(GType type, Object value) {
        if (type instanceof GLogicalType) {
            if(!((GLogicalType) type).threeState)
                return fromBoolean(value != null);

            if(value != null)
                return fromBoolean((boolean) value);
        }
        if(value == null)
            return null;
        if(type instanceof GIntegralType)
            return fromDouble(((Number)value).doubleValue());
        if(type instanceof GImageType)
            return fromString(GwtClientUtils.getAppDownloadURL((String) value, null, ((GImageType)type).extension));
        if(type instanceof GJSONType)
            return GwtClientUtils.jsonParse((String)value);

        return fromString(value.toString());
    }

    public static JavaScriptObject convertToJSValue(GPropertyDraw property, Object value) {
        return convertToJSValue(property.baseType, value);
    }

    protected JsArray<JavaScriptObject> getCaptions(NativeHashMap<String, Column> columnMap, Predicate<GPropertyDraw> filter) {
        JsArray<JavaScriptObject> columns = JavaScriptObject.createArray().cast();
        for (int i = 0, size = properties.size() ; i < size; i++) {
            GPropertyDraw property = properties.get(i);
            if (filter!= null && !filter.test(property))
                continue;

            List<GGroupObjectValue> propColumnKeys = columnKeys.get(i);
            for (int c = 0; c < propColumnKeys.size(); c++) {
                GGroupObjectValue columnKey = propColumnKeys.get(c);
                if(checkShowIf(i, columnKey))
                    continue;

                String columnName = property.integrationSID + (columnKey.isEmpty() ? "" : "_" + c);
                columnMap.put(columnName, new Column(property, columnKey));
                columns.push(fromString(columnName));
            }
        }
        if (filter == null)
            columns.push(fromString(keysFieldName));
        return columns;
    }

    protected final static String keysFieldName = "#__key";

    protected void changeSimpleGroupObject(JavaScriptObject object, boolean rendered, P elementClicked) {
        GGroupObjectValue key = getKey(object);

        long requestIndex;
        if(!GwtClientUtils.nullEquals(this.currentKey, key))
            requestIndex = changeGroupObject(key, rendered);
        else
            requestIndex = -2; // we're not waiting for any response, just show popup as it is

        Widget recordView;
        if(elementClicked != null && (recordView = grid.recordView) != null) {
            if(popupObject != null)
                hidePopup();

            popupKey = key;
            popupElementClicked = elementClicked;

            if (recordView.isVisible())
                showPopup();
            else
                popupRequestIndex = requestIndex;
        }
    }

    private void showPopup() {
        popupObject = showPopup(popupElementClicked, getPopupElement());

        popupRequestIndex = -2; // we are no longer waiting for popup
        popupElementClicked = null; // in theory it's better to do it on popupObject close, but this way is also ok
    }

    private Element getPopupElement() {
        return grid.recordView.getElement();
    }

    private void hidePopup() {
        hidePopup(popupObject);

        popupObject = null;
        popupKey = null;
    }

    protected abstract JavaScriptObject showPopup(P popupElementClicked, Element popupElement);

    protected abstract void hidePopup(JavaScriptObject popup);

    private JavaScriptObject popupObject;
    private P popupElementClicked = null;
    private GGroupObjectValue popupKey = null;
    private long popupRequestIndex = -2;

    @Override
    public void updateRecordLayout(long requestIndex) {
        Widget recordView = grid.recordView;
        if(popupObject == null) { // no popup
            if(requestIndex <= popupRequestIndex && recordView.isVisible()) // but has to be
                showPopup();
        } else {
            if(!recordView.isVisible())
                hidePopup();
        }
    }

    protected void changeJSProperty(String column, JavaScriptObject object, JavaScriptObject newValue) {
        changeProperty(column, object, (Serializable) GSimpleStateTableView.convertFromJSValue(columnMap.get(column).property.getExternalChangeType(), newValue));
    }

    protected void changeProperty(String column, JavaScriptObject object, Serializable newValue) {
        changeProperties(new String[]{column}, new JavaScriptObject[]{object}, new Serializable[]{newValue});
    }

    public static final String UNDEFINED = "undefined";

    protected void changeProperties(String[] columns, JavaScriptObject[] objects, Serializable[] newValues) {
        int length = columns.length;
        GPropertyDraw[] properties = new GPropertyDraw[length];
        GGroupObjectValue[] fullKeys = new GGroupObjectValue[length];
        boolean[] externalChanges = new boolean[length];
        GPushAsyncResult[] pushAsyncResults = new GPushAsyncResult[length];

        for (int i = 0; i < length; i++) {
            Column column = columnMap.get(columns[i]);
            properties[i] = column.property;
            fullKeys[i] = GGroupObjectValue.getFullKey(getChangeKey(objects[i]), column.columnKey);
            externalChanges[i] = true;
            Serializable newValue = newValues[i];
            pushAsyncResults[i] = newValue == UNDEFINED ? null : new GPushAsyncInput(new GUserInputResult(newValue));
        }

        Consumer<Long> onExec = changeRequestIndex -> {
            for (int i = 0; i < length; i++) {
                GPropertyDraw property = properties[i];
                if(newValues[i] != UNDEFINED && property.canUseChangeValueForRendering(property.getExternalChangeType())) { // or use the old value instead of the new value in that case
                    GGroupObjectValue fullKey = fullKeys[i];
                    form.pendingChangeProperty(property, fullKey, newValues[i], getValue(property, fullKey), changeRequestIndex);
                }
            }
        };
        String actionSID = GEditBindingMap.CHANGE;
        if(length == 1 && newValues[0] == UNDEFINED)
            form.executePropertyEventAction(properties[0], fullKeys[0], actionSID, (GPushAsyncInput) pushAsyncResults[0], externalChanges[0], onExec);
        else
            onExec.accept(form.asyncExecutePropertyEventAction(actionSID, null, null, properties, externalChanges, fullKeys, pushAsyncResults));
    }

    protected boolean isReadOnly(String property, GGroupObjectValue object) {
        Column column = columnMap.get(property);
        if(column == null)
            return false;
        return isReadOnly(column.property, object, column.columnKey);
    }

    protected boolean isReadOnly(String property, JavaScriptObject object) {
        return isReadOnly(property, getKey(object));
    }

    protected boolean isCurrentObjectKey(JavaScriptObject object){
        return isCurrentKey(getKey(object));
    }

    // change key can be outside view window, and can be obtained from getAsyncValues for example
    protected GGroupObjectValue getChangeKey(JavaScriptObject object) {
        if(object == null)
            return getSelectedKey();
        if(hasKey(object, keysFieldName))
            object = getValue(object, keysFieldName);
        return toObject(object);
    }
    protected GGroupObjectValue getKey(JavaScriptObject object) {
        return toObject(getValue(object, keysFieldName));
    }

    protected void changeDateTimeProperty(String property, JavaScriptObject object, int year, int month, int day, int hour, int minute, int second) {
        changeProperty(property, object, new GDateTimeDTO(year, month, day, hour, minute, second));
    }

    protected void changeDateProperty(String property, JavaScriptObject object, int year, int month, int day) {
        changeProperty(property, object, new GDateDTO(year, month, day));
    }

    protected void changeDateTimeProperties(String[] properties, JavaScriptObject[] objects, int[] years, int[] months, int[] days, int[] hours, int[] minutes, int[] seconds) {
        int length = objects.length;
        GDateTimeDTO[] gDateTimeDTOs = new GDateTimeDTO[length];
        for (int i = 0; i < length; i++) {
            gDateTimeDTOs[i] = new GDateTimeDTO(years[i], months[i], days[i], hours[i], minutes[i], seconds[i]);
        }
        changeProperties(properties, objects, gDateTimeDTOs);
    }

    protected void changeDateProperties(String[] properties, JavaScriptObject[] objects, int[] years, int[] months, int[] days) {
        int length = objects.length;
        GDateDTO[] gDateDTOs = new GDateDTO[length];
        for (int i = 0; i < length; i++) {
            gDateDTOs[i] = new GDateDTO(years[i], months[i], days[i]);
        }
        changeProperties(properties, objects, gDateDTOs);
    }

    protected void setDateIntervalViewFilter(String property, int pageSize, int startYear, int startMonth, int startDay, int endYear, int endMonth, int endDay, boolean isDateTimeFilter) {
        Object leftBorder = isDateTimeFilter ? new GDateTimeDTO(startYear, startMonth, startDay, 0, 0, 0) : new GDateDTO(startYear, startMonth, startDay) ;
        Object rightBorder = isDateTimeFilter ? new GDateTimeDTO(endYear, endMonth, endDay, 0, 0, 0) : new GDateDTO(endYear, endMonth, endDay);

        Column column = columnMap.get(property);
        setViewFilters(pageSize, new GPropertyFilter(new GFilter(column.property), grid.groupObject, column.columnKey, leftBorder, GCompare.GREATER_EQUALS),
                new GPropertyFilter(new GFilter(column.property), grid.groupObject, column.columnKey, rightBorder, GCompare.LESS_EQUALS));
    }

    protected void setBooleanViewFilter(String property, int pageSize) {
        Column column = columnMap.get(property);
        setViewFilters(pageSize, new GPropertyFilter(new GFilter(column.property), grid.groupObject, column.columnKey, true, GCompare.EQUALS));
    }

    private void setViewFilters(int pageSize, GPropertyFilter... filters) {
        form.setViewFilters(Arrays.stream(filters).collect(Collectors.toCollection(ArrayList::new)), pageSize);
        setPageSize(pageSize);
    }

    private String getPropertyJsValue(String property) {
        Column column = columnMap.get(property);
        Object value = column != null ? getJsValue(column.property, getSelectedKey(), column.columnKey) : null;
        return value != null ? value.toString() : null;
    }

    protected static String getThemedBackgroundColor(String color, boolean isCurrentKey) {
        if (isCurrentKey && color == null) {
            // for now focus color is not mixed with base cell color - as it is done in panel
            return StyleDefaults.getFocusedCellBackgroundColor(false);
        } else
            return ColorUtils.getThemedColor(color);
    }

    protected void getAsyncValues(String property, String value, JavaScriptObject successCallBack, JavaScriptObject failureCallBack) {
        Column column = columnMap.get(property);
        form.getAsyncValues(value, column.property, column.columnKey, ServerResponse.OBJECTS, new AsyncCallback<Pair<ArrayList<GAsync>, Boolean>>() {
            @Override
            public void onFailure(Throwable caught) {
                if(failureCallBack != null)
                    GwtClientUtils.call(failureCallBack);
            }

            @Override
            public void onSuccess(Pair<ArrayList<GAsync>, Boolean> result) {
                if(result.first == null) {
                    if(!result.second && failureCallBack != null)
                        GwtClientUtils.call(failureCallBack);
                    return;
                }

                JavaScriptObject[] results = new JavaScriptObject[result.first.size()];
                for (int i = 0; i < result.first.size(); i++) {
                    JavaScriptObject object = GwtClientUtils.newObject();
                    GAsync suggestion = result.first.get(i);
                    GwtClientUtils.setField(object, "displayString", fromString(suggestion.displayString));
                    GwtClientUtils.setField(object, "rawString", fromString(suggestion.rawString));
                    GwtClientUtils.setField(object, "key", fromObject(suggestion.key));
                    results[i] = object;
                }
                JavaScriptObject data = GwtClientUtils.newObject();
                GwtClientUtils.setField(data, "data", fromObject(results));
                GwtClientUtils.setField(data, "more", fromBoolean(result.second));
                GwtClientUtils.call(successCallBack, data);
         }});
    }

    NativeHashMap<GGroupObjectValue, JavaScriptObject> oldOptionsList = new NativeHashMap<>();
    private JavaScriptObject getDiff(JsArray<JavaScriptObject> list, boolean supportReordering) {
        List<JavaScriptObject> optionsToAdd = new ArrayList<>();
        List<JavaScriptObject> optionsToUpdate = new ArrayList<>();
        List<JavaScriptObject> optionsToRemove = new ArrayList<>();

        NativeHashMap<GGroupObjectValue, JavaScriptObject> mappedList = new NativeHashMap<>();
        for (int i = 0; i < list.length(); i++) {
            JavaScriptObject object = list.get(i);
            GwtClientUtils.setField(object, "index", fromDouble(i));
            mappedList.put(getKey(object), object);

            JavaScriptObject oldValue = oldOptionsList.remove(getKey(object));
            if (oldValue != null) {
                if (!GwtClientUtils.isJSObjectPropertiesEquals(object, oldValue)) {
                    if (supportReordering && Integer.parseInt(GwtClientUtils.getField(oldValue, "index").toString()) != i) {
                        optionsToRemove.add(oldValue);
                        optionsToAdd.add(object);
                    } else {
                        optionsToUpdate.add(object);
                    }
                }
            } else {
                optionsToAdd.add(object);
            }
        }

        oldOptionsList.foreachValue(optionsToRemove::add);
        oldOptionsList = mappedList;

        JavaScriptObject object = GwtClientUtils.newObject();
        GwtClientUtils.setField(object, "add", fromObject(optionsToAdd.toArray()));
        GwtClientUtils.setField(object, "update", fromObject(optionsToUpdate.toArray()));
        GwtClientUtils.setField(object, "remove", GwtClientUtils.sortArray(fromObject(optionsToRemove.toArray()), "index", true));
        return object;
    }

    protected native JavaScriptObject getController()/*-{
        var thisObj = this;
        return {
            changeProperty: function (property, object, newValue) {
                if(object === undefined)
                    object = null;
                if(newValue === undefined) // not passed
                    newValue = @GSimpleStateTableView::UNDEFINED;
                return thisObj.@GSimpleStateTableView::changeJSProperty(*)(property, object, newValue);
            },
            changeDateTimeProperty: function (property, object, year, month, day, hour, minute, second) {
                return thisObj.@GSimpleStateTableView::changeDateTimeProperty(*)(property, object, year, month, day, hour, minute, second);
            },
            changeDateProperty: function (property, object, year, month, day) {
                return thisObj.@GSimpleStateTableView::changeDateProperty(*)(property, object, year, month, day);
            },
            changeProperties: function (properties, objects, newValues) {
                return thisObj.@GSimpleStateTableView::changeProperties([Ljava/lang/String;[Lcom/google/gwt/core/client/JavaScriptObject;[Ljava/io/Serializable;)(properties, objects, newValues);
            },
            changeDateTimeProperties: function (properties, objects, years, months, days, hours, minutes, seconds) {
                return thisObj.@GSimpleStateTableView::changeDateTimeProperties(*)(properties, objects, years, months, days, hours, minutes, seconds);
            },
            changeDateProperties: function (properties, objects, years, months, days) {
                return thisObj.@GSimpleStateTableView::changeDateProperties(*)(properties, objects, years, months, days);
            },
            isCurrent: function (object) {
                return thisObj.@GSimpleStateTableView::isCurrentObjectKey(*)(object);
            },
            isPropertyReadOnly: function (property, object) {
                return thisObj.@GSimpleStateTableView::isReadOnly(Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;)(property, object);
            },
            changeObject: function (object, rendered, elementClicked) {
                if(rendered === undefined)
                    rendered = false;
                if(elementClicked === undefined)
                    elementClicked = null;
                return thisObj.@GSimpleStateTableView::changeSimpleGroupObject(*)(object, rendered, elementClicked);
            },
            setDateIntervalViewFilter: function (property, pageSize, startYear, startMonth, startDay, endYear, endMonth, endDay, isDateTimeFilter) {
                thisObj.@GSimpleStateTableView::setDateIntervalViewFilter(*)(property, pageSize, startYear, startMonth, startDay, endYear, endMonth, endDay, isDateTimeFilter);
            },
            setBooleanViewFilter: function (property, pageSize) {
                thisObj.@GSimpleStateTableView::setBooleanViewFilter(*)(property, pageSize);
            },
            getCurrentDay: function (propertyName) {
                return thisObj.@GSimpleStateTableView::getPropertyJsValue(*)(propertyName);
            },
            getGroupObjectBackgroundColor: function(object) {
                var color = object.color;
                if (color)
                    return color.toString();
                color = thisObj.@GStateTableView::getRowBackgroundColor(*)(thisObj.@GSimpleStateTableView::getKey(*)(object));
                if (color)
                    return color.toString();
                return null;
            },
            getDisplayBackgroundColor: function (color, isCurrentKey) {
                return @GSimpleStateTableView::getThemedBackgroundColor(*)(color, isCurrentKey);
            },
            getDisplayForegroundColor: function (color) {
                return @lsfusion.gwt.client.base.view.ColorUtils::getThemedColor(Ljava/lang/String;)(color);
            },
            getGroupObjectForegroundColor: function(object) {
                var color = thisObj.@GStateTableView::getRowForegroundColor(*)(thisObj.@GSimpleStateTableView::getKey(*)(object));
                if (color)
                    return color.toString();
                return null;
            },
            getValues: function (property, value, successCallback, failureCallback) {
                return thisObj.@GSimpleStateTableView::getAsyncValues(*)(property, value, successCallback, failureCallback);
            },
            getKey: function (object) {
                return thisObj.@GSimpleStateTableView::getKey(*)(object);
            },
            getDiff: function (newList, supportReordering) {
                return thisObj.@GSimpleStateTableView::getDiff(*)(newList, supportReordering);
            },
            getColorThemeName: function () {
                return @lsfusion.gwt.client.view.MainFrame::colorTheme.@java.lang.Enum::name()();
            }
        };
    }-*/;
}
