package engine.renderer;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL30;

import engine.Window;
import engine.components.SpriteRenderer;
import engine.util.AssetPool;

public class SpriteRenderBatch implements Comparable<SpriteRenderBatch> {
    // Vertex
    // ==========
    // Pos              Color                  tex coords      tex id
    // float, float     float, float, float    float, float    float

    private final int POS_SIZE = 2;
    private final int Color_SIZE = 4;
    private final int TEX_COORDS_SIZE = 2;
    private final int TEX_ID_SIZE = 1;

    private final int POS_OFFSET = 0;
    private final int COLOR_OFFSET = POS_OFFSET + POS_SIZE * Float.BYTES;
    private final int TEX_COORDS_OFFSET = COLOR_OFFSET + Color_SIZE * Float.BYTES;
    private final int TEX_ID_OFFSET = TEX_COORDS_OFFSET + TEX_COORDS_SIZE * Float.BYTES;

    private final int VERTEX_SIZE = 9;
    private final int VERTEX_SIZE_BYTES = VERTEX_SIZE * Float.BYTES;

    private SpriteRenderer[] sprites;
    private int numSprites;
    private boolean hasRoom;
    private float[] vertices;
    private int[] texSlots = {0, 1, 2, 3, 4, 5, 6, 7};

    private int vaoID, vboID;
    private int maxBatchSize;
    private Shader shader;
    private List<Texture> textures;
    private int zIndex;

    public SpriteRenderBatch(int maxBatchSize, int zIndex){
        shader = AssetPool.getShader("default.glsl");
        this.sprites = new SpriteRenderer[maxBatchSize];
        this.maxBatchSize = maxBatchSize;
        this.zIndex = zIndex;

        // 4 vertices quads    
        vertices = new float[maxBatchSize * 4 * VERTEX_SIZE]; // max number of squares * number of vertices per square * number of floats per vertex
    
        numSprites = 0;
        hasRoom = true;
        textures = new ArrayList<>();
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

        GL30.glVertexAttribPointer(2, TEX_COORDS_SIZE, GL30.GL_FLOAT, false, VERTEX_SIZE_BYTES, TEX_COORDS_OFFSET);
        GL30.glEnableVertexAttribArray(2);

        GL30.glVertexAttribPointer(3, TEX_ID_SIZE, GL30.GL_FLOAT, false, VERTEX_SIZE_BYTES, TEX_ID_OFFSET);
        GL30.glEnableVertexAttribArray(3);
    }

    public void addSprite(SpriteRenderer spr){
        // Get index and add renderer Object
        int index = this.numSprites;
        this.sprites[index] = spr;
        this.numSprites ++;

        if (spr.getTexture() != null){
            if (!textures.contains(spr.getTexture())){
                textures.add(spr.getTexture());
            }
        }

        // Add properties to local vertices array
        loadVertexProperties(index);

        if(numSprites >= this.maxBatchSize){
            this.hasRoom = false;
        }
    }

    public void render(){

        boolean rebufferData = false;

        for (int i=0; i<numSprites; ++i){
            SpriteRenderer spr = sprites[i];
            if(spr.isDirty()){
                loadVertexProperties(i);
                spr.setClean();
                rebufferData = true;
            }
        }

        if (rebufferData){
            // rebuffer data only if it changed
            // for now: rebuffer the whole vertex array, if one or more sprite chaged
            GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vboID);
            GL30.glBufferSubData(GL30.GL_ARRAY_BUFFER, 0, vertices);
        }

        // Use shader
        shader.use();
        shader.uploadMat4f("uProj", Window.getCurrentScene().getCamera().getProjectionMatrix());
        shader.uploadMat4f("uView", Window.getCurrentScene().getCamera().getViewMatrix());

        for (int i=0; i<textures.size(); ++i){
            GL30.glActiveTexture(GL30.GL_TEXTURE0 + i + 1); // reserve texture id 0 for no texture
            textures.get(i).bind();
        }

