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
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 */
public class Name extends PrimaryItem implements Floatable {
    private static final int WEIGHT = 1;

    /**
     * Check the syntactical correctness of the name.
     *
     * This invokes a newly constructed parser, i.e. is comparatively expensive.
     *
     * @param name Name to be checked
     * @return true if the name is syntactically valid.
     * @throws org.harctoolbox.irp.InvalidNameException
     */
    //  Alternatively, could check agains a regexp. But this keeps the grammar in one place.
    public static String parse(String name) throws InvalidNameException {
        try {
            ParserDriver parserDriver = new ParserDriver(name);
            IrpParser.NameContext nam = parserDriver.getParser().name();
            return nam.getText();
        } catch (ParseCancellationException ex) {
            throw new InvalidNameException(name);
        }
    }

    public static String parse(IrpParser.NameContext ctx) {
        return ctx.getText();
    }

    /**
     * Check the syntactical correctness of the name.
     *
     * This invokes a newly constructed parser, i.e. is comparatively expensive.
     *
     * @param name Name to be checked
     * @return true iff the name is syntactically valid.
     */
    public static boolean validName(String name) {
        try {
            String nam = parse(name);
            return nam.equals(name.trim());
        } catch (InvalidNameException ex) {
            return false;
        }
    }

    public static long toNumber(IrpParser.NameContext ctx, NameEngine nameEngine) throws UnassignedException {
        Expression exp = nameEngine.get(toString(ctx));
        return exp.toNumber(nameEngine);
    }

    public static String toString(IrpParser.NameContext ctx) {
        return ctx.getText();
    }

    private final String name;

    public Name(IrpParser.NameContext ctx) {
        name = ctx.getText();
    }

    public Name(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Name))
            return false;

        return name.equals(((Name) obj).name);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 61 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public String toIrpString() {
        return getName();
    }

    @Override
    public String toIrpString(int radix) {
        return toIrpString();
    }

    @Override
    public long toNumber(NameEngine nameEngine) throws UnassignedException {
        if (nameEngine == null)
            throw new UnassignedException(name);
        Expression expression = nameEngine.get(getName());
        return expression.toNumber(nameEngine);
    }

    @Override
    public Element toElement(Document document) {
        Element element = super.toElement(document);
        element.setTextContent(toString());
        return element;
    }

    @Override
    public double toFloat(NameEngine nameEngine, GeneralSpec generalSpec) throws UnassignedException {
        return toNumber(nameEngine);
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    @Override
    public Name toName() {
        return this;
    }

    @Override
    public int weight() {
        return WEIGHT;
    }

    @Override
    public long invert(long rhs) {
        return rhs;
    }

    @Override
    public boolean isUnary() {
        return true;
    }

    @Override
    public Map<String, Object> propertiesMap(boolean eval, GeneralSpec generalSpec, NameEngine nameEngine) {
        Map<String, Object> map = super.propertiesMap(4);
        map.put("name", name);
        map.put("eval", eval);
        map.put("scalar", eval);
        map.put("isDefinition", nameEngine.containsKey(name));
        return map;
    }
}
