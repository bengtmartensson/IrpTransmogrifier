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
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * This class corresponds to Chapter 9.
 * An expression is evaluated during execution time with the current name bindings.
 *
 */
public class Expression {

    //private static boolean debug;
    private IrpParser.ExpressionContext parseTree;
    private NameEngine nameEngine;

    public Expression(String irp) throws IrpSyntaxException {
        this(new ParserDriver(irp).expression());
    }

    public Expression(IrpParser.ExpressionContext parseTree) {
        this(parseTree, new NameEngine());
    }

    public Expression(IrpParser.ExpressionContext parseTree, NameEngine nameEngine) {
        this.parseTree = parseTree;
        this.nameEngine = nameEngine;
    }


    public long evaluate(Protocol env) {
        this.nameEngine = env.getNameEngine();
        return evaluate();
    }

    // FIXME: If this function encounters an unknown token (for example when extending the input grammar)
    // it will report that that name is not found in the name engine. Fix.
    public long evaluate() {
        return eval(parseTree.bare_expression().inclusive_or_expression());
    }

    private long eval(IrpParser.Inclusive_or_expressionContext ctx) {
        long result = 0L;
        for (IrpParser.Exclusive_or_expressionContext expr : ctx.exclusive_or_expression()) {
            result |= eval(expr);
        }
        return result;
    }

    private long eval(IrpParser.Exclusive_or_expressionContext ctx) {
        long result = 0L;
        for (IrpParser.And_expressionContext expr : ctx.and_expression()) {
            result ^= eval(expr);
        }
        return result;
    }

    private long eval(IrpParser.And_expressionContext ctx) {
        long result = -1L;
        for (IrpParser.Additive_expressionContext expr : ctx.additive_expression())
            result &= eval(expr);
        return result;
    }

    private long eval(IrpParser.Additive_expressionContext ctx) {
        long result = 0;
        for (ParseTree x : ctx.children)
            if (x instanceof IrpParser.Multiplicative_expressionContext)
                eval((IrpParser.Multiplicative_expressionContext) x);
            ; // TODO
        return result;
    }

    private long eval(IrpParser.Multiplicative_expressionContext ctx) {
        long result = 0L;
        for (ParseTree x : ctx.children)
            if (x instanceof IrpParser.Exponential_expressionContext)
                eval((IrpParser.Exponential_expressionContext) x);
        return result;
    }

    private long eval(IrpParser.Exponential_expressionContext ctx) {
        List<IrpParser.Unary_expressionContext> children = ctx.unary_expression();
        long result = eval(children.get(children.size() - 1));
        for (int i = children.size() - 1; i <= 0; i++)
            result = IrpUtils.power(eval(children.get(i)), result);
        return result;
    }

    private long eval(IrpParser.Unary_expressionContext ctx) {
        return ctx instanceof IrpParser.Bitfield_expressionContext ? eval((IrpParser.Bitfield_expressionContext) ctx)
                : ctx instanceof IrpParser.Primary_item_expressionContext ? eval((IrpParser.Primary_item_expressionContext) ctx)
                : ctx instanceof IrpParser.Minus_bitfield_expressonContext ? eval((IrpParser.Minus_bitfield_expressonContext) ctx)
                : ctx instanceof IrpParser.Minus_primary_item_expressionContext ? eval((IrpParser.Minus_primary_item_expressionContext) ctx)
                : ctx instanceof IrpParser.Count_bitfield_expressionContext ? eval((IrpParser.Count_bitfield_expressionContext) ctx)
                : eval((IrpParser.Count_primary_item_expressionContext) ctx);
    }

    private long eval(IrpParser.Bitfield_expressionContext ctx) {
        return eval(ctx.bitfield());
    }

    private long eval(IrpParser.Primary_item_expressionContext ctx) {
        // TODO
        return eval(ctx.primary_item());
    }

    private long eval(IrpParser.Minus_bitfield_expressonContext ctx) {
        return -eval(ctx.bitfield());
    }

    private long eval(IrpParser.Minus_primary_item_expressionContext ctx) {
        // TODO
        return -eval(ctx.primary_item());
    }

    private long eval(IrpParser.Count_bitfield_expressionContext ctx) {
        return (long) Long.bitCount(eval(ctx.bitfield()));
    }

    private long eval(IrpParser.Count_primary_item_expressionContext ctx) {
        return (long) Long.bitCount(eval(ctx.primary_item()));
    }

