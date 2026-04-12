package com.sashafiesta.ccgraphics.compression;

import net.jpountz.lz4.LZ4Factory;

public class LZ4GraphicsCompressor implements GraphicsCompressor {
    public static final byte TYPE_ID = 1;
    public static final String NAME = "lz4";
    public static final LZ4GraphicsCompressor INSTANCE = new LZ4GraphicsCompressor();

    private static final LZ4Factory FACTORY = LZ4Factory.fastestInstance();

    private LZ4GraphicsCompressor() {}

    @Override
    public byte typeId() {
        return TYPE_ID;
    }

    @Override
    public String name() {
        return NAME;
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
