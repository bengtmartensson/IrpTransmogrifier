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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Interface for classes that can produce an floating point number.
 */
public interface Floatable {
    /**
     * Produces a floating result.
     * @param nameEngine
     * @param generalSpec
     * @return Floating representation of class.
     * @throws org.harctoolbox.irp.IrpInvalidArgumentException
     * @throws org.harctoolbox.irp.NameUnassignedException
     */
    public double toFloat(GeneralSpec generalSpec, NameEngine nameEngine) throws IrpInvalidArgumentException, NameUnassignedException;

    public Element toElement(Document document);

    public String toIrpString(int radix);

    public boolean constant(NameEngine nameEngine);
}
