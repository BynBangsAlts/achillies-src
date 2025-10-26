package template.rip.api.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.*;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.JsonHelper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL20;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import static net.minecraft.client.gl.ShaderLoader.SHADERS_PATH;

public class ShaderUtils {

    public static final ShaderProgram empty = create("", VertexFormats.POSITION_TEXTURE);

    public static int compile(CompiledShader.Type type, String name, String extension) {
        CompiledShader compiledShader = getCompiledShader(name, type);
        if (compiledShader != null) {
            return compiledShader.getHandle();
        }
        RenderSystem.assertOnRenderThread();
        int i = GlStateManager.glCreateShader(type.getGlType());
        GlStateManager.glShaderSource(i, getSource(String.format("assets/shaders/%s.%s", name, extension)));
        GlStateManager.glCompileShader(i);
        if (GlStateManager.glGetShaderi(i, 35713) == 0) {
            String string = StringUtils.trim(GlStateManager.glGetShaderInfoLog(i, 32768));
            System.err.println("Couldn't compile " + type.getName() + " shader (" + name + ") : " + string);
        } else {
            return i;
        }
        return 0;
    }

    public static ShaderProgram create(String path, VertexFormat format) {
        JsonObject jsonObject = JsonHelper.deserialize(getSource(String.format("assets/shaders/%s.json", path)));
        String vertexShaderPath = JsonHelper.getString(jsonObject, "vertex");
        String fragmentShaderPath = JsonHelper.getString(jsonObject, "fragment");
        int i = GlStateManager.glCreateProgram();
        if (i <= 0) {
            System.err.printf("Could not create shader program (returned program ID %d%n", i);
        } else {
            format.bindAttributes(i);
            GlStateManager.glAttachShader(i, compile(CompiledShader.Type.VERTEX, vertexShaderPath, "vsh"));
            GlStateManager.glAttachShader(i, compile(CompiledShader.Type.FRAGMENT, fragmentShaderPath, "fsh"));
            GlStateManager.glLinkProgram(i);
            if (GlStateManager.glGetProgrami(i, GL20.GL_LINK_STATUS) == 0) {
                String string = GlStateManager.glGetProgramInfoLog(i, 32768);
                System.err.printf("Error encountered when linking program containing VS %s and FS %s. Log output: %s%n", vertexShaderPath, fragmentShaderPath, string);
            } else {
                ShaderProgram shaderProgram = new ShaderProgram(i);
                ShaderProgramDefinition shaderProgramDefinition = ShaderProgramDefinition.CODEC.parse(JsonOps.INSTANCE, jsonObject).getOrThrow(JsonSyntaxException::new);
                shaderProgram.set(shaderProgramDefinition.uniforms(), shaderProgramDefinition.samplers());
                return shaderProgram;
            }
        }

        return null;
    }

    public static String getSource(String path) {
        ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();
        Map<Identifier, Resource> map = resourceManager.findResources(SHADERS_PATH, id -> ShaderLoader.isDefinition(id) || ShaderLoader.isShaderSource(id));
        GlImportProcessor glImportProcessor = new GlImportProcessor(){
            private final Set<Identifier> processed = new ObjectArraySet<>();

            @Override
            public @NotNull String loadImport(boolean inline, String name) {
                Identifier identifier;
                try {
                    identifier = inline ? null : Identifier.of(name).withPrefixedPath(ShaderLoader.INCLUDE_PATH);
                } catch (InvalidIdentifierException invalidIdentifierException) {
                    System.err.printf("Malformed GLSL import %s: %s%n", name, invalidIdentifierException.getMessage());
                    return "#error " + invalidIdentifierException.getMessage();
                }
                if (processed.contains(identifier)) {
                    return "";
                } else {
                    processed.add(identifier);
                }
                String string;
                try {
                    BufferedReader reader = map.get(identifier).getReader();
                    string = IOUtils.toString(reader);
                    reader.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return string;
            }
        };
        try (InputStream stream = ShaderUtils.class.getClassLoader().getResourceAsStream(path)) {
            return String.join("", glImportProcessor.readSource(new String(stream.readAllBytes())));
        } catch (Exception e) {
            e.printStackTrace();
            return "null";
        }
    }

    public static CompiledShader getCompiledShader(String path, CompiledShader.Type type) {
        Identifier identifier = Identifier.ofVanilla(String.format("core/%s", path));
        ShaderLoader shaderLoader = MinecraftClient.getInstance().getShaderLoader();
        for (Map.Entry<ShaderLoader.ShaderKey, CompiledShader> entry : shaderLoader.cache.compiledShaders.entrySet()) {
            if (entry.getKey().id().getPath().equals(identifier.getPath()) && entry.getKey().type() == type) {
                return entry.getValue();
            }
        }
        return null;
    }
}
