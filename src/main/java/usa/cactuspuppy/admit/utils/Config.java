package usa.cactuspuppy.admit.utils;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.configuration.InvalidConfigurationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import usa.cactuspuppy.admit.Main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Custom YML parser supporting #comments and indented key-value pairs
 *
 * @author CactusPuppy
 */
public final class Config implements Map<String, String> {
    /**
     * How many spaces each level should indent
     */
    @Getter @Setter
    private static int spacesPerIndent = 2;

    @Getter
    private File configFile;

    /**
     * Stores the exit code of the constructor<br>
     *     -1 - Uninitialized
     *     0 - OK
     *     1 - File Not Found
     *     2 - Invalid Configuration
     */
    @Getter
    private int initCode = -1;

    /**
     * Pattern to match against potential config values
     */
    private static final Pattern KV_MATCH = Pattern.compile("( *)([^:\\n]+): *([^:\\n]*)");

    private List<ConfigNode> heads = new ArrayList<>();

    public Config() {
        configFile = new File(Main.getInstance().getDataFolder(), "config.yml");
        try {
            load(new FileInputStream(configFile));
            initCode = 0;
        } catch (FileNotFoundException e) {
            Logger.logWarning(this.getClass(), "Could not find config file " + configFile.getName() + " on config construction", e);
            initCode = 1;
        } catch (InvalidConfigurationException e) {
            //TODO
            initCode = 2;
        }
    }

    public Config(File config) {
        configFile = config;
        try {
            load(new FileInputStream(configFile));
        } catch (FileNotFoundException e) {
            Logger.logWarning(this.getClass(), "Could not find config file " + configFile.getName() + " on config construction", e);
        } catch (InvalidConfigurationException e) {
            //TODO
            initCode = 2;
        }
    }

    /**
     * Clears local cache of values and reloads them from {@code stream}.
     * @throws InvalidConfigurationException If the provided stream does not represent a valid configuration
     * @param stream Stream to load configuration values from
     */
    public void load(InputStream stream) throws InvalidConfigurationException {
        heads.clear();

        Scanner scanner = new Scanner(stream);
        while (scanner.hasNext()) {
            String line = scanner.nextLine();
            //TODO
        }
    }

    /**
     * Overwrite current values with values from disk.
     * Will not affect keys which do not exist in disk file.
     */
    public void reload() {
        try {
            load(new FileInputStream(configFile));
        } catch (FileNotFoundException | InvalidConfigurationException e) {
            Logger.logWarning(this.getClass(), "Problem reloading from disk", e);
        }
    }


    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public String get(Object key) {
        return null;
    }

    @Nullable
    @Override
    public String put(String key, String value) {
        return null;
    }

    @Override
    public String remove(Object key) {
        return null;
    }

    @Override
    public void putAll(@NotNull Map<? extends String, ? extends String> m) {

    }

    @Override
    public void clear() {

    }

    @NotNull
    @Override
    public Set<String> keySet() {
        return null;
    }

    @NotNull
    @Override
    public Collection<String> values() {
        return null;
    }

    @NotNull
    @Override
    public Set<Entry<String, String>> entrySet() {
        return null;
    }

    @Data
    public class ConfigNode {
        /**
         * Key of this node if it is a key,
         * {@code null} if not a key
         */
        private String key;

        /**
         * Value associated with {@link ConfigNode#key} if it exists,
         * otherwise it is the line itself
         */
        private String value;

        private List<ConfigNode> children = new ArrayList<>();
    }
}
