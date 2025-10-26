package template.rip.gui.clickguidev.dropdown;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import template.rip.gui.clickguidev.Array;
import template.rip.gui.clickguidev.dropdown.impl.Panel;
import template.rip.module.Module;

public class Dropdown extends Screen {

    private final Array<Panel> panels;

    public Dropdown() {
        super(Text.of("test"));

        MinecraftClient mc = MinecraftClient.getInstance();
        init(mc, mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight());

        panels = new Array<>(Panel.class, Module.Category.values().length);
        int x = 10;
        for (Module.Category category : Module.Category.values()) {
            panels.add(new Panel(x, 20, 100, 20, category));
            x += 120;
        }
    }

    @Override
    public void init() {
        super.init();
        MinecraftClient.getInstance().mouse.unlockCursor();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        for (Panel panel : panels.toArray()) {
            if (panel != null) {
                panel.render(context, mouseX, mouseY, delta);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        panels.reverse();
        for (Panel panel : panels.toArray()) {
            if (panel.mouseClicked(mouseX, mouseY, button)) {
                panels.remove(panel);
                panels.add(panels.length() - 1, panel);
                return true;
            }
        }
        panels.reverse();
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (Panel panel : panels.toArray()) {
            if (panel.mouseReleased(mouseX, mouseY, button)) {
                return true;
            }
        }
        return true;
    }
}
