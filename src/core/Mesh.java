package core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import org.joml.Vector3f;
import static org.lwjgl.opengl.GL30.*;

public class Mesh {

    // Static
    public static final int 
        ATTRB_POS = 0,
        ATTRB_UV = 1,
        ATTRB_NORM = 2,
        ATTRB_COL = 3,
        ATTRB_JOINT = 4,
        ATTRB_WEIGHT = 5,
        ATTRIBUTE_COUNT = 6;
    
    private static final int[] sizes = new int[]{3,2,3,3,3,3};
    
    
    private static final ArrayList<Mesh> loadedMeshes = new ArrayList<>();
    
    // Mesh Properties
    private int VAO;
    private int[] VBO;
    private int indexVBO;
    private int indexCount;
    
    
    /**
     * Creates an empty mesh
     */
    public Mesh(){
        VAO = 0;
        VBO = new int[ATTRIBUTE_COUNT];
        for(int i=0;i<VBO.length;i++){
            VBO[i]=0;
        }
        indexVBO = 0;
    }
    
    public Mesh(int[] index, float[] pos, float[] uv, float[] normal, float[] col){
        VAO = 0;
        VBO = new int[ATTRIBUTE_COUNT];
        for(int i=0;i<VBO.length;i++){
            VBO[i]=0;
        }
        indexVBO = 0;
        
        this.allocate();
        if(pos!=null)
            this.loadAttributeFloat(ATTRB_POS, pos);
        
        if(uv!=null)
        this.loadAttributeFloat(ATTRB_UV, uv);
        
        if(normal!=null)
        this.loadAttributeFloat(ATTRB_NORM, normal);
        
        if(col!=null)
        this.loadAttributeFloat(ATTRB_COL, col);
        
        if(index!=null)
        this.loadIndex(index);
      
    }
    
    /**
     * 
     * @param path
     */
    public Mesh(String path){
        VAO = 0;
        VBO = new int[ATTRIBUTE_COUNT];
        for(int i=0;i<VBO.length;i++){
            VBO[i]=0;
        }
        indexVBO = 0;
        allocate();
        try {
            loadFromPLY(path);
        } catch (IOException ex) {
           System.err.println("Failed to load mesh "+ path);
        }
    }
    
    /**
     * Creates new memory for the mesh
     */
    public void allocate(){
        if( VAO == 0){
            VAO = glGenVertexArrays();
            loadedMeshes.add(this);
            glBindVertexArray(VAO);
            
            if(indexVBO == 0){
                indexVBO = glGenBuffers();
            }
        }
    }
    
    public void loadAttributeFloat(int ID, float[] attribute) {
        if (VAO != 0 && ID < ATTRIBUTE_COUNT) {
            glBindVertexArray(VAO);
            if (VBO[ID] == 0) {
                VBO[ID] = glGenBuffers();
            }
            glBindBuffer(GL_ARRAY_BUFFER, VBO[ID]);
            glBufferData(GL_ARRAY_BUFFER, attribute, GL_STATIC_DRAW);
            glVertexAttribPointer(ID, sizes[ID], GL_FLOAT, false, 0, 0);
        }

    }

    public void loadSubAttributeFloat(int ID, float[] attribute, long byteOffset) {
        if (VAO != 0 && ID < ATTRIBUTE_COUNT) {
            glBindVertexArray(VAO);
            if (VBO[ID] == 0) {
                VBO[ID] = glGenBuffers();
            }
            glBindBuffer(GL_ARRAY_BUFFER, VBO[ID]);
            glBufferSubData(GL_ARRAY_BUFFER, byteOffset, attribute);
            glVertexAttribPointer(ID, sizes[ID], GL_FLOAT, false, 0, 0);
        }
    }

    public void loadAttributeInt(int ID, int[] attribute) {
        if (VAO != 0 && ID < ATTRIBUTE_COUNT) {
            glBindVertexArray(VAO);
            if (VBO[ID] == 0) {
                VBO[ID] = glGenBuffers();
            }
            glBindBuffer(GL_ARRAY_BUFFER, VBO[ID]);
            glBufferData(GL_ARRAY_BUFFER, attribute, GL_STATIC_DRAW);
            glVertexAttribIPointer(ID, sizes[ID], GL_INT, 0, 0);
        }

    }

    public void loadSubAttributeInt(int ID, int[] attribute, long byteOffset) {
        if (VAO != 0 && ID < ATTRIBUTE_COUNT) {
            glBindVertexArray(VAO);
            if (VBO[ID] == 0) {
                VBO[ID] = glGenBuffers();
            }
            glBindBuffer(GL_ARRAY_BUFFER, VBO[ID]);
            glBufferSubData(GL_ARRAY_BUFFER, byteOffset, attribute);
            glVertexAttribIPointer(ID, sizes[ID], GL_FLOAT, 0, 0);
        }
    }
    
