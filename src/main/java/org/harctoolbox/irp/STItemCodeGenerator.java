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
import java.util.Locale;
import java.util.Map;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.misc.Aggregate;

public class STItemCodeGenerator extends ItemCodeGenerator {

    private final ST st;

    STItemCodeGenerator(ST st) {
        this.st = st;
    }

    @Override
    public void addAttribute(String name, Object value) {
        st.add(name, value);
    }

    @Override
    public void setAttribute(String name, Object value) {
        st.remove(name);
        st.add(name, value);
    }

    @Override
    public String render() {
        return st.render(Locale.US);
    }

    @Override
    public void inspect() {
        st.inspect();
    }

    @Override
    public void addAggregate(String string, Object... args) {
        st.addAggr(string, args);
    }

    @Override
    public void addAggregateList(String name, AggregateLister aggregateLister, GeneralSpec generalSpec, NameEngine nameEngine) {
        Map<String, Object> map = aggregateLister.propertiesMap(generalSpec, nameEngine);
        addAggregateList(name, map);
    }

    @Override
    public void addAggregateList(String name, Map<String, Object> map) {
        Aggregate aggregate = new Aggregate();
        aggregate.properties = new HashMap<>(map);
        st.add(name, aggregate);
    }
}
