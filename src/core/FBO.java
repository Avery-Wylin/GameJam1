package core;
import static org.lwjgl.opengl.GL30.*;
import java.util.ArrayList;
import core.scene.Scene;
import java.nio.IntBuffer;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import static core.Shader.COLLECTION_USE_CAMERA_TRANSFORM;

public class FBO {
    private static ArrayList<FBO> loadedFBOs = new ArrayList<>();
    private static int windowWidth=0, windowHeight=0;
    private static Mesh windowMesh = new Mesh(new int[]{0,1,2,2,3,0}, new float[]{-1,-1,0, 1,-1,0, 1,1,0, -1,1,0 }, new float[]{0,0,1,0,1,1,0,1},null,null);
    private static int[] colorAttachments = new int[]{
        GL_COLOR_ATTACHMENT0,
        GL_COLOR_ATTACHMENT1,
        GL_COLOR_ATTACHMENT2,
        GL_COLOR_ATTACHMENT3,
        GL_COLOR_ATTACHMENT4,
        GL_COLOR_ATTACHMENT5,
        GL_COLOR_ATTACHMENT6,
        GL_COLOR_ATTACHMENT7,
    };
    public static final int MAX_COLOR_ATTACHMENTS = colorAttachments.length;
    
    private Shader shader;
    public int width=512,height=512;
    int FBOID = 0;
    private int[] colorAttachmentIDs = new int[MAX_COLOR_ATTACHMENTS];
    int[] colorTextureIDs = new int[MAX_COLOR_ATTACHMENTS];
    int depthAttachmentID = 0;
    int depthTextureID = 0;
    
    public FBO(int w, int h){
        this.width=w;
        this.height=h;
        setShader(defaultShader);
        for (int i=0; i<colorAttachmentIDs.length; i++){
            colorAttachmentIDs[i] = 0;
            colorTextureIDs[i] = 0;
        }
    }
    
    public void allocate(){
        
        // Create default depth buffer
        FBOID = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER,FBOID);
        
        // Draw to default color attachment
        glDrawBuffer(GL_COLOR_ATTACHMENT0);
        
