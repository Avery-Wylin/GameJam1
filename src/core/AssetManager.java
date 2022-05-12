package core;

import core.animation.Armature;
import core.animation.ArmatureInfo;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCCapabilities;

public class AssetManager {
    
    private static HashMap<String,Integer> textures = new HashMap<>();
    private static HashMap<String,Mesh> meshes = new HashMap<>();
    private static HashMap<String,Integer> soundSources = new HashMap<>();
    private static HashMap<String,Integer> soundBuffers = new HashMap<>();
    private static HashMap<String,ArmatureInfo> armatures = new HashMap<>();
    
    public static int getTexture(String texture){
        return textures.getOrDefault(texture,0);
    }
    
    public static void addTexture(String name, int textureID){
        textures.put(name, textureID);
    }
    
    public static Mesh getMesh(String mesh){
        return meshes.get(mesh);
    }
    
    public static void addMesh(String name, Mesh mesh){
        meshes.put(name, mesh);
    }
    
    public static int getSoundSource(String source){
        return soundSources.getOrDefault(source, 0);
    }
    
    public static int getSound(String sound){
        return soundBuffers.getOrDefault(sound, 0);
    }
    
    public static void addArmature(String name, ArmatureInfo armature){
        armatures.put(name, armature);
    }
    
    public static ArmatureInfo getArmature(String name){
        return armatures.get(name);
    }
    
    public static void free(){
        Mesh.freeAll();
        TextureManager.freeAll();
        FBO.freeAll();
        SoundBuffer.freeAll();
        SoundSource.freeAll();
        TextMesh.freeAll();
        Shader.freeAll();
    }
    
    public static void createALCapabilities(){
        long device = ALC10.alcOpenDevice((ByteBuffer) null);
        ALCCapabilities capabilities = ALC.createCapabilities(device);
        long context = ALC10.alcCreateContext(device, (IntBuffer) null);
        ALC10.alcMakeContextCurrent(context);
        AL.createCapabilities(capabilities);
    }
    
    public static void loadResources(){
        AssimpLoader.loadFromFBX("tetrapod");
        TextureManager.createTexture("sand");
    }
    
    
   
    
}
