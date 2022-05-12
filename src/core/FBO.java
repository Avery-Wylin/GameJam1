package core;
import static org.lwjgl.opengl.GL30.*;
import java.util.ArrayList;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import static core.Shader.COLLECTION_3D;

public class FBO {
    private static ArrayList<FBO> loadedFBOs = new ArrayList<>();
    private static int windowWidth=0, windowHeight=0;
    private Shader shader;
    
    private static Mesh windowMesh = new Mesh(new int[]{0,1,2,2,3,0}, new float[]{-1,-1,0, 1,-1,0, 1,1,0, -1,1,0 }, new float[]{0,0,1,0,1,1,0,1},null,null);
    public int width=512,height=512;
    int colorBufferID = 0;
    int depthBufferID = 0;
    int depthTextureID = 0;
    int colorTextureID = 0;
    
    public FBO(int w, int h){
        this.width=w;
        this.height=h;
        setShader(defaultShader);
    }
    
    public void allocate(){
        colorBufferID = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, colorBufferID);
        glDrawBuffer(GL_COLOR_ATTACHMENT0);
        glReadBuffer(GL_DEPTH_ATTACHMENT);
    }
    
    
    public void bind(){
        //unbind any used textures
        //glBindTexture(GL_TEXTURE_2D, 0);
        //bind the FBO, all rendering afterwards will be done to this FBO
        glBindFramebuffer(GL_FRAMEBUFFER, colorBufferID);
        //change viewport size to that of FBO
        glViewport(0, 0, width, height);
        
    }
    
    public void unbind(){
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }
    
    public static void bindDefault(){
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0,0,windowWidth,windowHeight);
    }
    
    public static void setWindowDimensions(int w, int h){
        windowWidth = w;
        windowHeight = h;
    }
    
    public void createColourTexture(int scaleFilter){
        colorTextureID = TextureManager.createColorFBO(this, width, height, scaleFilter);
    }
    
    public void createDepthTexture(){
        depthTextureID = TextureManager.createDepthFBO(this, width, height);
    }
    
    public int createDepthAttachment(){
        int depthId = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, depthId);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, width, height);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthId);
        return depthId;
    }
    
    public int getId(){
        return colorBufferID;
    }
    
    public void free(){
         glDeleteFramebuffers(colorBufferID);
         glDeleteRenderbuffers(depthBufferID);
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
        shader.uniformInt(Shader.FBO_IMAGE, 0);
        shader.uniformInt(Shader.FBO_DEPTH, 1);
    }
    
    public void draw(){
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, depthTextureID);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, colorTextureID);
        shader.bind();
        windowMesh.bind();
        glDrawElements(GL_TRIANGLES, windowMesh.getIndexCount(), GL_UNSIGNED_INT, 0);
    }
    
    
    
    
    // Default FBO shader
   public static final Shader defaultShader = new Shader(
            "defaultFBO",
            new String[]{"pos", "uv","", ""},
            new String[]{"img_tex", "depth_tex", "", "", "", "", "", "", "", "", "", ""},
            COLLECTION_3D
    ) {
        @Override
        public void loadUniformSettings() {
        }

        
    };
   
   // Default FBO shader
   public static final Shader outlineShader = new Shader(
            "outlineFBO",
            new String[]{"pos", "uv","", ""},
            new String[]{"img_tex", "depth_tex", "", "", "", "", "", "", "", "", "", ""},
            COLLECTION_3D
    ) {
        @Override
        public void loadUniformSettings() {
        }

        
    };
}
