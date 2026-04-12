package com.sashafiesta.ccgraphics;

import com.electronwill.nightconfig.core.ConfigSpec;
import com.electronwill.nightconfig.core.EnumGetMethod;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import com.electronwill.nightconfig.core.file.FileWatcher;
import com.electronwill.nightconfig.core.io.WritingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

public final class CCGraphicsConfig {
    private static final Logger LOG = LoggerFactory.getLogger(CCGraphicsConfig.class);

    private static final ConfigSpec SPEC = new ConfigSpec();

    static {
        SPEC.define("allow_grayscale_graphics", false, o -> o instanceof Boolean);
        SPEC.define("compression", CompressionType.LZ4_DIFF.name(),
            o -> o instanceof String s && Arrays.stream(CompressionType.values())
                .anyMatch(e -> e.name().equalsIgnoreCase(s)));
    }

    private static boolean allowGrayscaleGraphics = false;
    private static CommentedFileConfig config;

    private CCGraphicsConfig() {}

    public static boolean allowGrayscaleGraphics() {
        return allowGrayscaleGraphics;
    }

    public static void setAllowGrayscaleGraphics(boolean value) {
        allowGrayscaleGraphics = value;
    }

    public static void setCompression(CompressionType type) {
        type.apply();
    }

    /**
     * Load config using NightConfig. Used by Fabric; NeoForge uses its own config system.
     */
    public static synchronized void load(Path... paths) {
        if (paths.length == 0) return;
        unload();

        var path = Arrays.stream(paths).filter(Files::exists).findFirst().orElseGet(() -> paths[paths.length - 1]);

        config = CommentedFileConfig.builder(path).sync()
            .onFileNotFound(FileNotFoundAction.READ_NOTHING)
            .writingMode(WritingMode.REPLACE)
            .build();

        try {
            Files.createDirectories(path.getParent());
            FileWatcher.defaultInstance().addWatch(config.getNioPath(), CCGraphicsConfig::reload);
        } catch (IOException e) {
            LOG.error("Failed to watch config at {}.", path, e);
        }

        if (reload()) config.save();
    }

    public static synchronized void unload() {
        if (config == null) return;
        config.close();
        FileWatcher.defaultInstance().removeWatch(config.getNioPath());
        config = null;
        allowGrayscaleGraphics = false;
        CompressionType.LZ4_DIFF.apply();
    }

    private static synchronized boolean reload() {
        if (config == null) return false;

        LOG.info("Loading ccgraphics config from {}", config.getNioPath());
        config.load();

        var allowedValues = Arrays.stream(CompressionType.values())
            .map(Enum::name)
            .collect(Collectors.joining(", "));

        var isNew = config.isEmpty();
        config.setComment("allow_grayscale_graphics",
            " Allow graphics mode on non-color (standard) computers with grayscale rendering.\n" +
            " When false (default), graphics mode is blocked on non-color computers (CraftOS-PC compatible).");
        config.setComment("compression",
            " Compression algorithm for graphics data sent over the network.\n" +
            " Allowed values: " + allowedValues);

        var corrected = isNew ? SPEC.correct(config) : SPEC.correct(config, (action, entryPath, oldValue, newValue) ->
            LOG.warn("Corrected config key {} from {} to {}", String.join(".", entryPath), oldValue, newValue));

        allowGrayscaleGraphics = config.getOrElse("allow_grayscale_graphics", false);
        config.<CompressionType>getEnumOrElse("compression", CompressionType.LZ4_DIFF, EnumGetMethod.NAME_IGNORECASE).apply();

        return corrected > 0;
    }
}
