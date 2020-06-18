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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.harctoolbox.ircore.IrCoreUtils.EXTENDED_LATIN1_NAME;

/**
 * This class parses the <a href="http://lirc.org/html/lircd.conf.html">Lircd configuration file(s)</a>.
 * Its preferred public members are the static functions parseConfig,
 * updating a Map of {@link LircRemote}s, while guaranteeing that all remotes will
 * be presentm possibly with modified keys for satisfying the uniqueness requirement.
 */

// This code is not derived from the Lirc sources.
// It is however based upon the class org.harctoolbox.jirc,LircConfigFile.

// Reason for not using antlr: The syntax of names for commands and remotes
// (just non-whitespace) makes tokinization problematic.

public final class LircConfigFile {

    private final static Logger logger = Logger.getLogger(LircConfigFile.class.getName());

    /**
     * Default character set for the input files.
     */
    public final static String DEFAULT_CHARSET_NAME = EXTENDED_LATIN1_NAME;

    /**
     * Reads the file given as second argument and updates the dictionary of {@link LircRemote}s given as first argument.
     * The names in it are made unique by, if necessary, appending a suffix.
     *
     * @param dictionary Map of LircRemotes.
     * @param filename lircd.conf file, or directory containing such.
     * @param charsetName Name of the Charset used for reading.
     * @throws IOException Misc IO problem
     */
    static void readConfig(Map<String, LircRemote> dictionary, File filename, String charsetName) throws IOException {
        logger.log(Level.FINER, "Parsing {0}", filename.getCanonicalPath());
        if (filename.isFile()) {
            LircConfigFile config = new LircConfigFile(filename, filename.getCanonicalPath(), charsetName);
            accumulate(dictionary, config.remotes);
        } else if (filename.isDirectory()) {
            File[] files = filename.listFiles();
            for (File file : files) {
                // The program handles nonsensical files fine, however rejecting some
                // obviously irrelevant files saves time and log entries.
                if (file.getName().endsWith(".jpg") || file.getName().endsWith(".png")
                        || file.getName().endsWith(".gif") || file.getName().endsWith(".html")) {
                    logger.log(Level.INFO, "Rejecting file {0}", file.getCanonicalPath());
                    continue;
                }
                readConfig(dictionary, file, charsetName);
            }
        } else
            throw new FileNotFoundException("File or directory " + filename.getCanonicalPath() + " not existing, or not a normal file");
    }

    static void readConfig(Map<String, LircRemote> dictionary, File filename) throws IOException {
        readConfig(dictionary, filename, DEFAULT_CHARSET_NAME);
    }

    static void readConfig(Map<String, LircRemote> dictionary, Reader reader, String source) throws IOException {
        LircConfigFile config = new LircConfigFile(reader, source);
        accumulate(dictionary, config.remotes);
    }

    static void readConfig(Map<String, LircRemote> dictionary, Reader reader) throws IOException {
        readConfig(dictionary, reader, null);
    }

    public static List<LircRemote> readRemotesFileOrDirectory(File fileOrDirectory, String charSetName) throws IOException {
        Map<String, LircRemote> map = new LinkedHashMap<>(4);
        readConfig(map, fileOrDirectory, charSetName);
        return new ArrayList<>(map.values());
    }

    public static List<LircRemote> readRemotes(Reader reader, String source) throws IOException {
        return Collections.unmodifiableList(new LircConfigFile(reader, source).remotes);
    }

    public static List<LircRemote> readRemotes(Reader reader) throws IOException {
        return readRemotes(reader, null);
    }

    public static List<LircRemote> readRemotes(String urlOrFilename, String charSetName) throws IOException {
        List<LircRemote> rems;
        try {
            rems = readRemotesURL(urlOrFilename, charSetName);
        } catch (MalformedURLException ex) {
            rems = readRemotesFileOrDirectory(new File(urlOrFilename), charSetName);
            //rems = new LircConfigFile(new File(urlOrFilename), urlOrFilename, charSetName).remotes;
        }
        return Collections.unmodifiableList(rems);
    }

