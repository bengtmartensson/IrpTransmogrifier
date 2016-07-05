/*
Copyright (C) 2016 Bengt Martensson.

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

import org.antlr.v4.runtime.tree.ParseTree;
import org.harctoolbox.ircore.IncompatibleArgumentException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 */
public class NameOrNumber implements Floatable,XmlExport {
    private Object thing;

    public NameOrNumber(String str) throws IrpSyntaxException {
        this(new ParserDriver(str).getParser().name_or_number());
    }

    public NameOrNumber(IrpParser.Name_or_numberContext ctx) throws IrpSyntaxException {
        ParseTree child = ctx.getChild(0);
        thing = (child instanceof IrpParser.NameContext)
                ? new Name((IrpParser.NameContext) child)
                : new NumberWithDecimals((IrpParser.Number_with_decimalsContext) child);
    }

//    @Override
//    public long toNumber(NameEngine nameEngine) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
//        return thing.toNumber(nameEngine);
//    }
//
//    @Override
//    public String toInfixCode() throws IrpSyntaxException {
//        return thing.toInfixCode();
//    }

    @Override
    public String toString() {
        return thing.toString();
    }

    @Override
    public double toFloat(NameEngine nameEngine, GeneralSpec generalSpec)
            throws ArithmeticException, IncompatibleArgumentException, UnassignedException, IrpSyntaxException {
        return (thing instanceof Name) ? ((Name) thing).toNumber(nameEngine) : ((NumberWithDecimals) thing).toFloat();
    }

    Object toNumber(NameEngine nameEngine) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Element toElement(Document document) {
        Element element = document.createElement("name_or_number");
        // TODO: name
        element.appendChild(((NumberWithDecimals) thing).toElement(document));
        return element;
    }
}
