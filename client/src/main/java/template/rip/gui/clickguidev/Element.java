package template.rip.gui.clickguidev;

import net.minecraft.client.gui.DrawContext;

public abstract class Element {

    public abstract float render(DrawContext context, float mouseX, float mouseY, float delta);

    public abstract boolean mouseClicked(double mouseX, double mouseY, int button);

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    public boolean isHovered(double mouseX, double mouseY, double x, double y, double width, double height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
