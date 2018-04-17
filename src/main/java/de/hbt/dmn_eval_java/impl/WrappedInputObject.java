/*
 *  ©2018 HBT Hamburger Berater Team GmbH
 *  All Rights Reserved.
 */
package de.hbt.dmn_eval_java.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WrappedInputObject {

    private String type;
    private Object value;
    private List<String> properties = new ArrayList<>();

    public static WrappedInputObject fromPlainValue(Object value) {
        WrappedInputObject wrappedInputObject = new WrappedInputObject();
        wrappedInputObject.setValue(value);
        wrappedInputObject.setType("plain");
        return wrappedInputObject;
    }

    public static WrappedInputObject fromMap(Object map, Collection<String> keys) {
        WrappedInputObject wrappedInputObject = new WrappedInputObject();
        wrappedInputObject.setValue(map);
        wrappedInputObject.setType("map");
        wrappedInputObject.getProperties().addAll(keys);
        return wrappedInputObject;
    }

    public static WrappedInputObject fromFunction(Object function, int parameterCount) {
        WrappedInputObject wrappedInputObject = new WrappedInputObject();
        wrappedInputObject.setValue(function);
        wrappedInputObject.setType("function-" + parameterCount);
        return wrappedInputObject;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public List<String> getProperties() {
        return properties;
    }
}
