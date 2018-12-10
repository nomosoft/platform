package lsfusion.gwt.client.form.grid.editor;

import lsfusion.gwt.shared.GwtSharedUtils;
import lsfusion.gwt.shared.view.GPropertyDraw;
import lsfusion.gwt.client.form.grid.EditManager;

public class StringGridCellEditor extends TextBasedGridCellEditor {
    private boolean isVarString;
    private int stringLength; 

    public StringGridCellEditor(EditManager editManager, GPropertyDraw property, boolean isVarString, int stringLength) {
        super(editManager, property);
        this.isVarString = isVarString;
        this.stringLength = stringLength;
    }

    @Override
    protected String tryParseInputText(String inputText, boolean onCommit) {
        return inputText.isEmpty() ? null : inputText;
    }

    @Override
    protected String renderToString(Object value) {
        if (value == null) {
            return "";
        }

        return isVarString ? (String)value : GwtSharedUtils.rtrim(value.toString());
    }

    @Override
    protected boolean isStringValid(String string) {
        return string.length() <= stringLength;
    }
}
