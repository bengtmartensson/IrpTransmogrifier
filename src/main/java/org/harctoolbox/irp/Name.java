/*
Copyright (C) 2014 Bengt Martensson.

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

import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.harctoolbox.ircore.IncompatibleArgumentException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 */
public class Name extends PrimaryItem {
    String name;

    public Name(IrpParser.NameContext ctx) {
        name = ctx.getText();
    }

    public Name(String name) {
        //parse(name); // just to check validity
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Check the syntactical correctness of the name.
     *
     * This invokes a newly constructed parser, i.e. is comparatively expensive.
     *
     * @param name Name to be checked
     * @return true iff the name is syntactically valid.
     * @throws org.harctoolbox.irp.IrpSyntaxException
     */
    //  Alternatively, could check agains a regexp. But this keeps the grammar in one place.
    public static String parse(String name) throws IrpSyntaxException {
        try {
            ParserDriver parserDriver = new ParserDriver(name);
            IrpParser.NameContext nam = parserDriver.getParser().name();
            return nam.getText();
        } catch (ParseCancellationException ex) {
            throw new IrpSyntaxException("Invalid name: " + name);
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
        } catch (IrpSyntaxException ex) {
            return false;
        }
    }

    public static long toNumber(IrpParser.NameContext ctx, NameEngine nameEngine) throws IrpSyntaxException, UnassignedException, IncompatibleArgumentException {
        Expression exp = nameEngine.get(toString(ctx));
        return exp.toNumber(nameEngine);
    }

    public static String toString(IrpParser.NameContext ctx) {
        return ctx.getText();
    }

    public String toInfixCode() {
        return name;
    }

    @Override
    public long toNumber(NameEngine nameEngine) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
        Expression expression = nameEngine.get(name);
        return expression.toNumber(nameEngine);
    }

    @Override
    public Element toElement(Document document) {
        Element element = document.createElement("name");
        element.setAttribute("name", toString());
        return element;
    }
}
