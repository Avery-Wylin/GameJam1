package core.scene;

import core.AtmosphereSettings;
import core.FBO;
import core.Mesh;
import core.Shader;
import core.instances.Instance;
import core.instances.Renderable;
import core.instances.InstanceRender;
import java.util.HashMap;
import java.util.LinkedList;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glEnable;
import org.lwjgl.opengl.GL15;

/**
 *
 * @author ajw
 */
public class SceneRender implements Renderable{
    
    private Scene scene;
    public LinkedList<InstanceRender> instanceRenders = new LinkedList<>();
    private HashMap<String,InstanceRender> instanceRenderMap = new HashMap<>();
    public AtmosphereSettings atmosphere;
    private FBO mainFBO, effectFBO;
    
    public SceneRender(Scene scene){
        this.scene = scene;
        atmosphere = new AtmosphereSettings();
        
        mainFBO = new FBO(1024,1024);
        mainFBO.allocate();
        mainFBO.createDepthAttachment();
        mainFBO.createDepthTexture();
        mainFBO.createColourTexture(GL11.GL_LINEAR);
        
        effectFBO = new FBO(1024,1024);
        effectFBO.allocate();
        effectFBO.createDepthAttachment();
        effectFBO.createDepthTexture();
        effectFBO.createColourTexture(GL11.GL_NEAREST);
        effectFBO.setShader(FBO.outlineShader);
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
    
    @Override
    public void render() {
        
        // Main FBO
        mainFBO.bind();
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glEnable(GL_DEPTH_TEST);
        
        // Set the Camera
        atmosphere.loadUniforms();
        Shader.uniformAllMatrix4f(Shader.CAMERA, scene.camera.transform, Shader.COLLECTION_3D );
        Shader.uniformAllMatrix4f(Shader.PERSPECTIVE, scene.camera.perspective, Shader.COLLECTION_3D | Shader.COLLECTION_TEXT);
        GL15.glBindBuffer(GL_DEPTH_TEST, GL_TEXTURE_2D);
        
        // Render the sky
        atmosphere.render();
        
        // Render all instances
        for(InstanceRender temp:instanceRenders){
            temp.render();
        }
        
        // Effect FBO
        //effectFBO.bind();
        //glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        //mainFBO.draw();
        
        // Screen FBO
        FBO.bindDefault();
        mainFBO.draw();
        
    }
    
    /**
     * Removes instances that are no longer being rendered.
     */
    public void cleanInstanceRenders(){
        for(InstanceRender temp:instanceRenders){
            temp.cleanInstances();
        }
    }

    
    
}
