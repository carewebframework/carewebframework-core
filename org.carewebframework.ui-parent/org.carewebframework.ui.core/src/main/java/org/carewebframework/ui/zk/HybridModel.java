/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.zk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.carewebframework.ui.zk.HybridModel.GroupHeader;

import org.zkoss.zul.AbstractListModel;
import org.zkoss.zul.GroupsModel;
import org.zkoss.zul.SimpleGroupsModel;
import org.zkoss.zul.event.GroupsDataEvent;
import org.zkoss.zul.event.GroupsDataListener;
import org.zkoss.zul.event.ListDataEvent;
import org.zkoss.zul.ext.GroupsSortableModel;

/**
 * This is a hybrid list model/group model. If a grouper implementation is supplied, can act as a
 * group model. Otherwise, is a simple list model.
 * 
 * @param <T> The element class.
 * @param <G> The group class.
 */
@SuppressWarnings("rawtypes")
public class HybridModel<T, G> extends AbstractListModel<T> implements Collection<T>, GroupsModel<T, GroupHeader, Object>, GroupsSortableModel<T>, Iterable<T> {
    
    private static final long serialVersionUID = 1L;
    
    public interface IGrouper<T, G> {
        
        G getGroup(T element);
        
        String getGroupName(G group);
        
        int compareElement(T element1, T element2);
        
        int compareGroup(G group1, G group2);
        
    }
    
    /**
     * Simple implementation of a sorted list.
     * 
     * @param <E> The list element class.
     */
    private static class SortedList<E> extends ArrayList<E> {
        
        private static final long serialVersionUID = 1L;
        
        private int state;
        
        private Comparator<E> comparator;
        
        SortedList(Comparator<E> comparator) {
            setComparator(comparator);
        }
        
        protected void ensureSorted() {
            if (state != modCount) {
                state = modCount;
                Collections.sort(this, comparator);
            }
        }
        
        protected void setComparator(Comparator<E> comparator) {
            this.comparator = comparator;
            state = -1;
        }
    }
    
    public static class GroupHeader<T, G> extends SortedList<T> implements Comparable<GroupHeader> {
        
        private static final long serialVersionUID = 1L;
        
        protected final G group;
        
        private boolean opened = true;
        
        private final IGrouper<T, G> grouper;
        
        GroupHeader(G group, Comparator<T> elementComparator, IGrouper<T, G> grouper) {
            super(elementComparator);
            this.group = group;
            this.grouper = grouper;
        }
        
        public G getGroup() {
            return group;
        }
        
        @Override
        public String toString() {
            return grouper.getGroupName(group);
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public int compareTo(GroupHeader arg) {
            return grouper.compareGroup(group, (G) arg.group);
        }
        
        public boolean isOpened() {
            return opened;
        }
        
        public boolean setOpened(boolean opened) {
            boolean result = opened != this.opened;
            this.opened = opened;
            return result;
        }
    }
    
    private final List<T> data = new ArrayList<>();
    
    private IGrouper<T, G> grouper;
    
    private final Comparator<T> elementComparator = new Comparator<T>() {
        
        @Override
        public int compare(T t1, T t2) {
            return grouper == null ? 0 : grouper.compareElement(t1, t2);
        }
        
    };
    
    private final SortedList<GroupHeader<T, G>> groupHeaders = new SortedList<>(null);
    
    @SuppressWarnings("unchecked")
    private final SimpleGroupsModel<T, GroupHeader, ?, ?> groupsModel = new SimpleGroupsModel(
                                                                                              groupHeaders) {
        
        @Override
        public void group(Comparator cmpr, boolean ascending, int colIndex) {
            if (colIndex == -1) {
                fireEvent(GroupsDataEvent.STRUCTURE_CHANGED, -1, -1, -1);
            }
        }
        
        private static final long serialVersionUID = 1L;
        
    };
    
    public HybridModel() {
        this(null, null);
    }
    
    public HybridModel(IGrouper<T, G> grouper) {
        this(grouper, null);
    }
    
    public HybridModel(Collection<T> data) {
        this(null, data);
    }
    
    public HybridModel(IGrouper<T, G> grouper, Collection<T> data) {
        if (data != null) {
            this.data.addAll(data);
        }
        
        setGrouper(grouper);
    }
    
    public HybridModel(HybridModel<T, G> model) {
        this(model.grouper);
        setMultiple(model.isMultiple());
    }
    
    public boolean isGrouped() {
        return grouper != null;
    }
    
    private boolean addElement(T element) {
        boolean result = data.add(element);
        
        if (result && grouper != null) {
            addToGroup(element);
        }
        
        return result;
    }
    
    private boolean removeItem(T element) {
        boolean result = data.remove(element);
        
        if (grouper != null) {
            removeFromGroup(element);
        }
        
        return result;
    }
    
    private void addToGroup(T element) {
        findGroup(grouper.getGroup(element), true).add(element);
    }
    
    private void removeFromGroup(T element) {
        GroupHeader grp = findGroup(grouper.getGroup(element), false);
        
        if (grp != null) {
            grp.remove(element);
            
            if (grp.isEmpty()) {
                groupHeaders.remove(grp);
            }
        }
    }
    
