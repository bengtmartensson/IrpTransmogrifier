/*
Copyright (C) 2011, 2015 Bengt Martensson.

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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 */
public class ParameterSpecs {

    private LinkedHashMap<String, ParameterSpec>map;

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public Set<String> getNames() {
        return map.keySet();
    }

    public Collection<ParameterSpec> getParams() {
        return map.values();
    }

    public ParameterSpec getParameterSpec(String name) {
        return map.get(name);
    }

    public ParameterSpecs() {
        map = new LinkedHashMap<>();
    }

    public ParameterSpecs(String parameter_specs) throws IrpSyntaxException {
        this(new ParserDriver(parameter_specs).getParser().parameter_specs());
    }

    public ParameterSpecs(IrpParser.ProtocolContext ctx) {
        this(ctx.parameter_specs());
    }

    public ParameterSpecs(IrpParser.Parameter_specsContext t) {
        this();
        for (IrpParser.Parameter_specContext parameterSpec : t.parameter_spec()) {
            ParameterSpec ps = new ParameterSpec(parameterSpec);
            map.put(ps.getName(), ps);
        }
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("[");
        for (ParameterSpec ps : map.values())
            str.append(ps.toString()).append(", ");

        if (str.length() > 0)
            str.deleteCharAt(str.length()-2);
        str.append("]");
        return str.toString();
    }

    public Element toElement(Document document) {
        Element el = document.createElement("parameters");
        //el.appendChild(document.createComment(toString()));
        for (ParameterSpec parameterSpec : map.values())
            el.appendChild(parameterSpec.toElement(document));
        return el;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            System.out.println(new ParameterSpecs("[T@:0..1=0,D:0..31,F:0..128,S:0..255=D-255]"));
        } catch (IrpSyntaxException ex) {
            Logger.getLogger(ParameterSpecs.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void check(NameEngine nameEngine) {
//        for (ParameterSpec param : map.values()) {
//            param.check(nameEngine);
//        }
    }
}
