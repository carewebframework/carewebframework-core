/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.common;

import java.text.DateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeInfo.Id;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.map.jsontype.NamedType;
import org.codehaus.jackson.map.jsontype.TypeIdResolver;
import org.codehaus.jackson.map.jsontype.TypeResolverBuilder;
import org.codehaus.jackson.map.jsontype.impl.StdTypeResolverBuilder;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.type.TypeReference;

/**
 * A set of static methods supporting serialization and deserialization of objects using the JSON
 * format. This particular implementation uses the Jackson JSON library, but other JSON
 * implementations could be used.
 */
public class JSONUtil {
    
    public static final ObjectMapper mapper = new ObjectMapper();
    
    /**
     * Identifies properties that require type metadata (via "@class" property).
     */
    private static class CWTypeResolverBuilder extends StdTypeResolverBuilder {
        
        @Override
        public TypeDeserializer buildTypeDeserializer(DeserializationConfig config, JavaType baseType,
                                                      Collection<NamedType> subtypes, BeanProperty property) {
            if (property != null || baseType.isArrayType() || baseType.isCollectionLikeType()) {
                return null;
            }
            
            return super.buildTypeDeserializer(config, baseType, subtypes, property);
        }
        
        @Override
        public TypeSerializer buildTypeSerializer(SerializationConfig config, JavaType baseType,
                                                  Collection<NamedType> subtypes, BeanProperty property) {
            if (property != null || baseType.isArrayType() || baseType.isCollectionLikeType()) {
                return null;
            }
            
            return super.buildTypeSerializer(config, baseType, subtypes, property);
        }
    }
    
    /**
     * Resolves type identifiers to classes. Supports aliases and class names.
     */
    private static class CWTypedIdResolver implements TypeIdResolver {
        
        @Override
        public void init(JavaType baseType) {
        }
        
        @Override
        public String idFromValue(Object value) {
            return findId(value.getClass());
        }
        
        @Override
        public String idFromValueAndType(Object value, Class<?> suggestedType) {
            return findId(suggestedType);
        }
        
        @Override
        public JavaType typeFromId(String id) {
            try {
                return mapper.getTypeFactory().constructType(findClass(id));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        
        @Override
        public Id getMechanism() {
            return Id.CUSTOM;
        }
        
    }
    
    private static final Map<String, Class<?>> aliasToClass = new HashMap<String, Class<?>>();
    
    private static final Map<Class<?>, String> classToAlias = new HashMap<Class<?>, String>();
    
    static {
        TypeResolverBuilder<?> typer = new CWTypeResolverBuilder();
        typer = typer.init(JsonTypeInfo.Id.CLASS, new CWTypedIdResolver());
        typer = typer.inclusion(JsonTypeInfo.As.PROPERTY);
        typer = typer.typeProperty("@class");
        mapper.setDefaultTyping(typer);
    }
    
    /**
     * Register an alias for the specified class. The alias will be used when serializing objects of
     * this class. A given class or alias may only be registered once.
     * 
     * @param alias Alias to be used for serialization.
     * @param clazz Class to be associated with the alias.
     */
    public static synchronized void registerAlias(String alias, Class<?> clazz) {
        if (aliasToClass.containsKey(alias) && aliasToClass.get(alias) != clazz) {
            throw new RuntimeException("Alias '" + alias + "' is already registered to another class.");
        }
        
        if (classToAlias.containsKey(clazz) && classToAlias.get(clazz).equals(alias)) {
            throw new RuntimeException("Class '" + clazz.getName() + "' is already registered to another alias.");
        }
        
        aliasToClass.put(alias, clazz);
        classToAlias.put(clazz, alias);
    }
    
    /**
     * Removes a registered alias.
     * 
     * @param name Alias to be unregistered.
     */
    public static synchronized void unregisterAlias(String name) {
        classToAlias.remove(aliasToClass.get(name));
        aliasToClass.remove(name);
    }
    
    /**
     * Returns an alias given its associated class.
     * 
     * @param clazz The class whose alias is sought.
     * @return The alias associated with the specified class, or null if one does not exist.
     */
    public static final String getAlias(Class<?> clazz) {
        return classToAlias.get(clazz);
    }
    
    /**
     * Returns a class given its alias or class name.
     * 
     * @param id Alias or class name.
     * @return The associated class.
     * @throws ClassNotFoundException
     */
    private static Class<?> findClass(String id) throws ClassNotFoundException {
        Class<?> clazz = aliasToClass.get(id);
        return clazz == null ? ClassLoader.getSystemClassLoader().loadClass(id) : clazz;
    }
    
    /**
     * Returns the alias for a class or its class name if an alias has not been registered. This
     * value is used to identify the class type when serializing.
     * 
     * @param clazz Class whose id is sought.
     * @return The identifier to be used for serialization.
     */
    private static String findId(Class<?> clazz) {
        String id = classToAlias.get(clazz);
        return id == null ? clazz.getName() : id;
    }
    
    /**
     * Sets the date format to be used when serializing dates.
     * 
     * @param dateFormat Date format to use.
     */
    public static void setDateFormat(DateFormat dateFormat) {
        mapper.setDateFormat(dateFormat);
    }
    
    /**
     * Serializes an object to JSON format.
     * 
     * @param object Object to be serialized.
     * @return Serialized form of the object in JSON format.
     */
    public static String serialize(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Deserializes an object from JSON format.
     * 
     * @param data Serialized form of the object.
     * @return An instance of the deserialized object.
     */
    public static Object deserialize(String data) {
        if (data == null) {
            return null;
        }
        
        if (data.startsWith("[")) {
            return deserializeList(data, Object.class);
        }
        
        try {
            return mapper.readValue(data, Object.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Deserializes a list of objects.
     * 
     * @param data Serialized form of the list in JSON format.
     * @param clazz The class of objects found in the list.
     * @return A list of objects of the specified type.
     */
    public static <T> List<T> deserializeList(String data, Class<T> clazz) {
        try {
            return mapper.readValue(data, new TypeReference<List<T>>() {});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Enforce static class.
     */
    private JSONUtil() {
    };
}
