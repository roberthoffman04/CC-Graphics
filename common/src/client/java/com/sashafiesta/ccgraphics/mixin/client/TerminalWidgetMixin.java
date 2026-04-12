package com.sashafiesta.ccgraphics.mixin.client;

import com.sashafiesta.ccgraphics.duck.IGraphicsTerminal;
import com.mojang.blaze3d.platform.NativeImage;
import dan200.computercraft.client.gui.widgets.TerminalWidget;
import dan200.computercraft.core.input.UserComputerInput;
import dan200.computercraft.core.terminal.Terminal;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.client.Minecraft;

@Mixin(TerminalWidget.class)
abstract class TerminalWidgetMixin {
    @Shadow(remap = false) @Final private Terminal terminal;
    @Shadow(remap = false) @Final private UserComputerInput computerInput;
    @Shadow(remap = false) @Final private int innerX;
    @Shadow(remap = false) @Final private int innerY;
    @Shadow(remap = false) @Final private int innerWidth;
    @Shadow(remap = false) @Final private int innerHeight;

    @Unique private NativeImage ccgraphics$image;
    @Unique private DynamicTexture ccgraphics$texture;
    @Unique private ResourceLocation ccgraphics$textureLocation;
    @Unique private int ccgraphics$texWidth;
    @Unique private int ccgraphics$texHeight;

    @Unique
    private boolean ccgraphics$inGraphicsMode(double mouseX, double mouseY) {
        var gfx = (IGraphicsTerminal) terminal;
        return gfx.ccgraphics$getGraphicsMode() > 0
            && mouseX >= innerX && mouseY >= innerY
            && mouseX < innerX + innerWidth && mouseY < innerY + innerHeight;
    }

    @Unique
    private int ccgraphics$pixelX(double mouseX) {
        return Math.max(0, Math.min((int) (mouseX - innerX),
            ((IGraphicsTerminal) terminal).ccgraphics$getGraphicsWidth() - 1));
    }

    @Unique
    private int ccgraphics$pixelY(double mouseY) {
        return Math.max(0, Math.min((int) (mouseY - innerY),
            ((IGraphicsTerminal) terminal).ccgraphics$getGraphicsHeight() - 1));
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void ccgraphics$onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (ccgraphics$inGraphicsMode(mouseX, mouseY)) {
            computerInput.mouseClick(button + 1, ccgraphics$pixelX(mouseX), ccgraphics$pixelY(mouseY));
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
    private void ccgraphics$onMouseReleased(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (ccgraphics$inGraphicsMode(mouseX, mouseY)) {
            computerInput.mouseUp(button + 1, ccgraphics$pixelX(mouseX), ccgraphics$pixelY(mouseY));
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    private void ccgraphics$onMouseDragged(double mouseX, double mouseY, int button, double v2, double v3, CallbackInfoReturnable<Boolean> cir) {
        if (ccgraphics$inGraphicsMode(mouseX, mouseY)) {
            computerInput.mouseDrag(button + 1, ccgraphics$pixelX(mouseX), ccgraphics$pixelY(mouseY));
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void ccgraphics$onMouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY, CallbackInfoReturnable<Boolean> cir) {
        if (ccgraphics$inGraphicsMode(mouseX, mouseY) && deltaY != 0) {
            computerInput.mouseScroll(deltaY < 0 ? 1 : -1, ccgraphics$pixelX(mouseX), ccgraphics$pixelY(mouseY));
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "renderWidget", at = @At("TAIL"))
    private void ccgraphics$onRenderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        var gfx = (IGraphicsTerminal) terminal;
        if (gfx.ccgraphics$getGraphicsMode() <= 0) {
            if (ccgraphics$texture != null) ccgraphics$close();
            return;
        }

        var gw = gfx.ccgraphics$getGraphicsWidth();
        var gh = gfx.ccgraphics$getGraphicsHeight();

        if (ccgraphics$image == null || ccgraphics$texWidth != gw || ccgraphics$texHeight != gh) {
            ccgraphics$close();
            ccgraphics$texWidth = gw;
            ccgraphics$texHeight = gh;
            ccgraphics$image = new NativeImage(NativeImage.Format.RGBA, gw, gh, false);
            ccgraphics$texture = new DynamicTexture(ccgraphics$image);
            ccgraphics$textureLocation = Minecraft.getInstance().getTextureManager()
                .register("ccgfx_terminal", ccgraphics$texture);
        }

        var buf = gfx.ccgraphics$getGraphics();
        var palette = terminal.getPalette();
        var mode = gfx.ccgraphics$getGraphicsMode();
        for (var y = 0; y < gh; y++) {
            for (var x = 0; x < gw; x++) {
                int argb;
                var colorIndex = buf[y * gw + x] & 0xFF;
                if (colorIndex < 16) {
                    argb = palette.getRenderColours(15 - colorIndex);
                } else {
                    argb = gfx.ccgraphics$getExtPaletteARGB(colorIndex);
                }
                var a = (argb >> 24) & 0xFF;
                var r = (argb >> 16) & 0xFF;
                var g = (argb >> 8) & 0xFF;
                var b = argb & 0xFF;
                var abgr = (a << 24) | (b << 16) | (g << 8) | r;
                ccgraphics$image.setPixelRGBA(x, y, abgr);
            }
        }
        ccgraphics$texture.upload();

        graphics.bufferSource().endBatch();

        int marginColor = 0xFF000000;
        int margin = 2;
        graphics.fill(innerX - margin, innerY - margin, innerX + innerWidth + margin, innerY, marginColor);
        graphics.fill(innerX - margin, innerY + innerHeight, innerX + innerWidth + margin, innerY + innerHeight + margin, marginColor);
        graphics.fill(innerX - margin, innerY, innerX, innerY + innerHeight, marginColor);
        graphics.fill(innerX + innerWidth, innerY, innerX + innerWidth + margin, innerY + innerHeight, marginColor);

        graphics.blit(ccgraphics$textureLocation, innerX, innerY, 0, 0.0f, 0.0f, innerWidth, innerHeight, ccgraphics$texWidth, ccgraphics$texHeight);
    }

    @Unique
    private void ccgraphics$close() {
        if (ccgraphics$texture != null) {
            ccgraphics$texture.close();
            ccgraphics$texture = null;
            ccgraphics$image = null;
        }
        if (ccgraphics$textureLocation != null) {
            Minecraft.getInstance().getTextureManager().release(ccgraphics$textureLocation);
            ccgraphics$textureLocation = null;
        }
    }
}
