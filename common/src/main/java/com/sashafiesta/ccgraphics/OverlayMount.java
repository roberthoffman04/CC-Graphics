package com.sashafiesta.ccgraphics;

import dan200.computercraft.api.filesystem.Mount;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * A read-only mount that overlays one mount on top of another.
 * Files in the overlay take precedence over the base mount.
 */
public class OverlayMount implements Mount {
    private final Mount base;
    private final Mount overlay;

    public OverlayMount(Mount base, Mount overlay) {
        this.base = base;
        this.overlay = overlay;
    }

    @Override
    public boolean exists(String path) throws IOException {
        return overlay.exists(path) || base.exists(path);
    }

    @Override
    public boolean isDirectory(String path) throws IOException {
        // A path is a directory if it's a directory in either mount
        if (overlay.exists(path)) {
            if (overlay.isDirectory(path)) return true;
            // Overlay has a file here - not a directory, even if base has a directory
            return false;
        }
        return base.exists(path) && base.isDirectory(path);
    }

    @Override
    public void list(String path, List<String> contents) throws IOException {
        var seen = new LinkedHashSet<String>();

        // Collect from base first
        if (base.exists(path) && base.isDirectory(path)) {
            base.list(path, contents);
            seen.addAll(contents);
        }

        // Add overlay entries, deduplicating
        if (overlay.exists(path) && overlay.isDirectory(path)) {
            var overlayContents = new java.util.ArrayList<String>();
            overlay.list(path, overlayContents);
            for (var entry : overlayContents) {
                if (seen.add(entry)) {
                    contents.add(entry);
                }
            }
        }
    }

    @Override
    public long getSize(String path) throws IOException {
        if (overlay.exists(path)) return overlay.getSize(path);
        return base.getSize(path);
    }

    @Override
    public SeekableByteChannel openForRead(String path) throws IOException {
        if (overlay.exists(path)) return overlay.openForRead(path);
        return base.openForRead(path);
    }

    @Override
    public BasicFileAttributes getAttributes(String path) throws IOException {
        if (overlay.exists(path)) return overlay.getAttributes(path);
        return base.getAttributes(path);
    }
}
