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
import org.harctoolbox.ircore.IrCoreUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public final class ParameterSpec extends IrpObject {
    private static final int WEIGHT = 1;
    private static Random random;
    private static final String[] standardNames = { "D", "S", "F", "T" };

    static {
        random = new Random();
    }

    public static boolean isStandardName(String name) {
        for (String n : standardNames)
            if (name.equals(n))
                return true;
        return false;
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
        super(null);
        this.memory = false;
        this.name = new Name(name);
        this.memory = hasMemory;
        this.min = new Number(min);
        this.max = new Number(max);
        this.deflt = deflt != null ? Expression.newExpression(deflt) : null;
    }

    public ParameterSpec(String name, boolean memory, long min, long max, Expression deflt) throws InvalidNameException {
        super(null);
        this.memory = false;
        this.name = new Name(name);
        this.min = new Number(min);
        this.max = new Number(max);
        this.memory = memory;
        this.deflt = deflt;
    }

    public ParameterSpec(String name, boolean memory, long min, long max) throws InvalidNameException {
        this(name, memory, min, max, null);
        this.memory = false;
    }

    public ParameterSpec(String name, boolean memory, int length) throws InvalidNameException {
        this(name, memory, 0, length == 64 ? -1L : (1 << length) - 1);
    }

    @Override
    public String toIrpString(int radix) {
        return name + (memory ? "@" : "") + ":" + min.toIrpString(radix) + ".." + max.toIrpString(radix) + (deflt != null ? ("=" + deflt.toIrpString(radix)) : "");
    }

    public void check(NameEngine nameEngine) throws InvalidNameException, DomainViolationException, NameUnassignedException {
        if (!nameEngine.containsKey(name.getName())) {
            if (this.hasMemory())
                return;

            if (deflt != null)
                nameEngine.define(name.toString(), deflt);
            else
                throw new NameUnassignedException(name, true);
        }

        Long value = nameEngine.get(name.getName()).toLong(nameEngine);
        checkDomain(value);
    }

    public void checkDomain(long value) throws DomainViolationException {
        if (!isWithinDomain(value))
            throw new DomainViolationException(this, value);
    }

    public boolean isWithinDomain(long x) {
        return min.toLong() <= x && x <= max.toLong();
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

    public boolean hasDefault() {
        return deflt != null;
    }

    public long getMin() {
        return min.toLong();
    }

    public long getMax() {
        return max.toLong();
    }

    public boolean hasMemory() {
        return memory;
    }

    public long random() {
        return random(random);
    }

    public long random(Random rng) {
        long interval = getMax() - getMin() + 1;
        return interval <= Integer.MAX_VALUE ? randomSimple(rng) : randomHairy(rng);
    }

    private long randomSimple(Random rng) {
        return rng.nextInt((int) (getMax() - getMin() + 1)) + getMin();
    }

    private long randomHairy(Random rng) {
        long x = rng.nextLong() & IrCoreUtils.ones(Long.SIZE - 1); // between 0 and Long.MAX_VALUE
        double frac = ((double) x) / Long.MAX_VALUE; // between 0 and 1
        long out = (long) ((getMax() - getMin()) * frac + getMin());
        return out;
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
