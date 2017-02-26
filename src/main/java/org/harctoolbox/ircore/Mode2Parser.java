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

package org.harctoolbox.ircore;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class contains a parser for mode2 files.
 * Although sane, it probably should go somewhere else.
 */

public class Mode2Parser {
    private final static Logger logger = Logger.getLogger(Mode2Parser.class.getName());
    /**
     * Added at the end of IR sequences that would otherwise end with a flash.
     */
    public static final int DUMMYGAP = 50000;

    public static void main(String[] args) {
        try {
            IrSequence irSequence = (args.length == 0 || args[0].equals("-"))
                    ? toIrSequence(new InputStreamReader(System.in, "US-ASCII"))
                    : toIrSequence(new File(args[0]));
            if (args.length < 2)
                System.out.println(irSequence.toPrintString(false));
            else {
                double threshold = Double.parseDouble(args[1]);
                List<IrSequence> parts = irSequence.chop(threshold);
                int i = 0;
                for (IrSequence part : parts)
                    System.out.println("signal_" + i++ + ":" + part.toPrintString(false)); // Easy to parse for IrScrutinizer
            }
        } catch (IOException | ParseException ex) {
            Logger.getLogger(Mode2Parser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static IrSequence toIrSequence(File file) throws IOException, ParseException {
        try {
            return new Mode2Parser(file).irSequence;
        } catch (UnsupportedEncodingException ex) {
            throw new ThisCannotHappenException();
        }
    }

    public static IrSequence toIrSequence(Reader reader) throws IOException, ParseException {
        return new Mode2Parser(reader).irSequence;
    }

    private final IrSequence irSequence;

    private Mode2Parser(Reader reader) throws IOException, ParseException {
        LineNumberReader buf = new LineNumberReader(reader);
        List<Integer> list = new ArrayList<>(1024);
        int i = 0;
        while (true) {
            String str = buf.readLine();
            if (str == null)
                break;
            str = str.trim();
            if (str.isEmpty() || str.startsWith("#"))
                continue;
            String[] parts = str.split("\\s+");
            int value = Integer.parseInt(parts[1]);
            switch (parts[0]) {
                case "space":
                    if (i == 0)
                        continue;
                    if (i % 2 != 0)
                        list.add(value);
                    else
                        list.set(i - 1, list.get(i) + value);
                    break;
                case "pulse":
                    if (i % 2 == 0)
                        list.add(value);
                    else
                        list.set(i - 1, list.get(i - 1) + value);
                    break;
                default:
                    throw new ParseException("Unknown keyword", buf.getLineNumber());
            }
            i++;
        }
        if (i % 2 != 0)
            list.add(DUMMYGAP);

        double[] data = new double[list.size()];
        i = 0;
        for (Integer n : list)
            data[i++] = n;

        irSequence = new IrSequence(data);
    }

    private Mode2Parser(File file) throws IOException, ParseException {
        this(new InputStreamReader(new FileInputStream(file), "US-ASCII"));
    }

    private Mode2Parser() {
        irSequence = null;
    }
}
