package engine;

public class LevelScene extends Scene {

    public LevelScene(){
        Window.get().r -= 1.0f;
        Window.get().g -= 1.0f;
        Window.get().b -= 1.0f;
    }

    @Override
    public void update(float dt) {

    }
    
}
