package core.scene;

import core.AtmosphereSettings;
import core.FBO;
import core.Mesh;
import core.Shader;
import core.WaterPlane;
import core.instances.Instance;
import core.instances.InstanceRender;
import java.util.HashMap;
import java.util.LinkedList;
import org.joml.Vector3f;
import org.joml.Vector4f;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

/**
 *
 * @author ajw
 */
public class SceneRender {
    
    private Scene scene;
    public LinkedList<InstanceRender> instanceRenders = new LinkedList<>();
    private HashMap<String,InstanceRender> instanceRenderMap = new HashMap<>();
    public AtmosphereSettings atmosphere,underwater;
    private FBO mainFBO, effectFBO;
    public WaterPlane waterPlane;
    
    public SceneRender(Scene scene){
        this.scene = scene;
        atmosphere = new AtmosphereSettings();
        atmosphere.up = new Vector3f(1f,1f,1f);
        atmosphere.down = new Vector3f(0.05f,0f,0.1f);
        atmosphere.direction = new Vector3f(0,1f,0);
        
        underwater = new AtmosphereSettings();
        underwater.down = new Vector3f(0f,.2f,.3f);
        underwater.up = new Vector3f(.3f,.85f,.95f);
        underwater.direction = new Vector3f( 0f,1f,0f);
        underwater.fog_density = .03f;
        underwater.fog_exponent = .5f;
        
        mainFBO = new FBO(1024,1024);
        mainFBO.allocate();
        mainFBO.createDepthAttachment();
        mainFBO.createColorAttachment(0);
        mainFBO.setShader(FBO.fboShader);
        
        waterPlane = new WaterPlane();
    }
    
    
    public void createInstanceRender(String name, Mesh mesh, Shader shader){
        InstanceRender temp = new InstanceRender();
        temp.setMesh(mesh);
        temp.setShader(shader);
        instanceRenderMap.put(name,temp);
        instanceRenders.push(temp);
    }
    
    public void createInstanceRender(String name, Mesh mesh, Shader shader,String texture){
        InstanceRender temp = new InstanceRender();
        temp.setMesh(mesh);
        temp.setShader(shader);
        temp.setTexture(texture);
        instanceRenderMap.put(name,temp);
        instanceRenders.push(temp);
    }
    
    public void addInstance(Instance instance, String renderName){
        if(instanceRenderMap.get(renderName)!=null){
            instanceRenderMap.get(renderName).addInstance(instance);
            
        }
        else{
            System.err.println("Could not assign instance to "+renderName);
        }
    }
    
    public InstanceRender getInstanceRender(String name) {
        return instanceRenderMap.get(name);
    }
    
    public InstanceRender getInstanceRender(int index) {
        return instanceRenders.get(index);
    }
    
    public void render() {
        
        // Render the water
        
        // Top
        atmosphere.loadUniforms();
        waterPlane.bindTop();
        if(Scene.activeScene.camera.pos.y >= waterPlane.getWaterLevel()){
            scene.camera.pos.y = scene.camera.pos.y - 2*(scene.camera.pos.y-waterPlane.getWaterLevel());
            scene.camera.rot.x *= -1;
            scene.camera.updateCameraTransform();

            Shader.uniformAllMatrix4f(Shader.CAMERA, scene.camera.transform, Shader.COLLECTION_USE_CAMERA_TRANSFORM );
            Shader.uniformAllMatrix4f(Shader.PERSPECTIVE, scene.camera.perspective, Shader.COLLECTION_USE_CAMERA_TRANSFORM);

            scene.camera.pos.y = scene.camera.pos.y - 2*(scene.camera.pos.y-waterPlane.getWaterLevel());
            scene.camera.rot.x *= -1;
            scene.camera.updateCameraTransform();
        }
        else{
            
            Shader.uniformAllMatrix4f(Shader.CAMERA, scene.camera.transform, Shader.COLLECTION_USE_CAMERA_TRANSFORM );
            Shader.uniformAllMatrix4f(Shader.PERSPECTIVE, scene.camera.perspective, Shader.COLLECTION_USE_CAMERA_TRANSFORM);
        }
        
        Shader.enableCausticTexture = false;
        glEnable(GL30.GL_CLIP_DISTANCE0);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glEnable(GL_DEPTH_TEST);
        atmosphere.render();
        for(InstanceRender temp:instanceRenders){
            temp.render();
        }
        
        
        // Bottom
        waterPlane.bindBottom();
        underwater.loadUniforms();
       if(Scene.activeScene.camera.pos.y < waterPlane.getWaterLevel()){
            scene.camera.pos.y = scene.camera.pos.y - 2*(scene.camera.pos.y-waterPlane.getWaterLevel());
            scene.camera.rot.x *= -1;
            scene.camera.updateCameraTransform();

            Shader.uniformAllMatrix4f(Shader.CAMERA, scene.camera.transform, Shader.COLLECTION_USE_CAMERA_TRANSFORM );
            Shader.uniformAllMatrix4f(Shader.PERSPECTIVE, scene.camera.perspective, Shader.COLLECTION_USE_CAMERA_TRANSFORM);

            scene.camera.pos.y = scene.camera.pos.y - 2*(scene.camera.pos.y-waterPlane.getWaterLevel());
            scene.camera.rot.x *= -1;
            scene.camera.updateCameraTransform();
        }
        else{
            
            Shader.uniformAllMatrix4f(Shader.CAMERA, scene.camera.transform, Shader.COLLECTION_USE_CAMERA_TRANSFORM );
            Shader.uniformAllMatrix4f(Shader.PERSPECTIVE, scene.camera.perspective, Shader.COLLECTION_USE_CAMERA_TRANSFORM);
        }
        Shader.enableCausticTexture = true;
        glEnable(GL30.GL_CLIP_DISTANCE0);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glEnable(GL_DEPTH_TEST);
        atmosphere.render();
        for(InstanceRender temp:instanceRenders){
            temp.render();
        }
        
        // Main FBO
        mainFBO.bind();
        Shader.uniformAllMatrix4f(Shader.CAMERA, scene.camera.transform, Shader.COLLECTION_USE_CAMERA_TRANSFORM );
        Shader.uniformAllMatrix4f(Shader.PERSPECTIVE, scene.camera.perspective, Shader.COLLECTION_USE_CAMERA_TRANSFORM);
        glDisable(GL30.GL_CLIP_DISTANCE0);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glEnable(GL_DEPTH_TEST);
        
        if(Scene.activeScene.camera.pos.y >= waterPlane.getWaterLevel()){
            atmosphere.loadUniforms();
            Shader.enableCausticTexture = false;
        }
        else{
            Shader.enableCausticTexture = true;
        }
        
        
        
        // Render the sky
        atmosphere.render();
        
        // Render all instances
        for(InstanceRender temp:instanceRenders){
            temp.render();
        }
        waterPlane.draw();
        
        
        
        FBO.bindDefault();
        mainFBO.draw(0);
        
    }
    
    /**
     * Forcibly removes instances that are deleted.
     * This function should only be called in certain scenarios such as loading a new setting.
     * Instance Renders are already self-cleaning.
     */
    public void cleanInstanceRenders(){
        for(InstanceRender temp:instanceRenders){
            temp.cleanInstances();
        }
    }

    
    
}
