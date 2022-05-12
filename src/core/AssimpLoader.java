package core;

import core.animation.Animation;
import core.animation.ArmatureInfo;
import core.animation.Channel;
import core.animation.KeyframeQuat;
import core.animation.KeyframeVec;
import java.nio.IntBuffer;
import java.util.ArrayList;
import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIAnimation;
import org.lwjgl.assimp.AIBone;
import org.lwjgl.assimp.AIColor4D;
import org.lwjgl.assimp.AIFace;
import org.lwjgl.assimp.AIMatrix4x4;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AINode;
import org.lwjgl.assimp.AINodeAnim;
import org.lwjgl.assimp.AIQuatKey;
import org.lwjgl.assimp.AIQuaternion;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AIVector3D;
import org.lwjgl.assimp.AIVectorKey;
import org.lwjgl.assimp.AIVertexWeight;
import org.lwjgl.assimp.Assimp;

public class AssimpLoader {
    
    public static void loadFromFBX(String filename){
        String sep = System.getProperty("file.separator");
        filename = "assets" + sep + "models" + sep + filename + ".fbx";
        
        AIScene aiScene = Assimp.aiImportFile(filename,0);
        if(aiScene == null){
            System.err.println("Failed to load " + filename);
            return;
        }
        
        int meshCount = aiScene.mNumMeshes();
        int animationCount = aiScene.mNumAnimations();
        PointerBuffer aiMeshes = aiScene.mMeshes();
        PointerBuffer aiAnimations = aiScene.mAnimations();
        
        // Load Hierarchy
        loadHierarchy(aiScene.mRootNode());
        
        // Load Animations
        AIAnimation aiAnimation;
        Animation animation;
        String[] name;
        ArmatureInfo armature;
        
        for(int i=0; i<animationCount; i++){
            aiAnimation = AIAnimation.create(aiAnimations.get(i));
            System.out.println("Animation:"+aiAnimation.mName().dataString());
            animation = new Animation();
            name = aiAnimation.mName().dataString().split("\\|");
            
            // Ensures Name is correct size
            if(name.length==3){
                
                // Looks up the armature allocated when loading hierarchy
                armature = AssetManager.getArmature(name[1]);
                
                // Loads the animation with valid name and links it to the armature
                if(armature != null){
                    loadAnimation(aiAnimation, animation, armature);
                    armature.addAnimation(name[2], animation);
                }
                
            }
        }
        
        // Load Meshes
        AIMesh aiMesh;
        Mesh mesh;
        for(int i=0; i<meshCount; i++){
            aiMesh = AIMesh.create(aiMeshes.get(i));
            mesh = new Mesh();
            loadMesh(aiMesh, mesh);
            AssetManager.addMesh(aiMesh.mName().dataString(), mesh);
            System.out.println("Mesh: "+ aiMesh.mName().dataString());
        }
        aiScene.close();
    }
    
