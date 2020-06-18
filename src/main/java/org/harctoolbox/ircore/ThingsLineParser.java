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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.TransformerException;
import org.harctoolbox.xml.XmlUtils;
import org.xml.sax.SAXException;

public class ThingsLineParser<T> {

    private final static Logger logger = Logger.getLogger(ThingsLineParser.class.getName());
    private final String commentPrefix;
    private final ThingParser parser;

    public ThingsLineParser(ThingParser thingParser, String commentPrefix) {
        this.parser = thingParser;
        this.commentPrefix = commentPrefix;
    }

    /**
     * Reads Ts from the file/url in the first argument.
     * @param urlOrFilename
     * @param charSetName name of character set.
     * @param multiLines if true, successive lines are considered to belong to the same object, unless separated by empty lines.
     * @return List of Ts read from the first argument.
     * @throws IOException
     */
    public List<T> readThings(String urlOrFilename, String charSetName, boolean multiLines) throws IOException {
        try (InputStreamReader reader = IrCoreUtils.getInputReader(urlOrFilename, charSetName)) {
            return readThings(reader, multiLines);
        }
    }

    public List<T> readThings(String input, String xslt, String encoding, boolean multiLines) throws SAXException, IOException, UnsupportedEncodingException, TransformerException {
        Objects.requireNonNull(xslt);
        try (InputStreamReader reader = XmlUtils.mkReaderXml(input, xslt, encoding)) {
            return readThings(reader, multiLines);
        }
    }

    public List<T> readThings(Reader reader, boolean multiLines) throws IOException {
        BufferedReader in = new BufferedReader(reader);
        List<T> list = new ArrayList<>(4);
        while (true) {
            try {
                T thing = parseThing(in, multiLines);
                if (thing == null)
                    break;
                list.add(thing);
            } catch (IOException | InvalidArgumentException ex) {
                logger.log(Level.FINE, "{0}", ex.getMessage());
            }
        }
        return list;
    }

    public Map<String, T> readNamedThings(String urlOrFilename, String charSetName) throws IOException {
        try (InputStreamReader reader = IrCoreUtils.getInputReader(urlOrFilename, charSetName)) {
            return readNamedThings(reader);
        }
    }

    public Map<String, T> readNamedThings(String namedInput, String xslt, String encoding) throws SAXException, IOException, TransformerException {
        Objects.requireNonNull(xslt);
        try (InputStreamReader reader = XmlUtils.mkReaderXml(namedInput, xslt, encoding)) {
            return readNamedThings(reader);
        }
    }

    public Map<String, T> readNamedThings(Reader reader) throws IOException {
        BufferedReader in = new BufferedReader(reader);
        Map<String, T> map = new LinkedHashMap<>(4);
        while (true) {
            String line = in.readLine();
            if (line == null)
                break;
            line = line.trim();
            if (line.isEmpty() || (commentPrefix != null && line.startsWith(commentPrefix)))
                continue;
            String name = line;
            try {
                T thing = parseThing(in, true);
                if (thing != null)
                   map.put(name, thing);
            } catch (NumberFormatException | InvalidArgumentException ex) {
                logger.log(Level.WARNING, "{0}", ex.getMessage());
            }
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    T parseThing(BufferedReader in, boolean multiLines) throws IOException, InvalidArgumentException {
        ArrayList<String> list = new ArrayList<>(multiLines ? 4 : 1);
        String line;
        do {
            line = in.readLine();
            if (line == null)
                return null;
        } while (line.trim().isEmpty());
        list.add(line);
        while (multiLines) {
            line = in.readLine();
            if (line == null || line.trim().isEmpty())
                break;
            list.add(line);
        }
        return (T) parser.newThing(list);
    }

    public interface ThingParser {
        public Object newThing(List<String> list) throws InvalidArgumentException;
    }
}
