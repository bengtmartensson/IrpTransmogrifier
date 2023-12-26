/*
Copyright (C) 2017, 2018 Bengt Martensson.

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

import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.ModulatedIrSequence;
import org.harctoolbox.ircore.OddSequenceLengthException;
import org.harctoolbox.xml.DumbHtmlRenderer;
import org.harctoolbox.xml.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

/**
 * This class extends the Protocol class {@link Protocol} class with a few more
 * properties, in particular a name. It corresponds to an entry in the protocol data
 * base <code>IrpProtocols.xml</code>.
 */
public final class NamedProtocol extends Protocol implements HasPreferOvers,Comparable<NamedProtocol> {
    private final static Logger logger = Logger.getLogger(NamedProtocol.class.getName());
    private final static int MAX_NESTED_PREFER_OVERS = 10;

    public static Document toDocument(Iterable<NamedProtocol> protocols) {
        Document document = XmlUtils.newDocument();
        Element root = document.createElement(IrpDatabase.NAMED_PROTOCOLS_NAME);
        document.appendChild(root);
        for (NamedProtocol protocol : protocols) {
            Element el = protocol.toElement(document);
            root.appendChild(el);
        }
        return document;
    }

    @SuppressWarnings("empty-statement")
    private static void putParameter(Map<String, Object> map, String parameterName, Double userValue, Double databaseValue) {
        if (userValue != null)
            map.put(parameterName, userValue);
        else if (databaseValue != null)
            map.put(parameterName, databaseValue);
        else
            ;
    }

    private static boolean inInterval(double x, double lower, double upper) {
        return lower <= x && x <= upper;
    }

    private static double getDoubleWithSubstitute(Double value, double fallback) {
        return value != null ? value : fallback;
    }

    private final String name;
    private final transient DocumentFragment htmlDocumentation; // TODO: serialization...
    private final Double absoluteTolerance;
    private final Double relativeTolerance;
    private final Double frequencyTolerance;
    private final Double frequencyLower;
    private final Double frequencyUpper;
    private final Double minimumLeadout;
    private final boolean decodable;
    private final List<PreferOver> preferOver; // If true, the parameter values overwrite protocol specific values.
    private final Map<String, List<String>> auxParameters;
    private final boolean rejectRepeatless;

    public NamedProtocol(String name, String irp, DocumentFragment htmlDocumentation, String frequencyTolerance,
            String frequencyLower, String frequencyUpper,
            String absoluteTolerance, String relativeTolerance, String minimumLeadout, String decodable, String rejectRepeatless,
            List<String> preferOver, Map<String, List<String>> map)
            throws InvalidNameException, UnsupportedRepeatException, NameUnassignedException, IrpInvalidArgumentException {
        super(irp);
        this.name = name;
        this.htmlDocumentation = htmlDocumentation;
        this.frequencyTolerance = frequencyTolerance != null ? Double.valueOf(frequencyTolerance) : null;
        this.frequencyLower = frequencyLower != null ? Double.valueOf(frequencyLower) : null;
        this.frequencyUpper = frequencyUpper != null ? Double.valueOf(frequencyUpper) : null;
        this.absoluteTolerance = absoluteTolerance != null ? Double.valueOf(absoluteTolerance) : null;
        this.relativeTolerance = relativeTolerance != null ? Double.valueOf(relativeTolerance) : null;
        this.minimumLeadout = minimumLeadout != null ? Double.valueOf(minimumLeadout) : null;
        this.decodable = decodable == null || Boolean.parseBoolean(decodable);
        this.rejectRepeatless = rejectRepeatless != null && Boolean.parseBoolean(rejectRepeatless);
        this.preferOver = PreferOver.parse(preferOver);
        this.auxParameters = new HashMap<>(map.size());
        map.entrySet().stream().filter((kvp) -> (!IrpDatabase.isKnownKeyword(kvp.getKey()))).forEach((kvp) -> {
            this.auxParameters.put(kvp.getKey(), kvp.getValue());
        });
    }

    public NamedProtocol(String name, String irp, DocumentFragment documentation) throws InvalidNameException, UnsupportedRepeatException, NameUnassignedException, IrpInvalidArgumentException {
        this(name, irp, documentation, null, null, null, null, null, null, null, null, null, new HashMap<>(0));
    }

