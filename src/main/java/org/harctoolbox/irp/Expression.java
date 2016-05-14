/*
Copyright (C) 2011,2012, 2015 Bengt Martensson.

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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.antlr.v4.runtime.tree.ParseTree;
import org.harctoolbox.ircore.IncompatibleArgumentException;

/**
 * This class corresponds to Chapter 9.
 * An expression is evaluated during execution time with the current name bindings.
 */
public class Expression extends PrimaryItem {

    //private static boolean debug;
    private IrpParser.Bare_expressionContext parseTree;
    private static final String expfunctionName = "powl";

    /**
     * Construct an Expression from the number supplied as argument.
     * @param value
     * @throws IrpSyntaxException
     */
    public Expression(long value) throws IrpSyntaxException {
        this(new ParserDriver(Long.toString(value)));
    }

    /**
     * Construct an Expression by parsing the argument.
     * @param str
     * @throws IrpSyntaxException
     */
    public Expression(String str) throws IrpSyntaxException {
        this(new ParserDriver(str));
    }

    Expression(ParserDriver parserDriver) throws IrpSyntaxException {
        this(parserDriver.getParser().bare_expression());
    }

    Expression(IrpParser.ExpressionContext ctx) {
        this(ctx.bare_expression());
    }

    Expression(IrpParser.Expression_asitemContext ctx) {
        this(ctx.expression());
    }

    Expression(IrpParser.Bare_expressionContext ctx) {
        this.parseTree = ctx;
    }

    @Override
    public long toNumber(NameEngine nameEngine) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
        return toNumber(parseTree, nameEngine);
    }

//    @Override
//    public String toInfixCode() throws IrpSyntaxException {
//        return toInfixCode(parseTree);
//    }

    private long toNumber(IrpParser.Bare_expressionContext ctx, NameEngine nameEngine) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
        return toNumber(ctx.inclusive_or_expression(), nameEngine);
    }

//    private String toInfixCode(IrpParser.Bare_expressionContext ctx) throws IrpSyntaxException {
//        return toInfixCode(ctx.inclusive_or_expression());
//    }

    private long toNumber(IrpParser.Inclusive_or_expressionContext ctx, NameEngine nameEngine) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
        long result = 0L;
        for (IrpParser.Exclusive_or_expressionContext expr : ctx.exclusive_or_expression())
            result |= toNumber(expr, nameEngine);

        return result;
    }

//    private String toInfixCode(IrpParser.Inclusive_or_expressionContext ctx) throws IrpSyntaxException {
//        StringBuilder sb = new StringBuilder();
//        for (IrpParser.Exclusive_or_expressionContext expr : ctx.exclusive_or_expression()) {
//            if (sb.length() > 0)
//                sb.append("|");
//            sb.append(toInfixCode(expr));
//        }
//        return sb.toString();
//    }

    private long toNumber(IrpParser.Exclusive_or_expressionContext ctx, NameEngine nameEngine) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
        long result = 0L;
        for (IrpParser.And_expressionContext expr : ctx.and_expression())
            result ^= toNumber(expr, nameEngine);

        return result;
    }

//    private String toInfixCode(IrpParser.Exclusive_or_expressionContext ctx) throws IrpSyntaxException {
//        StringBuilder sb = new StringBuilder();
//        for (IrpParser.And_expressionContext expr : ctx.and_expression()) {
//            if (sb.length() > 0)
//                sb.append("^");
//            sb.append(toInfixCode(expr));
//        }
//        return sb.toString();
//    }

    private long toNumber(IrpParser.And_expressionContext ctx, NameEngine nameEngine) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
        long result = -1L;
        for (IrpParser.Shift_expressionContext expr : ctx.shift_expression()) {
            result &= toNumber(expr, nameEngine);
        }
        return result;
    }

//    private String toInfixCode(IrpParser.And_expressionContext ctx) throws IrpSyntaxException {
//        StringBuilder sb = new StringBuilder();
//        for (IrpParser.Shift_expressionContext expr : ctx.shift_expression()) {
//            if (sb.length() > 0)
//                sb.append("^");
//            sb.append(toInfixCode(expr));
//        }
//        return sb.toString();
//    }

    private long toNumber(IrpParser.Shift_expressionContext ctx, NameEngine nameEngine) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
        long result = toNumber(ctx.additive_expression(0), nameEngine);
        for (int i = 1; i < ctx.children.size(); i++) {
            ParseTree x = ctx.children.get(i);
            if (x instanceof IrpParser.Additive_expressionContext) {
                long op = toNumber((IrpParser.Additive_expressionContext) x, nameEngine);
                result = (ctx.children.get(i - 1).getText().charAt(0) == '<')
                        ? result << op : result >> op;
            }
        }
        return result;
    }

