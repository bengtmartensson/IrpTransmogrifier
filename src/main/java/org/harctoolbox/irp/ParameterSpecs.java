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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import org.harctoolbox.ircore.ThisCannotHappenException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public final class ParameterSpecs extends IrpObject implements Iterable<ParameterSpec>,AggregateLister {

    private Map<String, ParameterSpec> map = new LinkedHashMap<>(3);

    public ParameterSpecs() {
        super(null);
    }

    public ParameterSpecs(String parameter_specs) {
        this(new ParserDriver(parameter_specs).getParser().parameter_specs());
    }

    public ParameterSpecs(IrpParser.ProtocolContext ctx) {
        this(ctx.parameter_specs());
    }

    public ParameterSpecs(IrpParser.Parameter_specsContext ctx) {
        super(ctx);
        if (ctx == null)
            return;

        ctx.parameter_spec().stream().map((parameterSpec) -> new ParameterSpec(parameterSpec)).forEachOrdered((ps) -> {
            map.put(ps.getName(), ps);
        });
    }

    public ParameterSpecs(List<ParameterSpec> list) {
        this();
        list.forEach((ps) -> {
            map.put(ps.getName(), ps);
        });
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ParameterSpecs))
            return false;

        ParameterSpecs other = (ParameterSpecs) obj;
        if (map.size() != other.map.size())
            return false;

        boolean result = true;
        for (Map.Entry<String, ParameterSpec> kvp : map.entrySet()) {
            String key = kvp.getKey();
            if (!kvp.getValue().equals(other.map.get(key)))
                result = false;
        }
        return result;
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
    public String toIrpString(int radix) {
        return toIrpString(radix, "");
    }

    public String toIrpString(int radix, String separator) {
        if (isEmpty())
            return separator;
        StringBuilder str = new StringBuilder("[");
        map.values().stream().forEach((ps) -> {
            str.append(ps.toIrpString(radix)).append(",").append(separator);
        });

        if (str.length() > 0)
            str.delete(str.length() - 1 - separator.length(), str.length());
        str.append("]");
        return str.toString();
    }

//    @Override
//    public String toIrpString() {
//        return toString();
//    }

    @Override
    public Element toElement(Document document) {
        Element el = super.toElement(document);
        map.values().forEach((parameterSpec) -> {
            el.appendChild(parameterSpec.toElement(document));
        });
        return el;
    }

    void check(NameEngine nameEngine) throws DomainViolationException, InvalidNameException, NameUnassignedException {
        for (ParameterSpec parameter : map.values())
            parameter.check(nameEngine);
    }

    public Map<String, Long> random() {
        Map<String, Long> nameEngine = new HashMap<>(map.size());
        map.values().stream().forEach((parameter) -> {
            nameEngine.put(parameter.getName(), parameter.random());
        });

        return nameEngine;
    }

    public Map<String, Long> random(Random rng) {
        Map<String, Long> nameEngine = new HashMap<>(map.size());
        map.values().stream().forEach((parameter) -> {
            nameEngine.put(parameter.getName(), parameter.random(rng));
        });

        return nameEngine;
    }

    public Map<String, Long> randomUsingDefaults() {
        Map<String, Long> nameEngine = new HashMap<>(map.size());
        map.values().stream().filter((parameter) -> (parameter.getDefault() == null)).forEach((parameter) -> {
            nameEngine.put(parameter.getName(), parameter.random());
        });

        return nameEngine;
    }

    public Map<String, Long> randomUsingDefaults(Random rng) {
        Map<String, Long> nameEngine = new HashMap<>(map.size());
        map.values().stream().filter((parameter) -> (parameter.getDefault() == null)).forEach((parameter) -> {
            nameEngine.put(parameter.getName(), parameter.random(rng));
        });

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

    void reduceNamesMap(Map<String, Long> namesMap, boolean keepDefaulted) {
        removeNotInParameterSpec(namesMap);
        if (!keepDefaulted)
            remoteDefaulteds(namesMap);
    }

    private void remoteDefaulteds(Map<String, Long> namesMap) {
        NameEngine nameEngine = new NameEngine(namesMap);
        List<String> names = new ArrayList<>(namesMap.keySet());
        names.forEach((String name) -> {
            Expression expression = map.get(name).getDefault();
            if (!(expression == null))
                try {
                    long deflt = expression.toNumber(nameEngine);
                    if (namesMap.get(name) == deflt)
                        namesMap.remove(name);
                } catch (NameUnassignedException ex) {
                    throw new ThisCannotHappenException();
                }
        });
    }

    private void removeNotInParameterSpec(Map<String, Long> namesMap) {
        List<String> names = new ArrayList<>(namesMap.keySet());
         names.stream().filter((name) -> (!map.containsKey(name))).forEach((name) -> {
            namesMap.remove(name);
        });
    }

    public String code(CodeGenerator codeGenerator) {
        ItemCodeGenerator template = codeGenerator.newItemCodeGenerator(this);
        List<String> list = new ArrayList<>(map.size());
        map.values().stream().forEach((param) -> {
            list.add(param.code(codeGenerator));
        });

        template.addAttribute("arg", list);
        return template.render();
    }

//    void XXfillAttributes(ItemCodeGenerator template, String parameterSpecsName) {
//        map.values().stream().forEach((ps) -> {
//            template.addAggregate(parameterSpecsName + ".{name, min, max, memory}", ps.getName(), ps.getMin(), ps.getMax(), ps.hasMemory());
//        });
//    }

    @Override
    public Map<String, Object> propertiesMap(GeneralSpec generalSpec, NameEngine nameEngine) {
        Map<String, Object> result = new HashMap<>(2);
        result.put("kind", this.getClass().getSimpleName());
        List<Map<String, Object>> list = new ArrayList<>(map.size());
        result.put("list", list);
        map.values().stream().forEach((ps) -> {
            Map<String, Object> m = new HashMap<>(5);
            list.add(m);
            m.put("name", ps.getName());
            m.put("min", ps.getMin());
            m.put("max", ps.getMax());
            m.put("memory", ps.hasMemory());
            Expression dflt = ps.getDefault();
            if (dflt != null)
                m.put("default", dflt);
        });
        return result;
    }

    public boolean hasParameter(String name) {
        return map.containsKey(name);
    }

    public boolean hasParameterMemory(String parameterName) {
        ParameterSpec parameterSpec = getParameterSpec(parameterName);
        return parameterSpec != null && parameterSpec.hasMemory();
    }

    public long getParameterMax(String parameterName) throws NullPointerException {
        ParameterSpec parameterSpec = getParameterSpec(parameterName);
        return parameterSpec.getMax();
    }

    public long getParameterMin(String parameterName) throws NullPointerException {
        ParameterSpec parameterSpec = getParameterSpec(parameterName);
        return parameterSpec.getMin();
    }
}
