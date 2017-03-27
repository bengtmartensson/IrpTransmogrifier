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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of Definitions in Chapter 10 and Assignments in Chapter 11; these are not independent objects.
 *
 */

// TODO: There are probably too many accessing functions here.
// Clean up by eliminating and making private.

public class NameEngine extends IrpObject implements Cloneable, AggregateLister, Iterable<Map.Entry<String, Expression>> {
    private final static int WEIGHT = 0;

    private final static Logger logger = Logger.getLogger(NameEngine.class.getName());

    public static NameEngine parseLoose(String str) throws InvalidNameException, IrpSemanticException {
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
    private static Element mkElement(Document document, Map.Entry<String, Expression> definition) throws IrpSemanticException {
        Element element = document.createElement("Definition");
        element.appendChild(new Name(definition.getKey()).toElement(document));
        element.appendChild(definition.getValue().toElement(document));
        return element;
    }

    private Map<String, Expression> map;

    public NameEngine(int initialCapacity) {
        super(null);
        map = new LinkedHashMap<>(initialCapacity);
    }

    public NameEngine() {
        this(4);
    }

    public NameEngine(String str) throws InvalidNameException, IrpSemanticException {
        this();
        if (str != null && !str.isEmpty()) {
            ParserDriver parserDriver = new ParserDriver(str);
            parseDefinitions(parserDriver.getParser().definitions());
        }
    }

    public NameEngine(Map<String, Long> numericalParameters) {
        this(numericalParameters.size());
        numericalParameters.entrySet().stream().forEach((entry) -> {
            map.put(entry.getKey(), new NumberExpression(entry.getValue()));
        });
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.map);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final NameEngine other = (NameEngine) obj;
        return Objects.equals(this.map, other.map);
    }

//    @Override
//    public boolean equals(Object obj) {
//        if (obj == this)
//            return true;
//
//        if (!(obj instanceof NameEngine))
//            return false;
//
//        NameEngine other = (NameEngine) obj;
//        if (map.size() != other.map.size())
//            return false;
//
//        boolean result = true;
//        for (Map.Entry<String, Expression> kvp : map.entrySet()) {
//            String key = kvp.getKey();
//            if (!kvp.getValue().toIrpString().equals(other.map.get(key).toIrpString()))
//                result = false;
//        }
//
//        return result;
//    }

    public int size() {
        return map.size();
    }

    public boolean numericallyEquals(Object object) {
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
//                if (expr == null)
//                    return false;
                if (value != expr.toNumber(other)) {
                    logger.log(Level.INFO, "Variable \"{0}\" valued {1} instead of {2}", new Object[]{name, value, expr.toNumber(other)});
                    return false;
                }
            } catch (UnassignedException | IrpSemanticException ex) {
                Logger.getLogger(NameEngine.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }
        return true;
    }

    public boolean numericallyEquals(Map<String, Long> other) {
        if (other == null)
            return false;
        if (other.size() != this.size())
            return false;

        for (Map.Entry<String, Expression> kvp : map.entrySet()) {
            try {
                String name = kvp.getKey();
                long value = kvp.getValue().toNumber(this);

                if (value != other.get(name))
                    return false;
            } catch (UnassignedException | IrpSemanticException ex) {
                logger.log(Level.SEVERE, null, ex);
                return false;
            }
        }
        return true;
    }

