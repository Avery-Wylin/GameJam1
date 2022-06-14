package core;

import core.audio.SoundBuffer;
import core.audio.SoundSource;
import external.control.InputContext;
import core.scene.Scene;
import org.joml.Vector3f;
import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL11C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.glClear;
import static org.lwjgl.opengl.GL11C.glClearColor;
import static org.lwjgl.system.MemoryUtil.NULL;

public class GLWindow implements Runnable{
    public static GLWindow activeWindow;
    private long windowID;
    public Scene scene;
    public int width,height;
    public boolean enableCursor = false;
    
    SoundBuffer ring = new SoundBuffer("seraphim");
    SoundSource ringSrc = new SoundSource(true, true, "seraphim");
    
    public GLWindow(){
        activeWindow = this;
        init();
    }
    
    @Override
    public void run(){
        loop();
        
        // Free the Window upon termination
        glfwFreeCallbacks(windowID);
        glfwDestroyWindow(windowID);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }
    
    private void init(){
        
        GLFWErrorCallback.createPrint(System.err).set();
        if( !glfwInit() ){
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        
        // Window properties
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        
        // Create Window
        windowID = glfwCreateWindow(500, 500, "Mesh Generator", NULL, NULL);
        if(windowID == NULL){
            throw new RuntimeException("Failed to create the GLFW window");
        }
        
        // Set GL context to window
        glfwMakeContextCurrent(windowID);
        
        // V-Sync
        glfwSwapInterval(1);
        
        // Create Capabilities
        GL.createCapabilities();
        
        // Set Visible
        glfwShowWindow(windowID);
        
        enableCursor();
        
        // Get the resolution of the monitor
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        
        width = (int)(vidmode.width()/1.5f);
        height = (int)(vidmode.height()/1.5f);
        glfwSetWindowSize(windowID, width, height);
        
        
        // Move the window to the center
        glfwSetWindowPos(
            windowID, 
            (vidmode.width() - width)/2,
            (vidmode.height() - height)/2
        );
        
        // Resize Event
        glfwSetWindowSizeCallback(windowID,
            new GLFWWindowSizeCallback(){
                @Override
                public void invoke(long window, int w, int h){
                    FBO.setWindowDimensions(w, h);
                    width = w;
                    height = h;
                    
                    // Fit the GL view to the window, updates aspect ratio for Text, Camera, and UI
                    glViewport(0,0,width,height);
                    scene.camera.setViewDimensions(width, height);
                    scene.camera.updatePerspective();
                    TextMesh.updateAspect();
                }
            }
        );
        
        // Key Event
        glfwSetKeyCallback(windowID,
                (windowID, key, scancode, action, mods) -> {
                    InputContext.getActive().keyInput(windowID, key, scancode, action, mods);
                }
        );
        
        glfwSetCursorPosCallback(windowID,
                new GLFWCursorPosCallback() {
            float sensitivity = .5f;
            @Override
            public void invoke(long window, double x, double y) {
                InputContext.getActive().cursorInput(windowID, x, y);
            }
        }
        );
        
        glfwSetScrollCallback(windowID,
                new GLFWScrollCallback(){
                    @Override
                    public void invoke(long window, double xoffset, double yoffset) {
                        InputContext.getActive().scrollInput(window, xoffset, yoffset);
                    }
                    
                }
        );
        
    }
    
    public void disableCursor(){
        enableCursor = false;
        glfwSetInputMode(windowID, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
    }
    
    public void enableCursor(){
        enableCursor = true;
        glfwSetInputMode(windowID, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
    }
    
    private void loop(){
        
        // Set Render Settings
        glClearColor(0,0,0,0);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        
        
        while( !glfwWindowShouldClose(windowID)){
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            
            // Process Input Updates
            InputContext.getActive().update();
            
            // Render
            scene.camera.updateCameraTransform();
            scene.render();
            scene.update();
            
            glfwSwapBuffers(windowID);
            glfwPollEvents();
        }
    }
    
    
}
