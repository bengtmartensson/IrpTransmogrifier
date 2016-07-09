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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 *
 */
public class ParameterSpec extends IrpObject {
    private Name name;
    private Number min;
    private Number max;
    private Expression deflt;
    private boolean memory;

    public String toString(IrpParser parser) {
        return name + (memory ? "@" : "") + ":" + min + ".." + max + (deflt != null ? ("=" + deflt.toStringTree(parser)) : "");
    }

    @Override
    public String toString() {
        return toString(null);
    }

    @Override
    public String toIrpString() {
        return name + (memory ? "@" : "") + ":" + min + ".." + max + (deflt != null ? ("=" + deflt.toIrpString()) : "");
    }

    @Override
    public Element toElement(Document document) {
        Element el = document.createElement("parameter");
        el.setAttribute("name", name.toString());
        el.setAttribute("min", min.toString());
        el.setAttribute("max", max.toString());
        el.setAttribute("memory", Boolean.toString(memory));
        if (deflt != null) {
            Element def = document.createElement("default");
            el.appendChild(def);
            def.appendChild(deflt.toElement(document));
        }
        return el;
    }

    public ParameterSpec(String str) {
        this(new ParserDriver(str).getParser().parameter_spec());
    }

    public ParameterSpec(IrpParser.Parameter_specContext ctx) {
        this(ctx.name(), ctx.getChild(1).getText().equals("@"), ctx.number(0), ctx.number(1), ctx.expression());
    }

    public ParameterSpec(IrpParser.NameContext name, boolean hasMemory, IrpParser.NumberContext min, IrpParser.NumberContext max, IrpParser.ExpressionContext deflt) {
        this.memory = false;
        this.name = new Name(name);
        this.memory = hasMemory;
        this.min = new Number(min);
        this.max = new Number(max);
        this.deflt = deflt != null ? new Expression(deflt) : null;
    }

    public ParameterSpec(String name, boolean memory, int min, int max, Expression deflt) {
        this.memory = false;
        this.name = new Name(name);
        this.min = new Number(min);
        this.max = new Number(max);
        this.memory = memory;
        this.deflt = deflt;
    }

    public ParameterSpec(String name, boolean memory, int min, int max) {
        this(name, memory, min, max, null);
        this.memory = false;
    }

    public boolean isOK(long x) {
        return min.toNumber() <= x && x <= max.toNumber();
    }

    public String domainAsString() {
        return min + ".." + max;
    }

    public String getName() {
        return name.toString();
    }

    public Expression getDefault() {
        return deflt;
    }

    public long getMin() {
        return min.toNumber();
    }

    public long getMax() {
        return max.toNumber();
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