    public void loadIndex(int[] index){
        if( VAO != 0 && indexVBO != 0){
            glBindVertexArray(VAO);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexVBO);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, index, GL_STATIC_DRAW);
            indexCount = index.length;
        }
    }
    
    public void loadSubIndex(int[] index, long byteOffset){
        if( VAO != 0 && indexVBO != 0){
            glBindVertexArray(VAO);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexVBO);
            glBufferSubData(GL_ELEMENT_ARRAY_BUFFER, byteOffset, index);
            indexCount = index.length;
        }
    }
    
    public void bind() {
        if( VAO != 0){
            glBindVertexArray(VAO);
            for(int i = 0; i < VBO.length; i++){
                if( VBO[i] != 0)
                    glEnableVertexAttribArray(i);
                else
                    glDisableVertexAttribArray(i);
            }
        }
    }
    
    public boolean hasAttribute(int attribute){
        if(attribute>VBO.length)
            return false;
        return VBO[attribute]!=0;
    }
    
    public int getIndexCount() {
        return indexCount;
    }
    
    public int getVAO(){
        return VAO;
    }
    
    public void free(){
        if( VAO != 0){
            
            // Free VBOs
             glBindVertexArray(VAO);
             
             for(int i = 0; i < VBO.length; i++){
                 if( VBO[i] != 0){
                    glDeleteBuffers(VBO[i]);
                    VBO[i] = 0;
                 }
             }
             
             if(indexVBO == 0){
                 glDeleteBuffers(indexVBO);
             }
             
            // Free VAO
            glDeleteVertexArrays(VAO);
            VAO = 0;
            
            // Remove reference from loaded list
            loadedMeshes.remove(this);
        }
    }
    
    /**
     * Deletes all loaded VAOs
     */
    public static void freeAll(){
        for( int i = loadedMeshes.size() - 1; i >= 0; i--){
            loadedMeshes.get(i).free();
        }
    }
    
    
    protected void loadFromPLY(String fileName) throws FileNotFoundException, IOException{
        
        // open the file
        String sep = System.getProperty("file.separator");
        File file = new File("assets" + sep + "models"+ sep + fileName + ".ply");
        
        // create variables to store properties of the mesh
        boolean hasUV = false;
        boolean hasCol = false;
        boolean hasNormal = false;
        
        // read the properties
        int vert_count = 0, face_count = 0;
        int header_size = 1;
        BufferedReader prop_reader = new BufferedReader(new FileReader(file));
        String property = prop_reader.readLine();
        
        while(!property.equals("end_header")){
            
            String[] tokens = property.split(" ");
            
            if(tokens[0].equals("property")){
                if(tokens[2].matches("n.")){
                    hasNormal = true;
                }
                else if(tokens[2].matches("(red)|(green)|(blue)|(alpha)")){
                    hasCol = true;
                }
                else if(tokens[2].matches("u.")){
                    hasUV = true;
                }
            }
            // parse the number of elements
            else if(tokens[0].equals("element")){
                if(tokens[1].equals("vertex")){
                    vert_count = Integer.parseInt(tokens[2]);
                }
                else if(tokens[1].equals("face")){
                    face_count = Integer.parseInt(tokens[2]);
                }
            }
            // all other values such as comments will be ignored
            
            property = prop_reader.readLine();
            header_size++;
        }
        
        // create arrays now that size is known
        int[] faces = new int[face_count * 3];
        float[] pos = new float[vert_count * 3];
        float[] norm = new float[vert_count * 3];
        float[] rgb = new float[vert_count * 3];
        
        
        // create the input stream and the buffer to read into
        int vert_attrb = 20; // 6 floats + 4 char = 6*4 + 4 = 20 bytes per vertex
        InputStream input = new FileInputStream(file);
        
        // skip over the header
        int read_lines = 0;
        int read_byte;
        byte[] buffer4 = new byte[4];
        while(read_lines < header_size){
            read_byte = input.read();
            if(read_byte == '\n'){
                read_lines++;
            }
        }
        
        // read the vertex values
        for(int i=0; i<vert_count;i++){
            // float x
            input.read(buffer4);
            pos[i * 3] =  ByteBuffer.wrap(buffer4).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                    
            // float y
            input.read(buffer4);
            pos[i * 3 + 1] =  ByteBuffer.wrap(buffer4).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                    
            // float z
            input.read(buffer4);
            pos[i * 3 + 2] =  ByteBuffer.wrap(buffer4).order(ByteOrder.LITTLE_ENDIAN).getFloat();
            
            if(hasUV){

                // float u
                input.read(buffer4);
                pos[i * 2] =  ByteBuffer.wrap(buffer4).order(ByteOrder.LITTLE_ENDIAN).getFloat();

                // float v
                input.read(buffer4);
                pos[i * 2 + 1] =  ByteBuffer.wrap(buffer4).order(ByteOrder.LITTLE_ENDIAN).getFloat();
            }
            
            if(hasNormal){
                // float nx
                input.read(buffer4);
                norm[i * 3] =  ByteBuffer.wrap(buffer4).order(ByteOrder.LITTLE_ENDIAN).getFloat();

                // float ny
                input.read(buffer4);
                norm[i * 3 + 1] =  ByteBuffer.wrap(buffer4).order(ByteOrder.LITTLE_ENDIAN).getFloat();

                // float nz
                input.read(buffer4);
                norm[i * 3 + 2] =  ByteBuffer.wrap(buffer4).order(ByteOrder.LITTLE_ENDIAN).getFloat();
            }
            
            if(hasCol){
                // byte r
                read_byte = input.read();
                rgb[i * 3] = read_byte/255f;

                // byte g
                read_byte = input.read();
                rgb[i * 3 + 1] = read_byte/255f;

                // byte b
                read_byte = input.read();
                rgb[i * 3 + 2] = read_byte/255f;

                // ignore byte a
                input.read();
            }
            
        }
        
        // read face values
        for(int i=0; i<face_count; i++){
            read_byte = input.read();
            if(read_byte != 3){
                System.err.println("Mesh is not triangulate in file " + fileName);
                System.err.println("Face with " + read_byte + " edges detected at index " + i);
                System.err.println("Make sure your model is triangulated and does not include UV data.");
                System.exit(0);
            }
            
            // int vertex 1
            input.read(buffer4);
            faces[i * 3] =  ByteBuffer.wrap(buffer4).order(ByteOrder.LITTLE_ENDIAN).getInt();
            
            // int vertex 2
            input.read(buffer4);
            faces[i * 3 + 1] =  ByteBuffer.wrap(buffer4).order(ByteOrder.LITTLE_ENDIAN).getInt();
            
            // int vertex 3
            input.read(buffer4);
            faces[i * 3 + 2] =  ByteBuffer.wrap(buffer4).order(ByteOrder.LITTLE_ENDIAN).getInt();
            
        }
        
        // load mesh
        loadAttributeFloat(ATTRB_POS, pos);
        if(hasNormal)
            loadAttributeFloat(ATTRB_NORM, norm);
        if(hasCol)
            loadAttributeFloat(ATTRB_COL, rgb);
        loadIndex(faces);

    }
    
    public void smoothNormals(float[] points, int[] order){
       float normals[] = new float[points.length];
       int normalCount[] = new int[points.length/3];
       
        
        Vector3f a = new Vector3f();
        Vector3f b = new Vector3f();
        Vector3f c = new Vector3f();
        
        //find the normal of each triangle
        for(int i=0;i<order.length;i+=3){
            a.set(points[3 * order[i]], points[3 * order[i] + 1], points[3 * order[i] + 2]);
            b.set(points[3 * order[i + 1]], points[3 * order[i + 1] + 1], points[3 * order[i + 1] + 2]);
            c.set(points[3 * order[i + 2]], points[3 * order[i + 2] + 1], points[3 * order[i + 2] + 2]);
            
            b.sub(a);
            c.sub(a);
            b.cross(c,a);
            a.normalize();
            
            //store the normal of this triangle in each of the individual point's normals
            //add the normals, they will be divided after the loop completes
            
            //point a
            normals[3*order[i]]+=a.x;
            normals[3*order[i]+1]+=a.y;
            normals[3*order[i]+2]+=a.z;
            //point b
            normals[3*order[i+1]]+=a.x;
            normals[3*order[i+1]+1]+=a.y;
            normals[3*order[i+1]+2]+=a.z;
            //point c
            normals[3*order[i+2]]+=a.x;
            normals[3*order[i+2]+1]+=a.y;
            normals[3*order[i+2]+2]+=a.z;
            
            //increment each points normal count
            normalCount[order[i]]++;
            normalCount[order[i+1]]++;
            normalCount[order[i+2]]++;
        }
        
        //divide the normals
        int count = 0;
        for (int i = 0; i < normals.length; i += 3) {
            count = normalCount[i / 3];
            normals[i] /= count;
            normals[i + 1] /= count;
            normals[i + 2] /= count;
        }
        
        loadAttributeFloat(ATTRB_NORM, normals);
    }
}
