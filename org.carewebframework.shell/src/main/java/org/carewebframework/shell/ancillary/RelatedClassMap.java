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
package org.carewebframework.shell.ancillary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.carewebframework.shell.elements.ElementBase;

/**
 * Defines cardinality relationships between layout elements. This is used to constrain parent-child
 * relationships.
 */
public class RelatedClassMap {
    
    /**
     * Represents cardinality from source class to target class.
     */
    public static class Cardinality {

        private final Class<? extends ElementBase> sourceClass;
        
        private final Class<? extends ElementBase> targetClass;
        
        private final int maxOccurrences;
        
        private Cardinality(Class<? extends ElementBase> sourceClass, Class<? extends ElementBase> targetClass,
            int maxOccurrences) {
            this.sourceClass = sourceClass;
            this.targetClass = targetClass;
            this.maxOccurrences = maxOccurrences;
        }

        public Class<? extends ElementBase> getSourceClass() {
            return sourceClass;
        }

        public Class<? extends ElementBase> getTargetClass() {
            return targetClass;
        }

        public int getMaxOccurrences() {
            return maxOccurrences;
        }
    }
    
    /**
     * Maintains a list of cardinalities for a specific source class.
     */
    private static class Cardinalities {

        private final List<Cardinality> cardinalities = new ArrayList<>();

        private int total;
        
        /**
         * Return cardinality for the target class or one of its superclasses.
         *
         * @param targetClass
         * @return The associated cardinality, or null if none specified.
         */
        public Cardinality getCardinality(Class<? extends ElementBase> targetClass) {
            for (Cardinality cardinality : cardinalities) {
                if (cardinality.getTargetClass().isAssignableFrom(targetClass)) {
                    return cardinality;
                }
            }
            
            return null;
        }

        /**
         * Add a cardinality. Also updates the count of total cardinality.
         *
         * @param cardinality Cardinality to add.
         */
        protected void addCardinality(Cardinality cardinality) {
            cardinalities.add(cardinality);
            total = cardinality.maxOccurrences == Integer.MAX_VALUE ? Integer.MAX_VALUE : total + cardinality.maxOccurrences;
        }
    }
    
    private final Map<Class<? extends ElementBase>, Cardinalities> map = new HashMap<>();
    
    /**
     * Returns the cardinalities associated with this class or a superclass.
     *
     * @param sourceClass Class whose relation is sought.
     * @return The cardinalities, or null if none found.
     */
    public Cardinalities getCardinalities(Class<? extends ElementBase> sourceClass) {
        Class<?> clazz = sourceClass;
        Cardinalities cardinalities = null;
        
        while (cardinalities == null && clazz != null) {
            cardinalities = map.get(clazz);
            clazz = clazz == ElementBase.class ? null : clazz.getSuperclass();
        }
        
        return cardinalities;
    }

    /**
     * Returns cardinalities for the specified class, creating it if necessary.
     *
     * @param sourceClass Class whose cardinalities are sought.
     * @return The cardinalities associated with the specified class (never null).
     */
    private Cardinalities getOrCreateCardinalities(Class<? extends ElementBase> sourceClass) {
        Cardinalities cardinalities = map.get(sourceClass);
        
        if (cardinalities == null) {
            map.put(sourceClass, cardinalities = new Cardinalities());
        }
        
        return cardinalities;
    }
    
    /**
     * Adds cardinality relationship between source and target classes.
     *
     * @param sourceClass The source class.
     * @param targetClass Class to be registered.
     * @param maxOccurrences Maximum occurrences for this relationship.
     */
    public void addCardinality(Class<? extends ElementBase> sourceClass, Class<? extends ElementBase> targetClass,
                               int maxOccurrences) {
        Cardinality cardinality = new Cardinality(sourceClass, targetClass, maxOccurrences);
        getOrCreateCardinalities(sourceClass).addCardinality(cardinality);
    }
    
    /**
     * Returns true if the specified class has any related classes (i.e., has a total cardinality >
     * 0).
     *
     * @param sourceClass The source class.
     * @return True if the specified class has any related classes.
     */
    public boolean hasRelated(Class<? extends ElementBase> sourceClass) {
        return getTotalCardinality(sourceClass) > 0;
    }
    
    /**
     * Returns the sum of cardinalities across all related classes.
     *
     * @param sourceClass The source class.
     * @return The sum of cardinalities across all related classes.
     */
    public int getTotalCardinality(Class<? extends ElementBase> sourceClass) {
        Cardinalities cardinalities = getCardinalities(sourceClass);
        return cardinalities == null ? 0 : cardinalities.total;
    }
    
    /**
     * Returns true if targetClass or a superclass of targetClass is related to sourceClass.
     *
     * @param sourceClass The primary class.
     * @param targetClass The class to test.
     * @return True if targetClass or a superclass of targetClass is related to sourceClass.
     */
    public boolean isRelated(Class<? extends ElementBase> sourceClass, Class<? extends ElementBase> targetClass) {
        return getCardinality(sourceClass, targetClass).maxOccurrences > 0;
    }
    
    /**
     * Returns the cardinality between two element classes.
     *
     * @param sourceClass The primary class.
     * @param targetClass The class to test.
     * @return The cardinality in the class relationship (never null).
     */
    public Cardinality getCardinality(Class<? extends ElementBase> sourceClass, Class<? extends ElementBase> targetClass) {
        Cardinalities cardinalities = getCardinalities(sourceClass);
        Cardinality cardinality = cardinalities == null ? null : cardinalities.getCardinality(targetClass);
        return cardinality == null ? new Cardinality(sourceClass, targetClass, 0) : cardinality;
    }
    
}
