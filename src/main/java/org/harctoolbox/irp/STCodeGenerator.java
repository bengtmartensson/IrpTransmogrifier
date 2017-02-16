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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

public class STCodeGenerator extends CodeGenerator {

    private static final Logger logger = Logger.getLogger(STCodeGenerator.class.getName());

    private static final String sTGroupFileExtension = ".stg";

    private static String stDir = null;

    public static void setStDir(String newStDir) {
        stDir = newStDir;
    }

    public static void trackCreationEvents(boolean value) {
        STGroup.trackCreationEvents = value;
    }

    public static List<String> listTargets() throws IOException {
        File[] candidates = new File(stDir).listFiles((File dir, String name) -> name.toLowerCase().endsWith(sTGroupFileExtension));
        ArrayList<String> result = new ArrayList<>(candidates.length);
        for (File file : candidates) {
            if (!new STCodeGenerator(file).isAbstract()) {
                String f = file.getName();
                String name = f.substring(0, f.length() - 4);
                result.add(name);
            }
        }
        return result;
    }

    // STGroupFile(String) throws IllegalArgumentException (extends RuntimeException)
    // if the file is not found.
    // This does not fit in here, so throw IOException if the file is not readable.
    private static STGroupFile newSTGroupFile(File file) throws IOException {
        if (!file.canRead())
            throw new FileNotFoundException("ST Group file " + file.getCanonicalPath() + " cannot be read.");
        return new STGroupFile(file.getCanonicalPath());
    }

    private STGroup stGroup;

    public STCodeGenerator(String target) throws IOException {
        this(new File(stDir, target + sTGroupFileExtension));
    }

    public STCodeGenerator(File file) throws IOException {
        this(newSTGroupFile(file));
    }

    public STCodeGenerator(STGroup stGroup) {
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