//    private String toInfixCode(IrpParser.Shift_expressionContext ctx) throws IrpSyntaxException {
//        StringBuilder sb = new StringBuilder();
//        for (IrpParser.Additive_expressionContext expr : ctx.additive_expression()) {
//            if (sb.length() > 0)
//                sb.append("^");
//            sb.append(toInfixCode(expr));
//        }
//        return sb.toString();
//    }

    private long toNumber(IrpParser.Additive_expressionContext ctx, NameEngine nameEngine) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
        long result = 0L;
        for (int i = 0; i < ctx.children.size(); i++) {
            ParseTree x = ctx.children.get(i);
            if (x instanceof IrpParser.Multiplicative_expressionContext) {
                long op = toNumber((IrpParser.Multiplicative_expressionContext) x, nameEngine);
                if (i == 0 || ctx.children.get(i - 1).getText().charAt(0) == '+')
                    result += op;
                else
                    result -= op;
            }
        }
        return result;
    }

//    private String toInfixCode(IrpParser.Additive_expressionContext ctx) throws IrpSyntaxException {
//        StringBuilder sb = new StringBuilder();
//        for (ParseTree child : ctx.children) {
//            if (child instanceof IrpParser.Multiplicative_expressionContext)
//                sb.append(toInfixCode((IrpParser.Multiplicative_expressionContext) child));
//            else
//                sb.append(child.getText());
//        }
//        return sb.toString();
//    }

    private long toNumber(IrpParser.Multiplicative_expressionContext ctx, NameEngine nameEngine) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
        long result = 1L;
        for (int i = 0; i < ctx.children.size(); i++) {
            ParseTree x = ctx.children.get(i);
            if (x instanceof IrpParser.Exponential_expressionContext) {
                long op = toNumber((IrpParser.Exponential_expressionContext) x, nameEngine);
                if (i == 0 || ctx.children.get(i - 1).getText().charAt(0) == '*')
                    result *= op;
                else
                    result /= op;
            }
        }
        return result;
    }

//    private String toInfixCode(IrpParser.Multiplicative_expressionContext ctx) throws IrpSyntaxException {
//        StringBuilder sb = new StringBuilder();
//        for (ParseTree child : ctx.children) {
//            if (child instanceof IrpParser.Exponential_expressionContext)
//                sb.append(toInfixCode((IrpParser.Exponential_expressionContext) child));
//            else
//                sb.append(child.getText());
//        }
//        return sb.toString();
//    }

    // Note: exponentiation is right-associative, so we evaluate the arguments in descending  order
    private long toNumber(IrpParser.Exponential_expressionContext ctx, NameEngine nameEngine) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
        List<IrpParser.Unary_expressionContext> children = ctx.unary_expression();
        //long result = toNumber(children.get(children.size() - 1), nameEngine);
        UnaryExpression unaryExpession = new UnaryExpression(children.get(children.size()-1));
        long result = unaryExpession.toNumber(nameEngine);
        for (int i = children.size() - 2; i >= 0; i++) {
            unaryExpession = new UnaryExpression(children.get(i));
            result = IrpUtils.power(unaryExpession.toNumber(nameEngine), result);
        }
        return result;
    }

//    private String toInfixCode(IrpParser.Exponential_expressionContext ctx) throws IrpSyntaxException {
//        StringBuilder sb = new StringBuilder();
//        List<IrpParser.Unary_expressionContext> children = ctx.unary_expression();
//        //long result = toNumber(children.get(children.size() - 1), nameEngine);
//        for (int i = children.size() - 1; i >= 0; i++) {
//            IrpParser.Unary_expressionContext child = children.get(i);
//            if (i < children.size() - 1) {
//                sb.append(")");
//                sb.insert(0, ",");
//            }
//            sb.insert(0, (new UnaryExpression(child)).toInfixCode());
//            if (i < children.size() - 1)
//                sb.insert(0, expfunctionName + "(");
//        }
//        return sb.toString();
//    }

    /**
     * @return the parseTree
     */
    public IrpParser.Bare_expressionContext getParseTree() {
        return parseTree;
    }

    private static void usage(int code) {
        System.out.println("Usage:");
        System.out.println("\tExpression [-d]? <expression> [<name>=<value>|{<name>=<expression>}]*");
        System.exit(code);
    }

    /**
     * Evaluate the argument supplied on the command line, possibly with a name assignment.
     * @param args: First argument should be the expression to be evaliated, second argument
     * a name engine initialization string.
     */
    public static void main(String[] args) {
        if (args.length == 0)
            usage(IrpUtils.exitUsageError);
        int arg_i = 0;
        //if (args[0].equals("-d")) {
        //    debug = true;
        //    arg_i++;
        //}
        String str = args[arg_i++];
        try {
            Expression expression = new Expression(str);
            NameEngine nameEngine = arg_i >= args.length - 1 ? new NameEngine(args[arg_i]) : new NameEngine();
            long result = expression.toNumber(nameEngine);
            System.out.println(result);
        } catch (IrpSyntaxException | UnassignedException | IncompatibleArgumentException ex) {
            Logger.getLogger(Expression.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
