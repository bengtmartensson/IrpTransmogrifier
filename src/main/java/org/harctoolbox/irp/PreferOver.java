/*
Copyright (C) 2019 Bengt Martensson.

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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class PreferOver implements Serializable {

    public static List<PreferOver> parse(Iterable<String> strings) {
        if (strings == null)
            return new ArrayList<>(0);
        List<PreferOver> result = new ArrayList<>(8);
        for (String string : strings)
            result.add(parse(string));
        return result;
    }

    public static PreferOver parse(String string) {
        String[] arr = string.split(";");
        return arr.length == 1 ? new PreferOver(string) : new PreferOver(arr[1].trim(), arr[0]);
    }

    private Expression predicate;
    private String protocolName;
//    private NamedProtocol disfavored = null;

    public PreferOver(String name, Expression predicate) {
        Objects.requireNonNull(predicate, "Predicate must be non-null.");
        this.protocolName = name;
        this.predicate = predicate;
    }

    public PreferOver(String name, String predicateString) {
        this(name, Expression.newExpression(predicateString));
    }

    public PreferOver(String name) {
        this(name, Expression.TRUE);
    }

//    public void expand(IrpDatabase irpDatabase) throws UnknownProtocolException, InvalidNameException, UnsupportedRepeatException, IrpInvalidArgumentException, NameUnassignedException {
//        disfavored = irpDatabase.getNamedProtocol(protocolName);
//    }

    public String toBeRemoved(NameEngine nameEngine) {
        try {
            return predicate.toLong(nameEngine) == 0L ? null : protocolName;
        } catch (NameUnassignedException ex) {
            return null;
        }
    }

    public String toBeRemoved(Map<String, Long> map) {
        return toBeRemoved(new NameEngine(map));
    }

    public String toBeRemoved() {
        return protocolName;
    }

    @Override
    public String toString() {
        return (predicate == Expression.TRUE ? "" : predicate.toString()) + protocolName;
    }
}
