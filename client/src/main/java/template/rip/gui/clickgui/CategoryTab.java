package template.rip.gui.clickgui;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.ImVec4;
import imgui.flag.ImDrawFlags;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import template.rip.Template;
import template.rip.api.font.JColor;
import template.rip.api.object.ToolTipHolder;
import template.rip.api.util.GuiUtils;
import template.rip.api.util.KeyUtils;
import template.rip.api.util.RenderUtils;
import template.rip.api.util.UI;
import template.rip.gui.ImguiLoader;
import template.rip.gui.utils.Renderable;
import template.rip.gui.utils.Theme;
import template.rip.module.Module;
import template.rip.module.modules.client.AchillesSettingsModule;

import java.util.ArrayList;

public class CategoryTab implements Renderable {

    public Module.Category category;
    private boolean firstFrame, isWindowFocused, isWindowHovered;
    private final float posX, posY;
    public float scrollY;
    public float scrollUntil = 0;
    public float maxY = 40;

    public boolean isCollapsed = false;
    public long lastOpen = 0;

    public CategoryTab(Module.Category category, float posX, float posY) {
        this.category = category;
        this.posX = posX;
        this.posY = posY;
        this.scrollY = 0;
        this.firstFrame = true;
        this.isWindowFocused = false;
        this.isWindowHovered = false;
    }

    public boolean isWindowFocused() {
        return isWindowFocused;
    }

    public boolean isWindowHovered() {
        return isWindowHovered;
    }

    @Override
    public String getName() {
        return category.name;
    }

