package core;

import external.control.InputContext;
import core.scene.Scene;

public class Main{
    
    public static void main(String args[]){
        
        AssetManager.createALCapabilities();
        InputContext.createDefaultContexts();
        
        GLWindow window = new GLWindow();
        AssetManager.loadResources();
        Shader.initDefaultShaders();
        TextMesh.init();
        
        
        Scene localScene = new Scene();
        window.scene = localScene;
        window.run();
        
        AssetManager.free();
        System.exit(0);
        
    }
}