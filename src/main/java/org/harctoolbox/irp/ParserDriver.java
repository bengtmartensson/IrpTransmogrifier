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

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

/**
 * This class encapsulates the IrpParser.
 */
public class ParserDriver {

    private final IrpLexer lexer;
    private final CommonTokenStream tokens;
    private IrpParser parser;

    public ParserDriver(String irpString) {
        lexer = new IrpLexer(new ANTLRInputStream(irpString));
        tokens = new CommonTokenStream(lexer);
        parser = new IrpParser(tokens);
        parser.setErrorHandler(new ErrorStrategy());
    }

//    public static void reset() {
//        RepeatMarker.reset();
//        Extent.reset();
//    }

    public String toStringTree() {
        IrpParser.ProtocolContext protocol = parser.protocol();
        return protocol != null ? protocol.toStringTree(parser) : null;
    }

    public IrpParser getParser() {
        return parser;
    }
/*
    // TODO: having both getParser() and all these is silly...
    public IrpParser.DurationContext duration() {
        return parser.duration();
    }

    public IrpParser.ProtocolContext protocol() throws IrpSyntaxException {
        try {
            return parser.protocol();
        } catch (ParseCancellationException ex) {
            throw new IrpSyntaxException(ex);
        }
    }

    public IrpParser.GeneralspecContext generalspec() throws IrpSyntaxException {
        try {
            return parser.generalspec();
        } catch (ParseCancellationException ex) {
            throw new IrpSyntaxException(ex);
        }
    }

    public IrpParser.Parameter_specsContext parameterSpecs() throws IrpSyntaxException {
        try {
            return parser.parameter_specs();
        } catch (ParseCancellationException ex) {
            throw new IrpSyntaxException(ex);
        }
    }

    public IrpParser.ExpressionContext expression() throws IrpSyntaxException {
        try {
            return parser.expression();
        } catch (ParseCancellationException ex) {
            throw new IrpSyntaxException(ex);
        }
    }

    public IrpParser.Bare_expressionContext bare_expression() throws IrpSyntaxException {
        try {
            return parser.bare_expression();
        } catch (ParseCancellationException ex) {
            throw new IrpSyntaxException(ex);
        }
    }

    public IrpParser.BitfieldContext bitfield() throws IrpSyntaxException {
        try {
            return parser.bitfield();
        } catch (ParseCancellationException ex) {
            throw new IrpSyntaxException(ex);
        }
    }

    public IrpParser.DefinitionsContext definitions() throws IrpSyntaxException {
        try {
            return parser.definitions();
        } catch (ParseCancellationException ex) {
            throw new IrpSyntaxException(ex);
        }
    }
/*
    public static double parse(IrpParser.Name_or_numberContext ctx, NameEngine nameEngine) throws IrpSyntaxException {
        ParseTree child = ctx.getChild(0);
        return child instanceof IrpParser.NameContext ? parse((IrpParser.NameContext) child, nameEngine)
                : parse((IrpParser.Number_with_decimalsContext) child);
    }

    public static double parse(IrpParser.NameContext ctx, NameEngine nameEngine) throws IrpSyntaxException {
        IrpParser.Bare_expressionContext tree = nameEngine.get(parse(ctx));
        Expression exp = new Expression(tree, nameEngine);
        return exp.evaluate();
    }

    public static String parse(IrpParser.NameContext ctx) {
        return ctx.getText();
    }

    public static double parse(IrpParser.Number_with_decimalsContext ctx) {
        return (ctx instanceof IrpParser.IntegerAsFloatContext)
                ? parseINT(((IrpParser.IntegerAsFloatContext) ctx).INT())
                : parse(((IrpParser.FloatContext) ctx).float_number());
    }

    public static double parse(IrpParser.Float_numberContext ctx) {
        return (ctx instanceof IrpParser.DotIntContext)
                ? parse((IrpParser.DotIntContext) ctx)
                : parse((IrpParser.IntDotIntContext) ctx);
    }

    public static double parse(IrpParser.DotIntContext ctx) {
        return parseFloat(ctx.INT());
    }

    public static double parse(IrpParser.IntDotIntContext ctx) {
        return parseFloat(ctx.INT(0), ctx.INT(1));
    }

    private static long parseINT(TerminalNode n) {
        return Long.parseLong(n.getText());
    }

    private static double parseFloat(TerminalNode integ, TerminalNode matissa) {
        return Double.parseDouble(integ.getText() + "." + matissa.getText());
    }

    private static double parseFloat(TerminalNode matissa) {
        return Double.parseDouble("0." + matissa.getText());
    }

    /*public static void main(String[] args) {
        System.out.println(validName(" ksdjfk "));
        System.out.println(validName(" 4ksdjfk "));
        System.out.println(validName(" _4ksdjfk "));
        System.out.println(validName("msb"));
        System.out.println(validName("May the force be with you"));
    }*/
}
