package engine;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.joml.Vector2f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;

import engine.renderer.Shader;
import engine.renderer.Texture;
import engine.util.Time;

public class LevelEditorScene extends Scene {

    private float[] vertexArray = {
        // position                  // color                  // UV Coordinates  
        100.0f,    0.0f, 0.0f,       0.0f, 0.0f, 0.6f, 1.0f,   1, 1,                // Bottom right  (index 0)
          0.0f,  100.0f, 0.0f,       1.0f, 0.0f, 0.6f, 1.0f,   0, 0,                // Top left      (index 1)
        100.0f,  100.0f, 0.0f,       0.6f, 0.0f, 0.6f, 1.0f,   1, 0,                // Top right     (index 2)
          0.0f,    0.0f, 0.0f,       1.0f, 1.0f, 1.0f, 1.0f,   0, 1                 // Bottom left   (index 3)

    };

    // IMPORTANT: Must be in counter-clockwise order
    private int[] elementArray = {

        2, 1, 0,   // Top right triangle
        0, 1, 3    // Bottom left triangle

    };

    private int vaoID, vboID, eboID;

    private Shader defaultShader;
    private Texture testTexture;

    public LevelEditorScene(){
        
    }

    @Override
    public void init(){
        this.camera = new Camera(new Vector2f());
        defaultShader = new Shader("assets/shaders/default.glsl");
        defaultShader.compile();

        testTexture = new Texture("assets/images/test.jpg");

        // ========================
        // Generate VAO, VBO, 
        // ========================
        
        vaoID = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoID);

        // Create a float buffer of vertices
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertexArray.length);
        vertexBuffer.put(vertexArray).flip();

        // create vbo and upload the vertex buffer
        vboID = GL30.glGenBuffers();
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vboID);
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, vertexBuffer, GL30.GL_STATIC_DRAW);

        // create the indices and upload
        IntBuffer elementBuffer = BufferUtils.createIntBuffer(elementArray.length);
        elementBuffer.put(elementArray).flip();

        eboID = GL30.glGenBuffers();
        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, eboID);
        GL30.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, elementBuffer, GL30.GL_STATIC_DRAW);


        // Specify the structure of the vertex array -> the position of attributes within one vertex
        // => add the vertex attribute pointers

        int positionsSize = 3;
        int colorSize = 4;
        int uvSize = 2;
        int vertexSizeBytes = (positionsSize + colorSize + uvSize) * Float.BYTES;

        // GL30.glVertexAttribPointer(#attribute index, attribute Size, attribute type, attribute default, complete Size of the vertex in bytes, attribute offset on bytes);

        // Enable Position Attribute
        GL30.glVertexAttribPointer(0, positionsSize, GL30.GL_FLOAT, false, vertexSizeBytes, 0);
        GL30.glEnableVertexAttribArray(0);

        // Enable Position Attribute
        GL30.glVertexAttribPointer(1, colorSize, GL30.GL_FLOAT, false, vertexSizeBytes, positionsSize * Float.BYTES);
        GL30.glEnableVertexAttribArray(1);

        // Enable Texture Coordinates Attribute
        GL30.glVertexAttribPointer(2, uvSize, GL30.GL_FLOAT, false, vertexSizeBytes, (positionsSize + colorSize) * Float.BYTES);
        GL30.glEnableVertexAttribArray(2);
    }

    @Override
    public void update(float dt) {

 //       camera.position.x -= dt * 50.0f;
 //       camera.position.y -= dt * 10.0f;

        // use shader
        defaultShader.use();

        // Upload texture to shader
        defaultShader.uploadTexture("TEX_SAMPLER", 0);   // say Opengl, we will upload a texture to splot 0
        GL30.glActiveTexture(GL30.GL_TEXTURE0);                       // activate slot 0
        testTexture.bind();                                            // so now the texture will be loaded into the activated slot 0

        defaultShader.uploadMat4f("uProj", camera.getProjectionMatrix());
        defaultShader.uploadMat4f("uView", camera.getViewMatrix());
        defaultShader.uploadFloat("uTime", Time.getTime());

        // Bind the vao that we're using
        GL30.glBindVertexArray(vaoID);

        // Enable the vertex attribute pointers
        GL30.glEnableVertexAttribArray(0);
        GL30.glEnableVertexAttribArray(1);

        GL30.glDrawElements(GL30.GL_TRIANGLES, elementArray.length, GL30.GL_UNSIGNED_INT, 0);

        // Unbind everything
        GL30.glDisableVertexAttribArray(0);
        GL30.glDisableVertexAttribArray(1);
        GL30.glBindVertexArray(0);
        

        defaultShader.detach();

    }
}
