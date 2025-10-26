package template.rip.gui.clickguidev.dropdown.impl;

import net.minecraft.client.gui.DrawContext;
import template.rip.api.font.Fonts;
import template.rip.api.util.RenderUtils;
import template.rip.gui.clickguidev.Element;
import template.rip.module.Module;

import java.awt.*;

public class Button extends Element {

    private float x, y;
    private final float width, height;
    private final Module module;

    public Button(float x, float y, float width, float height, Module module) {
        this.width = width;
        this.height = height;
        this.module = module;
    }

    @Override
    public float render(DrawContext context, float x, float y, float delta) {
        RenderUtils.drawRect(context.getMatrices(), x, y, width, height, Color.BLACK.getRGB());
        this.x = x;
        this.y = y;
        float textX = x + 2;
        for (String name : module.getFullName()) {
            Fonts.TEST.get(16).drawString(context.getMatrices(), name, textX, y + 5, module.isEnabled() ? Color.WHITE.getRGB() : Color.GRAY.getRGB());
            textX += Fonts.TEST.get(16).getWidth(name);
        }
        return height;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY, x, y, width, height)) {
            module.toggle();
            return true;
        }
        return false;
    }
}
