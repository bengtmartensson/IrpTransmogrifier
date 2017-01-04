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

package org.harctoolbox.analyze;

/**
 * This class is a collection of useful utilities as static functions and constants.
 */
public class AnalyzeUtils {

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
     * Default min repeat last gap, in milli seconds.
     */
    public static final double defaultMinRepeatLastGap = 20000f; // 20 milli seconds minimum for a repetition

    private AnalyzeUtils() {
    }
}