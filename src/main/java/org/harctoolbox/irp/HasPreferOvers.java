/*
Copyright (C) 2019 Bengt Martensson.

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

import java.util.Set;

/**
 * Represents an object that has prefer-overs.
 */
public interface HasPreferOvers {

    /**
     * Returns the prefer-overs.
     * @return Set&lt;String&gt; of the names of protocols that are prefer-overs.
     */
    public Set<String> getPreferOverNames();

    public String getName();

    public HasPreferOvers getDecode();
}