    private static void loadMesh(AIMesh aiMesh, Mesh mesh){
        
        ArrayList<Float> pos = new ArrayList<>();
        ArrayList<Float> uv = new ArrayList<>();
        ArrayList<Float> norm = new ArrayList<>();
        ArrayList<Float> col = new ArrayList<>();
        ArrayList<Integer> index = new ArrayList<>();
        ArrayList<Vector3f> vWeights;
        ArrayList<Vector3i> vJoints;
        AIVector3D  v3;
        
        // Positions
        AIVector3D.Buffer vertices = aiMesh.mVertices();
        while(vertices.remaining()>0){
            v3 = vertices.get();
            pos.add(v3.x());
            pos.add(v3.y());
            pos.add(v3.z());
            
        }
        
        // UVs
        AIVector3D.Buffer textureCoords = aiMesh.mTextureCoords(0);
        if(textureCoords != null){
             while(textureCoords.remaining()>0){
                v3 = textureCoords.get();
                uv.add(v3.x());
                uv.add(v3.y());

            }
        }
         
        // Normals
        AIVector3D.Buffer normals = aiMesh.mNormals();
        if(normals != null){
             while(normals.remaining()>0){
                v3 = normals.get();
                norm.add(v3.x());
                norm.add(v3.y());
                norm.add(v3.z());
            }
        }
        
        // Colors
        AIColor4D.Buffer colors = aiMesh.mColors(0);
        AIColor4D  c4;
        if(colors != null){
            while(colors.remaining()>0){
                c4 = colors.get();
                col.add(c4.r());
                col.add(c4.g());
                col.add(c4.b());
            }
        }
        
        // Index
        AIFace.Buffer faces = aiMesh.mFaces();
        AIFace face;
        IntBuffer indices;
        while(faces.remaining()>0){
            face = faces.get();
            indices = face.mIndices();
            index.add(indices.get(0));
            index.add(indices.get(1));
            index.add(indices.get(2));
        }
        
        // Joints and Weights
        PointerBuffer aiBones = aiMesh.mBones();
        vWeights = new ArrayList<>(aiMesh.mNumVertices());
        vJoints = new ArrayList<>(aiMesh.mNumVertices());
        ArmatureInfo armature = AssetManager.getArmature(aiMesh.mName().dataString());
        if (aiBones != null && armature!=null) {
            for (int i = 0; i < aiMesh.mNumVertices(); i++) {
                vWeights.add(new Vector3f(0.0f));
                vJoints.add(new Vector3i(-1));
            }
            
            AIBone aiBone;
            AIVertexWeight.Buffer aiWeights;
            AIVertexWeight aiWeight;
            Vector3f tempWeight;
            Vector3i tempJoint;
            int minComponent = 0;

            // For each Joint
            while (aiBones.remaining() > 0) {
                aiBone = AIBone.create(aiBones.get());
                aiWeights = aiBone.mWeights();

                // For each Weight in a Joint
                while (aiWeights.remaining() > 0) {
                    aiWeight = aiWeights.get();
                    tempWeight = vWeights.get(aiWeight.mVertexId());
                    minComponent = tempWeight.minComponent();
                    if (tempWeight.get(minComponent) < aiWeight.mWeight()) {
                        tempWeight.setComponent(minComponent, aiWeight.mWeight());
                        tempJoint = vJoints.get(aiWeight.mVertexId());
                        tempJoint.setComponent(minComponent, armature.getJointID(aiBone.mName().dataString()));
                    }

                }

            }
        }
        
        // Declare Arrays
        float[] posArray = new float[pos.size()];
        float[] uvArray = new float[uv.size()];
        float[] normArray = new float[norm.size()];
        float[] colArray = new float[col.size()];
        float[] weightArray = new float[vWeights.size()*3];
        int[] jointArray = new int[vJoints.size()*3];
        int[] indexArray = new int[index.size()];
        
        // Convert to Arrays
        for(int i=0; i<posArray.length; i++){
            posArray[i] = pos.get(i);
        }
        for(int i=0; i<uvArray.length; i++){
            uvArray[i] = uv.get(i);
        }
        for(int i=0; i<normArray.length; i++){
            normArray[i] = norm.get(i);
        }
        for(int i=0; i<colArray.length; i++){
            colArray[i] = col.get(i);
        }
        for(int i=0; i<indexArray.length; i++){
            indexArray[i] = index.get(i);
        }
        Vector3f weight;
        for(int i=0; i<vWeights.size(); i++){
            
            weight = vWeights.get(i);
            weight.div(weight.x+weight.y+weight.z);
            weightArray[i*3] = weight.x;
            weightArray[i*3+1] = weight.y;
            weightArray[i*3+2] = weight.z;
        }
        for(int i=0; i<vJoints.size(); i++){
            jointArray[i*3] = vJoints.get(i).x;
            jointArray[i*3+1] = vJoints.get(i).y;
            jointArray[i*3+2] = vJoints.get(i).z;
        }
        
        
        // Load to Mesh
        mesh.allocate();
        if(posArray.length>0)
            mesh.loadAttributeFloat(Mesh.ATTRB_POS, posArray);
        if(uvArray.length>0)
            mesh.loadAttributeFloat(Mesh.ATTRB_UV, uvArray);
        if(normArray.length>0)
            mesh.loadAttributeFloat(Mesh.ATTRB_NORM, normArray);
        if(colArray.length>0)
            mesh.loadAttributeFloat(Mesh.ATTRB_COL, colArray);
        if(weightArray.length>0)
            mesh.loadAttributeFloat(Mesh.ATTRB_WEIGHT, weightArray);
        if(jointArray.length>0)
            mesh.loadAttributeInt(Mesh.ATTRB_JOINT, jointArray);
        mesh.loadIndex(indexArray);
        
    }
    
