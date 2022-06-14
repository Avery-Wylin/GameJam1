/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import core.Mesh;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDrawElements;

/**
 *
 * @author ajw
 */
public class AtmosphereSettings {
    
    private static Mesh skybox = new Mesh("sky");
    public static final float RADIUS = 100;
    private static Matrix4f skyTransform = new Matrix4f().scale(RADIUS);
    private static Shader skyshader = new Shader(
        "sky",
        "sky",
        new String[]{"pos","", "normal", ""},
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
            },
        Shader.COLLECTION_USE_CAMERA_TRANSFORM | Shader.COLLECTION_USE_ATMOSPHERE
    ){
        @Override
        public void loadUniformSettings() {
        }
        
    };
    
    
    public Vector3f
    up,
    down,
    direction;
    public float
    fog_density,
    fog_exponent;
    
    public AtmosphereSettings(){
        up = new Vector3f(1f,1f, 1f);
        down = new Vector3f(0f, 0f,0f);
        direction = new Vector3f( 0f, 1f, 0f);
        fog_density = .02f;
        fog_exponent = 2f;
    }

    public void loadUniforms() {
            Shader.uniformAllVector3f(Shader.SKY_UP, up, Shader.COLLECTION_USE_ATMOSPHERE);
            Shader.uniformAllVector3f(Shader.SKY_DOWN, down, Shader.COLLECTION_USE_ATMOSPHERE);
            Shader.uniformAllVector3f(Shader.SKY_VEC, direction, Shader.COLLECTION_USE_ATMOSPHERE);
            Shader.uniformAllFloat(Shader.FOG_DENSITY, fog_density, Shader.COLLECTION_USE_ATMOSPHERE);
            Shader.uniformAllFloat(Shader.FOG_EXPONENT, fog_exponent, Shader.COLLECTION_USE_ATMOSPHERE);
    }
    
    public void render() {
        skyshader.bind();
        skybox.bind();
        skyshader.uniformMatrix4f(Shader.TRANSFORM, skyTransform);
        glDrawElements(GL_TRIANGLES, skybox.getIndexCount(), GL_UNSIGNED_INT, 0);
    }

}