    public Set<String> preferredOvers() {
        HashSet<String> result = new HashSet<>(16);
        for (PreferOver prefOver : preferOver) {
            String remove = prefOver.toBeRemoved();
            if (remove != null)
                result.add(remove);
        }
        return result;
    }

    public Set<String> preferredOvers(Map<String, Long>params) {
        HashSet<String> result = new HashSet<>(16);
        for (PreferOver prefOver : preferOver) {
            String remove = prefOver.toBeRemoved(params);
            if (remove != null)
                result.add(remove);
        }
        return result;
    }

    public Set<String> preferredOvers(IrpDatabase irpDatabase) throws InvalidNameException, UnsupportedRepeatException, IrpInvalidArgumentException, NameUnassignedException, TooDeepPreferOversException {
        return preferredOvers(irpDatabase, 0);
    }

    public Set<String> preferredOvers(IrpDatabase irpDatabase, int level) throws UnsupportedRepeatException, IrpInvalidArgumentException, NameUnassignedException, TooDeepPreferOversException {
        if (level >= MAX_NESTED_PREFER_OVERS)
            throw new TooDeepPreferOversException(name);

        HashSet<String> result = new HashSet<>(16);
        for (PreferOver prefOver : preferOver) {
            String remove = prefOver.toBeRemoved();
            if (remove == null)
                continue;

            result.add(remove);
            try {
                NamedProtocol namedProtocol = irpDatabase.getNamedProtocol(remove);
                Set<String> secondary = namedProtocol.preferredOvers(irpDatabase);
                result.addAll(secondary);
            } catch (UnknownProtocolException | InvalidNameException ex) {
                logger.log(Level.SEVERE, "{0}", ex.getMessage());
            }
        }
        return result;
    }

    public void dumpPreferOvers(PrintStream out) {
        out.println(name + ":");
        preferOver.forEach((prefOver) -> {
            out.println("\t" + prefOver);
        });
    }

    public void dumpPreferOvers(PrintStream out, IrpDatabase irpDatabase) throws TooDeepPreferOversException {
        dumpPreferOvers(out, irpDatabase, 0);
    }

