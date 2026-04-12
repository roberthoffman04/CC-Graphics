package com.sashafiesta.ccgraphics.compression;

import net.jpountz.lz4.LZ4Factory;

/**
 * Temporal diff compressor: expects XOR'd data (current ^ previous frame),
 * compresses with LZ4. The XOR logic itself lives in NetworkedTerminalMixin.
 */
public class Lz4DiffGraphicsCompressor implements GraphicsCompressor {
    public static final byte TYPE_ID = 3;
    public static final String NAME = "lz4_diff";
    public static final Lz4DiffGraphicsCompressor INSTANCE = new Lz4DiffGraphicsCompressor();

    private static final LZ4Factory FACTORY = LZ4Factory.fastestInstance();

    private Lz4DiffGraphicsCompressor() {}

    @Override
    public byte typeId() {
        return TYPE_ID;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public boolean isDiff() {
        return true;
    }

    @Override
    public boolean hasTimedKeyframes() {
        return true;
    }

    @Override
    public byte[] compress(byte[] data) {
        return FACTORY.fastCompressor().compress(data);
    }

    @Override
    public byte[] decompress(byte[] data, int expectedSize) {
        return FACTORY.fastDecompressor().decompress(data, expectedSize);
    }
}
