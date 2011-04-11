package platform.server.form.view;

import platform.interop.form.layout.SimplexConstraints;
import platform.server.serialization.ServerSerializationPool;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class GridView extends ComponentView {

    public boolean showFind = false;
    public boolean showFilter = true;
    public boolean showGroupChange = true;
    public boolean showCountQuantity = true;
    public boolean showCalculateSum = true;
    public boolean showGroup = true;
    public boolean showPrintGroupButton = true;
    public boolean showPrintGroupXlsButton = true;

    public byte minRowCount = 0;
    public boolean tabVertical = false;
    public boolean autoHide = false;

    public GroupObjectView groupObject;

    public GridView() {
        
    }
    public GridView(int ID, GroupObjectView groupObject) {
        super(ID);

        this.groupObject = groupObject;
    }

    @Override
    public SimplexConstraints<ComponentView> getDefaultConstraints() {
        return SimplexConstraints.getGridDefaultConstraints(super.getDefaultConstraints());
    }

    @Override
    public void customSerialize(ServerSerializationPool pool, DataOutputStream outStream, String serializationType) throws IOException {
        super.customSerialize(pool, outStream, serializationType);
        outStream.writeBoolean(showFind);
        outStream.writeBoolean(showFilter);
        outStream.writeBoolean(showGroupChange);
        outStream.writeBoolean(showCountQuantity);
        outStream.writeBoolean(showCalculateSum);
        outStream.writeBoolean(showGroup);
        outStream.writeBoolean(showPrintGroupButton);
        outStream.writeBoolean(showPrintGroupXlsButton);

        outStream.writeByte(minRowCount);
        outStream.writeBoolean(tabVertical);
        outStream.writeBoolean(autoHide);

        pool.serializeObject(outStream, groupObject);
    }

    @Override
    public void customDeserialize(ServerSerializationPool pool, DataInputStream inStream) throws IOException {
        super.customDeserialize(pool, inStream);

        showFind = inStream.readBoolean();
        showFilter = inStream.readBoolean();
        showGroupChange = inStream.readBoolean();
        showCountQuantity = inStream.readBoolean();
        showCalculateSum = inStream.readBoolean();
        showGroup = inStream.readBoolean();
        showPrintGroupButton = inStream.readBoolean();
        showPrintGroupXlsButton = inStream.readBoolean();

        minRowCount = inStream.readByte();
        tabVertical = inStream.readBoolean();
        autoHide = inStream.readBoolean();

        groupObject = pool.deserializeObject(inStream);
    }

    public void hideToolbarItems() {
        showFind = false;
        showFilter = false;
        showGroupChange = false;
        showCountQuantity = false;
        showCalculateSum = false;
        showGroup = false;
        showPrintGroupButton = false;
        showPrintGroupXlsButton = false;
    }
}
