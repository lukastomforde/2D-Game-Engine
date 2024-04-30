package engine.components;

import org.joml.Vector4f;

import engine.objects.Component;

public class HexRenderer extends Component {

    private Vector4f color;
    
    public HexRenderer(Vector4f color){
        this.color = color;
    }

    @Override
    public void start(){

    }

    @Override
    public void update(float dt) {

    }

    public Vector4f getColor(){
        return color;
    }
    
}
