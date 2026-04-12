package com.sashafiesta.ccgraphics.compression;

public class RawGraphicsCompressor implements GraphicsCompressor {
    public static final byte TYPE_ID = 0;
    public static final String NAME = "raw";
    public static final RawGraphicsCompressor INSTANCE = new RawGraphicsCompressor();

    private RawGraphicsCompressor() {}

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
        return data.clone();
    }

    @Override
    public byte[] decompress(byte[] data, int expectedSize) {
        return data;
    }
}
