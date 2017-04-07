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

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.antlr.v4.gui.TreeViewer;
import org.antlr.v4.runtime.tree.ParseTree;
import org.harctoolbox.ircore.ThisCannotHappenException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class corresponds to Chapter 9.
 * An expression is evaluated during execution time with the current name bindings.
 */
public abstract class Expression extends PrimaryItem {

    private static final Logger logger = Logger.getLogger(Expression.class.getName());

    public static Expression newExpression(long value) {
        return new NumberExpression(value);
    }

    /**
     * Construct an Expression by parsing the argument.
     * @param str
     * @return
     * @throws org.harctoolbox.irp.IrpParseException
     */
    public static Expression newExpressionEOF(String str) throws IrpParseException {
        ParserDriver parserDriver = new ParserDriver(str);
        Expression expression = newExpression(parserDriver);
        String matched = expression.getParseTree().getText();
        if (matched.length() < str.replaceAll("\\s+", "").length())
            throw new IrpParseException(str, "Did not match all input, just \"" + matched + "\".");
        return expression;
    }

    public static Expression newExpression(String str) {
        try {
            // First try as number, for efficiency
            long number = Long.parseLong(str);
            return new NumberExpression(number);
        } catch (NumberFormatException ex) {
            return newExpression(new ParserDriver(str));
            //Expression exp = newExpression(parseDriver);
//            int last = exp.parseTree.getStop().getStopIndex();
//            if (last != str.length() - 1)
//                logger.log(Level.WARNING, "Did not match all input, just \"{0}\"", str.substring(0, last + 1));
            //return exp;
        }
    }

    public static Expression newExpression(ParserDriver parserDriver) {
        Expression expression = newExpression(parserDriver.getParser().expression());
        expression.parserDriver = parserDriver;
        return expression;
    }

    static Expression newExpression(IrpParser.Para_expressionContext para_expressionContext) {
        return newExpression(para_expressionContext.expression());
    }

    static Expression newExpression(ParseTree ctx, IrpParser.Para_expressionContext para_expressionContext) {
        return newExpression(ctx, para_expressionContext.expression());
    }

    public static Expression newExpression(IrpParser.ExpressionContext ctx) {
        return newExpression(ctx, ctx);
    }

    public static Expression newExpression(ParseTree original, IrpParser.ExpressionContext ctx) {
        List<ParseTree> children = ctx.children;
        switch (children.size()) {
            case 1:
                return OnePartExpression.newExpression(original, ctx.getChild(0));
            case 2:
                return TwoPartExpression.newExpression(original, ctx.getChild(0), ctx.getChild(1));
            case 3:
                return ThreePartExpression.newExpression(original, ctx.getChild(0), ctx.getChild(1), ctx.getChild(2));
            case 5:
                return FivePartExpression.newExpression(original, ctx.getChild(0), ctx.getChild(2), ctx.getChild(4));
            default:
                throw new ThisCannotHappenException("Unknown expression type");
        }
    }

    protected static long cBoolean(boolean x) {
        return x ? 1L : 0L;
    }

    private ParserDriver parserDriver = null;

    protected Expression(ParseTree ctx) {
        super(ctx);
    }

    private Expression(ParseTree ctx, ParserDriver parserDriver, Expression old) {
        super(ctx);
        this.parserDriver = parserDriver;

    }

    public String toStringTree() {
        return parserDriver != null ? toStringTree(parserDriver) : null;
    }

    public TreeViewer toTreeViewer() {
        return toTreeViewer(parserDriver);
    }

    @Override
    public long toNumber() throws NameUnassignedException {
        return toNumber(NameEngine.empty);
    }

    @Override
    public Map<String, Object> propertiesMap(int noProperites) {
        return IrpUtils.propertiesMap(noProperites, "Expression");
    }

    @Override
    public Element toElement(Document document) {
        return document.createElement("Expression");
    }
}
