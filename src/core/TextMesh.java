package core;

import core.scene.Scene;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import static org.lwjgl.opengl.GL30.*;

public class TextMesh {
    private static ArrayList<TextMesh> loadedText = new ArrayList<>();
    private static HashMap<String,FontInfo> loadedFonts = new HashMap<>();
    
    private static Shader shader;
    private Vector3f color = new Vector3f();
    private Matrix4f transform =  new Matrix4f().translation(0,0,0);
    private Matrix4f temp = new Matrix4f();
    public boolean useCamera = false;
    private static final int
            ATTRB_POS = 0,
            ATTRB_UV = 1,
            ATTRIBUTE_COUNT = 2;
    
    
    private String text;
    private int
    VAO = 0,
    posVBO = 0,
    uvVBO = 0,
    vertCount = 0,
    fontTextureID = 0;
    FontInfo font;
    
    public TextMesh(String text){
        this.text = text;
        font = loadedFonts.get("default");
        allocate();
        genText();
        fontTextureID = font.textureID;
    }
    
    public void allocate(){
        if(VAO == 0 ){
            loadedText.add(this);
            VAO = glGenVertexArrays();
        }
        glBindVertexArray(VAO);
        if(posVBO == 0){
            posVBO = glGenBuffers();
        }
        if(uvVBO == 0){
            uvVBO = glGenBuffers();
        }
    }
    
    public void free(){
        if(VAO != 0){
            glBindVertexArray(VAO);
            
            if(posVBO != 0){
                glDeleteBuffers(posVBO);
                posVBO = 0;
            }
            if(uvVBO != 0){
                glDeleteBuffers(uvVBO);
                uvVBO = 0;
            }
            
            glDeleteVertexArrays(VAO);
            VAO = 0;
            loadedText.remove(this);
        }
    }
    
    public static void freeAll(){
        for(int i=loadedText.size()-1; i>=0; i--){
            loadedText.get(i).free();
        }
    }
    
    public void setText(String newText){
        text = newText;
    }
    
    public void setColor(Vector3f color){
        this.color.set(color);
    }
    
    public void genText(){
        float[] pos = new float[text.length()*8];
        float[] uv = new float[text.length()*8];
        
        int posIndex = 0;
        int uvIndex = 0;
        int xoffset = 0;
        int yoffset = 0;
        char c;
        
        GlyphInfo glyph;
        for(int i=0; i<text.length(); i++){
            c = text.charAt(i);
            if(c=='\n'){
                xoffset = 0;
                yoffset +=font.lineHeight;
                continue;
            }
            glyph = font.glyphs.get(c);
            
            //tl
            pos[posIndex++] = (glyph.xoffset+xoffset)/font.resolution;
            pos[posIndex++] = (-glyph.yoffset-yoffset)/font.resolution;
            
            uv[uvIndex++] = (glyph.x)/font.resolution;
            uv[uvIndex++] = (1-glyph.y)/font.resolution;
            
            //bl
            pos[posIndex++] = (glyph.xoffset+xoffset)/font.resolution;
            pos[posIndex++] = (-glyph.yoffset-glyph.height-yoffset)/font.resolution;
            
            uv[uvIndex++] = (glyph.x)/font.resolution;
            uv[uvIndex++] = (1-glyph.y-glyph.height)/font.resolution;
            
            //br
            pos[posIndex++] = (glyph.xoffset+glyph.width+xoffset)/font.resolution;
            pos[posIndex++] = (-glyph.yoffset-glyph.height-yoffset)/font.resolution;
            
            uv[uvIndex++] = (glyph.x+glyph.width)/font.resolution;
            uv[uvIndex++] = (1-glyph.y-glyph.height)/font.resolution;
            
            //tr
            pos[posIndex++] = (glyph.xoffset+glyph.width+xoffset)/font.resolution;
            pos[posIndex++] = (-glyph.yoffset-yoffset)/font.resolution;
            
            uv[uvIndex++] = (glyph.x+glyph.width)/font.resolution;
            uv[uvIndex++] = (1-glyph.y)/font.resolution;
            
            xoffset += glyph.xadvance;
        }
        
        vertCount = pos.length/2;
        loadAttributePos(pos);
        loadAttributeUV(uv);
    }
    
