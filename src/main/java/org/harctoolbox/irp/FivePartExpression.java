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
import org.antlr.v4.runtime.tree.ParseTree;

final class FivePartExpression extends Expression {

    public static FivePartExpression newExpression(IrpParser.ExpressionContext ctx) {
        return newExpression(ctx, ctx.expression(0), ctx.expression(1), ctx.expression(2));
    }

    public static FivePartExpression newExpression(ParseTree ctx, IrpParser.ExpressionContext cond, IrpParser.ExpressionContext trueExpression, IrpParser.ExpressionContext falseExpression) {
        return new FivePartExpression(ctx, cond, trueExpression, falseExpression);
    }

    private final Expression conditional;
    private final Expression trueExp;
    private final Expression falseExp;

    private FivePartExpression(ParseTree ctx, Expression cond, Expression trueExpression, Expression falseExpression) {
        super(ctx);
        this.conditional = cond;
        this.trueExp = trueExpression;
        this.falseExp = falseExpression;
    }

    private FivePartExpression(ParseTree ctx, IrpParser.ExpressionContext cond, IrpParser.ExpressionContext trueExpression, IrpParser.ExpressionContext falseExpression) {
        this(ctx, Expression.newExpression(cond), Expression.newExpression(trueExpression), Expression.newExpression(falseExpression));
    }

    private FivePartExpression(ParseTree parseTree, PrimaryItem cond, PrimaryItem trueExpression, PrimaryItem falseExpression) {
        this(parseTree, PrimaryItemExpression.newExpression(cond),
                PrimaryItemExpression.newExpression(trueExpression),
                PrimaryItemExpression.newExpression(falseExpression));
    }

    @Override
    public PrimaryItem substituteConstantVariables(Map<String, Long> constantVariables) {
        return new FivePartExpression(getParseTree(), conditional.substituteConstantVariables(constantVariables),
                trueExp.substituteConstantVariables(constantVariables), falseExp.substituteConstantVariables(constantVariables));
    }

    @Override
    public String toIrpString(int radix) {
         return "("
                        + conditional.toIrpString(radix) + "?"
                        + trueExp.toIrpString(radix) + ":"
                        + falseExp.toIrpString(radix)
                        + ")";
    }

    @Override
    public Map<String, Object> propertiesMap(boolean eval, GeneralSpec generalSpec, NameEngine nameEngine) {
        Map<String, Object> map = super.propertiesMap(4);
        map.put("kind", "ConditionalOp");
        map.put("arg1", conditional.propertiesMap(true, generalSpec, nameEngine));
        map.put("arg2", trueExp.propertiesMap(true, generalSpec, nameEngine));
        map.put("arg3", falseExp.propertiesMap(true, generalSpec, nameEngine));
        return map;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.conditional);
        hash = 41 * hash + Objects.hashCode(this.trueExp);
        hash = 41 * hash + Objects.hashCode(this.falseExp);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final FivePartExpression other = (FivePartExpression) obj;
        if (!Objects.equals(this.conditional, other.conditional))
            return false;
        if (!Objects.equals(this.trueExp, other.trueExp))
            return false;
        return Objects.equals(this.falseExp, other.falseExp);
    }

    @Override
    public int weight() {
        return 2 + conditional.weight() + trueExp.weight() + falseExp.weight();
    }

    @Override
    public long toLong(NameEngine nameEngine) throws NameUnassignedException {
        long ctrl = conditional.toLong(nameEngine);
        return ctrl != 0L ? trueExp.toLong(nameEngine) : falseExp.toLong(nameEngine);
    }

    @Override
    public boolean constant(NameEngine nameEngine) {
        return trueExp.constant(nameEngine) && falseExp.constant(nameEngine);
    }
}
