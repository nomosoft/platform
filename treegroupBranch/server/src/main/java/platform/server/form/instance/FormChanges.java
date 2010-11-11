package platform.server.form.instance;

import platform.base.BaseUtils;
import platform.interop.ClassViewType;
import platform.server.logics.DataObject;
import platform.server.logics.ObjectValue;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

// появляется по сути для отделения клиента, именно он возвращается назад клиенту
public class FormChanges {

    Boolean dataChanged = null;

    public String message = "";

    public Map<GroupObjectInstance, ClassViewType> classViews = new HashMap<GroupObjectInstance, ClassViewType>();
    public Map<GroupObjectInstance, Map<ObjectInstance, ? extends ObjectValue>> objects = new HashMap<GroupObjectInstance, Map<ObjectInstance, ? extends ObjectValue>>();

    // assertion что ObjectInstance из того же GroupObjectInstance
    public Map<GroupObjectInstance, List<Map<ObjectInstance, DataObject>>> gridObjects = new HashMap<GroupObjectInstance, List<Map<ObjectInstance, DataObject>>>();

    // assertion для ключа GroupObjectInstance что в значении ObjectInstance из верхних GroupObjectInstance TreeGroupInstance'а этого ключа,
    // так же может быть ObjectInstance из этого ключа если GroupObject - отображается рекурсивно (тогда надо цеплять к этому GroupObjectValue, иначе к верхнему)
    public Map<GroupObjectInstance, List<Map<ObjectInstance, DataObject>>> treeObjects = new HashMap<GroupObjectInstance, List<Map<ObjectInstance, DataObject>>>();
    public Set<GroupObjectInstance> treeRefresh = new HashSet<GroupObjectInstance>(); // для каких групп объектов collaps'ить пришедшие ключи     

    public Map<PropertyReadInstance, Map<Map<ObjectInstance, DataObject>, Object>> properties = new HashMap<PropertyReadInstance, Map<Map<ObjectInstance, DataObject>, Object>>();

    public Set<PropertyDrawInstance> panelProperties = new HashSet<PropertyDrawInstance>();
    public Set<PropertyDrawInstance> dropProperties = new HashSet<PropertyDrawInstance>();

    void out(FormInstance<?> bv) {
        System.out.println(" ------- GROUPOBJECTS ---------------");
        for (GroupObjectInstance group : bv.groups) {
            List<Map<ObjectInstance, DataObject>> groupGridObjects = gridObjects.get(group);
            if (groupGridObjects != null) {
                System.out.println(group.getID() + " - GRID Changes");
                for (Map<ObjectInstance, DataObject> value : groupGridObjects)
                    System.out.println(value);
            }

            Map<ObjectInstance, ? extends ObjectValue> value = objects.get(group);
            if (value != null)
                System.out.println(group.getID() + " - Object Changes " + value);
        }

        System.out.println(" ------- PROPERTIES ---------------");
        System.out.println(" ------- Group ---------------");
        for (PropertyReadInstance property : properties.keySet()) {
            Map<Map<ObjectInstance, DataObject>, Object> propertyValues = properties.get(property);
            System.out.println(property + " ---- property");
            for (Map<ObjectInstance, DataObject> gov : propertyValues.keySet())
                System.out.println(gov + " - " + propertyValues.get(gov));
        }

        System.out.println(" ------- PANEL ---------------");
        for (PropertyDrawInstance property : panelProperties)
            System.out.println(property);

        System.out.println(" ------- Drop ---------------");
        for (PropertyDrawInstance property : dropProperties)
            System.out.println(property);

        System.out.println(" ------- CLASSVIEWS ---------------");
        for (Map.Entry<GroupObjectInstance, ClassViewType> classView : classViews.entrySet()) {
            System.out.println(classView.getKey() + " - " + classView.getValue());
        }

    }

    public void serialize(DataOutputStream outStream) throws IOException {

        outStream.writeInt(classViews.size());
        for (Map.Entry<GroupObjectInstance, ClassViewType> classView : classViews.entrySet()) {
            outStream.writeInt(classView.getKey().getID());
            outStream.writeInt(classView.getValue().ordinal());
        }

        outStream.writeInt(objects.size());
        for (Map.Entry<GroupObjectInstance, Map<ObjectInstance, ? extends ObjectValue>> objectValue : objects.entrySet()) {
            outStream.writeInt(objectValue.getKey().getID());
            // именно так чтобы гарантировано в том же порядке
            for (ObjectInstance object : objectValue.getKey().objects) {
                BaseUtils.serializeObject(outStream, objectValue.getValue().get(object).getValue());
            }
        }

        serializeKeyObjectsMap(outStream, gridObjects);
        serializeKeyObjectsMap(outStream, treeObjects);

        outStream.writeInt(treeRefresh.size());
        for (GroupObjectInstance group : treeRefresh) {
            outStream.writeInt(group.getID());
        }

        outStream.writeInt(panelProperties.size());
        for (PropertyDrawInstance propertyView : panelProperties) {
            outStream.writeInt(propertyView.getID());
        }
        
        outStream.writeInt(properties.size());
        for (Map.Entry<PropertyReadInstance,Map<Map<ObjectInstance,DataObject>,Object>> gridProperty : properties.entrySet()) {
            PropertyReadInstance propertyReadInstance = gridProperty.getKey();

            // сериализация PropertyReadInterface
            outStream.writeByte(propertyReadInstance.getTypeID());
            outStream.writeInt(propertyReadInstance.getID());

            outStream.writeInt(gridProperty.getValue().size());
            for (Map.Entry<Map<ObjectInstance, DataObject>, Object> gridPropertyValue : gridProperty.getValue().entrySet()) {
                Map<ObjectInstance, DataObject> objectValues = gridPropertyValue.getKey();

                // именно так чтобы гарантировано в том же порядке
                for (ObjectInstance object : propertyReadInstance.getSerializeList(panelProperties)) {
                    BaseUtils.serializeObject(outStream, objectValues.get(object).getValue());
                }

                BaseUtils.serializeObject(outStream, gridPropertyValue.getValue());
            }
        }

        outStream.writeInt(dropProperties.size());
        for (PropertyDrawInstance propertyView : dropProperties) {
            outStream.writeInt(propertyView.getID());
        }

        outStream.writeUTF(message);

        BaseUtils.serializeObject(outStream, dataChanged);
    }

    private void serializeKeyObjectsMap(DataOutputStream outStream, Map<GroupObjectInstance, List<Map<ObjectInstance, DataObject>>> keyObjects) throws IOException {
        outStream.writeInt(keyObjects.size());
        for (Map.Entry<GroupObjectInstance, List<Map<ObjectInstance, DataObject>>> gridObject : keyObjects.entrySet()) {

            outStream.writeInt(gridObject.getKey().getID());

            outStream.writeInt(gridObject.getValue().size());
            for (Map<ObjectInstance, DataObject> groupObjectValue : gridObject.getValue()) {
                // именно так чтобы гарантировано в том же порядке
                for (ObjectInstance object : gridObject.getKey().objects) {
                    BaseUtils.serializeObject(outStream, groupObjectValue.get(object).object);
                }
            }
        }
    }
}
