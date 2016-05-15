/*
Copyright (C) 2016 Bengt Martensson.

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

import java.util.List;
import org.harctoolbox.ircore.IncompatibleArgumentException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 */
public class BitspecIrstream extends IrStreamItem {
    private BitSpec bitSpec;
    private IrStream irStream;

    public BitspecIrstream(IrpParser.ProtocolContext ctx) throws IrpSyntaxException, InvalidRepeatException {
        this(ctx.bitspec_irstream());
    }

    public BitspecIrstream(IrpParser.Bitspec_irstreamContext ctx) throws IrpSyntaxException, InvalidRepeatException {
        bitSpec = new BitSpec(ctx.bitspec());
        irStream = new IrStream(ctx.irstream());
    }

    public Element toElement(Document document) {
        Element root = document.createElement("bitspec-irstream");
        root.appendChild(bitSpec.toElement(document));
        root.appendChild(irStream.toElement(document));
        return root;
    }

    @Override
    public boolean isEmpty(NameEngine nameEngine) throws IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<IrStreamItem> evaluate(BitSpec bitSpec) throws UnassignedException, IncompatibleArgumentException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
