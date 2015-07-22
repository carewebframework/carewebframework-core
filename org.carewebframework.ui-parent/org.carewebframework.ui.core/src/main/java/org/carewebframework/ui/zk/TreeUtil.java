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

import java.util.Iterator;

import org.carewebframework.common.MiscUtil;
import org.carewebframework.ui.zk.ZKUtil.MatchMode;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Tree;
import org.zkoss.zul.Treechildren;
import org.zkoss.zul.Treeitem;

/**
 * Useful ZK tree functions.
 */
public class TreeUtil {
    
    /**
     * Interface for tree item search.
     */
    public interface ITreeitemSearch {
        
        /**
         * Returns true if item matches the search text.
         * 
         * @param item Tree item to inspect.
         * @param text Search text.
         * @return True if considered a match.
         */
        boolean isMatch(Treeitem item, String text);
    }
    
    /**
     * Default logic for tree item search.
     */
    private static final ITreeitemSearch defaultTreeitemSearch = new ITreeitemSearch() {
        
        @Override
        public boolean isMatch(Treeitem item, String text) {
            String label = item.getLabel();
            return label != null && label.toLowerCase().contains(text.toLowerCase());
        }
        
    };
    
    /**
     * Returns the tree item associated with the specified \-delimited path.
     * 
     * @param tree Tree to search.
     * @param path \-delimited path to search. Search is not case sensitive.
     * @param create If true, tree nodes are created if they do not already exist.
     * @param clazz Class of Treeitem to create.
     * @return The tree item corresponding to the specified path, or null if not found.
     */
    public static Treeitem findNode(Tree tree, String path, boolean create, Class<? extends Treeitem> clazz) {
        return findNode(tree, path, create, clazz, MatchMode.CASE_INSENSITIVE);
    }
    
    /**
     * Returns the tree item associated with the specified \-delimited path.
     * 
     * @param tree Tree to search.
     * @param path \-delimited path to search.
     * @param create If true, tree nodes are created if they do not already exist.
     * @param clazz Class of Treeitem to create.
     * @param matchMode The match mode.
     * @return The tree item corresponding to the specified path, or null if not found.
     */
    public static Treeitem findNode(Tree tree, String path, boolean create, Class<? extends Treeitem> clazz,
                                    MatchMode matchMode) {
        Treechildren tc = tree.getTreechildren();
        
        if (tc == null) {
            if (create) {
                tree.appendChild(tc = new Treechildren());
            } else {
                return null;
            }
        }
        
        return ZKUtil.findNode(tc, Treechildren.class, clazz, path, create, matchMode);
    }
    
    /**
     * Returns the tree item associated with the specified \-delimited path.
     * 
     * @param tree Tree to search. Search is not case sensitive.
     * @param path \-delimited path to search.
     * @param create If true, tree nodes are created if they do not already exist.
     * @return The tree item corresponding to the specified path, or null if not found.
     */
    public static Treeitem findNode(Tree tree, String path, boolean create) {
        return findNode(tree, path, create, Treeitem.class);
    }
    
    /**
     * Returns the tree item associated with the specified \-delimited path.
     * 
     * @param tree Tree to search.
     * @param path \-delimited path to search.
     * @param create If true, tree nodes are created if they do not already exist.
     * @param matchMode The match mode.
     * @return The tree item corresponding to the specified path, or null if not found.
     */
    public static Treeitem findNode(Tree tree, String path, boolean create, MatchMode matchMode) {
        return findNode(tree, path, create, Treeitem.class, matchMode);
    }
    
    /**
     * Finds the node (tree item) with the specified label under the specified parent. If no node
     * matches the label, one will be created.
     * 
     * @param parent Tree children object whose children are to be searched.
     * @param label Label being sought (case-insensitive).
     * @param create If true and the matching tree item is not found, it will be created.
     * @param clazz Class of tree item to be created.
     * @param caseSensitive If true, match by exact case.
     * @return The node whose label matches that specified.
     */
    public static Treeitem findNodeByLabel(Treechildren parent, String label, boolean create,
                                           Class<? extends Treeitem> clazz, boolean caseSensitive) {
        Treeitem item = null;
        
        for (Component comp : parent.getChildren()) {
            if (comp instanceof Treeitem) {
                item = (Treeitem) comp;
                
                if (caseSensitive ? item.getLabel().equals(label) : item.getLabel().equalsIgnoreCase(label)) {
                    return item;
                }
            }
        }
        
        if (create) {
            try {
                item = clazz.newInstance();
                item.setLabel(label);
                item.setTooltiptext(label);
                parent.appendChild(item);
                return item;
            } catch (Exception e) {
                throw MiscUtil.toUnchecked(e);
            }
        }
        
        return null;
    }
    
