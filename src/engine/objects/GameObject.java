package engine.objects;

import java.util.ArrayList;
import java.util.List;

import engine.Transform;

public class GameObject {
    
    private String name;
    private List<Component> components;
    public Transform transform;
    private int zIndex;
    
    public GameObject(String name){
        init(name, new Transform(), 0);
    }

    public GameObject(String name, Transform transform, int zIndex){
        init(name, transform, zIndex);
    }

    public void init(String name, Transform transform, int zIndex){
        this.name = name;
        this.transform = transform;
        this.zIndex = zIndex;
        this.components = new ArrayList<>();
    }

    public <T extends Component> T getComponent(Class<T> componentClass){
        for (Component c : components){
            if(componentClass.isAssignableFrom(c.getClass())){
                try {
                    return componentClass.cast(c);   
                } catch (Exception e) {
                    assert false : "Error: Failed casting component.";
                }
            }
        }

        return null;
    }

    public <T extends Component> void removeComponent(Class<T> componentClass){
        for (int i=0; i<components.size(); ++i){
            Component c = components.get(i);
            if (componentClass.isAssignableFrom(c.getClass())){
                components.remove(i);
                return;
            }
        }
    }

    public void addComponent(Component c){
        components.add(c);
        c.gameObject = this;
    }

    public void update(float dt){
        for (int i=0; i<components.size(); ++i){
            components.get(i).update(dt);
        }
    }

    public void start(){
        for (int i=0; i<components.size(); ++i){
            components.get(i).start();
        }
    }

    public int zIndex(){
        return zIndex;
    }
}
