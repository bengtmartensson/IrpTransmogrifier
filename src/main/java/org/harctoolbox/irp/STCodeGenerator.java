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

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

public class STCodeGenerator extends CodeGenerator {

    private static final Logger logger = Logger.getLogger(XmlGenerator.class.getName());

    private static final String sTGroupFileExtension = ".stg";

    private static String stDir = "src/main/st";

    public static void setStDir(String newStDir) {
        stDir = newStDir;
    }

    public static void trackCreationEvents(boolean value) {
        STGroup.trackCreationEvents = value;
    }

    public static String fileExtension(String target) throws IOException {
        STCodeGenerator cg = new STCodeGenerator(target, null, null);
        return cg.fileExtension();
    }

    static String fileSuffix(String target) throws IOException {
        STCodeGenerator cg = new STCodeGenerator(target, null, null);
        return cg.fileSuffix();
    }

    // STGroupFile(String) throws IllegalArgumentException (extends RuntimeException)
    // if the file is not found.
    // This does not fit in here, so throw IOException if the file is not readable.
    private static STGroupFile newSTGroupFile(File file) throws IOException {
        if (!file.canRead())
            throw new IOException("ST Group file " + file.getCanonicalPath() + " cannot be read.");
        return new STGroupFile(file.getCanonicalPath());
    }

    private STGroup stGroup;

    public STCodeGenerator(String target, GeneralSpec generalSpec, NameEngine nameEngine) throws IOException {
        this(new File(stDir, target + sTGroupFileExtension), generalSpec, nameEngine);
    }

    public STCodeGenerator(File file, GeneralSpec generalSpec, NameEngine nameEngine) throws IOException {
        this(newSTGroupFile(file), generalSpec, nameEngine);
    }

    public STCodeGenerator(STGroup stGroup, GeneralSpec generalSpec, NameEngine nameEngine) {
        super(generalSpec, nameEngine);
        this.stGroup = stGroup;
    }

    @Override
    public ItemCodeGenerator newItemCodeGenerator(String name) {
        ST st = stGroup.getInstanceOf(name);
        if (st == null)
            logger.log(Level.WARNING, "Template {0} was not found", name);
        return new STItemCodeGenerator(st);
    }
}
