package com.sashafiesta.ccgraphics.mixin;

import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.computer.terminal.NetworkedTerminal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ServerComputer.class, remap = false)
public interface ServerComputerAccessor {
    @Accessor
    NetworkedTerminal getTerminal();
}
