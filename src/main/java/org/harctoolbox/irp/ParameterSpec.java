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

import java.util.Objects;
import java.util.Random;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ParameterSpec extends IrpObject {
    private static final int WEIGHT = 1;
    private static Random random;

    static {
        random = new Random();
    }
    public static void initRandom(long seed) {
        random = new Random(seed);
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

    public ParameterSpec(String name, boolean memory, long min, long max, Expression deflt) {
        this.memory = false;
        this.name = new Name(name);
        this.min = new Number(min);
        this.max = new Number(max);
        this.memory = memory;
        this.deflt = deflt;
    }

    public ParameterSpec(String name, boolean memory, long min, long max) {
        this(name, memory, min, max, null);
        this.memory = false;
    }

    public ParameterSpec(String name, boolean memory, int length) {
        this(name, memory, 0, (1 << length) - 1);
    }

    @Override
    public String toString() {
        return toIrpString();
    }

    @Override
    public String toIrpString() {
        return name + (memory ? "@" : "") + ":" + min + ".." + max + (deflt != null ? ("=" + deflt.toIrpString()) : "");
    }

    public void check(NameEngine nameEngine) throws DomainViolationException, UnassignedException, InvalidNameException {
        if (!nameEngine.containsKey(name.getName())) {
            if (this.hasMemory())
                return;

            if (deflt != null)
                nameEngine.define(name, deflt);
            else
                throw new UnassignedException("Parameter " + name + " not assigned, and has no default");
        }

        Long value = nameEngine.get(name.getName()).toNumber(nameEngine);
        checkDomain(value);
    }

    public void checkDomain(long value) throws DomainViolationException {
        if (!isWithinDomain(value))
            throw new DomainViolationException("Parameter " + name + " = " + value + " is outside of the allowed domain: "
                    + domainAsString());
    }

    public boolean isWithinDomain(long x) {
        return min.toNumber() <= x && x <= max.toNumber();
    }

    @Override
    public Element toElement(Document document) {
        Element el = super.toElement(document);
        el.setAttribute("name", name.toString());
        el.setAttribute("min", min.toString());
        el.setAttribute("max", max.toString());
        el.setAttribute("memory", Boolean.toString(memory));
        if (deflt != null) {
            Element def = document.createElement("Default");
            el.appendChild(def);
            def.appendChild(deflt.toElement(document));
        }
        return el;
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

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ParameterSpec))
            return false;

        ParameterSpec other = (ParameterSpec) obj;
        return memory == other.memory
                && min == other.min
                && max == other.max
                && name.equals(other.name)
                && deflt.equals(other.deflt);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 61 * hash + Objects.hashCode(this.name);
        hash = 61 * hash + Objects.hashCode(this.min);
        hash = 61 * hash + Objects.hashCode(this.max);
        hash = 61 * hash + Objects.hashCode(this.deflt);
        hash = 61 * hash + (this.memory ? 1 : 0);
        return hash;
    }

    String code(CodeGenerator codeGenerator) {
        ItemCodeGenerator template = codeGenerator.newItemCodeGenerator(this);
        template.addAttribute("name", name);
        template.addAttribute("min", min);
        template.addAttribute("max", max);
        template.addAttribute("deflt", deflt);
        template.addAttribute("memory", memory);
        return template.render();
    }
}
