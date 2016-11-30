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

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.ModulatedIrSequence;
import org.harctoolbox.ircore.OddSequenceLenghtException;

/**
 * This class exists <strong>only</strong> to be able to test the products of this package
 * against IrpMaster. It is not to be used for anything else.
 */
public class IrpMasterUtils {

    private static final String configFile = "../harctoolboxbundle/IrpMaster/src/main/config/IrpProtocols.ini"; // FIXME
    private static org.harctoolbox.IrpMaster.IrpMaster irpMaster = null;

    static {
        try {
            irpMaster = new org.harctoolbox.IrpMaster.IrpMaster(configFile);
        } catch (FileNotFoundException | org.harctoolbox.IrpMaster.IncompatibleArgumentException ex) {
            Logger.getLogger(IrpMasterUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static IrSequence toIrSequence(org.harctoolbox.IrpMaster.IrSequence oldIrSequence) throws OddSequenceLenghtException {
        return new IrSequence(oldIrSequence.toDoubles());
    }

    public static ModulatedIrSequence toModulatedIrSequence(org.harctoolbox.IrpMaster.ModulatedIrSequence oldIrSequence) throws OddSequenceLenghtException {
        return new ModulatedIrSequence(toIrSequence(oldIrSequence), oldIrSequence.getFrequency(), oldIrSequence.getDutyCycle());
    }

    public static IrSignal toIrSignal(org.harctoolbox.IrpMaster.IrSignal oldIrSignal) {
        try {
            return new IrSignal(toIrSequence(oldIrSignal.getIntroSequence()),
                    toIrSequence(oldIrSignal.getRepeatSequence()),
                    toIrSequence(oldIrSignal.getEndingSequence()),
                    oldIrSignal.getFrequency(), oldIrSignal.getDutyCycle());
        } catch (OddSequenceLenghtException ex) {
            Logger.getLogger(IrpMasterUtils.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public static IrSignal renderIrSignal(String protocolName, NameEngine parameters) throws org.harctoolbox.IrpMaster.IrpMasterException, UnassignedException, IrpSyntaxException {
        return renderIrSignal(protocolName, parameters.toMap());
    }

    public static IrSignal renderIrSignal(String protocolName, Map<String, Long> parameters) throws org.harctoolbox.IrpMaster.IrpMasterException {
        org.harctoolbox.IrpMaster.Protocol irpMasterProtol = irpMaster.newProtocol(protocolName);
        org.harctoolbox.IrpMaster.IrSignal irSignal = irpMasterProtol.renderIrSignal(new HashMap<>(parameters));
        return toIrSignal(irSignal);
    }
    private IrpMasterUtils() {
    }
}
