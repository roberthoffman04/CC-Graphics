package com.sashafiesta.ccgraphics;

import dan200.computercraft.api.filesystem.Mount;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * A read-only mount backed by files on the classpath (inside the mod JAR).
 * Files are registered at construction time via {@link #addFile(String)}.
 */
public class ClasspathMount implements Mount {
    private final String basePath;
    private final Set<String> files = new HashSet<>();
    private final Set<String> directories = new HashSet<>();

    /**
     * @param basePath classpath prefix, e.g. "data/ccgraphics/lua/rom"
     */
    public ClasspathMount(String basePath) {
        this.basePath = basePath.endsWith("/") ? basePath.substring(0, basePath.length() - 1) : basePath;
        directories.add(""); // root always exists
    }

    /**
     * Register a file path relative to the basePath.
     * E.g. addFile("apis/term.lua") if basePath is "data/ccgraphics/lua/rom".
     */
    public ClasspathMount addFile(String path) {
        files.add(path);
        // Ensure all parent directories are registered
        var parts = path.split("/");
        var dir = new StringBuilder();
        for (var i = 0; i < parts.length - 1; i++) {
            if (i > 0) dir.append("/");
            dir.append(parts[i]);
            directories.add(dir.toString());
        }
        return this;
    }

    @Override
    public boolean exists(String path) {
        return files.contains(path) || directories.contains(path);
    }

    @Override
    public boolean isDirectory(String path) {
        return directories.contains(path);
    }

    @Override
    public void list(String path, List<String> contents) throws IOException {
        if (!directories.contains(path)) throw new FileNotFoundException(path);
        var prefix = path.isEmpty() ? "" : path + "/";
        var seen = new HashSet<String>();
        for (var file : files) {
            if (file.startsWith(prefix)) {
                var rest = file.substring(prefix.length());
                var slash = rest.indexOf('/');
                var entry = slash < 0 ? rest : rest.substring(0, slash);
                if (!entry.isEmpty() && seen.add(entry)) {
                    contents.add(entry);
                }
            }
        }
        for (var dir : directories) {
            if (dir.startsWith(prefix) && !dir.equals(path)) {
                var rest = dir.substring(prefix.length());
                var slash = rest.indexOf('/');
                var entry = slash < 0 ? rest : rest.substring(0, slash);
                if (!entry.isEmpty() && seen.add(entry)) {
                    contents.add(entry);
                }
            }
        }
    }

    @Override
    public long getSize(String path) throws IOException {
        if (!files.contains(path)) throw new FileNotFoundException(path);
        var resource = basePath + "/" + path;
        try (var stream = ClasspathMount.class.getClassLoader().getResourceAsStream(resource)) {
            if (stream == null) throw new FileNotFoundException(resource);
            return stream.available();
        }
    }

    @Override
    public SeekableByteChannel openForRead(String path) throws IOException {
        if (!files.contains(path)) throw new FileNotFoundException(path);
        var resource = basePath + "/" + path;
        var stream = ClasspathMount.class.getClassLoader().getResourceAsStream(resource);
        if (stream == null) throw new FileNotFoundException(resource);
        // Read fully into a byte array and wrap as SeekableByteChannel
        var bytes = stream.readAllBytes();
        stream.close();
        return new dan200.computercraft.core.apis.handles.ArrayByteChannel(bytes);
    }
}