    public static List<LircRemote> readRemotesURL(String urlOrFilename, String charsetName) throws MalformedURLException, IOException {
        URL url = new URL(urlOrFilename);
        URLConnection urlConnection = url.openConnection();
        try (InputStream inputStream = urlConnection.getInputStream()) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, charsetName);
            return readRemotes(inputStreamReader, urlOrFilename);
        }
    }

    static long parseLircNumber(String s) {
        return s.toLowerCase(Locale.US).startsWith("0x") ? parseUnsignedLongHex(s.substring(2))
                : s.startsWith("0") ? Long.parseLong(s, 8)
                : Long.parseLong(s);
    }

    static long parseUnsignedLongHex(String s) {
        if (s.length() == 16) {
            long value = new BigInteger(s, 16).longValue();
            return value;
        }
        return Long.parseLong(s, 16);
    }

    private static void accumulate(Map<String, LircRemote> dictionary, Collection<LircRemote> values) {
        values.forEach((irRemote) -> {
            String remoteName = irRemote.getName();
            int n = 1;
            while (dictionary.containsKey(remoteName)) {
                remoteName = irRemote.getName() + "$" + n;
                n++;
            }

            if (n > 1)
                logger.log(Level.INFO, "Remote name {0} (source: {1}) already present, renaming to {2}",
                        new Object[]{irRemote.getName(), irRemote.getSource(), remoteName});
            dictionary.put(remoteName, irRemote);
        });
    }

    private List<LircRemote> remotes;
    private LineNumberReader reader;
    private String line;
    private String[] words;
    private boolean raw;

    private LircConfigFile(File configFileName, String source, String charsetName) throws UnsupportedEncodingException, FileNotFoundException, IOException {
        this(new InputStreamReader(new FileInputStream(configFileName), charsetName != null ? charsetName : DEFAULT_CHARSET_NAME), source);
    }

    private LircConfigFile(Reader reader, String source) throws IOException {
        this.remotes = new ArrayList<>(4);
        this.reader = new LineNumberReader(reader);
        line = null;
        words = new String[0];

        remotes = remotes(source);
    }

    private List<LircRemote> remotes(String source) throws IOException {
        List<LircRemote> rems = new ArrayList<>(4);
        while (true) {
            try {
                LircRemote remote = remote(source);
                rems.add(remote);
            } catch (ParseException ex) {
                try {
                    lookFor("end", "remote");
                } catch (EofException ex1) {
                    return rems;
                }
            } catch (EofException ex) {
                return rems;
            }
        }
    }

    private LircRemote remote(String source) throws IOException, ParseException, EofException {
        lookFor("begin", "remote");
        ProtocolParameters parameters = parameters();
        List<LircCommand> codes = codes();
        gobble("end", "remote");

        LircRemote irRemote = new LircRemote(parameters.name, parameters.flags,
                parameters.unaryParameters, parameters.binaryParameters, codes, raw, parameters.driver, source);
        return irRemote;
    }

    private void readLine() throws IOException, EofException {
        if (line != null)
            return;
        words = new String[0];
        while (words.length == 0) {
            line = reader.readLine();
            if (line == null)
                throw new EofException();

            line = line.trim();

            int idx = line.indexOf('#');
            if (idx != -1)
                line = line.substring(0, idx).trim();
            if (!line.isEmpty())
                words = line.split("\\s+");
        }
    }

    private void consumeLine() {
        line = null;
    }

    private void gobble(String... tokens) throws IOException, EofException, ParseException {
        while (true) {
            readLine();
            for (int i = 0; i < tokens.length; i++)
                if (words.length < tokens.length || !words[i].equalsIgnoreCase(tokens[i]))
                    throw new ParseException("Did not find " + String.join(" ", tokens), reader.getLineNumber());
            consumeLine();
            break;
        }
    }

    private void lookFor(String... tokens) throws IOException, EofException {
        while (true) {
            readLine();
            boolean hit = true;
            for (int i = 0; i < tokens.length; i++) {
                if (words.length < tokens.length || !words[i].equalsIgnoreCase(tokens[i])) {
                    hit = false;
                    break;
                }
            }
            consumeLine();
            if (hit)
                return;
        }
    }

    private ProtocolParameters parameters() throws IOException, ParseException, EofException {
        ProtocolParameters parameters = new ProtocolParameters();
        while (true) {
            readLine();
            switch (words[0]) {
                case "name":
                    parameters.name = words[1];
                    break;
                case "driver":
                    parameters.driver = words[1];
                    break;
                case "flags":
                    parameters.flags = flags(words);
                    break;
                case "begin":
                    return parameters;
                default:
                    try {
                    switch (words.length) {
                        case 2:
                            parameters.add(words[0], parseLircNumber(words[1]));
                            break;
                        case 3:
                            parameters.add(words[0], parseLircNumber(words[1]), parseLircNumber(words[2]));
                            break;
                        default:
                            throw new ParseException("silly parameter decl: " + line, reader.getLineNumber());
                    }
                    } catch (NumberFormatException ex) {
                        // except for a warning, just ignore unparsable parameters
                        logger.log(Level.INFO, "Could not parse line \"{0}\": {1}", new Object[]{line, ex});
                    }
            }
            consumeLine();
        }
    }

    private List<String> flags(String[] words) {
        StringJoiner str = new StringJoiner(" ");
        for (int i = 1; i < words.length; i++)
            str.add(words[i]);
        String array[] = str.toString().split("\\s*\\|\\s*");
        return Arrays.asList(array);
    }

    private List<LircCommand> codes() throws IOException, EofException, ParseException {
        try {
            return cookedCodes();
        } catch (ParseException ex) {
            raw = true;
            return rawCodes();
        }
    }

    private List<LircCommand> cookedCodes() throws IOException, EofException, ParseException {
        gobble("begin", "codes");
        List<LircCommand> codes = new ArrayList<>(32);
        while (true) {
            try {
                LircCommand code = cookedCode();
                codes.add(code);
            } catch (ParseException ex) {
                break;
            }
        }
        gobble("end", "codes");
        return codes;
    }

    private LircCommand cookedCode() throws IOException, EofException, ParseException {
        readLine();
        if (words.length < 2)
            throw new ParseException("", reader.getLineNumber());
        if (words[0].equalsIgnoreCase("end") && words[1].equalsIgnoreCase("codes"))
            throw new ParseException("", reader.getLineNumber());
        List<Long> codes = new ArrayList<>(words.length - 1);
        for (int i = 1; i < words.length; i++)
            codes.add(parseLircNumber(words[i]));
        LircCommand irNCode = new LircCommand(words[0], codes);

        consumeLine();
        return irNCode;
    }

    private List<LircCommand> rawCodes() throws IOException, EofException, ParseException {
        gobble("begin", "raw_codes");
        List<LircCommand> codes = new ArrayList<>(32);
        while (true) {
            try {
                LircCommand code = rawCode();
                codes.add(code);
            } catch (ParseException ex) {
                break;
            }
        }
        gobble("end", "raw_codes");
        return codes;
    }

    private LircCommand rawCode() throws IOException, EofException, ParseException {
        readLine();
        if (words.length < 2)
            throw new ParseException("", reader.getLineNumber());
        if (words[0].equalsIgnoreCase("end") && words[1].equalsIgnoreCase("raw_codes"))
            throw new ParseException("", reader.getLineNumber());
        if (!words[0].equalsIgnoreCase("name"))
            throw new ParseException("", reader.getLineNumber());
        String cmdName = words[1];
        consumeLine();
        List<Long> codes = integerList();
        LircCommand irNCode = new LircCommand(cmdName, codes);
        return irNCode;
    }

    private List<Long> integerList() throws IOException, EofException {
        List<Long> numbers = new ArrayList<>(64);
        while (true) {
            readLine();
            try {
                for (String w : words)
                    numbers.add(Long.parseLong(w));
            } catch (NumberFormatException ex) {
                return numbers;
            }
            consumeLine();
        }
    }

    private static class EofException extends Exception {

        EofException(String str) {
            super(str);
        }

        private EofException() {
            super();
        }
    }

    private static class ProtocolParameters {
        private String name = null;
        private String driver = null;
        private List<String> flags = new ArrayList<>(8);
        private Map<String, Long> unaryParameters = new HashMap<>(16);
        private Map<String, LircRemote.Pair> binaryParameters = new HashMap<>(8);

        public void add(String name, long x) {
            unaryParameters.put(name, x);
        }

        public void add(String name, long x, long y) {
            binaryParameters.put(name, new LircRemote.Pair(x, y));
        }
    }
}
