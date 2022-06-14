package core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import static org.lwjgl.opengl.GL20.*;

public abstract class Shader {
    
    // Static
    private static final ArrayList<Shader> loadedShaders = new ArrayList<>();
    public static final int MAX_UNIFORM_COUNT = 16;
    public static final int MAX_JOINT_COUNT = 64;
    public static Shader defaultShader, animatedShader;
    public static boolean enableCausticTexture = false;
    
    // Common uniforms
    public static final int
            TRANSFORM = 0,      FBO_DEPTH = 0,
            CAMERA = 1,         FBO_COLOR_0 = 1,
            PERSPECTIVE = 2,    FBO_COLOR_1 = 2,
            DIFFUSE = 3,        FBO_COLOR_2 = 3,    TEXT_COLOR = 3,     
            SPECULAR = 4,       FBO_COLOR_3 = 4,
            FOG_DENSITY = 5,    FBO_COLOR_4 = 5,
            FOG_EXPONENT = 6,   FBO_COLOR_5 = 6,
            SKY_UP = 7,         FBO_COLOR_6 = 7,
            SKY_DOWN = 8,       FBO_COLOR_7 = 8,
            SKY_VEC = 9,
            JOINTS = 10,
            CLIPPING_PLANE = 11,
            TIME = 12,
            
            /*Collections allow for loading a single uniform to an entire collection of shaders
              A shader may belong to many collections
              A shader belonging to 0 will never be used when loading a uniform to many.
              Some flags are mutually exclusive (FBO would overlap with camera and atmosphere)
            */
            
            COLLECTION_UNIQUE = 0,                  // Does not recieve any universal uniforms
            COLLECTION_USE_CAMERA_TRANSFORM = 1,    // Load camera perspective and location
            COLLECTION_FBO = 2,                     // Uses depth and all 8 FBO color attachments
            COLLECTION_TEXT = 4,                    // Uses Text Settings (such as color)
            COLLECTION_USE_ATMOSPHERE = 8,          // Uses Fog Color and World Settings 6-11
            COLLECTION_USE_CLIPPING = 16,           // Uses the global water level 
            COLLECTION_USE_TIME = 32,
    
            CAUSTIC_TEXTURE_SLOT = GL_TEXTURE1,
            CAUSTIC_TEXTURE_ID = AssetManager.getTexture("wave");
            
    
    // Shader Properties
    private int programID;
    private int vertexShaderID;
    private int fragmentShaderID;
    private int collection = 0;
    private String vertName = "";
    private String fragName = "";
    private String[] uniformNames;
    private String[] attributeNames;
    public boolean useCausticTexture = false;
    
    // Uniform List, Stores the value of the found uniform
    private int uniform[] = new int[MAX_UNIFORM_COUNT];
    
    public Shader(){
        programID = 0;
        vertexShaderID = 0;
        fragmentShaderID = 0;
        collection = 0;
    }
    
    public Shader(String vert, String frag, String[] attributes, String[] uniforms, int collection ){
        bind();
        this.collection = collection;
        // Load the shader from client files
        uniformNames = uniforms;
        attributeNames = attributes;
        vertexShaderID = glCreateShader(GL_VERTEX_SHADER);
        fragmentShaderID = glCreateShader(GL_FRAGMENT_SHADER);
        vertName = vert;
        fragName = frag;
        String sep = System.getProperty("file.separator");
        loadFromFile("assets"+sep+"shaders"+sep+"vert"+sep+""+vertName+".vert", vertexShaderID);
        loadFromFile("assets"+sep+"shaders"+sep+"frag"+sep+""+fragName+".frag",fragmentShaderID);
        
        // Create the shader program
        programID = glCreateProgram();
        glAttachShader(programID, vertexShaderID);
        glAttachShader(programID, fragmentShaderID);
        
        // Bind mesh attributes to the shader
        bindAttributes(attributes);
        
        // Link the Program together 
        glLinkProgram(programID);
        
        // Get all the uniforms used in the shader
        getUniformLocations(uniforms);
        
        // Validates the Program
        glValidateProgram(programID);
        loadedShaders.add(this);
        
    }
    
    /**
     * Binds VAO attributes to their listed shader names placed in the correct attribute index.
     * Use empty string to ignore the attribute in that index.
     * @param attributeNames List of attribute names used in the shader.
     */
    private void bindAttributes(String attributeNames[]){
        for( int i=0; i < attributeNames.length; i++ ){
            if(!attributeNames[i].isEmpty()){
                glBindAttribLocation(programID, i, attributeNames[i]);
            }
        }
    }
    
