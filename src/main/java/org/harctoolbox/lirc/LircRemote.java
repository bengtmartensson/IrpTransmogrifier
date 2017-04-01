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

package org.harctoolbox.lirc;

import java.util.List;
import java.util.Map;

/**
 * Semantic-agnostic model of a Lirc remote. May be either raw or cooked.
 */
public final class LircRemote {

    private final String name;
    private final List<String> flags;
    private final Map<String, Long> unaryParameters;
    private final Map<String, Pair> binaryParameters;
    private final List<LircCommand> commands;
    private final String driver;
    private final String source;
    private final boolean raw;

    LircRemote(String name, List<String> flags, Map<String, Long> unaryParameters,
            Map<String, Pair> binaryParameters, List<LircCommand> commands, boolean raw, String driver, String source) {
        this.name = name;
        this.flags = flags;
        this.unaryParameters = unaryParameters;
        this.binaryParameters = binaryParameters;
        this.commands = commands;
        this.raw = raw;
        this.driver = driver;
        this.source = source;
    }

    public String getName() {
        return name;
    }

    String getSource() {
        return source;
    }

    public boolean isMode2() {
        return driver == null && hasSaneTimingInfo();
    }

    public boolean hasFlag(String flag) {
        return flags.contains(flag);
    }

    public Long getUnaryParameters(String key) {
        return unaryParameters.get(key);
    }

    public Pair getBinaryParameters(String key) {
        return binaryParameters.get(key);
    }

    /**
     * @return the commands
     */
    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public List<LircCommand> getCommands() {
        return commands;
    }

    /**
     * @return the raw
     */
    public boolean isRaw() {
        return raw;
    }

    private boolean hasSaneTimingInfo() {
        return hasSane("zero") && hasSane("one");
    }

    private boolean hasSane(String name) {
        Pair parameter = binaryParameters.get(name);
        return parameter != null && !parameter.isTrivial();
    }

    public static class Pair {

        private final long first;
        private final long second;

        Pair(long x, long y) {
            this.first = x;
            this.second = y;
        }


        @Override
        public String toString() {
            return "(" + Long.toString(first) + ", " + Long.toString(second) + ")";
        }

        public boolean isTrivial() {
            return first == 0 && second == 0;
        }

        /**
         * @return the first
         */
        public long getFirst() {
            return first;
        }

        /**
         * @return the second
         */
        public long getSecond() {
            return second;
        }
    }
}
