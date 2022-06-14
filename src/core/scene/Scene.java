package core.scene;

import core.AssetManager;
import core.Camera;
import core.FBO;
import core.Shader;
import core.TextMesh;
import core.instances.Instance;
import core.instances.PhysicsInstance;
import core.instances.SphereInstance;
import java.util.ArrayList;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL11.glDisable;

public class Scene {
    
    public static Scene activeScene;
    
    public Camera camera;
    public ArrayList<Instance> instances = new ArrayList<>();
    public ArrayList<TextMesh> texts = new ArrayList<>();
    public SceneRender render;
    public ScenePhysics physics;
    public boolean paused = false;
    
    long time = System.currentTimeMillis();
    
    public Scene(){
        activeScene = this;
        camera = new Camera();
        render = new SceneRender(this);
        init();
    }
    
    public void init(){
        render.createInstanceRender("Render 1", AssetManager.getMesh("Icosphere"), Shader.defaultShader);
        
        TextMesh text = new TextMesh("Top Left");
        text.setColor(new Vector3f(1,0,0));
        text.setTransform2D(.5f, 0, 0);
        texts.add(text);
        
        physics = new ScenePhysics();
        physics.init(this);
        
        for(int i=0; i<50; i++){
            PhysicsInstance instance = new SphereInstance();
            instances.add(instance);
            render.addInstance(instance, "Render 1");
            //instance.setArmature(AssetManager.getArmature("Monkey").getArmatureCopy());
            //instance.getArmature().setAnimation("Squirm");
            //instance.getArmature().setTime((float)Math.random()*10);
            physics.addPhysicsInstance(instance);
        }
        
    }

    public void render() {
       render.render();
       FBO.bindDefault();
       
       // Render the text, disables culling and depth test to allow text to always be visible
       glDisable(GL11.GL_DEPTH_TEST);
       glDisable(GL11.GL_CULL_FACE);
       for(TextMesh text:texts)
           text.render();
    }
    
    public void update(){
        float delta = (System.currentTimeMillis()- time)/1000f;
        time = System.currentTimeMillis();
        if(!paused){
           physics.update(delta, this);
           for(Instance instance:instances){
               if(instance.hasArmature()){
                   instance.getArmature().addTime(delta/2f);
               }
           }
           //camera.focusPos.set(test.pos);
           //camera.rotateAround(4);
        }
    }
    
    
    
}
