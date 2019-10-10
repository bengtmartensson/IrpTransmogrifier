package org.harctoolbox.example;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.harctoolbox.ircore.InvalidArgumentException;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.ModulatedIrSequence;
import org.harctoolbox.ircore.Pronto;
import org.harctoolbox.ircore.ThisCannotHappenException;
import org.harctoolbox.irp.Decoder;
import org.harctoolbox.irp.Decoder.Decode;
import org.harctoolbox.irp.Expression;
import org.harctoolbox.irp.InvalidNameException;
import org.harctoolbox.irp.IrpDatabase;
import org.harctoolbox.irp.IrpParseException;
import org.harctoolbox.irp.NameEngine;
import org.harctoolbox.irp.NameUnassignedException;

// This example demonstrates several concepts.
// It first sets up a decoder from the Irp Database.
// It shows how to get information from the extra parameters in IrpProtocols.xml.
// and how to compute extra values give as Irp Expressions.

// See http://www.hifi-remote.com/forums/viewtopic.php?t=101943&start=11

public class DecoderAPIExample {

    @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch", "CallToPrintStackTrace", "UseSpecificCatch"})
    public static void main(String[] args) {
        try {
            DecoderAPIExample decoder = new DecoderAPIExample();

            if (args.length != 0) {
                String string = String.join(" ", args);
                decoder.decode(string);
            } else {
                decoder.decode("0000 0067 0000 0010 0060 0018 0018 0018 0018 0018 0018 0018 0030 0018 0030 0018 0018 0018 0030 0018 0018 0018 0030 0018 0018 0018 0030 0018 0030 0018 0018 0018 0018 0018 0018 0335");
                decoder.decode("0000 006C 0022 0002 015B 00AD 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0041 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0041 0016 0016 0016 0041 0016 0041 0016 0041 0016 0016 0016 0041 0016 0041 0016 05F7 015B 0057 0016 0E6C");
                decoder.decode("0000 006D 001A 0000 0157 00AC 0013 0055 0013 00AC 0013 0055 0013 00AC 0013 00AC 0013 0055 0013 0055 0013 0055 0013 0055 0013 0055 0013 0055 0013 0055 0013 00AC 0013 0055 0013 00AC 0013 0055 0013 0498 0157 0055 0013 0D24 0157 0055 0013 0D24 0157 0055 0013 0D24 0157 0055 0013 1365");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private final IrpDatabase irpDatabase;
    private Decoder decoder;
    private Decoder.DecoderParameters decoderParams;

    /**
     * Map from protocol names to executor (identified by String (to avoid parsing stuff with/without colons :-))
     */
    private Map<String, String> protocolToExecutor;

    /**
     * For an executer, a collection of additional parameter to compute.
     */
    private Map<String, NameEngine> executorParams;

    @SuppressWarnings("CollectionWithoutInitialCapacity")
    public DecoderAPIExample() throws IOException, IrpParseException, InvalidNameException {
        irpDatabase = new IrpDatabase((String) null);
        if (irpDatabase.size() == 0)
            throw new ThisCannotHappenException("Empty protocol data base!");
        String s = irpDatabase.getFirstProperty("Aiwa", "uei-executor");
        decoder = new Decoder(irpDatabase);
        decoderParams = new Decoder.DecoderParameters();
        decoderParams.setRemoveDefaultedParameters(false);

        protocolToExecutor = new HashMap<>();
        protocolToExecutor.put("nec1", "005A");

        executorParams = new HashMap<>();
        NameEngine exec005AParams = new NameEngine("{hex = ~F:-8, rawData = D:-32 | (S<<8):-32 | (F<<16):-32 | (~F<<24):-32}");
        executorParams.put("005A", exec005AParams);
    }

    public void decode(String pronto) throws Pronto.NonProntoFormatException, InvalidArgumentException, NameUnassignedException {
        decode(Pronto.parseLoose(pronto));
    }

    public void decode(IrSignal irSignal) throws NameUnassignedException {
        // First try to decode as a signal,...
        Decoder.SimpleDecodesSet sigDecodes = decoder.decodeIrSignal(irSignal, decoderParams);
        for (Decode decode : sigDecodes) {
            extendParameters(decode);
            System.out.println(decode);
        }

        // ... if that fails, try as Sequence.
        if (sigDecodes.isEmpty()) {
            ModulatedIrSequence seq = irSignal.toModulatedIrSequence();
            decode(seq);
        }
    }

    public void decode(ModulatedIrSequence irSequence) {
        Decoder.DecodeTree decodes = decoder.decode(irSequence, decoderParams);
        System.out.println(decodes);
    }

    private void extendParameters(Decode decode) throws NameUnassignedException {
        String protocolName = decode.getName().toLowerCase(Locale.US);
        NameEngine originalParameters = new NameEngine(decode.getMap());

        String executor = irpDatabase.getFirstProperty(protocolName, "uei-executor");
        if (executor == null)
            executor = protocolToExecutor.get(protocolName);

        if (executor != null) {
            NameEngine executorParameters = this.executorParams.get(executor);
            if (executorParameters != null) {
                for (Map.Entry<String, Expression> entry : executorParameters) {
                    String name = entry.getKey();
                    Expression exp = entry.getValue();
                    long value = exp.toLong(originalParameters);
                    decode.getMap().put(name, value);
                }
            }
        }
    }
}
