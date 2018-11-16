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
import java.util.Objects;
import org.antlr.v4.runtime.tree.ParseTree;
import org.harctoolbox.ircore.ThisCannotHappenException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

final class NameExpression extends PrimaryItemExpression {

    static Expression newExpression(ParseTree original, IrpParser.NameContext nameContext) {
        try {
            return new NameExpression(original, nameContext.getText());
        } catch (InvalidNameException ex) {
            throw new ThisCannotHappenException(ex);
        }
    }

    static Expression newExpression(Name name) {
        return new NameExpression(null, name);
    }

    private final Name name;

    private NameExpression(ParseTree original, String text) throws InvalidNameException {
        this(original, new Name(text));
    }

    private NameExpression(ParseTree original, Name name) {
        super(original);
        this.name = name;
    }

    @Override
    public String toIrpString(int radix) {
        return name.toIrpString(radix);
    }

    @Override
    public Map<String, Object> propertiesMap(boolean eval, GeneralSpec generalSpec, NameEngine nameEngine) {
        Map<String, Object> map = name.propertiesMap(true, generalSpec, nameEngine);
        map.put("scalar", true);
        return map;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final NameExpression other = (NameExpression) obj;
        return Objects.equals(this.name, other.name);
    }

    @Override
    public long toLong(NameEngine nameEngine) throws NameUnassignedException {
        return name.toLong(nameEngine);
    }

    @Override
    public Element toElement(Document document) {
        Element el = super.toElement(document);
        el.appendChild(name.toElement(document));
        return el;
    }

    @Override
    public Long invert(long rhs, NameEngine nameEngine, long bitmask) {
        return rhs;
    }

    @Override
    public PrimaryItem leftHandSide() {
        return name;
    }

    @Override
    public PrimaryItem substituteConstantVariables(Map<String, Long> constantVariables) {
        return name.substituteConstantVariables(constantVariables);
    }
}
