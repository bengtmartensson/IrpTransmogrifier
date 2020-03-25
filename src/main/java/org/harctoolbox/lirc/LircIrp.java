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

package org.harctoolbox.lirc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.ircore.ThisCannotHappenException;
import org.harctoolbox.irp.BareIrStream;
import org.harctoolbox.irp.BitDirection;
import org.harctoolbox.irp.BitSpec;
import org.harctoolbox.irp.BitspecIrstream;
import org.harctoolbox.irp.Duration;
import org.harctoolbox.irp.Extent;
import org.harctoolbox.irp.FiniteBitField;
import org.harctoolbox.irp.Flash;
import org.harctoolbox.irp.Gap;
import org.harctoolbox.irp.GeneralSpec;
import org.harctoolbox.irp.InvalidNameException;
import org.harctoolbox.irp.IrStream;
import org.harctoolbox.irp.IrStreamItem;
import org.harctoolbox.irp.NameEngine;
import org.harctoolbox.irp.NonUniqueBitCodeException;
import org.harctoolbox.irp.ParameterSpec;
import org.harctoolbox.irp.ParameterSpecs;
import org.harctoolbox.irp.Protocol;
import org.harctoolbox.irp.RepeatMarker;

/**
 * This class generates an IRP {@link Protocol} from the {@link LircRemote} given as argument.
 */
public final class LircIrp {

