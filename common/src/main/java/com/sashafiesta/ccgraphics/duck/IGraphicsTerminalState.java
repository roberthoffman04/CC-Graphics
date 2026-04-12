package com.sashafiesta.ccgraphics.duck;

public interface IGraphicsTerminalState {
    int ccgraphics$getGraphicsMode();

    byte ccgraphics$getGraphicsCompressionType();

    byte[] ccgraphics$getGraphicsData();

    void ccgraphics$setGraphicsData(int mode, byte compressionType, byte[] data);

    int[] ccgraphics$getExtPaletteData();

    void ccgraphics$setExtPaletteData(int[] data);
}
