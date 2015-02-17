package org.zuzuk.utils.serialization;

import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public enum ClassFields {

    Instance;

    private HashMap<Class, ArrayList<Field>> cacheMap = new HashMap<>();

    public synchronized ArrayList<Field> getFields(Object object) {
        Class<?> objectClass = object.getClass();
        ArrayList<Field> result = cacheMap.get(objectClass);
        if (result == null) {
            result = createFields(objectClass);
            cacheMap.put(objectClass, result);
        }
        return result;
    }

    private static ArrayList<Field> createFields(Class objectClass) {
        ArrayList<Field> result = new ArrayList<>();
        List<Field> declaredFields = FieldUtils.getAllFieldsList(objectClass);
        for (Field field : declaredFields) {
            int modifiers = field.getModifiers();
            if (!Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers)) {
                field.setAccessible(true);
                result.add(field);
            }
        }
        return result;
    }

}
