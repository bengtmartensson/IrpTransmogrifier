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

/**
 * This class implements Gap as per Chapter 3.
 *
 */
public final class Gap extends Duration {

    public Gap(String str) {
        this((new ParserDriver(str)).getParser().gap());
    }

    Gap(IrpParser.GapContext ctx) {
        super(ctx.name_or_number(), ctx.getChildCount() > 2 ? ctx.getChild(2).getText() : null);
    }

    public Gap(double us) {
        super(us);
    }

    public Gap(double d, String unit) {
        super(d, unit);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Gap))
            return false;

        return super.equals(obj);
    }

    @Override
    public double evaluateWithSign(GeneralSpec generalSpec, NameEngine nameEngine, double elapsed) throws IrpInvalidArgumentException, NameUnassignedException {
        return -evaluate(generalSpec, nameEngine, elapsed);
    }

    @Override
    public String toIrpString(int radix) {
        return "-" + super.toIrpString(radix);
    }

    @Override
    protected boolean isOn() {
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash + 31*super.hashCode();
    }
}

