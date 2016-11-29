package org.carewebframework.ui.manifest;

/**
 * Extends the Comparable interface by adding filter matching.
 *
 * @param <M> The model object.
 */
interface IMatchable<M> extends Comparable<M> {
    
    /**
     * Returns true if the entry matches the filter.
     * 
     * @param filter The filter.
     * @return True if the entry matches the filter.
     */
    boolean matches(String filter);
}
