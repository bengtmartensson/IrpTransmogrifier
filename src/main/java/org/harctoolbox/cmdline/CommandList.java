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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.harctoolbox.ircore.ThisCannotHappenException;
import org.harctoolbox.irp.InvalidNameException;
import org.harctoolbox.irp.IrpDatabase;
import org.harctoolbox.irp.IrpInvalidArgumentException;
import org.harctoolbox.irp.IrpUtils;
import org.harctoolbox.irp.NameUnassignedException;
import org.harctoolbox.irp.NamedProtocol;
import org.harctoolbox.irp.UnknownProtocolException;
import org.harctoolbox.irp.UnsupportedRepeatException;
import org.harctoolbox.xml.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;

@SuppressWarnings("FieldMayBeFinal")

@Parameters(commandNames = {"list"}, commandDescription = "List protocols and their properties.")
public class CommandList extends AbstractCommand {

    private static final Logger logger = Logger.getLogger(CommandList.class.getName());

    // not yet implemented
    //@Parameter(names = { "-b", "--browse" }, description = "Open the protoocol data base file in the browser")
    //private boolean browse = false;
    @Parameter(names = {"-a", "--all"}, description = "Implies (almost) all of the \"list xxx\"-options.")
    private boolean all = false;

    @Parameter(names = {"--check-sorted"}, description = "Check if the protocol are alphabetically.")
    private boolean checkSorted = false;

    @Parameter(names = {"-c", "--classify"}, description = "Classify the protocol(s).")
    private boolean classify = false;

    @Parameter(names = {"--documentation"}, description = "Print (possible longer) documentation, as a dumb rendering of the HTML documenation.")
    private boolean documentation = false;

    @Parameter(names = {"-d", "--dump"}, description = "Print the IRP data base as DOC tree stringified, including initial XML comments.")
    private boolean dump = false;

    @Parameter(names = {"--gui", "--display"}, description = "Display parse diagram.")
    private boolean gui = false;

    @Parameter(names = {"--html"}, description = "Print (possible longer) documentation as HTML.")
    private boolean html = false;

    @Parameter(names = {"-i", "--irp"}, description = "List IRP form, as given in the database (unparsed, i.e. preserving comments and whitespace, not taking --radix into account).")
    private boolean irp = false;

    // not really useful, therefore hidden
    @Parameter(names = {"--istring"}, hidden = true, description = "test toIrpString.")
    private boolean is = false;

    @Parameter(names = {"-m", "--mindiff"}, description = "Compute minimal difference between contained durations.")
    private boolean minDiff = false;

    @Parameter(names = {"-n", "--normalform"}, description = "List the normal form.")
    private boolean normalForm = false;

    // debugging feature, therefore hidden
    @Parameter(names = {"-p", "--parsedirp"}, hidden = true, description = "List IRP form, as parsed.")
    private boolean parsedIrp = false;

    @Parameter(names = {"--prefer-overs"}, description = "List all protocol's prefer-overs, recursively.")
    private boolean preferOvers = false;

    @Parameter(names = {"--name"}, description = "List protocol name, also if --quiet is given.")
    private boolean name = false;

    @Parameter(names = {"-r", "--radix"}, description = "Radix of parameter output.", validateWith = Radix.class)
    private int radix = 10;

    @Parameter(names = {"--stringtree"}, description = "Produce stringtree.")
    private boolean stringTree = false;

    @Parameter(names = {"-w", "--weight"}, description = "Compute weight of the protocols.")
    private boolean weight = false;

    @Parameter(names = {"--warnings"}, description = "Issue warnings for some problematic IRP constructs.")
    private boolean warnings = false;

    @Parameter(names = {"-x", "--xml"}, description = "Like dump, but without XML comments.")
    private boolean xml = false;

    @Parameter(description = "List of protocols (default all)")
    private List<String> protocols = new ArrayList<>(8);

    @Override
    public String description() {
        return "This command list miscellaneous properties of the protocol(s) given as arguments.";
    }

