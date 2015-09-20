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
     * Convert its argument from seconds to microseconds.
     * @param secs
     * @return Argument converted to microseconds.
     */
    public static double seconds2microseconds(double secs) {
        return 1000000.0 * secs;
    }

    private IrCoreUtils() {
    }
}