        shader.uploadTextureArray("uTextures", texSlots);

        GL30.glBindVertexArray(vaoID);
        GL30.glEnableVertexAttribArray(0);
        GL30.glEnableVertexAttribArray(1);

        GL30.glDrawElements(GL30.GL_TRIANGLES, this.numSprites * 6, GL30.GL_UNSIGNED_INT, 0);

        GL30.glDisableVertexAttribArray(0);
        GL30.glDisableVertexAttribArray(1);
        GL30.glBindVertexArray(0);

        for (int i=0; i<textures.size(); ++i){
            textures.get(i).unbind();
        }

        shader.detach();
    }

    private void loadVertexProperties(int index){
        SpriteRenderer sprite = this.sprites[index];

        // find offset within array (4 vertices per sprite)
        int offset = index * 4 * VERTEX_SIZE;
        
        // get the sprites color
        Vector4f color = sprite.getColor();

        // get the sprites texture coordinates
        Vector2f[] texCoords = sprite.getTexCoords();

        // find the sprites texture id
        int texId = 0;
        if(sprite.getTexture() != null){
            for (int i=0; i<textures.size(); ++i){
                if(textures.get(i) == sprite.getTexture()){
                    texId = i + 1;  // reserve texId 0 for no texture
                    break;
                }
            }
        }

        // Add vertices with the appropriate properties
        float xAdd = 1.0f;
        float yAdd = 1.0f;

        for (int i=0; i<4; ++i){
            switch(i){
                case 1:
                    yAdd = 0.0f;
                    break;
                case 2:
                    xAdd = 0.0f;
                    break;
                case 3:
                    yAdd = 1.0f;
                    break;
            }

            // Load position
            vertices[offset+0] = sprite.gameObject.transform.position.x + (xAdd * sprite.gameObject.transform.scale.x);
            vertices[offset+1] = sprite.gameObject.transform.position.y + (yAdd * sprite.gameObject.transform.scale.y);

            // Load Color
            vertices[offset+2] = color.x;
            vertices[offset+3] = color.y;
            vertices[offset+4] = color.z;
            vertices[offset+5] = color.w;

            // Load texture coordinates
            vertices[offset+6] = texCoords[i].x;
            vertices[offset+7] = texCoords[i].y;

            // Load texture id
            vertices[offset+8] = texId;

            offset += VERTEX_SIZE;
        }
    }

    private int[] generateIndices(){
        // 6 indicces per quad (3 per triangle)

        int[] elements = new int[6 * maxBatchSize];
        for (int i=0; i<maxBatchSize; ++i){
            loadElementIndices(elements, i);
        }

        return elements;
    }

    private void loadElementIndices(int[] elements, int index){
        // calculates the element indices for one quad


        int offsetArrayIndex = 6 * index;
        int offset = 4 * index;

        // 3, 2, 0,  0, 2, 1    7, 6, 4,   4, 6, 5  ....
        // Triang1   Triang2    Triang1    Triang2
        //       Quad 1               Quad 2

        // Triangle 1
        elements[offsetArrayIndex + 0] = offset + 3;
        elements[offsetArrayIndex + 1] = offset + 2;
        elements[offsetArrayIndex + 2] = offset + 0;

        // Triangle 2
        elements[offsetArrayIndex + 3] = offset + 0;
        elements[offsetArrayIndex + 4] = offset + 2;
        elements[offsetArrayIndex + 5] = offset + 1;
    }

    public boolean hasRoom(){
        return hasRoom;
    }

    public boolean hasTextureRoom(){
        return textures.size() < 8;
    }

    public boolean hasTexture(Texture texture){
        return textures.contains(texture);
    }

    public int zIndex(){
        return this.zIndex;
    }

    @Override
    public int compareTo(SpriteRenderBatch o) {
        return Integer.compare(this.zIndex, o.zIndex);
    }
}
