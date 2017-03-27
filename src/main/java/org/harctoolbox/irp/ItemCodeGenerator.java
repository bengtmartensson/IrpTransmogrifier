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

import java.util.Map;

public abstract class ItemCodeGenerator {

    protected ItemCodeGenerator() {}

    public abstract void setAttribute(String name, Object value);

    public abstract void addAttribute(String name, Object value);

    public abstract void addAggregate(String string, Object... args);

   public void addAggregateList(String name, AggregateLister aggregateLister, GeneralSpec generalSpec, NameEngine nameEngine) throws IrpSemanticException {
        Map<String, Object> map = aggregateLister.propertiesMap(generalSpec, nameEngine);
        addAggregateList(name, map);
    }

    public abstract void addAggregateList(String name, Map<String, Object> map);

    public abstract void inspect();

    public void setAttribute(Map<String, Object> map) {
        map.entrySet().stream().forEach((kvp) -> {
            setAttribute(kvp.getKey(), kvp.getValue());
        });
    }

    public void addAttribute(Map<String, Object> map) {
        map.entrySet().stream().forEach((kvp) -> {
            addAttribute(kvp.getKey(), kvp.getValue());
        });
    }

    /**
     * For uniformity, should end with a line separator.
     * @return
     */
    public abstract String render();
}
