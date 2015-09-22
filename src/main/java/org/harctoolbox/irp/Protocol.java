/*
Copyright (C) 2015 Bengt Martensson.

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
import org.antlr.v4.runtime.tree.ParseTree;

/**
 *
 */
public class Protocol {

    public static void main(String[] args) {
        String irpString = //"{38.4k,564}<1,-1|1,-3>(16,-8,D:8,S:8,F:8,~F:8,1,^108m,(16,-4,1,^108m)*) [D:0..255,S:0..255=255-D,F:0..255]";
                "{38.4k,22p,33%,msb}<1,-1|1,-3>(16,-8,D:8,S:8,F:8,~F:8,1,^108m,(16,-4,1,^108m)*) [D:0..255,S:0..255=255-D,F:0..255]";
        IrpLexer lex = new IrpLexer(new ANTLRInputStream(irpString));
        CommonTokenStream tokens = new CommonTokenStream(lex);
        IrpParser parser = new IrpParser(tokens);
        ParseTree parseTree = parser.protocol();
        System.out.println(parseTree.getChild(0).getChild(1).getChild(0).getChild(0).toStringTree(parser));

        System.out.println(parseTree.toStringTree(parser));
        //((IrpParser.ProtocolContext)parseTree).
        IrpTraverser irpTraverser = new IrpTraverser();
        irpTraverser.visit(parseTree);
    }
}
