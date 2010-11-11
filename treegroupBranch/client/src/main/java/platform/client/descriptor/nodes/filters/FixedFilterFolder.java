package platform.client.descriptor.nodes.filters;

import platform.client.descriptor.GroupObjectDescriptor;
import platform.client.descriptor.filter.FilterDescriptor;
import platform.client.descriptor.increment.IncrementDependency;
import platform.client.descriptor.nodes.GroupElementFolder;

import java.util.List;
import java.util.Set;

public class FixedFilterFolder extends GroupElementFolder<FixedFilterFolder> {

    private Set<FilterDescriptor> fixedFilters;

    public FixedFilterFolder(List<GroupObjectDescriptor> groupList, GroupObjectDescriptor group, final Set<FilterDescriptor> fixedFilters) {
        super(group, "Постоянные фильтры");

        this.fixedFilters = fixedFilters;

        for (FilterDescriptor filter : fixedFilters) {
            if (group == null || group.equals(filter.getGroupObject(groupList))) {
                add(filter.createNode(group));
            }
        }

        addCollectionReferenceActions(this, "fixedFilters", FilterDescriptor.derivedNames, FilterDescriptor.derivedClasses);
    }

    public Set<FilterDescriptor> getFixedFilters() {
        return fixedFilters;
    }

    public void addToFixedFilters(FilterDescriptor filter) {
        fixedFilters.add(filter);
        IncrementDependency.update(this, "fixedFilters");
    }

    public void removeFromFixedFilters(FilterDescriptor filter) {
        fixedFilters.remove(filter);
        IncrementDependency.update(this, "fixedFilters");
    }
}
