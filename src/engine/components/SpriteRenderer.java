package engine.components;

import org.joml.Vector2f;
import org.joml.Vector4f;

import engine.Transform;
import engine.objects.Component;
import engine.renderer.Texture;

public class SpriteRenderer extends Component {

    private Vector4f color;
    private Sprite sprite;
    private Transform lastTransform;
    private boolean isDirty = false;

    public SpriteRenderer(Vector4f color){
        this.color = color;
        this.sprite = new Sprite(null);
        this.isDirty = true;
    }

    public SpriteRenderer(Sprite sprite){
        this.sprite = sprite;
        this.color = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.isDirty = true;
    }

    @Override
    public void start(){
        this.lastTransform = gameObject.transform.copy();
    }

    @Override
    public void update(float dt) {
        if(!this.lastTransform.equals(this.gameObject.transform)){
            this.gameObject.transform.copyTo(this.lastTransform);
            isDirty = true;
        }
    }

    public Vector4f getColor(){
        return color;
    }

    public Texture getTexture(){
        return sprite.getTexture();
    }

    public Vector2f[] getTexCoords(){
        return sprite.getTexCoords();
    }

    public void setSprite(Sprite sprite){
        this.sprite = sprite;
        isDirty = true;
    }

    public void setColor(Vector4f color){
        if(!this.color.equals(color)){
            this.color.set(color);
            isDirty = true;
        }
    }

    public boolean isDirty(){
        return isDirty;
    }

    public void setClean(){
        isDirty = false;
    }
    
}
