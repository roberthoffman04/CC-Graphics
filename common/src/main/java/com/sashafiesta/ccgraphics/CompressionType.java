package com.sashafiesta.ccgraphics;

import com.sashafiesta.ccgraphics.compression.GraphicsCompressor;

public enum CompressionType {
    RAW("raw"),
    LZ4("lz4"),
    LZ4_DELTA("delta_lz4"),
    LZ4_DIFF("lz4_diff"),
    LZ4_ADIFF("lz4_adiff");

    private final String compressorName;

    CompressionType(String compressorName) {
        this.compressorName = compressorName;
    }

    public String compressorName() {
        return compressorName;
    }

    public void apply() {
        GraphicsCompressor.setDefault(compressorName);
    }
}
