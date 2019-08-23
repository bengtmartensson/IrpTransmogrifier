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
import java.io.PrintStream;
import java.util.logging.Logger;
import org.harctoolbox.irp.IrpDatabase;
import org.harctoolbox.irp.Version;

@SuppressWarnings("PublicField")

@Parameters(commandNames = {"version"}, commandDescription = "Report version and license.")
public class CommandVersion extends AbstractCommand {

    private static final Logger logger = Logger.getLogger(CommandCode.class.getName());

    @Parameter(names = {"-s", "--short"}, description = "Issue only the version number of the program proper.")
    @SuppressWarnings("FieldMayBeFinal")
    private boolean shortForm = false;

    @Override
    public String description() {
        return "This command returns the version. and licensing information for the program.";
    }

    public void version(PrintStream out, CommandCommonOptions commandLineArgs, IrpDatabase irpDatabase) {
        if (shortForm || commandLineArgs.quiet)
            out.println(Version.version);
        else {
            out.println(Version.versionString);
            //setupDatabase();
            out.println("Database: " + (commandLineArgs.configFile != null ? commandLineArgs.configFile : "")
                    + " version: " + irpDatabase.getConfigFileVersion());

            out.println("JVM: " + System.getProperty("java.vendor") + " " + System.getProperty("java.version") + " " + System.getProperty("os.name") + "-" + System.getProperty("os.arch"));
            out.println();
            out.println(Version.licenseString);
        }
    }
}
