package usa.cactuspuppy.admit.utils;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import usa.cactuspuppy.admit.Main;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Custom YML parser supporting #comments and indented key-value pairs
 *
 * @author CactusPuppy
 */
public final class Config {
    @Getter @Setter
    /**
     * How many spaces each level should indent
     */
    private static int spacesPerIndent = 2;

    @Getter
    private File configFile;
    /**
     * Pattern to match against potential config values
     */
    private static final Pattern KV = Pattern.compile("( *)([^:\\n]+): *([^:\\n]*)");
    /**
     * Key-value pairs from config
     */
    private HashMap<String, String> values = new HashMap<>();
    /**
     * Which line each KV pair was from
     */
    private LinkedHashMap<Integer, String> kvLocs = new LinkedHashMap<>();
    /**
     * Store where comments are located and what they are
     */
    private LinkedHashMap<Integer, String> nonKeyLocs = new LinkedHashMap<>();
    /**
     * Stores how many lines are
     */
    private int numLines;
    /**
     * Stores the exit code of the constructor<br>
     *     -1 - Uninitialized
     *     0 - OK
     *     1 - FileNotFound
     */
    @Getter
    private int initCode = -1;

    public Config() {
        configFile = new File(Main.getInstance().getDataFolder(), "config.yml");
        try {
            readValues(new FileInputStream(configFile));
            initCode = 0;
        } catch (FileNotFoundException e) {
            Logger.logWarning(this.getClass(), "Could not find config file " + configFile.getName() + " on config construction", e);
            initCode = 1;
        }
    }

    public Config(File config) {
        configFile = config;
        try {
            readValues(new FileInputStream(configFile));
        } catch (FileNotFoundException e) {
            Logger.logWarning(this.getClass(), "Could not find config file " + configFile.getName() + " on config construction", e);
        }
    }

    public void reload() {
        try {
            readValues(new FileInputStream(configFile));
        } catch (FileNotFoundException e) {
            Logger.logWarning(this.getClass(), "Problem reloading from disk", e);
        }
    }

    void readValues(InputStream inputStream) {
        //Initialize temporary variables
        LinkedList<Integer> currIndents = new LinkedList<>();
        currIndents.addLast(0);
        int lineIndex = 0;
        Scanner scan = new Scanner(inputStream);
        LinkedList<String> currPrefixes = new LinkedList<>();
        //Trackers
        String previousKey = "";
        int prevIndent = 0;

        //Read in
        while (scan.hasNext()) {
            String line = scan.nextLine();
            lineIndex += 1;
            int hashIndex = line.indexOf("#");
            if (hashIndex != -1) { //Found a comment, store it and trim out
                String comment = line.substring(hashIndex);
                line = line.substring(0, hashIndex);
                nonKeyLocs.put(lineIndex, comment);
            }
            Matcher m = KV.matcher(line);
            if (!m.matches()) { //No key-value pair found, store the rest
                if (nonKeyLocs.containsKey(lineIndex)) {
                    nonKeyLocs.put(lineIndex, line + nonKeyLocs.get(lineIndex));
                } else {
                    nonKeyLocs.put(lineIndex, line);
                }
                continue;
            }
            //Have a key-value pair
            String indent = m.group(1);
            String key = m.group(2);
            String value = m.group(3);
            key = key.trim();
            //Check key does not have a period
            if (key.contains(".")) {
                Logger.logWarning(this.getClass(), "Found a key with a period at line " + lineIndex + ", not reading key. Offending key: " + key);
                continue;
            }
            value = value.trim();
            int currentIndent = indent.length();
            if (currentIndent < prevIndent) {
                while (currentIndent <= currIndents.peekLast()) { //Pop prefixes off the end until reach appropriate indent level
                    currIndents.removeLast();
                    if (!currPrefixes.isEmpty()) currPrefixes.removeLast();
                    if (currIndents.isEmpty()) {
                        currIndents.addLast(0);
                        break;
                    }
                }
            } else if (currentIndent > prevIndent) {
                currPrefixes.addLast(previousKey); //Add new indent
                currIndents.addLast(prevIndent);
            }
            StringJoiner prefixJoiner = new StringJoiner(".");
            for (String prefix : currPrefixes) {
                if (prefix != null && !prefix.equals("")) prefixJoiner.add(prefix);
            }
            prefixJoiner.add(key);
            String keyActual = prefixJoiner.toString();
            values.put(keyActual, value); //Store KV pair
            kvLocs.put(lineIndex, keyActual); //Store line number
            previousKey = key;
            prevIndent = currentIndent;
        }
        numLines = lineIndex;
        scan.close();
    }

    /**
     * Saves the current config to the specified file, formatting to YML standards in the process
     *
     * @param overwrite whether to overwrite {@code save} if it exists
     * @return whether saving was successful
     */
    public boolean saveConfig(boolean overwrite) {
        if (configFile.isFile() && !overwrite) {
            return false;
        }
        try {
            FileWriter fW = new FileWriter(configFile);
            BufferedWriter writer = new BufferedWriter(fW);
            for (int currLine = 1; currLine <= numLines; currLine++) {
                StringBuilder builder = new StringBuilder();
                //Include KV pair
                if (kvLocs.containsKey(currLine)) {
                    String key = kvLocs.get(currLine);
                    int level = StringUtils.countMatches(key, ".") * 2;
                    StringBuilder kv = new StringBuilder(key.substring(key.lastIndexOf(".") + 1) + ":");
                    if (values.get(key) != null && !values.get(key).equals("")) {
                        kv.append(" ").append(values.get(key));
                    }
                    String line = kv.toString();
                    line = StringUtils.leftPad(line, level + line.length());
                    builder.append(line); //Pad left with appropriate indents
                }
                if (nonKeyLocs.containsKey(currLine)) {
                    if (kvLocs.containsKey(currLine)) {
                        builder.append(" ");
                    }
                    builder.append(nonKeyLocs.get(currLine));
                }
                writer.write(builder.toString());
                writer.newLine();
            }
            writer.close();
        } catch (Exception e) {
            Logger.logWarning(this.getClass(), "Problem saving config file " + configFile.getName(), e);
            return false;
        }
        return true;
    }

    public boolean saveConfig() {
        return saveConfig(true);
    }

    public String get(String key) {
        return values.get(key);
    }

    public String get(String key, String def) {
        String value = values.get(key);
        if (value == null) {
            return def;
        }
        return value;
    }

    /**
     * @return A copy of values currently stored in the cache
     */
    public Map<String, String> getValues() {
        return new HashMap<>(values);
    }

    Map<Integer, String> getNonKeyLocs() {
        return new HashMap<>(nonKeyLocs);
    }

    public boolean set(String key, String value) {
        if (!values.containsKey(key)) {
            return false;
        }
        values.put(key, value);
        return true;
    }

    @Override
    public String toString() {
        StringBuilder rv = new StringBuilder();
        Map<String, String> values = getValues();
        for (String s : values.keySet()) {
            rv.append(s).append(" -> ").append(values.get(s)).append("\n");
        }
        return rv.toString();
    }
}