    private static void loadAnimation(AIAnimation aiAnim, Animation anim, ArmatureInfo armature){
        
        // Animation Channels
        PointerBuffer aiChannels = aiAnim.mChannels();
        
        // Temporary Variable Definitions
        AIVectorKey.Buffer posKeys, scaleKeys;
        AIQuatKey.Buffer rotKeys;
        AIVectorKey keyVec;
        AIQuatKey keyQuat;
        AIVector3D aiVec;
        Vector3f vec;
        AIQuaternion aiQuat;
        Quaternionf quat;
        Channel channel;
        
        anim.setDuration((float)aiAnim.mDuration());
        anim.setSpeed((float)aiAnim.mTicksPerSecond());
        
        // For each Channel
        for(int i=0; i<aiAnim.mNumChannels();i++){
            AINodeAnim aiNode = AINodeAnim.create(aiChannels.get(i));
            String jointName = aiNode.mNodeName().dataString();
            channel = new Channel(armature.getJointID(jointName),aiNode.mNumPositionKeys(), aiNode.mNumRotationKeys(), aiNode.mNumScalingKeys());
            
            // For each Position Keyframe
            posKeys = aiNode.mPositionKeys();
            while(posKeys.remaining()>0){
                keyVec = posKeys.get();
                aiVec = keyVec.mValue();
                vec = new Vector3f(aiVec.x(),aiVec.y(),aiVec.z());
                channel.addPosKeyframe(new KeyframeVec(vec, (float)keyVec.mTime()));
            }
            
            // For each Rotation Keyframe
            rotKeys = aiNode.mRotationKeys();
            while(rotKeys.remaining()>0){
                keyQuat = rotKeys.get();
                aiQuat = keyQuat.mValue();
                quat = new Quaternionf(aiQuat.x(),aiQuat.y(),aiQuat.z(),aiQuat.w());
                quat.normalize();
                channel.addRotKeyframe(new KeyframeQuat(quat, (float)keyQuat.mTime()));
            }
            
            // For each Scale Keyframe
            scaleKeys = aiNode.mScalingKeys();
            while(scaleKeys.remaining()>0){
                keyVec = scaleKeys.get();
                aiVec = keyVec.mValue();
                vec = new Vector3f(aiVec.x(),aiVec.y(),aiVec.z());
                channel.addScaleKeyframe(new KeyframeVec(vec, (float)keyVec.mTime()));
            }
            
            // Add Channel to Animation
            anim.addChannel(channel);
        }
    }
    
    private static void loadHierarchy(AINode root){
        nodeRecursion(root,0);
    }
    
    private static void nodeRecursion(AINode node, int level){
        //System.out.println('|'+"-".repeat(level)+"NODE:"+level+' '+node.mName().dataString()+' '+getTotalChildren(node));
        
        // Determine Node Type
        String[] name = node.mName().dataString().split("\\|");
        
        // Armature
        if(name[0].equals("Armature") && name.length==2){
            
            
            
            // Assume all children are Joints
            ArmatureInfo armature = new ArmatureInfo(name[1], getTotalChildren(node));
            AssetManager.addArmature(name[1],armature );
            AINode child;
            
            for (int i = 0; i < node.mNumChildren(); i++) {
                    child = AINode.create(node.mChildren().get(i));
                    buildJointHierarchy(child, armature, new Matrix4f());
            }
        }
        
        // Mesh, Empty, Etc.
        else {
            if (node.mNumChildren() > 0) {
                AINode child;
                for (int i = 0; i < node.mNumChildren(); i++) {
                    child = AINode.create(node.mChildren().get(i));
                    nodeRecursion(child, level + 1);
                }
            }
        }
        
        
    }
    
    private static int getTotalChildren(AINode node){
        if(node.mNumChildren()>0){
            AINode child;
            int sum = node.mNumChildren();
            for(int i=0; i<node.mNumChildren(); i++){
                child = AINode.create(node.mChildren().get(i));
                sum+= getTotalChildren(child);
            }
           return sum;
        }
        return 0;
    }
    
    private static void buildJointHierarchy(AINode node, ArmatureInfo armature, Matrix4f parentTransform) {
        String name = node.mName().dataString();
        AIMatrix4x4 a = node.mTransformation();
        Matrix4f localTransform = new Matrix4f();
        localTransform.set(a.a1(), a.b1(), a.c1(), a.d1(), a.a2(), a.b2(), a.c2(), a.d2(), a.a3(), a.b3(), a.c3(), a.d3(), a.a4(), a.b4(), a.c4(), a.d4());
        
        // Create the rest transform for the joint in object space
        Matrix4f restTransform = parentTransform.mulAffine(localTransform, new Matrix4f());
        armature.initJoint(name, restTransform, localTransform);
        //System.out.println(name + ": "+restTransform.toString());
        //System.out.println(name + " Rot: "+new Quaternionf().setFromUnnormalized(localSpace).toString());
        System.out.println(name + " local transform:\n"+localTransform.toString());
        
        if (node.mNumChildren() > 0) {
            AINode child;
            for (int i = 0; i < node.mNumChildren(); i++) {
                child = AINode.create(node.mChildren().get(i));
                buildJointHierarchy(child, armature, restTransform);
                armature.parentJoint(name, child.mName().dataString());
            }
        }
    }
}