    /**
     * A little test/demo main that reads the files given as arguments,
     * and dumps the outcome on stdout.
     * @param args
     */
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public static void main(String[] args) {
        int radix = 16;
        try {
            Map<String, LircRemote> remotes = new LinkedHashMap<>(args.length);
            for (String file : args)
                LircConfigFile.readConfig(remotes, new File(file));

            for (LircRemote remote : remotes.values()) {
                LircIrp irp = new LircIrp(remote);
                System.out.println(irp.protocol.toIrpString(radix, false));
                remote.getCommands().stream().map((command) -> {
                    System.out.print(command.getName() + "\t");
                    return command;
                }).map((LircCommand command) -> {
                    command.getCodes().forEach((code) -> {
                        System.out.print("0x" + Long.toHexString(code) + " ");
                    });
                    return command;
                }).forEachOrdered((_item) -> {
                    System.out.println();
                });
                System.out.println();
            }
        } catch (IOException | RawRemoteException | LircCodeRemoteException | NonUniqueBitCodeException ex) {
            Logger.getLogger(LircIrp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static Protocol toProtocol(LircRemote lircRemote) throws RawRemoteException, LircCodeRemoteException, NonUniqueBitCodeException {
        return new LircIrp(lircRemote).protocol;
    }

    private final LircRemote remote;
    private Protocol protocol;
    private NameEngine nameEngine;
    private GeneralSpec generalSpec;
    private BitSpec bitSpec;
    private IrStream body;

    private LircIrp(LircRemote remote) throws RawRemoteException, LircCodeRemoteException, NonUniqueBitCodeException {
        this.remote = remote;
        if (remote.isRaw())
            throw new RawRemoteException(remote.getName());
        if (!remote.isMode2())
            throw new LircCodeRemoteException(remote.getName());

        setupBitSpec();
        setupBody();
        BitspecIrstream bitspecIrstream = new BitspecIrstream(bitSpec, body);
        setupGeneralSpec();
        setupNameEngine();
        ParameterSpecs parameterSpecs = mkParameterSpecs();

        protocol = new Protocol(generalSpec, bitspecIrstream, nameEngine, parameterSpecs);
    }

    private void setupGeneralSpec() {
        BitDirection bitDirection = BitDirection.newBitDirection(remote.hasFlag("REVERSE"));
        Double frequency = remote.getUnaryParameters("frequency") != null ? remote.getUnaryParameters("frequency").doubleValue() : GeneralSpec.DEFAULT_FREQUENCY;
        Double dutyCycle = remote.getUnaryParameters("duty_cycle") != null ? IrCoreUtils.percent2real(remote.getUnaryParameters("duty_cycle")) : null;
        generalSpec = new GeneralSpec(bitDirection, null, frequency, dutyCycle);
    }

    private List<IrStreamItem> lengthTwoBareIrStream(String key) {
        return lengthTwoBareIrStream(false, key);
    }

    private List<IrStreamItem> lengthTwoBareIrStream(boolean invert, String key) {
        return lengthTwoBareIrStream(invert, remote.getBinaryParameters(key));
    }

    private List<IrStreamItem> lengthTwoBareIrStream(LircRemote.Pair pair) {
        return lengthTwoBareIrStream(false, pair);
    }

    private List<IrStreamItem> lengthTwoBareIrStream(boolean invert, LircRemote.Pair pair) {
        if (pair == null || pair.isTrivial())
            return new ArrayList<>(0);

        return lengthXBareIrStream(invert, pair.getFirst(), pair.getSecond());
    }

    private List<IrStreamItem> lengthXBareIrStream(boolean invert, Long... vars) {
        List<IrStreamItem> list = new ArrayList<>(vars.length);
        for (int i = 0; i < vars.length; i++) {
            boolean odd = i % 2 != 0;
            Duration duration = odd == invert ? new Flash(vars[i]) : new Gap(vars[i]);
            list.add(duration);
        }
        return list;
    }

    private List<IrStreamItem> bareIrStreamFlash(String key) {
        return lengthOneBareIrStream(remote.getUnaryParameters(key));
    }

    private List<IrStreamItem> lengthOneBareIrStream(Long value) {
        if (value == null || value == 0L)
            return new ArrayList<>(0);

        return lengthXBareIrStream(false, value);
    }

    private void setupBitSpec() throws NonUniqueBitCodeException {
        ArrayList<BareIrStream> list = new ArrayList<>(remote.hasFlag("RCMM") ? 4 : 2);
        boolean biphaseNonInvert = remote.hasFlag("RC6");
        boolean biphaseInvert = remote.hasFlag("RC5") || remote.hasFlag("SHIFT_ENC");
        BareIrStream zero = new BareIrStream(lengthTwoBareIrStream(biphaseNonInvert, "zero"));
        list.add(zero);
        BareIrStream one  = new BareIrStream(lengthTwoBareIrStream(biphaseInvert, "one"));
        list.add(one);
        if (remote.hasFlag("RCMM")) {
            BareIrStream two = new BareIrStream(lengthTwoBareIrStream(false, "two"));
            list.add(two);
            BareIrStream three  = new BareIrStream(lengthTwoBareIrStream(false, "three"));
            list.add(three);
        }
        bitSpec = new BitSpec(list);
    }

    private List<IrStreamItem> mkBitField(String name, String lengthName) throws InvalidNameException {
        List<IrStreamItem> result = new ArrayList<>(1);
        Long value = remote.getUnaryParameters(name);
        Long length = remote.getUnaryParameters(lengthName);
        if (value != null && length > 0)
            result.add(new FiniteBitField(name, length));
        return result;
    }

    private List<IrStreamItem> ending() {
        List<IrStreamItem> list = new ArrayList<>(1);
        Long value = remote.getUnaryParameters("gap");
        Duration duration = remote.hasFlag("CONST_LENGTH") ? new Extent(value) : new Gap(value);
        list.add(duration);
        return list;
    }

    @SuppressWarnings("empty-statement")
    private void setupBody() {
        try {
            List<IrStreamItem> list = new ArrayList<>(8);
            list.addAll(lengthTwoBareIrStream("header"));
            list.addAll(bareIrStreamFlash("plead"));
            list.addAll(mkBitField("pre_data", "pre_data_bits"));
            list.addAll(lengthTwoBareIrStream("pre"));
            list.add(new FiniteBitField("F", remote.getUnaryParameters("bits")));
            list.addAll(lengthTwoBareIrStream("post"));
            list.addAll(mkBitField("post_data", "post_data_bits"));
            list.addAll(bareIrStreamFlash("ptrail"));
            list.addAll(lengthTwoBareIrStream("foot"));
            list.addAll(ending());

            int outerRepeatMax = Integer.MAX_VALUE;
            Long repeatMin = remote.getUnaryParameters("min_repeat");
            LircRemote.Pair repeatPair = remote.getBinaryParameters("repeat");
            RepeatMarker repeatMarker;
            if ((repeatPair != null && !repeatPair.isTrivial()) || remote.hasFlag("NO_HEAD_REP") || remote.hasFlag("NO_FOOT_REP")) {
                outerRepeatMax = repeatMin != null ? repeatMin.intValue() : 1;
                List<IrStreamItem> repeatList = new ArrayList<>(4);
                if (remote.hasFlag("REPEAT_HEADER"))
                    repeatList.addAll(lengthTwoBareIrStream("header"));
                if (repeatPair != null)
                    repeatList.addAll(lengthTwoBareIrStream(repeatPair));

                else
                    ;
                repeatList.addAll(bareIrStreamFlash("ptrail"));
                repeatList.addAll(ending());

                IrStream repeat = new IrStream(new BareIrStream(repeatList), new RepeatMarker(0, Integer.MAX_VALUE));
                list.add(repeat);
                repeatMarker = new RepeatMarker();
            } else {
                repeatMarker = new RepeatMarker(repeatMin != null ? repeatMin.intValue() + 1 : 0, Integer.MAX_VALUE);
            }

            BareIrStream stream = new BareIrStream(list);

            body = new IrStream(stream, repeatMarker);
        } catch (InvalidNameException ex) {
            throw new ThisCannotHappenException(ex);
        }
    }

    private void setupNameEngine() {
        nameEngine = new NameEngine(2);
        try {
            appendName("pre_data");
            appendName("post_data");
        } catch (InvalidNameException ex) {
            throw new ThisCannotHappenException(ex);
        }
    }

    private void appendName(String name) throws InvalidNameException {
        Long value = remote.getUnaryParameters(name);
        if (value != null)
            nameEngine.define(name, value);
    }

    private ParameterSpecs mkParameterSpecs() {
        List<ParameterSpec> list = new ArrayList<>(1);
        ParameterSpec spec;
        try {
            spec = new ParameterSpec("F", false, remote.getUnaryParameters("bits").intValue());
            list.add(spec);
            return new ParameterSpecs(list);
        } catch (InvalidNameException ex) {
            throw new ThisCannotHappenException(ex);
        }
    }

    public static class RawRemoteException extends Exception {

        public RawRemoteException(String name) {
            super("LircRemote \"" + name + "\" is a raw remote.");
        }
    }

    public static class LircCodeRemoteException extends Exception {

        LircCodeRemoteException(String name) {
            super("LircRemote \"" + name + "\" is a LircCode remote (i.e. it does not contain timing information).");
        }
    }
}
/*
List of all flags in lircd.conf:

const struct flaglist all_flags[] = {
	{ "RAW_CODES",	   RAW_CODES	 },
	{ "RC5",	   RC5		 },
	{ "SHIFT_ENC",	   SHIFT_ENC	 }, / * obsolete * /
	{ "RC6",	   RC6		 },
	{ "RCMM",	   RCMM		 },
	{ "SPACE_ENC",	   SPACE_ENC	 },
	{ "SPACE_FIRST",   SPACE_FIRST	 },
	{ "GOLDSTAR",	   GOLDSTAR	 },
	{ "GRUNDIG",	   GRUNDIG	 },
	{ "BO",		   BO		 },
	{ "SERIAL",	   SERIAL	 },
	{ "XMP",	   XMP		 },

	{ "REVERSE",	   REVERSE	 },
	{ "NO_HEAD_REP",   NO_HEAD_REP	 },
	{ "NO_FOOT_REP",   NO_FOOT_REP	 },
	{ "CONST_LENGTH",  CONST_LENGTH	 }, / * remember to adapt warning
					     * message when changing this * /
	{ "REPEAT_HEADER", REPEAT_HEADER },
	{ NULL,		   0		 },
};*/