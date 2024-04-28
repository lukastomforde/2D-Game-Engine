package engine;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.joml.Vector2f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;

import engine.renderer.Shader;

public class LevelEditorScene extends Scene {

    private float[] vertexArray = {
        // position              // color
        100.0f,    0.0f, 0.0f,       0.0f, 0.0f, 0.6f, 1.0f,   // Bottom right  (index 0)
          0.0f,  100.0f, 0.0f,       1.0f, 0.0f, 0.6f, 1.0f,   // Top left      (index 1)
        100.0f,  100.0f, 0.0f,       0.6f, 0.0f, 0.6f, 1.0f,   // Top right     (index 2)
          0.0f,    0.0f, 0.0f,       1.0f, 1.0f, 1.0f, 1.0f,   // Bottom left   (index 3)

    };

    // IMPORTANT: Must be in counter-clockwise order
    private int[] elementArray = {

        2, 1, 0,   // Top right triangle
        0, 1, 3    // Bottom left triangle

    };

    private int vaoID, vboID, eboID;

    private Shader defaultShader;

    public LevelEditorScene(){
        
    }

    @Override
    public void init(){
        this.camera = new Camera(new Vector2f());
        defaultShader = new Shader("assets/shaders/default.glsl");
        defaultShader.compile();



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
        int floatSizeBytes = 4;   // Size of one float in Bytes
        int vertexSizeBytes = (positionsSize + colorSize) * floatSizeBytes;

        // GL30.glVertexAttribPointer(#attribute index, attribute Size, attribute type, attribute default, complete Size of the vertex in bytes, attribute offset on bytes);

        // Enable Position Attribute
        GL30.glVertexAttribPointer(0, positionsSize, GL30.GL_FLOAT, false, vertexSizeBytes, 0);
        GL30.glEnableVertexAttribArray(0);

        // Enable Position Attribute
        GL30.glVertexAttribPointer(1, colorSize, GL30.GL_FLOAT, false, vertexSizeBytes, positionsSize*floatSizeBytes);
        GL30.glEnableVertexAttribArray(1);
    }

    @Override
    public void update(float dt) {

        camera.position.x -= dt * 50.0f;
        camera.position.y -= dt * 10.0f;

        // use shader
        defaultShader.use();
        defaultShader.uploadMat4f("uProj", camera.getProjectionMatrix());
        defaultShader.uploadMat4f("uView", camera.getViewMatrix());

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
