/*
Copyright (C) 2011, 2012, 2015 Bengt Martensson.

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.antlr.v4.runtime.tree.ParseTree;
import org.harctoolbox.ircore.IncompatibleArgumentException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of Definitions in Chapter 10 and Assignments in Chapter 11; these are not independent objects.
 *
 */

// TODO: There are probably too many accessing functions here.
// Clean up by eliminating and making private.

public class NameEngine extends IrpObject implements Cloneable, Iterable<Map.Entry<String, Expression>> {
    private final static int WEIGHT = 0;

    private final static Logger logger = Logger.getLogger(NameEngine.class.getName());

    public static NameEngine parseLoose(String str) throws IrpSyntaxException {
        NameEngine nameEngine = new NameEngine();
        if (str == null || str.trim().isEmpty())
            return nameEngine;

        String payload = str.trim().replaceFirst("^\\{", "").replaceFirst("\\}$", "");
        String[] definitions = payload.split("[\\s,;]+");
        for (String definition : definitions) {
            ParserDriver parserDriver = new ParserDriver(definition);
            nameEngine.parseDefinition(parserDriver.getParser().definition());
        }
        return nameEngine;
    }
    private static Element mkElement(Document document, Map.Entry<String, Expression> definition) {
        Element element = document.createElement("definition");
        element.appendChild(new Name(definition.getKey()).toElement(document));
        element.appendChild(definition.getValue().toElement(document));
        return element;
    }

    private HashMap<String, Expression> map;

    public NameEngine() {
        map = new LinkedHashMap<>(3);
    }

    private NameEngine(HashMap<String, Expression> map) {
        this.map = map;
    }

    public NameEngine(String str) throws IrpSyntaxException {
        this();
        if (str != null && !str.isEmpty()) {
            ParserDriver parserDriver = new ParserDriver(str);
            parseDefinitions(parserDriver.getParser().definitions());
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.map);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (!(obj instanceof NameEngine))
            return false;

        NameEngine other = (NameEngine) obj;
        if (map.size() != other.map.size())
            return false;

        for (Map.Entry<String, Expression> kvp : map.entrySet()) {
            String key = kvp.getKey();
            if (!kvp.getValue().equals(other.map.get(key)))
                return false;
        }

        return true;
    }

    public int size() {
        return map.size();
    }

