package dev.thefletchingtable.client;

import dev.thefletchingtable.FletchingMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class FletchingScreen extends AbstractContainerScreen<FletchingMenu> {

    // vanilla container colors
    private static final int BG_FILL  = 0xFFC6C6C6;
    private static final int LIGHT    = 0xFFFFFFFF;
    private static final int SHADOW   = 0xFF555555;
    private static final int SLOT_BG  = 0xFF8B8B8B;
    private static final int SLOT_DK  = 0xFF373737;
    private static final int ARROW_C  = 0xFF808080;

    public FletchingScreen(FletchingMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    // RENDERING //

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        renderPanel(g, x, y, imageWidth, imageHeight);

        // input slots
        renderSlotBg(g, x + 26, y + 35);
        renderSlotBg(g, x + 50, y + 35);
        renderSlotBg(g, x + 74, y + 35);

        // crafting arrow
        renderArrow(g, x + 99, y + 38);

        // result slot; larger 24x24 frame
        renderResultSlotBg(g, x + 130, y + 31);

        // player inventory slots
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                renderSlotBg(g, x + 8 + col * 18, y + 84 + row * 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            renderSlotBg(g, x + 8 + col * 18, y + 142);
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }

    // PANEL //

    private void renderPanel(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, BG_FILL);
        // outer bevel; top & left highlight
        g.fill(x, y, x + w - 1, y + 1, LIGHT);
        g.fill(x, y + 1, x + 1, y + h - 1, LIGHT);
        // outer bevel; bottom & right shadow
        g.fill(x + 1, y + h - 1, x + w, y + h, SHADOW);
        g.fill(x + w - 1, y + 1, x + w, y + h - 1, SHADOW);
    }

    // SLOTS //

    private void renderSlotBg(GuiGraphics g, int sx, int sy) {
        // top & left dark border
        g.fill(sx - 1, sy - 1, sx + 17, sy, SLOT_DK);
        g.fill(sx - 1, sy,     sx,     sy + 16, SLOT_DK);
        // bottom & right light border
        g.fill(sx,     sy + 16, sx + 17, sy + 17, LIGHT);
        g.fill(sx + 16, sy,     sx + 17, sy + 16, LIGHT);
        // inner fill
        g.fill(sx, sy, sx + 16, sy + 16, SLOT_BG);
    }

    private void renderResultSlotBg(GuiGraphics g, int sx, int sy) {
        int s = 24;
        // dark border top & left
        g.fill(sx - 1, sy - 1, sx + s + 1, sy, SLOT_DK);
        g.fill(sx - 1, sy,     sx,         sy + s, SLOT_DK);
        // light border bottom & right
        g.fill(sx,     sy + s, sx + s + 1, sy + s + 1, LIGHT);
        g.fill(sx + s, sy,     sx + s + 1, sy + s, LIGHT);
        // inner fill
        g.fill(sx, sy, sx + s, sy + s, SLOT_BG);
    }

    // ARROW //

    private void renderArrow(GuiGraphics g, int ax, int ay) {
        // shaft
        g.fill(ax, ay + 3, ax + 16, ay + 6, ARROW_C);
        // arrowhead triangle
        g.fill(ax + 13, ay + 1, ax + 16, ay + 8, ARROW_C);
        g.fill(ax + 16, ay + 2, ax + 18, ay + 7, ARROW_C);
        g.fill(ax + 18, ay + 3, ax + 20, ay + 6, ARROW_C);
        g.fill(ax + 20, ay + 4, ax + 21, ay + 5, ARROW_C);
    }
}
