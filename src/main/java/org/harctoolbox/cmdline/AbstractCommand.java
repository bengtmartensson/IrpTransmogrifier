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
import java.util.Locale;
import org.harctoolbox.ircore.IrCoreUtils;

public abstract class AbstractCommand {

    @Parameter(names = {"-h", "-?", "--help"}, help = true, description = "Print help for this command.")
    @SuppressWarnings("FieldMayBeFinal")
    private boolean help = false;

    @Parameter(names = {"--describe"}, help = true, description = "Print a possibly longer documentation for the present command.")
    @SuppressWarnings("FieldMayBeFinal")
    private boolean description = false;

    /**
     * Returns a possibly longer documentation of the command.
     *
     * @return Documentation string;
     */
    // Please override!
    public String description() {
        return "Documentation for this command has not yet been written.\nUse --help for the syntax of the command.";
    }

    public boolean process(CmdLineProgram instance) {
        if (help) {
            instance.usage(this.getClass().getSimpleName().substring(7).toLowerCase(Locale.US));
            return true;
        }
        if (description) {
            IrCoreUtils.trivialFormatter(instance.getOutputStream(), description(), 65);
            return true;
        }
        return false;
    }
}
