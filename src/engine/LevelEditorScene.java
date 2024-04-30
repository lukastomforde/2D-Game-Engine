package engine;

import org.joml.Vector2f;
import org.joml.Vector4f;

import engine.components.HexRenderer;
import engine.components.SpriteRenderer;
import engine.objects.GameObject;

public class LevelEditorScene extends Scene {

    public LevelEditorScene(){
        
    }

    @Override
    public void init(){
        this.camera = new Camera(new Vector2f());

        int xOffset = 10;
        int yOffset = 10;

        float totalWidth = (float)(600 - xOffset * 2);
        float totalHeight = (float)(600 - xOffset * 2);
        float sizeX = totalWidth / 100.0f;
        float sizeY = totalHeight / 100.0f;

        for (int x=0; x<100; x++){
            for (int y=0; y<100; y++){
                float xPos = xOffset + (x * sizeX);
                float yPos = yOffset + (y * sizeY);

                GameObject go = new GameObject("Obj" + x + "" + y, new Transform(new Vector2f(xPos, yPos), new Vector2f(sizeX, sizeY)));
                go.addComponent(new SpriteRenderer(new Vector4f(1-xPos/ totalWidth, yPos/totalWidth, 1.0f , 1.0f)));
                this.addGameObjectToScene(go);
            }
        }

        // add a hexagon
        GameObject hex = new GameObject("hex", new Transform(new Vector2f(750, 600), new Vector2f(100, 0)));
        hex.addComponent(new HexRenderer(new Vector4f(0.9f, 0.6f, 1.0f, 1.0f)));
        this.addGameObjectToScene(hex);
    }

    @Override
    public void update(float dt) {
        for(GameObject go : gameObjects){
            go.update(dt);
        }

        this.renderer.render();
    }
}
