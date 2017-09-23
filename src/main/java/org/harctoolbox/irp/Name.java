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
import java.util.regex.Pattern;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 */
public final class Name extends PrimaryItem implements Floatable {
    private static final int WEIGHT = 1;
    private static Pattern namePattern = Pattern.compile(IrpUtils.C_IDENTIFIER_REGEXP);

//    /**
//     * Check the syntactical correctness of the name.
//     *
//     * This invokes a newly constructed parser, i.e. is comparatively expensive.
//     *
//     * @param name Name to be checked
//     * @return true if the name is syntactically valid.
//     * @throws org.harctoolbox.irp.InvalidNameException
//     */
//    //  Alternatively, could check agains a regexp. But this keeps the grammar in one place.
//    public static String parse(String name) {
////        try {
//            ParserDriver parserDriver = new ParserDriver(name);
//            IrpParser.NameContext nam = parserDriver.getParser().name();
//            return nam.getText();
////        } catch (ParseCancellationException ex) {
////            throw new InvalidNameException(name);
////        }
//    }

//    public static String parse(IrpParser.NameContext ctx) {
//        return ctx.getText();
//    }

    /**
     * Check the syntactical correctness of the name.
     *
     * This invokes a newly constructed parser, i.e. is comparatively expensive.
     *
     * @param name Name to be checked
     * @return true iff the name is syntactically valid.
     */
    public static boolean validName(String name) {
        return namePattern.matcher(name.trim()).matches();
    }


    public static long toNumber(IrpParser.NameContext ctx, NameEngine nameEngine) throws NameUnassignedException {
        Expression exp = nameEngine.get(toString(ctx));
        return exp.toNumber(nameEngine);
    }

    public static String toString(IrpParser.NameContext ctx) {
        return ctx.getText();
    }

    /**
     * Check the syntactical correctness of the name.
     *
     * This invokes a newly constructed parser, i.e. is comparatively expensive.
     *
     * @param candidate Name to be checked
     * @throws org.harctoolbox.irp.InvalidNameException
     */
    public static void checkName(String candidate) throws InvalidNameException {
        if (!validName(candidate))
            throw new InvalidNameException(candidate);
    }

    private final String name;

    public Name(IrpParser.NameContext ctx) {
        super(ctx);
        name = ctx.getText();
    }

    public Name(String name) throws InvalidNameException {
        super(null);
        checkName(name);
        this.name = name;
    }

    @Override
    public PrimaryItem substituteConstantVariables(Map<String, Long> constantVariables) {
        return constantVariables.containsKey(name) ? new Number(constantVariables.get(name)) : this;
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
    public String toIrpString(int radix) {
        return name;
    }

    @Override
    public long toNumber(NameEngine nameEngine) throws NameUnassignedException {
        Expression expression = nameEngine.get(getName());
        return expression.toNumber(nameEngine);
    }

    @Override
    public long toNumber() throws NameUnassignedException {
        throw new NameUnassignedException(name);
    }

    @Override
    public Element toElement(Document document) {
        Element element = super.toElement(document);
        element.setTextContent(toString());
        return element;
    }

    @Override
    public double toFloat(GeneralSpec generalSpec, NameEngine nameEngine) throws NameUnassignedException {
        return toNumber(nameEngine);
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    @Override
    public int weight() {
        return WEIGHT;
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

    @Override
    public Long invert(long rhs, NameEngine nameEngine, long bitmask) {
        return rhs;
    }

    @Override
    public PrimaryItem leftHandSide() {
        return this;
    }
}