    public boolean numericallyEquals(NameEngine other) {
        try {
            return numericallyEquals(other.toMap());
        } catch (UnassignedException | IrpSyntaxException | IrpSemanticException ex) {
            return false;
        }
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

    public void define(String name, String value) throws IrpException {
        Expression exp = Expression.newExpression(value);
        define(name, exp/*.getParseTree()*/);
    }

    private void define(String name, IrpParser.ExpressionContext ctx) throws InvalidNameException, IrpSemanticException {
        if (!Name.validName(name))
            throw new InvalidNameException(name);
        Expression expression = Expression.newExpression(ctx);
        map.put(name, expression);
    }

    public void define(String name, Expression expression) throws InvalidNameException {
        if (!Name.validName(name)) // ????
            throw new InvalidNameException(name);
        map.put(name, expression);
    }

    public void define(Name name, Expression expression) throws InvalidNameException {
        define(name.toString(), expression);
    }

    public void define(String name, long value) throws InvalidNameException {
        define(name, new NumberExpression(value));
    }

    public void define(Name name, long value) throws InvalidNameException {
        define(name, new NumberExpression(value));
    }

//    public void define(PrimaryItem data, long value) throws InvalidNameException, IrpSemanticException {
//        Name name = data.toName();
//        if (name != null)
//            define(name, value);
//    }

    /**
     * Invoke the parser on the supplied argument, and stuff the result into the name engine.
     *
     * @param str String to be parsed, like "{C = F*4 + D + 3}".
     * @throws org.harctoolbox.irp.InvalidNameException
     * @throws org.harctoolbox.irp.IrpSemanticException
     */
    public void parseDefinitions(String str) throws InvalidNameException, IrpSemanticException {
        ParserDriver parserDriver = new ParserDriver(str);
        parseDefinitions(parserDriver.getParser().definitions());
    }

    public final void parseDefinitions(IrpParser.DefinitionsContext ctx /* DEFINITIONS */) throws InvalidNameException, IrpSemanticException {
        for (IrpParser.DefinitionContext definition : ctx.definitions_list().definition())
            parseDefinition(definition);
    }

    private void parseDefinition(IrpParser.DefinitionContext ctx /* DEFINITION */) throws InvalidNameException, IrpSemanticException {
        define(ctx.name().getText(), ctx.expression());
    }

    /**
     * Returns the expression associated to the name given as parameter.
     * @param name
     * @return
     * @throws org.harctoolbox.irp.UnassignedException
     */
    public Expression get(String name) throws UnassignedException {
        //Debug.debugNameEngine("NameEngine: " + name + (map.containsKey(name) ? (" = " + map.get(name).toStringTree()) : "-"));
        Expression expression = map.get(name);
        if (expression == null)
            throw new UnassignedException("Name " + name + " not defined");
        return expression;
    }

    public long toNumber(String name) throws UnassignedException, IrpSyntaxException, IrpSemanticException {
        Expression expression = get(name);
        return expression.toNumber(this);
    }

//    public ParseTree toParseTree(String name) throws UnassignedException {
//        return get(name).getParseTree();
//    }

    public boolean containsKey(String name) {
        return map.containsKey(name);
    }

    void add(NameEngine definitions) {
        map.putAll(definitions.map);
    }

    public String toString(IrpParser parser) {
        StringJoiner stringJoiner = new StringJoiner(",", "{", "}");
        map.entrySet().forEach((kvp) -> {
            stringJoiner.add(kvp.getKey() + "=" + kvp.getValue().toString());
        });
        return stringJoiner.toString();
    }

//    @Override
//    public String toString() {
//        return toIrpString();
//    }

    @Override
//    public String toIrpString() {
//        StringJoiner stringJoiner = new StringJoiner(",", "{", "}");
//        map.entrySet().stream().forEach((kvp) -> {
//            stringJoiner.add(kvp.getKey() + "=" + kvp.getValue().toIrpString());
//        });
//        return stringJoiner.toString();
//    }

    public String toIrpString(int radix) {
        StringJoiner stringJoiner = new StringJoiner(",", "{", "}");
        map.entrySet().stream().forEach((kvp) -> {
            stringJoiner.add(kvp.getKey() + "=" + kvp.getValue().toIrpString(radix));
        });
        return stringJoiner.toString();
    }

    @Override
    public Element toElement(Document document) throws IrpSemanticException {
        Element root = document.createElement("Definitions"); // do not use super!
        map.entrySet().forEach((definition) -> {
            root.appendChild(mkElement(document, definition));
        });
        return root;
    }


    public Map<String, Long> toMap() throws UnassignedException, IrpSyntaxException, IrpSemanticException {
        HashMap<String, Long> result = new HashMap<>(map.size());
        for (Map.Entry<String, Expression> kvp : map.entrySet()) {
            result.put(kvp.getKey(), kvp.getValue().toNumber(this));
        }
        return result;
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    void addBarfByConflicts(NameEngine nameEngine) throws NameConflictException, IrpSemanticException {
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
                }
            } else
                map.put(name, val);
        }
    }

    @Override
    public int weight() {
        return WEIGHT;
    }

//    public String code(CodeGenerator codeGenerator) {
//        ItemCodeGenerator template = codeGenerator.newItemCodeGenerator(this);
//        List<String> list = new ArrayList<>(map.size());
//        map.entrySet().stream().map((kvp) -> {
//            ItemCodeGenerator st = codeGenerator.newItemCodeGenerator("NameDefinition");
//            st.addAttribute("name", kvp.getKey());
//            st.addAttribute("expression", kvp.getValue().code(true, codeGenerator));
//            return st;
//        }).forEachOrdered((st) -> {
//            list.add(st.render());
//        });
//        template.addAttribute("definitions", list);
//        return template.render();
//    }

    @Override
    public Map<String, Object> propertiesMap(GeneralSpec generalSpec, NameEngine nameEngine) throws IrpSemanticException {
        Map<String, Object> result = new HashMap<>(2);
        result.put("kind", this.getClass().getSimpleName());
        List<Map<String, Object>> list = new ArrayList<>(map.size());
        result.put("list", list);
        for (Map.Entry<String, Expression> kvp : map.entrySet()) {
            Map<String, Object> m = new HashMap<>(2);
            m.put("name", kvp.getKey());
            m.put("expression", kvp.getValue().propertiesMap(true, generalSpec, nameEngine));
            list.add(m);
        }
        return result;
    }
}
