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

public abstract class Traverser {

    private final GeneralSpec generalSpec;
    protected NameEngine nameEngine;

    protected Traverser(GeneralSpec generalSpec, NameEngine nameEngine) {
        this.nameEngine = nameEngine;
        this.generalSpec = generalSpec;
    }

    boolean isFinished() {
        return true;
    }

    /**
     * @return the generalSpec
     */
    public GeneralSpec getGeneralSpec() {
        return generalSpec;
    }

    /**
     * @return the nameEngine
     */
    public NameEngine getNameEngine() {
        return nameEngine;
    }
}
