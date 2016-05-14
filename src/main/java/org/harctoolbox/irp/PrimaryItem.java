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

/**
 *
 */
public abstract class PrimaryItem implements Numerical {
    //private Numerical data; // Number, Name, or Expression

    public static PrimaryItem newPrimaryItem(IrpParser.Primary_itemContext ctx) throws IrpSyntaxException {
        return (ctx instanceof IrpParser.Name_asitemContext)
                ? new Name(((IrpParser.Name_asitemContext) ctx).name())
                : (ctx instanceof IrpParser.DOLLAR_ID_asitemContext)
                ? new Name(((IrpParser.DOLLAR_ID_asitemContext) ctx).getText())
                : (ctx instanceof IrpParser.Number_asitemContext)
                ? new Number(((IrpParser.Number_asitemContext) ctx).number())
                : new Expression(((IrpParser.Expression_asitemContext) ctx).expression());
    }

    protected PrimaryItem() {
    }

    public static PrimaryItem newPrimaryItem(long n) {
        return new Number(n);
    }

    public static PrimaryItem newPrimaryItem(String name) throws IrpSyntaxException {
        return new Name(name);
    }

//    @Override
//    public long toNumber(NameEngine nameEngine) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
//        return data.toNumber(nameEngine);
//    }

//    @Override
//    public String toInfixCode() throws IrpSyntaxException {
//        return data.toInfixCode();
//    }
}
