package engine.components;

import engine.objects.Component;

public class FontRenderer extends Component {

    @Override
    public void start(){
        if (gameObject.getComponent(SpriteRenderer.class) != null){
            System.out.println("Fount Sprite Renderer!");
        }
    }
    @Override
    public void update(float dt) {

    }
    
}