    public boolean numbericallyEquals(Object object) {
        if (object == this)
            return true;
        if (!(object instanceof NameEngine))
            return false;

        NameEngine other = (NameEngine) object;
        if (other.size() != this.size())
            return false;

        for (Map.Entry<String, Expression> kvp : map.entrySet()) {
            try {
                String name = kvp.getKey();
                long value = kvp.getValue().toNumber(this);
                Expression expr = other.get(name);
                if (expr == null)
                    return false;
                if (value != expr.toNumber(other)) {
                    logger.log(Level.INFO, "Variable \"{0}\" valued {1} instead of {2}", new Object[]{name, value, expr.toNumber(other)});
                    return false;
                }
            } catch (UnassignedException | IrpSyntaxException | IncompatibleArgumentException ex) {
                Logger.getLogger(NameEngine.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }
        return true;
    }

    /**
     * Creates a copy of the NameEngine. The bindings are copied, the expressions not.
     * @return Shallow copy.
     */
    @Override
    @SuppressWarnings("CloneDeclaresCloneNotSupported")
    public NameEngine clone() {
        NameEngine result = null;
        try {
            result = (NameEngine) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new InternalError(ex);
        }
        result.map = new LinkedHashMap<>(map.size());
        result.map.putAll(map);
        return result;
    }

    @Override
    public Iterator<Map.Entry<String, Expression>> iterator() {
        return map.entrySet().iterator();
    }

    public void define(String name, String value) throws IrpSyntaxException {
        Expression exp = new Expression(value);
        define(name, exp.getParseTree());
    }

    private void define(String name, IrpParser.ExpressionContext ctx) throws IrpSyntaxException {
        if (!Name.validName(name))
            throw new IrpSyntaxException("Invalid name: " + name);
        Expression expression = new Expression(ctx);
        map.put(name, expression);
    }

    public void define(String name, Expression expression) throws IrpSyntaxException {
        if (!Name.validName(name)) // ????
            throw new IrpSyntaxException("Invalid name: " + name);
        map.put(name, expression);
    }

    public void define(Name name, Expression expression) throws IrpSyntaxException {
        define(name.toString(), expression);
    }

    public void define(String name, long value) throws IrpSyntaxException {
        define(name, new Expression(value));
    }

    public void define(Name name, long value) throws IrpSyntaxException {
        define(name, new Expression(value));
    }

    public void define(PrimaryItem data, long value) throws IrpSyntaxException {
        Name name = data.toName();
        if (name != null)
            define(name, value);
    }

    /**
     * Invoke the parser on the supplied argument, and stuff the result into the name engine.
     *
     * @param str String to be parsed, like "{C = F*4 + D + 3}".
     * @throws org.harctoolbox.irp.IrpSyntaxException
     */
    public void parseDefinitions(String str) throws IrpSyntaxException {
        ParserDriver parserDriver = new ParserDriver(str);
        parseDefinitions(parserDriver.getParser().definitions());
    }

    public final void parseDefinitions(IrpParser.DefinitionsContext ctx /* DEFINITIONS */) throws IrpSyntaxException {
        for (IrpParser.DefinitionContext definition : ctx.definitions_list().definition())
            parseDefinition(definition);
    }

    private void parseDefinition(IrpParser.DefinitionContext ctx /* DEFINITION */) throws IrpSyntaxException {
        define(ctx.name().getText(), ctx.expression());
    }

    /**
     * Set names according to the content of the default values supplies in the first argument.
     *
     * @param parameterSpecs from where the default values are taken
     * @param initial If false, Parameters with memory (state variables) are not reset.
     * /
    public void loadDefaults(ParameterSpecs parameterSpecs, boolean initial) {
        for (ParameterSpec param : parameterSpecs.getParams()) {
            if ((initial || ! param.hasMemory()) && param.getDefault() != null) {
                //System.out.println(">>>>>" + param.getName());
                map.put(param.getName(), param.getDefault());
            }
        }
    }

    public void loadActualParameters(HashMap<String, Long> ivs, ParameterSpecs paramSpecs) throws DomainViolationException {
        for (Entry<String, Long> kvp : ivs.entrySet()) {
            String name = kvp.getKey();
            // if no Parameter Specs, do not annoy the user; he has been warned already.
            if (!paramSpecs.isEmpty()) {
                ParameterSpec ps = paramSpecs.getParameterSpec(name);
                if (ps == null) {
                    UserComm.warning("Parameter `" + name + "' unknown in ParameterSpecs.");
                } else if (!ps.isOK(kvp.getValue())) {
                    throw new DomainViolationException("Parameter " + name + " = " + kvp.getValue() + " outside of allowed domain (" + ps.domainAsString() + ").");
                }
            }
            assign(name, kvp.getValue());
        }
    }

    public void checkAssignments(ParameterSpecs paramSpecs) throws UnassignedException {
        for (String name : paramSpecs.getNames()) {
            if (!map.containsKey(name)) {
                throw new UnassignedException("Parameter `" + name + "' has not been assigned.");
            }
        }
    }*/

    /**
     * Returns the expression associated to the name given as parameter.
     * @param name
     * @return
     * @throws org.harctoolbox.irp.UnassignedException
     */
    public Expression get(String name) throws UnassignedException {
        //Debug.debugNameEngine("NameEngine: " + name + (map.containsKey(name) ? (" = " + map.get(name).toStringTree()) : "-"));
        if (!map.containsKey(name))
            throw new UnassignedException("Name " + name + " not defined");
        return map.get(name);
    }

    public long toNumber(String name) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
        Expression expression = get(name);
        return expression.toNumber(this);
    }

    public ParseTree toParseTree(String name) throws UnassignedException {
        return get(name).getParseTree();
    }

    /* *
     *
     * @param name Input name
     * @return StringTree of the value.
     * @throws UnassignedException
     * /
    public IrpParser.Bare_expressionContext evaluate(String name) throws UnassignedException {
        if (map.containsKey(name))
            return map.get(name);
        else
            throw new UnassignedException("Name `" + name + "' not defined.");
    }

    public IrpParser.Bare_expressionContext evaluate(IrpParser.NameContext ctx) throws UnassignedException {
        return evaluate(ctx.getText());
    }*/

    public boolean containsKey(String name) {
        return map.containsKey(name);
    }

    /*public String tryEvaluate(String name) {
        IrpParser.Bare_expressionContext result ;
        try {
            result = evaluate(name);
        } catch (UnassignedException ex) {
            System.err.println(ex.getMessage());
        }
        return result;
    }*/

    void add(NameEngine definitions) {
        map.putAll(definitions.map);
    }

    public String toString(IrpParser parser) {
        StringBuilder str = new StringBuilder(map.size()*10);
        map.keySet().stream().forEach((name) -> {
            str.append(name).append("=").append(map.get(name).getParseTree().toStringTree(parser)).append(",");
        });
        return "{" + (str.length() == 0 ? "" : str.substring(0, str.length()-1)) + "}";
    }

    @Override
    public String toString() {
//        StringBuilder str = new StringBuilder();
//        for (String name : map.keySet()) {
//            str.append(name).append("=").append(map.get(name).toString()).append(",");
//        }
//        return "{" + (str.length() == 0 ? "" : str.substring(0, str.length()-1)) + "}";
        return toIrpString();
    }

    @Override
    public String toIrpString() {
        StringJoiner stringJoiner = new StringJoiner(",", "{", "}");
        map.entrySet().stream().forEach((kvp) -> {
            stringJoiner.add(kvp.getKey() + "=" + kvp.getValue().toIrpString());
        });
        return stringJoiner.toString();
    }

    public String toIrpString(int radix) {
        StringJoiner stringJoiner = new StringJoiner(",", "{", "}");
//        map.entrySet().stream().forEach((kvp) -> {
//            stringJoiner.add(kvp.getKey() + "=" + IrpUtils.radixPrefix(radix) + kvp.getValue().toIrpString(radix));
//        });
        for (Map.Entry<String, Expression> kvp : map.entrySet()) {
            stringJoiner.add(kvp.getKey() + "=" + IrpUtils.radixPrefix(radix) + kvp.getValue().toIrpString(radix));
        }
        return stringJoiner.toString();
    }

//    /**
//     * Creates consisting of parameter values that can be used as part of filenames etc.
//     * Roughly, is a "pretty" variant of toString().
//     *
//     * @param equals String between name and value, often "=",
//     * @param separator String between name-value pairs, often ",".
//     * @return String
//     * @throws org.harctoolbox.irp.UnassignedException
//     */
//    public String notationString(String equals, String separator) throws UnassignedException {
//        StringBuilder str = new StringBuilder();
//        for (String name : map.keySet()) {
//            if (!name.startsWith("$") && !toParseTree(name).toStringTree().startsWith("("))
//                str.append(name).append(equals).append(toParseTree(name).toStringTree()).append(separator);
//        }
//        return (str.length() == 0 ? "" : str.substring(0, str.length()-1));
//    }

    @Override
    public Element toElement(Document document) {
        Element root = document.createElement("definitions");
        for (Map.Entry<String, Expression> definition : map.entrySet())
            root.appendChild(mkElement(document, definition));
        return root;
    }


    public HashMap<String, Long> toMap() throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
        HashMap<String, Long> result = new HashMap<>(map.size());
        for (Map.Entry<String, Expression> kvp : map.entrySet())
            result.put(kvp.getKey(), kvp.getValue().toNumber(this));
        return result;
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    void addBarfByConflicts(NameEngine nameEngine) throws NameConflictException {
        for (Map.Entry<String, Expression> kvp : nameEngine.map.entrySet()) {
            String name = kvp.getKey();
            Expression val = kvp.getValue();
            if (map.containsKey(name)) {
                try {
                    // FIXME
                    if (map.get(name).toNumber(this) != val.toNumber(nameEngine)) {
                        logger.log(Level.FINER, "Name conflict {0}", name);
                        throw new NameConflictException(name);
                    }
                } catch (UnassignedException ex) {

                } catch (IrpSyntaxException | IncompatibleArgumentException ex) {
                    Logger.getLogger(NameEngine.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else
                map.put(name, val);
        }
    }

    void reduce(ParameterSpecs parameterSpecs) {
        ArrayList<String> names = new ArrayList<>(map.keySet());
        names.stream().filter((name) -> (!parameterSpecs.contains(name))).forEach((name) -> {
            map.remove(name);
        });
    }

    @Override
    public int weight() {
        return WEIGHT;
    }
}
