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

/**
 * Interface for classes that can produce a numerical (long) result.
 */
public interface Numerical {
    /**
     * Produces a numerical result.
     * @param nameEngine used to resolve names in expressions.
     * @return result, long.
     * @throws UnassignedException
     */
    public long toNumber(NameEngine nameEngine) throws UnassignedException;
}
