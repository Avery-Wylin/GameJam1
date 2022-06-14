package core.instances;

import core.AssetManager;
import core.Mesh;
import core.Shader;
import java.util.ArrayList;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.opengl.GL13;
import static org.lwjgl.opengl.GL13.*;

public class InstanceRender {
    private Mesh mesh;
    Shader shader = Shader.defaultShader;
    private ArrayList<Instance> instances;
    public boolean backfaceCulling = true;
    private int textureID = 0;
    private int causticTextureID = 0;
    private int deletedCount = 0;
    
    public InstanceRender(){
        instances = new ArrayList<Instance>();
    }
    
    public void render() {
        deletedCount = 0;
        shader.bind();
        shader.bindTextures();
        mesh.bind();
        shader.loadUniformSettings();
        if(backfaceCulling){
            glCullFace(GL_BACK);
            glEnable(GL_CULL_FACE);
        }
        else
            glDisable(GL_CULL_FACE);
        
        glBindTexture(GL_TEXTURE_2D, textureID);
        
        float[] transformBuffer = new float[16];
        
        for(Instance instance:instances){
            if(instance.isVisible()){
                shader.uniformMatrix4f(Shader.TRANSFORM, instance.getGLTransform(transformBuffer));
                shader.uniformVector3f(Shader.DIFFUSE, instance.getColor());
                if(instance.hasArmature()){
                    shader.uniformMatrix4fArray(Shader.JOINTS, instance.getArmature().getJointTransforms());
                    glDrawElements(GL_TRIANGLES, mesh.getIndexCount(),GL_UNSIGNED_INT,0);
                }
                else{
                    glDrawElements(GL_TRIANGLES, mesh.getIndexCount(),GL_UNSIGNED_INT,0);
                }
            }
            else{
                deletedCount++;
            }
        }
        
        /* 
        Automatically clean the render list whenever most instances are deleted
        This is more efficient than having to move all elements for each removal in an ArrayList
        The threshold is set to 50% by default, but can be changed to any suitable number.
        */
        if((float)deletedCount/(instances.size()+1) > .5f){
            cleanInstances();
        }
        
    }
    
    public void setMesh(Mesh mesh){
        this.mesh = mesh;
    }
    
    public void setShader(Shader shader){
        this.shader = shader;
    }
    
    public void setTexture(String name){
        textureID = AssetManager.getTexture(name);
    }
    
    
    public void disableTexture(){
        textureID = 0;
    }
    
    public void addInstance( Instance instance){
        instances.add(instance);
    }
    
    public void removeInstance( Instance instance){
        instances.remove(instance);
    }
    
    public void cleanInstances(){
        for(int i = instances.size()-1; i>=0; i--){
            if(instances.get(i).isDeleted()){
               instances.remove(i);
            }
        }
    }
    
}
