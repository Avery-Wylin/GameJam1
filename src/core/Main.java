package core;

import core.control.InputContext;
import core.scene.Scene;

public class Main{
    
    public static void main(String args[]){
        
        AssetManager.createALCapabilities();
        InputContext.createDefaultContexts();
        
        GLWindow window = new GLWindow();
        InputContext.setActiveGLWindow(window);
        AssetManager.loadResources();
        Shader.initDefaultShaders();
        TextMesh.init();
        
        
        Scene localScene = new Scene();
        window.localScene = localScene;
        window.run();
        
        AssetManager.free();
        System.exit(0);
        
    }
}