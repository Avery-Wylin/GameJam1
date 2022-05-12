package core;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import org.lwjgl.opengl.GL30;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.stb.STBImage.*;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.*;


public class TextureManager {
    
    private static ArrayList<Integer> loadedTextures = new ArrayList<>();
    
    /**
     * 
     * @param textureName name of PNG file to load as a texture
     * @return the numeric Texture ID,  can also be fetched by name
     */
    public static int createTexture(String textureName){
        int textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID );
        
        stbi_set_flip_vertically_on_load(true);
    try ( MemoryStack stack = stackPush() ) {
        IntBuffer w = stack.mallocInt(1);
        IntBuffer h = stack.mallocInt(1);
        IntBuffer comp = stack.mallocInt(1);
        String sep = System.getProperty("file.separator");
        ByteBuffer image = stbi_load("assets"+sep+"textures"+sep+textureName+".png", w, h, comp,0);
        if(image == null){
            System.err.println("Failed to load "+textureName+".png" );
            return -1;
        }
        glTexImage2D(GL_TEXTURE_2D,0, GL_RGBA8, w.get(0), h.get(0), 0, GL_RGBA, GL_UNSIGNED_BYTE,image);
    }
        glGenerateMipmap(GL_TEXTURE_2D);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);        
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_LOD_BIAS, .05f);        
        
        AssetManager.addTexture(textureName, textureID);
        loadedTextures.add(textureID);
        return textureID;
    }
    
    public static int createFont(String fontName){
        int textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);

        stbi_set_flip_vertically_on_load(true);
        try ( MemoryStack stack = stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);
            String sep = System.getProperty("file.separator");
            ByteBuffer image = stbi_load("assets" + sep + "fonts" + sep + fontName + ".png", w, h, comp, 0);
            if (image == null) {
                System.err.println("Failed to load " + fontName + ".png");
                return -1;
            }
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, w.get(0), h.get(0), 0, GL_RGBA, GL_UNSIGNED_BYTE, image);
        }
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        loadedTextures.add(textureID);
        AssetManager.addTexture(fontName, textureID);
        return textureID;
    }
    
    public static int createColorFBO(FBO fbo,int w, int h, int scaleFilter){
        fbo.bind();
        int textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);
        glTexImage2D(GL_TEXTURE_2D,0,GL_RGB,w,h,0,GL_RGB,GL_UNSIGNED_BYTE,(ByteBuffer)null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameterIi(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, scaleFilter);
        glTexParameterIi(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, scaleFilter);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,GL_TEXTURE_2D,textureID,0);
        loadedTextures.add(textureID);
        AssetManager.addTexture("FBO Color:"+fbo.getId(), textureID);
        return textureID;
    }
    
    public static int createDepthFBO(FBO fbo,int w, int h){
        fbo.bind();
        int textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);
        glTexImage2D(GL_TEXTURE_2D,0,GL_DEPTH_COMPONENT32,w,h,0,GL_DEPTH_COMPONENT,GL_FLOAT,(ByteBuffer)null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameterIi(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameterIi(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT,GL_TEXTURE_2D,textureID,0);
        loadedTextures.add(textureID);
        AssetManager.addTexture("FBO Depth:"+fbo.getId(), textureID);
        return textureID;
    }
    
    public static void free(int textureID){
        glDeleteTextures(textureID);
        loadedTextures.remove(textureID);
    }
    
    public static void freeAll(){
        for(int textureID:loadedTextures){
            GL30.glDeleteTextures(textureID);
        }
        loadedTextures.clear();
    }
    
}
