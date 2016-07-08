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

import org.harctoolbox.ircore.IncompatibleArgumentException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class implements Gap as per Chapter 3.
 *
 */
public class Gap extends Duration {

    Gap(IrpParser.Gap_durationContext ctx) throws IrpSyntaxException {
        super(ctx.name_or_number(), ctx.getChildCount() > 2 ? ctx.getChild(2).getText() : null);
    }

    @Override
    public double evaluateWithSign(NameEngine nameEngine, GeneralSpec generalSpec, double elapsed)
            throws IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException {
        return -evaluate(nameEngine, generalSpec, elapsed);
    }

    @Override
    public Element toElement(Document document) {
        return toElement(document, "gap");
    }

    @Override
    boolean interleavingOk() {
        return true;
    }
}
