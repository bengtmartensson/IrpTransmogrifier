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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.antlr.v4.gui.TreeViewer;
import org.harctoolbox.ircore.IrCoreUtils;

/**
 * This class is a collection of useful utilities as static functions and constants.
 */
public final class IrpUtils {

    // The symbolic names for exit statii now in org.harctoolbox.cmdline.ExitStatus.
    // For backwards compability, keep them here. For now...

    // Symbolic names for exit statii
    public final static int EXIT_SUCCESS               = 0;
    public final static int EXIT_USAGE_ERROR           = 1;
    public final static int EXIT_SEMANTIC_USAGE_ERROR  = 2;
    public final static int EXIT_FATAL_PROGRAM_FAILURE = 3;
    public final static int EXIT_INTERNAL_FAILURE      = 4;
    public final static int EXIT_CONFIG_READ_ERROR     = 5;
    public final static int EXIT_CONFIG_WRITE_ERROR    = 6;
    public final static int EXIT_IO_ERROR              = 7;
    public final static int EXIT_XML_ERROR             = 8;
    public final static int EXIT_DYNAMICLINK_ERROR     = 9;
    public final static int EXIT_THIS_CANNOT_HAPPEN    = 10;
    public final static int EXIT_INTERRUPTED           = 11;
    public final static int EXIT_RESTART               = 99; // An invoking script is supposed to restart the program

    public final static String JP1_WIKI_URL = "http://www.hifi-remote.com/wiki/index.php?title=Main_Page";
    public final static String IRP_NOTATION_URL = "http://www.hifi-remote.com/wiki/index.php?title=IRP_Notation";
    public final static String DECODEIR_URL = "http://www.hifi-remote.com/wiki/index.php?title=DecodeIR";
    public final static String C_IDENTIFIER_REGEXP = "[A-Za-z_][A-Za-z0-9_]*";

    private final static Level enteringExitingLevel = Level.FINER;

    @SuppressWarnings("null")
    public static long variableGet(Map<String, Long> map, String name) {
        Objects.requireNonNull(map);
        return map.containsKey(name) ? map.get(name) :  IrCoreUtils.INVALID;
    }

    /**
     * Produces a header in the spirit of Makehex. Follows the convention of variable ordering:
     * D, (S), F, (T), then the rest alphabetically ordered,
     *
     * @param params HashMap&lt;String, Long&gt; of input parameters.
     * @return Nicely formatted header (String)
     */
    public static String variableHeader(HashMap<String, Long> params) {
        TreeMap<String, Long> map = new TreeMap<>(params);
        map.remove("D");
        map.remove("F");
        map.remove("S");
        map.remove("T");

        String result = formatVariable(params, "D", "Device Code: ", "")
                + formatVariable(params, "S", ".", "")
                + " "
                + formatVariable(params, "F", "Function: ", "")
                + formatVariable(params, "T", ", Toggle: ", "");

        result = map.keySet().stream().map((var) -> formatVariable(params, var, ", " + var + "=", "")).reduce(result, String::concat);

        return result;
    }

    private static String formatVariable(HashMap<String, Long>map, String name, String prefix, String postfix) {
        if (!map.containsKey(name))
            return "";

        return prefix + map.get(name) + postfix;
    }

    public static String toCIdentifier(String s) {
        String rep = s.replaceAll("[^_0-9a-zA-Z]", "_");
        return rep.matches("^\\d.*$") ? ("x" + rep) : rep;
    }

    static Map<String, Object> propertiesMap(int noProps, Object object) {
        return propertiesMap(noProps, object.getClass().getSimpleName());
    }

    static Map<String, Object> propertiesMap(int noProps, String kind) {
        Map<String, Object> result = new HashMap<>(noProps + 1);
        result.put("kind", kind);
        return result;
    }

    /**
     * Show the TreeViewer given as argument. Requires a graphic environment.
     *
     * @param tv
     * @param title
     */
    public static void showTreeViewer(TreeViewer tv, String title) {
        JPanel panel = new JPanel();
        //tv.setScale(2);
        panel.add(tv);

        JOptionPane.showMessageDialog(null, panel, title, JOptionPane.PLAIN_MESSAGE);
    }

    static List<IrStreamItem> mkIrStreamItemList(IrStreamItem irStreamItem) {
        List<IrStreamItem> list = new ArrayList<>(1);
        list.add(irStreamItem);
        return list;
    }

    private IrpUtils() {
    }
}
