package org.harctoolbox.analyze;

import org.harctoolbox.ircore.IrCoreException;

public class DecodeException extends IrCoreException {

    public DecodeException(int i) {
        super("Decode failed at token " + Integer.toString(i));
    }

}
