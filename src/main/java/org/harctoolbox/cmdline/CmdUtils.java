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

package org.harctoolbox.cmdline;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import static org.harctoolbox.ircore.IrCoreUtils.DEFAULT_CHARSET;
import static org.harctoolbox.ircore.IrCoreUtils.DEFAULT_CHARSET_NAME;
import org.harctoolbox.ircore.ThisCannotHappenException;

public class CmdUtils {


   public static void checkForOption(String functionName, List<String> args) throws UsageException {
        if (args != null && !args.isEmpty() && args.get(0).startsWith("-"))
            throw new UsageException("Unknown option to " + functionName + ": " + args.get(0));
   }

   // TODO: split on "" and ''.
   public static String[] shellSplit(String string) {
       return string.split("\\s+");
   }

    public static String execute(Class<? extends CmdLineProgram> clazz, String[] args) {
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        try (PrintStream outStream = new PrintStream(outBytes, false, DEFAULT_CHARSET_NAME)) {
            Constructor<? extends CmdLineProgram> constructor = clazz.getConstructor(PrintStream.class);
            CmdLineProgram instance = constructor.newInstance(outStream);
            ProgramExitStatus status = instance.run(args);
            if (!status.isSuccess())
                return null;

            outStream.flush();
            return new String(outBytes.toByteArray(), DEFAULT_CHARSET).trim();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | UnsupportedEncodingException ex) {
            // Get here only by programming errors, no need to confuse the use
            throw new ThisCannotHappenException(ex);
        }
    }

    public static ByteArrayOutputStream storeStdErr() {
        ByteArrayOutputStream errBytes = new ByteArrayOutputStream();
        PrintStream errStream;
        try {
            errStream = new PrintStream(errBytes, false, DEFAULT_CHARSET_NAME);
            System.setErr(errStream);
        } catch (UnsupportedEncodingException ex) {
            throw new ThisCannotHappenException();
        }
        return errBytes;
    }

   @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public static String restoreStdErr(ByteArrayOutputStream errBytes) {
        System.err.flush();
        System.err.close();
        try {
            String stderr = errBytes.toString(DEFAULT_CHARSET_NAME);
            System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err), true, DEFAULT_CHARSET_NAME));
            return stderr;
        } catch (UnsupportedEncodingException ex) {
            throw new ThisCannotHappenException();
        }
    }

    private CmdUtils() {
    }
}
