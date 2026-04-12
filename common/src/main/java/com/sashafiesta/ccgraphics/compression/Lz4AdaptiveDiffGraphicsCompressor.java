package com.sashafiesta.ccgraphics.compression;

import net.jpountz.lz4.LZ4Factory;

/**
 * Adaptive diff compressor: like lz4_diff but without timed keyframes.
 * Keyframes are only sent when >50% of pixels changed or on new viewer connect.
 */
public class Lz4AdaptiveDiffGraphicsCompressor implements GraphicsCompressor {
    public static final byte TYPE_ID = 4;
    public static final String NAME = "lz4_adiff";
    public static final Lz4AdaptiveDiffGraphicsCompressor INSTANCE = new Lz4AdaptiveDiffGraphicsCompressor();

    private static final LZ4Factory FACTORY = LZ4Factory.fastestInstance();

    private Lz4AdaptiveDiffGraphicsCompressor() {}

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
    public byte[] compress(byte[] data) {
        return FACTORY.fastCompressor().compress(data);
    }

    @Override
    public byte[] decompress(byte[] data, int expectedSize) {
        return FACTORY.fastDecompressor().decompress(data, expectedSize);
    }
}
