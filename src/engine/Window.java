package engine;

import org.lwjgl.Version;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

import engine.util.Time;

public class Window {
    
    private int width;
    private int height;
    private String title;

    private long glfwWindow = MemoryUtil.NULL;
    private static Window window = null;

    private static Scene currentScene;


    public float r,g,b,a;


    private Window() {
        this.width = 1920;
        this.height = 1080;
        this.title = "test";

        this.r = 1.0f;
        this.g = 1.0f;
        this.b = 1.0f;
        this.a = 1.0f;
    }

    public static Window get(){
        if (Window.window == null){
            Window.window = new Window();
        }

        return Window.window;
    }

    public static void changeScene(int newScene){
        switch (newScene){
            case 0:
                currentScene = new LevelEditorScene();
                currentScene.init();
                break;
            
            case 1:
                currentScene = new LevelScene();
                currentScene.init();
                break;
            
            default:
                assert false : "Unknown scene '" + newScene + "'";
        }
    }

    public void run(){
        System.out.println("Hello LWGJL " + Version.getVersion() + "!");

        init();
        loop();

        // Free the memory
        Callbacks.glfwFreeCallbacks(glfwWindow);
        GLFW.glfwDestroyWindow(glfwWindow);

        // Terminate GLFW and free the error callback
        GLFW.glfwTerminate();
        GLFW.glfwSetErrorCallback(null).free();
    }

    public void init(){
        // Set up an error callback
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW
        if (!GLFW.glfwInit()){
            throw new IllegalStateException("Unable to initialize GLFW!");
        }

        // Configer GLFW
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_MAXIMIZED, GLFW.GLFW_TRUE);

        // Create the window
        glfwWindow = GLFW.glfwCreateWindow(this.width, this.height, this.title, MemoryUtil.NULL, MemoryUtil.NULL);
        if (glfwWindow == MemoryUtil.NULL){
            throw new IllegalStateException("Failed to create the GLFW window!");
        }

        // Forward GLFW callbacks to our function (lambda expression)
        GLFW.glfwSetCursorPosCallback(glfwWindow, MouseListener::mousePosCallback);
        GLFW.glfwSetMouseButtonCallback(glfwWindow, MouseListener::mouseButtonCallback);
        GLFW.glfwSetScrollCallback(glfwWindow, MouseListener::mouseScrollCallback);
        GLFW.glfwSetKeyCallback(glfwWindow, KeyListener::keyCallback);

        // Make the OpenGL context current
        GLFW.glfwMakeContextCurrent(glfwWindow);

        // enable v-sync
        GLFW.glfwSwapInterval(1);

        // Mske the window visible
        GLFW.glfwShowWindow(glfwWindow);

        // This line is critical for LWJGL's interoperations with GLFW's OpenGL context
        // or any context that is managed externally.
        // ! will break without this line!
        GL.createCapabilities();

        Window.changeScene(0);
    }

    public void loop(){

        float beginTime = Time.getTime();
        float endTime;
        float dt = -1.0f;
        float fps = -1.0f;

        while (!GLFW.glfwWindowShouldClose(glfwWindow)) {
            // Poll events
            GLFW.glfwPollEvents();

            GL11.glClearColor(r, g, b, a);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

            if (dt >= 0) {
                currentScene.update(dt);
            }

            /*
            if (KeyListener.isKeyPressed(GLFW.GLFW_KEY_SPACE)){
                System.out.println("Space key pressed");
            }*/
 
            GLFW.glfwSwapBuffers(glfwWindow);
            
            endTime = Time.getTime();
            dt = endTime-beginTime;
            beginTime = endTime;
            fps = 1.0f / dt;

            System.out.println("fps:" + fps);
        }
    }
}