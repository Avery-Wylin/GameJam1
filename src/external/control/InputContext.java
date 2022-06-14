package external.control;

import core.AssetManager;
import core.GLWindow;
import core.Shader;
import core.instances.PhysicsInstance;
import core.instances.SphereInstance;
import core.scene.Scene;
import external.entities.Player;
import org.joml.Vector3f;
import static org.lwjgl.glfw.GLFW.*;

public abstract class InputContext {
    
    public static final int DEBUG = 0;
    public static final int FIRST_PERSON = 1;
    private static final int CONTEXT_COUNT = 8;
    private static InputContext contexts[] = new InputContext[CONTEXT_COUNT];
    private static int activeContext = 0;
    
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
    
    
    public abstract void keyInput(long windowID, int key, int scancode, int action, int mods);
    public abstract void cursorInput(long windowID, double x, double y);
    public abstract void scrollInput(long window, double xoffset, double yoffset);
    public abstract void open();
    public abstract void close();
    public abstract void update();
    
    
    public static void createDefaultContexts(){
        
/* DEBUG CONTEXT*/

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
                            Scene.activeScene.render.atmosphere.loadUniforms();
                            break;
                        case GLFW_KEY_W:
                            if (GLWindow.activeWindow.enableCursor) {
                                GLWindow.activeWindow.disableCursor();
                            } else {
                                GLWindow.activeWindow.enableCursor();
                            }
                            break;
                        case GLFW_KEY_ESCAPE:
                            glfwSetWindowShouldClose(windowID, true);
                            break;
                        case GLFW_KEY_R:
                            //GLWindow.activeWindow.localScene.test.getArmature().resetAnimation();
                            break;
                        case GLFW_KEY_T:
                            //GLWindow.activeWindow.localScene.test.getArmature().addTime(1f);
                            //System.out.println(GLWindow.activeWindow.localScene.test.getArmature().getTime());
                            break;
                        case GLFW_KEY_F:
                            //GLWindow.activeWindow.localScene.test.getArmature().forceUpdate();
                            //GLWindow.activeWindow.localScene.test.getArmature().printHierarchy();
                            break;
                        case GLFW_KEY_P:
                            Scene.activeScene.paused = !Scene.activeScene.paused;
                            break;
                        case GLFW_KEY_Y:
                            isActingForce = true;
                            break;
                            
                        case GLFW_KEY_G:
                            PhysicsInstance sphere = new SphereInstance();
                            sphere.setArmature(AssetManager.getArmature("Monkey").getArmatureCopy());
                            sphere.getArmature().setAnimation("Squirm");
                            Scene.activeScene.instances.add(sphere);
                            Scene.activeScene.render.addInstance(sphere, "Render 1");
                            Scene.activeScene.physics.addPhysicsInstance(sphere);
                            
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
                
                if (GLWindow.activeWindow.width <= 0 || GLWindow.activeWindow.height <= 0)
                    return;
                
                if( !GLWindow.activeWindow.enableCursor) {
                        //Camera.defaultCamera.rot.y -= (x - width / 2) / width;
                        orbitAngle.y -= (x - GLWindow.activeWindow.width / 2) / GLWindow.activeWindow.width;
                    if (Math.abs(Scene.activeScene.camera.rot.x + (y - GLWindow.activeWindow.height / 2)) / GLWindow.activeWindow.height < 1f) {
                        //Camera.defaultCamera.rot.x -= (y - height / 2) / height;
                        orbitAngle.x -= (y - GLWindow.activeWindow.height / 2) / GLWindow.activeWindow.height;
                    }
                    Scene.activeScene.camera.rotateAround(orbitAngle, orbitRad);
                    glfwSetCursorPos(windowID, GLWindow.activeWindow.width / 2, GLWindow.activeWindow.height / 2);
                }
                else if(isActingForce){
                   Vector3f ray = Scene.activeScene.camera.rayCast((float)x, (float)y, 15);
                   Scene.activeScene.physics.attractTowards(ray.x, ray.y, ray.z, 40f);
                }
            }

            @Override
            public void scrollInput(long window, double xoffset, double yoffset) {
                orbitRad-=yoffset*.5f;
                Scene.activeScene.camera.rotateAround(orbitAngle, orbitRad);
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
        
/* FIRST PERSON CONTEXT*/

        contexts[FIRST_PERSON] = new InputContext() {
            short motionFlag = 0;
            float speed = .01f;
            Player player;
            
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
                    Scene.activeScene.camera.pos.x += speed * Math.cos(Scene.activeScene.camera.rot.y);
                    Scene.activeScene.camera.pos.z += speed * Math.sin(Scene.activeScene.camera.rot.y);
                }
                    
            }
        };
        
                
                
    }
}