    @Override
    public void render() {
        String name = getName();
        if (name == null) return;
        switch (Template.moduleManager.getModule(AchillesSettingsModule.class).clickGuiStyle.getMode()) {
            case Glass -> {
                //
                // Glass ClickGUI
                //

                int imGuiWindowFlags = 0;
                imGuiWindowFlags |= ImGuiWindowFlags.AlwaysAutoResize;
                imGuiWindowFlags |= ImGuiWindowFlags.NoDocking;
                imGuiWindowFlags |= ImGuiWindowFlags.NoTitleBar;

                float percentage = (float) Math.sin((((double) Math.min(System.currentTimeMillis() - 250L - lastOpen, 250L) / 250L) * Math.PI) / 2);
                float percentage1 = (float) Math.sin((((double) Math.min(System.currentTimeMillis() - lastOpen, 250L) / 250L) * Math.PI) / 2);

                if (isCollapsed) percentage1 = 1 - percentage1;
                if (isCollapsed) percentage = 1 - percentage;
                if (percentage > 1) percentage = 1;

                if (!isCollapsed) {
                    float temp = percentage1;
                    percentage1 = percentage;
                    percentage = temp;
                } else {
                    percentage = 1f - (float) Math.sin((((double) Math.min(System.currentTimeMillis() - lastOpen, 250L) / 250L) * Math.PI) / 2);
                    percentage1 = 0f;
                }
                ImGui.getStyle().setButtonTextAlign(0, 0.5f);
                //ImGui.pushFont(ImguiLoader.getNormalFontAwesome());

                float windowY = maxY * percentage;
                if (windowY < 1.0f) windowY = 30f;
                if (percentage1 == 1f && isCollapsed) windowY = 30;
                if (percentage == 1f && !isCollapsed) windowY = 30;
                ImGui.getStyle().setWindowMinSize(200, windowY);

                ImGui.getStyle().setWindowPadding(0f, 0f);
                ImGui.getStyle().setFramePadding(0f, 0f);
                ImGui.getStyle().setCellPadding(0f, 0f);
                ImGui.getStyle().setItemSpacing(8, 4);
                ImGui.getStyle().setItemInnerSpacing(4, 4);
                ImGui.getStyle().setWindowBorderSize(1f);
                ImGui.getStyle().setFrameBorderSize(0f);

                ImGui.begin(name, imGuiWindowFlags);

                if (scrollUntil > ImGui.getScrollMaxY()) {
                    scrollUntil = ImGui.getScrollMaxY();
                } else if (scrollUntil < 0) {
                    scrollUntil = 0;
                }

                scrollY = (float) (scrollY + 0.2 * (scrollUntil - scrollY));
                ImGui.setScrollY(scrollY);

                ImGui.getBackgroundDrawList().addRectFilled(
                        ImGui.getCursorScreenPos().x,
                        ImGui.getCursorScreenPos().y,
                        ImGui.getCursorScreenPos().x + ImGui.getWindowSize().x,
                        ImGui.getCursorScreenPos().y + 40,
                        0xFF202430,
                        8f,
                        ImDrawFlags.None);
                //ImGui.getForegroundDrawList().addRectFilled(0, 0, ImGui.getWindowSize().x, 40f, 0xFFFFFF);
                ImGui.setCursorPosY(ImGui.getCursorPosY() + 10);
                ImGui.setCursorPosX(ImGui.getCursorPosX() + 10);
                ImGui.pushFont(ImguiLoader.fontAwesome20);
                ImGui.text(name.substring(0, 1));
                ImGui.popFont();
                ImGui.sameLine();
                ImGui.pushFont(ImguiLoader.mediumPoppins24);

                float w = ImGui.getWindowSize().x;
                float t = ImGui.calcTextSize(name.substring(2)).x;
                float x = ImGui.getCursorPosX();

                ImGui.setCursorPosX((w - t) * 0.5f);
                ImGui.text(name.substring(2));
                ImGui.setCursorPosX(x);
                ImGui.popFont();
                ImGui.pushFont(ImguiLoader.poppins20);
                ImGui.getStyle().setFramePadding(4, 6);
                ImGui.getStyle().setCellPadding(4, 4);
                ImGui.getStyle().setWindowPadding(4, 4);

                ImGui.setCursorPos(0, 40);

                if (ImGui.isMouseClicked(1)) {
                    float mouseX = ImGui.getMousePosX();
                    float mouseY = ImGui.getMousePosY();
                    if (mouseX >= ImGui.getCursorScreenPos().x && mouseX <= ImGui.getCursorScreenPos().x + ImGui.getWindowSize().x &&
                            mouseY >= ImGui.getCursorScreenPos().y - 40 && mouseY <= ImGui.getCursorScreenPos().y) {
                        lastOpen = System.currentTimeMillis();
                        isCollapsed = !isCollapsed;
                    }
                }
                if (firstFrame) {
                    ImGui.setWindowPos(posX, posY);
                    firstFrame = false;
                }

                isWindowFocused = ImGui.isWindowFocused();
                isWindowHovered = ImGui.isWindowHovered();

                float[] color = JColor.getGuiColor().getFloatColor();
                ImGui.pushStyleColor(ImGuiCol.Button, color[0], color[1], color[2], 1f);
                ImGui.pushStyleColor(ImGuiCol.ButtonHovered, color[0], color[1], color[2], 1f);
                ImGui.pushStyleColor(ImGuiCol.ButtonActive, color[0], color[1], color[2], 1f);
                ImVec2 pos = ImGui.getCursorPos();
                ImGui.setCursorPos(pos.x, pos.y);
                ImGui.button("#", 200f, 2f);
                ImGui.popStyleColor(3);

                if (isCollapsed || percentage != 1f) {
                    ImGui.getStyle().setWindowPadding(0f, 0f);
                    ImGui.getStyle().setFramePadding(0f, 0f);
                    ImGui.getStyle().setCellPadding(0f, 0f);
                    ImGui.popFont();
                    ImGui.setCursorPos(pos.x, pos.y + 3);
                    ImGui.end();
                    return;
                }
                ImGui.setCursorPos(pos.x, pos.y + 10);
                ArrayList<Module> toToggle = new ArrayList<>();
                for (Module module : Template.moduleManager.getModulesByCategory(category)) {
                    if (module.isNotSearched()) {
                        continue;
                    }
                    if (module.getName() == null) {
                        ImGui.end();
                        return;
                    }
                    ImGui.pushID(module.getName());

                    // if (module.isEnabled()) {
                    //     float[] color = JColor.getGuiColor().getFloatColor();

                    //     //ImGui.pushStyleColor(ImGuiCol.Text, 0.80f, 0.84f, 0.96f, 1.00f);
                    //     //ImGui.pushStyleColor(ImGuiCol.Button, color[0], color[1], color[2], 1f);
                    //     //ImGui.pushStyleColor(ImGuiCol.ButtonHovered, color[0], color[1], color[2], 1f);
                    //     //ImGui.pushStyleColor(ImGuiCol.ButtonActive, color[0], color[1], color[2], 1f);
                    //     ImGui.pushStyleColor(ImGuiCol.Text, color[0], color[1], color[2], 1f);
                    // } else {
                    //     //ImGui.pushStyleColor(ImGuiCol.Text, 0.42f, 0.44f, 0.53f, 1.00f);
                    //     //ImGui.pushStyleColor(ImGuiCol.Button,        0.13f, 0.14f, 0.19f, 1f);
                    //     //ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.15f, 0.16f, 0.21f, 1f);
                    //     //ImGui.pushStyleColor(ImGuiCol.ButtonActive,  0.17f, 0.18f, 0.23f, 1f);
                    //     ImGui.pushStyleColor(ImGuiCol.Text, 0.80f, 0.84f, 0.96f, 1.00f);
                    // }
                    ImGui.pushStyleColor(ImGuiCol.Button, 0f, 0f, 0f, 0f);
                    ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0f, 0f, 0f, 0f);
                    ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0f, 0f, 0f, 0f);

                    ImVec2 pos1 = ImGui.getCursorPos();
                    boolean isToggled = ImGui.button("", 190f, 30f);
                    ImGui.popStyleColor(3);
                    ImVec2 pos2 = ImGui.getCursorPos();

                    if (isToggled) {
                        toToggle.add(module);
                    }

                    boolean isModuleHovered = ImGui.isItemHovered();
                    if (isModuleHovered) {
                        ToolTipHolder.setToolTip(module.getDescription().getContent());

                        if (ImGui.isMouseClicked(1)) {
                            module.toggleShowOptions();
                            //System.out.println(module.getName());
                            ImGui.setWindowFocus(name);
                        }
                    }

                    if (percentage1 == 1f) {
                        JColor disabledColor = new JColor(0.38f, 0.43f, 0.55f, 1f);
                        disabledColor = isModuleHovered ? disabledColor.jBrighter() : disabledColor;

                        JColor enabledColor = new JColor(0.76f, 0.82f, 0.92f, 1f);
                        enabledColor = isModuleHovered ? enabledColor.jBrighter() : enabledColor;

                        if (module.nameColor == null) module.nameColor = module.isEnabled() ? enabledColor : disabledColor;

                        JColor moduleColor = module.isEnabled() ? module.nameColor.smoothTransition(enabledColor, 0.15f) : module.nameColor.smoothTransition(disabledColor, 0.15f);
                        module.nameColor = moduleColor;
                        float[] color1 = moduleColor.getFloatColorWAlpha();
                        ImGui.pushStyleColor(ImGuiCol.Text, color1[0], color1[1], color1[2], color1[3]);
                    } else {
                        if (module.isEnabled()) {
                            ImGui.pushStyleColor(ImGuiCol.Text, 0.76f, 0.82f, 0.92f, percentage1);
                        } else {
                            ImGui.pushStyleColor(ImGuiCol.Text, 0.38f, 0.43f, 0.55f, percentage1);
                        }
                    }

                    ImVec2 size = RenderUtils.calcTextSize(module.getFullName());
                    ImGui.setCursorPos(pos1.x + 100 - size.x / 2, pos1.y + 15 - size.y / 2);

                    ImVec2 prePos = ImGui.getCursorPos();

                    RenderUtils.drawTexts(module.getFullName());

                    ImGui.popStyleColor(1);

                    ImGui.setCursorPos(180, prePos.y);
                    ImGui.pushStyleColor(ImGuiCol.Text, 0.83f, 0.86f, 0.94f, percentage1);
                    if (module.showOptions()) {
                        ImGui.text("\uF077");
                    } else {
                        ImGui.text("\uF078");
                    }
                    ImGui.popStyleColor(1);

                    ImGui.setCursorPos(pos2.x, pos2.y);

                    if (module.showOptions()) {
                        float settingsHeight = module.getSettingsHeight();
                        float percentageSettingsOpacity = (float) Math.sin((((double) Math.min((System.currentTimeMillis() - 150L) - module.lastToggleOptions, 150L) / 150L) * Math.PI) / 2);

                        module.setAnimatingOpening(percentageSettingsOpacity != 1);
//                    System.out.println(module.isAnimatingOpening());

                        if (module.settingsOpenProgress == settingsHeight) {
                            ImGui.getStyle().setFramePadding(4, 4);
                            ImGui.getStyle().setButtonTextAlign(0.5f, 0.5f);

                            ImGui.indent(10f);

                            boolean isAnimationDone = percentageSettingsOpacity == 1;

                            if (!isAnimationDone) {
                                ImVec4 colorText = ImGui.getStyle().getColor(ImGuiCol.Text);
                                ImGui.pushStyleColor(ImGuiCol.Text, colorText.x, colorText.y, colorText.z, colorText.w * percentageSettingsOpacity);
                            }
                            ImGui.pushFont(ImguiLoader.poppins18);
                            module.renderSettings();
                            ImGui.popFont();
                            if (!isAnimationDone) ImGui.popStyleColor(1);

                            ImGui.unindent(10f);

                            ImGui.getStyle().setButtonTextAlign(0f, 0f);
                            ImGui.getStyle().setFramePadding(4, 6);
                        } else {
                            float percentageSettings = (float) Math.sin((((double) Math.min(System.currentTimeMillis() - module.lastToggleOptions, 150L) / 150L) * Math.PI) / 2);

                            float height = settingsHeight * percentageSettings;
                            ImGui.dummy(1f, height);

                            module.settingsOpenProgress = height;
                        }
                    } else if (module.settingsOpenProgress != 0) {
                        float settingsHeight = module.getSettingsHeight();
                        float percentageSettingsOpacity = 1f - (float) Math.sin((((double) Math.min(System.currentTimeMillis() - module.lastToggleOptions, 100L) / 100L) * Math.PI) / 2);

                        module.setAnimatingOpening(percentageSettingsOpacity != 0);

                        if (percentageSettingsOpacity != 0) {
                            ImGui.getStyle().setFramePadding(4, 4);
                            ImGui.getStyle().setButtonTextAlign(0.5f, 0.5f);

                            ImGui.indent(10f);

                            boolean isAnimationDone = percentageSettingsOpacity == 1;

                            if (!isAnimationDone) {
                                ImVec4 colorText = ImGui.getStyle().getColor(ImGuiCol.Text);
                                ImGui.pushStyleColor(ImGuiCol.Text, colorText.x, colorText.y, colorText.z, colorText.w * percentageSettingsOpacity);
                            }
                            ImGui.pushFont(ImguiLoader.poppins18);
                            module.renderSettings();
                            ImGui.popFont();
                            if (!isAnimationDone) ImGui.popStyleColor(1);

                            ImGui.unindent(10f);

                            ImGui.getStyle().setButtonTextAlign(0f, 0f);
                            ImGui.getStyle().setFramePadding(4, 6);
                        } else {
                            float percentageSettings = 1f - (float) Math.sin((((double) Math.min(System.currentTimeMillis() - 100L - module.lastToggleOptions, 150L) / 150L) * Math.PI) / 2);

                            module.setAnimatingOpening(percentageSettings != 1);

                            float height = settingsHeight * percentageSettings;

                            ImGui.getStyle().setWindowPadding(0f, 0f);
                            ImGui.getStyle().setFramePadding(0f, 0f);
                            ImGui.getStyle().setCellPadding(0f, 0f);
                            ImGui.dummy(1f, height);
                            ImGui.getStyle().setButtonTextAlign(0f, 0f);
                            ImGui.getStyle().setFramePadding(4, 6);

                            module.settingsOpenProgress = height;
                        }
                    }
                    ImGui.popID();
                }
                ImGui.popFont();
                ImGui.setCursorPos(0, ImGui.getCursorPosY() + 5);

                maxY = ImGui.getWindowSizeY();

                //GuiUtils.drawShadow(
                //        new ImVec2(ImGui.getWindowPosX()+20,
                //                ImGui.getWindowPosY()+20),
                //        new ImVec2(ImGui.getWindowPosX()+ImGui.getWindowSizeX()-20,
                //                ImGui.getWindowPosY()+ImGui.getWindowSizeY()-20),
                //        -12,
                //        ImGui.getColorU32(0.13f, 0.14f, 0.19f, 0f),
                //        ImGui.getColorU32(0.13f, 0.14f, 0.19f, 0.5f),
                //        20
                //);
                //uiUtils.drawWindowShadow(ImGui.getColorU32(0f, 0f, 0f, 0.3f),
                //       ImGui.getColorU32(0f, 0f, 0f, 0f),
                //       5f);
                ImGui.end();
                toToggle.forEach(Module::toggle);
            }
            case Flat -> {
                //
                // Flat ClickGUI
                //

                int imGuiWindowFlags = 0;
                imGuiWindowFlags |= ImGuiWindowFlags.AlwaysAutoResize;
                imGuiWindowFlags |= ImGuiWindowFlags.NoDocking;
                imGuiWindowFlags |= ImGuiWindowFlags.NoTitleBar;

                float percentage = 1f;
                float percentage1 = 1f;

                if (isCollapsed) {
                    percentage = 0f;
                    percentage1 = 0f;
                }
                ImGui.getStyle().setButtonTextAlign(0, 0.5f);

                ImGui.getStyle().setWindowPadding(0f, 0f);
                ImGui.getStyle().setFramePadding(0f, 0f);
                ImGui.getStyle().setCellPadding(0f, 0f);
                ImGui.getStyle().setItemSpacing(0, 1);
                ImGui.getStyle().setItemInnerSpacing(0, 0);
                ImGui.getStyle().setWindowBorderSize(0f);
                ImGui.getStyle().setFrameBorderSize(0f);

                float windowY = maxY * percentage;
                if (windowY < 1.0f) windowY = 30f;
                if (percentage1 == 1f && isCollapsed) windowY = 30;
                if (percentage == 1f && !isCollapsed) windowY = 30;
                ImGui.getStyle().setWindowMinSize(200, windowY);

                ImGui.begin(name, imGuiWindowFlags);
                ImGui.getStyle().setWindowBorderSize(1f);
                if (scrollUntil > ImGui.getScrollMaxY()) {
                    scrollUntil = ImGui.getScrollMaxY();
                } else if (scrollUntil < 0) {
                    scrollUntil = 0;
                }

                scrollY = (float) (scrollY + 0.2 * (scrollUntil - scrollY));
                ImGui.setScrollY(scrollY);

                ImGui.pushFont(ImguiLoader.poppins20);
                ImGui.getStyle().setFramePadding(4, 6);
                ImGui.getStyle().setCellPadding(4, 4);
                ImGui.getStyle().setWindowPadding(4, 4);

                ImGui.setCursorPos(0, 40);

                if (ImGui.isMouseClicked(1)) {
                    float mouseX = ImGui.getMousePosX();
                    float mouseY = ImGui.getMousePosY();
                    if (mouseX >= ImGui.getCursorScreenPos().x && mouseX <= ImGui.getCursorScreenPos().x + ImGui.getWindowSize().x &&
                            mouseY >= ImGui.getCursorScreenPos().y - 40 && mouseY <= ImGui.getCursorScreenPos().y) {
                        lastOpen = System.currentTimeMillis();
                        isCollapsed = !isCollapsed;
                    }
                }
                if (firstFrame) {
                    ImGui.setWindowPos(posX, posY);
                    firstFrame = false;
                }

                isWindowFocused = ImGui.isWindowFocused();
                isWindowHovered = ImGui.isWindowHovered();

                float[] color = JColor.getGuiColor().getFloatColor();
                ImVec2 pos = ImGui.getCursorPos();
                //0.14f, 0.14f, 0.18f, 1.00f

                if (isCollapsed || percentage != 1f) {
                    GuiUtils.drawWindowShadow(ImGui.getColorU32(0f, 0f, 0f, 0.3f), ImGui.getColorU32(0f, 0f, 0f, 0f), 5f);

                    //UI.roundedClipRect(ImGui.getBackgroundDrawList(), () -> ImGui.getBackgroundDrawList().addRectFilled(0, 0, 1000, 1000,
                    //        ImGui.getColorU32(1,1,1,0.5f)), 200, 200, 200, 200, 10);

                    // Header
                    ImGui.pushStyleColor(ImGuiCol.Text, 0.83f, 0.86f, 0.94f, 0.9f);

                    ImGui.setCursorPos(0,0);
                    ImGui.getWindowDrawList().addRectFilled(
                            ImGui.getCursorScreenPos().x,
                            ImGui.getCursorScreenPos().y + ImGui.getScrollY(),
                            ImGui.getCursorScreenPos().x + ImGui.getWindowSize().x,
                            ImGui.getCursorScreenPos().y + ImGui.getScrollY() + 40,
                            ImGui.getColorU32(0.12f, 0.14f, 0.18f, 1.00f),
                            8f,
                            ImDrawFlags.None);
                    ImGui.setCursorPosY(ImGui.getScrollY() + 10);
                    ImGui.setCursorPosX(15);
                    ImGui.pushFont(ImguiLoader.fontAwesome16);
                    ImGui.text(name.substring(0, 1));
                    ImGui.popFont();
                    ImGui.sameLine(0,0);
                    ImGui.pushFont(ImguiLoader.poppins20);
                    ImGui.setCursorPosX(ImGui.getCursorPosX()+10);
                    ImGui.text(name.substring(2));
                    ImGui.popFont();
                    ImGui.popStyleColor(1);
                    ImGui.getStyle().setWindowPadding(0f, 0f);
                    ImGui.getStyle().setFramePadding(0f, 0f);
                    ImGui.getStyle().setCellPadding(0f, 0f);
                    ImGui.popFont();
                    ImGui.setCursorPos(pos.x, pos.y + 3);
                    ImGui.end();
                    return;
                }
                ArrayList<Module> toToggle = new ArrayList<>();
                for (Module module : Template.moduleManager.getModulesByCategory(category)) {
                    if (module.isNotSearched()) {
                        continue;
                    }
                    ImGui.pushID(module.getName());

                    if (!module.isEnabled()) {
                        ImGui.pushStyleColor(ImGuiCol.Button, 0f, 0f, 0f, 0f);
                        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.13f, 0.15f, 0.19f, 1f);
                        ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.14f, 0.16f, 0.20f, 1f);
                    } else {
                        ImGui.pushStyleColor(ImGuiCol.Button,        color[0], color[1], color[2], 1f);
                        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, color[0], color[1], color[2], 0.9f);
                        ImGui.pushStyleColor(ImGuiCol.ButtonActive,  color[0], color[1], color[2], 0.8f);
                    }

                    ImVec2 pos1 = ImGui.getCursorPos();
                    ImGui.getStyle().setFrameRounding(0f);
                    boolean isToggled = ImGui.button("", 200f, 40f);
                    ImGui.getStyle().setFrameRounding(4f);
                    ImGui.popStyleColor(3);
                    ImVec2 pos2 = ImGui.getCursorPos();

                    if (isToggled) {
                        toToggle.add(module);
                    }

                    boolean isModuleHovered = ImGui.isItemHovered();
                    if (isModuleHovered) {
                        ToolTipHolder.setToolTip(module.getDescription().getContent());

                        if (ImGui.isMouseClicked(1)) {
                            module.toggleShowOptions();
                            ImGui.setWindowFocus(name);
                        }
                    }

                    if (module.isEnabled()) {
                        ImGui.pushStyleColor(ImGuiCol.Text, 0.12f, 0.14f, 0.18f, 1f);
                        //ImGui.pushStyleColor(ImGuiCol.Text, 0.76f, 0.82f, 0.92f, 1f);
                    } else {
                        if (ImGui.isItemHovered()) {
                            ImGui.pushStyleColor(ImGuiCol.Text, 0.83f, 0.86f, 0.94f, 0.9f);
                            //ImGui.pushStyleColor(ImGuiCol.Text, 0.76f, 0.82f, 0.92f, 0.7f);
                        } else {
                            ImGui.pushStyleColor(ImGuiCol.Text, 0.83f, 0.86f, 0.94f, 0.6f);
                            //ImGui.pushStyleColor(ImGuiCol.Text, 0.76f, 0.82f, 0.92f, 0.5f);
                        }
                    }

                    ImVec2 textSize = RenderUtils.calcTextSize(module.getFullName());
                    ImGui.setCursorPos(pos1.x + 15, pos1.y + 20 - textSize.y / 2);

                    ImVec2 prePos = ImGui.getCursorPos();

                    RenderUtils.drawTexts(module.getFullName());

                    ImGui.popStyleColor(1);

                    ImGui.setCursorPos(180, prePos.y);
                    if (module.isEnabled()) {
                        ImGui.pushStyleColor(ImGuiCol.Text, 0.12f, 0.14f, 0.18f,1f);
                    } else {
                        ImGui.pushStyleColor(ImGuiCol.Text, 0.83f, 0.86f, 0.94f, 0.6f);
                    }
                    ImGui.text("\uF142");
                    ImGui.popStyleColor(1);

                    if (!KeyUtils.getKeyName(module.keybind.getCode()).equalsIgnoreCase("None")) {
                        String key = KeyUtils.getKeyName(module.keybind.getCode());

                        ImGui.pushFont(ImguiLoader.poppins22);
                        if (module.isEnabled()) {
                            ImGui.pushStyleColor(ImGuiCol.Text,          0.12f, 0.14f, 0.18f, 1.00f);
                            //ImGui.pushStyleColor(ImGuiCol.Button,        0.12f, 0.14f, 0.18f, 0.1f);
                            //ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.12f, 0.14f, 0.18f, 0.1f);
                            //ImGui.pushStyleColor(ImGuiCol.ButtonActive,  0.12f, 0.14f, 0.18f, 0.1f);
                        } else {
                            ImGui.pushStyleColor(ImGuiCol.Text, 0.32f, 0.34f, 0.43f, 1.00f);
                            //ImGui.pushStyleColor(ImGuiCol.Button, 0.70f, 0.74f, 0.86f, 0.05f);
                            //ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.70f, 0.74f, 0.86f, 0.05f);
                            //ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.70f, 0.74f, 0.86f, 0.05f);
                        }
                        ImGui.pushStyleVar(ImGuiStyleVar.ButtonTextAlign, 0.5f, 0.5f);
                        float width = 25f;
                        float height = 25f;
                        if (ImGui.calcTextSize(key).x > width) width = ImGui.calcTextSize(key).x + 10;
                        ImGui.setCursorPos(170f - width , prePos.y - 2);
                        ImGui.getWindowDrawList().addRectFilled(
                                ImGui.getCursorScreenPosX(),
                                ImGui.getCursorScreenPosY(),
                                ImGui.getCursorScreenPosX()+width,
                                ImGui.getCursorScreenPosY()+height,
                                !module.isEnabled() ?
                                        ImGui.getColorU32(0.70f, 0.74f, 0.86f, 0.05f) : ImGui.getColorU32(0.12f, 0.14f, 0.18f, 0.15f),
                                4f
                        );
                        ImGui.setCursorPos(170f - width + width / 2 - ImGui.calcTextSize(key).x / 2, prePos.y - 2 + height / 2 - ImGui.calcTextSize(key).y / 2);
                        ImGui.text(key);
                        ImGui.popFont();
                        ImGui.popStyleVar(1);
                        ImGui.popStyleColor(1);
                    }

                    ImGui.setCursorPos(pos2.x, pos2.y);

                    if (module.showOptions()) {
                        ImGui.getStyle().setItemSpacing(8, 4);
                        ImGui.getStyle().setItemInnerSpacing(4, 4);
                        imGuiWindowFlags = 0;
                        imGuiWindowFlags |= ImGuiWindowFlags.AlwaysAutoResize;
                        ImGui.pushStyleColor(ImGuiCol.ChildBg, 1f, 1f, 1f, 0.01f);
                        //ImGui.pushStyleColor(ImGuiCol.Text, 0.83f, 0.86f, 0.94f, 0.8f);
                        //ImGui.beginChild("Module/"+module.getName()+"/Settings", 200f, module.getSettingsHeight2(), false, imGuiWindowFlags);
                        ImGui.indent(10f);
                        ImGui.setCursorPosY(ImGui.getCursorPosY()+5);
                        ImGui.pushFont(ImguiLoader.poppins18);
                        ImGui.getStyle().setFramePadding(4, 4);
                        ImGui.getStyle().setButtonTextAlign(0.5f, 0.5f);
                        module.renderSettings();
                        ImGui.getStyle().setButtonTextAlign(0f, 0f);
                        ImGui.getStyle().setFramePadding(4, 6);
                        ImGui.popFont();
                        ImGui.unindent(10f);
                        ImGui.setCursorPosY(ImGui.getCursorPosY()+10);
                        //ImGui.endChild();
                        ImGui.popStyleColor(1);
                        ImGui.getStyle().setItemSpacing(0, 1);
                        ImGui.getStyle().setItemInnerSpacing(0, 0);
                    }
                    ImGui.popID();
                }
                ImGui.setCursorPosY(ImGui.getCursorPosY()+7f);
                ImGui.popFont();

                maxY = ImGui.getWindowSizeY();
                GuiUtils.drawWindowShadow(ImGui.getColorU32(0f, 0f, 0f, 0.3f), ImGui.getColorU32(0f, 0f, 0f, 0f), 5f);

                //UI.roundedClipRect(ImGui.getBackgroundDrawList(), () -> ImGui.getBackgroundDrawList().addRectFilled(0, 0, 1000, 1000,
                //        ImGui.getColorU32(1,1,1,0.5f)), 200, 200, 200, 200, 10);

                // Header
                ImGui.pushStyleColor(ImGuiCol.Text, 0.83f, 0.86f, 0.94f, 0.9f);

                ImGui.setCursorPos(0,0);
                ImGui.getWindowDrawList().addRectFilled(
                        ImGui.getCursorScreenPos().x,
                        ImGui.getCursorScreenPos().y + ImGui.getScrollY(),
                        ImGui.getCursorScreenPos().x + ImGui.getWindowSize().x,
                        ImGui.getCursorScreenPos().y + ImGui.getScrollY() + 40,
                        ImGui.getColorU32(0.12f, 0.14f, 0.18f, 1.00f),
                        8f,
                        ImDrawFlags.None);
                ImGui.setCursorPosY(ImGui.getScrollY() + 10);
                ImGui.setCursorPosX(15);
                ImGui.pushFont(ImguiLoader.fontAwesome16);
                ImGui.text(name.substring(0, 1));
                ImGui.popFont();
                ImGui.sameLine(0,0);
                ImGui.pushFont(ImguiLoader.poppins20);
                ImGui.setCursorPosX(ImGui.getCursorPosX()+10);
                ImGui.text(name.substring(2));
                ImGui.popFont();
                ImGui.popStyleColor(1);
                ImGui.end();
                toToggle.forEach(Module::toggle);
            }
            case Moon -> {
                //
                // Moon ClickGUI
                //

                int imGuiWindowFlags = 0;
                imGuiWindowFlags |= ImGuiWindowFlags.AlwaysAutoResize;
                imGuiWindowFlags |= ImGuiWindowFlags.NoDocking;
                imGuiWindowFlags |= ImGuiWindowFlags.NoTitleBar;

                float percentage = 1f;
                float percentage1 = 1f;

                if (isCollapsed) {
                    percentage = 0f;
                    percentage1 = 0f;
                }
                ImGui.getStyle().setButtonTextAlign(0, 0.5f);

                ImGui.getStyle().setWindowPadding(0f, 0f);
                ImGui.getStyle().setFramePadding(0f, 0f);
                ImGui.getStyle().setCellPadding(0f, 0f);
                ImGui.getStyle().setItemSpacing(0, 1);
                ImGui.getStyle().setItemInnerSpacing(0, 0);
                ImGui.getStyle().setWindowBorderSize(1f);
                ImGui.getStyle().setFrameBorderSize(0f);

                float windowY = maxY * percentage;
                if (windowY < 1.0f) windowY = 30f;
                if (percentage1 == 1f && isCollapsed) windowY = 30;
                if (percentage == 1f && !isCollapsed) windowY = 30;
                ImGui.getStyle().setWindowMinSize(200, windowY);

                ImGui.pushStyleColor(ImGuiCol.Border, 0.21f, 0.24f, 0.31f, 1.00f);
                ImGui.pushStyleColor(ImGuiCol.WindowBg, 0.15f, 0.17f, 0.22f, 1f);
                ImGui.getStyle().setWindowRounding(16f);
                ImGui.begin(name, imGuiWindowFlags);
                ImGui.getStyle().setWindowBorderSize(1f);
                if (scrollUntil > ImGui.getScrollMaxY()) {
                    scrollUntil = ImGui.getScrollMaxY();
                } else if (scrollUntil < 0) {
                    scrollUntil = 0;
                }

                scrollY = (float) (scrollY + 0.2 * (scrollUntil - scrollY));
                ImGui.setScrollY(scrollY);

                ImGui.pushFont(ImguiLoader.product20);
                ImGui.getStyle().setFramePadding(4, 6);
                ImGui.getStyle().setCellPadding(4, 4);
                ImGui.getStyle().setWindowPadding(4, 4);

                ImGui.setCursorPos(0, 40);

                if (ImGui.isMouseClicked(1)) {
                    float mouseX = ImGui.getMousePosX();
                    float mouseY = ImGui.getMousePosY();
                    if (mouseX >= ImGui.getCursorScreenPos().x && mouseX <= ImGui.getCursorScreenPos().x + ImGui.getWindowSize().x &&
                            mouseY >= ImGui.getCursorScreenPos().y - 40 && mouseY <= ImGui.getCursorScreenPos().y) {
                        lastOpen = System.currentTimeMillis();
                        isCollapsed = !isCollapsed;
                    }
                }
                if (firstFrame) {
                    ImGui.setWindowPos(posX, posY);
                    firstFrame = false;
                }

                isWindowFocused = ImGui.isWindowFocused();
                isWindowHovered = ImGui.isWindowHovered();

                float[] color = JColor.getGuiColor().getFloatColor();
                ImVec2 pos = ImGui.getCursorPos();
                //0.14f, 0.14f, 0.18f, 1.00f

                ImGui.popStyleColor(2);

                if (isCollapsed || percentage != 1f) {
                    GuiUtils.drawWindowShadow(ImGui.getColorU32(0f, 0f, 0f, 0.2f),
                            ImGui.getColorU32(0f, 0f, 0f, 0f),
                            15f);


                    //UI.roundedClipRect(ImGui.getBackgroundDrawList(), () -> ImGui.getBackgroundDrawList().addRectFilled(0, 0, 1000, 1000,
                    //        ImGui.getColorU32(1,1,1,0.5f)), 200, 200, 200, 200, 10);

                    // Header
                    ImGui.pushStyleColor(ImGuiCol.Text, 0.83f, 0.86f, 0.94f, 0.9f);

                    ImGui.setCursorPos(0,0);
                    ImGui.getWindowDrawList().addRectFilled(
                            ImGui.getCursorScreenPos().x,
                            ImGui.getCursorScreenPos().y + ImGui.getScrollY(),
                            ImGui.getCursorScreenPos().x + ImGui.getWindowSize().x,
                            ImGui.getCursorScreenPos().y + ImGui.getScrollY() + 40,
                            ImGui.getColorU32(0.11f, 0.12f, 0.16f, 0.6f),
                            16f,
                            ImDrawFlags.RoundCornersTop
                    );
                    ImGui.setCursorPosY(ImGui.getScrollY() + 10);
                    ImGui.setCursorPosX(15);
                    ImGui.pushFont(ImguiLoader.fontAwesome16);
                    ImGui.text(name.substring(0, 1));
                    ImGui.popFont();
                    ImGui.sameLine(0,0);
                    ImGui.pushFont(ImguiLoader.product26);
                    ImGui.pushStyleColor(ImGuiCol.Text, 0.83f, 0.86f, 0.94f, 0.9f);
                    ImGui.setCursorPosY(7);
                    ImGui.setCursorPosX(ImGui.getCursorPosX()+5);
                    ImGui.text(name.substring(2));
                    ImGui.popFont();
                    ImGui.popStyleColor(2);
                    ImGui.getStyle().setWindowPadding(0f, 0f);
                    ImGui.getStyle().setFramePadding(0f, 0f);
                    ImGui.getStyle().setCellPadding(0f, 0f);
                    ImGui.popFont();
                    ImGui.setCursorPos(pos.x, pos.y + 3);
                    ImGui.end();
                    return;
                }

                ImGui.pushStyleColor(ImGuiCol.Border, 0.16f, 0.17f, 0.21f, 1.00f);
                ImGui.pushStyleColor(ImGuiCol.WindowBg, 0.09f, 0.10f, 0.14f, 1.00f);

                ArrayList<Module> toToggle = new ArrayList<>();
                for (Module module : Template.moduleManager.getModulesByCategory(category)) {
                    if (module.isNotSearched()) {
                        continue;
                    }
                    ImGui.pushID(module.getName());

                    if (!module.isEnabled()) {
                        ImGui.pushStyleColor(ImGuiCol.Button, 0f, 0f, 0f, 0f);
                        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 1f, 1f, 1f, 0.02f);
                        ImGui.pushStyleColor(ImGuiCol.ButtonActive, 1f, 1f, 1f, 0.04f);
                    } else {
                        //ImGui.pushStyleColor(ImGuiCol.Button,        color[0], color[1], color[2], 1f);
                        //ImGui.pushStyleColor(ImGuiCol.ButtonHovered, color[0], color[1], color[2], 0.9f);
                        //ImGui.pushStyleColor(ImGuiCol.ButtonActive,  color[0], color[1], color[2], 0.8f);
                        ImGui.pushStyleColor(ImGuiCol.Button, 0f, 0f, 0f, 0f);
                        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0f, 0f, 0f, 0.02f);
                        ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0f, 1f, 0f, 0.04f);
                    }

                    ImVec2 pos1 = ImGui.getCursorPos();
                    ImGui.getStyle().setFrameRounding(0f);
                    if(module.isEnabled()) {
                        float posX = ImGui.getWindowPosX() + ImGui.getCursorPosX();
                        float posY = ImGui.getWindowPosY() + ImGui.getCursorPosY() - ImGui.getScrollY();
                        ImGui.getWindowDrawList().addRectFilledMultiColor(
                                posX,
                                posY,
                                posX + 200,
                                posY + 34,
                                UI.getColorOne().getU32(),
                                UI.getColorTwo().getU32(),
                                UI.getColorTwo().getU32(),
                                UI.getColorOne().getU32()
                        );
                    }
                    ImGui.setCursorPos(pos1.x, pos1.y);
                    boolean isToggled = ImGui.button("", 200f, 35f);
                    ImGui.getStyle().setFrameRounding(4f);
                    ImGui.popStyleColor(3);
                    ImVec2 pos2 = ImGui.getCursorPos();

                    if (isToggled) {
                        toToggle.add(module);
                    }

                    boolean isModuleHovered = ImGui.isItemHovered();
                    if (isModuleHovered) {
                        ToolTipHolder.setToolTip(module.getDescription().getContent());

                        if (ImGui.isMouseClicked(1)) {
                            module.toggleShowOptions();
                            ImGui.setWindowFocus(name);
                        }
                    }

                    if (module.isEnabled()) {
                        ImGui.pushStyleColor(ImGuiCol.Text, 0.83f, 0.86f, 0.94f, 1f);
                        //ImGui.pushStyleColor(ImGuiCol.Text, 0.76f, 0.82f, 0.92f, 1f);
                    } else {
                        if (ImGui.isItemHovered()) {
                            ImGui.pushStyleColor(ImGuiCol.Text, 0.83f, 0.86f, 0.94f, 0.9f);
                            //ImGui.pushStyleColor(ImGuiCol.Text, 0.76f, 0.82f, 0.92f, 0.7f);
                        } else {
                            ImGui.pushStyleColor(ImGuiCol.Text, 0.83f, 0.86f, 0.94f, 0.8f);
                            //ImGui.pushStyleColor(ImGuiCol.Text, 0.76f, 0.82f, 0.92f, 0.5f);
                        }
                    }

                    ImVec2 textSize = RenderUtils.calcTextSize(module.getFullName());
                    ImGui.setCursorPos(pos1.x + 15, pos1.y + 17 - textSize.y / 2);

                    ImVec2 prePos = ImGui.getCursorPos();

                    RenderUtils.drawTexts(module.getFullName());

                    ImGui.popStyleColor(1);

                    ImGui.setCursorPos(200, prePos.y);
                    ImGui.pushStyleColor(ImGuiCol.Text, 0.83f, 0.86f, 0.94f, 1f);
                    ImGui.pushFont(ImguiLoader.fontAwesome16);
                    //if (module.showOptions()) {
                    //    ImGui.text("\uF0D8");
                    //} else {
                    //    ImGui.text("\uF0D7");
                    //}
                    ImGui.popFont();
                    ImGui.popStyleColor(1);


                    ImGui.setCursorPos(pos2.x, pos2.y);

                    if (module.showOptions()) {
                        ImGui.getStyle().setItemSpacing(8, 4);
                        ImGui.getStyle().setItemInnerSpacing(4, 4);
                        imGuiWindowFlags = 0;
                        imGuiWindowFlags |= ImGuiWindowFlags.AlwaysAutoResize;
                        ImGui.pushStyleColor(ImGuiCol.ChildBg, 1f, 1f, 1f, 0.01f);
                        //ImGui.pushStyleColor(ImGuiCol.Text, 0.83f, 0.86f, 0.94f, 0.8f);
                        //ImGui.beginChild("Module/"+module.getName()+"/Settings", 200f, module.getSettingsHeight2(), false, imGuiWindowFlags);
                        ImGui.indent(10f);
                        ImGui.setCursorPosY(ImGui.getCursorPosY()+5);
                        ImGui.pushFont(ImguiLoader.outfit18);
                        ImGui.getStyle().setFramePadding(4, 4);
                        ImGui.getStyle().setButtonTextAlign(0.5f, 0.5f);
                        module.renderSettings();
                        ImGui.getStyle().setButtonTextAlign(0f, 0f);
                        ImGui.getStyle().setFramePadding(4, 6);
                        ImGui.popFont();
                        ImGui.unindent(10f);
                        ImGui.setCursorPosY(ImGui.getCursorPosY()+10);
                        //ImGui.endChild();
                        ImGui.popStyleColor(1);
                        ImGui.getStyle().setItemSpacing(0, 1);
                        ImGui.getStyle().setItemInnerSpacing(0, 0);
                    }
                    ImGui.setCursorPosY(ImGui.getCursorPosY()-1);

                    ImGui.popID();
                }
                ImGui.setCursorPosY(ImGui.getCursorPosY()+7f);
                ImGui.popFont();

                maxY = ImGui.getWindowSizeY();
                GuiUtils.drawWindowShadow(ImGui.getColorU32(0f, 0f, 0f, 0.3f), ImGui.getColorU32(0f, 0f, 0f, 0f), 5f);

                //UI.roundedClipRect(ImGui.getBackgroundDrawList(), () -> ImGui.getBackgroundDrawList().addRectFilled(0, 0, 1000, 1000,
                //        ImGui.getColorU32(1,1,1,0.5f)), 200, 200, 200, 200, 10);

                // Header
                ImGui.pushStyleColor(ImGuiCol.Text, 0.83f, 0.86f, 0.94f, 0.9f);

                ImGui.setCursorPos(0,0);
                ImGui.getWindowDrawList().addRectFilled(
                        ImGui.getCursorScreenPos().x,
                        ImGui.getCursorScreenPos().y + ImGui.getScrollY(),
                        ImGui.getCursorScreenPos().x + ImGui.getWindowSize().x,
                        ImGui.getCursorScreenPos().y + ImGui.getScrollY() + 40,
                        ImGui.getColorU32(0.11f, 0.12f, 0.16f, 0.6f),
                        16f,
                        ImDrawFlags.RoundCornersTop);
                ImGui.setCursorPosY(ImGui.getScrollY() + 10);
                ImGui.setCursorPosX(15);
                ImGui.pushFont(ImguiLoader.fontAwesome16);
                ImGui.text(name.substring(0, 1));
                ImGui.popFont();
                ImGui.sameLine(0,0);
                ImGui.pushFont(ImguiLoader.outfit26);
                ImGui.setCursorPosY(7);
                ImGui.setCursorPosX(ImGui.getCursorPosX()+5);
                ImGui.text(name.substring(2));
                ImGui.popFont();
                ImGui.popStyleColor(3);
                ImGui.end();
                toToggle.forEach(Module::toggle);
            }
        }
    }

    @Override
    public Theme getTheme() {
        return AchillesMenu.getInstance().getTheme();
    }
}