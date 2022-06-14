package core.animation;

import java.util.ArrayList;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Armature {
    
    ArmatureInfo info;
    private float[] jointTransforms;
    private Joint[] joints;
    private float timeMultiplier = 1f;
    float time = 0;
    Animation currentAnimation;
    
    public Armature(int jointCount, ArmatureInfo info){
        this.info = info;
        jointTransforms = new float[jointCount*16];
        joints = new Joint[jointCount];
        for(int i=0; i<jointCount; i++){
            joints[i] = new Joint(i);
        }
        update();
    }
    
    Armature(Armature a, ArmatureInfo info){
        this.info = info;
        jointTransforms = new float[a.jointTransforms.length];
        joints = new Joint[a.joints.length];
        for(int i=0; i<joints.length; i++){
            joints[i] =  new Joint(a.joints[i]);
        }
        update();
    }
    
    public Joint getJoint(int index){
        return joints[index];
    }
    
    public float[] getJointTransforms(){
        return jointTransforms;
    }
    
    public void update(){
        for(Joint j:joints){
            j.calculateTransform();
        }
    }
    
    public void forceUpdate(){
         for(Joint j:joints){
            j.markUpdated();
        }
        update();
    }
    
    public void parentJoint(int parent, int child){
        if((parent != -1 && child != -1) && parent!=child){
            joints[child].parent = parent;
            if(!joints[parent].children.contains(child)){
                joints[parent].children.add(child);
            }
        }
    }
    
    public void printHierarchy(){
        for(Joint j:joints){
            System.out.println(j.ID+": "+j.children.toString());
        }
    }
    
    
    public void resetAnimation(){
        for(Joint j:joints){
           j.pos.set(0);
           j.rot.set(0,0,0,1);
           j.scale.set(0);
        }
        setTime(0);
    }
    
    public void setTime(float time){
        this.time = time;
        if(currentAnimation != null){
            currentAnimation.deformArmature(this, time);
        }
    }
    
    public float getTime(){
        return time;
    }
    
    public void addTime(float t){
        this.time += t*timeMultiplier;
        if(currentAnimation != null){
            currentAnimation.deformArmature(this, time);
        }
    }
    
    public void setAnimation(String name){
        currentAnimation = info.getAnimation(name);
    }
    
    public void setTimeMultiplier(float t){
        timeMultiplier = t;
    }
    
    class Joint{
        
        // A temporary matrix to store results
        private static Matrix4f tempTransform = new Matrix4f();
        
        // Determines if all child joints need to update their transform
        private boolean updated = false;
        
        // Position, Rotation, and Scale keyframes in local Parent Bone Space
        Vector3f pos = new Vector3f(0.0f);
        Quaternionf rot = new Quaternionf();
        Vector3f scale = new Vector3f(1.0f);
        
        // The joint ID (Position in Joint Array)
        int ID = -1;
        
        // The parent ID (Position in Joint Array)
        int parent = -1;
        
        // List of Children indices to update
        ArrayList<Integer> children = new ArrayList<>();
        
        // The inverse matrix of the joint's resting transform, used to calculate the keyframe transform
        Matrix4f inverseRestTransform = new Matrix4f();;
        Matrix4f restTransform = new Matrix4f();
        
        // The current object-space transform of the matrix
        // This value is equivalent to the product of all parent transforms
        Matrix4f transform = new Matrix4f();
        

        Joint(int ID){
            this.ID = ID;
        }
        
        Joint(Joint j){
            updated = j.updated;
            j.pos.get(pos);
            j.rot.get(rot);
            j.scale.get(scale);
            ID = j.ID;
            parent = j.parent;
            children = new ArrayList<>(j.children.size());
            for(int i=0; i<j.children.size(); i++){
                children.add(j.children.get(i));
            }
            j.transform.get(transform);
            j.restTransform.get(restTransform);
            j.inverseRestTransform.get(inverseRestTransform);
            
        }
        
        /**
         * Uses a rest transformation of the joint in object space to create the inverse resting position.
         * @param restTransform The resting transform in object space of the joint.
         */
        public void setRestTransform(Matrix4f restTransform){
            this.restTransform.set(restTransform);
            inverseRestTransform.set(restTransform);
            inverseRestTransform.invert();
        }
        
        public void setLocalTransform(Matrix4f localTransform){
            localTransform.getTranslation(pos);
            localTransform.getScale(scale);
            rot.setFromNormalized(localTransform);
        }
        
        public Matrix4f getTransform(){
            return transform;
        }
        
        void lerpRot(float val, Quaternionf a, Quaternionf b){
            a.slerp(b, val, rot);
        }
        
        void lerpPos(float val, Vector3f a, Vector3f b){
            a.lerp(b, val, pos);
        }
        
        void lerpScale(float val, Vector3f a, Vector3f b){
            a.lerp(b, val, scale);
        }
        
        void markUpdated(){
           updated = true;
           for(Integer child:children){
               joints[child].markUpdated();
           }
        }
        
        void calculateTransform(){
            // Update the transform if necessary
            if(updated){
                updated = false;
                
                // If the joint has a parent, ensure the parent is updated
                if(parent!=-1){
                    joints[parent].calculateTransform();
                    transform.translation(pos).scale(scale).rotate(rot);
                    joints[parent].getTransform().mul(transform,transform);
                }
                else{   
                    transform.translation(pos).scale(scale).rotate(rot);
                }
                
                // Place the transform relative to the rest transform
                transform.mul(inverseRestTransform,tempTransform);
                tempTransform.get(jointTransforms, 16*ID);
            }
        }
    }
    
}


