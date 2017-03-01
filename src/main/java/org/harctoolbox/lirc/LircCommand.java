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

package org.harctoolbox.lirc;

import java.util.Collections;
import java.util.List;

/**
 * This class consists of a semantic-agnostic model of a Lirc command.
 * The "codes" are either codes for parametrized protocols (in general only one),
 * or a sequence of durations in micro seconds ("raw codes").
 */
public final class LircCommand {

    private final String name;

    // May be either command codes (for cooked remotes),
    // or a list of durations (for raw remotes).
    private final List<Long> codes;

    LircCommand(String name, List<Long> codes) {
        this.name = name;
        this.codes = codes;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the code
     */
    public List<Long> getCodes() {
        return Collections.unmodifiableList(codes);
    }
}
