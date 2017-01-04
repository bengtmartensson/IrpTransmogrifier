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
 * Thrown when a name is assigned a value that fatally contradicts the current value.
 */
public class NameConflictException extends IrpException {

    public NameConflictException(String name) {
        super("Conflicting assignments of " + name);
    }

    public NameConflictException(String name, long newValue, long oldValue) {
        super("Conflicting assignments of " + name + ", new: " + newValue + ", old: " + oldValue);
    }
}
