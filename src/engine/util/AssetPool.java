package engine.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import engine.components.Spritesheet;
import engine.renderer.Shader;
import engine.renderer.Texture;

public class AssetPool {
    
    private static String shaderPath = "assets/shaders/";
    private static String texturePath = "assets/textures/";
    private static String spritesheetPath = "assets/textures/";

    private static Map<String, Shader> shaders = new HashMap<>();
    private static Map<String, Texture> textures = new HashMap<>();
    private static Map<String, Spritesheet> spritesheets = new HashMap<>();

    public static void loadShader(String shaderName){
        File file = new File(shaderPath + shaderName);
        Shader shader = new Shader(shaderPath + shaderName);
        shader.compile();
        shaders.put(file.getAbsolutePath(), shader);
    }

    public static Shader getShader(String shaderName){
        File file = new File(shaderPath + shaderName);
        Shader shader = shaders.get(file.getAbsolutePath());
        assert shader != null : "Shader '" + shaderPath + "" + shaderName + "' not loaded.";
        return shader;
    }

    public static void loadTexture(String textureName){
        File file = new File(texturePath + textureName);
        Texture texture = new Texture(texturePath + textureName);
        textures.put(file.getAbsolutePath(), texture);
    }

    public static Texture getTexture(String textureName){
        File file = new File(texturePath + textureName);
        Texture texture = textures.get(file.getAbsolutePath());
        assert texture != null : "Texture '" + texturePath + "" + textureName + "' not loaded.";
        return texture;
    }

    public static void addSpritesheet(String spritesheetName, Spritesheet spritesheet){
        File file = new File(spritesheetPath + spritesheetName);
        if(!spritesheets.containsKey(file.getAbsolutePath())){
            spritesheets.put(file.getAbsolutePath(), spritesheet);
        }
    }

    public static Spritesheet getSpritesheet(String spritesheetName){
        File file = new File(spritesheetPath + spritesheetName);
        Spritesheet spritesheet = spritesheets.get(file.getAbsolutePath());
        assert spritesheet != null : "Spritesheet '" + spritesheetPath + "" + spritesheetName + "' not added to spritesheet pool.";
        return spritesheet;
    }
}