        // Create the depth attachment
        glReadBuffer(GL_DEPTH_ATTACHMENT);
    }
    
    public void bind(){
        
        //bind the FBO, all rendering afterwards will be done to this FBO
        glBindFramebuffer(GL_FRAMEBUFFER, FBOID);
        
        //change viewport size to that of FBO
        glViewport(0, 0, width, height);
        
    }
    
    public void setDrawSlot(int slot){
        slot = Math.max(Math.min(slot, MAX_COLOR_ATTACHMENTS), 0);
        glDrawBuffer(colorAttachments[slot]);
    }
    
    public void unbind(){
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0,0,windowWidth,windowHeight);
    }
    
    public static void bindDefault(){
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0,0,windowWidth,windowHeight);
    }
    
    public static void setWindowDimensions(int w, int h){
        windowWidth = w;
        windowHeight = h;
    }
    
    public void createColourTexture(int slot, int scaleFilter){
        if(colorTextureIDs[slot] == 0)
            colorTextureIDs[slot] = TextureManager.createColorFBO(this, slot, width, height, scaleFilter);
    }
    
    public void createDepthTexture(){
        depthTextureID = TextureManager.createDepthFBO(this, width, height);
    }
    
    public int createDepthAttachment(){
        depthAttachmentID = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, depthAttachmentID);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, width, height);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthAttachmentID);
        return depthAttachmentID;
    }
    
    public int createColorAttachment(int slot){
        slot = Math.max(Math.min(slot, MAX_COLOR_ATTACHMENTS), 0);
        if(colorAttachmentIDs[slot] == 0 ){
            colorAttachmentIDs[slot] = glGenRenderbuffers();
            glBindRenderbuffer(GL_RENDERBUFFER, colorAttachmentIDs[slot]);
            glRenderbufferStorage(GL_RENDERBUFFER, GL_RGBA8, width, height);
            glFramebufferRenderbuffer(GL_FRAMEBUFFER, colorAttachments[slot], GL_RENDERBUFFER, colorAttachmentIDs[slot]);
            updateDrawableBuffers();
        }
        
        return colorAttachments[slot];
    }
    
    public int getAttachmentID(int slot){
        slot = Math.max(Math.min(slot, MAX_COLOR_ATTACHMENTS), 0);
        return colorAttachments[slot];
    }
    
    public int getTexture(int slot){
        slot = Math.max(Math.min(slot, MAX_COLOR_ATTACHMENTS), 0);
        return colorTextureIDs[slot];
    }
    
    public int getDepthTexture(){
        return depthTextureID;
    }
    
    private void updateDrawableBuffers(){
        IntBuffer attachments = BufferUtils.createIntBuffer(MAX_COLOR_ATTACHMENTS);
        for(int i=0; i<colorAttachmentIDs.length; i++){
            if(colorAttachmentIDs[i] != 0){
                attachments.put(colorAttachments[i]);
            }
        }
        attachments.flip();
        glDrawBuffers(attachments);
    }
    
    public int getID(){
        return FBOID;
    }
    
    public void free(){
        
         
        // Delete attachments
        glDeleteRenderbuffers(depthAttachmentID);
        
        for(int i = 0; i<colorAttachmentIDs.length; i++){
            if(colorAttachmentIDs[i] != 0){
                glDeleteRenderbuffers(colorAttachmentIDs[i]);
                colorAttachmentIDs[i] = 0;
            }
        }
        glDeleteRenderbuffers(depthAttachmentID);
        
        // Delete any used textures
        for(int i = 0; i<colorTextureIDs.length; i++){
            if(colorTextureIDs[i] != 0){
                 TextureManager.free(colorTextureIDs[i]);
                colorTextureIDs[i] = 0;
            }
        }
        if(depthTextureID != 0)
             TextureManager.free(depthTextureID);
        
        // Delete the framebuffer
        glDeleteFramebuffers(FBOID);
        
        // Remove this FBO from the loaded list
        loadedFBOs.remove(this);
    }
    
    public static void freeAll(){
        for(int i=loadedFBOs.size()-1;i>=0;i--){
            FBO temp = loadedFBOs.get(i);
            temp.free();
        }
    }
    
    public void setShader(Shader shader){
        this.shader = shader;
    }
    
    public void draw(int slot){
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
        glBindFramebuffer(GL_READ_FRAMEBUFFER, this.FBOID);
        glReadBuffer(colorAttachments[slot]);
        glBlitFramebuffer(0, 0, width, height, 0, 0, windowWidth, windowHeight, GL_COLOR_BUFFER_BIT, GL_NEAREST);
    }
    
    public void draw(int slot,FBO destination){
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, destination.FBOID);
        glBindFramebuffer(GL_READ_FRAMEBUFFER, this.FBOID);
        glReadBuffer(colorAttachments[slot]);
        glBlitFramebuffer(0, 0, width, height, 0, 0, destination.width, destination.height, GL_COLOR_BUFFER_BIT, GL_LINEAR);
    }
    
    public void drawUsingShader(){
        
        // Bind the textures to the correct slots
        for(int i=0; i<colorTextureIDs.length; i++){
            if(colorTextureIDs[i] != 0){
                glActiveTexture(GL_TEXTURE1+i);
                glBindTexture(GL_TEXTURE_2D, colorTextureIDs[i]);
            }
        }
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, depthTextureID);
        
        // Bind the filter shader
        shader.bind();
        
        // Bind the window mesh (takes up the whole screen)
        windowMesh.bind();
        
        // Draw the mesh in the whatever active FBO is used
        glDrawElements(GL_TRIANGLES, windowMesh.getIndexCount(), GL_UNSIGNED_INT, 0);
    }
    
    // Default FBO shader
   public static final Shader defaultShader = new Shader(
           "defaultFBO",
           "defaultFBO",
            new String[]{"pos", "uv","", ""},
            new String[]{"depth", "color_0", "color_1", "color_2", "color_3", "color_4", "color_5", "color_6", "color_7", "", "", ""},
            COLLECTION_USE_CAMERA_TRANSFORM
    ) {
        @Override
        public void loadUniformSettings() {
        }

        
    };
   
   // Default FBO shader
   public static final Shader fboShader = new Shader(
           "effectFBO",
           "effectFBO",
           new String[]{"pos", "uv","", ""},
           new String[]{},
           COLLECTION_USE_CAMERA_TRANSFORM
    ) {
        @Override
        public void loadUniformSettings() {
        }

        
    };
   
}
