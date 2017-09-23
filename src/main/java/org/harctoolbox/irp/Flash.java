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

/**
 * This class implements Flash as per Chapter 3.
 *
 */
public final class Flash extends Duration {

    public Flash(String str) {
        this(new ParserDriver(str));
    }

    public Flash(ParserDriver parserDriver) {
        this(parserDriver.getParser().flash());
    }

    public Flash(IrpParser.FlashContext ctx) {
        super(ctx.name_or_number(), ctx.getChildCount() > 1 ? ctx.getChild(1).getText() : null);
    }

    public Flash(double d, String unit) {
        super(d, unit);
    }

    public Flash(double d) {
        super(d);
    }

    public Flash(NameOrNumber non, String unit) {
        super(non, unit);
    }

    @Override
    public IrStreamItem substituteConstantVariables(Map<String, Long> constantVariables) {
        return new Flash(nameOrNumber.substituteConstantVariables(constantVariables), unit);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Flash))
            return false;

        return super.equals(obj);
    }

    @Override
    public double evaluateWithSign(GeneralSpec generalSpec, NameEngine nameEngine, double elapsed) throws NameUnassignedException, IrpInvalidArgumentException {
        return evaluate(generalSpec, nameEngine, elapsed);
    }

    @Override
    protected boolean isOn() {
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        return hash + 31*super.hashCode();
    }

    @Override
    protected Flash evaluatedDuration(GeneralSpec generalSpec, NameEngine nameEngine) throws NameUnassignedException, IrpInvalidArgumentException {
        return new Flash(evaluate(generalSpec, nameEngine));
    }
}
