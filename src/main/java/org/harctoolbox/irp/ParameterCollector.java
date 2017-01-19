/*
Copyright (C) 2017 Bengt Martensson.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or (at
your option) any later version.

This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License along with
this program. If not, see http://www.gnu.org/licenses/.
*/

package org.harctoolbox.irp;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ParameterCollector implements Cloneable {

    private final static Logger logger = Logger.getLogger(ParameterCollector.class.getName());

    public final static long INVALID = -1L;

    private HashMap<String, BitwiseParameter> map;

    public ParameterCollector() {
        map = new LinkedHashMap<>(8);
    }

    ParameterCollector(Map<String, Long> nameMap) {
        this();
        nameMap.entrySet().stream().forEach((kvp) -> {
            try {
                add(kvp.getKey(), kvp.getValue());
            } catch (NameConflictException ex) {
            }
        });
    }

    void add(String name, BitwiseParameter parameter) throws NameConflictException {
        logger.log(Level.FINER, "Assigning {0} = {1}", new Object[]{name, parameter});
        BitwiseParameter oldParameter = map.get(name);
        if (oldParameter != null) {
            if (oldParameter.isConsistent(parameter)) {
                oldParameter.aggregate(parameter);
            } else {
                logger.log(Level.FINE, "Name inconsistency: {0}, new value: {1}, old value: {2}", new Object[]{name, parameter.toString(), oldParameter.toString()});
                throw new NameConflictException(name, parameter.getValue(), oldParameter.getValue());
            }
        } else {
            overwrite(name, parameter);
        }
    }

    final void add(String name, long value) throws NameConflictException {
        add(name, new BitwiseParameter(value));
    }

    void add(String name, long value, long bitmask) throws NameConflictException {
        add(name, new BitwiseParameter(value, bitmask));
    }

    private void overwrite(String name, BitwiseParameter parameter) {
        logger.log(Level.FINER, "Overwriting {0} = {1}", new Object[]{name, parameter});
        map.put(name, parameter);
    }

    void setExpected(String name, long value) {
        logger.log(Level.FINER, "Set expected {0} = {1}", new Object[]{name, value});
        BitwiseParameter oldParameter = map.get(name);
        if (oldParameter != null)
            oldParameter.setExpected(value);
        else
            map.put(name, new BitwiseParameter(0L, 0L, value));
    }

    public Set<String> getNames() {
        return map.keySet();
    }

    BitwiseParameter get(String name) {
        return map.get(name);
    }

    public long getValue(String name) {
        return map.containsKey(name) ? map.get(name).getValue() : INVALID;
    }

    public NameEngine toNameEngine() throws InvalidNameException {
        NameEngine nameEngine = new NameEngine();
        for (Map.Entry<String, BitwiseParameter> kvp : map.entrySet()) {
            BitwiseParameter parameter = kvp.getValue();
            if (!parameter.isEmpty())
                nameEngine.define(kvp.getKey(), parameter.getValuePreferExpected());
        }
        return nameEngine;
    }

    public Map<String, Long> collectedNames() throws InvalidNameException {
        Map<String, Long> names = new HashMap<>(map.size());
        map.entrySet().forEach((kvp) -> {
            BitwiseParameter parameter = kvp.getValue();
            if (!parameter.isEmpty())
                names.put(kvp.getKey(), parameter.getValue());
        });
        return names;
    }

    void transferToNamesMap(Map<String, Long> nameEngine) {
        map.entrySet().stream().forEach((kvp) -> {
            nameEngine.put(kvp.getKey(), kvp.getValue().getValue());
        });
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder(100);
        str.append("{");
        map.entrySet().stream().forEach((kvp) -> {
            if (str.length() > 1)
                str.append(";");
            str.append(kvp.getKey()).append("=").append(kvp.getValue().toString());
        });
        return str.append("}").toString();
    }

    @Override
    @SuppressWarnings("CloneDeclaresCloneNotSupported")
    public ParameterCollector clone() {
        ParameterCollector result;
        try {
            result = (ParameterCollector) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new InternalError(ex);
        }
        result.map = new LinkedHashMap<>(10);
        map.entrySet().stream().forEach((kvp) -> {
            result.map.put(kvp.getKey(), kvp.getValue().clone());
        });
        return result;
    }

    boolean isConsistent(String name, long value) {
        BitwiseParameter param = get(name);
        return param.isConsistent(value);
    }

    void checkConsistency(NameEngine nameEngine, NameEngine definitions) throws NameConflictException, UnassignedException {
        for (Map.Entry<String, BitwiseParameter> kvp : map.entrySet()) {
            String name = kvp.getKey();
            BitwiseParameter param = kvp.getValue();
            Expression expression = definitions.get(name);
            long expected = expression.toNumber(nameEngine);
            if (!param.isConsistent(expected))
                throw new NameConflictException(name, param.getValue(), expected);
        }
    }
}
