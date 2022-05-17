package core.control;

import core.GLWindow;
import core.Shader;
import core.scene.Scene;
import org.joml.Vector3f;
import static org.lwjgl.glfw.GLFW.*;

public abstract class InputContext {
    
    public static final int DEBUG = 0;
    public static final int FIRST_PERSON = 1;
    private static final int CONTEXT_COUNT = 8;
    private static InputContext contexts[] = new InputContext[CONTEXT_COUNT];
    private static int activeContext = 0;
    private static GLWindow activeWindow;
    
    public static InputContext getInputContext(int ID){
        return contexts[Math.min(Math.max(ID,0), CONTEXT_COUNT)];
    }
    
    public static InputContext getActive(){
        return contexts[activeContext];
    }
    
    public static void setActiveContext(int contextSlot){
        contexts[activeContext].close();
        activeContext = Math.min(Math.max(contextSlot,0), CONTEXT_COUNT);
        contexts[activeContext].open();
    }
    
    public static void setActiveGLWindow(GLWindow window){
        activeWindow = window;
    }
    
    public abstract void keyInput(long windowID, int key, int scancode, int action, int mods);
    public abstract void cursorInput(long windowID, double x, double y);
    public abstract void scrollInput(long window, double xoffset, double yoffset);
    public abstract void open();
    public abstract void close();
    public abstract void update();
    
    
    public static void createDefaultContexts(){
        
        // Debug Context
        contexts[DEBUG] = new InputContext() {
            
            public Vector3f orbitAngle = new Vector3f();
            public float orbitRad = 4f;
            public boolean isActingForce = false;
            
            @Override
            public void keyInput(long windowID, int key, int scancode, int action, int mods) {
                if (action == GLFW_PRESS) {
                    switch (key) {
                        case GLFW_KEY_1:
                            setActiveContext(FIRST_PERSON);
                            break;
                        case GLFW_KEY_Q:
                            Shader.recompileAll();
                            activeWindow.localScene.render.atmosphere.update();
                            activeWindow.localScene.render.atmosphere.loadUniforms();
                            break;
                        case GLFW_KEY_W:
                            if (activeWindow.enableCursor) {
                                activeWindow.disableCursor();
                            } else {
                                activeWindow.enableCursor();
                            }
                            break;
                        case GLFW_KEY_ESCAPE:
                            glfwSetWindowShouldClose(windowID, true);
                            break;
                        case GLFW_KEY_R:
                            //activeWindow.localScene.test.getArmature().resetAnimation();
                            break;
                        case GLFW_KEY_T:
                            //activeWindow.localScene.test.getArmature().addTime(1f);
                            //System.out.println(activeWindow.localScene.test.getArmature().getTime());
                            break;
                        case GLFW_KEY_F:
                            //activeWindow.localScene.test.getArmature().forceUpdate();
                            //activeWindow.localScene.test.getArmature().printHierarchy();
                            break;
                        case GLFW_KEY_P:
                            activeWindow.localScene.paused = !activeWindow.localScene.paused;
                            break;
                        case GLFW_KEY_Y:
                            isActingForce = true;
                            break;
                    }
                }
                else if(action == GLFW_RELEASE){
                    switch(key){
                      case GLFW_KEY_Y:
                            isActingForce = false;
                            break;  
                    }
                }
            }

            @Override
            public void cursorInput(long windowID, double x, double y) {
                
                if (activeWindow.width <= 0 || activeWindow.height <= 0)
                    return;
                
                if( !activeWindow.enableCursor) {
                        //Camera.defaultCamera.rot.y -= (x - width / 2) / width;
                        orbitAngle.y -= (x - activeWindow.width / 2) / activeWindow.width;
                    if (Math.abs(activeWindow.localScene.camera.rot.x + (y - activeWindow.height / 2)) / activeWindow.height < 1f) {
                        //Camera.defaultCamera.rot.x -= (y - height / 2) / height;
                        orbitAngle.x -= (y - activeWindow.height / 2) / activeWindow.height;
                    }
                    activeWindow.localScene.camera.rotateAround(orbitAngle, orbitRad);
                    glfwSetCursorPos(windowID, activeWindow.width / 2, activeWindow.height / 2);
                }
                else if(isActingForce){
                   Vector3f ray = activeWindow.localScene.camera.rayCast((float)x, (float)y, 15);
                   activeWindow.localScene.physics.attractTowards(ray.x, ray.y, ray.z, 40f);
                }
            }

            @Override
            public void scrollInput(long window, double xoffset, double yoffset) {
                orbitRad-=yoffset*.5f;
                activeWindow.localScene.camera.rotateAround(orbitAngle, orbitRad);
            }

            @Override
            public void open() {
            }

            @Override
            public void close() {
            }

            @Override
            public void update() {
            }
        };
        
        // First Person
        contexts[FIRST_PERSON] = new InputContext() {
            short motionFlag = 0;
            float speed = .01f;
            
            @Override
            public void keyInput(long windowID, int key, int scancode, int action, int mods) {
                if (action == GLFW_PRESS) {
                    switch (key) {
                        case GLFW_KEY_0:
                            setActiveContext(DEBUG);
                            break;
                        case GLFW_KEY_W:
                            motionFlag |= 1;
                            break;
                        case GLFW_KEY_A:
                            motionFlag |= 2;
                            break;
                        case GLFW_KEY_S:
                            motionFlag |= 4;
                            break;
                        case GLFW_KEY_D:
                            motionFlag |= 8;
                            break;
                    }
                }
                if (action == GLFW_RELEASE) {
                    switch (key) {
                        case GLFW_KEY_0:
                            setActiveContext(DEBUG);
                            break;
                        case GLFW_KEY_W:
                            motionFlag &= ~1;
                            break;
                        case GLFW_KEY_A:
                            motionFlag &= ~2;
                            break;
                        case GLFW_KEY_S:
                            motionFlag &= ~4;
                            break;
                        case GLFW_KEY_D:
                            motionFlag &= ~8;
                            break;
                    }
                }
            }

            @Override
            public void cursorInput(long windowID, double x, double y) {
            }

            @Override
            public void scrollInput(long window, double xoffset, double yoffset) {
            }

            @Override
            public void open() {
            }

            @Override
            public void close() {
            }

            @Override
            public void update() {
                if((motionFlag & 1) == 1){
                    Scene.camera.pos.x += speed * Math.cos(Scene.camera.rot.y);
                    Scene.camera.pos.z += speed * Math.sin(Scene.camera.rot.y);
                }
                    
            }
        };
        
                
                
    }
}