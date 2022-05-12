package core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL20;
import static org.lwjgl.opengl.GL20.*;

public abstract class Shader {
    
    // Static
    private static final ArrayList<Shader> loadedShaders = new ArrayList<>();
    public static final int MAX_UNIFORM_COUNT = 16;
    public static final int MAX_JOINT_COUNT = 64;
    public static Shader defaultShader, animatedShader;
    
    // Common uniforms
    public static final int
            TRANSFORM = 0, FBO_IMAGE = 0,
            CAMERA = 1, FBO_DEPTH = 1,
            PERSPECTIVE = 2,
            DIFFUSE = 3, TEXT_COLOR = 3,
            SPECULAR = 4,
            WORLD_EXPONENT = 5,
            GLOSS = 6,
            ZENITH = 7,
            HORIZON = 8,
            ALBEDO = 9,
            FOG_DENSITY = 10,
            FOG_EXPONENT = 11,
            JOINTS = 12,
            
            /*Collections allow for loading a single uniform to an entire collection of shaders
              A shader may belong to many collections
              A shader belonging to 0 will never be used when loading a uniform to many
            */
            
            COLLECTION_UNIQUE = 0,
            COLLECTION_3D = 1,
            COLLECTION_FBO = 2,
            COLLECTION_TEXT = 4;
            
            
    
    // Shader Properties
    private int programID;
    private int vertexShaderID;
    private int fragmentShaderID;
    private int collection = 0;
    private String fileName = "";
    private String[] uniformNames;
    private String[] attributeNames;
    
    // Uniform List, Stores the value of the found uniform
    private int uniform[] = new int[MAX_UNIFORM_COUNT];
    
    public Shader(){
        programID = 0;
        vertexShaderID = 0;
        fragmentShaderID = 0;
        collection = 0;
        fileName = "";
    }
    
    public Shader(String name, String[] attributes, String[] uniforms, int collection ){
        bind();
        this.collection = collection;
        // Load the shader from client files
        fileName = name;
        uniformNames = uniforms;
        attributeNames = attributes;
        vertexShaderID = glCreateShader(GL_VERTEX_SHADER);
        fragmentShaderID = glCreateShader(GL_FRAGMENT_SHADER);
        String sep = System.getProperty("file.separator");
        loadFromFile("assets"+sep+"shaders"+sep+"vert"+sep+""+name+".vert", vertexShaderID);
        loadFromFile("assets"+sep+"shaders"+sep+"frag"+sep+""+name+".frag",fragmentShaderID);
        
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
    
    public void uniformMatrix4fArray(int uniformID, float[] matrices) {
        glUniformMatrix4fv(uniform[uniformID], false, matrices);
        return;
    }
    
    public static void uniformAllMatrix4f(int uniformID, Matrix4f matrix, int collectionMask){
        for(Shader shader:loadedShaders){
            if((shader.collection & collectionMask) == shader.collection){
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
    
    public void recompile(String newFileName){
        if(newFileName != null){
            fileName = newFileName;
        }
        
            String sep = System.getProperty("file.separator");
            loadFromFile("assets"+sep+"shaders"+sep+"vert"+sep+""+fileName+".vert", vertexShaderID);
            loadFromFile("assets"+sep+"shaders"+sep+"frag"+sep+""+fileName+".frag",fragmentShaderID);
            
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
            shader.recompile(null);
        }
    }
    
    public static void initDefaultShaders(){
        
     // Default Shader
    defaultShader = new Shader(
                "default",
                new String[]{"pos", "uv", "normal", "vertex_color"},
                new String[]{"transform", "camera", "perspective", "diffuse", "specular", "world_exponent", "gloss", "zenith", "horizon", "albedo", "fog_density", "fog_exponent"},
                Shader.COLLECTION_3D
        ) {
            public Vector3f diffuse = new Vector3f(.8f, .7f, .7f);
            public float specular = 1f,
                    gloss = 10f;

            @Override
            public void loadUniformSettings() {
                uniformVector3f(Shader.DIFFUSE, diffuse);
                uniformFloat(Shader.SPECULAR, specular);
                uniformFloat(Shader.GLOSS, gloss);
            }

        };
    
    // Animated Default Shader
    animatedShader = new Shader(
                "animated",
                new String[]{"pos", "uv", "normal", "vertex_color", "joint", "weight"},
                new String[]{"transform", "camera", "perspective", "diffuse", "specular", "world_exponent", "gloss", "zenith", "horizon", "albedo", "fog_density", "fog_exponent", "joints"},
                Shader.COLLECTION_3D
        ) {
            public Vector3f diffuse = new Vector3f(.7f, .7f, .7f);
            public float specular = 1f,
                    gloss = 10f;

            @Override
            public void loadUniformSettings() {
                uniformVector3f(Shader.DIFFUSE, diffuse);
                uniformFloat(Shader.SPECULAR, specular);
                uniformFloat(Shader.GLOSS, gloss);
            }

        };
    }
    
}
