/*
Copyright (C) 2016 Bengt Martensson.

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

public abstract class CodeGenerator {

    private final GeneralSpec generalSpec;
    private final NameEngine nameEngine;
    //private final StringBuilder stringBuilder;

    protected CodeGenerator(GeneralSpec generalSpec, NameEngine nameEngine) {
        this.generalSpec = generalSpec;
        this.nameEngine = nameEngine;
        //this.stringBuilder = new StringBuilder(8192);
    }

    public abstract ItemCodeGenerator newItemCodeGenerator(String name);

//    public ItemCodeGenerator newItemCodeGenerator(Class<?> clazz) {
//        return newItemCodeGenerator(clazz.getSimpleName());
//    }

    /**
     * @return the generalSpec
     */
    public GeneralSpec getGeneralSpec() {
        return generalSpec;
    }

    public ItemCodeGenerator newItemCodeGenerator(Object object) {
        return newItemCodeGenerator(object.getClass().getSimpleName());
    }

    public String fileExtension() {
        return newItemCodeGenerator("CodeFileExtension").render();
    }

    public String fileSuffix() {
        return newItemCodeGenerator("FileSuffix").render();
    }

//    public void add(String str) {
//        stringBuilder.append(str);
//    }

//    public void addLine(String str) {
//        add(str);
//        add(IrCoreUtils.lineSeparator);
//    }

//    public void addLine(ItemCodeGenerator st) {
//        addLine(st.render());
//    }

//    public void addStatement(ItemCodeGenerator st) {
//        add(st.render());
//        addLine(newItemCodeGenerator("StatementSeparator").render());
//    }

//    public String result() {
//        return stringBuilder.toString();
//    }

    /**
     * @return the nameEngine
     */
    public NameEngine getNameEngine() {
        return nameEngine;
    }
}
