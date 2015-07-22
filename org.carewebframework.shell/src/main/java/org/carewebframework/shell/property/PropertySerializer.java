/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.shell.property;

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;

import org.carewebframework.api.spring.SpringUtil;
import org.carewebframework.common.MiscUtil;

/**
 * Base class for property serialization support. Contains a number of preconfigured serializers.
 *
 * @param <T> Data type of property.
 */
public abstract class PropertySerializer<T> {
    
    /**
     * Serialize an object instance to a string. Override to provide alternate implementations.
     * 
     * @param value Object instance.
     * @return Serialized form.
     */
    public String serialize(Object value) {
        return value.toString();
    }
    
    /**
     * Deserialize a string to an object instance.
     * 
     * @param value String value.
     * @return Object instance.
     */
    public abstract Object deserialize(String value);
    
    /**
     * Serializer for String data type.
     */
    public static final PropertySerializer<String> STRING = new PropertySerializer<String>() {
        
        @Override
        public String deserialize(String value) {
            return value;
        }
        
    };
    
    /**
     * Serializer for Integer data type.
     */
    public static final PropertySerializer<Integer> INTEGER = new PropertySerializer<Integer>() {
        
        @Override
        public Integer deserialize(String value) {
            return Integer.parseInt(value);
        }
        
    };
    
    /**
     * Serializer for Long data type.
     */
    public static final PropertySerializer<Long> LONG = new PropertySerializer<Long>() {
        
        @Override
        public Long deserialize(String value) {
            return Long.parseLong(value);
        }
    };
    
    /**
     * Serializer for Double data type.
     */
    public static final PropertySerializer<Double> DOUBLE = new PropertySerializer<Double>() {
        
        @Override
        public Double deserialize(String value) {
            return Double.parseDouble(value);
        }
    };
    
    /**
     * Serializer for Boolean data type.
     */
    public static final PropertySerializer<Boolean> BOOLEAN = new PropertySerializer<Boolean>() {
        
        @Override
        public Boolean deserialize(String value) {
            return Boolean.parseBoolean(value);
        }
    };
    
    /**
     * Serializer for Date data type.
     */
    public static final PropertySerializer<Date> DATE = new PropertySerializer<Date>() {
        
        @Override
        public String serialize(Object value) {
            return Long.toString(((Date) value).getTime());
        }
        
        @Override
        public Date deserialize(String value) {
            return new Date(Long.parseLong(value));
        }
    };
    
    /**
     * Serializer class for enumerations.
     */
    @SuppressWarnings("rawtypes")
    public static class EnumSerializer extends PropertySerializer<Enum>implements Iterable {
        
        private final Class<Enum> enumClass;
        
        public EnumSerializer(Class<Enum> enumClass) {
            this.enumClass = enumClass;
        }
        
        @Override
        public String serialize(Object value) {
            return ((Enum<?>) value).name();
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public Object deserialize(String value) {
            return Enum.valueOf(enumClass, value);
        }
        
        @Override
        public Iterator iterator() {
            return Arrays.asList(enumClass.getEnumConstants()).iterator();
        }
        
    };
    
    /**
     * Serializer class for iterables.
     */
    @SuppressWarnings("rawtypes")
    public static class IterableSerializer extends PropertySerializer<Iterable>implements Iterable {
        
        private final Class<Iterable> iterClass;
        
        private final String beanId;
        
        public IterableSerializer(String beanId) {
            this.iterClass = null;
            this.beanId = beanId;
        }
        
        public IterableSerializer(Class<Iterable> iterClass) {
            this.iterClass = iterClass;
            this.beanId = null;
        }
        
        @Override
        public Object deserialize(String value) {
            for (Object member : this) {
                if (value.equals(member.toString())) {
                    return member;
                }
            }
            
            throw new IllegalArgumentException(value);
        }
        
        public Iterable getIterator() {
            try {
                return iterClass != null ? iterClass.newInstance()
                        : SpringUtil.getAppContext().getBean(beanId, Iterable.class);
            } catch (Exception e) {
                throw MiscUtil.toUnchecked(e);
            }
        }
        
        @Override
        public Iterator iterator() {
            return getIterator().iterator();
        }
        
    };
    
}
