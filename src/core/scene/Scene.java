package core.scene;

import core.AssetManager;
import core.Camera;
import core.FBO;
import core.Shader;
import core.TextMesh;
import core.animation.Armature;
import core.instances.Actable;
import core.instances.Instance;
import java.util.ArrayList;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL11.glDisable;

public class Scene {
    public static Camera camera;
    ArrayList<Actable> actableObjects = new ArrayList<>();
    ArrayList<Instance> instances = new ArrayList<>();
    ArrayList<TextMesh> texts = new ArrayList<>();
    public SceneRender render;
    public ScenePhysics physics;
    public boolean paused = false;
    
    long time = System.currentTimeMillis();
    
    public Scene(){
        camera = new Camera();
        render = new SceneRender(this);
        init();
    }
    
    public void init(){
        render.createInstanceRender("Render 1", AssetManager.getMesh("Suzanne"), Shader.defaultShader);
        render.createInstanceRender("Render 2", AssetManager.getMesh("Sphere"), Shader.defaultShader);
        
        TextMesh text = new TextMesh("Top Left");
        text.setColor(new Vector3f(1,0,0));
        text.setTransform2D(.5f, 0, 0);
        texts.add(text);
        
        for(int i=0; i<200; i++){
            Instance instance = new Instance();
            instances.add(instance);
            if(Math.random()>.5f)
                render.addInstance(instance, "Render 1");
            else
                render.addInstance(instance, "Render 2");
        }
        
        physics = new ScenePhysics();
        physics.init();
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
            for(Actable obj:actableObjects){
                obj.action(delta);
            }
           physics.update(instances,delta);
           //camera.focusPos.set(test.pos);
           //camera.rotateAround(4);
        }
    }
    
}
