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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.harctoolbox.ircore.InvalidArgumentException;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.OddSequenceLengthException;
import org.harctoolbox.ircore.Pronto;
import org.harctoolbox.ircore.ThisCannotHappenException;

/**
 * Extends org.harctoolbox.ircore with functions for handling short Pronto format for the protocols rc5, rc5x, rc6, and nec1.
 */
public final class ShortPronto extends Pronto {
    private final static int RC5_CODE = 0x5000;
    private final static int RC5X_CODE = 0x5001;
    private final static int RC6_CODE = 0x6000;
    private final static int NEC1_CODE = 0x900a;

    private final static int RC5_FREQUENCY = 0x0073;
    private final static int RC5X_FREQUENCY = 0x0073;
    private final static int RC6_FREQUENCY = 0x0073;
    private final static int NEC1_FREQUENCY = 0x006C;

    private final static String RC5_IRP  = "{36k,msb,889}<1,-1|-1,1>((1,~F:1:6,T:1,D:5,F:6,^114m)*,T=1-T)[D:0..31,F:0..127,T@:0..1=0]";
    private final static String RC5X_IRP = "{36k,msb,889}<1,-1|-1,1>((1,~S:1:6,T:1,D:5,-4,S:6,F:6,^114m)*,T=1-T)[D:0..31,S:0..127,F:0..63,T@:0..1=0]";
    private final static String RC6_IRP  = "{36k,444,msb}<-1,1|1,-1>((6,-2,1:1,0:3,<-2,2|2,-2>(T:1),D:8,F:8,^107m)*,T=1-T)[D:0..255,F:0..255,T@:0..1=0]";
    private final static String NEC1_IRP = "{38.4k,564}<1,-1|1,-3>(16,-8,D:8,S:8,F:8,~F:8,1,-78,(16,-4,1,-173)*) [D:0..255,S:0..255=255-D,F:0..255]";

    private final static String RC5_NAME  = "RC5";
    private final static String RC5X_NAME = "RC5x";
    private final static String RC6_NAME  = "RC6";
    private final static String NEC1_NAME = "NEC1";

    private final static Logger logger = Logger.getLogger(ShortPronto.class.getName());
    private final static Decoder decoder = miniDecoder();

    private static Decoder miniDecoder() {
        Map<String, String> map = new HashMap<>(4);
        map.put(RC5_NAME, RC5_IRP);
        map.put(RC5X_NAME, RC5X_IRP);
        map.put(RC6_NAME, RC6_IRP);
        map.put(NEC1_NAME, NEC1_IRP);
        try {
            IrpDatabase irpDatabase = IrpDatabase.parseIrp(map);
            return new Decoder(irpDatabase);
        } catch (IrpParseException ex) {
            //throw new ThisCannotHappenException();
            return null;
        }
    }

