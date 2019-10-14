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

import com.beust.jcommander.IStringConverter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileListParser implements IStringConverter<List<File>> {

    @Override
    public List<File> convert(String files) {
        String[] paths = files.split(",");
        List<File> fileList = new ArrayList<>(4);
        for (String path : paths)
            fileList.add(new File(path));
        
        return fileList;
    }
}
