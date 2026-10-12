package com.kompsos.system.lib;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class GraphicsUtil {
    public static void drawTextList(Font font, GuiGraphicsExtractor drawContext, int x, int y, int color, int verticalPadding, Component... texts) {
        for (int i = 0; i < texts.length; i++) {
            drawContext.text(font, texts[i], x,((font.lineHeight + verticalPadding) * i) + y, color);
        }
    }
}
