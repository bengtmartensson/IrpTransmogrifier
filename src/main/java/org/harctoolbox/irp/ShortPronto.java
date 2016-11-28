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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.harctoolbox.ircore.InvalidArgumentException;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.OddSequenceLenghtException;
import org.harctoolbox.ircore.Pronto;
import org.harctoolbox.ircore.ThisCannotHappenException;

/**
 * Extends org.harctoolbox.ircore with functions for handling short Pronto format for the protocols rc5, rc5x, rc6, and nec1.
 */
public class ShortPronto extends Pronto {
    private final static int rc5Code = 0x5000;
    private final static int rc5xCode = 0x5001;
    private final static int rc6Code = 0x6000;
    private final static int nec1Code = 0x900a;
    private final static int rc5Frequency = 0x0073;
    private final static int rc5xFrequency = 0x0073;
    private final static int rc6Frequency = 0x0073;
    private final static int nec1Frequency = 0x006C;

    private final static String rc5Irp  = "{36k,msb,889}<1,-1|-1,1>((1:1,~F:1:6,T:1,D:5,F:6,^114m)*,T=1-T)[T@:0..1=0,D:0..31,F:0..127]";
    private final static String rc5xIrp = "{36k,msb,889}<1,-1|-1,1>((1:1,~S:1:6,T:1,D:5,-4,S:6,F:6,^114m)*,T=1-T) [D:0..31,S:0..127,F:0..63,T@:0..1=0]";
    private final static String rc6Irp  = "{36k,444,msb}<-1,1|1,-1>((6,-2,1:1,0:3,<-2,2|2,-2>(T:1),D:8,F:8,^107m)*,T=1-T) [D:0..255,F:0..255,T@:0..1=0]";
    private final static String nec1Irp = "{38.4k,564}<1,-1|1,-3>(16,-8,D:8,S:8,F:8,~F:8,1,-78,(16,-4,1,-173)*) [D:0..255,S:0..255=255-D,F:0..255]";

    private static final Logger logger = Logger.getLogger(ShortPronto.class.getName());

