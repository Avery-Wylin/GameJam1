package core.animation;

import core.animation.Armature.Joint;
import java.util.ArrayList;
import org.joml.Vector3i;

public class Channel {
    int jointID = -1;
    private ArrayList<KeyframeVec> pos;
    private ArrayList<KeyframeQuat> rot;
    private ArrayList<KeyframeVec> scale;
    
    public Channel(int jointID, int posCount, int rotCount, int scaleCount){
        this.jointID = jointID;
        pos = new ArrayList<>(posCount);
        rot = new ArrayList<>(rotCount);
        scale = new ArrayList<>(scaleCount);
    }
    

    public void addPosKeyframe(KeyframeVec key) {
        pos.add(key);
    }
    
    public void addRotKeyframe(KeyframeQuat key) {
        rot.add(key);
    }
    
    public void addScaleKeyframe(KeyframeVec key) {
        scale.add(key);
    }
    
    public void setJointID(int jointID){
        this.jointID = jointID;
    }
    
    public int getJointID(){
        return jointID;
    }
    
    public void lerpJoint(Joint j, float time){
        KeyframeVec vecA, vecB;
        KeyframeQuat quatA, quatB;
        if(!pos.isEmpty()){
            for(int i=0; i<pos.size()-1; i++){
                vecA = pos.get(i);
                vecB = pos.get(i+1);
                if(vecA.time<=time && vecB.time > time){
                    j.lerpPos((time-vecA.time)/(vecB.time-vecA.time), vecA.val, vecB.val);
                    break;
                }
            }
        }
        
        if(!rot.isEmpty()){
            for(int i=0; i<rot.size()-1; i++){
                quatA = rot.get(i);
                quatB = rot.get(i+1);
                if(quatA.time<=time && quatB.time >time){
                    j.lerpRot((time-quatA.time)/(quatB.time-quatA.time), quatA.val, quatB.val);
                    break;
                }
            }
        }
        
        if(!scale.isEmpty()){
            for(int i=0; i<scale.size()-1; i++){
                vecA = scale.get(i);
                vecB = scale.get(i+1);
                if(vecA.time<=time && vecB.time > time){
                    j.lerpScale((time-vecA.time)/(vecB.time-vecA.time), vecA.val, vecB.val);
                    break;
                }
            }
        }
    }
    
    
}
    

