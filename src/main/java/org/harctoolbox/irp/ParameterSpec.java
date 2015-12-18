/*
Copyright (C) 2011, 2015 Bengt Martensson.

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

import org.antlr.v4.runtime.tree.ParseTree;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 *
 */
public class ParameterSpec {
    private String name;
    private long min;
    private long max;
    private IrpParser.Bare_expressionContext deflt;
    private boolean memory = false;

    public String toString(IrpParser parser) {
        return name + (memory ? "@" : "") + ":" + min + ".." + max + (deflt != null ? ("=" + deflt.toStringTree(parser)) : "");
    }

    @Override
    public String toString() {
        return toString(null);
    }

    public Element toElement(Document document) {
        Element el = document.createElement("parameter");
        el.setAttribute("name", name);
        el.setAttribute("min", Long.toString(min));
        el.setAttribute("max", Long.toString(max));
        el.setAttribute("memory", Boolean.toString(memory));
        if (deflt != null)
            el.setAttribute("default", deflt.toString());
        return el;
    }

    public ParameterSpec(String name, int min, int max, boolean memory, int deflt) {
// FIXME
//    this(name, min, max, memory, IrpParser.newIntegerTree(deflt));
    }

    public ParameterSpec(String name, int min, int max) {
        this(name, min, max, false);
    }

    public ParameterSpec(String name, int min, int max, boolean memory, IrpParser.Bare_expressionContext deflt) {
        this.name = name;
        this.min = min;
        this.max = max;
        this.memory = memory;
        this.deflt = deflt;
    }

    public ParameterSpec(String name, int min, int max, boolean memory) {
        this(name, min, max, memory, (IrpParser.Bare_expressionContext) null);
    }

    /*public ParameterSpec(String name, int min, int max, boolean memory, String bare_expression) {
        this.name = name;
        this.min = min;
        this.max = max;
        this.memory = memory;
        IrpLexer lex = new IrpLexer(new ANTLRStringStream(bare_expression));
        CommonTokenStream tokens = new CommonTokenStream(lex);
        IrpParser parser = new IrpParser(tokens);
        IrpParser.bare_expression_return r;
        try {
            r = parser.bare_expression();
            CommonTree ct = (CommonTree) r.getTree();
            deflt = ct;
        } catch (RecognitionException ex) {
            throw new ParseException(ex);
        }
    }*/

    public ParameterSpec(IrpParser.MemoryfullParameterSpecContext t) {
        load(t);
    }

    public ParameterSpec(IrpParser.MemorylessParameterSpecContext t) {
        load(t);
    }

    /*public ParameterSpec(String parameter_spec) {
        IrpLexer lex = new IrpLexer(new ANTLRStringStream(parameter_spec));
        CommonTokenStream tokens = new CommonTokenStream(lex);
        IrpParser parser = new IrpParser(tokens);
        IrpParser.parameter_spec_return r;
        try {
            r = parser.parameter_spec();
            CommonTree ct = (CommonTree) r.getTree();
            load(ct);
        } catch (RecognitionException ex) {
            throw new ParseException(ex);
        }
    */

    private void load(IrpParser.MemorylessParameterSpecContext t) {
        memory = false;
        name = t.name().ID().getText();//.getChild(0).getText();
        min = Long.parseLong(t.INT(0).getText());
        max = Long.parseLong(t.INT(1).getText());
        deflt = t.bare_expression();
    }

    private void load(IrpParser.MemoryfullParameterSpecContext t) {
        memory = true;
        name = t.name().ID().getText();//.getChild(0).getText();
        min = Long.parseLong(t.INT(0).getText());
        max = Long.parseLong(t.INT(1).getText());
        deflt = t.bare_expression();
    }

    public boolean isOK(long x) {
        return min <= x && x <= max;
    }

    public String domainAsString() {
        return min + ".." + max;
    }

    public String getName() {
        return name;
    }

    public ParseTree getDefault() {
        return deflt;
    }

    public long getMin() {
        return min;
    }

    public long getMax() {
        return max;
    }

    public boolean hasMemory() {
        return memory;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
/*        ParameterSpec dev = null;
        ParameterSpec toggle = null;
        ParameterSpec func = null;
        try {
            dev = new ParameterSpec("d", 0, 255, false, "255-s");
            toggle = new ParameterSpec("t", 0, 1, true, 0);
            func = new ParameterSpec("F", 0, 1, false, 0);
            System.out.println(new ParameterSpec("Fx", 0, 1, false, 0));
            System.out.println(new ParameterSpec("Fx", 0, 1, false));
            System.out.println(new ParameterSpec("Fx", 0, 1));
            System.out.println(new ParameterSpec("D:0..31"));
            System.out.println(new ParameterSpec("D@:0..31=42"));
            System.out.println(new ParameterSpec("D:0..31=42*3+33"));
            System.out.println(dev);
            System.out.println(toggle);
            System.out.println(func);
            System.out.println(dev.isOK(-1));
            System.out.println(dev.isOK(0));
            System.out.println(dev.isOK(255));
            System.out.println(dev.isOK(256));
        } catch (ParseException ex) {
            System.out.println(ex.getMessage());
        }*/
    }
}