    /**
     * Loads the uniform locations by name to their respective index.
     * The index of each name corresponds to the uniformID. 
     * @param uniformNames List of custom uniform names used in the shader.
     */
    public void getUniformLocations(String uniformNames[]){
        if(uniformNames.length > MAX_UNIFORM_COUNT)
            System.err.print("Shader "+ programID + " has "+ uniformNames.length +" uniforms, which is greater than the allocated " + MAX_UNIFORM_COUNT);
        for(int i=0; i< Math.min(MAX_UNIFORM_COUNT, uniformNames.length); i++){
            if(!uniformNames[i].isEmpty()){
                uniform[i] = glGetUniformLocation(programID, uniformNames[i]);
            }
        }
    }
    
    public void uniformMatrix4f(int uniformID, Matrix4f matrix){
        float[] buffer = new float[16];
        matrix.get(buffer);
        glUniformMatrix4fv(uniform[uniformID], false, buffer);
    }
    
    public void uniformMatrix4f(int uniformID, float[] matrix){
        glUniformMatrix4fv(uniform[uniformID], false, matrix);
    }
    
    public void uniformMatrix4fArray(int uniformID, float[] matrices) {
        glUniformMatrix4fv(uniform[uniformID], false, matrices);
        return;
    }
    
    public static void uniformAllMatrix4f(int uniformID, Matrix4f matrix, int collectionMask){
        for(Shader shader:loadedShaders){
            if((shader.collection & collectionMask) == collectionMask){
                shader.bind();
                float[] buffer = new float[16];
                matrix.get(buffer);
                glUniformMatrix4fv(shader.uniform[uniformID], false, buffer);
            }
        }
        glUseProgram(0);
    }
    
    public void uniformVector4f(int uniformID, Vector4f vector){
        glUniform4f(uniform[uniformID], vector.x, vector.y, vector.z, vector.w);
    }
    
    public static void uniformAllVector4f(int uniformID, Vector4f vector, int collectionMask) {
        for (Shader shader : loadedShaders) {
            if((shader.collection & collectionMask) == collectionMask){
                shader.bind();
                glUniform4f(shader.uniform[uniformID], vector.x, vector.y, vector.z, vector.w);
            }
        }
        glUseProgram(0);
    }
    
    public void uniformVector3f(int uniformID, Vector3f vector){
        glUniform3f(uniform[uniformID], vector.x, vector.y, vector.z);
    }
    
    public static void uniformAllVector3f(int uniformID, Vector3f vector, int collectionMask) {
        for (Shader shader : loadedShaders) {
            if((shader.collection & collectionMask) == collectionMask){
                shader.bind();
                glUniform3f(shader.uniform[uniformID], vector.x, vector.y, vector.z);
            }
        }
        glUseProgram(0);
    }
    
    public void uniformFloat(int uniformID, float val){
        glUniform1f(uniform[uniformID], val);
    }
    
    public static void uniformAllFloat(int uniformID, float val, int collectionMask) {
        for (Shader shader : loadedShaders) {
            if((shader.collection & collectionMask) == collectionMask) {
                shader.bind();
                glUniform1f(shader.uniform[uniformID], val);
            }
        }
        glUseProgram(0);
    }
    
    public void uniformInt(int uniformID, int val){
        glUniform1i(uniform[uniformID], val);
    }
    
     public static void uniformAllInt(int uniformID, int val, int collectionMask) {
        for (Shader shader : loadedShaders) {
            if((shader.collection & collectionMask) == collectionMask){
                shader.bind();
                glUniform1i(shader.uniform[uniformID], val);
            }
        }
        glUseProgram(0);
    }
     
    public int getCollection(){
        return collection;
    }
    
    public void setCollection(int collection){
        this.collection = collection;
    }
     
    public abstract void loadUniformSettings();
    
    public void bind(){
        glUseProgram(programID);
    }
    
    public void bindTextures(){
        
        if(useCausticTexture){
                glActiveTexture(CAUSTIC_TEXTURE_SLOT);
            if(enableCausticTexture){
                glBindTexture(GL_TEXTURE_2D, CAUSTIC_TEXTURE_ID);
            }
            else{
                glBindTexture(GL_TEXTURE_2D, 0);
            }
            glActiveTexture(GL_TEXTURE0);
        }
    }
    