    private long eval(IrpParser.Primary_itemContext ctx) {
        return ctx.number() != null ? eval(ctx.number()) : 0; // FIXME

    }

    private long eval(IrpParser.NumberContext ctx) {
        return Long.parseLong(ctx.INT().getText());
    }

    //name
    //    | DOLLAR_ID
//	| number
//	| expression

    private long eval(IrpParser.BitfieldContext ctx) {
        // TODO
        return 0;
    }

    /*

bitfield_expression
        | primary_item      # primary_item_expession
        | '-' bitfield      # minus_bitfield_expresson
        | '-' primary_item  # minus_primary_item_expression
        | '#' bitfield      # count_bitfield
        | '#' primary_item  # count_primary_item

        //public long expression(CommonTree tree, int level) throws UnassignedException, DomainViolationException {
        //nodeBegin(tree, level);
        //String type = tree.getText();
        long result =
                  type.equals("**") ? IrpUtils.power(expression((CommonTree) tree.getChild(0), level+1), expression((CommonTree) tree.getChild(1), level+1))
                : type.equals("%")  ? expression((CommonTree) tree.getChild(0), level+1) % expression((CommonTree) tree.getChild(1), level+1)
                : type.equals("/")  ? expression((CommonTree) tree.getChild(0), level+1) / expression((CommonTree) tree.getChild(1), level+1)
                : type.equals("*")  ? expression((CommonTree) tree.getChild(0), level+1) * expression((CommonTree) tree.getChild(1), level+1)
                : type.equals("-")  ? expression((CommonTree) tree.getChild(0), level+1) - expression((CommonTree) tree.getChild(1), level+1)
                : type.equals("+")  ? expression((CommonTree) tree.getChild(0), level+1) + expression((CommonTree) tree.getChild(1), level+1)
                : type.equals("&")  ? expression((CommonTree) tree.getChild(0), level+1) & expression((CommonTree) tree.getChild(1), level+1)
                : type.equals("^")  ? expression((CommonTree) tree.getChild(0), level+1) ^ expression((CommonTree) tree.getChild(1), level+1)
                : type.equals("|")  ? expression((CommonTree) tree.getChild(0), level+1) | expression((CommonTree) tree.getChild(1), level+1)
                : type.equals("UMINUS")   ? -expression((CommonTree) tree.getChild(0), level+1)
                : type.equals("BITCOUNT") ? (long) Long.bitCount(expression((CommonTree) tree.getChild(0), level+1))
                : type.equals("BITFIELD") ? bitfield(tree, level+1, true).toLong()
                : type.equals("INFINITE_BITFIELD") ? bitfield(tree, level+1, true).toLong()
                : type.matches("[0-9]+")  ? Long.parseLong(type)
                : env.evaluateName(type);
        nodeEnd(tree, level, result);
        return result;
    }

    public static long evaluate(Protocol env, String irp) throws IrpSyntaxException {
        Expression expression = new Expression(irp);
        return expression.evaluate(env);
    }

    private static void usage(int code) {
        System.out.println("Usage:");
        System.out.println("\tExpression [-d]? <expression> [<name>=<value>|{<name>=<expression>}]*");
        System.exit(code);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //if (args.length == 0)
        //    usage(IrpUtils.exitUsageError);
        int arg_i = 0;
        //if (args[0].equals("-d")) {
        //    debug = true;
        //    arg_i++;
        //}
        Protocol prot = new Protocol();
        String expression = null;
        //try {
            expression = args[arg_i].trim();
            //prot.assign(args, arg_i+1);
        //} catch (IncompatibleArgumentException ex) {
            //System.err.println(ex.getMessage());
            //usage(IrpUtils.exitUsageError);
        //} catch (ArrayIndexOutOfBoundsException ex) {
            //usage(IrpUtils.exitUsageError);
        //}
        if (expression != null && ((expression.charAt(0) != '(') || (expression.charAt(expression.length()-1) != ')')))
            expression = '(' + expression + ')';
//        try {
//            System.out.println(evaluate(prot, expression));
//        } catch (UnassignedException | DomainViolationException ex) {
//            System.err.println(ex.getMessage());
//        }
    }

    /**
     * @return the parseTree
     */
    public IrpParser.ExpressionContext getParseTree() {
        return parseTree;
    }

    /**
     * @return the nameEngine
     */
    public NameEngine getNameEngine() {
        return nameEngine;
    }

    /**
     * @param nameEngine the nameEngine to set
     */
    public void setNameEngine(NameEngine nameEngine) {
        this.nameEngine = nameEngine;
    }
}
