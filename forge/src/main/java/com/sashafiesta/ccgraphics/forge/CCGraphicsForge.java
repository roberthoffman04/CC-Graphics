package com.sashafiesta.ccgraphics.forge;

import com.sashafiesta.ccgraphics.CCGraphicsConfig;
import com.sashafiesta.ccgraphics.CCGraphicsDataComponents;
import com.sashafiesta.ccgraphics.CompressionType;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod("ccgraphics")
public class CCGraphicsForge {
    private static final DeferredRegister<DataComponentType<?>> COMPONENTS =
        DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, "ccgraphics");

    private static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> GRAPHICS_DISABLED =
        COMPONENTS.register("graphics_disabled", () -> {
            var type = CCGraphicsDataComponents.buildGraphicsDisabled();
            CCGraphicsDataComponents.GRAPHICS_DISABLED = type;
            return type;
        });

    private static ModConfigSpec.BooleanValue allowGrayscaleGraphics;
    private static ModConfigSpec.EnumValue<CompressionType> compression;

    public CCGraphicsForge(IEventBus modBus, ModContainer container) {
        COMPONENTS.register(modBus);

        var builder = new ModConfigSpec.Builder();
        allowGrayscaleGraphics = builder
            .comment(
                "Allow graphics mode on non-color (standard) computers with grayscale rendering.",
                "When false (default), graphics mode is blocked on non-color computers (CraftOS-PC compatible)."
            )
            .define("allow_grayscale_graphics", false);
        compression = builder
            .comment(
                "Compression algorithm for graphics data sent over the network."
            )
            .defineEnum("compression", CompressionType.LZ4_DIFF);
        var spec = builder.build();

        container.registerConfig(ModConfig.Type.SERVER, spec);
        modBus.addListener(CCGraphicsForge::onConfigEvent);
    }

    private static void onConfigEvent(ModConfigEvent event) {
        if (event instanceof ModConfigEvent.Unloading) return;
        CCGraphicsConfig.setAllowGrayscaleGraphics(allowGrayscaleGraphics.get());
        CCGraphicsConfig.setCompression(compression.get());
    }
}
