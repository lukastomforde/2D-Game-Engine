package engine;

public abstract class Scene {
    
    protected Camera camera;
    private boolean isRunning = false;

    public Scene(){}

    public void init(){}

    public abstract void update(float dt);

    
}
