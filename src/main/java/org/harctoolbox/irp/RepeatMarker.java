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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class implements Repeatmarker as per Chapter 8.
 */
public class RepeatMarker extends IrpObject {

    private int min;
    private int max;

    public RepeatMarker(String str) throws InvalidRepeatException {
        this((new ParserDriver(str)).getParser().repeat_marker());
    }

    public RepeatMarker(IrpParser.Repeat_markerContext ctx) throws InvalidRepeatException {
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
        this.min = min;
        this.max = max;
    }

    public RepeatMarker(int n) {
        this(n, n);
    }

    public RepeatMarker() {
        this(1, 1);
    }

    public RepeatMarker(char ch) throws InvalidRepeatException {
        this(Character.toString(ch));
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
    public String toString() {
        return
                //(min == 1 && max == 1) ? ""
                  (min == 0 && max == Integer.MAX_VALUE) ? "*"
                : (min == 1 && max == Integer.MAX_VALUE) ? "+"
                : (min == 1 && max == 1) ? ""
                : (min == max) ? Integer.toString(min)
                : (max == Integer.MAX_VALUE) ? Integer.toString(min) + "+"
                : "??";
    }

    @Override
    public String toIrpString() {
        return toString();
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

    @Override
    public Element toElement(Document document) {
        Element element = document.createElement("repeat_marker");
        if (min > 0)
            element.setAttribute("min", Integer.toString(min));
        if (max < Integer.MAX_VALUE)
            element.setAttribute("max", Integer.toString(max));
        element.setTextContent(toString());
        return element;
    }

    @Override
    public int weight() {
        return (min == 1 && max == 1) ? 0 : 1;
    }
}
