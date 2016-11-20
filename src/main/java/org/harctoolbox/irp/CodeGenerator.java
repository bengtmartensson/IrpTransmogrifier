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

    public abstract ItemCodeGenerator newItemCodeGenerator(String name);

    public ItemCodeGenerator newItemCodeGenerator(Object object) {
        return newItemCodeGenerator(object.getClass().getSimpleName());
    }

    public String render(String name) {
        return newItemCodeGenerator(name).render();
    }

    public String fileExtension() {
        return render("CodeFileExtension");
    }

    public String fileSuffix() {
        return render("FileSuffix");
    }

    public boolean isAbstract() {
        return Boolean.parseBoolean(render("IsAbstract"));
    }

    public boolean manyProtocolsInOneFile() {
        return Boolean.parseBoolean(render("ManyProtocolsInOneFile"));
    }
}
