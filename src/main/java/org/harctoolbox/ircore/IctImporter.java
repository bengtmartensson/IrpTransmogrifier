/*
Copyright (C) 2013,2014, 2017 Bengt Martensson.

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class allows for import of IR sequences in the ICT Format, used by the IRScope.
 * It has only two public members, which are static function mapping filename/url
 * onto a dictionary of ModulatedIrSequence-s.
 */
public class IctImporter {

    private static final int INVALID = -1;
    private static final int LENGTH_INSERTED_GAP = 100000;
    private static final int IRSCOPE_ENDING_GAP = -500000;
    private static final String IRSCOPE_ENDING_STRING = Integer.toString(IRSCOPE_ENDING_GAP);

    private final static Logger logger = Logger.getLogger(IrSequence.class.getName());

    /**
     * Reads an ICT file from URL or file, produces a map of ModulatedIrSequence-s.
     * @param urlOrFilename URL or Filename
     * @param charSetName Character set for the file/URL.
     * @return Map name -&gt; ModulatedIrSequence
     * @throws IOException
     * @throws UnsupportedEncodingException
     * @throws ParseException
     */
    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public static Map<String, ModulatedIrSequence> parse(String urlOrFilename, String charSetName) throws IOException, UnsupportedEncodingException, ParseException {
        IctImporter ictImporter = new IctImporter();
        ictImporter.load(urlOrFilename, charSetName);
        return ictImporter.sequences;
    }

    /**
     * Equivalent to parse(urlOrFilename, "US-ASCII").
     * @param urlOrFilename URL or Filename
     * @return Map name -&gt; ModulatedIrSequence
     * @throws IOException
     * @throws ParseException
     */
    public static Map<String, ModulatedIrSequence> parse(String urlOrFilename) throws IOException, ParseException {
        return parse(urlOrFilename, "US-ASCII");
    }

    private int lineNumber;
    private int anonymousNumber;
    private int frequency = INVALID;
    private int sampleCount = INVALID;
    private int noSamples;
    private boolean hasComplainedAboutMissingFrequency;

    private final Map<String, ModulatedIrSequence> sequences;

    private IctImporter() {
        sequences = new LinkedHashMap<>(4);
    }

    private void load(String urlOrFilename, String charSetName) throws UnsupportedEncodingException, IOException, ParseException {
        if (urlOrFilename.equals("-"))
            load(new InputStreamReader(System.in, charSetName));
        try {
            loadURL(urlOrFilename, charSetName);
        } catch (MalformedURLException ex) {
            load(new File(urlOrFilename), charSetName);
        }
    }

    private void loadURL(String urlOrFilename, String charsetName) throws MalformedURLException, IOException, ParseException {
        URL url = new URL(urlOrFilename);
        URLConnection urlConnection = url.openConnection();
        try (InputStream inputStream = urlConnection.getInputStream()) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, charsetName);
            load(inputStreamReader);
        }
    }

    private void load(File file, String charSetName) throws FileNotFoundException, IOException, ParseException {
        try (FileInputStream fis = new FileInputStream(file)) {
            load(new InputStreamReader(new FileInputStream(file), charSetName));
        }
    }

    private void load(Reader reader) throws IOException, ParseException {
        hasComplainedAboutMissingFrequency = false;
        BufferedReader bufferedReader = new BufferedReader(reader);
        anonymousNumber = 0;
        lineNumber = 0;
        noSamples = 0;
        String name = "unnamed_" + anonymousNumber;
        ArrayList<Integer> data = new ArrayList<>(64);


        String line;
        line = bufferedReader.readLine();
        if (line == null || !line.trim().equals("irscope 0"))
            throw new ParseException("No \"irscope 0\" line found", 1);
        while (true) {
            line = bufferedReader.readLine();
            if (line == null)
                break;
            lineNumber++;
            String[] chunks = line.split("[ ,=]");
            if (chunks[0].equals("carrier_frequency"))
                frequency = Integer.parseInt(chunks[1]);
            else if (chunks[0].equals("sample_count"))
                sampleCount = Integer.parseInt(chunks[1]);
            else if (chunks[0].startsWith("+")) {
                data.add(Integer.parseInt(chunks[0].substring(1)));
                noSamples++;
            } else if (chunks[0].equals("pulse")) {
                data.add(Integer.parseInt(chunks[1]));
                noSamples++;
            } else if (chunks[0].equals(IRSCOPE_ENDING_STRING)) {
                data.add(IRSCOPE_ENDING_GAP);
                noSamples++;
                processSignal(data, name);
                data.clear();
                anonymousNumber++;
                name = "unnamed_" + anonymousNumber;
            } else if (chunks[0].equals("note")) {
                if (!data.isEmpty()) {
                    processSignal(data, name);
                    data.clear();
                }
                chunks = line.split("=");
                if (chunks.length >= 2) {
                    name = chunks[1];
                    anonymousNumber--;
                }
            } else if (chunks[0].equals("irscope"))
                ;
            else if (chunks[0].startsWith("-")) {
                data.add(Integer.parseInt(chunks[0].substring(1)));
                noSamples++;
            } else if (chunks[0].equals("space"))
                if (data.isEmpty())
                    ; // Ignore leading gaps
                else {
                    data.add(Integer.parseInt(chunks[1]));
                    noSamples++;
                }
            else if (chunks[0].startsWith("#"))
                ; // Comment, ignore
            else
                logger.log(Level.WARNING, "Ignored line: {0}", lineNumber);
        }
        processSignal(data, name);
        if (noSamples != sampleCount) {
            if (sampleCount == -1)
                logger.log(Level.WARNING, "sample_count missing ({0} samples found)", noSamples);
            else
                logger.log(Level.WARNING, "sample_count erroneous (expected {0}, found {1})", new Object[]{sampleCount, noSamples});
        }
    }

    private void processSignal(ArrayList<Integer> data, String name) {
        if (data.isEmpty())
            return;

        if (data.size() % 2 == 1) {
            logger.log(Level.WARNING, "Last sample was pulse, appending a {0} microsecond gap", LENGTH_INSERTED_GAP);
            data.add(LENGTH_INSERTED_GAP);
        }

        if (frequency < 0 && hasComplainedAboutMissingFrequency) {
            hasComplainedAboutMissingFrequency = true;
            frequency = (int) ModulatedIrSequence.DEFAULT_FREQUENCY;
            logger.log(Level.WARNING, "Carrier_frequency missing, assuming {0}", frequency);
        }

        IrSequence irSequence;
        try {
            irSequence = new IrSequence(data);
        } catch (OddSequenceLengthException ex) {
            // have treated this case already
            throw new ThisCannotHappenException(ex);
        }
        sequences.put(name, new ModulatedIrSequence(irSequence, (double) frequency));
    }
}
