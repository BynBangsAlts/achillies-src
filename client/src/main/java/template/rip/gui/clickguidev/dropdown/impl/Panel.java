package template.rip.gui.clickguidev.dropdown.impl;

import net.minecraft.client.gui.DrawContext;
import template.rip.Template;
import template.rip.api.font.Fonts;
import template.rip.api.util.RenderUtils;
import template.rip.gui.clickguidev.Element;
import template.rip.module.Module;

import java.awt.*;
import java.util.TreeSet;

public class Panel extends Element {

    private final Button[] buttons;
    private final float width, height;
    private final Module.Category category;
    private float x, y, dragX, dragY;
    private boolean dragging;

    public Panel(float x, float y, float width, float height, Module.Category category) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.category = category;

        TreeSet<Module> modules = Template.moduleManager.getModulesByCategory(category);
        buttons = new Button[modules.size()];

        float moduleY = y + 20;
        int i = 0;
        for (Module module : modules) {
            buttons[i] = new Button(x, moduleY, width, 20, module);
            moduleY += 20;
            i++;
        }
    }

    @Override
    public float render(DrawContext context, float mouseX, float mouseY, float delta) {
        if (dragging) {
            x = mouseX - dragX;
            y = mouseY - dragY;
        }

        RenderUtils.drawRect(context.getMatrices(), x, y, width, height, Color.GRAY.getRGB());
        Fonts.TEST.get(20).drawString(context.getMatrices(), category.name().toLowerCase(), x + 2, y + 5, -1);

        float buttonY = y + 20;
        for (Button button : buttons) {
            buttonY += button.render(context, x, buttonY, delta);
        }
        return 0;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY, x, y, width, height)) {
            dragging = true;
            dragX = (float) (mouseX - x);
            dragY = (float) (mouseY - y);
            return true;
        }
        for (Button b : buttons) {
            if (b.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (dragging) {
            dragging = false;
            return true;
        }
        return false;
    }
}
