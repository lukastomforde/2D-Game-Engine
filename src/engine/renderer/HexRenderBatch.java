package engine.renderer;

import org.joml.Vector4f;
import org.lwjgl.opengl.GL30;

import engine.Window;
import engine.components.HexRenderer;

public class HexRenderBatch {
    // Vertex
    // ==========
    // Pos              Color
    // float, float     float, float, float

    private final float SQRT3_2 = (float) (Math.sqrt(3.0) * 0.5);

    private final int POS_SIZE = 2;
    private final int Color_SIZE = 4;

    private final int POS_OFFSET = 0;
    private final int COLOR_OFFSET = POS_OFFSET + POS_SIZE * Float.BYTES;

    private final int VERTEX_SIZE = 6;
    private final int VERTEX_SIZE_BYTES = VERTEX_SIZE * Float.BYTES;

    private HexRenderer[] hexes;
    private int numHexes;
    private boolean hasRoom;
    private float[] vertices;

    private int vaoID, vboID;
    private int maxBatchSize;
    private Shader shader;

    public HexRenderBatch(int maxBatchSize){
        shader = new Shader("assets/shaders/default.glsl");
        shader.compile();
        this.hexes = new HexRenderer[maxBatchSize];
        this.maxBatchSize = maxBatchSize;

        // 7 vertices per hex    
        vertices = new float[maxBatchSize * 7 * VERTEX_SIZE]; // max number of hexes * number of vertices per hex * number of floats per vertex
    
        numHexes = 0;
        hasRoom = true;
    }

    public void start(){
        // Generate and bind a Vertex Array Object
        vaoID = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoID);

        // Allocate space for the vertices
        vboID = GL30.glGenBuffers();
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vboID);
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, vertices.length * Float.BYTES, GL30.GL_DYNAMIC_DRAW);  // vertex synamic, becaus vertices can change

        // Create and upload the indices buffer
        int eboID = GL30.glGenBuffers();
        int[] indices = generateIndices();
        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, eboID);
        GL30.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, indices, GL30.GL_STATIC_DRAW);  // indices static, because the indices will never change (when objects are moving arround, the position changes -> vertices are changing, but not there structure -> indices are not changing)
    
        // Enable the buffer attribute pointers
        GL30.glVertexAttribPointer(0, POS_SIZE, GL30.GL_FLOAT, false, VERTEX_SIZE_BYTES, POS_OFFSET);
        GL30.glEnableVertexAttribArray(0);

        GL30.glVertexAttribPointer(1, Color_SIZE, GL30.GL_FLOAT, false, VERTEX_SIZE_BYTES, COLOR_OFFSET);
        GL30.glEnableVertexAttribArray(1);
    }

    public void addHex(HexRenderer hex){
        // Get index and add renderer Object
        int index = this.numHexes;
        this.hexes[index] = hex;
        this.numHexes ++;

        // Add properties to local vertices array
        loadVertexProperties(index);

        if(numHexes >= this.maxBatchSize){
            this.hasRoom = false;
        }
    }

    public void render(){
        // For now, we will rebuffer all data every frame
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vboID);
        GL30.glBufferSubData(GL30.GL_ARRAY_BUFFER, 0, vertices);

        // Use shader
        shader.use();
        shader.uploadMat4f("uProj", Window.getCurrentScene().getCamera().getProjectionMatrix());
        shader.uploadMat4f("uView", Window.getCurrentScene().getCamera().getViewMatrix());

        GL30.glBindVertexArray(vaoID);
        GL30.glEnableVertexAttribArray(0);
        GL30.glEnableVertexAttribArray(1);

        GL30.glDrawElements(GL30.GL_TRIANGLES, this.numHexes * 18, GL30.GL_UNSIGNED_INT, 0);

        GL30.glDisableVertexAttribArray(0);
        GL30.glDisableVertexAttribArray(1);
        GL30.glBindVertexArray(0);

        shader.detach();
    }

    private void loadVertexProperties(int index){
        HexRenderer hex = this.hexes[index];

        // Find offset within array (7 vertices per hex)
        int offset = index * 7 * VERTEX_SIZE;
        
        Vector4f color = hex.getColor();

        // Add vertices with the appropriate properties
        float xAdd = 0.0f;
        float yAdd = 0.0f;

        for (int i=0; i<7; ++i){

            // change this
            switch(i){
                case 1:
                    xAdd = +0.5f;
                    yAdd = SQRT3_2;
                    break;
                
                case 2:
                    xAdd = -0.5f;
                    yAdd = SQRT3_2;
                    break;

                case 3:
                    xAdd = -1.0f;
                    yAdd = 0.0f;               
                    break;

                case 4:
                    xAdd = -0.5f;
                    yAdd = -SQRT3_2;
                    break;

                case 5:
                    xAdd = +0.5f;
                    yAdd = -SQRT3_2;
                    break;

                case 6:
                    xAdd = 1.0f;
                    yAdd = 0.0f;
                    break;
            }

            // Load position
            vertices[offset+0] = hex.gameObject.transform.position.x + (xAdd * hex.gameObject.transform.scale.x/2.0f);
            vertices[offset+1] = hex.gameObject.transform.position.y + (yAdd * hex.gameObject.transform.scale.x/2.0f);

            // Load Color
            vertices[offset+2] = color.x;
            vertices[offset+3] = color.y;
            vertices[offset+4] = color.z;
            vertices[offset+5] = color.w;

            offset += VERTEX_SIZE;
        }
    }

    private int[] generateIndices(){
        // 18 indicces per hex (3 per triangle)

        int[] elements = new int[18 * maxBatchSize];
        for (int i=0; i<maxBatchSize; ++i){
            loadElementIndices(elements, i);
        }

        return elements;
    }

    private void loadElementIndices(int[] elements, int index){
        // calculates the element indices for one quad


        int offsetArrayIndex = 18 * index;
        int offset = 7 * index;

        // Triangle 1
        elements[offsetArrayIndex + 0] = offset + 0;
        elements[offsetArrayIndex + 1] = offset + 1;
        elements[offsetArrayIndex + 2] = offset + 2;

        // Triangle 2
        elements[offsetArrayIndex + 3] = offset + 0;
        elements[offsetArrayIndex + 4] = offset + 2;
        elements[offsetArrayIndex + 5] = offset + 3;

        // Triangle 1
        elements[offsetArrayIndex + 6] = offset + 0;
        elements[offsetArrayIndex + 7] = offset + 3;
        elements[offsetArrayIndex + 8] = offset + 4;

        // Triangle 2
        elements[offsetArrayIndex + 9]  = offset + 0;
        elements[offsetArrayIndex + 10] = offset + 4;
        elements[offsetArrayIndex + 11] = offset + 5;

        // Triangle 1
        elements[offsetArrayIndex + 12] = offset + 0;
        elements[offsetArrayIndex + 13] = offset + 5;
        elements[offsetArrayIndex + 14] = offset + 6;

        // Triangle 2
        elements[offsetArrayIndex + 15] = offset + 0;
        elements[offsetArrayIndex + 16] = offset + 6;
        elements[offsetArrayIndex + 17] = offset + 1;
    }

    public boolean hasRoom(){
        return hasRoom;
    }

}
