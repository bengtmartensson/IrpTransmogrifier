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

package org.harctoolbox.analyze;

import java.util.ArrayList;
import java.util.List;
import org.harctoolbox.irp.IrStreamItem;

public class TrivialDecoder extends AbstractDecoder {

    public TrivialDecoder(Analyzer analyzer, Analyzer.AnalyzerParams params) {
        super(analyzer, params);
    }

    @Override
    protected List<IrStreamItem> process(int beg, int length) {
        List<IrStreamItem> items = new ArrayList<>(length);
        for (int i = beg; i < beg + length - 1; i += 2) {
            int mark = analyzer.getCleanedTime(i);
            int space = analyzer.getCleanedTime(i + 1);

            items.add(newFlash(mark));
            if (i == beg + length - 2 && params.isUseExtents())
                items.add(newExtent(analyzer.getTotalDuration(beg, length)));
            else
                items.add(newGap(space));
        }
        return items;
    }
}
