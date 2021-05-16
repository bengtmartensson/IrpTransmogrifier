/*
Copyright (C) 2017, 2019 Bengt Martensson.

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
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.harctoolbox.ircore.ThisCannotHappenException;

public final class ParameterCollector implements Cloneable {

    private final static Logger logger = Logger.getLogger(ParameterCollector.class.getName());

    public final static long INVALID = -1L;

    public final static ParameterCollector EMPTY = new ParameterCollector(new HashMap<>(0), null);

    private HashMap<String, BitwiseParameter> map;

    private final Map<String, Long> parameterSpecBitmasks;

    public ParameterCollector(int capacity, Map<String, Long>parameterSpecBitmasks) {
        map = new LinkedHashMap<>(capacity);
        this.parameterSpecBitmasks = parameterSpecBitmasks;
    }

    public ParameterCollector() {
        this(0, new HashMap<String, Long>(4));
    }

    ParameterCollector(Map<String, Long> nameMap, Map<String, Long>parameterSpecBitmasks) {
        this(nameMap.size(), parameterSpecBitmasks);
        nameMap.entrySet().stream().forEach((kvp) -> {
            try {
                add(kvp.getKey(), kvp.getValue());
            } catch (ParameterInconsistencyException ex) {
            }
        });
    }

    public ParameterCollector(NameEngine nameEngine, Map<String, Long>parameterSpecBitmasks) throws NameUnassignedException {
        this(nameEngine.size(), parameterSpecBitmasks);
        for (Map.Entry<String, Expression> kvp : nameEngine) {
            String name = kvp.getKey();
            Expression expr = kvp.getValue();
            try {
                add(name, expr.toLong(nameEngine));
            } catch (ParameterInconsistencyException ex) {
            }
        }
    }

    public ParameterCollector(NameEngine nameEngine) throws NameUnassignedException {
        this(nameEngine, new HashMap<>(0));
    }

    ParameterCollector(ParameterSpecs parameterSpecs) {
        this(new HashMap<>(4), parameterSpecs.bitmasks());
    }

    void add(String name, BitwiseParameter parameter) throws ParameterInconsistencyException {
        logger.log(Level.FINER, "Assigning {0} = {1}", new Object[]{name, parameter});
        BitwiseParameter oldParameter = map.get(name);
        if (oldParameter == parameter)
            return;
        if (oldParameter != null) {
            if (oldParameter.isConsistent(parameter)) {
                oldParameter.aggregate(parameter);
            } else {
                logger.log(Level.FINE, "Name inconsistency: {0}, new value: {1}, old value: {2}", new Object[]{name, parameter.toString(), oldParameter.toString()});
                throw new ParameterInconsistencyException(name, parameter, oldParameter);
            }
        } else {
            overwrite(name, parameter);
        }
    }

    void add(String name, long value) throws ParameterInconsistencyException {
        add(name, new BitwiseParameter(value));
    }

    void add(String name, long value, long bitmask) throws ParameterInconsistencyException {
        add(name, new BitwiseParameter(value, bitmask));
    }

    BitwiseParameter remove(String name) {
        return map.remove(name);
    }

    private void overwrite(String name, BitwiseParameter parameter) {
        logger.log(Level.FINER, "Overwriting {0} = {1}", new Object[]{name, parameter});
        map.put(name, parameter);
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

    public NameEngine toNameEngine() {
        NameEngine nameEngine = new NameEngine(map.size());
        map.entrySet().forEach((kvp) -> {
            String name = kvp.getKey();
            BitwiseParameter parameter = kvp.getValue();
            Long bitmask = parameterSpecBitmasks.get(name);
            if (/*!parameter.isEmpty() &&*/ parameter.isFinished(bitmask))
                try {
                    nameEngine.define(kvp.getKey(), parameter.getValue/*PreferExpected*/());
                } catch (InvalidNameException ex) {
                    throw new ThisCannotHappenException(ex);
                }
        });
        return nameEngine;
    }

    void fixParameterSpecs(ParameterSpecs parameterSpecs) {
        map.entrySet().forEach((kvp) -> {
            String name = kvp.getKey();
            BitwiseParameter parameter = kvp.getValue();
            Long bitmask = parameterSpecBitmasks.get(name);
            if (!parameter.isEmpty() && bitmask != null && parameter.isFinished(bitmask)) {
                long val = parameter.getValue();
                long modulus = 1L << parameter.length();
                val = parameterSpecs.fixValue(name, val, modulus);
                parameter.assign(val);
            }
        });
    }

    public Map<String, Long> collectedNames() {
        Map<String, Long> names = new HashMap<>(map.size());
        collectedNames(names);
        return names;
    }

    public void collectedNames(Map<String, Long> names) {
        map.entrySet().forEach((kvp) -> {
            BitwiseParameter parameter = kvp.getValue();
            if (!parameter.isEmpty())
                names.put(kvp.getKey(), parameter.getValue());
        });
    }

    void transferToNamesMap(Map<String, Long> nameEngine) {
        map.entrySet().stream().forEach((kvp) -> {
            nameEngine.put(kvp.getKey(), kvp.getValue().getValue());
        });
    }

    @Override
    public String toString() {
        StringJoiner str = new StringJoiner(";", "{", "}");
        map.entrySet().stream().forEach((kvp) -> {
            str.add(kvp.getKey() + "=" + kvp.getValue().toString());
        });
        return str.toString();
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
            result.map.put(kvp.getKey(), new BitwiseParameter(kvp.getValue()));
        });
        return result;
    }

    boolean isConsistent(String name, long value) {
        BitwiseParameter param = get(name);
        return param.isConsistent(value);
    }

    void checkConsistency(RecognizeData recognizeData) throws NameUnassignedException, ParameterInconsistencyException {
        for (Map.Entry<String, BitwiseParameter> kvp : map.entrySet()) {
            String name = kvp.getKey();
            BitwiseParameter param = kvp.getValue();
            Expression expression = recognizeData.nameEngine.get(name);
            BitwiseParameter expected = expression.toBitwiseParameter(recognizeData);
            if (!param.isConsistent(expected))
                throw new ParameterInconsistencyException(name, expected, param);
        }
    }

    public boolean contains(String name) {
        return map.containsKey(name);
    }

    public Long getBitmask(String name) {
        return parameterSpecBitmasks.get(name);
    }

    public boolean isFinished(String name) {
        BitwiseParameter param = get(name);
        Long bitmask = parameterSpecBitmasks.get(name);
        return param.isFinished(bitmask);
    }
}
