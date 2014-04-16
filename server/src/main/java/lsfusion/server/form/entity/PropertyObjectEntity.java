package lsfusion.server.form.entity;

import lsfusion.base.BaseUtils;
import lsfusion.base.SFunctionSet;
import lsfusion.base.TwinImmutableObject;
import lsfusion.base.col.MapFact;
import lsfusion.base.col.interfaces.immutable.ImMap;
import lsfusion.base.col.interfaces.immutable.ImOrderSet;
import lsfusion.base.col.interfaces.mutable.MExclMap;
import lsfusion.server.logics.property.ActionProperty;
import lsfusion.server.logics.property.CalcProperty;
import lsfusion.server.logics.property.Property;
import lsfusion.server.logics.property.PropertyInterface;
import lsfusion.server.serialization.ServerCustomSerializable;
import lsfusion.server.serialization.ServerSerializationPool;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public abstract class PropertyObjectEntity<P extends PropertyInterface, T extends Property<P>> extends TwinImmutableObject implements ServerCustomSerializable {

    public T property;
    public ImMap<P, PropertyObjectInterfaceEntity> mapping;

    protected PropertyObjectEntity() {
        //нужен для десериализации
        creationScript = null;
        creationPath = null;
    }

    public String toString() {
        return property.toString();
    }

    public boolean calcTwins(TwinImmutableObject o) {
        return property.equals(((PropertyObjectEntity) o).property) && mapping.equals(((PropertyObjectEntity) o).mapping);
    }

    public int immutableHashCode() {
        return property.hashCode() * 31 + mapping.hashCode();
    }

    public PropertyObjectEntity(T property, ImMap<P, PropertyObjectInterfaceEntity> mapping) {
        this(property, mapping, null, null);
    }

    public PropertyObjectEntity(T property, ImMap<P, PropertyObjectInterfaceEntity> mapping, String creationScript, String creationPath) {
        this.property = property;
        this.mapping = mapping;
        this.creationScript = creationScript==null ? null : creationScript.substring(0, Math.min(10000, creationScript.length()));
        this.creationPath = creationPath;
    }

    public GroupObjectEntity getApplyObject(ImOrderSet<GroupObjectEntity> groupList) {
        GroupObjectEntity applyObject = null;
        int maxIndex = -1;
        for (ObjectEntity object : getObjectInstances()) {
            int index = groupList.indexOf(object.groupTo);
            if (index > maxIndex) {
                applyObject = object.groupTo;
                maxIndex = index;
            }
        }
        return applyObject;
    }

    public Collection<ObjectEntity> getObjectInstances() {
        Collection<ObjectEntity> result = new ArrayList<ObjectEntity>();
        for(PropertyObjectInterfaceEntity object : mapping.valueIt())
            if(object instanceof ObjectEntity)
                result.add((ObjectEntity) object);
        return result;
    }
    
    public ImMap<P, ObjectEntity> getMapObjectInstances() {
        return BaseUtils.immutableCast(mapping.filterFnValues(new SFunctionSet<PropertyObjectInterfaceEntity>() {
            public boolean contains(PropertyObjectInterfaceEntity element) {
                return element instanceof ObjectEntity;
            }
        }));
    }

    public void fillObjects(Set<ObjectEntity> objects) {
        objects.addAll(getObjectInstances());
    }

    public void customSerialize(ServerSerializationPool pool, DataOutputStream outStream, String serializationType) throws IOException {
        pool.serializeObject(outStream, property);

        outStream.writeInt(mapping.size());
        for (int i=0,size=mapping.size();i<size;i++) {
            pool.serializeObject(outStream, mapping.getKey(i));
            pool.serializeObject(outStream, mapping.getValue(i));
        }
    }

    public void customDeserialize(ServerSerializationPool pool, DataInputStream inStream) throws IOException {
        String propertySID = inStream.readUTF();

        property = (T) pool.context.BL.getProperty(propertySID);

        int size = inStream.readInt();
        MExclMap<P,PropertyObjectInterfaceEntity> mMapping = MapFact.mExclMap(size);
        for (int i = 0; i < size; ++i) {
            int interId = inStream.readInt();
            P inter = property.getInterfaceById(interId);
            PropertyObjectInterfaceEntity value = pool.deserializeObject(inStream);

            mMapping.exclAdd(inter, value);
        }
        mapping = mMapping.immutable();
    }

    protected final String creationScript;
    protected final String creationPath;

    public String getCreationScript() {
        return creationScript;
    }

    public String getCreationPath() {
        return creationPath;
    }

    public static <I extends PropertyInterface, T extends Property<I>> PropertyObjectEntity<I, ?> create(T property, ImMap<I, ? extends PropertyObjectInterfaceEntity> map, String creationScript, String creationPath) {
        if(property instanceof CalcProperty)
            return new CalcPropertyObjectEntity<I>((CalcProperty<I>)property, map, creationScript, creationPath);
        else
            return new ActionPropertyObjectEntity<I>((ActionProperty<I>) property, map, creationScript, creationPath);
    }
}
