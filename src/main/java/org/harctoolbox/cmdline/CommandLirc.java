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

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.irp.NonUniqueBitCodeException;
import org.harctoolbox.lirc.LircCommand;
import org.harctoolbox.lirc.LircConfigFile;
import org.harctoolbox.lirc.LircIrp;
import org.harctoolbox.lirc.LircRemote;

@SuppressWarnings("FieldMayBeFinal")

@Parameters(commandNames = {"lirc"}, commandDescription = "Convert Lirc configuration files to IRP form.")
public class CommandLirc extends AbstractCommand {

    private static final Logger logger = Logger.getLogger(CommandLirc.class.getName());

    @Parameter(names = {"-c", "--commands"}, description = "Also list the commands if the remotes.")
    private boolean commands = false;

    @Parameter(names = {"-r", "--radix"}, hidden = true, description = "Radix for outputting result, default 16.", validateWith = Radix.class) // Too much...?
    private int radix = 16;

    @Parameter(description = "Lirc config files/directories/URLs; empty for <stdin>.", required = false)
    private List<String> files = new ArrayList<>(8);

    @Override
    public String description() {
        return "This command reads a Lirc configuration, from a file, directory, or an URL, "
                + "and computes a correponding IRP form. "
                + "No attempt is made to clean up, for example by rounding times or "
                + "finding a largest common divider.";
    }

    public void lirc(PrintStream out, String encoding) throws IOException {
        List<LircRemote> list;
        if (files.isEmpty())
            list = LircConfigFile.readRemotes(new InputStreamReader(System.in, /*commandLineArgs.*/encoding));
        else {
            list = new ArrayList<>(files.size());
            for (String f : files) {
                list.addAll(LircConfigFile.readRemotes(f, /*commandLineArgs.*/encoding));
            }
        }

        for (LircRemote rem : list) {
            out.print(rem.getName() + ":\t");
            try {
                out.println(LircIrp.toProtocol(rem).toIrpString(radix, false));
            } catch (LircIrp.RawRemoteException ex) {
                out.println("raw remote");
            } catch (LircIrp.LircCodeRemoteException ex) {
                out.println("lirc code remote, does not contain relevant information.");
            } catch (NonUniqueBitCodeException ex) {
                out.println("Non-unique bitcodes");
            }
            if (commands) {
                for (LircCommand cmd : rem.getCommands()) {
                    out.print(cmd.getName() + ":\t");
                    cmd.getCodes().forEach((x) -> {
                        out.print(IrCoreUtils.radixPrefix(radix) + Long.toUnsignedString(x, radix) + " ");
                    });
                    out.println();
                }
            }
        }
    }
}
