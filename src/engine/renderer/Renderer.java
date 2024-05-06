package engine.renderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import engine.components.HexRenderer;
import engine.components.SpriteRenderer;
import engine.objects.GameObject;

public class Renderer {
    
    private final int MAX_BATCH_SIZE = 1000;
    private List<SpriteRenderBatch> spritBatches;
    private List<HexRenderBatch> hexBatches;

    public Renderer(){
        this.spritBatches = new ArrayList<>();
        this.hexBatches = new ArrayList<>();
    }

    public void add(GameObject go){
        SpriteRenderer spr = go.getComponent(SpriteRenderer.class);
        if (spr != null){
            addSprite(spr);
            return;
        }

        HexRenderer hex = go.getComponent(HexRenderer.class);
        if (hex != null){
            addHex(hex);
        }
    }

    private void addSprite(SpriteRenderer sprite){
        boolean added = false;
        for(SpriteRenderBatch batch : spritBatches){
            if(batch.hasRoom() && batch.zIndex() == sprite.gameObject.zIndex()){
                Texture texture = sprite.getTexture();
                if(texture == null || batch.hasTexture(texture) || batch.hasTextureRoom()){
                    batch.addSprite(sprite);
                    added = true;
                    break;
                }
            }
        }

        if (!added){
            SpriteRenderBatch batch = new SpriteRenderBatch(MAX_BATCH_SIZE, sprite.gameObject.zIndex());
            batch.start();
            spritBatches.add(batch);
            batch.addSprite(sprite);
            Collections.sort(spritBatches);
        }
    }

    private void addHex(HexRenderer hex){
        boolean added = false;
        for(HexRenderBatch batch : hexBatches){
            if(batch.hasRoom()){
                batch.addHex(hex);
                added = true;
                break;
            }
        }

        if (!added){
            HexRenderBatch batch = new HexRenderBatch(MAX_BATCH_SIZE);
            batch.start();
            hexBatches.add(batch);
            batch.addHex(hex);
        }
    }

    public void render(){
        for (SpriteRenderBatch batch : spritBatches){
            batch.render();
        }

        //for (HexRenderBatch batch : hexBatches){
        //    batch.render();
        //}
    }
}