    /**
     * Creates a new IrSignals by interpreting its argument as CCF signal.
     * @param ccf CCF signal as array of integers
     * @return  IrSignal
     * @throws org.harctoolbox.ircore.OddSequenceLengthException
     */
    @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
    public static IrSignal parse(int[] ccf) throws InvalidArgumentException {
        if (ccf.length < MIN_CCF_LENGTH)
            throw new InvalidArgumentException("CCF is invalid since less than " + MIN_CCF_LENGTH + " numbers long.");
        if (ccf.length % 2 != 0)
            throw new OddSequenceLengthException("CCF is invalid since it has an odd number ("
                    + ccf.length + ") of durations.");
        String irp = null;
        Integer D = null;
        Integer S = null;
        Integer F = null;

        int type = ccf[TYPE_INDEX];
        int introLength = ccf[INTRO_LENGTH_INDEX];
        int repeatLength = ccf[REPEAT_LENGTH_INDEX];
        if (NUMBER_METADATA + 2*(introLength+repeatLength) != ccf.length)
            throw new InvalidArgumentException("Inconsistent length in CCF (claimed "
                    + (introLength + repeatLength) + " pairs, was " + (ccf.length - NUMBER_METADATA)/2 + " pairs).");
        IrSignal irSignal = null;

        int index = NUMBER_METADATA;
        switch (type) {
            case LEARNED_CODE: // 0x0000
            case LEARNED_UNMODULATED_CODE: // 0x0100
                irSignal = Pronto.parse(ccf);
                break;

            case RC5_CODE: // 0x5000:
                if (repeatLength != 1)
                    throw new InvalidArgumentException("wrong repeat length");
                irp = RC5_IRP;
                D = ccf[index++];
                F = ccf[index++];
                break;

            case RC5X_CODE: // 0x5001:
                if (repeatLength != 2)
                    throw new InvalidArgumentException("wrong repeat length");
                irp = RC5X_IRP;
                D = ccf[index++];
                S = ccf[index++];
                F = ccf[index++];
                break;

            case RC6_CODE: // 0x6000:
                if (repeatLength != 1)
                    throw new InvalidArgumentException("wrong repeat length");
                irp = RC6_IRP;
                D = ccf[index++];
                F = ccf[index++];
                break;

            case NEC1_CODE: // 0x900a:
                if (repeatLength != 1)
                    throw new InvalidArgumentException("wrong repeat length");
                irp = NEC1_IRP;
		D = ccf[index] >> 8;
                S = ccf[index++] & 0xff;
		F = ccf[index] >> 8;
		int cmd_chk = 0xff - (ccf[index++] & 0xff);
		if (F != cmd_chk)
		    throw new InvalidArgumentException("checksum erroneous");
                break;

            default:
                throw new InvalidArgumentException("CCF type 0x" + Integer.toHexString(type) + " not supported");
        }

        if (irSignal == null) {
            NameEngine nameEngine = new NameEngine(3);
            try {
                nameEngine.define("D", D);
                if (S != null)
                    nameEngine.define("S", S);
                nameEngine.define("F", F);
                Protocol protocol = new Protocol(irp);
                irSignal = protocol.toIrSignal(nameEngine);
            } catch (DomainViolationException ex) {
                logger.log(Level.SEVERE, "{0}", ex.getMessage());
                throw new InvalidArgumentException(ex);
            } catch (IrpInvalidArgumentException | NameUnassignedException | ArithmeticException | InvalidNameException | UnsupportedRepeatException ex) {
                throw new ThisCannotHappenException(ex);
            }
        }
        return irSignal;
    }

    /**
     * Equivalent to parse(String) followed by parse(IrString).
     *
     * @param longString
     * @return
     * @throws InvalidArgumentException
     * @throws org.harctoolbox.ircore.Pronto.NonProntoFormatException
     */
    public static String long2short(String longString) throws InvalidArgumentException, NonProntoFormatException {
        IrSignal irSignal = parse(longString);
        return toString(irSignal);
    }

    /**
     * Creates a new IrSignals by interpreting its argument as CCF string.
     * @param hexstring String in Pronto hex format
     * @return IrSignal
     * @throws InvalidArgumentException
     * @throws org.harctoolbox.ircore.Pronto.NonProntoFormatException
     */
    public static IrSignal parse(String hexstring) throws InvalidArgumentException, NonProntoFormatException {
        int[] ccf;
        try {
            ccf = Pronto.parseAsInts(hexstring);
        } catch (NumberFormatException ex) {
            throw new InvalidArgumentException("Non-parseable CCF strings");
        }
        if (ccf == null)
            throw new InvalidArgumentException("Invalid CCF strings");

        return parse(ccf);
    }

    /**
     * Computes the "short" Pronto form of some signals, given by protocol number and parameter values.
     *
     * @param protocolName Name of protocol, presently "rc5", "rc5x", "rc6", and "nec1" are recognized.
     * @param D As in protocol definition
     * @param S As in protocol definition
     * @param F As in protocol definition
     * @return CCF as string, or null on failure.
     * @throws org.harctoolbox.ircore.InvalidArgumentException
     */
    public static String toString(String protocolName, Long D, Long S, Long F) throws InvalidArgumentException {
        int[] ccf = toArray(protocolName, D, S, F);
        return toString(ccf);
    }

