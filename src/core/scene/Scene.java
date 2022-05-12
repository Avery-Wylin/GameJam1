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
import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL11.glDisable;

public class Scene {
    public static Camera camera;
    ArrayList<Actable> actableObjects = new ArrayList<>();
    ArrayList<Instance> instances = new ArrayList<>();
    ArrayList<TextMesh> texts = new ArrayList<>();
    public SceneRender render;
    public Instance test;
    
    long time = System.currentTimeMillis();
    
    public Scene(){
        camera = new Camera();
        render = new SceneRender(this);
        init();
    }
    
    public void init(){
        render.createInstanceRender("Sphere", AssetManager.getMesh("Tetrapod"), Shader.animatedShader,"sand");
        test = new Instance();
        test.setArmature(AssetManager.getArmature("Tetrapod").getArmatureCopy());
        test.getArmature().setAnimation("Tail Down");
        test.getArmature().setTime(0);
        test.getArmature().update();
        instances.add( test);
        
        texts.add( new TextMesh("ARMATURE\nTIME"));
        
        for(Instance i:instances){
            render.addInstance(i, "Sphere");
        }
    }

    public void render() {
       render.render();
       FBO.bindDefault();
       glDisable(GL11.GL_DEPTH_TEST);
       glDisable(GL11.GL_CULL_FACE);
       for(TextMesh text:texts)
           text.render();
    }
    
    public void update(){
        float delta = (System.currentTimeMillis()- time)/1000f;
        time = System.currentTimeMillis();
        for(Actable obj:actableObjects){
            obj.action(delta);
        }
        
        test.getArmature().addTime(delta);
    }
    
}
