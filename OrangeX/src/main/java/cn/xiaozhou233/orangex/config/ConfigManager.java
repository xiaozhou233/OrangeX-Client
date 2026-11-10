package cn.xiaozhou233.orangex.config;

import cn.xiaozhou233.orangex.config.impl.JsonConfigSerializer;
import cn.xiaozhou233.orangex.modules.ModuleManager;
import cn.xiaozhou233.orangex.value.Value;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class ConfigManager {

    private static final Pattern VALID_NAME =
            Pattern.compile("[a-zA-Z0-9_-]+");

    private static final String EXTENSION = ".json";
    private static final String TEMP_SUFFIX = ".json.tmp";
    private static final String BACKUP_SUFFIX = ".json.bak";
    private static final String CURRENT_FILE = "current.txt";
    private static final String DEFAULT_CONFIG = "default";

    private final File directory;
    private final ModuleManager moduleManager;
    private final ConfigSerializer serializer;

    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private Config currentConfig;
    private volatile boolean dirty;
    private boolean initialised;

    public ConfigManager(File directory, ModuleManager moduleManager) {
        this(directory, moduleManager, new JsonConfigSerializer());
    }

    public ConfigManager(
            File directory,
            ModuleManager moduleManager,
            ConfigSerializer serializer
    ) {
        this.directory = directory;
        this.moduleManager = moduleManager;
        this.serializer = serializer;
    }

    public synchronized void init() {
        ensureDirectory();

        if (!getFile(DEFAULT_CONFIG).isFile()) {
            create(DEFAULT_CONFIG);
        }

        // Track any later value change so the client can persist settings even
        // when the user never opens the config screen.
        Value.setGlobalChangeHook(this::markDirty);

        String name = readCurrentName();
        if (name == null || getConfig(name) == null) {
            name = DEFAULT_CONFIG;
        }

        if (!load(name)) {
            // Never leave an unreadable file in place: keep a backup and start
            // over from the current (default) state.
            backupUnreadable(name);
            create(name);
            if (!load(name)) {
                System.err.println(
                        "[OrangeX] Could not restore config '" + name + "'");
            }
        }

        if (!initialised) {
            initialised = true;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    saveIfDirty();
                } catch (Throwable throwable) {
                    System.err.println(
                            "[OrangeX] Failed to save config on shutdown: "
                                    + throwable.getMessage());
                }
            }, "OrangeX Config Save"));
        }
    }

    /**
     * Creates a config file from the current in-memory module state, so a new
     * preset starts out identical to what the user currently sees.
     */
    public synchronized Config create(String name) {
        validateName(name);

        File file = getFile(name);

        if (file.exists()) {
            throw new IllegalArgumentException(
                    "Config already exists: " + name);
        }

        Config config = new Config(this, name, file);

        if (!save(name)) {
            throw new IllegalStateException(
                    "Failed to create config: " + name);
        }

        return config;
    }

    public synchronized boolean load(String name) {
        Config config = getConfig(name);

        if (config == null) {
            return false;
        }

        // Do not silently drop unsaved changes when switching presets.
        if (!isCurrent(name) && dirty && currentConfig != null && !save()) {
            System.err.println(
                    "[OrangeX] Refusing to switch config: changes to '"
                            + currentConfig.getName() + "' could not be saved");
            return false;
        }

        File file = config.getFile();
        boolean wasDirty = dirty;

        // Snapshot the live state so a malformed file cannot leave modules
        // half-restored.
        JsonObject snapshot = serializer.serialize(moduleManager);

        try (FileReader reader = new FileReader(file)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

            int version = json.has("version")
                    ? json.get("version").getAsInt()
                    : 1;

            if (version != ConfigSerializer.CURRENT_VERSION) {
                throw new IllegalStateException(
                        "Unsupported config version: " + version);
            }

            serializer.deserialize(moduleManager, json);

            currentConfig = config;
            dirty = false;
            writeCurrentName(name);
            return true;

        } catch (Exception e) {
            System.err.println(
                    "[OrangeX] Failed to load config '" + name + "': " + e.getMessage());

            try {
                serializer.deserialize(moduleManager, snapshot);
            } catch (Exception rollback) {
                System.err.println(
                        "[OrangeX] Failed to restore previous state: "
                                + rollback.getMessage());
            }

            dirty = wasDirty;
            return false;
        }
    }

    public synchronized boolean save() {
        if (currentConfig == null) {
            return false;
        }

        return save(currentConfig.getName());
    }

    /**
     * Persists the current module state under {@code name}.
     *
     * @return {@code true} when the file was written.
     */
    public synchronized boolean save(String name) {
        validateName(name);

        File file = getFile(name);
        File tempFile = new File(directory, name + TEMP_SUFFIX);

        JsonObject json = serializer.serialize(moduleManager);

        try {
            ensureDirectory();

            try (FileWriter writer = new FileWriter(tempFile)) {
                gson.toJson(json, writer);
            }

            try {
                Files.move(
                        tempFile.toPath(),
                        file.toPath(),
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );
            } catch (IOException e) {
                Files.move(
                        tempFile.toPath(),
                        file.toPath(),
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

            if (currentConfig != null
                    && currentConfig.getName().equalsIgnoreCase(name)) {
                dirty = false;
            }

            return true;

        } catch (IOException e) {
            tempFile.delete();

            System.err.println(
                    "[OrangeX] Failed to save config '" + name + "': "
                            + e.getMessage());
            return false;
        }
    }

    /**
     * Saves only when values or module states changed since the last load or
     * save. Used by periodic/GUI/shutdown save hooks.
     */
    public synchronized boolean saveIfDirty() {
        if (!dirty || currentConfig == null) {
            return false;
        }
        return save();
    }

    public synchronized boolean delete(String name) {
        Config config = getConfig(name);

        if (config == null) {
            return false;
        }

        // Deleting the active config would leave the client with nowhere to
        // save to; require the caller to load another config first.
        if (isCurrent(name)) {
            System.err.println(
                    "[OrangeX] Refusing to delete the active config: " + name);
            return false;
        }

        boolean deleted = config.getFile().delete();

        if (deleted && name.equalsIgnoreCase(readCurrentName())) {
            clearCurrentName();
        }

        return deleted;
    }

    public synchronized boolean rename(String oldName, String newName) {
        validateName(oldName);
        validateName(newName);

        if (oldName.equalsIgnoreCase(newName)) {
            // A pure case change cannot be represented reliably on
            // case-insensitive file systems.
            return false;
        }

        Config oldConfig = getConfig(oldName);

        if (oldConfig == null || getFile(newName).exists()) {
            return false;
        }

        File newFile = getFile(newName);

        try {
            Files.move(
                    oldConfig.getFile().toPath(),
                    newFile.toPath()
            );

            if (isCurrent(oldName)) {
                currentConfig = new Config(this, newName, newFile);
                writeCurrentName(newName);
            }

            return true;

        } catch (IOException e) {
            System.err.println(
                    "[OrangeX] Failed to rename config: " + e.getMessage());
            return false;
        }
    }

    public Config getConfig(String name) {
        if (name == null) {
            return null;
        }

        File file = getFile(name);

        if (!file.isFile()) {
            return null;
        }

        return new Config(this, name, file);
    }

    public Config getCurrentConfig() {
        return currentConfig;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void markDirty() {
        dirty = true;
    }

    public synchronized List<Config> getConfigs() {
        File[] files = directory.listFiles(
                (dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(EXTENSION)
        );

        if (files == null) {
            return Collections.emptyList();
        }

        List<Config> configs = new ArrayList<>();

        for (File file : files) {
            String name = file.getName();
            name = name.substring(0, name.length() - EXTENSION.length());

            if (VALID_NAME.matcher(name).matches()) {
                configs.add(new Config(this, name, file));
            }
        }

        configs.sort(Comparator.comparing(
                Config::getName, String.CASE_INSENSITIVE_ORDER));

        return configs;
    }

    public File getDirectory() {
        return directory;
    }

    private boolean isCurrent(String name) {
        return currentConfig != null
                && currentConfig.getName().equalsIgnoreCase(name);
    }

    private File getFile(String name) {
        validateName(name);
        return new File(directory, name + EXTENSION);
    }

    private void validateName(String name) {
        if (name == null || !VALID_NAME.matcher(name).matches()) {
            throw new IllegalArgumentException(
                    "Invalid config name: " + name);
        }
    }

    private void ensureDirectory() {
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IllegalStateException(
                    "Failed to create config directory: " + directory);
        }
    }

    /**
     * @return the last config that was successfully loaded, or {@code null}.
     */
    private String readCurrentName() {
        File file = new File(directory, CURRENT_FILE);

        if (!file.isFile()) {
            return null;
        }

        try {
            String name = new String(
                    Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8)
                    .trim();
            return VALID_NAME.matcher(name).matches() ? name : null;
        } catch (IOException e) {
            return null;
        }
    }

    private void writeCurrentName(String name) {
        try {
            Files.write(
                    new File(directory, CURRENT_FILE).toPath(),
                    name.getBytes(StandardCharsets.UTF_8)
            );
        } catch (IOException e) {
            System.err.println(
                    "[OrangeX] Failed to remember current config: "
                            + e.getMessage());
        }
    }

    private void clearCurrentName() {
        File file = new File(directory, CURRENT_FILE);
        if (file.isFile() && !file.delete()) {
            System.err.println(
                    "[OrangeX] Failed to clear current config marker");
        }
    }

    private void backupUnreadable(String name) {
        File file = getFile(name);

        if (!file.isFile()) {
            return;
        }

        File backup = new File(directory, name + BACKUP_SUFFIX);

        try {
            Files.move(
                    file.toPath(),
                    backup.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );
            System.err.println(
                    "[OrangeX] Backed up unreadable config to " + backup);
        } catch (IOException e) {
            System.err.println(
                    "[OrangeX] Failed to back up config '" + name + "': "
                            + e.getMessage());
        }
    }
}
