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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

/**
 *
 */
public class ParameterSpecs {

    private HashMap<String, ParameterSpec>map;
    private IrpParser irpParser;

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

    public ParameterSpecs(String parameter_specs) {
        this();
        IrpLexer lex = new IrpLexer(new ANTLRInputStream(parameter_specs));
        CommonTokenStream tokens = new CommonTokenStream(lex);
        irpParser = new IrpParser(tokens);
        IrpParser.Parameter_specsContext parseTree = irpParser.parameter_specs();
        System.out.println(parseTree.toStringTree(irpParser));
        load(parseTree);
    }

    public ParameterSpecs(IrpParser.Parameter_specsContext t) {
        this();
        load(t);
    }

    private void load(IrpParser.Parameter_specsContext t) {
        for (IrpParser.Parameter_specContext parameterSpec : t.parameter_spec()) {
            ParameterSpec ps = parameterSpec instanceof IrpParser.MemoryfullParameterSpecContext
                    ? new ParameterSpec((IrpParser.MemoryfullParameterSpecContext) parameterSpec)
                    : new ParameterSpec((IrpParser.MemorylessParameterSpecContext) parameterSpec);
            map.put(ps.getName(), ps);
        }
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (ParameterSpec ps : map.values())
            str.append(ps.toString(irpParser)).append(", ");

        if (str.length() > 0)
            str.deleteCharAt(str.length()-2);
        return "[" + str.toString() + "]";
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
//        try {
            System.out.println(new ParameterSpecs("[T@:0..1=0,D:0..31,F:0..128,S:0..255=D-255]"));
  //      } catch (ParseException ex) {
    //        System.err.println(ex.getMessage());
      //  }
    }
}