    /**
     * Computes the "short" Pronto form of some signals, if possible,.
     *
     * @param irSignal
     * @param fallback If true, if no short form, return the long form, otherwise null.
     * @return CCF as string, or null on failure.
     */
    public static String toString(IrSignal irSignal, boolean fallback) {
        Decoder.SimpleDecodesSet decodes = decoder.decodeIrSignal(irSignal);
        if (decodes.isEmpty())
            return fallback ? Pronto.toString(irSignal) : null;
        Decoder.Decode decode = decodes.first();
        return toString(decode);
    }

    /**
     * Computes the "short" Pronto form of some signals, if possible,.
     *
     * @param irSignal
     * @return CCF as string, or null on failure.
     */
    public static String toString(IrSignal irSignal) {
        return toString(irSignal, true);
    }

    private static String toString(Decoder.Decode decode) {
        try {
            Map<String, Long> map = decode.getMap();
            return toString(decode.getName(), map.get("D"), map.get("S"), map.get("F"));
        } catch (InvalidArgumentException ex) {
            logger.log(Level.SEVERE, null, ex);
            throw new ThisCannotHappenException();
        }
    }

    /**
     * Computes the "short" Pronto form of some signals, given by protocol number and parameter values.
     *
     * @param protocolName Name of protocol, presently "rc5", "rc5x", "rc6", and "nec1".
     * @param D As in protocol definition
     * @param S As in protocol definition
     * @param F As in protocol definition
     * @return integer array of short CCF, or null om failure.
     * @throws org.harctoolbox.ircore.InvalidArgumentException
     */
    @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
    private static int[] toArray(String protocolName, Long D, Long S, Long F) throws InvalidArgumentException {
        int index = 0;
        if (protocolName.equalsIgnoreCase(RC5_NAME)) {
            if (D > 31 || S != null || F > 127)
                throw new InvalidArgumentException("Invalid parameters");

            int[] result = new int[6];
            result[index++] = RC5_CODE;
            result[index++] = RC5_FREQUENCY;
            result[index++] = 0;
            result[index++] = 1;
            result[index++] = D.intValue();
            result[index++] = F.intValue();

            return result;
        } else if (protocolName.equalsIgnoreCase(RC5X_NAME)) {
            if (D > 31 || S > 127 || S < 0 || F > 63)
                throw new InvalidArgumentException("Invalid parameters");

            int[] result = new int[8];
            result[index++] = RC5X_CODE;
            result[index++] = RC5X_FREQUENCY;
            result[index++] = 0;
            result[index++] = 2;
            result[index++] = D.intValue();
            result[index++] = S.intValue();
            result[index++] = F.intValue();
            result[index++] = 0;

            return result;
        } else if (protocolName.equalsIgnoreCase(RC6_NAME)) {
            if (D > 255 || S != null || F > 255)
                throw new InvalidArgumentException("Invalid parameters");

            int[] result = new int[6];
            result[index++] = RC6_CODE;
            result[index++] = RC6_FREQUENCY;
            result[index++] = 0;
            result[index++] = 1;
            result[index++] = D.intValue();
            result[index++] = F.intValue();

            return result;
        } else if (protocolName.equalsIgnoreCase(NEC1_NAME)) {
            if (D > 255 || (S != null && S > 255) || F > 255)
                throw new InvalidArgumentException("Invalid parameters");

            int[] result = new int[6];
            result[index++] = NEC1_CODE;
            result[index++] = NEC1_FREQUENCY;
            result[index++] = 0;
            result[index++] = 1;
            result[index++] = (int)((D << 8) + ((S != null && S >= 0) ? S : (0xff - D)));
            result[index++] = (int)((F << 8) + (0xff - F));

            return result;
        } else {
            return new int[0];
        }
    }

    private ShortPronto() {
    }
}