    /**
     * Search the entire tree for a tree item matching the specified label.
     * 
     * @param tree Tree containing the item of interest.
     * @param label Label to match.
     * @param caseSensitive If true, match is case-sensitive.
     * @return The matching tree item, or null if not found.
     */
    public static Treeitem findNodeByLabel(Tree tree, String label, boolean caseSensitive) {
        for (Treeitem item : tree.getItems()) {
            if (caseSensitive ? label.equals(item.getLabel()) : label.equalsIgnoreCase(item.getLabel())) {
                return item;
            }
        }
        
        return null;
    }
    
    /**
     * Returns the path of the specified tree node. This consists of the indexes or labels of this
     * and all parent nodes separated by a "\" character.
     * 
     * @param item The node (tree item) whose path is to be returned. If this value is null, a zero
     *            length string is returned.
     * @param useLabels If true, use the labels as identifiers; otherwise, use indexes.
     * @return The path of the node as described.
     */
    public static String getPath(Treeitem item, boolean useLabels) {
        StringBuilder sb = new StringBuilder();
        boolean needsDelim = false;
        
        while (item != null) {
            if (needsDelim) {
                sb.insert(0, '\\');
            } else {
                needsDelim = true;
            }
            
            sb.insert(0, useLabels ? item.getLabel() : item.getIndex());
            item = item.getParentItem();
        }
        
        return sb.toString();
    }
    
    /**
     * Sorts all nodes in a tree alphabetically.
     * 
     * @param tree Tree to be sorted.
     */
    public static void sort(Tree tree) {
        sort(tree.getTreechildren(), true);
    }
    
    /**
     * Alphabetically sorts children under the specified parent.
     * 
     * @param parent Parent node (Treechildren) whose child nodes (Treeitem) are to be sorted.
     * @param recurse If true, sorting is recursed through all children.
     */
    public static void sort(Treechildren parent, boolean recurse) {
        if (parent == null || parent.getChildren().size() < 2) {
            return;
        }
        
        int i = 1;
        int size = parent.getChildren().size();
        
        while (i < size) {
            Treeitem item1 = (Treeitem) parent.getChildren().get(i - 1);
            Treeitem item2 = (Treeitem) parent.getChildren().get(i);
            
            if (compare(item1, item2) > 0) {
                item2.detach();
                parent.insertBefore(item2, item1);
                i = i == 1 ? 2 : i - 1;
            } else {
                i++;
            }
        }
        
        if (recurse) {
            for (Object item : parent.getChildren()) {
                sort(((Treeitem) item).getTreechildren(), recurse);
            }
        }
    }
    
    /**
     * Case insensitive comparison of labels of two tree items.
     * 
     * @param item1 First tree item.
     * @param item2 Second tree item.
     * @return Result of the comparison.
     */
    private static int compare(Treeitem item1, Treeitem item2) {
        String label1 = item1.getLabel();
        String label2 = item2.getLabel();
        return label1 == label2 ? 0 : label1 == null ? -1 : label2 == null ? -1 : label1.compareToIgnoreCase(label2);
    }
    
    /**
     * Expand / collapse a tree to the specified depth.
     * 
     * @param tree Tree whose tree items are to be expanded / collapsed.
     * @param depth Expand tree items to this depth. Tree items below this depth are collapsed.
     */
    public static void expand(Tree tree, int depth) {
        expand(tree.getTreechildren(), depth);
    }
    
    /**
     * Expand / collapse tree children to specified depth.
     * 
     * @param parent Tree children whose tree items are to be expanded / collapsed.
     * @param depth Expand tree items to this depth. Tree items below this depth are collapsed.
     */
    public static void expand(Treechildren parent, int depth) {
        if (parent == null || depth <= 0) {
            return;
        }
        
        depth--;
        
        for (Object object : parent.getChildren()) {
            Treeitem item = (Treeitem) object;
            item.getTree().renderItem(item);
            Treechildren tc = item.getTreechildren();
            
            if (tc != null) {
                item.setOpen(depth > 0);
                expand(tc, depth);
            }
        }
    }
    
