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

package org.harctoolbox.ircore;

/**
 * Some useful static functions.
 */
public class IrCoreUtils {

    /**
     * Default absolute tolerance in micro seconds.
     */
    public static final double defaultAbsoluteTolerance = 60;

    /**
     * Default relative tolerance as a number between 0 and 1.
     */
    public static final double defaultRelativeTolerance = 0.1;

    /**
     * Default absolute tolerance for frequency comparison.
     */
    public static final double defaultFrequencyTolerance = 500;

    /**
     * Default absolute tolerance for frequency comparison.
     */
    public static final double invalid = -1f;

    /**
     * Convert its argument from seconds to microseconds.
     * @param secs seconds
     * @return Argument converted to microseconds.
     */
    public static double seconds2microseconds(double secs) {
        return 1000000f * secs;
    }

    public static double milliseconds2microseconds(double ms) {
        return 1000f * ms;
    }

    public static double khz2Hz(double khz) {
        return 1000f * khz;
    }

    public static double percent2real(double percent) {
        return 0.01f * percent;
    }

    public static long real2percent(double x) {
        return Math.round(100f * x);
    }

    public static double l1Norm(Double[] sequence) {
        double sum = 0;
        for (Double d : sequence)
            sum += Math.abs(d);
        return sum;
    }

    public static double l1Norm(Iterable<Double> sequence) {
        double sum = 0;
        for (Double d : sequence)
            sum += Math.abs(d);

        return sum;
    }

    public static double l1Norm(double[] sequence) {
        return l1Norm(sequence, 0, sequence.length);
    }

    public static double l1Norm(double[] sequence, int beg, int length) {
        double sum = 0;
        for (int i = beg; i < beg + length; i++)
            sum += Math.abs(sequence[i]);
        return sum;
    }

    /**
     * Reverses the bits, living in a width-bit wide world.
     *
     * @param x data
     * @param width width in bits
     * @return bitreversed
     */

    public static long reverse(long x, int width) {
        long y = Long.reverse(x);
        if (width > 0)
            y >>>= Long.SIZE - width;
        return y;
    }

    /**
     * Tests for approximate equality.
     *
     * @param x first argument
     * @param y second argument
     * @param absoluteTolerance
     * @param relativeTolerance
     * @return true if either absolute or relative requirement is satisfied.
     */
    public static boolean approximatelyEquals(double x, double y, double absoluteTolerance, double relativeTolerance) {
        double absDiff = Math.abs(x - y);
        boolean absoluteOk = absDiff <= absoluteTolerance;
        double max = Math.max(Math.abs(x), Math.abs(y));
        boolean relativeOk = max > 0 && absDiff / max <= relativeTolerance;
        return absoluteOk || relativeOk;
    }

    public static boolean approximatelyEquals(double x, double y) {
        return approximatelyEquals(x, y, defaultAbsoluteTolerance, defaultRelativeTolerance);
    }

    /**
     * Tests for approximate equality.
     *
     * @param x first argument
     * @param y second argument
     * @param absoluteTolerance
     * @param relativeTolerance
     * @return true if either absolute or relative requirement is satisfied.
     */
    public static boolean approximatelyEquals(int x, int y, int absoluteTolerance, double relativeTolerance) {
        int absDiff = Math.abs(x - y);
        boolean absoluteOk = absDiff <= absoluteTolerance;
        int max = Math.max(Math.abs(x), Math.abs(y));
        boolean relativeOk = max > 0 && absDiff / (double) max <= relativeTolerance;
        return absoluteOk || relativeOk;
    }

    /**
     * Return a number consisting of width number of 1, probably for using as bit mask.
     * @param width &gt; 0
     * @return
     */
    public static long ones(long width) {
        return (1L << width) - 1L;
    }

    /**
     * Return a number consisting of width number of 1, probably for using as bit mask.
     * @param width &gt; 0
     * @return
     */
    public static int ones(int width) {
        return (1 << width) - 1;
    }

    public static long maskTo(long data, long width) {
        return data & ones(width);
    }

    public static long maskTo(int data, int width) {
        return data & ones(width);
    }

    private IrCoreUtils() {
    }
}
