package template.rip.api.font;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import template.rip.api.util.ColorUtil;
import template.rip.api.util.RenderUtils;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FontRenderer {

   private final Color shadowColor = new Color(0, 0, 0, 100);
   private final CharacterData[] regularData;
   private final String COLOR_INVOKER = "§";
   private final boolean antiAlias;
   private final Font font;
   private int prevGuiScale = RenderUtils.getGuiScale();
   private float scale = RenderUtils.getGuiScale() / 2f;
   /*for optimization*/
   private float fontSize = 0.5f / scale;
   private float roundScale = Math.round(scale);
   private float widthDivider = scale * 2;

   public FontRenderer(Font font) {
      this(font, 256);
   }

   public FontRenderer(Font font, int characterCount) {
      this(font, characterCount, true);
   }

   public FontRenderer(Font font, boolean antiAlias) {
      this(font, 256, antiAlias);
   }

   public FontRenderer(Font font, int characterCount, boolean antiAlias) {
      this.font = font;
      this.antiAlias = antiAlias;

      regularData = setup(new CharacterData[characterCount]);
   }

   private CharacterData[] setup(CharacterData[] characterData) {
      Font derivedFont = font.deriveFont(Font.PLAIN, font.getSize() * scale);
      BufferedImage utilityImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
      Graphics2D utilityGraphics = (Graphics2D) utilityImage.getGraphics();
      utilityGraphics.setFont(derivedFont);
      FontMetrics fontMetrics = utilityGraphics.getFontMetrics();

      for (int index = 0; index < characterData.length; ++index) {
         char character = (char) index;
         Rectangle2D characterBounds = fontMetrics.getStringBounds(character + "", utilityGraphics);
         int width = (int) Math.ceil(characterBounds.getWidth() + 8);
         int height = (int) Math.ceil(characterBounds.getHeight());
         BufferedImage characterImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
         Graphics2D graphics = (Graphics2D) characterImage.getGraphics();
         graphics.setFont(derivedFont);
         graphics.setColor(Color.WHITE);

         if (antiAlias) {
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
         }

         graphics.drawString(character + "", 4, fontMetrics.getAscent());
         graphics.dispose();

         NativeImage ni = new NativeImage(width, height, false);
         int[] px = characterImage.getRGB(0, 0, width, height, null, 0, width);
         for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
               ni.setColor(x, y, px[y * width + x]);
            }
         }

         NativeImageBackedTexture dynTex = new NativeImageBackedTexture(ni);
         dynTex.setFilter(false, false);
         MinecraftClient.getInstance().getTextureManager().registerTexture(Identifier.of("renderer", "temp/" + IntStream.range(0, 32)
                 .mapToObj(operand -> String.valueOf((char) new Random().nextInt('a', 'z' + 1)))
                 .collect(Collectors.joining())), dynTex);

         dynTex.upload();
         characterData[index] = new CharacterData(character, (float) width, (float) height, dynTex.getGlId());
      }

      utilityGraphics.dispose();
      return characterData;
   }

   private void checkScale() {
       int guiScale = RenderUtils.getGuiScale();
       if (guiScale != prevGuiScale) {
          prevGuiScale = guiScale;
          scale = guiScale / 2f;
          fontSize = 0.5f / scale;
          roundScale = Math.round(scale);
          widthDivider = scale * 2;
          setup(regularData);
       }
   }

   public void drawCenteredString(MatrixStack poseStack, String text, float x, float y, int color) {
      drawString(poseStack, text, x - getWidth(text) / 2, y, color);
   }

   public void drawCenteredStringWithShadow(MatrixStack poseStack, String text, float x, float y, int color) {
      float width = getWidth(text) / 2;
      poseStack.translate(0.5, 0.5, 0);
      drawString(poseStack, text, x - width, y, shadowColor.getRGB());
      poseStack.translate(-0.5, -0.5, 0);
      drawString(poseStack, text, x - width, y, color);
   }

   public void drawStringWithShadow(MatrixStack poseStack, String text, float x, float y, int color) {
      poseStack.translate(0.5, 0.5, 0);
      drawString(poseStack, text, x, y, shadowColor.getRGB());
      poseStack.translate(-0.5, -0.5, 0);
      drawString(poseStack, text, x, y, color);
   }

   public void drawGradientString(MatrixStack stack, String text, float x, float y, int start, int end, boolean shadow) {
      float charX = x;

      for (int i = 0; i < text.length(); i++) {
         float percent = i / (float) (text.length() - 1);
         int color = ColorUtil.interpolateColor(start, end, percent);
         String character = String.valueOf(text.charAt(i));

         if (shadow) drawStringWithShadow(stack, character, charX, y, color);
         else drawString(stack, character, charX, y, color);
         charX += getWidth(character);
      }
   }

   public void drawString(MatrixStack poseStack, String text, float x, float y, int color) {
      if (text.isEmpty()) {
         return;
      }

      float a = (float)(color >> 24 & 0xFF) / 255.0f;
      float r = (float)(color >> 16 & 0xFF) / 255.0f;
      float g = (float)(color >> 8 & 0xFF) / 255.0f;
      float b = (float)(color & 0xFF) / 255.0f;

      checkScale();
      RenderSystem.setShaderColor(1, 1, 1, 1);
      poseStack.push();
      poseStack.translate(x - 3 + roundScale, y - 3 + roundScale, 0);
      poseStack.scale(fontSize, fontSize, 1);
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
      CharacterData[] characterData = regularData;
      int length = text.length();
      RenderSystem.setShaderColor(r, g, b, a);
      RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX);

      float xOffset = 0;
      for (int i = 0; i < length; ++i) {
         char character = text.charAt(i);
         String pString = String.valueOf((i > 0) ? text.charAt(i - 1) : '.');

         if (!pString.equals(COLOR_INVOKER) && character <= 'ÿ') {
            drawChar(poseStack, character, characterData, xOffset, 0);
            CharacterData charData = characterData[character];

            xOffset += charData.width - 8.0f;
         }
      }

      RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
      poseStack.pop();
      RenderSystem.disableBlend();

      RenderSystem.bindTexture(0);
   }

   private void drawChar(MatrixStack poseStack, char character, CharacterData[] characterData, float x, float y) {
      CharacterData charData = characterData[character];
      charData.bind();

      Matrix4f matrix = poseStack.peek().getPositionMatrix();
      BufferBuilder bufferbuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_TEXTURE);
      bufferbuilder.vertex(matrix, x, y + charData.height, 0).texture(0, 1);
      bufferbuilder.vertex(matrix, x + charData.width, y + charData.height, 0).texture(1, 1);
      bufferbuilder.vertex(matrix, x + charData.width, y, 0).texture(1, 0);
      bufferbuilder.vertex(matrix, x, y, 0).texture(0, 0);
      BufferRenderer.drawWithGlobalProgram(bufferbuilder.end());
   }

   public float getWidth(String text) {
      float width = 0;

      for (int length = text.length(), i = 0; i < length; ++i) {
         char character = text.charAt(i);
         char previous = (i > 0) ? text.charAt(i - 1) : '.';
         String pString = String.valueOf(previous);

         if (!pString.equals(COLOR_INVOKER) && character <= 'ÿ') {
            CharacterData charData = regularData[character];
            width += (charData.width - 8) / widthDivider;
         }
      }

      return width;
   }

   public float getHeight(String text) {
      float height = 0;
      for (int length = text.length(), i = 0; i < length; ++i) {
         char character = text.charAt(i);
         char previous = (i > 0) ? text.charAt(i - 1) : '.';
         String pString = String.valueOf(previous);

         if (!pString.equals(COLOR_INVOKER) && character <= 'ÿ') {
            CharacterData charData = regularData[character];
            height = Math.max(height, charData.height);
         }
      }

      return height / 2 - 2;
   }

   public float getMonoHeight() {
      return getHeight("I");
   }

   static class CharacterData {

      public char character;
      public float width;
      public float height;
      private final int textureId;

      public CharacterData(char character, float width, float height, int textureId) {
         this.character = character;
         this.width = width;
         this.height = height;
         this.textureId = textureId;
      }

      public void bind() {
         RenderSystem.setShaderTexture(0, textureId);
      }
   }
}