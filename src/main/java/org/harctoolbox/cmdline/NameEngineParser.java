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

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.ParameterException;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.harctoolbox.irp.NameEngine;

public class NameEngineParser implements IStringConverter<NameEngine> {

    @Override
    public NameEngine convert(String value) {
        try {
            return NameEngine.parseLoose(value);
        } catch (ParseCancellationException ex) {
            throw new ParameterException("Parse error as name engine: \"" + value + "\"");
        } catch (IllegalArgumentException ex) {
            throw new ParameterException(ex);
        }
    }
}
