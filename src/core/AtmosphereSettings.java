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
        new String[]{"pos","", "normal", ""},
        new String[]{"transform", "camera", "perspective", "", "", "world_exponent", "", "zenith", "horizon", "albedo", "fog_density", "fog_exponent"},
        Shader.COLLECTION_3D
    ){
        @Override
        public void loadUniformSettings() {
        }
        
    };
    
    
    private boolean updated = true;
    public Vector3f
    zenith,
    horizon,
    albedo;
    public float
    fog_density,
    fog_exponent,
    world_exponent;
    
    public AtmosphereSettings(){
        zenith = new Vector3f(.85f,.9f, 1f);
        horizon = new Vector3f(.65f,.85f,.9f);
        albedo = new Vector3f(.2f,.3f,.7f);
        fog_density = .1f;
        fog_exponent = 1f;
        world_exponent = .5f;
        updated = true;
    }

    public void loadUniforms() {
        if(updated){
            Shader.uniformAllVector3f(Shader.ZENITH, zenith, Shader.COLLECTION_3D);
            Shader.uniformAllVector3f(Shader.HORIZON, horizon, Shader.COLLECTION_3D);
            Shader.uniformAllVector3f(Shader.ALBEDO, albedo, Shader.COLLECTION_3D);
            Shader.uniformAllFloat(Shader.FOG_DENSITY, fog_density, Shader.COLLECTION_3D);
            Shader.uniformAllFloat(Shader.FOG_EXPONENT, fog_exponent, Shader.COLLECTION_3D);
            Shader.uniformAllFloat(Shader.WORLD_EXPONENT, world_exponent, Shader.COLLECTION_3D);
            updated = false;
        }
    }
    
    public void render() {
        skyshader.bind();
        skybox.bind();
        skyshader.uniformMatrix4f(Shader.TRANSFORM, skyTransform);
        glDrawElements(GL_TRIANGLES, skybox.getIndexCount(), GL_UNSIGNED_INT, 0);
    }

    public void update() {
        updated = true;
    }
}