    public void dumpPreferOvers(PrintStream out, IrpDatabase irpDatabase, int level) throws TooDeepPreferOversException {
        if (level >= MAX_NESTED_PREFER_OVERS)
            throw new TooDeepPreferOversException(name);

        //out.println(IrCoreUtils.tabs(level) + this.name + ":");
        for (PreferOver prefOver : preferOver) {
            String r = prefOver.toBeRemoved();
            try {
                out.println(IrCoreUtils.tabs(level+1) + prefOver);
                NamedProtocol namedProtocol = irpDatabase.getNamedProtocol(r);
                namedProtocol.dumpPreferOvers(out, irpDatabase, level+1);
            } catch (UnknownProtocolException ex) {
                logger.log(Level.WARNING, "{0}", ex.getMessage());
            } catch (InvalidNameException | UnsupportedRepeatException | IrpInvalidArgumentException | NameUnassignedException ex) {
                Logger.getLogger(NamedProtocol.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * This version overrides a version in {@link Protocol}. It uses protocol specific parameter values for
     * <code>frequencyTolerance, absoluteTolerance, relativeTolerance</code>,
     * and <code>minimumLeadout</code>, if defined.
     * @param irSignal
     * @param strict
     * @return
     * @throws SignalRecognitionException
     * @throws org.harctoolbox.irp.Protocol.ProtocolNotDecodableException
     */
    @Override
    public Map<String, Long> recognize(IrSignal irSignal, boolean strict) throws SignalRecognitionException, ProtocolNotDecodableException {
        Decoder.DecoderParameters params = new Decoder.DecoderParameters();
        return recognize(irSignal, params);
    }

    @Override
    public Map<String, Long> recognize(IrSignal irSignal, Decoder.DecoderParameters params/*boolean strict,
            Double userFrequencyTolerance, Double userAbsoluteTolerance, Double userRelativeTolerance, Double userMinimumLeadout, boolean override*/)
            throws ProtocolNotDecodableException, SignalRecognitionException
    {
        if (!isDecodeable())
            throw new ProtocolNotDecodableException(name);
        logger.log(Level.FINE, "Protocol: {0}: \"{1}\", actual data: {2}", new Object[]{getName(), getIrp(), irSignal.toString(true)});

        Decoder.DecoderParameters fixedParams = params.select(isRejectRepeats(), frequencyTolerance, absoluteTolerance, relativeTolerance, minimumLeadout);
        Map<String, Long> parameters = super.recognize(irSignal, fixedParams);
        return parameters;
    }

    /**
     * Tries to match the ModulatedIrSequence in the argument, if match, return the matching parameters. If no match, throws exception.
     * The ModulatedIrSequence should contain intro or (one or many) repeat sequences, and possibly an ending sequence.
     * @param irSequence ModulatedIrSequence to be matched
     * @param beginPos Where the match is effectively started, normally 0.
     * @param userSuppliedDecoderParameters
     * @return Decoder.Decode object, containing matching data.
     * @throws SignalRecognitionException
     * @throws org.harctoolbox.irp.Protocol.ProtocolNotDecodableException
      */
    public Decoder.Decode recognize(ModulatedIrSequence irSequence, int beginPos, Decoder.DecoderParameters userSuppliedDecoderParameters)
            throws SignalRecognitionException, ProtocolNotDecodableException {
        if (!isDecodeable())
            throw new ProtocolNotDecodableException(name);

        logger.log(Level.FINE, "Protocol: {0}: \"{1}\", actual data: {2}", new Object[]{getName(), getIrp(), irSequence.toString(true)});
        Decoder.DecoderParameters actualParameters = userSuppliedDecoderParameters.select(false/*isRejectRepeats()*/, frequencyTolerance, absoluteTolerance, relativeTolerance, minimumLeadout);
        Decoder.Decode decode = super.recognize(irSequence, beginPos, isRejectRepeats(), actualParameters);
        return new Decoder.Decode(this, decode);
    }

    @SuppressWarnings("null")
    @Override
    protected void checkFrequency(Double frequency, Decoder.DecoderParameters params) throws SignalRecognitionException {
        if (params.getFrequencyTolerance() < 0) {
            logger.log(Level.FINER, "Frequency not checked since frequencyTolerance < 0");
            return;
        }

        double lower = frequencyLower != null ? frequencyLower : getFrequencyWithDefault() - params.getFrequencyTolerance();
        double upper = frequencyUpper != null ? frequencyUpper : getFrequencyWithDefault() + params.getFrequencyTolerance();
        boolean success = inInterval(frequency, lower, upper);
        logger.log(Level.FINER, "Frequency was checked, {0}OK.", success ? "" : "NOT ");
        if (!success)
            throw new SignalRecognitionException("Frequency does not match");
    }

    @Override
    public String warningsString() {
        String str = super.warningsString();
        if (!name.matches(IrpUtils.C_IDENTIFIER_REGEXP))
            str = str + "WARNING: The name \"" + name + "\" is not a valid C name." + IrCoreUtils.LINE_SEPARATOR;
        return str;
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 41 * hash + Objects.hashCode(this.name);
        hash = 41 * hash + Objects.hashCode(this.htmlDocumentation);
        hash = 41 * hash + Objects.hashCode(this.absoluteTolerance);
        hash = 41 * hash + Objects.hashCode(this.relativeTolerance);
        hash = 41 * hash + Objects.hashCode(this.frequencyTolerance);
        hash = 41 * hash + Objects.hashCode(this.minimumLeadout);
        hash = 41 * hash + Objects.hashCode(this.decodable);
        hash = 41 * hash + Objects.hashCode(this.preferOver);
        hash = 41 * hash + Objects.hashCode(this.auxParameters);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NamedProtocol))
            return false;

        NamedProtocol other = (NamedProtocol) obj;
        return super.equals(obj)
                && name.equals(other.name)
                && htmlDocumentation.equals(other.htmlDocumentation)
                && Double.compare(absoluteTolerance, other.absoluteTolerance) == 0
                && Double.compare(relativeTolerance, other.relativeTolerance) == 0
                && Double.compare(frequencyTolerance, other.frequencyTolerance) == 0
                && Double.compare(minimumLeadout, other.minimumLeadout) == 0
                && decodable == other.decodable
                && preferOver.equals(other.preferOver)
                && auxParameters.equals(other.auxParameters);
    }

    public IrSignal render(NameEngine nameEngine) throws DomainViolationException, NameUnassignedException, IrpInvalidArgumentException, InvalidNameException, ProtocolNotRenderableException, OddSequenceLengthException {
        List<String> list = auxParameters.get(IrpDatabase.DECODE_ONLY_NAME);
        if (list != null)
            if (Boolean.parseBoolean(list.get(0)))
                throw new ProtocolNotRenderableException(name);
        return super.toIrSignal(nameEngine);
    }

    @Override
    public String toString() {
        return name + ": " + super.toString();
    }

    @Override
    public String toString(int radix) {
        return name + ": " + super.toString(radix);
    }

    @Override
    public String toString(int radix, String separator) {
        return toString(radix);
    }

    /**
     * @return the name
     */
    @Override
    public String getName() {
        return name;
    }

    public DocumentFragment getDocumentation() {
        return htmlDocumentation;
    }

    public boolean isDecodeable() {
        return decodable;
    }

    public boolean isRejectRepeats() {
        return rejectRepeatless;
    }

    /**
     * Returns the parameters relativeTolerance, or null if not assigned.
     * @return
     */
    public Double getRelativeTolerance() {
        return relativeTolerance;
    }

    /**
     * Returns the parameters relativeTolerance, or the default value if not assigned.
     * @return
     */
    public double getRelativeToleranceWithDefault() {
        return getDoubleWithSubstitute(relativeTolerance, IrCoreUtils.DEFAULT_RELATIVE_TOLERANCE);
    }

    public Double getAbsoluteTolerance() {
        return absoluteTolerance;
    }

    public double getAbsoluteToleranceWithDefault() {
        return getDoubleWithSubstitute(absoluteTolerance, IrCoreUtils.DEFAULT_ABSOLUTE_TOLERANCE);
    }

    public Double getFrequencyTolerance() {
        return frequencyTolerance;
    }

    public double getFrequencyToleranceWithDefault() {
        return getDoubleWithSubstitute(frequencyTolerance, IrCoreUtils.DEFAULT_FREQUENCY_TOLERANCE);
    }

    public Double getFrequencyUpper() {
        return frequencyUpper;
    }

    public Double getFrequencyLower() {
        return frequencyLower;
    }

    public Double getMinimumLeadout() {
        return minimumLeadout;
    }

    public double getMinimumLeadoutWithDefault() {
        return getDoubleWithSubstitute(minimumLeadout, IrCoreUtils.DEFAULT_MINIMUM_LEADOUT);
    }

    List<PreferOver> getPreferOver() {
        return Collections.unmodifiableList(preferOver);
    }

    @Override
    public Set<String> getPreferOverNames() {
        HashSet<String> result = new HashSet<>(preferOver.size());
        preferOver.forEach((po) -> {
            result.add(po.toBeRemoved());
        });
        return result;
    }

    @Override
    public Document toDocument() {
        Document document = XmlUtils.newDocument();
        document.appendChild(toElement(document));
        return document;
    }

    @Override
    public Element toElement(Document document) {
        Element root = super.toElement(document);
        root.setAttribute(IrpDatabase.NAME_NAME, getName());
        XmlUtils.addAttributeIfNonNull(root, IrpDatabase.ABSOLUTE_TOLERANCE_NAME, absoluteTolerance);
        XmlUtils.addAttributeIfNonNull(root, IrpDatabase.RELATIVE_TOLERANCE_NAME, relativeTolerance);
        XmlUtils.addAttributeIfNonNull(root, IrpDatabase.FREQUENCY_TOLERANCE_NAME, frequencyTolerance);
        XmlUtils.addAttributeIfNonNull(root, IrpDatabase.FREQUENCY_LOWER_NAME, frequencyLower);
        XmlUtils.addAttributeIfNonNull(root, IrpDatabase.FREQUENCY_UPPER_NAME, frequencyUpper);
        XmlUtils.addAttributeIfNonNull(root, IrpDatabase.MINIMUM_LEADOUT_NAME, minimumLeadout);
        XmlUtils.addBooleanAttributeIfFalse(root, IrpDatabase.DECODABLE_NAME, decodable);
        XmlUtils.addBooleanAttributeIfTrue(root, IrpDatabase.REJECT_REPEATLESS_NAME, rejectRepeatless);

        DocumentFragment html = getDocumentation();
        if (html != null) {
            Element docu = document.createElement(IrpDatabase.HTML_NAME);
            docu.appendChild(document.adoptNode(html.cloneNode(true)));
            root.appendChild(docu);

            Element textDocu = document.createElement(IrpDatabase.DOCUMENTATION_ELEMENT_NAME);
            String textdoc = DumbHtmlRenderer.render(getDocumentation());
            textDocu.setTextContent(textdoc);
            root.appendChild(textDocu);
        }

        Element irpElement = document.createElement(IrpDatabase.IRP_ELEMENT_NAME);
        irpElement.appendChild(document.createTextNode(getIrp()));
        root.appendChild(irpElement);

        Element parameters = document.createElement(IrpDatabase.PARAMETERS_NAME);
        root.appendChild(parameters);
        for (Map.Entry<String, List<String>> param : auxParameters.entrySet()) {
            Element parameter = document.createElement(IrpDatabase.PARAMETER_ELEMENT_NAME);
            parameters.appendChild(parameter);
            parameter.setAttribute(IrpDatabase.NAME_NAME, param.getKey());
            param.getValue().stream().map((val) -> {
                Element value = document.createElement(IrpDatabase.VALUE_NAME);
                value.setTextContent(val);
                return value;
            }).forEachOrdered((value) -> {
                parameter.appendChild(value);
            });
        }

        return root;
    }

    ItemCodeGenerator code(CodeGenerator codeGenerator, Map<String, String> parameters, Double absoluteTolerance, Double relativeTolerance, Double frequencyTolerance) {
        ItemCodeGenerator template = codeGenerator.newItemCodeGenerator(this);
        template.addAggregateList(IrpDatabase.META_DATA_NAME, metaDataPropertiesMap(parameters, absoluteTolerance, relativeTolerance, frequencyTolerance));
        template.addAggregateList(IrpDatabase.PROTOCOL_NAME, this, getGeneralSpec(), getDefinitions());
        return template;
    }

    private Map<String, Object> metaDataPropertiesMap(Map<String, String> parameters, Double userAbsoluteTolerance, Double userRelativeTolerance, Double userFrequencyTolerance) {
        Map<String, Object> map = IrpUtils.propertiesMap(parameters.size() + 11, this);
        auxParameters.entrySet().forEach((kvp) -> {
            map.put(kvp.getKey(), kvp.getValue());
        });
        map.put(IrpDatabase.PROTOCOL_NAME_NAME, getName());
        map.put(IrpDatabase.IRP_NAME, getIrp());
        map.put(IrpDatabase.DOCUMENTATION_NAME, IrCoreUtils.javaifyString(DumbHtmlRenderer.render(getDocumentation())));
        putParameter(map, IrpDatabase.RELATIVE_TOLERANCE_NAME, userRelativeTolerance, relativeTolerance);
        putParameter(map, IrpDatabase.ABSOLUTE_TOLERANCE_NAME, userAbsoluteTolerance, absoluteTolerance);
        putParameter(map, IrpDatabase.FREQUENCY_TOLERANCE_NAME, userFrequencyTolerance, frequencyTolerance);
        map.putAll(parameters);
        return map;
    }

    @Override
    public int compareTo(NamedProtocol namedProtocol) {
        return IrCoreUtils.lexicalCompare(this.name.compareTo(namedProtocol.name), this.toIrpString().compareTo(namedProtocol.toIrpString()));
    }

    public static class TooDeepPreferOversException extends IrpException {
        public static final int MAX_NESTED_PREFER_OVERS = NamedProtocol.MAX_NESTED_PREFER_OVERS;

        private TooDeepPreferOversException(String name) {
            super(name);
        }
    }
}