    public void unbind(){
        glUseProgram(0);
    }
    
    public void free(){
        unbind();
        loadedShaders.remove(this);
        glDetachShader(programID, vertexShaderID);
        glDetachShader(programID, fragmentShaderID);
        glDeleteShader(vertexShaderID);
        glDeleteShader(fragmentShaderID);
        glDeleteProgram(programID);
    }
    
    public static void freeAll(){
        for(int i = loadedShaders.size()-1; i>=0; i--){
            loadedShaders.get(i).free();
        }
        loadedShaders.clear();
    }
    
    private int loadFromFile(String file, int shaderID){
        StringBuilder shaderSource = new StringBuilder();
        try{
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while((line=reader.readLine()) != null){
                shaderSource.append(line).append('\n');
            }
            reader.close();
        }
        catch(IOException e){
            System.err.println("Shader "+file+" could not be read or does not exist.");
            return 0;
            //System.exit(-1);
        }
        glShaderSource(shaderID, shaderSource);
        glCompileShader(shaderID);
        if(glGetShaderi(shaderID,GL_COMPILE_STATUS)==GL_FALSE){
            System.err.println("Shader "+file+" could not be compiled.");
            System.err.println(glGetShaderInfoLog(shaderID, 500));
            return 0;
            //System.exit(-1);
        }
        return shaderID;
    }
    
    public void recompile(){
            String sep = System.getProperty("file.separator");
            loadFromFile("assets"+sep+"shaders"+sep+"vert"+sep+""+vertName+".vert", vertexShaderID);
            loadFromFile("assets"+sep+"shaders"+sep+"frag"+sep+""+fragName+".frag",fragmentShaderID);
            
            if(programID == 0){
                programID = glCreateProgram();
            }
            glAttachShader(programID, vertexShaderID);
            glAttachShader(programID, fragmentShaderID);
            bindAttributes(attributeNames);
            glLinkProgram(programID);
            getUniformLocations(uniformNames);
            glValidateProgram(programID);
            loadUniformSettings();
    }
    
    public static void recompileAll(){
        for(Shader shader:loadedShaders){
            shader.recompile();
        }
    }
    
    public static void initDefaultShaders(){
        
     // Default Shader
    defaultShader = new Shader(
                "default",
                "default",
                new String[]{"pos", "uv", "normal", "vertex_color"},
                new String[]{
                "transform",                //0
                "camera",                   //1
                "perspective",              //2
                "diffuse",                  //3
                "specular",                 //4
                "fog_density",              //5
                "fog_exponent",             //6
                "sky_up",                   //7
                "sky_down",                 //8
                "sky_vec",                  //9
                "",                         //10
                "clipping_plane",           //11
                "time"                      //12
            },
               COLLECTION_USE_CAMERA_TRANSFORM |
               COLLECTION_USE_ATMOSPHERE |
               COLLECTION_USE_CLIPPING |
               COLLECTION_USE_TIME
        )
            
    {
            public Vector3f diffuse = new Vector3f(.8f, .7f, .7f);
            public float specular = 10f,
                    gloss = 3f;

            @Override
            public void loadUniformSettings() {
                uniformVector3f(Shader.DIFFUSE, diffuse);
                uniformFloat(Shader.SPECULAR, specular);
            }

        };
    
    // Animated Default Shader
    animatedShader = new Shader(
                "animated",
                "default",
                new String[]{"pos", "uv", "normal", "vertex_color", "joint", "weight"},
                new String[]{
                "transform",                //0
                "camera",                   //1
                "perspective",              //2
                "diffuse",                  //3
                "specular",                 //4
                "fog_density",              //5
                "fog_exponent",             //6
                "sky_up",                   //7
                "sky_down",                 //8
                "sky_vec",                  //9
                "joints",                   //10
                "clipping_plane",           //11
                "time"                      //12
            },
               COLLECTION_USE_CAMERA_TRANSFORM |
               COLLECTION_USE_ATMOSPHERE |
               COLLECTION_USE_CLIPPING
        ) {
            public Vector3f diffuse = new Vector3f(.7f, .7f, .7f);
            public float specular = 10f,
                    gloss = 3f;

            @Override
            public void loadUniformSettings() {
                uniformVector3f(Shader.DIFFUSE, diffuse);
                uniformFloat(Shader.SPECULAR, specular);
            }

        };
    defaultShader.useCausticTexture = true;
    animatedShader.useCausticTexture = true;
    }
    
}
