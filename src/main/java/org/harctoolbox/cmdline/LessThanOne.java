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
package org.harctoolbox.cmdline;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

 public class LessThanOne implements IParameterValidator {

    @Override
    public void validate(String name, String value) throws ParameterException {
        try {
            double d = Double.parseDouble(value);
            if (d < 0 || d >= 1)
                throw new ParameterException("Parameter " + name + " must be  be between 0 and 1 (found " + value + ")");
        } catch (NumberFormatException ex) {
            throw new ParameterException("Parameter " + name + " must be a double (found " + value + ")");
        }
    }
}
