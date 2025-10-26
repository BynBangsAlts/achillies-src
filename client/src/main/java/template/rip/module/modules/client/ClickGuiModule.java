package template.rip.module.modules.client;

import template.rip.api.object.Description;
import template.rip.gui.clickguidev.dropdown.Dropdown;
import template.rip.module.Module;

public class ClickGuiModule extends Module {

    public ClickGuiModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    protected void enable() {
        mc.currentScreen = new Dropdown();
    }

    @Override
    protected void disable() {
        mc.currentScreen = null;
    }
}
