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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class implements Repeatmarker as per Chapter 8.
 */
public final class RepeatMarker extends IrpObject {

    public static RepeatMarker newRepeatMarker(char ch) {
        return ch == '*' ? new RepeatMarker(0, Integer.MAX_VALUE)
                : ch == '+' ? new RepeatMarker(1, Integer.MAX_VALUE)
                : new RepeatMarker(Character.toString(ch));
    }

    private int min;
    private int max;

    public RepeatMarker(String str) {
        this(new ParserDriver(str));
    }

    private RepeatMarker(ParserDriver parserDriver) {
        this(parserDriver.getParser().repeat_marker());
    }

    public RepeatMarker(IrpParser.Repeat_markerContext ctx) {
        super(ctx);
        String ch = ctx.getChild(0).getText();
        switch (ch) {
            case "*":
                min = 0;
                max = Integer.MAX_VALUE;
                break;
            case "+":
                min = 1;
                max = Integer.MAX_VALUE;
                break;
            default:
                min = Integer.parseInt(ch);
                if (ctx.getChildCount() > 1)
                    max = Integer.MAX_VALUE;
                else
                    max = min;
                break;
        }
    }

    public RepeatMarker(int min, int max) {
        super(null);
        this.min = min;
        this.max = max;
    }

    public RepeatMarker(int n) {
        this(n, n);
    }

    public RepeatMarker() {
        this(1, 1);
    }


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RepeatMarker))
            return false;

        RepeatMarker other = (RepeatMarker) obj;
        return min == other.min
                && max == other.max;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + this.min;
        hash = 89 * hash + this.max;
        return hash;
    }

    public boolean isInfinite() {
        return max == Integer.MAX_VALUE;
    }

    @Override
    public int numberOfInfiniteRepeats() {
        return isInfinite() ? 1 : 0;
    }

    @Override
    public String toIrpString(int radix) {
        return
                //(min == 1 && max == 1) ? ""
                  (min == 0 && max == Integer.MAX_VALUE) ? "*"
                : (min == 1 && max == Integer.MAX_VALUE) ? "+"
                : (min == 1 && max == 1) ? ""
                : (min == max) ? Integer.toString(min)
                : (max == Integer.MAX_VALUE) ? Integer.toString(min) + "+"
                : "??";
    }

    /**
     * @return the min
     */
    public int getMin() {
        return min;
    }

    /**
     * @return the max
     */
    public int getMax() {
        return max;
    }

    boolean isRPlus() {
        return isInfinite() && getMin() > 0;
    }

    @Override
    public Element toElement(Document document) {
        Element element = super.toElement(document);
        if (min > 0)
            element.setAttribute("min", Integer.toString(min));
        if (max < Integer.MAX_VALUE)
            element.setAttribute("max", Integer.toString(max));
        element.setTextContent(toString());
        return element;
    }

    public boolean isTrivial() {
        return min == 1 && max == 1;
    }

    @Override
    public int weight() {
        return isTrivial() ? 0 : 1;
    }
}
