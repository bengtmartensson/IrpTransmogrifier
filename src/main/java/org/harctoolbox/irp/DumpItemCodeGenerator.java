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
import java.util.List;
import java.util.Map;
import org.harctoolbox.ircore.IrCoreUtils;

public final class DumpItemCodeGenerator extends ItemCodeGenerator {

    private static String mkPrefix(int level) {
        StringBuilder s = new StringBuilder(level);
        for (int i = 0; i < level; i++)
            s.append('\t');
        return s.toString();
    }

    @SuppressWarnings("unchecked")
    private static String render(Object object, int level) {
        if (object instanceof List<?>)
            return render((List<?>) object, level);
        else if (object instanceof Map<?, ?>)
            return render((Map<String, Object>) object, level);
        else
            return object.toString();
    }

    private static String render(List<?> list, int level) {
        StringBuilder str = new StringBuilder(100);
        String prefix = mkPrefix(level);
        str.append("[");
        if (list.isEmpty())
            str.append("]");
        else {
            list.forEach((obj) -> {
                str.append(IrCoreUtils.LINESEPARATOR).append(prefix).append(render(obj, level + 1));
            });

            str.append(IrCoreUtils.LINESEPARATOR).append(mkPrefix(level - 1)).append(']');
        }
        return str.toString();
    }

    private static String render(Map<String, Object> map, int level) {
        StringBuilder str = new StringBuilder(100);
        String prefix = mkPrefix(level);
        str.append("{");
        if (map.isEmpty())
            str.append("}");
        else {
            map.entrySet().forEach((kvp) -> {
                str.append(IrCoreUtils.LINESEPARATOR).append(prefix).append(kvp.getKey()).append(" = ").append(render(kvp.getValue(), level + 1));
            });

            str.append(IrCoreUtils.LINESEPARATOR).append(mkPrefix(level - 1)).append('}');
        }
        return str.toString();
    }

    private static String render(String name, Object object, int level) {
        String prefix = mkPrefix(level);
        StringBuilder str = new StringBuilder(100);
        str.append(prefix).append(name).append(" = ").append(render(object, level + 1));
        return str.toString();
    }

    private final String name;
    private final Map<String, Object> aggregates;

    public DumpItemCodeGenerator(String name) {
        this.name = name;
        aggregates = new HashMap<>(10);
    }

    @Override
    public void setAttribute(String name, Object value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addAttribute(String name, Object value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addAggregate(String string, Object... args) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addAggregateList(String name, Map<String, Object> map) {
        aggregates.put(name, map);
    }

    @Override
    public void inspect() {
        throw new UnsupportedOperationException("inspect not supported.");
    }

    @Override
    public String render() {
        return render(name, aggregates, 0) + IrCoreUtils.LINESEPARATOR;
    }

    @Override
    public void inspectAndWait() {
        throw new UnsupportedOperationException("inspect not supported.");
    }
}
