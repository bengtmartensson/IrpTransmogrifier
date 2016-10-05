/*
Copyright (C) 2011, 2015 Bengt Martensson.

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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.harctoolbox.ircore.IncompatibleArgumentException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 */
public class ParameterSpecs extends IrpObject implements Iterable<ParameterSpec> {

    private LinkedHashMap<String, ParameterSpec>map;

    public ParameterSpecs() {
        map = new LinkedHashMap<>(3);
    }

    public ParameterSpecs(String parameter_specs) throws IrpSyntaxException {
        this(new ParserDriver(parameter_specs).getParser().parameter_specs());
    }

    public ParameterSpecs(IrpParser.ProtocolContext ctx) {
        this(ctx.parameter_specs());
    }

    public ParameterSpecs(IrpParser.Parameter_specsContext t) {
        this();
        if (t != null) {
            t.parameter_spec().stream().map((parameterSpec) -> new ParameterSpec(parameterSpec)).forEach((ps) -> {
                map.put(ps.getName(), ps);
            });
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ParameterSpecs))
            return false;

        ParameterSpecs other = (ParameterSpecs) obj;
        if (map.size() != other.map.size())
            return false;

        for (Map.Entry<String, ParameterSpec> kvp : map.entrySet()) {
            String key = kvp.getKey();
            if (!kvp.getValue().equals(other.map.get(key)))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.map);
        return hash;
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }
    public Set<String> getNames() {
        return map.keySet();
    }
    public Collection<ParameterSpec> getParams() {
        return map.values();
    }
    public ParameterSpec getParameterSpec(String name) {
        return map.get(name);
    }

    @Override
    public String toString() {
        if (isEmpty())
            return "";
        StringBuilder str = new StringBuilder("[");
        map.values().stream().forEach((ps) -> {
            str.append(ps.toString()).append(",");
        });

        if (str.length() > 0)
            str.deleteCharAt(str.length()-1);
        str.append("]");
        return str.toString();
    }

    @Override
    public String toIrpString() {
        return toString();
    }

    @Override
    public Element toElement(Document document) {
        Element el = document.createElement("parameters");
        //el.appendChild(document.createComment(toString()));
        for (ParameterSpec parameterSpec : map.values())
            el.appendChild(parameterSpec.toElement(document));
        return el;
    }

    void check(NameEngine nameEngine) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException, DomainViolationException {
        for (ParameterSpec parameter : map.values())
            parameter.check(nameEngine);
    }

    public NameEngine random() throws IrpSyntaxException {
        NameEngine nameEngine = new NameEngine();
        for (ParameterSpec parameter : map.values())
            nameEngine.define(parameter.getName(), parameter.random());

        return nameEngine;
    }

    public NameEngine randomUsingDefaults() throws IrpSyntaxException {
        NameEngine nameEngine = new NameEngine();
        for (ParameterSpec parameter : map.values())
            if (parameter.getDefault() == null)
                nameEngine.define(parameter.getName(), parameter.random());

        return nameEngine;
    }

    @Override
    public Iterator<ParameterSpec> iterator() {
        return map.values().iterator();
    }

    boolean contains(String name) {
        return map.containsKey(name);
    }

    @Override
    public int weight() {
        int weight = 0;
        weight = map.values().stream().map((parameterSpec) -> parameterSpec.weight()).reduce(weight, Integer::sum);
        return weight;
    }
}
