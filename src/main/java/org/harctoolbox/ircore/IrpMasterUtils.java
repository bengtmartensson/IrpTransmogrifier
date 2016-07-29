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

package org.harctoolbox.ircore;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.harctoolbox.IrpMaster.IncompatibleArgumentException;
import org.harctoolbox.IrpMaster.IrpMaster;
import org.harctoolbox.IrpMaster.IrpMasterException;
import org.harctoolbox.IrpMaster.Protocol;
import org.harctoolbox.irp.IrpSyntaxException;
import org.harctoolbox.irp.NameEngine;
import org.harctoolbox.irp.UnassignedException;

/**
 * This class exists <strong>only</strong> to be able to test the products of this package
 * against IrpMaster. It is not to be used for anything else.
 */
public class IrpMasterUtils {

    private static final String configFile = "/usr/local/share/irscrutinizer/IrpProtocols.ini";
    private static IrpMaster irpMaster = null;

    static {
        try {
            irpMaster = new IrpMaster(configFile);
        } catch (FileNotFoundException | IncompatibleArgumentException ex) {
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

    public static IrSignal renderIrSignal(String protocolName, NameEngine parameters) throws IrpMasterException, UnassignedException, IrpSyntaxException, org.harctoolbox.ircore.IncompatibleArgumentException {
        return renderIrSignal(protocolName, parameters.getMap());
    }

    public static IrSignal renderIrSignal(String protocolName, HashMap<String, Long> parameters) throws IrpMasterException {
        Protocol irpMasterProtol = irpMaster.newProtocol(protocolName);
        org.harctoolbox.IrpMaster.IrSignal irSignal = irpMasterProtol.renderIrSignal(parameters);
        return toIrSignal(irSignal);
    }
    private IrpMasterUtils() {
    }
}
