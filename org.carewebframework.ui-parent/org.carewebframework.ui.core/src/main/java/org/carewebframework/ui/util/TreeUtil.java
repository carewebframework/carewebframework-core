/*
 * #%L
 * carewebframework
 * %%
 * Copyright (C) 2008 - 2016 Regenstrief Institute, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This Source Code Form is also subject to the terms of the Health-Related
 * Additional Disclaimer of Warranty and Limitation of Liability available at
 *
 *      http://www.carewebframework.org/licensing/disclaimer.
 *
 * #L%
 */
package org.carewebframework.ui.util;

import org.carewebframework.common.MiscUtil;
import org.carewebframework.ui.util.CWFUtil.MatchMode;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Treenode;
import org.carewebframework.web.component.Treeview;

/**
 * Useful tree functions.
 */
public class TreeUtil {

    /**
     * Interface for tree item search.
     */
    public interface ITreenodeSearch {

        /**
         * Returns true if item matches the search text.
         *
         * @param item Tree item to inspect.
         * @param text Search text.
         * @return True if considered a match.
         */
        boolean isMatch(Treenode item, String text);
    }

    /**
     * Default logic for tree item search.
     */
    private static final ITreenodeSearch defaultTreenodeSearch = new ITreenodeSearch() {

        @Override
        public boolean isMatch(Treenode item, String text) {
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
     * @param clazz Class of Treenode to create.
     * @return The tree item corresponding to the specified path, or null if not found.
     */
    public static Treenode findNode(Treeview tree, String path, boolean create, Class<? extends Treenode> clazz) {
        return findNode(tree, path, create, clazz, MatchMode.CASE_INSENSITIVE);
    }

    /**
     * Returns the tree item associated with the specified \-delimited path.
     *
     * @param tree Tree to search.
     * @param path \-delimited path to search.
     * @param create If true, tree nodes are created if they do not already exist.
     * @param clazz Class of Treenode to create.
     * @param matchMode The match mode.
     * @return The tree item corresponding to the specified path, or null if not found.
     */
    public static Treenode findNode(Treeview tree, String path, boolean create, Class<? extends Treenode> clazz,
                                    MatchMode matchMode) {
        if (tree.getChildCount() == 0 && !create) {
            return null;
        }

        return CWFUtil.findNode(tree, clazz, path, create, matchMode);
    }

    /**
     * Returns the tree item associated with the specified \-delimited path.
     *
     * @param tree Tree to search. Search is not case sensitive.
     * @param path \-delimited path to search.
     * @param create If true, tree nodes are created if they do not already exist.
     * @return The tree item corresponding to the specified path, or null if not found.
     */
    public static Treenode findNode(Treeview tree, String path, boolean create) {
        return findNode(tree, path, create, Treenode.class);
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
    public static Treenode findNode(Treeview tree, String path, boolean create, MatchMode matchMode) {
        return findNode(tree, path, create, Treenode.class, matchMode);
    }

    /**
     * Finds the node (tree item) with the specified label under the specified parent. If no node
     * matches the label, one will be created.
     *
     * @param parent Tree node whose children are to be searched.
     * @param label Label being sought (case-insensitive).
     * @param create If true and the matching tree item is not found, it will be created.
     * @param clazz Class of tree item to be created.
     * @param caseSensitive If true, match by exact case.
     * @return The node whose label matches that specified.
     */
    public static Treenode findNodeByLabel(Treenode parent, String label, boolean create, Class<? extends Treenode> clazz,
                                           boolean caseSensitive) {
        Treenode item = null;

        for (BaseComponent comp : parent.getChildren()) {
            if (comp instanceof Treenode) {
                item = (Treenode) comp;

                if (caseSensitive ? item.getLabel().equals(label) : item.getLabel().equalsIgnoreCase(label)) {
                    return item;
                }
            }
        }

        if (create) {
            try {
                item = clazz.newInstance();
                item.setLabel(label);
                item.setHint(label);
                parent.addChild(item);
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
    public static Treenode findNodeByLabel(Treeview tree, String label, boolean caseSensitive) {
        for (Treenode item : tree.getChildren(Treenode.class)) {
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
    public static String getPath(Treenode item, boolean useLabels) {
        StringBuilder sb = new StringBuilder();
        boolean needsDelim = false;

        while (item != null) {
            if (needsDelim) {
                sb.insert(0, '\\');
            } else {
                needsDelim = true;
            }

            sb.insert(0, useLabels ? item.getLabel() : item.getIndex());
            item = (Treenode) item.getParent();
        }

        return sb.toString();
    }

    /**
     * Sorts all nodes in a tree alphabetically.
     *
     * @param tree Tree to be sorted.
     */
    public static void sort(Treeview tree) {
        sort(tree, true);
    }

    /**
     * Alphabetically sorts children under the specified parent.
     *
     * @param parent Parent whose child nodes (Treenode) are to be sorted.
     * @param recurse If true, sorting is recursed through all children.
     */
    public static void sort(BaseComponent parent, boolean recurse) {
        if (parent == null || parent.getChildren().size() < 2) {
            return;
        }

        int i = 1;
        int size = parent.getChildren().size();

        while (i < size) {
            Treenode item1 = (Treenode) parent.getChildren().get(i - 1);
            Treenode item2 = (Treenode) parent.getChildren().get(i);

            if (compare(item1, item2) > 0) {
                parent.swapChildren(i - 1, i);
                i = i == 1 ? 2 : i - 1;
            } else {
                i++;
            }
        }

        if (recurse) {
            for (BaseComponent child : parent.getChildren()) {
                sort(child, recurse);
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
    private static int compare(Treenode item1, Treenode item2) {
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
    public static void expand(Treeview tree, int depth) {
        expandChildren(tree, depth);
    }

    /**
     * Expand / collapse tree children to specified depth.
     *
     * @param parent Tree children whose tree items are to be expanded / collapsed.
     * @param depth Expand tree items to this depth. Tree items below this depth are collapsed.
     */
    private static void expandChildren(BaseComponent parent, int depth) {
        if (parent == null || depth <= 0) {
            return;
        }

        depth--;

        for (Object object : parent.getChildren()) {
            Treenode item = (Treenode) object;
            item.setCollapsed(depth <= 0);
            expandChildren(item, depth);
        }
    }

    /**
     * Search the tree for a tree item whose label contains the specified text.
     *
     * @param tree Tree to search.
     * @param text Text to find.
     * @return The first matching tree item, or null if none found.
     */
    public static Treenode search(Treeview tree, String text) {
        return search(tree, text, defaultTreenodeSearch);
    }

    /**
     * Search the tree for a tree item whose label contains the specified text.
     *
     * @param start Tree item to start search.
     * @param text Text to find.
     * @return The first matching tree item after the starting item, or null if none found.
     */
    public static Treenode search(Treenode start, String text) {
        return search(start.getTreeview(), text, defaultTreenodeSearch);
    }

    /**
     * Search the tree for a tree item whose label contains the specified text.
     *
     * @param start Tree item to start search.
     * @param text Text to find.
     * @param search Search logic.
     * @return The first matching tree item after the starting item, or null if none found.
     */
    public static Treenode search(Treenode start, String text, ITreenodeSearch search) {
        return search(start.getTreeview(), text, search);
    }

    /**
     * Search the tree for a tree node whose label contains the specified text.
     *
     * @param root Node where search should begin.
     * @param text Text to find.
     * @param search Search logic.
     * @return The first matching tree item after the last item, or null if none found.
     */
    private static Treenode search(Iterable<Treenode> root, String text, ITreenodeSearch search) {
        for (Treenode node : root) {
            if (search.isMatch(node, text)) {
                return node;
            }
        }
        return null;
    }

    /**
     * Recursively adjusts the visibility of tree expand/collapse icons based on the visibility of
     * the child nodes.
     *
     * @param nodes Treenode components
     */
    /*TODO:
    public static void adjustVisibility(Iterable<Treenode> nodes) {
        if (nodes != null) {
            boolean visibleChildren = parent.getVisibleItemCount() > 0;

            if (visibleChildren) {
                for (BaseComponent item : parent.getChildren()) {
                    adjustVisibility(((Treenode) item).getTreechildren());
                }
            }

            if (parent.getParent() instanceof Treenode) {
                adjustVisibility(parent, visibleChildren);
                adjustVisibility((Treenode) parent.getParent(), visibleChildren);
                adjustVisibility(parent.getLinkedTreerow(), visibleChildren);
            }
        }
    }
    */

    /**
     * Enforces static class.
     */
    private TreeUtil() {
    };

}
