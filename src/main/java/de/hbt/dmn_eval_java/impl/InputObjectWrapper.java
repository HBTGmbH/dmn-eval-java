/*
 *  ©2018 HBT Hamburger Berater Team GmbH
 *  All Rights Reserved.
 */
package de.hbt.dmn_eval_java.impl;

import de.hbt.dmn_eval_java.DecisionEvaluationException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class to wrap the input for a decision evaluation so that in Javascript it is possible to enumerate
 * over the attributes.
 */
public final class InputObjectWrapper {

    public static final InputObjectWrapper INSTANCE = new InputObjectWrapper();

    private InputObjectWrapper() {
        // prevent instantiation
    }

    public WrappedInputObject wrapInput(Object input) {
        if (input instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) input;
            Map<String, Object> wrappedMap = new HashMap<>();
            for (Map.Entry<?, ?> entry: map.entrySet()) {
                WrappedInputObject wrappedValue = wrapInput(entry.getValue());
                wrappedMap.put(entry.getKey().toString(), wrappedValue);

            }
            return WrappedInputObject.fromMap(wrappedMap, wrappedMap.keySet());
        }
        Class<?> inputClass = input.getClass();
        if ((input instanceof Number) || (input instanceof String) || (input instanceof Boolean) || (input instanceof Date) ||
                (input instanceof Collection) || inputClass.isArray() || inputClass.isEnum()) {
            return WrappedInputObject.fromPlainValue(input);
        }
        if ((inputClass.getAnnotation(FunctionalInterface.class) != null) ||
                inputClass.isSynthetic() && !(inputClass.isLocalClass() || inputClass.isAnonymousClass())) {
            return wrapFunction(input);
        }
        return wrapObject(input);
    }

    public WrappedInputObject wrapObject(Object object) {
        Class<?> objectClass = object.getClass();
        List<String> properties = new ArrayList<>();
        Map<String, Object> objectMap = new HashMap<>();
        for (Method method: objectClass.getDeclaredMethods()) {
            String methodName = method.getName();
            if ((method.getParameterCount() == 0) && methodName.startsWith("get") && (methodName.length() > 3)) {
                String fieldName = methodName.substring(3, 4).toLowerCase() + ((methodName.length() > 4)? methodName.substring(4) : "");
                properties.add(fieldName);
                try {
                    Object fieldValue = method.invoke(object);
                    objectMap.put(fieldName, wrapInput(fieldValue));
                } catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
                    throw new DecisionEvaluationException("Failed to process decision input: cannot get value from getter method '" + methodName + "'", e);
                }
            }
        }
        return WrappedInputObject.fromMap(objectMap, properties);
    }

    public WrappedInputObject wrapFunction(Object function) {
        Class<?> functionClass = function.getClass();
        Method applyMethod = Arrays.stream(functionClass.getMethods()).filter(method -> method.getName().contains("apply")).findAny().get();

        int parameterCount = applyMethod.getParameterCount();
        return WrappedInputObject.fromFunction(function, parameterCount);
    }
}
