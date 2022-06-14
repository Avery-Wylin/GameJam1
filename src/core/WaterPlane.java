package core;

import core.scene.Scene;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;

public class WaterPlane {
    
    // Water Plane shader
    public static final Shader shader = new Shader(
            "water",
            "water",
            new String[]{"pos", "", "", ""},
            new String[]{
                "transform",                //0
                "camera",                   //1
                "perspective",              //2
                "",                         //3
                "",                         //4
                "fog_density",              //5
                "fog_exponent",             //6
                "sky_up",                   //7
                "sky_down",                 //8
                "sky_vec",                  //9
                "",                         //10
                "",                         //11
                "time"                      //12
            },
            Shader.COLLECTION_USE_CAMERA_TRANSFORM | Shader.COLLECTION_USE_ATMOSPHERE | Shader.COLLECTION_USE_TIME
    ) {
        @Override
        public void loadUniformSettings() {
        }
    };
    
    FBO topFBO, bottomFBO;
    
    private float waterLevel;
    private float time = 0f;
    public int rippleTextureID = 0;
    public int depthTextureID = 0;
    private static Mesh plane;
    public static final float PLANE_SIZE = AtmosphereSettings.RADIUS;
    private Matrix4f transform = new Matrix4f();
    
    static{
        // Create the plane mesh
        plane = new Mesh();
        plane.allocate();
        float[] pos = new float[]{-1,0,-1, 1,0,-1, 1,0,1, -1,0,1};
        int[] index = new int[]{0,2,1, 0,3,2};
        plane.loadIndex(index);
        plane.loadAttributeFloat(Mesh.ATTRB_POS, pos);
        shader.loadUniformSettings();
    }
    
    public WaterPlane(){
        
        
        
        setWaterLevel(1);
        
        // Create the  top and bottom FBO textures
        // Note that only the textures are created, not the attachments.
        topFBO = new FBO(512, 512);
        topFBO.bind();
        topFBO.allocate();
        topFBO.createDepthTexture();
        topFBO.createColourTexture(0, GL_NEAREST);
        
        bottomFBO = new FBO(512, 512);
        bottomFBO.bind();
        bottomFBO.allocate();
        bottomFBO.createDepthTexture();
        bottomFBO.createColourTexture(0, GL_NEAREST);
        
        rippleTextureID = AssetManager.getTexture("wave");
        
    }
    
    public void setTime(float time){
        this.time = time;
    }
    
    public void setWaterLevel(float waterLevel){
        this.waterLevel = waterLevel;
    }
    
    public float getWaterLevel(){
        return waterLevel;
    }
    
    public void bindTop(){
        topFBO.bind();
        topFBO.setDrawSlot(0);
        Shader.uniformAllVector4f(Shader.CLIPPING_PLANE, new Vector4f(0,1,0,-(waterLevel-.05f)), Shader.COLLECTION_USE_CLIPPING);
    }
    
    public void bindBottom(){
        bottomFBO.bind();
        bottomFBO.setDrawSlot(0);
        Shader.uniformAllVector4f(Shader.CLIPPING_PLANE, new Vector4f(0,-1,0,waterLevel+.05f), Shader.COLLECTION_USE_CLIPPING);
    }
    
    public void draw(){
        
        // Bind the shader
        shader.bind();
        
        
        // Bind the Textures
        
        // Camera is above water
        if(Scene.activeScene.camera.pos.y >= waterLevel){
            
            
            // Top is reflected
            glActiveTexture(GL_TEXTURE1);
            glBindTexture(GL_TEXTURE_2D, topFBO.getTexture(0));

            // Bottom is refracted
            glActiveTexture(GL_TEXTURE2);
            glBindTexture(GL_TEXTURE_2D, bottomFBO.getTexture(0));

            // Depth is below
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, bottomFBO.getDepthTexture());
        }
        // Camera is below water
        else{
            
             // Bottom is reflected
            glActiveTexture(GL_TEXTURE1);
            glBindTexture(GL_TEXTURE_2D, bottomFBO.getTexture(0));

            // Top is refracted
            glActiveTexture(GL_TEXTURE2);
            glBindTexture(GL_TEXTURE_2D, topFBO.getTexture(0));

            // Depth is above
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, topFBO.getDepthTexture());
        }
        
        // Ripple
        glActiveTexture(GL_TEXTURE3);
        glBindTexture(GL_TEXTURE_2D, rippleTextureID);
        
        // Set the texture used back to 0
        glActiveTexture(GL_TEXTURE0);
        
        
        // Load uniforms
        transform.translation(Scene.activeScene.camera.pos.x,waterLevel,Scene.activeScene.camera.pos.z).scale(PLANE_SIZE);
        shader.uniformMatrix4f(Shader.TRANSFORM, transform); // Scale the plane
        time +=.001f;
        if(time > 1){
            time = time - (int)Math.ceil(time);
        }
        Shader.uniformAllFloat(Shader.TIME, time, Shader.COLLECTION_USE_TIME); // Time
        shader.bind();
        glDisable(GL_CULL_FACE);
        
        // Bind the plane and draw
        plane.bind();
        glDrawElements(GL_TRIANGLES, plane.getIndexCount(), GL_UNSIGNED_INT, 0);
        
    }
    
    
}
