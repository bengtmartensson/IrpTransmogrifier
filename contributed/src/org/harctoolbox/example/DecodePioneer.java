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
package org.harctoolbox.example;

import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.Pronto;
import org.harctoolbox.irp.Decoder;
import org.harctoolbox.irp.Decoder.Decode;
import org.harctoolbox.irp.IrpDatabase;

public class DecodePioneer {

    public static void main(String[] args) {
        try {
            System.out.println(org.harctoolbox.irp.Version.versionString);
            IrSignal irSignal = Pronto.parse("0000 0068 0000 0022 0169 00B4 0017 0017 0017 0044 0017 0017 0017 0044 0017 0044 0017 0017 0017 0044 0017 0017 0017 0044 0017 0017 0017 0044 0017 0017 0017 0017 0017 0044 0017 0017 0017 0044 0017 0017 0017 0044 0017 0044 0017 0017 0017 0017 0017 0044 0017 0017 0017 0017 0017 0044 0017 0017 0017 0017 0017 0044 0017 0044 0017 0017 0017 0044 0017 0044 0017 0636");
            Decoder.DecoderParameters decoderParameters = new Decoder.DecoderParameters();
            //decoderParameters.setAllDecodes(true);
            //decoderParameters.setFrequencyTolerance(1000.0);
            //Decoder.setDebugProtocolRegExp("pioneer");
            Decoder decoder = new Decoder(new IrpDatabase((String) null));
            //Map<String, Decode> sigDecodes;
            //sigDecodes = decoder.decodeIrSignal(irSignal, decoderParameters);
            //for (Decode decode : sigDecodes.values())
            //    System.out.println(decode);
            decoderParameters.setFrequencyTolerance(1000.0);
            Decoder.setDebugProtocolRegExp("pioneer");
            Decoder.SimpleDecodesSet sigDecodes = decoder.decodeIrSignal(irSignal, decoderParameters);
            for (Decode decode : sigDecodes)
                System.out.println(decode);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