    private GroupHeader<T, G> findGroup(G group, boolean autoCreate) {
        for (GroupHeader<T, G> grp : groupHeaders) {
            if (grp.group == group || grp.group.equals(group)) {
                return grp;
            }
        }
        
        if (!autoCreate) {
            return null;
        }
        
        GroupHeader<T, G> grp = new GroupHeader<T, G>(group, elementComparator, grouper);
        groupHeaders.add(grp);
        return grp;
    }
    
    public IGrouper<T, G> getGrouper() {
        return grouper;
    }
    
    public void setGrouper(IGrouper<T, G> grouper) {
        if (grouper != this.grouper) {
            this.grouper = grouper;
            groupHeaders.clear();
            
            if (grouper != null) {
                for (T element : data) {
                    addToGroup(element);
                }
            }
            
            groupsModel.group(null, true, -1);
        }
    }
    
    /******** List ********/
    
    @Override
    public void clear() {
        int i = data.size() - 1;
        
        if (i >= 0) {
            clearSelection();
            data.clear();
            
            if (groupHeaders != null) {
                groupHeaders.clear();
            }
            
            fireEvent(ListDataEvent.INTERVAL_REMOVED, 0, i);
        }
    }
    
    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }
    
    @Override
    public int size() {
        return data.size();
    }
    
    @Override
    public boolean contains(Object o) {
        return data.contains(o);
    }
    
    @Override
    public Object[] toArray() {
        return data.toArray();
    }
    
    @Override
    public <E> E[] toArray(E[] a) {
        return data.toArray(a);
    }
    
    @Override
    public boolean add(T element) {
        return addAll(Collections.singleton(element));
    }
    
    @Override
    public boolean addAll(Collection<? extends T> elements) {
        int i = data.size();
        int count = 0;
        
        for (T element : elements) {
            if (addElement(element)) {
                count++;
            }
        }
        
        if (count > 0) {
            fireEvent(ListDataEvent.INTERVAL_ADDED, i, i + count);
        }
        
        return count > 0;
    }
    
    @Override
    public boolean containsAll(Collection<?> c) {
        return data.containsAll(c);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public boolean retainAll(Collection<?> elements) {
        boolean result = false;
        
        for (Object element : data) {
            if (!elements.contains(element)) {
                result |= removeItem((T) element);
            }
        }
        
        if (result) {
            fireEvent(ListDataEvent.CONTENTS_CHANGED, 0, 0);
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object element) {
        int i = data.indexOf(element);
        
        if (i >= 0) {
            removeItem((T) element);
            fireEvent(ListDataEvent.INTERVAL_REMOVED, i, i);
        }
        
        return i >= 0;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public boolean removeAll(Collection<?> elements) {
        int count = 0;
        
        for (Object element : elements) {
            if (removeItem((T) element)) {
                count++;
            }
        }
        
        if (count > 0) {
            fireEvent(ListDataEvent.CONTENTS_CHANGED, 0, 0);
        }
        
        return count > 0;
    }
    
    /******** ListModel ********/
    
    @Override
    public T getElementAt(int index) {
        return data.get(index);
    }
    
    @Override
    public int getSize() {
        return data.size();
    }
    
    /******** GroupModel ********/
    
    @Override
    public GroupHeader getGroup(int groupIndex) {
        groupHeaders.ensureSorted();
        return (GroupHeader) groupsModel.getGroup(groupIndex);
    }
    
    @Override
    public int getGroupCount() {
        return groupsModel.getGroupCount();
    }
    
    @Override
    public T getChild(int groupIndex, int index) {
        getGroup(groupIndex).ensureSorted();
        return groupsModel.getChild(groupIndex, index);
    }
    
    @Override
    public int getChildCount(int groupIndex) {
        return groupsModel.getChildCount(groupIndex);
    }
    
    @Override
    public Object getGroupfoot(int groupIndex) {
        return groupsModel.getGroupfoot(groupIndex);
    }
    
    @Override
    public boolean hasGroupfoot(int groupIndex) {
        return groupsModel.hasGroupfoot(groupIndex);
    }
    
    @Override
    public void addGroupsDataListener(GroupsDataListener l) {
        groupsModel.addGroupsDataListener(l);
    }
    
    @Override
    public void removeGroupsDataListener(GroupsDataListener l) {
        groupsModel.removeGroupsDataListener(l);
    }
    
    @Override
    public boolean isGroupOpened(int groupIndex) {
        return getGroup(groupIndex).isOpened();
    }
    
    @Override
    public boolean addOpenGroup(int groupIndex) {
        return getGroup(groupIndex).setOpened(true);
    }
    
    @Override
    public boolean removeOpenGroup(int groupIndex) {
        return getGroup(groupIndex).setOpened(false);
    }
    
    /******** Iterable<T> ********/
    
    @Override
    public Iterator<T> iterator() {
        return data.iterator();
    }
    
    /******** GroupsSortableModel<T> ********/
    
    @Override
    public void sort(Comparator<T> cmpr, boolean ascending, int colIndex) {
        if (isGrouped()) {
            groupsModel.sort(cmpr, ascending, colIndex);
        }
        
        Collections.sort(data, cmpr);
        fireEvent(ListDataEvent.STRUCTURE_CHANGED, -1, -1);
    }
    
    @Override
    public void group(Comparator<T> cmpr, boolean ascending, int colIndex) {
        // NOP
    }
    
}
