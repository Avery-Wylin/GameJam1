package core.animation;

import core.animation.Armature.Joint;
import java.util.HashMap;
import org.joml.Matrix4f;

public class ArmatureInfo {
    private String name;
    private HashMap<String,Animation> animations;
    private HashMap<String,Integer> jointNames;
    private Armature defaultArmature;
    private int jointCount;
    
    public ArmatureInfo(String name, int jointCount){
        this.name = name;
        animations = new HashMap<>();
        jointNames = new HashMap<>();
        defaultArmature = new Armature(jointCount, this);
    }
    
    public int initJoint(String name, Matrix4f restTransform, Matrix4f localTransform){
        int size = jointNames.size();
        jointNames.put(name, size);
        defaultArmature.getJoint(size).setRestTransform(restTransform);
        defaultArmature.getJoint(size).setLocalTransform(localTransform);
        defaultArmature.getJoint(size).markUpdated();
        return size;
    }
    
    public int getJointID(String jointName){
        return jointNames.getOrDefault(jointName, -1);
    }
    
    public Matrix4f getJointTransform(int ID){
        return defaultArmature.getJoint(ID).getTransform();
    }
    
    public Armature getArmatureCopy(){
        return new Armature(defaultArmature, this);
    }
    
    public void parentJoint(String parent, String child){
        defaultArmature.parentJoint(jointNames.getOrDefault(parent, -1), jointNames.getOrDefault(child, -1));
    }
    
    public String getName(){
        return name;
    }
    
    public void addAnimation(String animationName, Animation animation){
        animations.put(animationName, animation);
    }
    
    public Animation getAnimation(String animationName){
        return animations.get(animationName);
    }
    

}
