package template.rip.api.font;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

public enum Fonts {

    TEST("Outfit-Regular", "ttf");

    Fonts(String name, String extension) {
        this.name = name;
        this.extension = extension;
    }

    private final String name;
    private final String extension;
    private final HashMap<Integer, FontRenderer> sizes = new HashMap<>();

    public FontRenderer get() {
        return get(0);
    }

    public FontRenderer get(int size) {
        if (!sizes.containsKey(size)) {
            Font font = getResource(String.format("assets/fonts/%s.%s", name, extension), size);

            if (font != null) {
                sizes.put(size, new FontRenderer(font));
            } else {
                System.err.println("Font " + name + " not found");
            }
        }

        return sizes.get(size);
    }

    private static Font getResource(final String resource, final int size) {
        try {
            return Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(Fonts.class.getClassLoader().getResourceAsStream(resource))).deriveFont((float) size);
        } catch (final FontFormatException | IOException ignored) {
            return null;
        }
    }
}