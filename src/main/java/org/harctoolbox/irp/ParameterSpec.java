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

import java.util.Random;
import org.harctoolbox.ircore.IncompatibleArgumentException;
import org.harctoolbox.ircore.IrCoreUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 *
 */
public class ParameterSpec extends IrpObject {
    private static final int WEIGHT = 1;
    private static Random random;

    static {
        random = new Random();
    }
    public static void initRandom(long seed) {
        random = new Random(seed);
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
        System.out.println(dev.isOk(-1));
        System.out.println(dev.isOk(0));
        System.out.println(dev.isOk(255));
        System.out.println(dev.isOk(256));
        } catch (ParseException ex) {
        System.out.println(ex.getMessage());
        }*/
    }
    private Name name;
    private Number min;
    private Number max;
    private Expression deflt;
    private boolean memory;


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

    @Override
    public String toString() {
        return toIrpString();
    }

    @Override
    public String toIrpString() {
        return name + (memory ? "@" : "") + ":" + min + ".." + max + (deflt != null ? ("=" + deflt.toIrpString()) : "");
    }

    public void check(NameEngine nameEngine) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException, DomainViolationException {
        if (!nameEngine.containsKey(name.getName())) {
            if (this.hasMemory())
                return;

            if (deflt != null)
                nameEngine.define(name, deflt);
            else
                throw new UnassignedException("Parameter " + name + " not assigned, and has no default");
        }

        long value = nameEngine.get(name.getName()).toNumber(nameEngine);
        if (value == IrCoreUtils.invalid && deflt == null)
            throw new UnassignedException("Parameter " + name + " not assigned, and has no default");
        if (!isOk(value))
            throw new DomainViolationException("Parameter " + name + " outside of the allowed domain: "
                    + domainAsString());
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

    public boolean isOk(long x) {
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

    public long random() {
        long bound = getMax() - getMin() + 1;
        if (bound > Integer.MAX_VALUE) {
            return random.nextInt(Integer.MAX_VALUE); // FIXME
        } else
            return random.nextInt((int) bound) + getMin();
    }

    @Override
    public int weight() {
        return WEIGHT;
    }
}
