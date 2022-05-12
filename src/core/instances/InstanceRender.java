package core.instances;

import core.AssetManager;
import core.Mesh;
import core.Shader;
import java.util.ArrayList;
import static org.lwjgl.opengl.GL11.*;

public class InstanceRender implements Renderable{
    private Mesh mesh;
    Shader shader = Shader.defaultShader;
    private ArrayList<Instance> instances;
    public boolean backfaceCulling = true;
    private int textureID = 0;
    
    public InstanceRender(){
        instances = new ArrayList<Instance>();
    }
    
    @Override
    public void render() {
        shader.bind();
        mesh.bind();
        shader.loadUniformSettings();
        if(backfaceCulling){
            glCullFace(GL_BACK);
            glEnable(GL_CULL_FACE);
        }
        else
            glDisable(GL_CULL_FACE);
        
        if(textureID != 0){
            glEnable(GL_TEXTURE_2D);
            glBindTexture(GL_TEXTURE_2D, textureID);
        }
        else
            glDisable(GL_TEXTURE_2D);
        
        for(Instance instance:instances){
            if(instance.visible){
                shader.uniformMatrix4f(Shader.TRANSFORM, instance.getTransform());
                if(instance.hasArmature()){
                        shader.uniformMatrix4fArray(Shader.JOINTS, instance.getArmature().getJointTransforms());
                    glDrawElements(GL_TRIANGLES, mesh.getIndexCount(),GL_UNSIGNED_INT,0);
                }
                else{
                    glDrawElements(GL_TRIANGLES, mesh.getIndexCount(),GL_UNSIGNED_INT,0);
                }
            }
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
    
    public void cleanInstances(){
        for(int i = instances.size()-1; i>=0; i--){
            if(instances.get(i).isDeleted()){
               instances.remove(i);
            }
        }
    }
    
}