    public void list(PrintStream out, CommandCommonOptions commandLineArgs, IrpDatabase irpDatabase) throws UsageException, InvalidNameException, UnsupportedRepeatException, IrpInvalidArgumentException, NameUnassignedException, UnknownProtocolException, UnsupportedEncodingException, NamedProtocol.TooDeepPreferOversException {
        CmdUtils.checkForOption("list", protocols);
        if (checkSorted) {
            String offender = irpDatabase.checkSorted();
            if (offender == null)
                out.println("Protocol data base is sorted.");
            else
                out.println("Protocol data base is NOT sorted, first offending protocol: " + offender + ".");
            return;
        }

        if (!commandLineArgs.quiet) {
            protocols.stream().filter((protocol) -> (irpDatabase.isAlias(protocol))).forEachOrdered((protocol) -> {
                out.println(protocol + " -> " + irpDatabase.expandAlias(protocol));
            });
        }
        List<String> list = irpDatabase.evaluateProtocols(protocols, commandLineArgs.sort, commandLineArgs.regexp, commandLineArgs.urlDecode);
        if (list.isEmpty())
            throw new UsageException("No protocol matched.");

        if (dump || xml) {
            Document document = irpDatabase.toDocument(list, dump);
            XmlUtils.printDOM(out, document, commandLineArgs.outputEncoding, "Irp Documentation");
            return;
        }

        for (String name : list) {
            String protocolName = irpDatabase.expandAlias(name);
            NamedProtocol protocol;
            try {
                protocol = irpDatabase.getNamedProtocol(protocolName);
                logger.log(Level.FINE, "Protocol {0} parsed", protocolName);
            } catch (UnknownProtocolException ex) {
                logger.log(Level.WARNING, "{0}", ex.getMessage());
                continue;
            } catch (InvalidNameException | NameUnassignedException | IrpInvalidArgumentException | UnsupportedRepeatException ex) {
                logger.log(Level.WARNING, "Unparsable protocol {0}", protocolName);
                continue;
            }

            if (!commandLineArgs.quiet || this.name || all)
                // Use one line for the first, relatively short items
                listProperty(out, "name", irpDatabase.getName(protocolName), commandLineArgs.quiet);

            if (irp || all)
                listProperty(out, "irp", irpDatabase.getIrp(protocolName), commandLineArgs.quiet);

            if (parsedIrp)
                listProperty(out, "parsedirp", protocol.toIrpString(radix), commandLineArgs.quiet);

            if (normalForm)
                try {
                    // already checked it once...
                    listProperty(out, "normal form", irpDatabase.getNormalFormIrp(protocolName, radix), commandLineArgs.quiet);
                } catch (NameUnassignedException | UnknownProtocolException | InvalidNameException | UnsupportedRepeatException | IrpInvalidArgumentException ex) {
                    throw new ThisCannotHappenException(ex);
                }

            if (documentation || all)
                listProperty(out, "documentation", irpDatabase.getDocumentation(protocolName), commandLineArgs.quiet);

            if (html)
                listDocumentFragment(out, protocol.getDocumentation(), commandLineArgs.quiet, commandLineArgs.outputEncoding);

            if (stringTree)
                listProperty(out, "stringTree", protocol.toStringTree(), commandLineArgs.quiet);

            if (is)
                listProperty(out, "irpString", protocol.toIrpString(radix), commandLineArgs.quiet);

            if (gui)
                IrpUtils.showTreeViewer(protocol.toTreeViewer(), "Parse tree for " + protocolName);

            if (weight || all)
                listProperty(out, "Weight", protocol.weight(), commandLineArgs.quiet);

            if (minDiff || all)
                listProperty(out, "minDiff", protocol.minDurationDiff(), commandLineArgs.quiet);

            if (classify || all)
                listProperty(out, "classification", protocol.classificationString(), commandLineArgs.quiet);

            if (warnings || all)
                listProperty(out, "warnings", protocol.warningsString(), commandLineArgs.quiet);

            if (preferOvers || all) {
                out.println("preferovers tree:");
                if (commandLineArgs.quiet && ! this.name && !protocol.preferredOvers().isEmpty())
                    out.println(irpDatabase.getName(protocolName) + ":");
                protocol.dumpPreferOvers(out, irpDatabase);
            }
        }
    }

//    private void listProperty(PrintStream out, String propertyName, DocumentFragment documentFragment, boolean quiet) {
//        if (quiet && propertyName != null)
//            out.print(propertyName + "=");
//        out.println(DumbHtmlRenderer.render(documentFragment));
//    }

    private void listProperty(PrintStream out, String propertyName, String propertyValue, boolean quiet) {
        if (!/*commandLineArgs.*/quiet && propertyName != null)
            out.print(propertyName + "=");
        out.println(propertyValue);
    }

    private void listProperty(PrintStream out, String propertyName, double value, boolean quiet) {
        listProperty(out, propertyName, Math.round(value), quiet);
    }

    private void listProperty(PrintStream out, String propertyName, int value, boolean quiet) {
        listProperty(out, propertyName, Integer.toString(value), quiet);
    }

    private void listProperty(PrintStream out, String propertyName, long value, boolean quiet) {
        listProperty(out, propertyName, Long.toString(value), quiet);
    }

    private void listDocumentFragment(PrintStream out, DocumentFragment fragment, boolean quiet, String encoding) throws UnsupportedEncodingException {
        Objects.requireNonNull(fragment);
        Document document = XmlUtils.wrapDocumentFragment(fragment, "http://www.w3.org/1999/xhtml", "div", "class", "protocol-documentation");
        if (! quiet)
            out.print("html=");
        XmlUtils.printHtmlDOM(out, document, encoding);
        out.println();
    }
}
