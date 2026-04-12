package com.sashafiesta.ccgraphics.mixin;

import com.sashafiesta.ccgraphics.duck.IGraphicsTerminal;
import dan200.computercraft.core.input.ComputerInput;
import dan200.computercraft.core.input.UserComputerInput;
import dan200.computercraft.core.terminal.Terminal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Bypasses UserComputerInput's coordinate clamping when the terminal is in graphics mode.
 * Without this, mouse coordinates are clamped to 1..termWidth / 1..termHeight (character grid),
 * which destroys pixel-precise coordinates needed for graphics modes.
 * <p>
 * Applied on both client and server side (UserComputerInput wraps input on both sides).
 */
@Mixin(value = UserComputerInput.class, remap = false)
abstract class UserComputerInputMixin {
    @Shadow @Final private ComputerInput delegate;
    @Shadow @Final private boolean mouseSupport;
    @Shadow private int lastMouseX;
    @Shadow private int lastMouseY;
    @Shadow private int lastMouseDown;

    @Unique
    private Terminal ccgraphics$terminal;

    @Inject(method = "<init>(Ldan200/computercraft/core/input/ComputerInput;Ldan200/computercraft/core/terminal/Terminal;)V",
            at = @At("TAIL"))
    private void ccgraphics$captureTerminal(ComputerInput delegate, Terminal terminal, CallbackInfo ci) {
        ccgraphics$terminal = terminal;
    }

    @Unique
    private boolean ccgraphics$isGraphicsMode() {
        return ccgraphics$terminal != null && ((IGraphicsTerminal) ccgraphics$terminal).ccgraphics$getGraphicsMode() > 0;
    }

    @Inject(method = "mouseClick(III)V", at = @At("HEAD"), cancellable = true)
    private void ccgraphics$mouseClick(int button, int x, int y, CallbackInfo ci) {
        if (!mouseSupport || button < 1 || button > 3) return;
        if (ccgraphics$isGraphicsMode()) {
            lastMouseX = x;
            lastMouseY = y;
            delegate.mouseClick(button, x, y);
            lastMouseDown = button;
            ci.cancel();
        }
    }

    @Inject(method = "mouseUp(III)V", at = @At("HEAD"), cancellable = true)
    private void ccgraphics$mouseUp(int button, int x, int y, CallbackInfo ci) {
        if (!mouseSupport || button < 1 || button > 3) return;
        if (ccgraphics$isGraphicsMode()) {
            lastMouseX = x;
            lastMouseY = y;
            if (lastMouseDown == button) {
                delegate.mouseUp(button, x, y);
                lastMouseDown = -1;
            }
            ci.cancel();
        }
    }

    @Inject(method = "mouseDrag(III)V", at = @At("HEAD"), cancellable = true)
    private void ccgraphics$mouseDrag(int button, int x, int y, CallbackInfo ci) {
        if (!mouseSupport || button < 1 || button > 3) return;
        if (ccgraphics$isGraphicsMode()) {
            if (button == lastMouseDown && (x != lastMouseX || y != lastMouseY)) {
                delegate.mouseDrag(button, x, y);
                lastMouseX = x;
                lastMouseY = y;
            }
            ci.cancel();
        }
    }

    @Inject(method = "mouseScroll(III)V", at = @At("HEAD"), cancellable = true)
    private void ccgraphics$mouseScroll(int direction, int x, int y, CallbackInfo ci) {
        if (!mouseSupport || direction == 0) return;
        if (ccgraphics$isGraphicsMode()) {
            lastMouseX = x;
            lastMouseY = y;
            delegate.mouseScroll(direction, x, y);
            ci.cancel();
        }
    }
}
