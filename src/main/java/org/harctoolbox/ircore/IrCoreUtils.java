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
    public static final double defaultRelativeTolerance = 0.2;

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
     * @param secs
     * @return Argument converted to microseconds.
     */
    public static double seconds2microseconds(double secs) {
        return 1000000f * secs;
    }

    public static double milliseconds2microseconds(double secs) {
        return 1000f * secs;
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

    /**
     * Tests for approximate equality.
     *
     * @param x first argument
     * @param y second argument
     * @param absoluteTolerance
     * @param relativeTolerance
     * @return true if either absolute or relative requirement is satisfied.
     */
    public static boolean isEqual(double x, double y, double absoluteTolerance, double relativeTolerance) {
        double absDiff = Math.abs(x - y);
        boolean absoluteOk = absDiff <= absoluteTolerance;
        double max = Math.max(Math.abs(x), Math.abs(y));
        boolean relativeOk = max > 0 && absDiff / max <= relativeTolerance;
        return absoluteOk || relativeOk;
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
    public static boolean isEqual(int x, int y, int absoluteTolerance, double relativeTolerance) {
        int absDiff = Math.abs(x - y);
        boolean absoluteOk = absDiff <= absoluteTolerance;
        int max = Math.max(Math.abs(x), Math.abs(y));
        boolean relativeOk = max > 0 && (double) absDiff / (double) max <= relativeTolerance;
        return absoluteOk || relativeOk;
    }

    private IrCoreUtils() {
    }
}
