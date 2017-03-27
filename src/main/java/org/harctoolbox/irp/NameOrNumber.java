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

import java.util.Objects;
import org.antlr.v4.runtime.tree.ParseTree;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class NameOrNumber extends IrpObject implements Floatable {
    private static final int WEIGHT = 1;

    private Floatable thing;

    public NameOrNumber(String str) {
        this(new ParserDriver(str).getParser().name_or_number());
    }

    public NameOrNumber(IrpParser.Name_or_numberContext ctx) {
        super(ctx);
        ParseTree child = ctx.getChild(0);
        if (child instanceof IrpParser.NameContext)
            thing = new Name(ctx.name());
        else
            thing = new NumberWithDecimals(ctx.number_with_decimals());
    }

    public NameOrNumber(double x) {
        super(null);
        thing = new NumberWithDecimals(x);
    }

//    @Override
//    public String toString() {
//        return toIrpString();
//    }

    @Override
    public double toFloat(GeneralSpec generalSpec, NameEngine nameEngine) throws UnassignedException, IrpSemanticException {
        return thing.toFloat(generalSpec, nameEngine);
    }

    @Override
    public Element toElement(Document document) throws IrpSemanticException {
        Element element = super.toElement(document);
        element.appendChild(thing.toElement(document));
        return element;
    }

    @Override
    public String toIrpString(int radix) {
        return thing.toIrpString(radix);
    }

    double toRawNumber() throws IrpSemanticException {
        if (!(thing instanceof NumberWithDecimals))
            throw new IrpSemanticException("Not a number");
        return ((NumberWithDecimals) thing).toFloat();
    }

    @Override
    public int weight() {
        return WEIGHT;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NameOrNumber))
            return false;

        NameOrNumber other = (NameOrNumber) obj;
        return thing.equals(other.thing);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.thing);
        return hash;
    }
}
