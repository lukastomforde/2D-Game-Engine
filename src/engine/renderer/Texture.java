package engine.renderer;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.stb.STBImage;

public class Texture {
    
    private String filepath;
    private int texID;
    private int width, height;

    public Texture(String filepath){
        this.filepath = filepath;

        // Generate texture on GPU
        texID = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texID);

        // Set texture parameters
        // Repeat image in bth directions if the vertex shape exeeds the texture area
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);  // x direction (left/right)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);  // y direction (up/down)

        // When stretching the image -> pixelate (change to blur for realistic games)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);

        // When shrinking an image -> pixelate
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        IntBuffer width = BufferUtils.createIntBuffer(1);
        IntBuffer height = BufferUtils.createIntBuffer(1);
        IntBuffer channels = BufferUtils.createIntBuffer(1);

        STBImage.stbi_set_flip_vertically_on_load(true);
        ByteBuffer image = STBImage.stbi_load(filepath, width, height, channels, 0);

        if (image != null){
            this.width = width.get(0);
            this.height = height.get(0);
            
            if (channels.get(0) == 3){
                GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, width.get(0), height.get(0), 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, image);
            } else if (channels.get(0) == 4){
                GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width.get(0), height.get(0), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, image);
            } else {
                assert false : "Error: (Texture) Unknown number of color channels '" + channels.get(0) + "'";
            }

        } else {
            assert false : "Error: (Texture) Could not load image '" + filepath + "'";
        }

        STBImage.stbi_image_free(image);
    }

    public void bind(){
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texID);
    }

    public void unbind(){
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    public int getWidth(){
        return width;
    }

    public int getHeight(){
        return height;
    }
}
