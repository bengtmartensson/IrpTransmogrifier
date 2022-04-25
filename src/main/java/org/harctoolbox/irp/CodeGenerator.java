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

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.harctoolbox.ircore.IrCoreUtils;

public abstract class CodeGenerator {

    private static final Logger logger = Logger.getLogger(CodeGenerator.class.getName());

    public abstract ItemCodeGenerator newItemCodeGenerator(String name);

    public ItemCodeGenerator newItemCodeGenerator(Object object) {
        return newItemCodeGenerator(object.getClass().getSimpleName());
    }

    public String render(String name) {
        return newItemCodeGenerator(name).render();
    }

    public abstract String fileName(String protocolName);

    public boolean isAbstract() {
        return Boolean.parseBoolean(render("IsAbstract"));
    }

    public boolean manyProtocolsInOneFile() {
        return Boolean.parseBoolean(render("ManyProtocolsInOneFile"));
    }

    public void generate(Collection<String> protocolNames, IrpDatabase irpDatabase, File directory, boolean inspect, Map<String, String> parameters,
            Double absoluteTolerance, Double relativeTolerance, Double frequencyTolerance, String tool, String toolVersion, String commandLine) throws IOException, UnknownProtocolException, InvalidNameException, UnsupportedRepeatException, NameUnassignedException, IrpInvalidArgumentException {
        if (isAbstract())
            throw new IrpInvalidArgumentException("This target cannot generete code since it is declared abstract.");
        if (directory == null || !directory.isDirectory() || !directory.canWrite())
            throw new IOException("directory must be a writeable directory");

        STCodeGenerator.trackCreationEvents(inspect); // ???

        for (String protocolName : protocolNames) {
            NamedProtocol protocol;
            protocol = irpDatabase.getNamedProtocol(protocolName);
            String protocolCName = IrCoreUtils.toCName(protocol.getName());
            String filename = new File(directory + "/" + fileName(protocolCName)).getCanonicalPath();
            try (PrintStream out = IrCoreUtils.getPrintStream(filename)) {
                generate(protocol, out, true, inspect, parameters, absoluteTolerance, relativeTolerance, frequencyTolerance, tool, toolVersion, commandLine);
                logger.log(Level.INFO, "Wrote {0}", filename);
            }
        }
    }


    public void generate(Collection<String> protocolNames, IrpDatabase irpDatabase, PrintStream out, boolean inspect, Map<String, String> parameters,
            Double absoluteTolerance, Double relativeTolerance, Double frequencyTolerance, String tool, String toolVersion, String commandLine) throws IrpInvalidArgumentException {
        if (protocolNames == null || protocolNames.isEmpty())
            throw new IrpInvalidArgumentException("protocolNames cannot be null or empty");
        if (isAbstract())
            throw new IrpInvalidArgumentException("This target cannot generete code since it is declared abstract.");
        if (!manyProtocolsInOneFile() && protocolNames.size() > 1)
            throw new IrpInvalidArgumentException("This target cannot generate more than one protocol in one file");

        setInspect(inspect);

        generateFileBegin(out, tool, toolVersion, commandLine);

        protocolNames.forEach((protocolName) -> {
            try {
                generate(protocolName, irpDatabase, out, false, inspect, parameters, absoluteTolerance, relativeTolerance, frequencyTolerance, tool, toolVersion, commandLine);
            } catch (NameUnassignedException | UnknownProtocolException | ArithmeticException | InvalidNameException | UnsupportedRepeatException | IrpInvalidArgumentException ex) {
                logger.log(Level.WARNING, "{0}, ignoring this protol", ex.getMessage());
            }
        });
        generateFileEnd(out);
    }

    private void generateFileBegin(PrintStream out, String tool, String toolVersion, String commandLine) {
        ItemCodeGenerator itemCodeGenerator = newItemCodeGenerator("FileBegin");
        Map<String, Object> map = new HashMap<>(5);
        map.put("date", new Date().toString());
        map.put("userName", System.getProperty("user.name"));
        map.put("tool", tool);
        map.put("toolVersion", toolVersion);
        map.put("commandLineArguments", commandLine);
        itemCodeGenerator.addAggregateList("GenerateData", map);
        out.println(itemCodeGenerator.render());
    }

    private void generateFileEnd(PrintStream out) {
        ItemCodeGenerator itemCodeGenerator = newItemCodeGenerator("FileEnd");
        out.print(itemCodeGenerator.render());
    }

    private void generate(String protocolName, IrpDatabase irpDatabase, PrintStream out, boolean printPostAndPre, boolean inspect, Map<String, String> parameters,
            Double absoluteTolerance, Double relativeTolerance, Double frequencyTolerance, String tool, String toolVersion, String commandLine) throws UnknownProtocolException, InvalidNameException, UnsupportedRepeatException, IrpInvalidArgumentException, NameUnassignedException {
        NamedProtocol protocol = irpDatabase.getNamedProtocol(protocolName);
        generate(protocol, out, printPostAndPre, inspect, parameters, absoluteTolerance, relativeTolerance, frequencyTolerance, tool, toolVersion, commandLine);
    }

    private void generate(NamedProtocol protocol, PrintStream out, boolean printPostAndPre, boolean inspect, Map<String, String> parameters,
            Double absoluteTolerance, Double relativeTolerance, Double frequencyTolerance, String tool, String toolVersion, String commandLine) {
        if (printPostAndPre)
            generateFileBegin(out, tool, toolVersion, commandLine);
        ItemCodeGenerator code = protocol.code(this, parameters, absoluteTolerance, relativeTolerance, frequencyTolerance);// contains a trailing newline
        out.println(code.render());
        if (printPostAndPre)
            generateFileEnd(out);
        if (inspect)
            code.inspectAndWait();
    }

    public abstract void setInspect(boolean inspect);
}