    /**
     * Search the tree for a tree item whose label contains the specified text.
     * 
     * @param tree Tree to search.
     * @param text Text to find.
     * @return The first matching tree item, or null if none found.
     */
    public static Treeitem search(Tree tree, String text) {
        return search(tree, null, text, defaultTreeitemSearch);
    }
    
    /**
     * Search the tree for a tree item whose label contains the specified text.
     * 
     * @param tree Tree to search.
     * @param text Text to find.
     * @param search Search logic.
     * @return The first matching tree item, or null if none found.
     */
    public static Treeitem search(Tree tree, String text, ITreeitemSearch search) {
        return search(tree, null, text, search);
    }
    
    /**
     * Search the tree for a tree item whose label contains the specified text.
     * 
     * @param start Tree item to start search.
     * @param text Text to find.
     * @return The first matching tree item after the starting item, or null if none found.
     */
    public static Treeitem search(Treeitem start, String text) {
        return search(start.getTree(), start, text, defaultTreeitemSearch);
    }
    
    /**
     * Search the tree for a tree item whose label contains the specified text.
     * 
     * @param start Tree item to start search.
     * @param text Text to find.
     * @param search Search logic.
     * @return The first matching tree item after the starting item, or null if none found.
     */
    public static Treeitem search(Treeitem start, String text, ITreeitemSearch search) {
        return search(start.getTree(), start, text, search);
    }
    
    /**
     * Search the tree for a tree item whose label contains the specified text.
     * 
     * @param tree Tree to search.
     * @param last Last item found.
     * @param text Text to find.
     * @param search Search logic.
     * @return The first matching tree item after the last item, or null if none found.
     */
    private static Treeitem search(Tree tree, Treeitem last, String text, ITreeitemSearch search) {
        Iterator<Treeitem> it = last == null ? new TreeIterator(tree) : new TreeIterator(last);
        
        while (it.hasNext()) {
            Treeitem item = it.next();
            
            if (!item.isLoaded()) {
                tree.renderItem(item);
            }
            
            if (search.isMatch(item, text)) {
                return item;
            }
        }
        return null;
    }
    
    /**
     * Makes certain a tree item is visible.
     * 
     * @param item The tree item.
     */
    public static void makeVisible(Treeitem item) {
        if (item == null) {
            return;
        }
        
        makeVisible(item.getParentItem());
        item.setOpen(true);
        Clients.scrollIntoView(item);
    }
    
    /**
     * Recursively adjusts the visibility of tree expand/collapse icons based on the visibility of
     * the child nodes.
     * 
     * @param tree Tree component
     */
    public static void adjustVisibility(Tree tree) {
        adjustVisibility(tree.getTreechildren());
    }
    
    /**
     * Recursively adjusts the visibility of tree expand/collapse icons based on the visibility of
     * the child nodes.
     * 
     * @param parent Treechildren component
     */
    public static void adjustVisibility(Treechildren parent) {
        if (parent != null) {
            boolean visibleChildren = parent.getVisibleItemCount() > 0;
            
            if (visibleChildren) {
                for (Component item : parent.getChildren()) {
                    adjustVisibility(((Treeitem) item).getTreechildren());
                }
            }
            
            if (parent.getParent() instanceof Treeitem) {
                adjustVisibility(parent, visibleChildren);
                adjustVisibility((Treeitem) parent.getParent(), visibleChildren);
                adjustVisibility(parent.getLinkedTreerow(), visibleChildren);
            }
        }
    }
    
    /**
     * Shows or hides the expand/collapse icon.
     * 
     * @param comp Component of the tree hierarchy.
     * @param visible If true, show the icon.
     */
    private static void adjustVisibility(HtmlBasedComponent comp, boolean visible) {
        if (comp != null) {
            ZKUtil.updateSclass(comp, "cwf-treerow-hidebtn", visible);
        }
    }
    
    /**
     * Enforces static class.
     */
    private TreeUtil() {
    };
    
}
