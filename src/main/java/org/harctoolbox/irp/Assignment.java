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

import java.util.List;
import org.harctoolbox.ircore.IncompatibleArgumentException;

/**
 *
 */
public class Assignment extends IrStreamItem implements Numerical {
    private Name name;
    private Expression value;

    public Assignment(String str) {
        this((new ParserDriver(str)).getParser().assignment());
    }

    public Assignment(IrpParser.AssignmentContext assignment) {
        this(assignment.name(), assignment.bare_expression());
    }

    public Assignment(IrpParser.NameContext name, IrpParser.Bare_expressionContext be) {
        this(new Name(name), new Expression(be));
    }

    public Assignment(Name name, Expression expression) {
        this.name = name;
        this.value = expression;
    }

    public static long parse(String str, NameEngine nameEngine) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
        Assignment assignment = new Assignment(str);
        return assignment.toNumber(nameEngine);
    }

    @Override
    public boolean isEmpty(NameEngine nameEngine) throws IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<IrStreamItem> evaluate(BitSpec bitSpec) throws UnassignedException, IncompatibleArgumentException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long toNumber(NameEngine nameEngine) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
        return value.toNumber(nameEngine);
    }

    public String getName() {
        return name.toString();
    }

    @Override
    public String toString() {
        return name + "=" + value;
    }
}