    /**
     * Creates a new IrSignals by interpreting its argument as CCF signal.
     * @param ccf CCF signal
     * @return  IrSignal
     * @throws org.harctoolbox.ircore.OddSequenceLenghtException
     * @throws InvalidArgumentException
     */
    public static IrSignal parse(int[] ccf) throws OddSequenceLenghtException, InvalidArgumentException {
        if (ccf.length < 4)
            throw new InvalidArgumentException("CCF is invalid since less than 4 numbers long.");
        if (ccf.length % 2 != 0)
            throw new OddSequenceLenghtException("CCF is invalid since it has an odd number ("
                    + ccf.length + ") of durations.");
        String irp = null;
        int dev = (int) IrpUtils.invalid;
        int subdev = (int) IrpUtils.invalid;
        int cmd = (int) IrpUtils.invalid;

        int index = 0;
        int type = ccf[index++];
        //int frequencyCode = ccf[index++];
        index++;
        int introLength = ccf[index++];
        int repeatLength = ccf[index++];
        if (index + 2*(introLength+repeatLength) != ccf.length)
            throw new InvalidArgumentException("Inconsistent length in CCF (claimed "
                    + (introLength + repeatLength) + " pairs, was " + (ccf.length - 4)/2 + " pairs).");
        IrSignal irSignal = null;

        switch (type) {
            case learnedCode: // 0x0000
            case learnedZeroFrequencyCode: // 0x0100
                irSignal = Pronto.parse(ccf);
                break;

            case rc5Code: // 0x5000:
                if (repeatLength != 1)
                    throw new InvalidArgumentException("wrong repeat length");
                irp = rc5Irp;
                dev = ccf[index++];
                cmd = ccf[index++];
                break;

            case rc5xCode: // 0x5001:
                if (repeatLength != 2)
                    throw new InvalidArgumentException("wrong repeat length");
                irp = rc5xIrp;
                dev = ccf[index++];
                subdev = ccf[index++];
                cmd = ccf[index++];
                break;

            case rc6Code: // 0x6000:
                if (repeatLength != 1)
                    throw new InvalidArgumentException("wrong repeat length");
                irp = rc6Irp;
                dev = ccf[index++];
                cmd = ccf[index++];
                break;

            case nec1Code: // 0x900a:
                if (repeatLength != 1)
                    throw new InvalidArgumentException("wrong repeat length");
                irp = nec1Irp;
		dev = ccf[index] >> 8;
                subdev = ccf[index++] & 0xff;
		cmd = ccf[index] >> 8;
		int cmd_chk = 0xff - (ccf[index++] & 0xff);
		if (cmd != cmd_chk)
		    throw new InvalidArgumentException("checksum erroneous");
                break;

            default:
                throw new InvalidArgumentException("CCF type 0x" + Integer.toHexString(type) + " not supported");
        }

        if (irSignal == null) {
            NameEngine nameEngine = new NameEngine(3);
            try {
                nameEngine.define("D", dev);
                if (subdev != (int) IrpUtils.invalid)
                    nameEngine.define("S", subdev);
                nameEngine.define("F", cmd);
            } catch (IrpSyntaxException ex) {
                Logger.getLogger(ShortPronto.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                Protocol protocol = new Protocol(irp);
                irSignal = protocol.toIrSignal(nameEngine); // do not catch DomainViolationException
            } catch (IrpSemanticException | IrpSyntaxException | ArithmeticException | InvalidRepeatException | UnassignedException ex) {
                throw new ThisCannotHappenException(ex);
            } catch (DomainViolationException ex) {
                logger.log(Level.SEVERE, "{0}", ex.getMessage());
                throw new InvalidArgumentException(ex);
            }
        }
        return irSignal;
    }


    /**
     * Creates a new IrSignals by interpreting its argument as CCF string.
     * @param list Strings representing hexadecimal numbers
     * @return IrSignal
     * @throws InvalidArgumentException
     */
    public static IrSignal parse(List<String> list) throws InvalidArgumentException {
        int[] ccf;
        try {
            ccf = Pronto.parseAsInts(list);
        } catch (NumberFormatException ex) {
            throw new InvalidArgumentException("Non-parseable CCF strings");
        }
        if (ccf == null)
            throw new InvalidArgumentException("Invalid CCF strings");

        return parse(ccf);
    }

    /**
     * The string version of shortCCF(...).
     *
     * @param protocolName
     * @param device
     * @param subdevice
     * @param command
     * @return CCF as string, or null on failure.
     * @throws org.harctoolbox.ircore.InvalidArgumentException
     */
    public static String shortCCFString(String protocolName, int device, int subdevice, int command) throws InvalidArgumentException {
        int[] ccf = shortCCF(protocolName, device, subdevice, command);
        return toPrintString(ccf);
    }

    /**
     * Computes the "short" Pronto form of some signals, given by protocol number and parameter values.
     *
     * @param protocolName Name of protcol, presently "rc5", "rc5x", "rc6", and "nec1".
     * @param device
     * @param subdevice
     * @param command
     * @return integer array of short CCF, or null om failure.
     * @throws InvalidArgumentException for paramters outside of its allowed domain.
     */
    public static int[] shortCCF(String protocolName, int device, int subdevice, int command) throws InvalidArgumentException {
        int index = 0;
        if (protocolName.equalsIgnoreCase("rc5")) {
            if (device > 31 || subdevice != (int) IrpUtils.invalid || command > 127)
                throw new InvalidArgumentException("Invalid parameters");

            int[] result = new int[6];
            result[index++] = rc5Code;
            result[index++] = rc5Frequency;
            result[index++] = 0;
            result[index++] = 1;
            result[index++] = device;
            result[index++] = command;

            return result;
        } else if (protocolName.equalsIgnoreCase("rc5x")) {
            if (device > 31 || subdevice > 127 || subdevice < 0 || command > 63)
                throw new InvalidArgumentException("Invalid parameters");

            int[] result = new int[8];
            result[index++] = rc5xCode;
            result[index++] = rc5xFrequency;
            result[index++] = 0;
            result[index++] = 2;
            result[index++] = device;
            result[index++] = subdevice;
            result[index++] = command;
            result[index++] = 0;

            return result;
        } else if (protocolName.equalsIgnoreCase("rc6")) {
            if (device > 255 || subdevice != (int) IrpUtils.invalid || command > 255)
                throw new InvalidArgumentException("Invalid parameters");

            int[] result = new int[6];
            result[index++] = rc6Code;
            result[index++] = rc6Frequency;
            result[index++] = 0;
            result[index++] = 1;
            result[index++] = device;
            result[index++] = command;

            return result;
        } else if (protocolName.equalsIgnoreCase("nec1")) {
            if (device > 255 || subdevice > 255 || command > 255)
                throw new InvalidArgumentException("Invalid parameters");

            int[] result = new int[6];
            result[index++] = nec1Code;
            result[index++] = nec1Frequency;
            result[index++] = 0;
            result[index++] = 1;
            result[index++] = (device << 8) + (subdevice != (int) IrpUtils.invalid ? subdevice : (0xff - device));
            result[index++] = (command << 8) + (0xff - command);

            return result;
        } else {
            return new int[0];
        }
    }

    private ShortPronto() {
    }
}
