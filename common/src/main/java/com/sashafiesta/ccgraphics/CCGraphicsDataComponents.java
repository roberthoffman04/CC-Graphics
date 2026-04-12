package com.sashafiesta.ccgraphics;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;

public final class CCGraphicsDataComponents {
    public static DataComponentType<Boolean> GRAPHICS_DISABLED;

    private CCGraphicsDataComponents() {}

    public static DataComponentType<Boolean> buildGraphicsDisabled() {
        return DataComponentType.<Boolean>builder()
            .persistent(Codec.BOOL)
            .networkSynchronized(ByteBufCodecs.BOOL)
            .build();
    }
}
