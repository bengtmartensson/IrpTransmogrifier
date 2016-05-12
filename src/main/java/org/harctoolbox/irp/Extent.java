/*
Copyright (C) 2011, 2016 Bengt Martensson.

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

/**
 * This class implements Extents as per Chapter 4.
 *
 */
public class Extent extends Duration {

    Extent(IrpParser.ExtentContext ctx) throws IrpSyntaxException {
        super(ctx.name_or_number(), ctx.getChildCount() > 2 ? ctx.getChild(2).getText() : null);
    }

    @Override
    public double evaluate(double elapsed, NameEngine nameEngine, GeneralSpec generalSpec) throws IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException {
        double time = super.evaluate(0f, nameEngine, generalSpec) - elapsed;
        if (time < 0)
            throw new IncompatibleArgumentException("Argument of extent smaller than actual duration.");
        return time;
    }

    @Override
    public double evaluateWithSign(double elapsed, NameEngine nameEngine, GeneralSpec generalSpec) throws IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException {
        return -evaluate(elapsed, nameEngine, generalSpec);
    }
}