    public void render() {
        if( VAO != 0){
            glBindTexture(GL_TEXTURE_2D, fontTextureID);
            shader.bind();
            shader.uniformMatrix4f(Shader.TRANSFORM, temp.set(Scene.camera.perspective).mul(Scene.camera.transform).mul(transform));
            glBindVertexArray(VAO);
            glEnableVertexAttribArray(ATTRB_POS);
            glEnableVertexAttribArray(ATTRB_UV);
            glDrawArrays(GL_QUADS, 0, vertCount);
        }
    }
    
    public void loadAttributePos(float[] attribute) {
            glBindVertexArray(VAO);
            if (posVBO == 0) {
                posVBO = glGenBuffers();
            }
            glBindBuffer(GL_ARRAY_BUFFER, posVBO);
            glBufferData(GL_ARRAY_BUFFER, attribute, GL_STATIC_DRAW);
            glVertexAttribPointer(ATTRB_POS, 2, GL_FLOAT, false, 0, 0);
    }
    
    public void loadAttributeUV(float[] attribute) {
            glBindVertexArray(VAO);
            if (uvVBO == 0) {
                uvVBO = glGenBuffers();
            }
            glBindBuffer(GL_ARRAY_BUFFER, uvVBO);
            glBufferData(GL_ARRAY_BUFFER, attribute, GL_STATIC_DRAW);
            glVertexAttribPointer(ATTRB_UV, 2, GL_FLOAT, false, 0, 0);
    }
    
    public static void loadFont(String name){
        FontInfo font = new FontInfo();
        font.textureID= TextureManager.createFont(name);
        font.name = name;
        String sep = System.getProperty("file.separator");
        
        try {
            BufferedReader reader = new BufferedReader(new FileReader("assets"+sep+"fonts"+sep+name+".fnt"));
            String line;
            String[] data;
            while ((line = reader.readLine()) != null) {
                data = line.split(" +");
                // If new glyph to read
                if (data[0].equals("char")) {
                    GlyphInfo glyph = new GlyphInfo();
                    //id
                    char id = (char) Integer.parseInt((data[1].split("="))[1]);

                    //x
                    glyph.x = Integer.parseInt((data[2].split("="))[1]);

                    //y
                    glyph.y = Integer.parseInt((data[3].split("="))[1]);

                    //width
                    glyph.width = Integer.parseInt((data[4].split("="))[1]);

                    //height
                    glyph.height = Integer.parseInt((data[5].split("="))[1]);

                    //xoffset
                    glyph.xoffset = Integer.parseInt((data[6].split("="))[1]);

                    //yoffset
                    glyph.yoffset = Integer.parseInt((data[7].split("="))[1]);

                    //xadvance
                    glyph.xadvance = Integer.parseInt((data[8].split("="))[1]);

                    font.glyphs.put(id, glyph);
                }
                else if(data[0].equals("common")){
                    font.lineHeight = Integer.parseInt((data[2].split("="))[1]);
                    font.resolution = Integer.parseInt((data[3].split("="))[1]);
                }

            }
            loadedFonts.put(name, font);
        } catch (FileNotFoundException ex) {
            System.err.println("Font " + name + " was not found");
        } catch (IOException ex) {
            System.err.println("Font " + name + " failed to load");
        }
    }
    
    public static void init() {
        loadFont("default");
        shader = new Shader(
                "text",
                new String[]{"pos", "uv", "", ""},
                new String[]{"transform", "", "perspective", "text_color", "", "", "", "", "", "", "", ""},
                Shader.COLLECTION_TEXT
        ) {
            @Override
            public void loadUniformSettings() {
            }

        };
    }
    
}

class FontInfo{
    String name;
    int textureID;
    float resolution = 256;
    HashMap<Character,GlyphInfo> glyphs;
    int lineHeight = 0;
    
    FontInfo(){
        glyphs = new HashMap<>();
    }
    
}

class GlyphInfo{
    int x;
    int y;
    int width;
    int height;
    int xoffset;
    int yoffset;
    int xadvance;
}
