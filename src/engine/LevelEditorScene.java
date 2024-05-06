package engine;

import org.joml.Vector2f;
import org.joml.Vector4f;

import engine.components.SpriteRenderer;
import engine.components.Spritesheet;
import engine.objects.GameObject;
import engine.util.AssetPool;

public class LevelEditorScene extends Scene {

    private GameObject obj1;
    private Spritesheet sprites;

    public LevelEditorScene(){
        
    }

    @Override
    public void init(){
        this.camera = new Camera(new Vector2f());

        /*
        int xOffset = 10;
        int yOffset = 10;

        float totalWidth = (float)(600 - xOffset * 2);
        float totalHeight = (float)(600 - xOffset * 2);
        float sizeX = totalWidth / 100.0f;
        float sizeY = totalHeight / 100.0f;

        for (int x=0; x<100; x++){
            for (int y=0; y<100; y++){
                float xPos = xOffset + (x * sizeX) + x*2.0f;
                float yPos = yOffset + (y * sizeY) + y*2.0f;

                GameObject go = new GameObject("Obj" + x + "" + y, new Transform(new Vector2f(xPos, yPos), new Vector2f(sizeX, sizeY)));
                go.addComponent(new SpriteRenderer(new Vector4f(1-xPos/ totalWidth, yPos/totalWidth, 1.0f , 1.0f)));
                this.addGameObjectToScene(go);
            }
        }

        // add a hexagon
        GameObject hex = new GameObject("hex", new Transform(new Vector2f(900, 600), new Vector2f(100, 0)));
        hex.addComponent(new HexRenderer(new Vector4f(0.9f, 0.6f, 1.0f, 1.0f)));
        this.addGameObjectToScene(hex);
*/

        loadResources();

        sprites = AssetPool.getSpritesheet("spritesheet.png");

        obj1 = new GameObject("Object 1", new Transform(new Vector2f(100, 100), new Vector2f(200, 200)), 1);
        obj1.addComponent(new SpriteRenderer(sprites.getSprite(1)));
        addGameObjectToScene(obj1);

        GameObject obj2 = new GameObject("Object 2", new Transform(new Vector2f(500, 100), new Vector2f(200, 200)), 2);
        obj2.addComponent(new SpriteRenderer(new Vector4f(1.0f, 0.0f, 0.0f, 0.3f)));
        addGameObjectToScene(obj2);

        /*
        GameObject obj2 = new GameObject("Object 2", new Transform(new Vector2f(400, 100), new Vector2f(200, 200)));
        obj2.addComponent(new SpriteRenderer(sprites.getSprite(2)));
        addGameObjectToScene(obj2);

        GameObject obj3 = new GameObject("Object 2", new Transform(new Vector2f(700, 100), new Vector2f(200, 200)));
        obj3.addComponent(new SpriteRenderer(sprites.getSprite(3)));
        addGameObjectToScene(obj3);
        */
    }

    private void loadResources(){
        AssetPool.loadShader("default.glsl");
        AssetPool.loadTexture("spritesheet.png");

        AssetPool.addSpritesheet("spritesheet.png", new Spritesheet(AssetPool.getTexture("spritesheet.png"), 16, 16, 26, 0));
    }

    private int spriteIndex = 0;
    private float spriteFlipTime = 0.2f;
    private float spriteFlipTimeCounter = 0.0f;

    @Override
    public void update(float dt) {        
        for(GameObject go : gameObjects){
            go.update(dt);
        }

        spriteFlipTimeCounter -= dt;
        if (spriteFlipTimeCounter <= 0){
            spriteFlipTimeCounter = spriteFlipTime;
            spriteIndex ++;
            if (spriteIndex > 3){
                spriteIndex = 0;
            }
            obj1.getComponent(SpriteRenderer.class).setSprite(sprites.getSprite(spriteIndex));
        }

        obj1.transform.position.x += 40 * dt;

        this.renderer.render();
    }
}
